package com.example.windeye

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class RaceResult(
    val playerName: String,
    val time: Float,
    val trackType: String,
    val weather: String,
    val timestamp: Long
)

class RecordsManager(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("boat_race_records", Context.MODE_PRIVATE)
    
    fun saveRaceResult(playerName: String, time: Float, trackType: String, weather: String): Boolean {
        val raceResult = RaceResult(playerName, time, trackType, weather, System.currentTimeMillis())
        
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
            put("time", result.time)
            put("trackType", result.trackType)
            put("weather", result.weather)
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
        val recordKey = "record_${result.trackType}_${result.weather}"
        val currentRecord = prefs.getFloat(recordKey, Float.MAX_VALUE)
        
        var isNewRecord = false
        
        // Record général (tous types confondus)
        val generalRecord = prefs.getFloat("record_general", Float.MAX_VALUE)
        if (result.time < generalRecord) {
            prefs.edit().putFloat("record_general", result.time).apply()
            prefs.edit().putString("record_general_player", result.playerName).apply()
            prefs.edit().putString("record_general_details", "${result.trackType} • ${result.weather}").apply()
            isNewRecord = true
        }
        
        // Record par catégorie
        if (result.time < currentRecord) {
            prefs.edit().putFloat(recordKey, result.time).apply()
            prefs.edit().putString("${recordKey}_player", result.playerName).apply()
            isNewRecord = true
        }
        
        // Record personnel
        val personalKey = "personal_${result.playerName}"
        val personalRecord = prefs.getFloat(personalKey, Float.MAX_VALUE)
        if (result.time < personalRecord) {
            prefs.edit().putFloat(personalKey, result.time).apply()
            prefs.edit().putString("${personalKey}_details", "${result.trackType} • ${result.weather}").apply()
        }
        
        return isNewRecord
    }
    
    fun getGeneralRecord(): Triple<Float, String, String>? {
        val time = prefs.getFloat("record_general", Float.MAX_VALUE)
        if (time == Float.MAX_VALUE) return null
        
        val player = prefs.getString("record_general_player", "") ?: ""
        val details = prefs.getString("record_general_details", "") ?: ""
        
        return Triple(time, player, details)
    }
    
    fun getPersonalRecord(playerName: String): Pair<Float, String>? {
        val time = prefs.getFloat("personal_$playerName", Float.MAX_VALUE)
        if (time == Float.MAX_VALUE) return null
        
        val details = prefs.getString("personal_${playerName}_details", "") ?: ""
        return Pair(time, details)
    }
    
    fun getCategoryRecords(): List<Triple<String, Float, String>> {
        val records = mutableListOf<Triple<String, Float, String>>()
        
        val trackTypes = listOf("Ligne droite", "Parcours sinueux", "Circuit ovale", "Parcours en huit", "Parcours zigzag")
        val weatherTypes = listOf("Calme", "Agité", "Tempête")
        
        for (track in trackTypes) {
            for (weather in weatherTypes) {
                val recordKey = "record_${track}_${weather}"
                val time = prefs.getFloat(recordKey, Float.MAX_VALUE)
                if (time != Float.MAX_VALUE) {
                    val player = prefs.getString("${recordKey}_player", "") ?: ""
                    records.add(Triple("$track • $weather", time, player))
                }
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
                obj.getDouble("time").toFloat(),
                obj.getString("trackType"),
                obj.getString("weather"),
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
