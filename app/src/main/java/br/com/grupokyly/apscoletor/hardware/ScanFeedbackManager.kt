package br.com.grupokyly.apscoletor.hardware

import android.content.Context
import android.content.Intent
import android.util.Log
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import br.com.grupokyly.apscoletor.domain.hardware.FeedbackProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanFeedbackManager @Inject constructor(
    @ApplicationContext private val context: Context
) : FeedbackProvider {

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
    override fun scanPartialSuccess() {
        triggerGoodReadLed(success = true)
        playBeep(ToneGenerator.TONE_PROP_BEEP, 100)
        vibrate(longArrayOf(0, 100))
    }

    /**
     * LED verde + 2 bipes simples (SKU atingiu quantidade, próximo endereço)
     */
    override fun scanSkuComplete() {
        playBeep(ToneGenerator.TONE_PROP_BEEP2, 200)
        vibrate(longArrayOf(0, 100, 50, 100))
    }

    /**
     * LED vermelho + bipe contínuo 2 segundos (SKU errada ou peça sem saldo)
     */
    override fun scanError() {
        triggerGoodReadLed(success = false)
        playBeep(ToneGenerator.TONE_SUP_ERROR, 2000)
        vibrate(longArrayOf(0, 500, 100, 500, 100, 500))
    }

    /**
     * Sinal sonoro DISTINTO (diferente de positivo e negativo)
     */
    override fun boxFinal() {
        playBeep(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 500)
        vibrate(longArrayOf(0, 300, 100, 300))
    }

    /**
     * Sinal sonoro diferente do finalizado para indicar caixa parcial
     */
    override fun boxPartial() {
        playBeep(ToneGenerator.TONE_CDMA_NETWORK_USA_RINGBACK, 400)
        vibrate(longArrayOf(0, 200, 200, 200))
    }

    private fun triggerGoodReadLed(success: Boolean) {
        // Datalogic Good Read LED via Intent
        try {
            val intent = Intent("com.datalogic.decode.action.GOOD_READ_OVERRIDE").apply {
                putExtra("LED_COLOR", if (success) "GREEN" else "RED")
                putExtra("LED_DURATION_MS", if (success) 200 else 500)
            }
            context.sendBroadcast(intent)
        } catch (e: Exception) {
            Log.d("ScanFeedbackManager", "LED Intent não suportado: ${e.message}")
            // Falha silenciosa — o beep já é o feedback primário
        }
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

    /**
     * Sinal sonoro GRAVE + Vibração gaguejada para indicar Erro de Corredor/Sequência
     */
    override fun scanSequenceError() {
        triggerGoodReadLed(success = false)
        playBeep(ToneGenerator.TONE_CDMA_ABBR_ALERT, 800)
        vibrate(longArrayOf(0, 150, 100, 150, 100, 500))
    }

    /**
     * Sinal curto e agudo para Item Duplicado
     */
    override fun scanDuplicateError() {
        triggerGoodReadLed(success = false)
        playBeep(ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE, 300)
        vibrate(longArrayOf(0, 100))
    }

    /**
     * Som de sucesso suave para Divergência/Pular Item (Não é erro, é uma ação finalizada com sucesso)
     */
    override fun scanDivergenceSaved() {
        triggerGoodReadLed(success = true)
        playBeep(ToneGenerator.TONE_PROP_PROMPT, 400)
        vibrate(longArrayOf(0, 50, 50, 50))
    }

    override fun release() {
        toneGenerator?.release()
    }
}
