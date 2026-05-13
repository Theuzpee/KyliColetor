package br.com.grupokyly.apscoletor.presentation.picking.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

@Composable
fun BoxPartialContent(
    state: PickingUiState.BoxPartial,
    onNewBox: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Ícone + Título
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFF2A1A00), CircleShape)
                    .border(2.dp, WarningOrange, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = WarningOrange,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.box_partial_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.box_partial_subtitle),
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }

        // 2. Card de resumo geral
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundSecondary, RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryInfoRow(
                label = stringResource(R.string.label_status),
                value = stringResource(R.string.box_partial_badge),
                valueColor = WarningOrange,
                isBadge = true
            )
            SummaryInfoRow(
                label = stringResource(R.string.label_pedido),
                value = state.box.orderId
            )
            SummaryInfoRow(
                label = stringResource(R.string.label_papeleta),
                value = state.box.papeletaCode
            )
            SummaryInfoRow(
                label = stringResource(R.string.label_pecas_coletadas_resumo),
                value = "${state.totalCollected} de ${state.totalRequired}",
                valueColor = if (state.totalCollected == state.totalRequired) SuccessGreen else WarningOrange
            )
        }

        // 3. Itens Coletados
        if (state.collectedItems.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundSecondary, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${stringResource(R.string.label_itens_coletados)} (${state.collectedItems.size})",
                        color = SuccessGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                state.collectedItems.forEach { item ->
                    ItemSummaryCard(item = item, isCollected = true)
                }
            }
        }

        // 4. Itens Pendentes / Falta
        if (state.pendingItems.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2A1A00), RoundedCornerShape(12.dp))
                    .border(1.dp, WarningOrange.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = WarningOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${stringResource(R.string.label_itens_pendentes)} (${state.pendingItems.size})",
                        color = WarningOrange,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                state.pendingItems.forEach { item ->
                    ItemSummaryCard(item = item, isCollected = false)
                }
            }
        }

        // 5. Botão Nova Caixa
        Button(
            onClick = onNewBox,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryYellow,
                contentColor = Color.Black
            )
        ) {
            Text(
                text = stringResource(R.string.btn_nova_caixa),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun SummaryInfoRow(
    label: String,
    value: String,
    valueColor: Color = TextPrimary,
    isBadge: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextSecondary, fontSize = 14.sp)
        if (isBadge) {
            Box(
                modifier = Modifier
                    .background(valueColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(value, color = valueColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Text(value, color = valueColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ItemSummaryCard(item: PickingItem, isCollected: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isCollected) Color(0xFF1A2A1A) else Color(0xFF2A1A00),
                RoundedCornerShape(6.dp)
            )
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "${item.reference} · ${item.color} · ${item.size}",
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                color = TextPrimary
            )
            Text(
                text = item.address,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${item.quantityCollected}/${item.quantityRequired}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCollected) SuccessGreen else WarningOrange
            )
            Text(
                text = if (isCollected) "✓ coletado" else "pendente",
                fontSize = 10.sp,
                color = if (isCollected) SuccessGreen else WarningOrange
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BoxPartialContentPreview() {
    ApsColetorTheme {
        BoxPartialContent(
            state = PickingUiState.BoxPartial(
                box = Box(1, "PAP-PARCIAL-001", "PED-1002", BoxStatus.PARCIAL, 80, 100),
                collectedItems = listOf(
                    PickingItem(1, 1, "REF-2A", "PRETO", "M", "B02", 10, 10, ItemStatus.COMPLETO),
                    PickingItem(2, 1, "REF-2B", "PRETO", "G", "B02", 10, 10, ItemStatus.COMPLETO)
                ),
                pendingItems = listOf(
                    PickingItem(3, 1, "REF-FALTA", "BRANCO", "M", "B03", 5, 0, ItemStatus.FALTA)
                ),
                totalCollected = 20,
                totalRequired = 25
            ),
            onNewBox = {}
        )
    }
}
