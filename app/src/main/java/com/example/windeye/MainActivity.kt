package com.example.windeye

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.Manifest

class MainActivity : Activity() {
    
    private lateinit var statusText: TextView
    private lateinit var gameCodeText: TextView
    private lateinit var joinCodeEdit: EditText
    private lateinit var createButton: Button
    private lateinit var joinButton: Button
    
    private var networkManager: NetworkManager? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initViews()
        setupListeners()
        checkPermissions()
    }
    
    private fun initViews() {
        statusText = findViewById(R.id.statusText)
        gameCodeText = findViewById(R.id.gameCodeText)
        joinCodeEdit = findViewById(R.id.joinCodeEdit)
        createButton = findViewById(R.id.createButton)
        joinButton = findViewById(R.id.joinButton)
    }
    
    private fun setupListeners() {
        createButton.setOnClickListener {
            createGame()
        }
        
        joinButton.setOnClickListener {
            joinGame()
        }
    }
    
    private fun checkPermissions() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 123)
        }
    }
    
    private fun createGame() {
        statusText.text = "Création de la partie..."
        createButton.isEnabled = false
        
        networkManager = NetworkManager()
        networkManager?.startServer { gameCode ->
            runOnUiThread {
                if (gameCode != null) {
                    gameCodeText.text = "Code de partie: $gameCode"
                    statusText.text = "En attente du second joueur..."
                    
                    networkManager?.setOnClientConnected {
                        runOnUiThread {
                            startGame(true, gameCode)
                        }
                    }
                } else {
                    statusText.text = "Erreur lors de la création"
                    createButton.isEnabled = true
                }
            }
        }
    }
    
    private fun joinGame() {
        val gameCode = joinCodeEdit.text.toString().trim()
        if (gameCode.isEmpty()) {
            Toast.makeText(this, "Entrez un code de partie", Toast.LENGTH_SHORT).show()
            return
        }
        
        statusText.text = "Connexion en cours..."
        joinButton.isEnabled = false
        
        networkManager = NetworkManager()
        networkManager?.connectToServer(gameCode) { success ->
            runOnUiThread {
                if (success) {
                    startGame(false, gameCode)
                } else {
                    statusText.text = "Connexion échouée"
                    joinButton.isEnabled = true
                    Toast.makeText(this, "Impossible de rejoindre la partie", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun startGame(isHost: Boolean, gameCode: String) {
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("isHost", isHost)
        intent.putExtra("gameCode", gameCode)
        startActivity(intent)
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123 && grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission microphone requise", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
