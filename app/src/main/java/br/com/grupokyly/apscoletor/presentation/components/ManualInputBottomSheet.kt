package br.com.grupokyly.apscoletor.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.grupokyly.apscoletor.presentation.theme.PrimaryYellow
import br.com.grupokyly.apscoletor.presentation.theme.TextPrimary
import br.com.grupokyly.apscoletor.presentation.theme.TextSecondary
import br.com.grupokyly.apscoletor.presentation.theme.WarningOrange
import br.com.grupokyly.apscoletor.presentation.theme.ErrorRed
import br.com.grupokyly.apscoletor.presentation.theme.BackgroundSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualInputBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    title: String = "Código danificado?",
    hint: String = "Digite o código manualmente",
    label: String = "Código de barras"
) {
    var inputText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF252525),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(TextSecondary.copy(alpha = 0.4f), CircleShape)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cabeçalho
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = hint,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, null, tint = TextSecondary)
                }
            }

            // Banner informativo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        WarningOrange.copy(alpha = 0.1f),
                        RoundedCornerShape(8.dp)
                    )
                    .border(1.dp, WarningOrange.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Warning,
                    null,
                    tint = WarningOrange,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Entrada manual será registrada para rastreabilidade",
                    fontSize = 12.sp,
                    color = WarningOrange
                )
            }

            // Campo de digitação
            OutlinedTextField(
                value = inputText,
                onValueChange = { raw ->
                    val formatted = formatCode(raw, title)
                    inputText = formatted
                    isError = false
                },
                label = { Text(label, color = TextSecondary) },
                placeholder = { Text("Ex: PECA-001, PAP-001, C37.09.6B") },
                isError = isError,
                supportingText = if (isError) {
                    { Text("Digite um código válido", color = ErrorRed) }
                } else null,
                leadingIcon = {
                    Icon(
                        Icons.Default.Edit,
                        null,
                        tint = if (isError) ErrorRed else PrimaryYellow
                    )
                },
                trailingIcon = if (inputText.isNotEmpty()) {
                    {
                        IconButton(onClick = { inputText = "" }) {
                            Icon(Icons.Default.Clear, null, tint = TextSecondary)
                        }
                    }
                } else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                    capitalization = KeyboardCapitalization.Characters
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (validateCode(inputText, title)) {
                            onConfirm(inputText)
                            onDismiss()
                        } else {
                            isError = true
                        }
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryYellow,
                    unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f),
                    errorBorderColor = ErrorRed,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = PrimaryYellow,
                    focusedContainerColor = BackgroundSecondary,
                    unfocusedContainerColor = BackgroundSecondary
                ),
                shape = RoundedCornerShape(8.dp)
            )

            // Botões
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, TextSecondary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                ) {
                    Text("Cancelar", fontSize = 15.sp)
                }
                Button(
                    onClick = {
                        if (validateCode(inputText, title)) {
                            onConfirm(inputText)
                            onDismiss()
                        } else {
                            isError = true
                        }
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryYellow,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(
                        Icons.Default.Check,
                        null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Confirmar",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun formatCode(rawInput: String, type: String): String {
    val input = rawInput.uppercase()
    if (input.isEmpty()) return ""

    // Se for Endereço ou Localização
    if (type.contains("endereço", ignoreCase = true) || type.contains("localização", ignoreCase = true)) {
        val clean = input.replace(".", "")
        val builder = StringBuilder()
        for (i in clean.indices) {
            builder.append(clean[i])
            if (i == 2 && clean.length > 3) {
                builder.append('.')
            } else if (i == 4 && clean.length > 5) {
                builder.append('.')
            }
        }
        return builder.toString().take(10)
    }

    // Se for Supervisor, Colaborador, Papeleta ou Peça
    val prefixes = listOf("SUP", "OP", "EMP", "PAP", "PECA", "REF")
    for (prefix in prefixes) {
        if (input.startsWith(prefix)) {
            val remaining = input.substring(prefix.length).replace("-", "")
            if (remaining.isNotEmpty()) {
                return "$prefix-$remaining"
            }
        }
    }

    return input
}

private fun validateCode(input: String, type: String): Boolean {
    val cleanInput = input.trim().uppercase()
    if (cleanInput.isEmpty()) return false

    return when {
        type.contains("supervisor", ignoreCase = true) -> {
            cleanInput.matches(Regex("^SUP-\\d+$")) || cleanInput.matches(Regex("^SUP\\d+$"))
        }
        type.contains("colaborador", ignoreCase = true) || type.contains("operador", ignoreCase = true) -> {
            cleanInput.matches(Regex("^(OP|EMP)-\\d+$")) || cleanInput.matches(Regex("^(OP|EMP)\\d+$"))
        }
        type.contains("papeleta", ignoreCase = true) -> {
            cleanInput.matches(Regex("^PAP-[A-Z0-9\\-]+$")) || cleanInput.matches(Regex("^PAP[A-Z0-9\\-]+$"))
        }
        type.contains("endereço", ignoreCase = true) || type.contains("localização", ignoreCase = true) -> {
            cleanInput.matches(Regex("^[A-Z0-9]{3}\\.[A-Z0-9]{2}\\.[A-Z0-9]{2}$")) ||
            cleanInput.matches(Regex("^[A-Z]\\d{2}$")) ||
            cleanInput.contains("ANDAR")
        }
        else -> cleanInput.length >= 3
    }
}
