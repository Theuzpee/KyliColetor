package br.com.grupokyly.apscoletor.presentation.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.core.content.ContextCompat

@Composable
fun CameraScannerScreen(
    onBarcodeDetected: (String) -> Unit,
    onDismiss: () -> Unit
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
            // Preview da câmera
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).also { previewView ->
                        cameraScanner.startCamera(lifecycleOwner, previewView)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Overlay de mira desenhado em cima
            Canvas(modifier = Modifier.fillMaxSize()) {
                val scanAreaSize = size.width * 0.7f
                val left = (size.width - scanAreaSize) / 2
                val top = (size.height - scanAreaSize) / 2
                val scrimColor = Color.Black.copy(alpha = 0.6f)

                // 1. Retângulo Superior
                drawRect(
                    color = scrimColor,
                    topLeft = Offset(0f, 0f),
                    size = Size(size.width, top)
                )
                // 2. Retângulo Inferior
                drawRect(
                    color = scrimColor,
                    topLeft = Offset(0f, top + scanAreaSize),
                    size = Size(size.width, size.height - (top + scanAreaSize))
                )
                // 3. Retângulo Esquerdo (entre topo e rodapé)
                drawRect(
                    color = scrimColor,
                    topLeft = Offset(0f, top),
                    size = Size(left, scanAreaSize)
                )
                // 4. Retângulo Direito (entre topo e rodapé)
                drawRect(
                    color = scrimColor,
                    topLeft = Offset(left + scanAreaSize, top),
                    size = Size(size.width - (left + scanAreaSize), scanAreaSize)
                )

                // Cantos amarelos
                val cornerLen = 40f
                val strokePx = 6f
                val cornerColor = Color(0xFFFFD600)

                // Canto Superior Esquerdo
                drawLine(cornerColor, Offset(left, top), Offset(left + cornerLen, top), strokePx)
                drawLine(cornerColor, Offset(left, top), Offset(left, top + cornerLen), strokePx)

                // Canto Superior Direito
                drawLine(cornerColor, Offset(left + scanAreaSize, top), Offset(left + scanAreaSize - cornerLen, top), strokePx)
                drawLine(cornerColor, Offset(left + scanAreaSize, top), Offset(left + scanAreaSize, top + cornerLen), strokePx)

                // Canto Inferior Esquerdo
                drawLine(cornerColor, Offset(left, top + scanAreaSize), Offset(left + cornerLen, top + scanAreaSize), strokePx)
                drawLine(cornerColor, Offset(left, top + scanAreaSize), Offset(left, top + scanAreaSize - cornerLen), strokePx)

                // Canto Inferior Direito
                drawLine(cornerColor, Offset(left + scanAreaSize, top + scanAreaSize), Offset(left + scanAreaSize - cornerLen, top + scanAreaSize), strokePx)
                drawLine(cornerColor, Offset(left + scanAreaSize, top + scanAreaSize), Offset(left + scanAreaSize, top + scanAreaSize - cornerLen), strokePx)
            }

            // Instrução
            Text(
                text = "Aponte para o código de barras",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp)
            )

            // Botão fechar
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

        } else {
            // Sem permissão
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    null,
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD600))
                ) {
                    Text("Conceder permissão", color = Color.Black)
                }
            }
        }
    }
}
