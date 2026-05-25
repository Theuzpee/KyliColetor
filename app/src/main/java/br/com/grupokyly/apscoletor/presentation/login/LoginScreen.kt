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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import br.com.grupokyly.apscoletor.BuildConfig
import br.com.grupokyly.apscoletor.presentation.scanner.CameraScannerScreen
import br.com.grupokyly.apscoletor.presentation.components.ManualInputBottomSheet
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
    var supervisorInput by remember { mutableStateOf("") }
    var operatorInput by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    var showCameraScanner by remember { mutableStateOf(false) }
    var activeFieldForCamera by remember { mutableStateOf<String?>(null) }
    var showManualSupervisor by remember { mutableStateOf(false) }
    var showManualOperator by remember { mutableStateOf(false) }

    // Detectar quando supervisor foi preenchido pelo scanner (Keyboard Wedge)
    LaunchedEffect(supervisorInput) {
        if (supervisorInput.endsWith("\n") || supervisorInput.endsWith("\t")) {
            supervisorInput = supervisorInput.trim()
            focusManager.moveFocus(FocusDirection.Down)
        }
    }

    LaunchedEffect(operatorInput) {
        if (operatorInput.endsWith("\n") || operatorInput.endsWith("\t")) {
            operatorInput = operatorInput.trim()
            if (supervisorInput.isNotBlank() && operatorInput.isNotBlank()) {
                onEvent(LoginEvent.OnLogin(supervisorInput, operatorInput))
            }
        }
    }
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
        Column(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = supervisorInput,
                onValueChange = { supervisorInput = it },
                label = { Text("SUPERVISOR", color = TextSecondary, fontSize = 11.sp) },
                placeholder = {
                    Text(
                        "Bipe ou digite o código do supervisor",
                        color = TextSecondary
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = PrimaryYellow
                    )
                },
                trailingIcon = {
                    if (!BuildConfig.IS_DATALOGIC_DEVICE) {
                        IconButton(onClick = {
                            activeFieldForCamera = "supervisor"
                            showCameraScanner = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Escanear com a câmera",
                                tint = PrimaryYellow
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryYellow,
                    unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = PrimaryYellow,
                    focusedContainerColor = BackgroundSecondary,
                    unfocusedContainerColor = BackgroundSecondary
                ),
                shape = RoundedCornerShape(8.dp)
            )
            TextButton(
                onClick = { showManualSupervisor = true },
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("Digitar manualmente", color = TextSecondary, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. Seção Colaborador
        Column(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = operatorInput,
                onValueChange = { operatorInput = it },
                label = { Text("COLABORADOR", color = TextSecondary, fontSize = 11.sp) },
                placeholder = {
                    Text(
                        "Bipe ou digite o código do colaborador",
                        color = TextSecondary
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = null,
                        tint = if (supervisorInput.isNotBlank()) TextPrimary
                        else TextSecondary.copy(alpha = 0.4f)
                    )
                },
                trailingIcon = {
                    if (!BuildConfig.IS_DATALOGIC_DEVICE) {
                        IconButton(onClick = {
                            activeFieldForCamera = "operator"
                            showCameraScanner = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Escanear com a câmera",
                                tint = if (supervisorInput.isNotBlank()) PrimaryYellow
                                else TextSecondary.copy(alpha = 0.4f)
                            )
                        }
                    }
                },
                enabled = supervisorInput.isNotBlank(), // só ativa após supervisor
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (supervisorInput.isNotBlank() && operatorInput.isNotBlank()) {
                            onEvent(LoginEvent.OnLogin(supervisorInput, operatorInput))
                        }
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryYellow,
                    unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    disabledBorderColor = TextSecondary.copy(alpha = 0.2f),
                    disabledTextColor = TextSecondary.copy(alpha = 0.4f),
                    disabledContainerColor = BackgroundSecondary.copy(alpha = 0.5f),
                    cursorColor = PrimaryYellow,
                    focusedContainerColor = BackgroundSecondary,
                    unfocusedContainerColor = BackgroundSecondary
                ),
                shape = RoundedCornerShape(8.dp)
            )
            TextButton(
                onClick = { showManualOperator = true },
                enabled = supervisorInput.isNotBlank(),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = if (supervisorInput.isNotBlank()) TextSecondary else TextSecondary.copy(alpha = 0.4f),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Digitar manualmente",
                    color = if (supervisorInput.isNotBlank()) TextSecondary else TextSecondary.copy(alpha = 0.4f),
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 5. Botão Entrar
        if (state is LoginUiState.Loading) {
            CircularProgressIndicator(color = PrimaryYellow)
        } else {
            Button(
                onClick = {
                    onEvent(LoginEvent.OnLogin(supervisorInput, operatorInput))
                },
                enabled = supervisorInput.isNotBlank() && operatorInput.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryYellow,
                    contentColor = Color.Black,
                    disabledContainerColor = PrimaryYellow.copy(alpha = 0.4f),
                    disabledContentColor = Color.Black.copy(alpha = 0.4f)
                )
            ) {
                Text(
                    text = stringResource(R.string.login_btn),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (showCameraScanner) {
            CameraScannerScreen(
                onBarcodeDetected = { barcode ->
                    if (activeFieldForCamera == "supervisor") {
                        supervisorInput = barcode
                    } else if (activeFieldForCamera == "operator") {
                        operatorInput = barcode
                    }
                    showCameraScanner = false
                    activeFieldForCamera = null
                },
                onDismiss = {
                    showCameraScanner = false
                    activeFieldForCamera = null
                }
            )
        }

        if (showManualSupervisor) {
            ManualInputBottomSheet(
                onDismiss = { showManualSupervisor = false },
                onConfirm = { supervisorInput = it },
                title = "Crachá do supervisor danificado?",
                hint = "Digite o código do supervisor",
                label = "Código do supervisor"
            )
        }

        if (showManualOperator) {
            ManualInputBottomSheet(
                onDismiss = { showManualOperator = false },
                onConfirm = { operatorInput = it },
                title = "Crachá do colaborador danificado?",
                hint = "Digite o código do colaborador",
                label = "Código do colaborador"
            )
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
