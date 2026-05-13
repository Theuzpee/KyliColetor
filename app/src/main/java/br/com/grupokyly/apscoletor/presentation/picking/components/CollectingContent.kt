package br.com.grupokyly.apscoletor.presentation.picking.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import br.com.grupokyly.apscoletor.domain.model.Box
import br.com.grupokyly.apscoletor.domain.model.BoxStatus
import br.com.grupokyly.apscoletor.domain.model.ItemStatus
import br.com.grupokyly.apscoletor.domain.model.PickingItem
import br.com.grupokyly.apscoletor.presentation.picking.PickingUiState
import br.com.grupokyly.apscoletor.presentation.theme.*

import br.com.grupokyly.apscoletor.domain.model.AddressConfirmationState

data class AddressVisuals(
    val bgColor: Color,
    val borderColor: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val message: String,
    val iconTint: Color
)

@Composable
fun CollectingContent(
    state: PickingUiState.Collecting,
    onFinalize: () -> Unit,
    onSavePartial: () -> Unit,
    onSaveMultiFloor: () -> Unit,
    onSkipRequest: () -> Unit
) {
    // Fake progress calculation
    val progress = if (state.currentItem.quantityRequired > 0) {
        state.collectedCount.toFloat() / state.currentItem.quantityRequired.toFloat()
    } else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Cabeçalho (Status Bar Mock)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Inventory, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = state.box.orderId, color = TextSecondary, fontSize = 12.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.BatteryFull, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                Text(text = "100%", color = SuccessGreen, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(imageVector = Icons.Default.Wifi, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                Text(text = "Online", color = SuccessGreen, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = state.operatorName, color = TextSecondary, fontSize = 12.sp)
            }
        }
        
        if (state.isMultiFloor) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2A2500), RoundedCornerShape(4.dp))
                    .border(1.dp, PrimaryYellow, RoundedCornerShape(4.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = PrimaryYellow,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.collecting_multi_floor_banner),
                    color = PrimaryYellow,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // 2. Bloco do endereço
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundSecondary, RoundedCornerShape(8.dp))
                .border(2.dp, PrimaryYellow, RoundedCornerShape(8.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "VÁ PARA O ENDEREÇO", fontSize = 11.sp, color = TextSecondary)
            Text(
                text = state.currentItem.address,
                fontFamily = FontFamily.Monospace,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryYellow
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 3. Bloco SKU
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${state.collectedCount} de ${state.currentItem.quantityRequired} peças",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "SKU ${state.currentItem.id}", // placeholder for barcode
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = SuccessGreen,
                trackColor = BackgroundSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${state.currentItem.reference} · ${state.currentItem.color} · ${state.currentItem.size}",
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                color = TextPrimary
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 4. Campo de confirmação de endereço
        val addressVisuals = when (state.addressConfirmation) {
            AddressConfirmationState.Pending -> AddressVisuals(
                bgColor = BackgroundSecondary,
                borderColor = PrimaryYellow,
                icon = Icons.Default.LocationOn,
                message = stringResource(R.string.address_pending),
                iconTint = PrimaryYellow
            )
            AddressConfirmationState.Confirmed -> AddressVisuals(
                bgColor = Color(0xFF1A2A1A),
                borderColor = SuccessGreen,
                icon = Icons.Default.CheckCircle,
                message = stringResource(R.string.address_confirmed),
                iconTint = SuccessGreen
            )
            AddressConfirmationState.Error -> AddressVisuals(
                bgColor = Color(0xFF2A1A1A),
                borderColor = ErrorRed,
                icon = Icons.Default.Error,
                message = stringResource(R.string.address_error),
                iconTint = ErrorRed
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(addressVisuals.bgColor, RoundedCornerShape(8.dp))
                .border(2.dp, addressVisuals.borderColor, RoundedCornerShape(8.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = addressVisuals.icon,
                contentDescription = null,
                tint = addressVisuals.iconTint
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = addressVisuals.message,
                color = TextPrimary,
                fontSize = 14.sp
            )
        }
        if (state.lastScannedItems.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundSecondary, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 10.dp)
            ) {
                Text(
                    text = stringResource(R.string.last_reads),
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                state.lastScannedItems.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = item.barcode,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = item.time,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))

        // 6. Botões do rodapé
        val isActionsEnabled = state.addressConfirmation == AddressConfirmationState.Confirmed
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onSkipRequest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.5.dp, WarningOrange),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningOrange)
            ) {
                Text(stringResource(R.string.btn_item_em_falta), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onSavePartial,
                    enabled = isActionsEnabled,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isActionsEnabled) TextSecondary else TextSecondary.copy(alpha = 0.4f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary, disabledContentColor = TextPrimary.copy(alpha = 0.4f)),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Text(
                        stringResource(R.string.btn_salvar_parcial), 
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.SemiBold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                OutlinedButton(
                    onClick = onSaveMultiFloor,
                    enabled = isActionsEnabled,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isActionsEnabled) PrimaryYellow else PrimaryYellow.copy(alpha = 0.4f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryYellow, disabledContentColor = PrimaryYellow.copy(alpha = 0.4f)),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Text(
                        stringResource(R.string.btn_save_multi_floor), 
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.SemiBold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                Button(
                    onClick = onFinalize,
                    enabled = isActionsEnabled,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryYellow, contentColor = Color.Black),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Text(
                        stringResource(R.string.btn_finalizar_caixa), 
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CollectingContentPreview() {
    ApsColetorTheme {
        CollectingContent(
            state = PickingUiState.Collecting(
                box = Box(1, "PAP123", "PED123", BoxStatus.EM_COLETA, 0, 0),
                currentItem = PickingItem(1, 1, "1000079", "ÚNICO", "18", "C37.09.6B", 3, 1, ItemStatus.PENDENTE),
                currentItemIndex = 2,
                collectedCount = 1,
                totalItems = 10
            ),
            onFinalize = {},
            onSavePartial = {},
            onSaveMultiFloor = {},
            onSkipRequest = {}
        )
    }
}
