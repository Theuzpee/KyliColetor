package br.com.grupokyly.apscoletor.hardware

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import br.com.grupokyly.apscoletor.BuildConfig
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class ScannerReceiver @Inject constructor(
    private val cameraScanner: CameraScanner?
) {

    constructor() : this(null)

    private val _scannedDataFlow = MutableSharedFlow<String>(
        extraBufferCapacity = 10
    )
    open val scannedDataFlow: SharedFlow<String> = _scannedDataFlow.asSharedFlow()

    init {
        cameraScanner?.let { scanner ->
            val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
            mergeWithCameraFlow(scope)
        }
    }

    fun mergeWithCameraFlow(scope: CoroutineScope) {
        scope.launch {
            cameraScanner?.scannedDataFlow?.collect { barcode ->
                _scannedDataFlow.tryEmit(barcode)
            }
        }
    }

    private var isRegistered = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            val scanData: String? = when (intent?.action) {

                // Datalogic Memor 11 — Intent output
                "com.datalogic.decode.action.SCAN_RESULT" -> {
                    intent.getStringExtra(
                        "com.datalogic.decode.DecodeResult.EXTRA_BARCODE_DATA_STRING"
                    )
                }

                // Datalogic alternativo (dependendo da configuração)
                "com.datalogic.decode.action.DECODE_DATA_ACTION" -> {
                    intent.getStringExtra("EXTRA_BARCODE_DATA")
                }

                // Fallback — DataWedge/Zebra (para outros dispositivos)
                "br.com.grupokyly.apscoletor.SCAN" -> {
                    intent.getStringExtra("com.symbol.datawedge.data_string")
                }

                else -> null
            }

            if (!scanData.isNullOrBlank()) {
                Log.d("ScannerReceiver", "Código lido: $scanData (action: ${intent?.action})")
                _scannedDataFlow.tryEmit(scanData)
            }
        }
    }

    open fun register(context: Context) {
        if (!isRegistered) {
            val appContext = context.applicationContext
            val filter = IntentFilter().apply {
                // Datalogic Memor 11
                addAction("com.datalogic.decode.action.SCAN_RESULT")
                addAction("com.datalogic.decode.action.DECODE_DATA_ACTION")
                addCategory(Intent.CATEGORY_DEFAULT)
                // Fallback outros dispositivos
                addAction("br.com.grupokyly.apscoletor.SCAN")
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    appContext.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
                } else {
                    appContext.registerReceiver(receiver, filter)
                }
                isRegistered = true
                Log.d("ScannerReceiver", "Receiver registrado com sucesso.")
            } catch (e: Exception) {
                Log.e("ScannerReceiver", "Erro ao registrar: ${e.message}")
            }
        }
     }
 
     open fun unregister(context: Context) {
        if (isRegistered) {
            try {
                context.applicationContext.unregisterReceiver(receiver)
                isRegistered = false
                Log.d("ScannerReceiver", "Receiver removido.")
            } catch (e: Exception) {
                Log.e("ScannerReceiver", "Erro ao remover: ${e.message}")
            }
        }
    }

    // Para testes no celular pessoal
    fun simulateScan(data: String) {
        if (BuildConfig.DEBUG) {
            _scannedDataFlow.tryEmit(data)
        }
    }
}
