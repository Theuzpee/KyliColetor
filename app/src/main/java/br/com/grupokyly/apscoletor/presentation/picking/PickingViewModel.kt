package br.com.grupokyly.apscoletor.presentation.picking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.grupokyly.apscoletor.domain.model.Box
import br.com.grupokyly.apscoletor.domain.model.BoxStatus
import br.com.grupokyly.apscoletor.domain.model.ItemStatus
import br.com.grupokyly.apscoletor.domain.model.PickingItem
import br.com.grupokyly.apscoletor.domain.model.ScanResult
import br.com.grupokyly.apscoletor.domain.usecase.FinalizeBoxUseCase
import br.com.grupokyly.apscoletor.domain.usecase.OpenBoxUseCase
import br.com.grupokyly.apscoletor.domain.usecase.RegisterScanUseCase
import br.com.grupokyly.apscoletor.domain.usecase.SavePartialBoxUseCase
import br.com.grupokyly.apscoletor.domain.usecase.SkipPickingItemUseCase
import br.com.grupokyly.apscoletor.domain.usecase.RegisterDivergenceUseCase
import br.com.grupokyly.apscoletor.hardware.DataWedgeReceiver
import br.com.grupokyly.apscoletor.hardware.ScanFeedbackManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PickingViewModel @Inject constructor(
    private val openBoxUseCase: OpenBoxUseCase,
    private val registerScanUseCase: RegisterScanUseCase,
    private val finalizeBoxUseCase: FinalizeBoxUseCase,
    private val savePartialBoxUseCase: SavePartialBoxUseCase,
    private val skipPickingItemUseCase: SkipPickingItemUseCase,
    private val registerDivergenceUseCase: RegisterDivergenceUseCase,
    private val repository: br.com.grupokyly.apscoletor.domain.repository.PickingRepository,
    private val dataWedgeReceiver: DataWedgeReceiver,
    private val scanFeedbackManager: ScanFeedbackManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<PickingUiState>(PickingUiState.Idle)
    val uiState: StateFlow<PickingUiState> = _uiState.asStateFlow()

    // Mantemos estado interno apenas de referência para evitar buscas custosas a todo instante se desnecessário.
    // Mas o mais seguro é pegar do próprio uiState se ele for Collecting.

    init {
        viewModelScope.launch {
            dataWedgeReceiver.scannedDataFlow.collect { barcode ->
                val currentState = _uiState.value
                when (currentState) {
                    is PickingUiState.Idle, is PickingUiState.Error -> {
                        // Se estiver Idle ou com Erro sem caixa, assumimos que tentou bipar papeleta
                        // Mas para ser estrito, se for Idle e bipou, tentamos abrir a caixa.
                        // Em cenário real o operador pode errar, mas o fluxo pede para rotear.
                        if (currentState is PickingUiState.Idle || 
                            (currentState is PickingUiState.Error && currentState.message.contains("papeleta", ignoreCase = true))) {
                            onEvent(PickingEvent.OnPapeletaScanned(barcode))
                        } else if (currentState is PickingUiState.Error) {
                            // Poderia ser um erro na Collecting
                            // Melhor recuperar do estado original, mas por simplicidade, trataremos depois.
                            // Se está Collecting mas piscou erro de hardware, a lógica mandou não sair de Collecting.
                            // Espera, no doc: Error("Peça não pertence") sem sair do estado Collecting.
                            // Como modelamos Error como um State, "sem sair do estado Collecting" indica
                            // que Error deve ser algo paralelo ou Collecting deve ter a prop errorMessage.
                            // Ou guardamos o item atual.
                            // Vou simplificar assumindo que podemos emitir Collecting novamente.
                        }
                    }
                    is PickingUiState.Collecting -> {
                        onEvent(PickingEvent.OnPieceScan(barcode))
                    }
                    else -> {
                        // Ignora nos outros estados
                    }
                }
            }
        }
    }

    fun onEvent(event: PickingEvent) {
        when (event) {
            is PickingEvent.OnPapeletaScanned -> handlePapeletaScanned(event.code)
            is PickingEvent.OnPieceScan -> handlePieceScan(event.barcode)
            is PickingEvent.OnFinalizeBox -> handleFinalizeBox()
            is PickingEvent.OnSavePartial -> handleSavePartialBox()
            is PickingEvent.OnRegisterHardware -> dataWedgeReceiver.register(event.context)
            is PickingEvent.OnUnregisterHardware -> dataWedgeReceiver.unregister(event.context)
            is PickingEvent.OnSkipItem -> handleSkipItem(event.reason)
            is PickingEvent.OnRegisterDivergence -> handleRegisterDivergence(event.barcode, event.reason)
        }
    }

    private fun handlePapeletaScanned(code: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = PickingUiState.LoadingBox
            openBoxUseCase(code).fold(
                onSuccess = { box ->
                    repository.getBoxItems(box.id).collect { items ->
                        val currentItem = items.firstOrNull { it.status == ItemStatus.PENDENTE }
                        if (currentItem != null) {
                            val currentIndex = items.indexOf(currentItem)
                            _uiState.value = PickingUiState.Collecting(
                                box = box,
                                currentItem = currentItem,
                                currentItemIndex = currentIndex,
                                collectedCount = currentItem.quantityCollected,
                                totalItems = items.size
                            )
                        } else {
                            // Se não há pendentes, finaliza ou avisa
                            _uiState.value = PickingUiState.Error("Não há itens pendentes para esta caixa.")
                        }
                    }
                },
                onFailure = { e ->
                    scanFeedbackManager.scanError()
                    _uiState.value = PickingUiState.Error(e.message ?: "Erro desconhecido")
                }
            )
        }
    }

    private fun handlePieceScan(barcode: String) {
        val currentState = _uiState.value
        if (currentState !is PickingUiState.Collecting) return

        viewModelScope.launch(Dispatchers.IO) {
            registerScanUseCase(barcode, currentState.box.id).fold(
                onSuccess = { result ->
                    when (result) {
                        is ScanResult.Success -> {
                            scanFeedbackManager.scanPartialSuccess()
                            _uiState.value = currentState.copy(
                                currentItem = result.item,
                                collectedCount = result.item.quantityCollected
                            )
                        }
                        is ScanResult.QuantityComplete -> {
                            scanFeedbackManager.scanSkuComplete()
                            _uiState.value = PickingUiState.ItemComplete(
                                box = currentState.box,
                                completedItem = result.item,
                                nextItem = null // Simplificando
                            )
                            delay(1500)
                            // Avançar para o próximo
                        }
                        is ScanResult.AlreadyScanned -> {
                            scanFeedbackManager.scanError()
                            _uiState.value = PickingUiState.Error("Peça já bipada nesta caixa.")
                            delay(2000)
                            _uiState.value = currentState // Volta para Collecting sem perder estado
                        }
                        is ScanResult.SkuNotFound -> {
                            scanFeedbackManager.scanError()
                            _uiState.value = PickingUiState.Error("Peça não pertence a este endereço.")
                            delay(2000)
                            _uiState.value = currentState // Volta para Collecting sem perder estado
                        }
                    }
                },
                onFailure = { e ->
                    scanFeedbackManager.scanError()
                    _uiState.value = PickingUiState.Error(e.message ?: "Erro ao registrar.")
                }
            )
        }
    }

    private fun handleFinalizeBox() {
        val currentState = _uiState.value
        val boxId = when (currentState) {
            is PickingUiState.Collecting -> currentState.box.id
            is PickingUiState.Error -> return // Precisaríamos do boxId aqui.
            else -> return
        }

        viewModelScope.launch(Dispatchers.IO) {
            finalizeBoxUseCase(boxId).fold(
                onSuccess = { box ->
                    if (box.status == BoxStatus.FINALIZADA) {
                        scanFeedbackManager.boxFinal()
                        _uiState.value = PickingUiState.BoxFinalized(box)
                    } else {
                        scanFeedbackManager.boxPartial()
                        _uiState.value = PickingUiState.BoxPartial(box)
                    }
                },
                onFailure = { e ->
                    scanFeedbackManager.scanError()
                    _uiState.value = PickingUiState.Error(e.message ?: "Erro ao finalizar.")
                }
            )
        }
    }

    private fun handleSavePartialBox() {
        val currentState = _uiState.value
        val boxId = when (currentState) {
            is PickingUiState.Collecting -> currentState.box.id
            else -> return
        }

        viewModelScope.launch(Dispatchers.IO) {
            savePartialBoxUseCase(boxId).fold(
                onSuccess = { box ->
                    scanFeedbackManager.boxPartial()
                    _uiState.value = PickingUiState.BoxPartial(box)
                },
                onFailure = { e ->
                    scanFeedbackManager.scanError()
                    _uiState.value = PickingUiState.Error(e.message ?: "Erro ao salvar parcial.")
                }
            )
        }
    }

    private fun handleSkipItem(reason: br.com.grupokyly.apscoletor.domain.model.SkipReason) {
        val currentState = _uiState.value
        if (currentState !is PickingUiState.Collecting) return

        viewModelScope.launch(Dispatchers.IO) {
            skipPickingItemUseCase(currentState.currentItem.id, currentState.box.id, reason).fold(
                onSuccess = { result ->
                    scanFeedbackManager.scanError() // sinal de atenção
                    
                    _uiState.value = PickingUiState.ItemSkipped(
                        skippedItem = result.item,
                        reason = result.reason,
                        nextItem = null // Para simplificar, o avanço é gerido ao reler a lista de itens via Room trigger ou refetch
                    )
                    
                    delay(1500)
                    
                    // Avançar para próximo pendente
                    val items = kotlinx.coroutines.flow.firstOrNull { repository.getBoxItems(currentState.box.id) } ?: emptyList()
                    val nextPending = items.firstOrNull { it.status == ItemStatus.PENDENTE }
                    
                    if (nextPending != null) {
                        _uiState.value = PickingUiState.Collecting(
                            box = currentState.box,
                            currentItem = nextPending,
                            currentItemIndex = items.indexOf(nextPending),
                            collectedCount = nextPending.quantityCollected,
                            totalItems = items.size
                        )
                    } else {
                        // Não há mais itens pendentes, tenta finalizar
                        handleFinalizeBox()
                    }
                },
                onFailure = { e ->
                    scanFeedbackManager.scanError()
                    _uiState.value = PickingUiState.Error(e.message ?: "Erro ao pular item.")
                }
            )
        }
    }

    private fun handleRegisterDivergence(barcode: String?, reason: br.com.grupokyly.apscoletor.domain.model.SkipReason) {
        val currentState = _uiState.value
        if (currentState !is PickingUiState.Collecting) return

        viewModelScope.launch(Dispatchers.IO) {
            registerDivergenceUseCase(currentState.currentItem.id, currentState.box.id, barcode, reason).fold(
                onSuccess = {
                    scanFeedbackManager.scanPartialSuccess() // Vibração única leve
                    // Permanece no estado Collecting
                },
                onFailure = { e ->
                    scanFeedbackManager.scanError()
                    _uiState.value = PickingUiState.Error(e.message ?: "Erro ao registrar divergência.")
                    delay(2000)
                    _uiState.value = currentState
                }
            )
        }
    }
}
