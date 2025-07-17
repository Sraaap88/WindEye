package com.example.windeye

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.*
import kotlin.random.Random

class BoatRaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    private var windGauge: WindGauge? = null
    private var networkManager: NetworkManager? = null
    private var playerNumber: Int = 1
    
    // État de la course
    private var myProgress: Float = 0f // 0-100%
    private var opponentProgress: Float = 0f
    private var mySpeed: Float = 0f // 0-1
    private var opponentSpeed: Float = 0f
    
    // Paramètres de la course
    private var raceDistance: Float = 1000f
    private var weatherCondition: WeatherType = WeatherType.CALM
    private var raceFinished: Boolean = false
    private var winner: Int = 0
    private var raceTrack: RaceTrack = RaceTrack.STRAIGHT
    private var trackProgress: Float = 0f // Position sur le tracé (0-100%)
    
    // Animation
    private var waveOffset: Float = 0f
    private var boatRoll: Float = 0f
    private var frameCount: Long = 0
    
    // Météo aléatoire
    enum class WeatherType(val waveIntensity: Float, val visibility: Float) {
        CALM(0.2f, 1.0f),
        ROUGH(0.5f, 0.8f),
        STORM(0.8f, 0.6f)
    }
    
    // Types de trajets aléatoires
    enum class RaceTrack(val displayName: String, val laps: Int) {
        STRAIGHT("Ligne droite", 1),
        CURVES("Parcours sinueux", 1),
        OVAL("Circuit ovale", 2),
        FIGURE_EIGHT("Parcours en huit", 2),
        ZIGZAG("Parcours zigzag", 1)
    }
    
    // Paint objects
    private val skyPaint = Paint().apply {
        shader = LinearGradient(0f, 0f, 0f, 400f, 
            intArrayOf(Color.parseColor("#87CEEB"), Color.parseColor("#4682B4")), 
            null, Shader.TileMode.CLAMP)
    }
    
    private val seaPaint = Paint().apply {
        color = Color.parseColor("#006994")
    }
    
    private val wavePaint = Paint().apply {
        color = Color.parseColor("#4682B4")
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    
    private val boatPaint = Paint().apply {
        color = Color.parseColor("#8B4513")
        style = Paint.Style.FILL
    }
    
    private val mirrorPaint = Paint().apply {
        color = Color.parseColor("#E6E6FA")
        style = Paint.Style.FILL
    }
    
    private val mirrorBorderPaint = Paint().apply {
        color = Color.parseColor("#333333")
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    
    private val finishLinePaint = Paint().apply {
        color = Color.parseColor("#FF0000")
        alpha = 150
        style = Paint.Style.FILL
    }
    
    private val foamPaint = Paint().apply {
        color = Color.parseColor("#FFFFFF")
        alpha = 180
    }
    
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 32f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }
    
    fun setPlayerNumber(number: Int) {
        playerNumber = number
    }
    
    fun setNetworkManager(manager: NetworkManager?) {
        networkManager = manager
    }
    
    fun startRace() {
        // Générer parcours et météo aléatoires
        generateRandomRace()
        
        // Démarrer la détection de souffle
        windGauge = WindGauge(context)
        windGauge?.setOnWindChangeListener { amplitude ->
            if (!raceFinished) {
                handleWindInput(amplitude)
            }
        }
        windGauge?.startListening()
        
        // Démarrer l'animation
        startAnimation()
    }
    
    private fun generateRandomRace() {
        // Distance aléatoire selon le type de trajet
        raceDistance = Random.nextFloat() * 400f + 800f
        
        // Type de trajet aléatoire
        raceTrack = RaceTrack.values()[Random.nextInt(RaceTrack.values().size)]
        
        // Ajuster la distance selon le type
        when (raceTrack) {
            RaceTrack.OVAL, RaceTrack.FIGURE_EIGHT -> raceDistance *= raceTrack.laps
            else -> { /* Garder distance normale */ }
        }
        
        // Météo aléatoire
        weatherCondition = WeatherType.values()[Random.nextInt(WeatherType.values().size)]
        
        // Reset positions
        myProgress = 0f
        opponentProgress = 0f
        trackProgress = 0f
        mySpeed = 0f
        opponentSpeed = 0f
        raceFinished = false
        winner = 0
    }
    
    private fun handleWindInput(amplitude: Float) {
        // Convertir amplitude en vitesse (0-1)
        mySpeed = amplitude.coerceIn(0f, 1f)
        
        // Ajuster selon météo
        val weatherMultiplier = when (weatherCondition) {
            WeatherType.CALM -> 1.0f
            WeatherType.ROUGH -> 0.8f
            WeatherType.STORM -> 0.6f
        }
        
        val adjustedSpeed = mySpeed * weatherMultiplier
        
        // Avancer le bateau
        myProgress += adjustedSpeed * 0.8f // Vitesse d'avancement
        
        // Vérifier victoire
        if (myProgress >= 100f && !raceFinished) {
            raceFinished = true
            winner = playerNumber
            networkManager?.sendRaceFinished(playerNumber)
        }
        
        // Envoyer position à l'adversaire
        networkManager?.sendPosition(myProgress, adjustedSpeed)
        
        invalidate()
    }
    
    fun updateOpponentPosition(progress: Float, speed: Float) {
        opponentProgress = progress
        opponentSpeed = speed
        
        // Vérifier si adversaire a gagné
        if (progress >= 100f && !raceFinished) {
            raceFinished = true
            winner = if (playerNumber == 1) 2 else 1
        }
        
        invalidate()
    }
    
    fun showWinner(winnerNumber: Int) {
        raceFinished = true
        winner = winnerNumber
        invalidate()
    }
    
    fun startNewRace() {
        generateRandomRace()
        invalidate()
    }
    
    private fun startAnimation() {
        post(object : Runnable {
            override fun run() {
                if (!raceFinished) {
                    frameCount++
                    
                    // Animation des vagues
                    waveOffset += mySpeed * 5f + 1f
                    
                    // Roulis du bateau selon vitesse
                    boatRoll = sin(frameCount * 0.1f) * mySpeed * 3f
                    
                    invalidate()
                    postDelayed(this, 16) // ~60 FPS
                }
            }
        })
    }
    
    fun stopRace() {
        windGauge?.stopListening()
        removeCallbacks(null)
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val width = width.toFloat()
        val height = height.toFloat()
        
        // Appliquer effets météo
        val visibility = weatherCondition.visibility
        val waveIntensity = weatherCondition.waveIntensity
        
        // Ciel
        canvas.drawRect(0f, 0f, width, height * 0.4f, skyPaint)
        
        // Mer
        canvas.drawRect(0f, height * 0.4f, width, height, seaPaint)
        
        // Ligne d'arrivée (se rapproche selon progression)
        if (myProgress > 70f) {
            val finishAlpha = ((myProgress - 70f) / 30f * 255).toInt()
            finishLinePaint.alpha = finishAlpha
            canvas.drawRect(width * 0.3f, height * 0.4f, width * 0.7f, height * 0.6f, finishLinePaint)
        }
        
        // Vagues animées
        drawWaves(canvas, width, height, waveIntensity)
        
        // Éléments du cockpit
        drawCockpit(canvas, width, height)
        
        // Écume selon vitesse
        if (mySpeed > 0.3f) {
            drawFoam(canvas, width, height)
        }
        
        // Miroir de navigation
        drawNavigationMirror(canvas, width, height)
        
        // Effets météo
        drawWeatherEffects(canvas, width, height, visibility)
        
        // Interface
        drawUI(canvas, width, height)
    }
    
    private fun drawWaves(canvas: Canvas, width: Float, height: Float, intensity: Float) {
        val seaTop = height * 0.4f
        val numWaves = 8
        
        for (i in 0 until numWaves) {
            val y = seaTop + (height - seaTop) * (i / numWaves.toFloat())
            val waveHeight = intensity * 20f * (1f - i / numWaves.toFloat())
            
            val path = Path()
            path.moveTo(0f, y)
            
            for (x in 0..width.toInt() step 20) {
                val waveY = y + sin((x + waveOffset) * 0.02f) * waveHeight
                path.lineTo(x.toFloat(), waveY)
            }
            
            wavePaint.alpha = (255 * (1f - i / numWaves.toFloat())).toInt()
            canvas.drawPath(path, wavePaint)
        }
    }
    
    private fun drawCockpit(canvas: Canvas, width: Float, height: Float) {
        // Bord du bateau (bas)
        val boatBottom = height * 0.85f
        canvas.drawRect(0f, boatBottom, width, height, boatPaint)
        
        // Cordages (gauche et droite)
        val ropePaint = Paint().apply {
            color = Color.parseColor("#D2691E")
            strokeWidth = 6f
        }
        
        // Cordage gauche
        canvas.drawLine(width * 0.1f, height * 0.6f, width * 0.15f, height, ropePaint)
        
        // Cordage droit
        canvas.drawLine(width * 0.9f, height * 0.6f, width * 0.85f, height, ropePaint)
        
        // Barre (centre-bas)
        canvas.drawRect(width * 0.45f, height * 0.9f, width * 0.55f, height * 0.95f, boatPaint)
    }
    
    private fun drawFoam(canvas: Canvas, width: Float, height: Float) {
        val foamIntensity = mySpeed
        
        for (i in 0 until (foamIntensity * 20).toInt()) {
            val x = Random.nextFloat() * width
            val y = height * 0.7f + Random.nextFloat() * height * 0.3f
            val radius = Random.nextFloat() * 8f + 2f
            
            foamPaint.alpha = (180 * foamIntensity * Random.nextFloat()).toInt()
            canvas.drawCircle(x, y, radius, foamPaint)
        }
    }
    
    private fun drawNavigationMirror(canvas: Canvas, width: Float, height: Float) {
        val mirrorX = width * 0.8f
        val mirrorY = height * 0.15f
        val mirrorRadius = 60f
        
        // Miroir
        canvas.drawCircle(mirrorX, mirrorY, mirrorRadius, mirrorPaint)
        canvas.drawCircle(mirrorX, mirrorY, mirrorRadius, mirrorBorderPaint)
        
        // Bateau adverse dans le miroir
        val progressDiff = myProgress - opponentProgress
        val opponentColor = if (playerNumber == 1) Color.BLUE else Color.RED
        
        val opponentPaint = Paint().apply {
            color = opponentColor
            textSize = 24f
            textAlign = Paint.Align.CENTER
        }
        
        // Position de l'adversaire dans le miroir
        val opponentX = mirrorX + progressDiff * 0.5f
        val opponentY = mirrorY
        
        canvas.drawText("⛵", opponentX, opponentY + 8f, opponentPaint)
        
        // Flèche directionnelle
        val arrowPaint = Paint().apply {
            color = Color.BLACK
            textSize = 20f
            textAlign = Paint.Align.CENTER
        }
        
        val arrow = when {
            progressDiff > 5f -> "⬇️" // Derrière
            progressDiff < -5f -> "⬆️" // Devant
            else -> "↔️" // À côté
        }
        
        canvas.drawText(arrow, mirrorX, mirrorY - 35f, arrowPaint)
    }
    
    private fun drawWeatherEffects(canvas: Canvas, width: Float, height: Float, visibility: Float) {
        if (visibility < 1.0f) {
            // Brouillard/pluie
            val fogPaint = Paint().apply {
                color = Color.parseColor("#CCCCCC")
                alpha = ((1f - visibility) * 150).toInt()
            }
            canvas.drawRect(0f, 0f, width, height, fogPaint)
            
            // Gouttes de pluie si tempête
            if (weatherCondition == WeatherType.STORM) {
                val rainPaint = Paint().apply {
                    color = Color.parseColor("#4682B4")
                    strokeWidth = 2f
                }
                
                for (i in 0 until 50) {
                    val x = Random.nextFloat() * width
                    val y = Random.nextFloat() * height
                    canvas.drawLine(x, y, x, y + 20f, rainPaint)
                }
            }
        }
    }
    
    private fun drawUI(canvas: Canvas, width: Float, height: Float) {
        // Indicateur météo
        val weatherText = when (weatherCondition) {
            WeatherType.CALM -> "🌊 Calme"
            WeatherType.ROUGH -> "🌊🌊 Agité"
            WeatherType.STORM -> "🌊🌊🌊 Tempête"
        }
        
        canvas.drawText(weatherText, width * 0.15f, 50f, textPaint)
        
        // Indicateur de souffle
        val windText = "💨 ${(mySpeed * 100).toInt()}%"
        canvas.drawText(windText, width * 0.15f, 90f, textPaint)
        
        // Message de victoire
        if (raceFinished) {
            val winnerText = if (winner == playerNumber) "🏆 VICTOIRE!" else "😞 Défaite..."
            textPaint.textSize = 48f
            canvas.drawText(winnerText, width / 2f, height / 2f, textPaint)
            textPaint.textSize = 32f
            
            canvas.drawText("Soufflez fort pour une nouvelle course!", width / 2f, height / 2f + 60f, textPaint)
        }
    }
}
