package br.com.grupokyly.apscoletor.presentation.picking.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.grupokyly.apscoletor.R
import br.com.grupokyly.apscoletor.domain.model.Box
import br.com.grupokyly.apscoletor.domain.model.BoxStatus
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
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
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
                contentDescription = "Atenção",
                tint = WarningOrange,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.box_partial_title),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = stringResource(R.string.box_partial_subtitle),
            fontSize = 14.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Card de resumo
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundSecondary, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Status:", color = TextSecondary, fontSize = 14.sp)
                Box(
                    modifier = Modifier
                        .background(WarningOrange.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = stringResource(R.string.box_partial_badge), color = WarningOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            SummaryRow("Pedido:", state.box.orderId)
            Spacer(modifier = Modifier.height(8.dp))
            SummaryRow("Papeleta:", state.box.papeletaCode)
            Spacer(modifier = Modifier.height(8.dp))
            SummaryRow("Peças coletadas:", "? de ?")
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        // Placeholder for items list
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundPrimary)
                .border(1.dp, WarningOrange.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Text(text = "Itens divergentes aparecerão aqui", color = TextSecondary, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(48.dp))

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
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextSecondary, fontSize = 14.sp)
        Text(text = value, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Preview(showBackground = true)
@Composable
fun BoxPartialContentPreview() {
    ApsColetorTheme {
        BoxPartialContent(
            state = PickingUiState.BoxPartial(
                box = Box(1, "PAP777", "PED777", BoxStatus.PARCIAL, 80, 100)
            ),
            onNewBox = {}
        )
    }
}
