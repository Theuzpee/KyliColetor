package br.com.grupokyly.apscoletor.presentation.picking.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.grupokyly.apscoletor.R
import br.com.grupokyly.apscoletor.domain.model.SkipReason
import br.com.grupokyly.apscoletor.presentation.picking.PickingUiState

@Composable
fun ItemSkippedContent(state: PickingUiState.ItemSkipped) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF3E0)), // Fundo levemente laranja para atenção
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Atenção",
                modifier = Modifier.size(120.dp),
                tint = Color(0xFFFF9800) // Laranja
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = stringResource(id = R.string.item_skipped_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF9800)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val reasonStringId = when (state.reason) {
                SkipReason.DESABASTECIDO -> R.string.skip_reason_desabastecido
                SkipReason.SUJA -> R.string.skip_reason_suja
                SkipReason.AMASSADA -> R.string.skip_reason_amassada
                SkipReason.DESEMBALADA -> R.string.skip_reason_desembalada
                SkipReason.DESCASCADA -> R.string.skip_reason_descascada
                SkipReason.TAG_ERRADO -> R.string.skip_reason_tag_errado
                SkipReason.NAO_LE_CODIGO -> R.string.skip_reason_nao_le_codigo
            }
            
            Text(
                text = stringResource(id = reasonStringId),
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            val nextAction = if (state.nextItem != null) {
                "Indo para o próximo endereço..."
            } else {
                "Encerrando caixa..."
            }

            Text(
                text = nextAction,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}
