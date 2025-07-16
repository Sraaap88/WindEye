package com.example.windeye

import android.app.Activity
import android.os.Bundle
import com.example.windeye.databinding.ActivityMainBinding

class MainActivity : Activity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var eyeAnimationManager: EyeAnimationManager
    private lateinit var windGauge: WindGauge

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        eyeAnimationManager = EyeAnimationManager()
        windGauge = WindGauge(this)

        // Met à jour l'œil toutes les 50 ms avec la force du souffle
        windGauge.setOnWindChangeListener { force ->
            eyeAnimationManager.updateWindForce(force)
            binding.eyeView.updateState(eyeAnimationManager.currentState)
        }

        windGauge.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        windGauge.stopListening()
    }
}
