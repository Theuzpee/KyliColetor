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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Check
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

import androidx.compose.material.icons.filled.CameraAlt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectingContent(
    state: PickingUiState.Collecting,
    onFinalize: () -> Unit,
    onSavePartial: () -> Unit,
    onSaveMultiFloor: () -> Unit,
    onSkipRequest: () -> Unit,
    onManualInput: (String) -> Unit,
    onScanClick: () -> Unit
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

            // 4. HISTÓRICO DE LEITURAS VISUAL (Rastreabilidade) - AGORA LOGO ABAIXO DO 0/5 E PROGRESSO
            if (state.lastScannedItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF141414), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.last_reads),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF666666),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    state.lastScannedItems.forEach { read ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (read.isManual) Icons.Default.Edit else Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (read.isManual) WarningOrange else SuccessGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = read.barcode,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = read.time,
                                fontSize = 12.sp,
                                color = Color(0xFF666666)
                            )
                        }
                    }
                }
            }

            // 5. BOTÕES DE AÇÃO: ESCANEAR CÓDIGO (CÂMERA) E DIGITAÇÃO MANUAL
            Spacer(modifier = Modifier.height(16.dp))
            var showManualInput by remember { mutableStateOf(false) }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onScanClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.addressConfirmation == AddressConfirmationState.Confirmed) SuccessGreen else PrimaryYellow,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (state.addressConfirmation == AddressConfirmationState.Confirmed) "Escanear Peça" else "Escanear Endereço",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                
                OutlinedButton(
                    onClick = { showManualInput = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF444444)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Digitar Código",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
            }

            if (showManualInput) {
                br.com.grupokyly.apscoletor.presentation.components.ManualInputBottomSheet(
                    onDismiss = { showManualInput = false },
                    onConfirm = { code ->
                        onManualInput(code)
                    },
                    title = when (state.addressConfirmation) {
                        AddressConfirmationState.Pending -> "Código do endereço danificado?"
                        AddressConfirmationState.Confirmed -> "Código da peça danificado?"
                        else -> "Código danificado?"
                    },
                    hint = when (state.addressConfirmation) {
                        AddressConfirmationState.Pending -> "Digite o endereço do corredor"
                        AddressConfirmationState.Confirmed -> "Digite o código da peça"
                        else -> "Digite o código manualmente"
                    }
                )
            }
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
