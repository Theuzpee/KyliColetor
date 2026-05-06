package br.com.grupokyly.apscoletor.presentation.picking.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.grupokyly.apscoletor.R
import br.com.grupokyly.apscoletor.domain.model.SkipReason
import br.com.grupokyly.apscoletor.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkipItemBottomSheet(
    onDismissRequest: () -> Unit,
    onSkip: (SkipReason) -> Unit,
    onRegisterDivergence: (SkipReason) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = BackgroundCard,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Cabeçalho
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(id = R.string.skip_sheet_title),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "REF · COR · TAM — ENDEREÇO", // TODO: Pass details
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
                IconButton(onClick = onDismissRequest) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar", tint = TextPrimary)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Botão de desabastecimento
            OutlinedButton(
                onClick = { onSkip(SkipReason.DESABASTECIDO) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.5.dp, WarningOrange),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningOrange)
            ) {
                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text(
                    text = stringResource(id = R.string.skip_reason_desabastecido),
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // Divisória
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Divider(modifier = Modifier.weight(1f), color = BackgroundSecondary)
                Text(
                    text = "OU REGISTRAR DEFEITO",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Divider(modifier = Modifier.weight(1f), color = BackgroundSecondary)
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Grade de defeitos
            val divergenceReasons = listOf(
                SkipReason.SUJA to R.string.skip_reason_suja,
                SkipReason.AMASSADA to R.string.skip_reason_amassada,
                SkipReason.DESEMBALADA to R.string.skip_reason_desembalada,
                SkipReason.DESCASCADA to R.string.skip_reason_descascada,
                SkipReason.TAG_ERRADO to R.string.skip_reason_tag_errado,
                SkipReason.NAO_LE_CODIGO to R.string.skip_reason_nao_le_codigo
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = 240.dp)
            ) {
                items(divergenceReasons) { (reason, stringRes) ->
                    OutlinedButton(
                        onClick = { onRegisterDivergence(reason) },
                        modifier = Modifier.height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BackgroundSecondary),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextPrimary,
                            containerColor = Color.Transparent
                        )
                    ) {
                        Text(stringResource(id = stringRes), fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botão "Fechar"
            OutlinedButton(
                onClick = onDismissRequest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, TextPrimary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
            ) {
                Text(text = stringResource(id = R.string.btn_fechar), fontSize = 16.sp)
            }
        }
    }
}
