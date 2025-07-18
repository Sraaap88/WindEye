package com.example.windeye

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class RaceSelectionActivity : Activity() {
    
    private lateinit var playerNameText: TextView
    private lateinit var sprintButton: Button
    private lateinit var classicButton: Button
    private lateinit var enduranceButton: Button
    private lateinit var backButton: Button
    
    private var playerName: String = ""
    private var recordsManager: RecordsManager? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_race_selection)
        
        playerName = intent.getStringExtra("playerName") ?: "Joueur"
        recordsManager = RecordsManager(this)
        
        initViews()
        setupListeners()
        displayStats()
    }
    
    private fun initViews() {
        playerNameText = findViewById(R.id.playerNameText)
        sprintButton = findViewById(R.id.sprintButton)
        classicButton = findViewById(R.id.classicButton)
        enduranceButton = findViewById(R.id.enduranceButton)
        backButton = findViewById(R.id.backButton)
        
        playerNameText.text = "🚤 $playerName - Choisissez votre course"
    }
    
    private fun setupListeners() {
        sprintButton.setOnClickListener {
            startRace(RaceType.SPRINT)
        }
        
        classicButton.setOnClickListener {
            startRace(RaceType.CLASSIC)
        }
        
        enduranceButton.setOnClickListener {
            startRace(RaceType.ENDURANCE)
        }
        
        backButton.setOnClickListener {
            finish()
        }
    }
    
    private fun displayStats() {
        // Afficher les records pour chaque type
        val sprintRecord = recordsManager?.getBestPositionForRaceType(playerName, "Sprint") ?: "Aucun"
        val classicRecord = recordsManager?.getBestPositionForRaceType(playerName, "Classique") ?: "Aucun"
        val enduranceRecord = recordsManager?.getBestPositionForRaceType(playerName, "Endurance") ?: "Aucun"
        
        // Mettre à jour les textes des boutons avec les records
        sprintButton.text = "⚡ SPRINT (20s)\nVotre record: $sprintRecord"
        classicButton.text = "🏁 CLASSIQUE (40s)\nVotre record: $classicRecord" 
        enduranceButton.text = "💪 ENDURANCE (60s)\nVotre record: $enduranceRecord"
    }
    
    private fun startRace(raceType: RaceType) {
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("playerName", playerName)
        intent.putExtra("raceType", raceType.name)
        startActivity(intent)
    }
}

enum class RaceType(val displayName: String, val duration: Float, val emoji: String) {
    SPRINT("Sprint", 20f, "⚡"),
    CLASSIC("Classique", 40f, "🏁"),
    ENDURANCE("Endurance", 60f, "💪")
}
