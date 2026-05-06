package br.com.grupokyly.apscoletor.presentation.login

import br.com.grupokyly.apscoletor.domain.model.UserSession

sealed class LoginUiState {
    object Idle : LoginUiState()
    
    data class WaitingOperator(
        val supervisorName: String,
        val shift: String
    ) : LoginUiState()
    
    object Loading : LoginUiState()
    
    data class Success(val session: UserSession) : LoginUiState()
    
    data class Error(val message: String) : LoginUiState()
}
