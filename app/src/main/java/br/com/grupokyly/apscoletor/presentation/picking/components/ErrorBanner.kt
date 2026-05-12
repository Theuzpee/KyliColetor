package br.com.grupokyly.apscoletor.presentation.picking.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.grupokyly.apscoletor.presentation.theme.ApsColetorTheme
import br.com.grupokyly.apscoletor.presentation.theme.ErrorRed
import androidx.compose.ui.graphics.Color

@Composable
fun ErrorBanner(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(ErrorRed, shape = RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Erro",
            tint = Color.White
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White
        )
    }
}

@Preview
@Composable
fun ErrorBannerPreview() {
    ApsColetorTheme {
        ErrorBanner(message = "Peça não pertence a este endereço.")
    }
}
