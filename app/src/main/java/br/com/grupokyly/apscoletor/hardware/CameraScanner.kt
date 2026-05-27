package br.com.grupokyly.apscoletor.hardware

import android.content.Context
import android.util.Log
import android.util.Size
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CameraScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _scannedDataFlow = MutableSharedFlow<String>(
        extraBufferCapacity = 10
    )
    val scannedDataFlow: SharedFlow<String> = _scannedDataFlow.asSharedFlow()

    private var isScanning = false
    private var lastScanTime = 0L
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: androidx.camera.core.Camera? = null
    private var barcodeScanner: BarcodeScanner? = null
    private var currentSessionId = 0

    private val _isTorchEnabled = MutableStateFlow(false)
    val isTorchEnabled: StateFlow<Boolean> = _isTorchEnabled.asStateFlow()

    fun startScanning(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ) {
        stopScanning() // Garante a liberação de recursos de qualquer sessão anterior

        isScanning = true
        val sessionId = ++currentSessionId

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            if (sessionId != currentSessionId || !isScanning) {
                return@addListener
            }
            try {
                val provider = cameraProviderFuture.get()
                cameraProvider = provider

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val scanner = BarcodeScanning.getClient(
                    BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
                            Barcode.FORMAT_CODE_128,
                            Barcode.FORMAT_CODE_39,
                            Barcode.FORMAT_EAN_13,
                            Barcode.FORMAT_EAN_8,
                            Barcode.FORMAT_QR_CODE,
                            Barcode.FORMAT_DATA_MATRIX
                        )
                        .build()
                )
                barcodeScanner = scanner

                val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(1280, 720))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(
                    ContextCompat.getMainExecutor(context)
                ) { imageProxy ->
                    if (sessionId == currentSessionId && isScanning) {
                        processImage(imageProxy, scanner)
                    } else {
                        imageProxy.close()
                    }
                }

                provider.unbindAll()
                val cameraInstance = provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis
                )
                camera = cameraInstance
                _isTorchEnabled.value = cameraInstance.cameraInfo.torchState.value == androidx.camera.core.TorchState.ON
            } catch (e: Exception) {
                Log.e("CameraScanner", "Erro ao iniciar câmera: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImage(
        imageProxy: ImageProxy,
        scanner: BarcodeScanner
    ) {
        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                barcodes.firstOrNull()?.rawValue?.let { barcode ->
                    val currentTime = System.currentTimeMillis()
                    if (barcode.isNotBlank() && currentTime - lastScanTime >= 1000L) {
                        lastScanTime = currentTime
                        _scannedDataFlow.tryEmit(barcode)
                        Log.d("CameraScanner", "Código lido pela câmera: $barcode")
                    }
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    fun toggleTorch() {
        val currentCamera = camera ?: return
        val nextState = !_isTorchEnabled.value
        currentCamera.cameraControl.enableTorch(nextState).addListener({
            _isTorchEnabled.value = nextState
        }, ContextCompat.getMainExecutor(context))
    }

    fun stopScanning() {
        isScanning = false
        currentSessionId++
        
        val provider = cameraProvider
        val scanner = barcodeScanner
        
        cameraProvider = null
        barcodeScanner = null
        camera = null
        _isTorchEnabled.value = false

        ContextCompat.getMainExecutor(context).execute {
            try {
                provider?.unbindAll()
            } catch (e: Exception) {
                Log.e("CameraScanner", "Erro ao unbindAll: ${e.message}")
            }
            try {
                scanner?.close()
            } catch (e: Exception) {
                Log.e("CameraScanner", "Erro ao fechar BarcodeScanner: ${e.message}")
            }
        }
    }
}
