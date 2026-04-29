package br.com.grupokyly.apscoletor.presentation.picking.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.grupokyly.apscoletor.R
import br.com.grupokyly.apscoletor.domain.model.Box
import br.com.grupokyly.apscoletor.domain.model.BoxStatus
import br.com.grupokyly.apscoletor.domain.model.ItemStatus
import br.com.grupokyly.apscoletor.domain.model.PickingItem
import br.com.grupokyly.apscoletor.presentation.picking.PickingUiState
import br.com.grupokyly.apscoletor.presentation.theme.ApsColetorTheme
import br.com.grupokyly.apscoletor.presentation.theme.SuccessGreen
import br.com.grupokyly.apscoletor.presentation.theme.TextGray

@Composable
fun CollectingContent(
    state: PickingUiState.Collecting,
    onFinalize: () -> Unit,
    onSavePartial: () -> Unit,
    onSkipRequest: () -> Unit
) {
    val progress = if (state.currentItem.quantityRequired > 0) {
        state.collectedCount.toFloat() / state.currentItem.quantityRequired.toFloat()
    } else 0f
    
    val isComplete = state.collectedCount >= state.currentItem.quantityRequired
    val progressColor = if (isComplete) SuccessGreen else MaterialTheme.colorScheme.primary

    val progress = if (state.totalItems > 0) {
        state.currentItemIndex.toFloat() / state.totalItems.toFloat()
    } else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Topo: Progresso Geral
        Column {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.label_endereco_progresso, state.currentItemIndex + 1, state.totalItems),
                style = MaterialTheme.typography.labelMedium,
                color = TextGray
            )
        }

        // Centro: Endereço e Produto
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.label_localizacao),
                style = MaterialTheme.typography.labelMedium,
                color = TextGray
            )
            Text(
                text = state.currentItem.address,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "${state.currentItem.reference} · ${state.currentItem.color} · ${state.currentItem.size}",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Contador
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.label_pecas_coletadas, state.collectedCount, state.currentItem.quantityRequired),
                style = MaterialTheme.typography.headlineMedium,
                color = progressColor
            )
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(12.dp),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surface
            )
        }

        // Botões de Ação
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onSkipRequest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = androidx.compose.ui.graphics.Color(0xFFFF9800))
            ) {
                Text(stringResource(R.string.btn_item_em_falta))
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onSavePartial,
                    modifier = Modifier.weight(1f).height(56.dp)
                ) {
                    Text(stringResource(R.string.btn_salvar_parcial))
                }
                Button(
                    onClick = onFinalize,
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                ) {
                    Text(stringResource(R.string.btn_finalizar_caixa))
                }
            }
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
            onSkipRequest = {}
        )
    }
}
