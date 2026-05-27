package br.com.grupokyly.apscoletor.presentation.picking

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
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
import br.com.grupokyly.apscoletor.presentation.picking.components.BoxFinalizedContent
import br.com.grupokyly.apscoletor.presentation.picking.components.BoxPartialContent
import br.com.grupokyly.apscoletor.presentation.picking.components.BoxMultiFloorContent
import br.com.grupokyly.apscoletor.presentation.picking.components.CollectingContent
import br.com.grupokyly.apscoletor.presentation.picking.components.ErrorBanner
import br.com.grupokyly.apscoletor.presentation.picking.components.IdleContent
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
    viewModel: PickingViewModel = hiltViewModel()
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

    // Abre a câmera automaticamente assim que o app entra no estado Idle
    LaunchedEffect(uiState) {
        if (uiState is PickingUiState.Idle && !showCameraScanner) {
            cameraScannerMode = "papeleta"
            showCameraScanner = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Conteúdo principal por estado ────────────────────────────────────
        when (val state = uiState) {
            is PickingUiState.Idle -> {
                // Câmera abre automaticamente via LaunchedEffect acima.
                // Este conteúdo fica como fallback enquanto a câmera não abre.
                IdleContent(onManualClick = {
                    cameraScannerMode = "papeleta"
                    showCameraScanner = true
                })
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
                    onManualInput   = { code -> viewModel.onEvent(PickingEvent.OnManualInput(code)) }
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
                onNewBox = { /* reset via viewmodel */ }
            )

            is PickingUiState.BoxPartial -> BoxPartialContent(
                state = state,
                onNewBox = { /* reset via viewmodel */ }
            )

            is PickingUiState.BoxMultiFloor -> BoxMultiFloorContent(
                state = state,
                onNewBox = { /* reset via viewmodel */ }
            )

            is PickingUiState.Error -> {
                lastCollectingState?.let {
                    CollectingContent(
                        state = it,
                        onFinalize       = { },
                        onSavePartial    = { },
                        onSaveMultiFloor = { },
                        onSkipRequest    = { showSkipSheet = true },
                        onManualInput    = { }
                    )
                } ?: IdleContent(onManualClick = {
                    cameraScannerMode = "papeleta"
                    showCameraScanner = true
                })

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
                manualInputLabel = label
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
    }
}
