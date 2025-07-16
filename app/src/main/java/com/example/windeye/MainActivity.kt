package com.example.windeye

import android.app.Activity
import android.os.Bundle

class MainActivity : Activity() {

    private lateinit var eyeAnimationManager: EyeAnimationManager
    private lateinit var windGauge: WindGauge
    private lateinit var eyeView: EyeView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        eyeView = findViewById(R.id.eyeView)
        eyeAnimationManager = EyeAnimationManager()
        windGauge = WindGauge(this)

        // Met à jour l'œil toutes les 50 ms avec la force du souffle
        windGauge.setOnWindChangeListener { force ->
            eyeAnimationManager.updateWindForce(force)
            eyeView.updateState(eyeAnimationManager.currentState)
        }

        windGauge.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        windGauge.stopListening()
    }
}
