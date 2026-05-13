package br.com.grupokyly.apscoletor.presentation.picking.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
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
fun BoxFinalizedContent(
    state: PickingUiState.BoxFinalized,
    onNewBox: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Ícone animado + título
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = scaleIn(animationSpec = tween(500))
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFF1A2A1A), CircleShape)
                        .border(2.dp, SuccessGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Sucesso",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.box_finalized_title),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.box_finalized_subtitle),
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }

        // 2. Card de resumo
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundSecondary, RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryInfoRow(
                label = stringResource(R.string.label_status),
                value = "FINALIZADA",
                valueColor = SuccessGreen,
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
                valueColor = SuccessGreen
            )
            // Tempo de coleta
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.label_tempo_coleta),
                    color = TextSecondary,
                    fontSize = 14.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (state.collectionTimeMinutes > 0)
                            "${state.collectionTimeMinutes} min"
                        else "< 1 min",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 3. Lista de itens coletados
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

        // 4. Botão Nova Caixa
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

@Preview(showBackground = true)
@Composable
fun BoxFinalizedContentPreview() {
    ApsColetorTheme {
        BoxFinalizedContent(
            state = PickingUiState.BoxFinalized(
                box = Box(1, "PAP-FINALIZADA-001", "PED-1001", BoxStatus.FINALIZADA, 100, 100),
                collectedItems = listOf(
                    PickingItem(1, 1, "REF-1A", "AZUL", "P", "A01", 5, 5, ItemStatus.COMPLETO),
                    PickingItem(2, 1, "REF-1B", "AZUL", "M", "A01", 5, 5, ItemStatus.COMPLETO),
                    PickingItem(3, 1, "REF-1C", "AZUL", "G", "A01", 5, 5, ItemStatus.COMPLETO)
                ),
                totalCollected = 15,
                totalRequired = 15,
                collectionTimeMinutes = 7
            ),
            onNewBox = {}
        )
    }
}
