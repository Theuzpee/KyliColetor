package br.com.grupokyly.apscoletor.presentation.picking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.grupokyly.apscoletor.domain.model.Box
import br.com.grupokyly.apscoletor.domain.model.BoxStatus
import br.com.grupokyly.apscoletor.domain.model.ItemStatus
import br.com.grupokyly.apscoletor.domain.model.PickingItem
import br.com.grupokyly.apscoletor.domain.model.ScanResult
import br.com.grupokyly.apscoletor.domain.usecase.GetBoxItemsUseCase
import br.com.grupokyly.apscoletor.domain.usecase.FinalizeBoxUseCase
import br.com.grupokyly.apscoletor.domain.usecase.OpenBoxUseCase
import br.com.grupokyly.apscoletor.domain.usecase.RegisterScanUseCase
import br.com.grupokyly.apscoletor.domain.usecase.SavePartialBoxUseCase
import br.com.grupokyly.apscoletor.domain.usecase.SaveMultiFloorBoxUseCase
import br.com.grupokyly.apscoletor.domain.usecase.SkipPickingItemUseCase
import br.com.grupokyly.apscoletor.domain.usecase.RegisterDivergenceUseCase
import br.com.grupokyly.apscoletor.domain.validator.AddressValidator
import br.com.grupokyly.apscoletor.hardware.ScannerReceiver
import br.com.grupokyly.apscoletor.domain.hardware.FeedbackProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import br.com.grupokyly.apscoletor.util.Clock
import br.com.grupokyly.apscoletor.util.toTimeString

@HiltViewModel
class PickingViewModel @Inject constructor(
    private val openBoxUseCase: OpenBoxUseCase,
    private val registerScanUseCase: RegisterScanUseCase,
    private val finalizeBoxUseCase: FinalizeBoxUseCase,
    private val savePartialBoxUseCase: SavePartialBoxUseCase,
    private val saveMultiFloorBoxUseCase: SaveMultiFloorBoxUseCase,
    private val skipPickingItemUseCase: SkipPickingItemUseCase,
    private val registerDivergenceUseCase: RegisterDivergenceUseCase,
    private val getBoxItemsUseCase: GetBoxItemsUseCase,
    private val validateAddressUseCase: br.com.grupokyly.apscoletor.domain.usecase.ValidateAddressUseCase,
    private val scannerReceiver: ScannerReceiver,
    private val scanFeedbackManager: FeedbackProvider,
    private val clock: Clock,
    private val sessionManager: br.com.grupokyly.apscoletor.data.local.SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<PickingUiState>(PickingUiState.Idle)
    val uiState: StateFlow<PickingUiState> = _uiState.asStateFlow()
    
    private var currentOperatorName: String = "Operador"

    private fun buildUpdatedHistory(
        currentHistory: List<br.com.grupokyly.apscoletor.domain.model.ScannedPreview>,
        barcode: String
    ): List<br.com.grupokyly.apscoletor.domain.model.ScannedPreview> {
        val newItem = br.com.grupokyly.apscoletor.domain.model.ScannedPreview(
            barcode = barcode,
            time = clock.now().toTimeString()
        )
        return (listOf(newItem) + currentHistory).take(5)
    }

    // Mantemos estado interno apenas de referência para evitar buscas custosas a todo instante se desnecessário.
    // Mas o mais seguro é pegar do próprio uiState se ele for Collecting.

    init {
        viewModelScope.launch {
            sessionManager.sessionFlow.collect { session ->
                if (session != null) {
                    currentOperatorName = session.operatorName
                }
            }
        }
        viewModelScope.launch {
            scannerReceiver.scannedDataFlow.collect { barcode ->
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
                        if (currentState.addressConfirmation == br.com.grupokyly.apscoletor.domain.model.AddressConfirmationState.Pending) {
                            onEvent(PickingEvent.OnAddressScan(barcode))
                        } else if (currentState.addressConfirmation == br.com.grupokyly.apscoletor.domain.model.AddressConfirmationState.Confirmed) {
                            onEvent(PickingEvent.OnPieceScan(barcode))
                        }
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
            is PickingEvent.OnAddressScan -> handleAddressScan(event.barcode)
            is PickingEvent.OnPieceScan -> handlePieceScan(event.barcode)
            is PickingEvent.OnFinalizeBox -> handleFinalizeBox()
            is PickingEvent.OnSavePartial -> handleSavePartialBox()
            is PickingEvent.OnSaveMultiFloor -> handleSaveMultiFloor()
            is PickingEvent.OnRegisterHardware -> scannerReceiver.register(event.context)
            is PickingEvent.OnUnregisterHardware -> scannerReceiver.unregister(event.context)
            is PickingEvent.OnSkipItem -> handleSkipItem(event.reason)
            is PickingEvent.OnRegisterDivergence -> handleRegisterDivergence(event.barcode, event.reason, event.evidencePhotoUrl)
            is PickingEvent.OnDebugScan -> handleDebugScan(event.barcode)
            is PickingEvent.OnAdvanceToNextItem -> handleAdvanceToNextItem()
            is PickingEvent.OnResumeBoxConfirmed -> handleResumeBoxConfirmed()
        }
    }

    private fun handleAdvanceToNextItem() {
        val currentState = _uiState.value
        val (box, prevAddress) = when (currentState) {
            is PickingUiState.ItemComplete -> Pair(currentState.box, currentState.completedItem.address)
            is PickingUiState.ItemSkipped -> Pair(currentState.box, currentState.skippedItem.address)
            else -> return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val items = getBoxItemsUseCase(box.id).firstOrNull() ?: emptyList()
            val nextPending = items.firstOrNull { it.status == ItemStatus.PENDENTE }
            
            if (nextPending != null) {
                val keepAddress = (nextPending.address == prevAddress)
                
                _uiState.value = PickingUiState.Collecting(
                    box = box,
                    currentItem = nextPending,
                    currentItemIndex = items.indexOf(nextPending),
                    collectedCount = nextPending.quantityCollected,
                    totalItems = items.size,
                    lastScannedItems = emptyList(), // zera os lidos quando avança para outro SKU
                    isMultiFloor = box.isReopened,
                    floorLabel = if (box.isReopened) "Continuando coleta — itens pendentes" else "",
                    addressConfirmation = if (keepAddress) br.com.grupokyly.apscoletor.domain.model.AddressConfirmationState.Confirmed else br.com.grupokyly.apscoletor.domain.model.AddressConfirmationState.Pending,
                    operatorName = currentOperatorName
                )
            } else {
                handleFinalizeBoxInternal(box.id, box.createdAt)
            }
        }
    }

    private fun handleDebugScan(barcode: String) {
        val currentState = _uiState.value
        when (currentState) {
            is PickingUiState.Idle, is PickingUiState.Error -> {
                if (currentState is PickingUiState.Idle || 
                    (currentState is PickingUiState.Error && currentState.message.contains("papeleta", ignoreCase = true))) {
                    onEvent(PickingEvent.OnPapeletaScanned(barcode))
                }
            }
            is PickingUiState.Collecting -> {
                if (currentState.addressConfirmation == br.com.grupokyly.apscoletor.domain.model.AddressConfirmationState.Pending) {
                    onEvent(PickingEvent.OnAddressScan(barcode))
                } else if (currentState.addressConfirmation == br.com.grupokyly.apscoletor.domain.model.AddressConfirmationState.Confirmed) {
                    onEvent(PickingEvent.OnPieceScan(barcode))
                }
            }
            else -> {}
        }
    }

    private fun handlePapeletaScanned(code: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = PickingUiState.LoadingBox
            openBoxUseCase(code).fold(
                onSuccess = { box ->
                    // Substituindo collect por firstOrNull para evitar resetar o estado da UI a cada insert no banco (Bug Fix)
                    val items = getBoxItemsUseCase(box.id).firstOrNull() ?: emptyList()
                    val currentItem = items.firstOrNull { it.status == ItemStatus.PENDENTE }
                    if (currentItem != null) {
                        val currentIndex = items.indexOf(currentItem)
                        
                        val completedCount = items.count { it.status == ItemStatus.COMPLETO }
                        val pendingCount = items.count { it.status == ItemStatus.PENDENTE || it.status == ItemStatus.FALTA }
                        val divergencesCount = items.count { it.status == ItemStatus.FALTA }
                        
                        val hasStarted = completedCount > 0 || box.isReopened
                        
                        if (hasStarted) {
                            _uiState.value = PickingUiState.BoxResuming(
                                box = box,
                                collectedItemsCount = completedCount,
                                pendingItemsCount = pendingCount,
                                divergencesCount = divergencesCount,
                                nextAddress = currentItem.address,
                                isMultiFloor = box.isReopened
                            )
                        } else {
                            _uiState.value = PickingUiState.Collecting(
                                box = box,
                                currentItem = currentItem,
                                currentItemIndex = currentIndex,
                                collectedCount = currentItem.quantityCollected,
                                totalItems = items.size,
                                lastScannedItems = emptyList(),
                                isMultiFloor = box.isReopened,
                                floorLabel = if (box.isReopened) "Continuando coleta — itens pendentes" else "",
                                operatorName = currentOperatorName
                            )
                        }
                    } else {
                        // Finaliza direto se a caixa for aberta mas já não tiver itens pendentes
                        handleFinalizeBoxInternal(box.id, box.createdAt)
                    }
                },
                onFailure = { e ->
                    scanFeedbackManager.scanError()
                    _uiState.value = PickingUiState.Error(e.message ?: "Erro desconhecido")
                }
            )
        }
    }

    private fun handleAddressScan(barcode: String) {
        val currentState = _uiState.value
        if (currentState !is PickingUiState.Collecting) return
        if (currentState.addressConfirmation != br.com.grupokyly.apscoletor.domain.model.AddressConfirmationState.Pending) return

        viewModelScope.launch {
            val expectedAddress = currentState.currentItem.address
            val isValid = validateAddressUseCase(barcode, expectedAddress)

            if (isValid) {
                scanFeedbackManager.scanPartialSuccess()
                _uiState.value = currentState.copy(
                    addressConfirmation = br.com.grupokyly.apscoletor.domain.model.AddressConfirmationState.Confirmed
                )
            } else {
                // Inteligência Contextual: Verificar se o erro foi de Corredor
                val isWrongAisle = barcode.isNotEmpty() && expectedAddress.isNotEmpty() &&
                        barcode.first().uppercaseChar() != expectedAddress.first().uppercaseChar()

                if (isWrongAisle) {
                    scanFeedbackManager.scanSequenceError()
                    _uiState.value = PickingUiState.Error("Aviso Contextual: Você está no corredor errado! Esperado: Corredor ${expectedAddress.first().uppercaseChar()}")
                } else {
                    scanFeedbackManager.scanError()
                    _uiState.value = currentState.copy(
                        addressConfirmation = br.com.grupokyly.apscoletor.domain.model.AddressConfirmationState.Error
                    )
                }
                
                delay(3000)
                
                // Reverte estado do endereço ou tira o banner de erro dependendo de onde ele parou
                val currentNow = _uiState.value
                if (currentNow is PickingUiState.Collecting && currentNow.currentItem.id == currentState.currentItem.id) {
                    _uiState.value = currentNow.copy(
                        addressConfirmation = br.com.grupokyly.apscoletor.domain.model.AddressConfirmationState.Pending
                    )
                } else if (currentNow is PickingUiState.Error) {
                    _uiState.value = currentState.copy(
                        addressConfirmation = br.com.grupokyly.apscoletor.domain.model.AddressConfirmationState.Pending
                    )
                }
            }
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
                            
                            val updatedHistory = buildUpdatedHistory(currentState.lastScannedItems, barcode)
                            
                            _uiState.value = currentState.copy(
                                currentItem = result.item,
                                collectedCount = result.item.quantityCollected,
                                lastScannedItems = updatedHistory
                            )
                        }
                        is ScanResult.QuantityComplete -> {
                            scanFeedbackManager.scanSkuComplete()
                            
                            // To keep history when going to next item, it should be passed to next state
                            // val updatedHistory = buildUpdatedHistory(currentState.lastScannedItems, barcode)
                            
                            _uiState.value = PickingUiState.ItemComplete(
                                box = currentState.box,
                                completedItem = currentState.currentItem,
                                nextItem = null // O avanço real agora ocorre via Jetpack Compose
                            )
                        }
                        is ScanResult.AlreadyScanned -> {
                            scanFeedbackManager.scanDuplicateError()
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
                        is ScanResult.ItemSkipped -> {
                            // Normalmente não retornado pelo registerScanUseCase
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
        val (boxId, startedAt) = when (currentState) {
            is PickingUiState.Collecting -> Pair(currentState.box.id, currentState.box.createdAt)
            is PickingUiState.ItemComplete -> Pair(currentState.box.id, currentState.box.createdAt)
            is PickingUiState.Error -> {
                // Tenta extrair a box do last state se existir. Se não, aborta.
                return
            }
            else -> return
        }
        handleFinalizeBoxInternal(boxId, startedAt)
    }

    private fun handleFinalizeBoxInternal(boxId: Long, startedAt: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            finalizeBoxUseCase(boxId).fold(
                onSuccess = { box ->
                    val items = getBoxItemsUseCase(box.id).firstOrNull() ?: emptyList()
                    val collected = items.filter { it.status == ItemStatus.COMPLETO }
                    val totalCollected = collected.sumOf { it.quantityCollected }
                    val totalRequired = items.sumOf { it.quantityRequired }
                    val timeMinutes = ((System.currentTimeMillis() - startedAt) / 60000).toInt()

                    if (box.status == BoxStatus.FINALIZADA) {
                        scanFeedbackManager.boxFinal()
                        _uiState.value = PickingUiState.BoxFinalized(
                            box = box,
                            collectedItems = collected,
                            totalCollected = totalCollected,
                            totalRequired = totalRequired,
                            collectionTimeMinutes = timeMinutes
                        )
                    } else {
                        val pending = items.filter {
                            it.status == ItemStatus.PENDENTE || it.status == ItemStatus.FALTA
                        }
                        scanFeedbackManager.boxPartial()
                        _uiState.value = PickingUiState.BoxPartial(
                            box = box,
                            collectedItems = collected,
                            pendingItems = pending,
                            totalCollected = totalCollected,
                            totalRequired = totalRequired
                        )
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
        val currentState = _uiState.value as? PickingUiState.Collecting ?: return

        viewModelScope.launch(Dispatchers.IO) {
            savePartialBoxUseCase(currentState.box.id).fold(
                onSuccess = { box ->
                    val items = getBoxItemsUseCase(box.id).firstOrNull() ?: emptyList()
                    val collected = items.filter { it.status == ItemStatus.COMPLETO }
                    val pending = items.filter {
                        it.status == ItemStatus.PENDENTE || it.status == ItemStatus.FALTA
                    }
                    scanFeedbackManager.boxPartial()
                    _uiState.value = PickingUiState.BoxPartial(
                        box = box,
                        collectedItems = collected,
                        pendingItems = pending,
                        totalCollected = collected.sumOf { it.quantityCollected },
                        totalRequired = items.sumOf { it.quantityRequired }
                    )
                },
                onFailure = { e ->
                    scanFeedbackManager.scanError()
                    _uiState.value = PickingUiState.Error(e.message ?: "Erro ao salvar parcial.")
                }
            )
        }
    }

    private fun handleSaveMultiFloor() {
        val currentState = _uiState.value
        val boxId = when (currentState) {
            is PickingUiState.Collecting -> currentState.box.id
            else -> return
        }

        viewModelScope.launch(Dispatchers.IO) {
            saveMultiFloorBoxUseCase(boxId).fold(
                onSuccess = { box ->
                    scanFeedbackManager.boxPartial()
                    
                    val items = getBoxItemsUseCase(box.id).firstOrNull() ?: emptyList()
                    val totalPending = items.count { it.status == ItemStatus.PENDENTE || it.status == ItemStatus.FALTA }
                    
                    _uiState.value = PickingUiState.BoxMultiFloor(
                        box = box,
                        collectedInThisFloor = items.count { it.status == ItemStatus.COMPLETO },
                        totalPending = totalPending
                    )
                },
                onFailure = { e ->
                    scanFeedbackManager.scanError()
                    _uiState.value = PickingUiState.Error(e.message ?: "Erro ao salvar multi-andar.")
                    delay(2000)
                    _uiState.value = currentState
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
                    scanFeedbackManager.scanDivergenceSaved() // feedback de alerta positivo
                    
                    _uiState.value = PickingUiState.ItemSkipped(
                        box = currentState.box,
                        skippedItem = result.item,
                        reason = result.reason,
                        nextItem = null // O avanço real agora ocorre via Jetpack Compose
                    )
                },
                onFailure = { e ->
                    scanFeedbackManager.scanError()
                    _uiState.value = PickingUiState.Error(e.message ?: "Erro ao pular item.")
                }
            )
        }
    }

    private fun handleRegisterDivergence(barcode: String?, reason: br.com.grupokyly.apscoletor.domain.model.SkipReason, evidencePhotoUrl: String?) {
        val currentState = _uiState.value
        if (currentState !is PickingUiState.Collecting) return

        viewModelScope.launch(Dispatchers.IO) {
            registerDivergenceUseCase(currentState.currentItem.id, currentState.box.id, barcode, reason, evidencePhotoUrl).fold(
                onSuccess = {
                    scanFeedbackManager.scanDivergenceSaved() // Feedback exclusivo de divergência
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

    private fun handleResumeBoxConfirmed() {
        val currentState = _uiState.value
        if (currentState is PickingUiState.BoxResuming) {
            viewModelScope.launch(Dispatchers.IO) {
                val items = getBoxItemsUseCase(currentState.box.id).firstOrNull() ?: emptyList()
                val currentItem = items.firstOrNull { it.status == ItemStatus.PENDENTE }
                
                if (currentItem != null) {
                    if (currentState.isMultiFloor) scanFeedbackManager.scanPartialSuccess()
                    
                    _uiState.value = PickingUiState.Collecting(
                        box = currentState.box,
                        currentItem = currentItem,
                        currentItemIndex = items.indexOf(currentItem),
                        collectedCount = currentItem.quantityCollected,
                        totalItems = items.size,
                        lastScannedItems = emptyList(),
                        isMultiFloor = currentState.isMultiFloor,
                        floorLabel = if (currentState.isMultiFloor) "Continuando coleta — itens pendentes" else "",
                        operatorName = currentOperatorName
                    )
                }
            }
        }
    }
}
