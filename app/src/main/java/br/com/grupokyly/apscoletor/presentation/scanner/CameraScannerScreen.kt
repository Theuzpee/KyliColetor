package br.com.grupokyly.apscoletor.presentation.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.core.content.ContextCompat
import br.com.grupokyly.apscoletor.presentation.components.ManualInputBottomSheet

@Composable
fun CameraScannerScreen(
    onBarcodeDetected: (String) -> Unit,
    onDismiss: () -> Unit,
    manualInputTitle: String = "Papeleta danificada?",
    manualInputHint: String = "Digite o código da papeleta",
    manualInputLabel: String = "Código de barras"
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraScanner = hiltViewModel<CameraScannerViewModel>()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var showManualInput by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraScanner.stopCamera()
        }
    }

    // Coletar scans da câmera
    LaunchedEffect(Unit) {
        cameraScanner.scannedDataFlow.collect { barcode ->
            onBarcodeDetected(barcode)
            onDismiss()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (hasCameraPermission) {
            // ── Preview da câmera (fundo completo) ──────────────────────────
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).also { previewView ->
                        cameraScanner.startCamera(lifecycleOwner, previewView)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // ── Overlay escuro com janela de leitura ─────────────────────────
            Canvas(modifier = Modifier.fillMaxSize()) {
                val scanWidth  = size.width * 0.78f
                val scanHeight = scanWidth * 0.48f          // proporção paisagem (barcode)
                val left = (size.width  - scanWidth)  / 2f
                val top  = (size.height - scanHeight) / 2f - size.height * 0.05f
                val scrimColor = Color.Black.copy(alpha = 0.65f)

                // 4 sombras ao redor da janela
                drawRect(scrimColor, Offset(0f, 0f),             Size(size.width, top))
                drawRect(scrimColor, Offset(0f, top + scanHeight), Size(size.width, size.height - (top + scanHeight)))
                drawRect(scrimColor, Offset(0f, top),             Size(left, scanHeight))
                drawRect(scrimColor, Offset(left + scanWidth, top), Size(size.width - (left + scanWidth), scanHeight))

                // Cantos azuis (estilo referência)
                val cornerLen = 52f
                val stroke    = 7f
                val blue      = Color(0xFF1E6FFF)

                // Canto Superior Esquerdo
                drawLine(blue, Offset(left, top),                        Offset(left + cornerLen, top),              stroke)
                drawLine(blue, Offset(left, top),                        Offset(left, top + cornerLen),              stroke)
                // Canto Superior Direito
                drawLine(blue, Offset(left + scanWidth, top),            Offset(left + scanWidth - cornerLen, top),  stroke)
                drawLine(blue, Offset(left + scanWidth, top),            Offset(left + scanWidth, top + cornerLen),  stroke)
                // Canto Inferior Esquerdo
                drawLine(blue, Offset(left, top + scanHeight),           Offset(left + cornerLen, top + scanHeight), stroke)
                drawLine(blue, Offset(left, top + scanHeight),           Offset(left, top + scanHeight - cornerLen), stroke)
                // Canto Inferior Direito
                drawLine(blue, Offset(left + scanWidth, top + scanHeight), Offset(left + scanWidth - cornerLen, top + scanHeight), stroke)
                drawLine(blue, Offset(left + scanWidth, top + scanHeight), Offset(left + scanWidth, top + scanHeight - cornerLen), stroke)

                // Linha de scan animada (linha azul no centro)
                drawRect(
                    color = blue.copy(alpha = 0.6f),
                    topLeft = Offset(left + 8f, top + scanHeight / 2f - 1.5f),
                    size = Size(scanWidth - 16f, 3f)
                )
            }

            // ── Banner superior ──────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.92f),
                    tonalElevation = 0.dp
                ) {
                    Text(
                        text = "Posicione o código de barras na marcação",
                        color = Color(0xFF111111),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                    )
                }
            }

            // ── Botão fechar (X) no canto superior direito ───────────────────
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 10.dp, end = 12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(44.dp)
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Fechar scanner",
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // ── Botão inferior: "Digitar código de barras" ──────────────────
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 28.dp, start = 24.dp, end = 24.dp)
                    .fillMaxWidth()
            ) {
                Button(
                    onClick = { showManualInput = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E6FFF),
                        contentColor   = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Digitar código de barras",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

        } else {
            // ── Sem permissão de câmera ──────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Permissão de câmera necessária",
                    color = Color.White,
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E6FFF))
                ) {
                    Text("Conceder permissão", color = Color.White)
                }
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = { showManualInput = true }) {
                    Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Digitar manualmente", color = Color.White)
                }
            }
        }
    }

    // ── Bottom Sheet de digitação manual ────────────────────────────────────
    if (showManualInput) {
        ManualInputBottomSheet(
            onDismiss = { showManualInput = false },
            onConfirm = { code ->
                showManualInput = false
                onBarcodeDetected(code)
                onDismiss()
            },
            title = manualInputTitle,
            hint = manualInputHint,
            label = manualInputLabel
        )
    }
}
