package br.com.grupokyly.apscoletor.presentation.picking

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import br.com.grupokyly.apscoletor.presentation.picking.components.BoxFinalizedContent
import br.com.grupokyly.apscoletor.presentation.picking.components.BoxPartialContent
import br.com.grupokyly.apscoletor.presentation.picking.components.BoxMultiFloorContent
import br.com.grupokyly.apscoletor.presentation.picking.components.CollectingContent
import br.com.grupokyly.apscoletor.presentation.picking.components.ErrorBanner
import br.com.grupokyly.apscoletor.presentation.picking.components.ItemCompleteContent
import br.com.grupokyly.apscoletor.presentation.scanner.CameraScannerScreen
import br.com.grupokyly.apscoletor.presentation.picking.components.LoadingContent
import br.com.grupokyly.apscoletor.presentation.picking.components.ItemSkippedContent
import br.com.grupokyly.apscoletor.presentation.picking.components.SkipItemBottomSheet
import br.com.grupokyly.apscoletor.presentation.picking.components.BoxResumeTimelineContent
import br.com.grupokyly.apscoletor.presentation.components.ManualInputBottomSheet
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import br.com.grupokyly.apscoletor.domain.model.SkipReason
import br.com.grupokyly.apscoletor.presentation.theme.PrimaryYellow

@Composable
fun PickingScreen(
    viewModel: PickingViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current.applicationContext

    // Mantém o último estado de coleta para renderizar abaixo do banner de erro
    var lastCollectingState by remember {
        mutableStateOf<PickingUiState.Collecting?>(null)
    }

    var showSkipSheet             by remember { mutableStateOf(false) }
    var pendingDivergenceReason   by remember { mutableStateOf<SkipReason?>(null) }
    // showCameraScanner: modo de uso — "papeleta" ou "peca"
    var showCameraScanner         by remember { mutableStateOf(false) }
    var cameraScannerMode         by remember { mutableStateOf("papeleta") }
    var showManualInputAfterDivergence by remember { mutableStateOf(false) }
    var showLogoutDialog          by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        val reason = pendingDivergenceReason
        if (bitmap != null && reason != null) {
            val mockPhotoUrl =
                "https://storage.googleapis.com/kyly-evidence/divergences/photo_${System.currentTimeMillis()}.jpg"
            viewModel.onEvent(
                PickingEvent.OnRegisterDivergence(
                    barcode = null,
                    reason = reason,
                    evidencePhotoUrl = mockPhotoUrl
                )
            )
            if (reason == SkipReason.NAO_LE_CODIGO) {
                showManualInputAfterDivergence = true
            }
        }
        pendingDivergenceReason = null
    }

    DisposableEffect(Unit) {
        viewModel.onEvent(PickingEvent.OnRegisterHardware(context))
        onDispose {
            viewModel.onEvent(PickingEvent.OnUnregisterHardware(context))
        }
    }



    Box(modifier = Modifier.fillMaxSize()) {

        // ── Conteúdo principal por estado ────────────────────────────────────
        when (val state = uiState) {
            is PickingUiState.Idle -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    CameraScannerScreen(
                        onBarcodeDetected = { barcode ->
                            viewModel.onEvent(PickingEvent.OnPapeletaScanned(barcode))
                        },
                        onDismiss = {},
                        manualInputTitle = "Papeleta danificada?",
                        manualInputHint = "Digite o código da papeleta",
                        manualInputLabel = "Código de barras"
                    )
                    
                    // Botão de Logout no canto superior direito
                    IconButton(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .statusBarsPadding()
                            .padding(top = 12.dp, end = 16.dp)
                            .size(44.dp)
                            .background(Color(0xFF1E1E1E).copy(alpha = 0.85f), androidx.compose.foundation.shape.CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Sair da conta",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            is PickingUiState.LoggedOut -> {
                LaunchedEffect(Unit) {
                    onNavigateToLogin()
                }
            }

            is PickingUiState.LoadingBox -> LoadingContent()

            is PickingUiState.BoxResuming -> {
                BoxResumeTimelineContent(
                    state = state,
                    onResumeConfirmed = { viewModel.onEvent(PickingEvent.OnResumeBoxConfirmed) }
                )
            }

            is PickingUiState.Collecting -> {
                lastCollectingState = state
                CollectingContent(
                    state = state,
                    onFinalize      = { viewModel.onEvent(PickingEvent.OnFinalizeBox) },
                    onSavePartial   = { viewModel.onEvent(PickingEvent.OnSavePartial) },
                    onSaveMultiFloor = { viewModel.onEvent(PickingEvent.OnSaveMultiFloor) },
                    onSkipRequest   = { showSkipSheet = true },
                    onManualInput   = { code -> viewModel.onEvent(PickingEvent.OnManualInput(code)) },
                    onScanClick     = {
                        cameraScannerMode = "peca"
                        showCameraScanner = true
                    }
                )
            }

            is PickingUiState.ItemComplete -> {
                ItemCompleteContent(state)
                LaunchedEffect(state) {
                    kotlinx.coroutines.delay(1500)
                    viewModel.onEvent(PickingEvent.OnAdvanceToNextItem)
                }
            }

            is PickingUiState.ItemSkipped -> {
                ItemSkippedContent(state)
                LaunchedEffect(state) {
                    kotlinx.coroutines.delay(1500)
                    viewModel.onEvent(PickingEvent.OnAdvanceToNextItem)
                }
            }

            is PickingUiState.BoxFinalized -> BoxFinalizedContent(
                state = state,
                onNewBox = { viewModel.onEvent(PickingEvent.OnNewBox) }
            )

            is PickingUiState.BoxPartial -> BoxPartialContent(
                state = state,
                onNewBox = { viewModel.onEvent(PickingEvent.OnNewBox) }
            )

            is PickingUiState.BoxMultiFloor -> BoxMultiFloorContent(
                state = state,
                onNewBox = { viewModel.onEvent(PickingEvent.OnNewBox) }
            )

            is PickingUiState.Error -> {
                lastCollectingState?.let {
                    CollectingContent(
                        state = it,
                        onFinalize       = { },
                        onSavePartial    = { },
                        onSaveMultiFloor = { },
                        onSkipRequest    = { showSkipSheet = true },
                        onManualInput    = { },
                        onScanClick     = {
                            cameraScannerMode = "peca"
                            showCameraScanner = true
                        }
                    )
                } ?: CameraScannerScreen(
                    onBarcodeDetected = { barcode ->
                        viewModel.onEvent(PickingEvent.OnPapeletaScanned(barcode))
                    },
                    onDismiss = {},
                    manualInputTitle = "Papeleta danificada?",
                    manualInputHint = "Digite o código da papeleta",
                    manualInputLabel = "Código de barras"
                )

                Box(modifier = Modifier.align(Alignment.TopCenter)) {
                    ErrorBanner(message = state.message)
                }
            }
        }

        // ── Bottom Sheet: item em falta / divergência ─────────────────────
        if (showSkipSheet) {
            SkipItemBottomSheet(
                onDismissRequest = { showSkipSheet = false },
                onSkip = { reason ->
                    showSkipSheet = false
                    viewModel.onEvent(PickingEvent.OnSkipItem(reason))
                },
                onRegisterDivergence = { reason ->
                    showSkipSheet = false
                    pendingDivergenceReason = reason
                    cameraLauncher.launch(null)
                }
            )
        }

        // ── Scanner de câmera (modo papeleta ou peça) ─────────────────────
        if (showCameraScanner) {
            val (title, hint, label) = when (cameraScannerMode) {
                "peca" -> Triple(
                    "Código da peça danificado?",
                    "Digite o código da peça manualmente",
                    "Código da peça"
                )
                else -> Triple(
                    "Papeleta danificada?",
                    "Digite o código da papeleta",
                    "Código da papeleta"
                )
            }
            CameraScannerScreen(
                onBarcodeDetected = { barcode ->
                    if (cameraScannerMode == "peca") {
                        viewModel.onEvent(PickingEvent.OnManualInput(barcode))
                    } else {
                        viewModel.onEvent(PickingEvent.OnPapeletaScanned(barcode))
                    }
                    showCameraScanner = false
                },
                onDismiss = { showCameraScanner = false },
                manualInputTitle = title,
                manualInputHint  = hint,
                manualInputLabel = label,
                showCloseButton  = true
            )
        }

        // ── Manual input pós-divergência ──────────────────────────────────
        if (showManualInputAfterDivergence) {
            ManualInputBottomSheet(
                onDismiss = { showManualInputAfterDivergence = false },
                onConfirm = { code ->
                    viewModel.onEvent(PickingEvent.OnManualInput(code))
                    showManualInputAfterDivergence = false
                },
                title = "Tentar digitar o código?",
                hint  = "Se souber o código da peça, tente digitá-lo",
                label = "Código da peça"
            )
        }

        // ── Dialog de confirmação de Logout ───────────────────────────────
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = {
                    Text(
                        text = "Sair da conta?",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                text = {
                    Text(
                        text = "Você será desconectado e precisará bipar seu crachá novamente para entrar.",
                        color = Color(0xFFAAAAAA),
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutDialog = false
                            viewModel.onEvent(PickingEvent.OnLogout)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE53935), // Error Red
                            contentColor = Color.White
                        )
                    ) {
                        Text("Sair")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showLogoutDialog = false },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                    ) {
                        Text("Cancelar")
                    }
                },
                containerColor = Color(0xFF1E1E1E),
                textContentColor = Color.White
            )
        }
    }
}
