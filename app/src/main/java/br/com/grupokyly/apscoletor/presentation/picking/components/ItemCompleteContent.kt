package br.com.grupokyly.apscoletor.presentation.picking.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

@Composable
fun ItemCompleteContent(state: PickingUiState.ItemComplete) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Sucesso",
            modifier = Modifier.size(120.dp),
            tint = SuccessGreen
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.item_complete_title),
            style = MaterialTheme.typography.headlineMedium,
            color = SuccessGreen
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.item_complete_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ItemCompleteContentPreview() {
    ApsColetorTheme {
        ItemCompleteContent(
            state = PickingUiState.ItemComplete(
                box = Box(1, "PAP123", "PED123", BoxStatus.EM_COLETA, 0, 0),
                completedItem = PickingItem(1, 1, "1000079", "ÚNICO", "18", "C37.09.6B", 3, 3, ItemStatus.COMPLETO),
                nextItem = null
            )
        )
    }
}
