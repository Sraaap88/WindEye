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
    
        val eyeView: EyeView = findViewById(R.id.eyeView)
        val windGauge = WindGauge(this) // Si tu l'utilises visuellement, il faudra aussi l'ajouter à l'affichage
    
        // Vérifier la permission avant de démarrer
        if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            // eyeView démarre déjà tout seul via son init
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.RECORD_AUDIO), 123)
        }
    }
    private fun startListening() {
        windGauge.setOnWindChangeListener { force ->
            eyeAnimationManager.updateWindForce(force)
            eyeView.updateState(eyeAnimationManager.currentState)
        }
        windGauge.startListening()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123 && grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            startListening()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        windGauge.stopListening()
    }
}
