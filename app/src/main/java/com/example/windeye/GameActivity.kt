package com.example.windeye

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import android.widget.Button

class GameActivity : Activity() {
    
    private lateinit var boatRaceView: BoatRaceView
    private lateinit var statusText: TextView
    private lateinit var timerText: TextView
    private lateinit var backButton: Button
    
    private var playerName: String = ""
    private var recordsManager: RecordsManager? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        
        playerName = intent.getStringExtra("playerName") ?: "Joueur"
        recordsManager = RecordsManager(this)
        
        initViews()
        setupGame()
    }
    
    private fun initViews() {
        boatRaceView = findViewById(R.id.boatRaceView)
        statusText = findViewById(R.id.statusText)
        timerText = findViewById(R.id.timerText)
        backButton = findViewById(R.id.backButton)
        
        statusText.text = "🚤 $playerName prêt à partir!"
        
        backButton.setOnClickListener {
            finish()
        }
    }
    
    private fun setupGame() {
        boatRaceView.setPlayerName(playerName)
        boatRaceView.setRecordsManager(recordsManager)
        
        // Callback quand la course est finie
        boatRaceView.setOnRaceFinished { raceTime, trackType, weather ->
            runOnUiThread {
                showRaceResult(raceTime, trackType, weather)
            }
        }
        
        // Callback pour mettre à jour le timer
        boatRaceView.setOnTimerUpdate { time ->
            runOnUiThread {
                timerText.text = "⏱️ ${String.format("%.1f", time)}s"
            }
        }
        
        // Démarrer la course
        boatRaceView.startRace()
    }
    
    private fun showRaceResult(raceTime: Float, trackType: String, weather: String) {
        val isNewRecord = recordsManager?.saveRaceResult(playerName, raceTime, trackType, weather) ?: false
        
        val resultText = if (isNewRecord) {
            "🏆 NOUVEAU RECORD!\n${String.format("%.1f", raceTime)}s"
        } else {
            "🏁 Course terminée!\n${String.format("%.1f", raceTime)}s"
        }
        
        statusText.text = resultText
    }
    
    override fun onDestroy() {
        super.onDestroy()
        boatRaceView.stopRace()
    }
}
