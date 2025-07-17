package com.example.windeye

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class GameActivity : Activity() {
    
    private lateinit var boatRaceView: BoatRaceView
    private lateinit var statusText: TextView
    
    private var isHost: Boolean = false
    private var gameCode: String = ""
    private var networkManager: NetworkManager? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        
        isHost = intent.getBooleanExtra("isHost", false)
        gameCode = intent.getStringExtra("gameCode") ?: ""
        
        initViews()
        setupGame()
    }
    
    private fun initViews() {
        boatRaceView = findViewById(R.id.boatRaceView)
        statusText = findViewById(R.id.statusText)
        
        statusText.text = "Partie: $gameCode - ${if (isHost) "Joueur 1" else "Joueur 2"}"
    }
    
    private fun setupGame() {
        val playerNumber = if (isHost) 1 else 2
        boatRaceView.setPlayerNumber(playerNumber)
        boatRaceView.setNetworkManager(networkManager)
        
        // Démarrer la course
        boatRaceView.startRace()
        
        // Configuration réseau
        networkManager?.setOnDataReceived { data ->
            runOnUiThread {
                when (data["type"]) {
                    "opponent_position" -> {
                        val progress = data["progress"] as? Float ?: 0f
                        val speed = data["speed"] as? Float ?: 0f
                        boatRaceView.updateOpponentPosition(progress, speed)
                    }
                    "race_finished" -> {
                        val winner = data["winner"] as? Int ?: 0
                        boatRaceView.showWinner(winner)
                    }
                    "new_race" -> {
                        boatRaceView.startNewRace()
                    }
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        boatRaceView.stopRace()
        networkManager?.disconnect()
    }
}
