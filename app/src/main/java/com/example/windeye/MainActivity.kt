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
    
    private lateinit var welcomeText: TextView
    private lateinit var playerNameEdit: EditText
    private lateinit var startRaceButton: Button
    private lateinit var viewRecordsButton: Button
    
    private var recordsManager: RecordsManager? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        recordsManager = RecordsManager(this)
        
        initViews()
        setupListeners()
        checkPermissions()
        updateUI()
    }
    
    private fun initViews() {
        welcomeText = findViewById(R.id.welcomeText)
        playerNameEdit = findViewById(R.id.playerNameEdit)
        startRaceButton = findViewById(R.id.startRaceButton)
        viewRecordsButton = findViewById(R.id.viewRecordsButton)
    }
    
    private fun setupListeners() {
        startRaceButton.setOnClickListener {
            selectRaceType()
        }
        
        viewRecordsButton.setOnClickListener {
            viewRecords()
        }
    }
    
    private fun checkPermissions() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 123)
        }
    }
    
    private fun updateUI() {
        val totalPlayers = recordsManager?.getTotalPlayers() ?: 0
        val totalRaces = recordsManager?.getTotalRaces() ?: 0
        
        welcomeText.text = "🏆 $totalPlayers joueurs • $totalRaces courses"
        
        // Proposer le dernier nom utilisé
        val lastPlayerName = recordsManager?.getLastPlayerName()
        if (!lastPlayerName.isNullOrEmpty()) {
            playerNameEdit.setText(lastPlayerName)
        }
    }
    
    private fun selectRaceType() {
        val playerName = playerNameEdit.text.toString().trim()
        if (playerName.isEmpty()) {
            Toast.makeText(this, "Entrez votre nom", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (playerName.length > 15) {
            Toast.makeText(this, "Nom trop long (15 caractères max)", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Sauvegarder le nom pour la prochaine fois
        recordsManager?.saveLastPlayerName(playerName)
        
        val intent = Intent(this, RaceSelectionActivity::class.java)
        intent.putExtra("playerName", playerName)
        startActivity(intent)
    }
    
    private fun viewRecords() {
        val intent = Intent(this, RecordsActivity::class.java)
        startActivity(intent)
    }
    
    override fun onResume() {
        super.onResume()
        updateUI()
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123 && grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission microphone requise", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
