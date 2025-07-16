package com.example.windeye

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.content.pm.PackageManager
import android.Manifest

class EyeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val animationManager = EyeAnimationManager(context)
    private var windGauge: WindGauge? = null

    init {
        addView(animationManager)
    }

    fun startBreathMonitoring() {
        if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        windGauge = WindGauge(context).apply {
            setOnWindChangeListener { strength ->
                animationManager.updateFromBreath(strength)
            }
            startListening()
        }
    }

    fun stopBreathMonitoring() {
        windGauge?.stopListening()
        windGauge = null
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopBreathMonitoring()
    }
}
