package com.example.windeye

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import android.widget.Button

class GameActivity : Activity() {
    
    private lateinit var boatRaceView: BoatRaceView
    private lateinit var statusText: TextView
    private lateinit var timerText: TextView
    private lateinit var positionText: TextView
    private lateinit var backButton: Button
    
    private var playerName: String = ""
    private var raceType: RaceType = RaceType.CLASSIC
    private var recordsManager: RecordsManager? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        
        playerName = intent.getStringExtra("playerName") ?: "Joueur"
        val raceTypeName = intent.getStringExtra("raceType") ?: "CLASSIC"
        raceType = RaceType.valueOf(raceTypeName)
        recordsManager = RecordsManager(this)
        
        initViews()
        setupGame()
    }
    
    private fun initViews() {
        boatRaceView = findViewById(R.id.boatRaceView)
        statusText = findViewById(R.id.statusText)
        timerText = findViewById(R.id.timerText)
        positionText = findViewById(R.id.positionText)
        backButton = findViewById(R.id.backButton)
        
        statusText.text = "${raceType.emoji} ${raceType.displayName} - $playerName VS IA"
        
        backButton.setOnClickListener {
            finish()
        }
    }
    
    private fun setupGame() {
        boatRaceView.setPlayerName(playerName)
        boatRaceView.setRaceType(raceType)
        boatRaceView.setRecordsManager(recordsManager)
        
        // Callback quand la course est finie
        boatRaceView.setOnRaceFinished { finalPosition, raceTime ->
            runOnUiThread {
                showRaceResult(finalPosition, raceTime)
            }
        }
        
        // Callback pour mettre à jour le timer
        boatRaceView.setOnTimerUpdate { time ->
            runOnUiThread {
                timerText.text = "⏱️ ${String.format("%.1f", time)}s"
            }
        }
        
        // Callback pour mettre à jour la position
        boatRaceView.setOnPositionUpdate { position ->
            runOnUiThread {
                val positionText = when(position) {
                    1 -> "🥇 1er"
                    2 -> "🥈 2ème" 
                    3 -> "🥉 3ème"
                    4 -> "4ème"
                    else -> "$position"
                }
                this.positionText.text = positionText
            }
        }
        
        // Démarrer la course
        boatRaceView.startRace()
    }
    
    private fun showRaceResult(finalPosition: Int, raceTime: Float) {
        val isNewRecord = recordsManager?.saveRaceResult(
            playerName, 
            finalPosition, 
            raceTime, 
            raceType.displayName
        ) ?: false
        
        val positionText = when(finalPosition) {
            1 -> "🥇 VICTOIRE! (1er)"
            2 -> "🥈 Bien joué! (2ème)"
            3 -> "🥉 Pas mal! (3ème)" 
            4 -> "😞 Dernière place"
            else -> "Position: $finalPosition"
        }
        
        val resultText = if (isNewRecord) {
            "$positionText\n🏆 NOUVEAU RECORD!\n${String.format("%.1f", raceTime)}s"
        } else {
            "$positionText\n${String.format("%.1f", raceTime)}s"
        }
        
        statusText.text = resultText
    }
    
    override fun onDestroy() {
        super.onDestroy()
        boatRaceView.stopRace()
    }
}
