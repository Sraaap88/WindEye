package com.example.windeye

import android.content.Context
import android.media.MediaRecorder
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import kotlin.math.log10

class EyeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val animationManager = EyeAnimationManager(context)
    private var recorder: MediaRecorder? = null

    init {
        addView(animationManager)
        startBreathMonitoring()
    }

    private fun startBreathMonitoring() {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile("/dev/null")
            prepare()
            start()
        }

        val thread = Thread {
            while (true) {
                try {
                    Thread.sleep(100)
                    recorder?.maxAmplitude?.let {
                        val dB = 20 * log10(it.toDouble().coerceAtLeast(1.0)) / 90.0
                        post { animationManager.updateFromBreath(dB.toFloat()) }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        thread.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        recorder?.stop()
        recorder?.release()
        recorder = null
    }
}
