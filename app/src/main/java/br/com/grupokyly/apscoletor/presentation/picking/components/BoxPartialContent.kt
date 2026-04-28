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
import androidx.compose.material.icons.filled.Warning
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
import br.com.grupokyly.apscoletor.presentation.theme.AlertOrange
import br.com.grupokyly.apscoletor.presentation.theme.ApsColetorTheme
import br.com.grupokyly.apscoletor.presentation.theme.TextGray

@Composable
fun BoxPartialContent(
    state: PickingUiState.BoxPartial,
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
            imageVector = Icons.Default.Warning,
            contentDescription = "Atenção",
            modifier = Modifier.size(100.dp),
            tint = AlertOrange
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.box_partial_title),
            fontSize = 32.sp,
            color = AlertOrange,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = stringResource(R.string.box_partial_subtitle),
            fontSize = 24.sp,
            color = AlertOrange,
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.label_papeleta, state.box.papeletaCode),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(R.string.box_partial_instruction),
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
fun BoxPartialContentPreview() {
    ApsColetorTheme {
        BoxPartialContent(
            state = PickingUiState.BoxPartial(
                box = Box(1, "PAP777", "PED777", BoxStatus.PARCIAL, 0, 0)
            ),
            onNewBox = {}
        )
    }
}
