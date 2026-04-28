package br.com.grupokyly.apscoletor.hardware

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class DataWedgeReceiver @Inject constructor() {

    private val _scannedDataFlow = MutableSharedFlow<String>(extraBufferCapacity = 10)
    val scannedDataFlow: SharedFlow<String> = _scannedDataFlow.asSharedFlow()

    private var isRegistered = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Assegura que o processamento não bloqueie a thread principal
            // O goAsync() poderia ser usado para processamentos pesados antes de emitir,
            // mas como é apenas extração de string e emissão no flow, é super rápido.
            if (intent?.action == ACTION_SCAN) {
                intent.extras?.keySet()?.forEach { key ->
                    Log.d("DataWedgeReceiver_DEBUG", "Extra encontrado: $key = ${intent.extras?.get(key)}")
                }
                
                val scanData = intent.getStringExtra(EXTRA_DATA_STRING)
                if (!scanData.isNullOrBlank()) {
                    Log.d("DataWedgeReceiver", "Código lido: $scanData")
                    // tryEmit é thread-safe e não suspende. Se o buffer encher, ele descarta ou lida
                    // dependendo do BufferOverflow, mas com extraBufferCapacity=10 é seguro para o cenário.
                    _scannedDataFlow.tryEmit(scanData)
                }
            }
        }
    }

    fun register(context: Context) {
        if (!isRegistered) {
            val appContext = context.applicationContext
            val filter = IntentFilter(ACTION_SCAN)
            filter.addCategory(Intent.CATEGORY_DEFAULT)
            // No Android 14+ é necessário especificar as flags de exportação se não for sistema
            // Usando Context.RECEIVER_EXPORTED se suportado, senão o padrão.
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    appContext.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
                } else {
                    appContext.registerReceiver(receiver, filter)
                }
                isRegistered = true
                Log.d("DataWedgeReceiver", "Receiver registrado com sucesso.")
            } catch (e: Exception) {
                Log.e("DataWedgeReceiver", "Erro ao registrar receiver: ${e.message}")
            }
        }
    }

    fun unregister(context: Context) {
        if (isRegistered) {
            try {
                context.unregisterReceiver(receiver)
                isRegistered = false
                Log.d("DataWedgeReceiver", "Receiver desregistrado com sucesso.")
            } catch (e: Exception) {
                Log.e("DataWedgeReceiver", "Erro ao desregistrar receiver: ${e.message}")
            }
        }
    }

    companion object {
        const val ACTION_SCAN = "br.com.grupokyly.apscoletor.SCAN"
        const val EXTRA_DATA_STRING = "com.symbol.datawedge.data_string"
    }
}
