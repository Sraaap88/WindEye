package com.example.windeye

import android.app.Activity
import android.os.Bundle
import android.content.pm.PackageManager
import android.Manifest

class MainActivity : Activity() {

    private lateinit var eyeView: EyeView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    
        eyeView = findViewById(R.id.eyeView)
    
        // Vérifier la permission avant de démarrer
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            // Permission déjà accordée - démarrer le monitoring
            eyeView.startBreathMonitoring()
        } else {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 123)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission accordée - démarrer le monitoring
            eyeView.startBreathMonitoring()
        }
    }
}
