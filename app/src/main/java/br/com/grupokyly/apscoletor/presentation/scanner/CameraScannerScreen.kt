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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.grupokyly.apscoletor.presentation.components.ManualInputBottomSheet
import br.com.grupokyly.apscoletor.presentation.theme.BackgroundPrimary
import br.com.grupokyly.apscoletor.presentation.theme.PrimaryYellow
import br.com.grupokyly.apscoletor.presentation.theme.TextPrimary
import br.com.grupokyly.apscoletor.presentation.theme.TextSecondary

@Composable
fun CameraScannerScreen(
    onBarcodeDetected: (String) -> Unit,
    onDismiss: () -> Unit,
    manualInputTitle: String = "Papeleta danificada?",
    manualInputHint: String = "Digite o código da papeleta",
    manualInputLabel: String = "Código de barras",
    showCloseButton: Boolean = false
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
        onDispose { cameraScanner.stopCamera() }
    }

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

            // ── Overlay com janela de leitura ────────────────────────────────
            Canvas(modifier = Modifier.fillMaxSize()) {
                val scanWidth  = size.width * 0.78f
                val scanHeight = scanWidth * 0.46f          // proporção barcode horizontal
                val left  = (size.width  - scanWidth)  / 2f
                val top   = (size.height - scanHeight) / 2f - size.height * 0.04f
                val scrim = Color.Black.copy(alpha = 0.65f)

                // 4 sombras ao redor da janela transparente
                drawRect(scrim, Offset(0f, 0f),               Size(size.width, top))
                drawRect(scrim, Offset(0f, top + scanHeight), Size(size.width, size.height - (top + scanHeight)))
                drawRect(scrim, Offset(0f, top),              Size(left, scanHeight))
                drawRect(scrim, Offset(left + scanWidth, top), Size(size.width - (left + scanWidth), scanHeight))

                // Cantos amarelos (paleta do app)
                val cornerLen  = 56f
                val stroke     = 7f
                val yellow     = android.graphics.Color.parseColor("#FFD600").let {
                    Color(it)
                }

                // Superior Esquerdo
                drawLine(yellow, Offset(left, top), Offset(left + cornerLen, top), stroke)
                drawLine(yellow, Offset(left, top), Offset(left, top + cornerLen), stroke)
                // Superior Direito
                drawLine(yellow, Offset(left + scanWidth, top), Offset(left + scanWidth - cornerLen, top), stroke)
                drawLine(yellow, Offset(left + scanWidth, top), Offset(left + scanWidth, top + cornerLen), stroke)
                // Inferior Esquerdo
                drawLine(yellow, Offset(left, top + scanHeight), Offset(left + cornerLen, top + scanHeight), stroke)
                drawLine(yellow, Offset(left, top + scanHeight), Offset(left, top + scanHeight - cornerLen), stroke)
                // Inferior Direito
                drawLine(yellow, Offset(left + scanWidth, top + scanHeight), Offset(left + scanWidth - cornerLen, top + scanHeight), stroke)
                drawLine(yellow, Offset(left + scanWidth, top + scanHeight), Offset(left + scanWidth, top + scanHeight - cornerLen), stroke)

                // Linha de scan amarela no centro da janela
                drawRect(
                    color = yellow.copy(alpha = 0.55f),
                    topLeft = Offset(left + 12f, top + scanHeight / 2f - 1.5f),
                    size    = Size(scanWidth - 24f, 3f)
                )
            }

            val isTorchEnabled by cameraScanner.isTorchEnabled.collectAsState()

            // ── Faixa superior com instrução ─────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(
                        start = if (showCloseButton) 72.dp else 16.dp,
                        end = 72.dp,
                        top = 12.dp,
                        bottom = 12.dp
                    ),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = BackgroundPrimary.copy(alpha = 0.88f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Posicione o código de barras na marcação",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }

            // Botão de voltar (Back Arrow) no canto superior esquerdo
            if (showCloseButton) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(top = 12.dp, start = 16.dp)
                        .size(44.dp)
                        .background(Color(0xFF1E1E1E).copy(alpha = 0.85f), androidx.compose.foundation.shape.CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Botão de lanterna (Flashlight) no canto superior direito
            IconButton(
                onClick = { cameraScanner.toggleTorch() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 12.dp, end = 16.dp)
                    .size(44.dp)
                    .background(
                        color = if (isTorchEnabled) PrimaryYellow.copy(alpha = 0.85f) else Color(0xFF1E1E1E).copy(alpha = 0.85f),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Lanterna",
                    tint = if (isTorchEnabled) Color.Black else Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            if (!showCloseButton) {
                // ── Botão "Digitar código de barras" — paleta amarela ────────────
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
                            containerColor = PrimaryYellow,
                            contentColor   = Color.Black
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
                            fontWeight = FontWeight.Bold
                        )
                    }
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
                    tint = PrimaryYellow,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Permissão de câmera necessária",
                    color = TextPrimary,
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryYellow,
                        contentColor   = Color.Black
                    )
                ) {
                    Text("Conceder permissão", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = { showManualInput = true }) {
                    Icon(Icons.Default.Edit, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Digitar manualmente", color = TextSecondary)
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
            hint  = manualInputHint,
            label = manualInputLabel
        )
    }
}
