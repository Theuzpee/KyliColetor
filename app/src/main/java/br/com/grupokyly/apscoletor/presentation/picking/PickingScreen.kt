package br.com.grupokyly.apscoletor.presentation.picking

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.grupokyly.apscoletor.presentation.picking.components.BoxFinalizedContent
import br.com.grupokyly.apscoletor.presentation.picking.components.BoxPartialContent
import br.com.grupokyly.apscoletor.presentation.picking.components.CollectingContent
import br.com.grupokyly.apscoletor.presentation.picking.components.ErrorBanner
import br.com.grupokyly.apscoletor.presentation.picking.components.IdleContent
import br.com.grupokyly.apscoletor.presentation.picking.components.ItemCompleteContent
import br.com.grupokyly.apscoletor.presentation.picking.components.LoadingContent
import br.com.grupokyly.apscoletor.presentation.picking.components.ItemSkippedContent
import br.com.grupokyly.apscoletor.presentation.picking.components.SkipItemBottomSheet

@Composable
fun PickingScreen(
    viewModel: PickingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current.applicationContext
    
    // Remember the last collecting state to render beneath the error banner
    var lastCollectingState by androidx.compose.runtime.remember { 
        androidx.compose.runtime.mutableStateOf<PickingUiState.Collecting?>(null) 
    }

    var showSkipSheet by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    DisposableEffect(Unit) {
        viewModel.onEvent(PickingEvent.OnRegisterHardware(context))
        onDispose {
            viewModel.onEvent(PickingEvent.OnUnregisterHardware(context))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is PickingUiState.Idle -> IdleContent()
            is PickingUiState.LoadingBox -> LoadingContent()
            is PickingUiState.Collecting -> {
                lastCollectingState = state
                CollectingContent(
                    state = state,
                    onFinalize = { viewModel.onEvent(PickingEvent.OnFinalizeBox) },
                    onSavePartial = { viewModel.onEvent(PickingEvent.OnSavePartial) },
                    onSkipRequest = { showSkipSheet = true }
                )
            }
            is PickingUiState.ItemComplete -> ItemCompleteContent(state)
            is PickingUiState.ItemSkipped -> ItemSkippedContent(state)
            is PickingUiState.BoxFinalized -> BoxFinalizedContent(
                state = state,
                onNewBox = { /* Idealmente reseta a viewmodel */ }
            )
            is PickingUiState.BoxPartial -> BoxPartialContent(
                state = state,
                onNewBox = { /* Same as above */ }
            )
            is PickingUiState.Error -> {
                // Mantém a tela de Collecting desenhada por baixo
                lastCollectingState?.let {
                    CollectingContent(
                        state = it,
                        onFinalize = { },
                        onSavePartial = { },
                        onSkipRequest = { showSkipSheet = true }
                    )
                } ?: IdleContent()
                
                Box(modifier = Modifier.align(Alignment.TopCenter)) {
                    ErrorBanner(message = state.message)
                }
            }
        }

        if (showSkipSheet) {
            SkipItemBottomSheet(
                onDismissRequest = { showSkipSheet = false },
                onSkip = { reason ->
                    showSkipSheet = false
                    viewModel.onEvent(PickingEvent.OnSkipItem(reason))
                },
                onRegisterDivergence = { reason ->
                    viewModel.onEvent(PickingEvent.OnRegisterDivergence(barcode = null, reason = reason))
                }
            )
        }
    }
}
