package com.example.windeye

import android.app.Activity
import android.os.Bundle

class MainActivity : Activity() {

    private lateinit var eyeView: EyeView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    
        eyeView = findViewById(R.id.eyeView)
    
        // Vérifier la permission avant de démarrer
        if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            // eyeView démarre déjà automatiquement via son init
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.RECORD_AUDIO), 123)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123 && grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            // La permission a été accordée, eyeView peut maintenant fonctionner
            // Pas besoin d'action supplémentaire car EyeView gère tout automatiquement
        }
    }
}
