package br.com.grupokyly.apscoletor.presentation.scanner

import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import br.com.grupokyly.apscoletor.hardware.CameraScanner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

@HiltViewModel
class CameraScannerViewModel @Inject constructor(
    private val cameraScanner: CameraScanner
) : ViewModel() {

    val scannedDataFlow: SharedFlow<String> = cameraScanner.scannedDataFlow

    fun startCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        cameraScanner.startScanning(lifecycleOwner, previewView)
    }

    fun stopCamera() {
        cameraScanner.stopScanning()
    }

    override fun onCleared() {
        super.onCleared()
        cameraScanner.stopScanning()
    }
}
