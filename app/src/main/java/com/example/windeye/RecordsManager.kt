package com.example.windeye

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class RaceResult(
    val playerName: String,
    val position: Int,
    val time: Float,
    val raceType: String,
    val timestamp: Long
)

class RecordsManager(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("boat_race_records", Context.MODE_PRIVATE)
    
    fun saveRaceResult(playerName: String, position: Int, time: Float, raceType: String): Boolean {
        val raceResult = RaceResult(playerName, position, time, raceType, System.currentTimeMillis())
        
        // Ajouter à l'historique
        addToHistory(raceResult)
        
        // Vérifier si c'est un record
        return checkAndSaveRecord(raceResult)
    }
    
    private fun addToHistory(result: RaceResult) {
        val historyJson = prefs.getString("race_history", "[]")
        val historyArray = JSONArray(historyJson)
        
        val resultJson = JSONObject().apply {
            put("playerName", result.playerName)
            put("position", result.position)
            put("time", result.time)
            put("raceType", result.raceType)
            put("timestamp", result.timestamp)
        }
        
        historyArray.put(resultJson)
        
        // Garder seulement les 50 dernières courses
        while (historyArray.length() > 50) {
            historyArray.remove(0)
        }
        
        prefs.edit().putString("race_history", historyArray.toString()).apply()
    }
    
    private fun checkAndSaveRecord(result: RaceResult): Boolean {
        var isNewRecord = false
        
        // Record de victoire (1ère place) par type de course
        if (result.position == 1) {
            val recordKey = "record_win_${result.raceType}"
            val currentRecord = prefs.getFloat(recordKey, Float.MAX_VALUE)
            
            if (result.time < currentRecord) {
                prefs.edit().putFloat(recordKey, result.time).apply()
                prefs.edit().putString("${recordKey}_player", result.playerName).apply()
                isNewRecord = true
            }
        }
        
        // Meilleure position par type de course
        val positionKey = "best_position_${result.playerName}_${result.raceType}"
        val currentBestPosition = prefs.getInt(positionKey, 4)
        
        if (result.position < currentBestPosition) {
            prefs.edit().putInt(positionKey, result.position).apply()
            prefs.edit().putFloat("${positionKey}_time", result.time).apply()
            isNewRecord = true
        }
        
        return isNewRecord
    }
    
    fun getBestPositionForRaceType(playerName: String, raceType: String): String {
        val positionKey = "best_position_${playerName}_${raceType}"
        val bestPosition = prefs.getInt(positionKey, -1)
        
        return if (bestPosition == -1) {
            "Aucun"
        } else {
            when (bestPosition) {
                1 -> "🥇 1er"
                2 -> "🥈 2ème"
                3 -> "🥉 3ème"
                4 -> "4ème"
                else -> "${bestPosition}ème"
            }
        }
    }
    
    fun getWinRecords(): List<Triple<String, Float, String>> {
        val records = mutableListOf<Triple<String, Float, String>>()
        val raceTypes = listOf("Sprint", "Classique", "Endurance")
        
        for (raceType in raceTypes) {
            val recordKey = "record_win_$raceType"
            val time = prefs.getFloat(recordKey, Float.MAX_VALUE)
            if (time != Float.MAX_VALUE) {
                val player = prefs.getString("${recordKey}_player", "") ?: ""
                records.add(Triple(raceType, time, player))
            }
        }
        
        return records.sortedBy { it.second }
    }
    
    fun getRecentRaces(limit: Int = 10): List<RaceResult> {
        val historyJson = prefs.getString("race_history", "[]")
        val historyArray = JSONArray(historyJson)
        val results = mutableListOf<RaceResult>()
        
        for (i in (historyArray.length() - 1) downTo maxOf(0, historyArray.length() - limit)) {
            val obj = historyArray.getJSONObject(i)
            results.add(RaceResult(
                obj.getString("playerName"),
                obj.getInt("position"),
                obj.getDouble("time").toFloat(),
                obj.getString("raceType"),
                obj.getLong("timestamp")
            ))
        }
        
        return results
    }
    
    fun getTotalPlayers(): Int {
        val historyJson = prefs.getString("race_history", "[]")
        val historyArray = JSONArray(historyJson)
        val players = mutableSetOf<String>()
        
        for (i in 0 until historyArray.length()) {
            val obj = historyArray.getJSONObject(i)
            players.add(obj.getString("playerName"))
        }
        
        return players.size
    }
    
    fun getTotalRaces(): Int {
        val historyJson = prefs.getString("race_history", "[]")
        return JSONArray(historyJson).length()
    }
    
    fun saveLastPlayerName(name: String) {
        prefs.edit().putString("last_player_name", name).apply()
    }
    
    fun getLastPlayerName(): String? {
        return prefs.getString("last_player_name", null)
    }
}
