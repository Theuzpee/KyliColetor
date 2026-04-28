package br.com.grupokyly.apscoletor.hardware

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanFeedbackManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val toneGenerator: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_MUSIC, 100)
    } catch (e: Exception) {
        null
    }

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    /**
     * LED verde + 1 bipe simples (peça OK, SKU ainda não completa)
     */
    fun scanPartialSuccess() {
        playBeep(ToneGenerator.TONE_PROP_BEEP, 100)
        vibrate(longArrayOf(0, 100))
        // Nota: Controle do LED verde físico geralmente depende de intent específica da fabricante
        // ou de API dedicada. O bipe e vibração são o feedback primário.
    }

    /**
     * LED verde + 2 bipes simples (SKU atingiu quantidade, próximo endereço)
     */
    fun scanSkuComplete() {
        playBeep(ToneGenerator.TONE_PROP_BEEP2, 200)
        vibrate(longArrayOf(0, 100, 50, 100))
    }

    /**
     * LED vermelho + bipe contínuo 2 segundos (SKU errada ou peça sem saldo)
     */
    fun scanError() {
        playBeep(ToneGenerator.TONE_SUP_ERROR, 2000)
        vibrate(longArrayOf(0, 500, 100, 500, 100, 500))
    }

    /**
     * Sinal sonoro DISTINTO (diferente de positivo e negativo)
     */
    fun boxFinal() {
        playBeep(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 500)
        vibrate(longArrayOf(0, 300, 100, 300))
    }

    /**
     * Sinal sonoro diferente do finalizado para indicar caixa parcial
     */
    fun boxPartial() {
        playBeep(ToneGenerator.TONE_CDMA_NETWORK_USA_RINGBACK, 400)
        vibrate(longArrayOf(0, 200, 200, 200))
    }

    private fun playBeep(toneType: Int, durationMs: Int) {
        toneGenerator?.stopTone()
        toneGenerator?.startTone(toneType, durationMs)
    }

    private fun vibrate(pattern: LongArray) {
        if (vibrator == null || !vibrator.hasVibrator()) return
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }

    fun release() {
        toneGenerator?.release()
    }
}
