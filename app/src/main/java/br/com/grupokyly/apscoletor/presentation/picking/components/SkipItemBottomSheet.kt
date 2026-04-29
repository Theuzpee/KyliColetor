package br.com.grupokyly.apscoletor.presentation.picking.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.grupokyly.apscoletor.R
import br.com.grupokyly.apscoletor.domain.model.SkipReason

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkipItemBottomSheet(
    onDismissRequest: () -> Unit,
    onSkip: (SkipReason) -> Unit,
    onRegisterDivergence: (SkipReason) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.skip_sheet_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(24.dp))

            // SEÇÃO 1
            Text(
                text = "Peça não encontrada no endereço:",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { onSkip(SkipReason.DESABASTECIDO) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(
                    text = stringResource(id = R.string.skip_reason_desabastecido),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(24.dp))

            // SEÇÃO 2
            Text(
                text = "Selecione o motivo e direcione ao cestinho de divergências:",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Grade 2 colunas
            val divergenceReasons = listOf(
                SkipReason.SUJA to R.string.skip_reason_suja,
                SkipReason.AMASSADA to R.string.skip_reason_amassada,
                SkipReason.DESEMBALADA to R.string.skip_reason_desembalada,
                SkipReason.DESCASCADA to R.string.skip_reason_descascada,
                SkipReason.TAG_ERRADO to R.string.skip_reason_tag_errado,
                SkipReason.NAO_LE_CODIGO to R.string.skip_reason_nao_le_codigo
            )

            for (i in divergenceReasons.indices step 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val first = divergenceReasons[i]
                    OutlinedButton(
                        onClick = { onRegisterDivergence(first.first) },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                    ) {
                        Text(stringResource(id = first.second), fontSize = 16.sp)
                    }

                    if (i + 1 < divergenceReasons.size) {
                        val second = divergenceReasons[i + 1]
                        OutlinedButton(
                            onClick = { onRegisterDivergence(second.first) },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                        ) {
                            Text(stringResource(id = second.second), fontSize = 16.sp)
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botão fechar
            TextButton(
                onClick = onDismissRequest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(text = stringResource(id = R.string.btn_fechar), fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
