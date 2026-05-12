package br.com.grupokyly.apscoletor.presentation.picking.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.grupokyly.apscoletor.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScannerComponent(
    modifier: Modifier = Modifier,
    onDebugScan: (String) -> Unit
) {
    if (!BuildConfig.DEBUG) return

    var expanded by remember { mutableStateOf(false) }
    var mockBarcode by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxWidth()) {
        // Pill tab — collapsed by default so it doesn't block the UI
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { expanded = !expanded },
                modifier = Modifier
                    .height(28.dp)
                    .defaultMinSize(minWidth = 1.dp, minHeight = 1.dp),
                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF333300),
                    contentColor = Color(0xFFFFCC00)
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
            ) {
                Text(
                    text = if (expanded) "▼ DEV" else "▲ DEV",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(expandFrom = Alignment.Bottom),
            exit = shrinkVertically(shrinkTowards = Alignment.Bottom)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = mockBarcode,
                        onValueChange = { mockBarcode = it },
                        label = { Text("Simular Leitura (Bipagem)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (mockBarcode.isNotBlank()) {
                                onDebugScan(mockBarcode)
                                mockBarcode = ""
                            }
                        },
                        modifier = Modifier.height(56.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Bipar")
                    }
                }
            }
        }
    }
}
