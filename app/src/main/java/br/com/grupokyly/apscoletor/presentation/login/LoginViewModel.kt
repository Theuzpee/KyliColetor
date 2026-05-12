package br.com.grupokyly.apscoletor.presentation.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.grupokyly.apscoletor.domain.usecase.LoginUseCase
import br.com.grupokyly.apscoletor.hardware.ScannerReceiver
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
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val scannerReceiver: ScannerReceiver,
    private val scanFeedbackManager: ScanFeedbackManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private var supervisorBarcode: String? = null

    init {
        viewModelScope.launch {
            scannerReceiver.scannedDataFlow.collect { barcode ->
                if (barcode.isNotBlank()) {
                    handleScan(barcode)
                }
            }
        }
    }

    private fun handleScan(barcode: String) {
        when (_uiState.value) {
            is LoginUiState.Idle, is LoginUiState.Error -> {
                onEvent(LoginEvent.OnSupervisorScanned(barcode))
            }
            is LoginUiState.WaitingOperator -> {
                onEvent(LoginEvent.OnOperatorScanned(barcode))
            }
            else -> {} // Ignorar scan
        }
    }

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.OnSupervisorScanned -> {
                supervisorBarcode = event.barcode
                scanFeedbackManager.scanPartialSuccess()
                _uiState.value = LoginUiState.WaitingOperator(supervisorName = "Supervisor", shift = "TURNO") // Mock por enquanto
            }
            is LoginEvent.OnOperatorScanned -> {
                val currentSupervisorBarcode = supervisorBarcode ?: return
                _uiState.value = LoginUiState.Loading

                viewModelScope.launch(Dispatchers.IO) {
                    val result = loginUseCase(currentSupervisorBarcode, event.barcode)
                    
                    result.fold(
                        onSuccess = { session ->
                            scanFeedbackManager.scanSkuComplete()
                            _uiState.value = LoginUiState.Success(session)
                        },
                        onFailure = { exception ->
                            scanFeedbackManager.scanError()
                            _uiState.value = LoginUiState.Error(exception.message ?: "Erro desconhecido")
                            
                            delay(3000)
                            if (_uiState.value is LoginUiState.Error) {
                                supervisorBarcode = null
                                _uiState.value = LoginUiState.Idle
                            }
                        }
                    )
                }
            }
            is LoginEvent.OnRegisterHardware -> {
                scannerReceiver.register(event.context)
            }
            is LoginEvent.OnUnregisterHardware -> {
                scannerReceiver.unregister(event.context)
            }
            is LoginEvent.OnClearError -> {
                supervisorBarcode = null
                _uiState.value = LoginUiState.Idle
            }
            is LoginEvent.OnLogin -> {
                handleLogin(event.supervisorBarcode, event.operatorBarcode)
            }
        }
    }

    private fun handleLogin(
        supervisorBarcode: String,
        operatorBarcode: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = LoginUiState.Loading
            val result = loginUseCase(supervisorBarcode, operatorBarcode)
            result.fold(
                onSuccess = { session ->
                    scanFeedbackManager.scanSkuComplete()
                    _uiState.value = LoginUiState.Success(session)
                },
                onFailure = { error ->
                    scanFeedbackManager.scanError()
                    _uiState.value = LoginUiState.Error(
                        error.message ?: "Erro ao fazer login."
                    )
                    delay(3000)
                    _uiState.value = LoginUiState.Idle
                }
            )
        }
    }
}
