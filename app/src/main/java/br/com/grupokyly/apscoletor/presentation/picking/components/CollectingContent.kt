package br.com.grupokyly.apscoletor.presentation.picking.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.grupokyly.apscoletor.R
import br.com.grupokyly.apscoletor.domain.model.AddressConfirmationState
import br.com.grupokyly.apscoletor.presentation.picking.PickingUiState
import br.com.grupokyly.apscoletor.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectingContent(
    state: PickingUiState.Collecting,
    onFinalize: () -> Unit,
    onSavePartial: () -> Unit,
    onSaveMultiFloor: () -> Unit,
    onSkipRequest: () -> Unit
) {
    val progress = if (state.currentItem.quantityRequired > 0) {
        state.collectedCount.toFloat() / state.currentItem.quantityRequired.toFloat()
    } else 0f

    // Estado para o Bottom Sheet de Opções Secundárias (Reduzir poluição na tela)
    var showMenuSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D)) // Preto Profundo (High Contrast)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        
        // 1. HEADER MINIMALISTA
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = state.box.orderId,
                color = Color(0xFF666666),
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp
            )
            IconButton(onClick = { showMenuSheet = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Opções", tint = Color.White)
            }
        }

        // 2. ENDEREÇO ALVO (FOCO ABSOLUTO)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1A1A), RoundedCornerShape(12.dp))
                .border(2.dp, if (state.addressConfirmation == AddressConfirmationState.Confirmed) SuccessGreen else PrimaryYellow, RoundedCornerShape(12.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "DESTINO", fontSize = 12.sp, color = Color(0xFFAAAAAA), letterSpacing = 2.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.currentItem.address,
                fontFamily = FontFamily.Monospace,
                fontSize = 64.sp, // Tamanho gigantesco para visão à distância
                fontWeight = FontWeight.Black,
                color = if (state.addressConfirmation == AddressConfirmationState.Confirmed) SuccessGreen else PrimaryYellow,
                maxLines = 1
            )
            
            // Sub-status de endereço
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when (state.addressConfirmation) {
                        AddressConfirmationState.Confirmed -> Icons.Default.CheckCircle
                        AddressConfirmationState.Error -> Icons.Default.Error
                        else -> Icons.Default.LocationOn
                    },
                    contentDescription = null,
                    tint = when (state.addressConfirmation) {
                        AddressConfirmationState.Confirmed -> SuccessGreen
                        AddressConfirmationState.Error -> ErrorRed
                        else -> PrimaryYellow
                    },
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (state.addressConfirmation) {
                        AddressConfirmationState.Confirmed -> "ENDEREÇO VALIDADO"
                        AddressConfirmationState.Error -> "CORREDOR/ENDEREÇO ERRADO"
                        else -> "AGUARDANDO BIPE DA GÔNDOLA"
                    },
                    color = when (state.addressConfirmation) {
                        AddressConfirmationState.Confirmed -> SuccessGreen
                        AddressConfirmationState.Error -> ErrorRed
                        else -> PrimaryYellow
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 3. INFORMAÇÃO DA PEÇA E PROGRESSO (VISUAL LINEAR)
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${state.currentItem.reference} · ${state.currentItem.color} · ${state.currentItem.size}",
                fontFamily = FontFamily.Monospace,
                fontSize = 18.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = state.collectedCount.toString(),
                    fontSize = 80.sp, // Contador Massivo
                    fontWeight = FontWeight.Black,
                    color = if (state.collectedCount == state.currentItem.quantityRequired) SuccessGreen else Color.White
                )
                Text(
                    text = " / ${state.currentItem.quantityRequired}",
                    fontSize = 32.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = SuccessGreen,
                trackColor = Color(0xFF222222)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Menu Inferior Compacto (Aparece clicando nos "3 pontinhos")
        if (showMenuSheet) {
            ModalBottomSheet(
                onDismissRequest = { showMenuSheet = false },
                containerColor = BackgroundSecondary
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Ações Secundárias", color = TextSecondary, fontSize = 14.sp)
                    
                    OutlinedButton(
                        onClick = { showMenuSheet = false; onSkipRequest() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.5.dp, WarningOrange),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningOrange)
                    ) {
                        Text(stringResource(R.string.btn_item_em_falta), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedButton(
                            onClick = { showMenuSheet = false; onSavePartial() },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                        ) {
                            Text("Salvar Parcial")
                        }
                        OutlinedButton(
                            onClick = { showMenuSheet = false; onSaveMultiFloor() },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryYellow)
                        ) {
                            Text("Multi-andar")
                        }
                    }

                    Button(
                        onClick = { showMenuSheet = false; onFinalize() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryYellow, contentColor = Color.Black)
                    ) {
                        Text(stringResource(R.string.btn_finalizar_caixa), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
