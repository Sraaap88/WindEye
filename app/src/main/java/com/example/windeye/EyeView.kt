package com.example.windeye

import android.content.Context
import android.media.MediaRecorder
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.content.pm.PackageManager
import android.Manifest
import kotlin.math.log10

class EyeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val animationManager = EyeAnimationManager(context)
    private var recorder: MediaRecorder? = null
    private var monitoringThread: Thread? = null
    private var isMonitoring = false

    init {
        addView(animationManager)
        // Ne pas démarrer automatiquement - attendre la permission
    }

    fun startBreathMonitoring() {
        if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        try {
            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile("/dev/null")
                prepare()
                start()
            }

            isMonitoring = true
            monitoringThread = Thread {
                while (isMonitoring) {
                    try {
                        Thread.sleep(100)
                        recorder?.maxAmplitude?.let {
                            val dB = 20 * log10(it.toDouble().coerceAtLeast(1.0)) / 90.0
                            post { animationManager.updateFromBreath(dB.toFloat()) }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        break
                    }
                }
            }
            monitoringThread?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopBreathMonitoring() {
        isMonitoring = false
        monitoringThread?.interrupt()
        monitoringThread = null
        
        try {
            recorder?.stop()
            recorder?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        recorder = null
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopBreathMonitoring()
    }
}
