package br.com.grupokyly.apscoletor.presentation.picking.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.grupokyly.apscoletor.R
import br.com.grupokyly.apscoletor.domain.model.Box
import br.com.grupokyly.apscoletor.domain.model.BoxStatus
import br.com.grupokyly.apscoletor.presentation.picking.PickingUiState
import br.com.grupokyly.apscoletor.presentation.theme.ApsColetorTheme
import br.com.grupokyly.apscoletor.presentation.theme.SuccessGreen
import br.com.grupokyly.apscoletor.presentation.theme.TextGray

@Composable
fun BoxFinalizedContent(
    state: PickingUiState.BoxFinalized,
    onNewBox: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Inventory,
            contentDescription = "Caixa Finalizada",
            modifier = Modifier.size(100.dp),
            tint = SuccessGreen
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.box_finalized_title),
            fontSize = 36.sp,
            color = SuccessGreen,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.label_papeleta, state.box.papeletaCode),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(R.string.label_pedido, state.box.orderId),
            style = MaterialTheme.typography.bodyLarge,
            color = TextGray
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onNewBox,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(stringResource(R.string.btn_nova_caixa), color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BoxFinalizedContentPreview() {
    ApsColetorTheme {
        BoxFinalizedContent(
            state = PickingUiState.BoxFinalized(
                box = Box(1, "PAP999", "PED999", BoxStatus.FINALIZADA, 0, 0)
            ),
            onNewBox = {}
        )
    }
}
