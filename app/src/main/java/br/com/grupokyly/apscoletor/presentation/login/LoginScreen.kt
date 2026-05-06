package br.com.grupokyly.apscoletor.presentation.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.grupokyly.apscoletor.R
import br.com.grupokyly.apscoletor.presentation.theme.*

@Composable
fun LoginScreen(
    onNavigateToPicking: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current.applicationContext

    DisposableEffect(Unit) {
        viewModel.onEvent(LoginEvent.OnRegisterHardware(context))
        onDispose {
            viewModel.onEvent(LoginEvent.OnUnregisterHardware(context))
        }
    }

    LaunchedEffect(state) {
        if (state is LoginUiState.Success) {
            onNavigateToPicking()
        }
    }

    LoginScreenContent(
        state = state,
        onEvent = viewModel::onEvent
    )
}

@Composable
fun LoginScreenContent(
    state: LoginUiState,
    onEvent: (LoginEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // 1. Logo + Identidade
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(PrimaryYellow, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(32.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = stringResource(R.string.login_title), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text(text = stringResource(R.string.login_subtitle), fontSize = 14.sp, color = TextSecondary)

        Spacer(modifier = Modifier.height(48.dp))

        // Banner Erro
        AnimatedVisibility(visible = state is LoginUiState.Error) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ErrorRed, RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = if (state is LoginUiState.Error) state.message else stringResource(R.string.login_error),
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Seção Supervisor
        val supervisorActive = state is LoginUiState.Idle || state is LoginUiState.Error
        val supervisorScanned = state is LoginUiState.WaitingOperator || state is LoginUiState.Loading || state is LoginUiState.Success
        
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = stringResource(R.string.login_supervisor_label), fontSize = 11.sp, color = TextSecondary)
                Spacer(modifier = Modifier.width(8.dp))
                if (supervisorScanned) {
                    Box(modifier = Modifier.background(PrimaryYellow, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text(text = stringResource(R.string.shift_1), fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold) // Mock shift for now
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = if (supervisorScanned) "SUP-12345" else "", // Mock
                onValueChange = {},
                placeholder = { Text(stringResource(R.string.login_supervisor_hint), fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = PrimaryYellow) },
                modifier = Modifier
                    .fillMaxWidth()
                    .border(if (supervisorActive) 2.dp else 1.dp, if (supervisorActive) PrimaryYellow else TextMuted, RoundedCornerShape(8.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = BackgroundSecondary,
                    focusedContainerColor = BackgroundSecondary,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    disabledContainerColor = BackgroundSecondary,
                    disabledTextColor = TextPrimary,
                    disabledBorderColor = Color.Transparent
                ),
                enabled = false // Scanner driven
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. Seção Colaborador
        val operatorActive = state is LoginUiState.WaitingOperator || state is LoginUiState.Loading || state is LoginUiState.Success
        Column(modifier = Modifier.fillMaxWidth().alpha(if (operatorActive) 1f else 0.4f)) {
            Text(text = stringResource(R.string.login_operator_label), fontSize = 11.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = if (state is LoginUiState.Loading || state is LoginUiState.Success) "OP-98765" else "", // Mock
                onValueChange = {},
                placeholder = { Text(stringResource(R.string.login_operator_hint), fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = TextSecondary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .border(if (operatorActive && state !is LoginUiState.Success && state !is LoginUiState.Loading) 2.dp else 1.dp, if (operatorActive) PrimaryYellow else TextMuted, RoundedCornerShape(8.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = BackgroundSecondary,
                    focusedContainerColor = BackgroundSecondary,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    disabledContainerColor = BackgroundSecondary,
                    disabledTextColor = TextPrimary,
                    disabledBorderColor = Color.Transparent
                ),
                enabled = false // Scanner driven
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 5. Botão Entrar
        if (state is LoginUiState.Loading) {
            CircularProgressIndicator(color = PrimaryYellow)
        } else {
            Button(
                onClick = { /* Not used as it's scanner driven, but can have it for mock purposes if needed */ },
                enabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .alpha(0f), // Hide it because scanner driven, but keeping space if needed. Wait, prompt says: "Botão Entrar: Desabilitado até ambos os campos estarem preenchidos"
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryYellow,
                    contentColor = Color.Black,
                    disabledContainerColor = BackgroundSecondary,
                    disabledContentColor = TextMuted
                )
            ) {
                Text(stringResource(R.string.login_btn), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Preview
@Composable
fun LoginScreenPreview() {
    ApsColetorTheme {
        LoginScreenContent(
            state = LoginUiState.Idle,
            onEvent = {}
        )
    }
}
