package br.com.grupokyly.apscoletor.presentation.picking.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.grupokyly.apscoletor.presentation.picking.PickingUiState
import br.com.grupokyly.apscoletor.presentation.theme.*

@Composable
fun BoxResumeTimelineContent(
    state: PickingUiState.BoxResuming,
    onResumeConfirmed: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = if (state.isMultiFloor) "CONTINUAR MULTI-ANDAR" else "RETOMAR CAIXA",
                color = PrimaryYellow,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.box.papeletaCode,
                fontFamily = FontFamily.Monospace,
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Pedido: ${state.box.orderId}",
                color = TextSecondary,
                fontSize = 16.sp
            )
        }

        // Timeline visual
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundSecondary, RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF333333), RoundedCornerShape(12.dp))
                .padding(20.dp)
        ) {
            Text(text = "Status da Operação", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))

            // Step 1: Passado (Coletados)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(SuccessGreen.copy(alpha = 0.2f), CircleShape)
                        .border(2.dp, SuccessGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Itens já coletados", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text("${state.collectedItemsCount} itens bipados no histórico", color = SuccessGreen, fontSize = 14.sp)
                }
            }

            // Connector
            Box(
                modifier = Modifier
                    .padding(start = 15.dp, top = 4.dp, bottom = 4.dp)
                    .width(2.dp)
                    .height(30.dp)
                    .background(Color(0xFF444444))
            )

            // Step 2: Divergências
            if (state.divergencesCount > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(WarningOrange.copy(alpha = 0.2f), CircleShape)
                            .border(2.dp, WarningOrange, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = WarningOrange, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Divergências registradas", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text("${state.divergencesCount} itens com anomalia", color = WarningOrange, fontSize = 14.sp)
                    }
                }

                // Connector
                Box(
                    modifier = Modifier
                        .padding(start = 15.dp, top = 4.dp, bottom = 4.dp)
                        .width(2.dp)
                        .height(30.dp)
                        .background(Color(0xFF444444))
                )
            }

            // Step 3: Próximo passo
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(PrimaryYellow.copy(alpha = 0.2f), CircleShape)
                        .border(2.dp, PrimaryYellow, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("!", color = PrimaryYellow, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Faltam ${state.pendingItemsCount} itens", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text("Próximo destino: ${state.nextAddress}", color = PrimaryYellow, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        Button(
            onClick = onResumeConfirmed,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryYellow, contentColor = Color.Black)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Text(
                    "INICIAR COLETA",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null)
            }
        }
    }
}
