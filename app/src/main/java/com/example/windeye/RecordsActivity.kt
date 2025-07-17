package com.example.windeye

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.ScrollView
import android.widget.LinearLayout

class RecordsActivity : Activity() {
    
    private lateinit var recordsScrollView: ScrollView
    private lateinit var recordsLayout: LinearLayout
    private lateinit var backButton: Button
    
    private var recordsManager: RecordsManager? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_records)
        
        recordsManager = RecordsManager(this)
        
        initViews()
        setupListeners()
        displayRecords()
    }
    
    private fun initViews() {
        recordsScrollView = findViewById(R.id.recordsScrollView)
        recordsLayout = findViewById(R.id.recordsLayout)
        backButton = findViewById(R.id.backButton)
    }
    
    private fun setupListeners() {
        backButton.setOnClickListener {
            finish()
        }
    }
    
    private fun displayRecords() {
        recordsLayout.removeAllViews()
        
        // Record général
        addSectionTitle("🏆 RECORD GÉNÉRAL")
        val generalRecord = recordsManager?.getGeneralRecord()
        if (generalRecord != null) {
            addRecordItem("${String.format("%.1f", generalRecord.first)}s", generalRecord.second, generalRecord.third)
        } else {
            addEmptyMessage("Aucun record encore")
        }
        
        addSpacer()
        
        // Records par catégorie
        addSectionTitle("📊 RECORDS PAR CATÉGORIE")
        val categoryRecords = recordsManager?.getCategoryRecords() ?: emptyList()
        if (categoryRecords.isNotEmpty()) {
            categoryRecords.take(8).forEach { (category, time, player) ->
                addRecordItem("${String.format("%.1f", time)}s", player, category)
            }
        } else {
            addEmptyMessage("Aucun record par catégorie")
        }
        
        addSpacer()
        
        // Courses récentes
        addSectionTitle("📅 COURSES RÉCENTES")
        val recentRaces = recordsManager?.getRecentRaces(8) ?: emptyList()
        if (recentRaces.isNotEmpty()) {
            recentRaces.forEach { race ->
                val timeText = String.format("%.1f", race.time) + "s"
                val details = "${race.trackType} • ${race.weather}"
                addRecordItem(timeText, race.playerName, details)
            }
        } else {
            addEmptyMessage("Aucune course récente")
        }
    }
    
    private fun addSectionTitle(title: String) {
        val titleView = TextView(this).apply {
            text = title
            textSize = 20f
            setTextColor(android.graphics.Color.WHITE)
            setPadding(0, 30, 0, 15)
            gravity = android.view.Gravity.CENTER
        }
        recordsLayout.addView(titleView)
    }
    
    private fun addRecordItem(time: String, player: String, details: String) {
        val itemLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 15, 20, 15)
            setBackgroundColor(android.graphics.Color.parseColor("#333333"))
        }
        
        val timeView = TextView(this).apply {
            text = "⏱️ $time"
            textSize = 18f
            setTextColor(android.graphics.Color.parseColor("#FFD700"))
            gravity = android.view.Gravity.CENTER
        }
        
        val playerView = TextView(this).apply {
            text = "👤 $player"
            textSize = 16f
            setTextColor(android.graphics.Color.WHITE)
            gravity = android.view.Gravity.CENTER
        }
        
        val detailsView = TextView(this).apply {
            text = details
            textSize = 14f
            setTextColor(android.graphics.Color.parseColor("#CCCCCC"))
            gravity = android.view.Gravity.CENTER
        }
        
        itemLayout.addView(timeView)
        itemLayout.addView(playerView)
        itemLayout.addView(detailsView)
        
        recordsLayout.addView(itemLayout)
        
        // Espacement
        val spacer = TextView(this).apply {
            text = ""
            textSize = 8f
        }
        recordsLayout.addView(spacer)
    }
    
    private fun addEmptyMessage(message: String) {
        val emptyView = TextView(this).apply {
            text = message
            textSize = 16f
            setTextColor(android.graphics.Color.parseColor("#888888"))
            gravity = android.view.Gravity.CENTER
            setPadding(20, 20, 20, 20)
        }
        recordsLayout.addView(emptyView)
    }
    
    private fun addSpacer() {
        val spacer = TextView(this).apply {
            text = ""
            textSize = 20f
        }
        recordsLayout.addView(spacer)
    }
}
