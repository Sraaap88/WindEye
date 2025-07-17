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
    private var recordsManager: RecordsManager? = null
    private var playerName: String = ""
    
    // État de la course
    private var myProgress: Float = 0f // 0-100%
    private var mySpeed: Float = 0f // 0-1
    
    // Timer de course
    private var raceStartTime: Long = 0
    private var raceTime: Float = 0f
    private var raceFinished: Boolean = false
    
    // Paramètres de la course
    private var raceDistance: Float = 1000f
    private var weatherCondition: WeatherType = WeatherType.CALM
    private var raceTrack: RaceTrack = RaceTrack.STRAIGHT
    
    // Animation
    private var waveOffset: Float = 0f
    private var boatRoll: Float = 0f
    private var frameCount: Long = 0
    
    // Callbacks
    private var onRaceFinished: ((Float, String, String) -> Unit)? = null
    private var onTimerUpdate: ((Float) -> Unit)? = null
    
    // Types de trajets aléatoires
    enum class RaceTrack(val displayName: String, val laps: Int, val difficulty: Float) {
        STRAIGHT("Ligne droite", 1, 1.0f),
        CURVES("Parcours sinueux", 1, 1.1f),
        OVAL("Circuit ovale", 2, 1.3f),
        FIGURE_EIGHT("Parcours en huit", 2, 1.4f),
        ZIGZAG("Parcours zigzag", 1, 1.2f)
    }
    
    // Météo aléatoire
    enum class WeatherType(val displayName: String, val waveIntensity: Float, val visibility: Float, val speedMultiplier: Float) {
        CALM("Calme", 0.2f, 1.0f, 1.0f),
        ROUGH("Agité", 0.5f, 0.8f, 0.8f),
        STORM("Tempête", 0.8f, 0.6f, 0.6f)
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
        textSize = 28f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }
    
    fun setPlayerName(name: String) {
        playerName = name
    }
    
    fun setRecordsManager(manager: RecordsManager?) {
        recordsManager = manager
    }
    
    fun setOnRaceFinished(callback: (Float, String, String) -> Unit) {
        onRaceFinished = callback
    }
    
    fun setOnTimerUpdate(callback: (Float) -> Unit) {
        onTimerUpdate = callback
    }
    
    fun startRace() {
        generateRandomRace()
        
        // Démarrer le timer
        raceStartTime = System.currentTimeMillis()
        raceTime = 0f
        
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
        // Type de trajet aléatoire
        raceTrack = RaceTrack.values()[Random.nextInt(RaceTrack.values().size)]
        
        // Météo aléatoire
        weatherCondition = WeatherType.values()[Random.nextInt(WeatherType.values().size)]
        
        // Distance ajustée selon difficulté
        raceDistance = 800f * raceTrack.difficulty
        
        // Reset état
        myProgress = 0f
        mySpeed = 0f
        raceFinished = false
    }
    
    private fun handleWindInput(amplitude: Float) {
        // Convertir amplitude en vitesse (0-1)
        mySpeed = amplitude.coerceIn(0f, 1f)
        
        // Ajuster selon météo
        val adjustedSpeed = mySpeed * weatherCondition.speedMultiplier
        
        // Avancer le bateau
        myProgress += adjustedSpeed * 0.7f
        
        // Vérifier fin de course
        if (myProgress >= 100f && !raceFinished) {
            finishRace()
        }
        
        invalidate()
    }
    
    private fun finishRace() {
        raceFinished = true
        raceTime = (System.currentTimeMillis() - raceStartTime) / 1000f
        
        // Notifier la fin de course
        onRaceFinished?.invoke(raceTime, raceTrack.displayName, weatherCondition.displayName)
        
        windGauge?.stopListening()
    }
    
    private fun startAnimation() {
        post(object : Runnable {
            override fun run() {
                frameCount++
                
                // Mettre à jour le timer
                if (!raceFinished && raceStartTime > 0) {
                    raceTime = (System.currentTimeMillis() - raceStartTime) / 1000f
                    onTimerUpdate?.invoke(raceTime)
                }
                
                // Animation des vagues
                waveOffset += mySpeed * 5f + 1f
                
                // Roulis du bateau selon vitesse et trajet
                val trackMultiplier = raceTrack.difficulty
                boatRoll = sin(frameCount * 0.1f) * mySpeed * 3f * trackMultiplier
                
                invalidate()
                
                if (!raceFinished) {
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
        
        // Ciel
        canvas.drawRect(0f, 0f, width, height * 0.4f, skyPaint)
        
        // Mer
        canvas.drawRect(0f, height * 0.4f, width, height, seaPaint)
        
        // Ligne d'arrivée (se rapproche selon progression)
        if (myProgress > 70f) {
            val finishAlpha = ((myProgress - 70f) / 30f * 255).toInt()
            finishLinePaint.alpha = finishAlpha
            val finishPosition = getFinishLinePosition(width, height)
            canvas.drawRect(finishPosition.left, finishPosition.top, 
                          finishPosition.right, finishPosition.bottom, finishLinePaint)
        }
        
        // Tracé de course
        drawRaceTrack(canvas, width, height)
        
        // Vagues animées
        drawWaves(canvas, width, height, weatherCondition.waveIntensity)
        
        // Éléments du cockpit
        drawCockpit(canvas, width, height)
        
        // Écume selon vitesse
        if (mySpeed > 0.3f) {
            drawFoam(canvas, width, height)
        }
        
        // Effets météo
        drawWeatherEffects(canvas, width, height, weatherCondition.visibility)
        
        // Interface
        drawUI(canvas, width, height)
    }
    
    private fun getFinishLinePosition(width: Float, height: Float): RectF {
        return when (raceTrack) {
            RaceTrack.STRAIGHT -> RectF(width * 0.3f, height * 0.4f, width * 0.7f, height * 0.6f)
            RaceTrack.CURVES -> RectF(width * 0.25f, height * 0.35f, width * 0.75f, height * 0.65f)
            RaceTrack.OVAL -> RectF(width * 0.4f, height * 0.3f, width * 0.6f, height * 0.7f)
            RaceTrack.FIGURE_EIGHT -> RectF(width * 0.35f, height * 0.45f, width * 0.65f, height * 0.55f)
            RaceTrack.ZIGZAG -> RectF(width * 0.2f, height * 0.4f, width * 0.8f, height * 0.6f)
        }
    }
    
    private fun drawRaceTrack(canvas: Canvas, width: Float, height: Float) {
        val trackPaint = Paint().apply {
            color = Color.parseColor("#87CEEB")
            style = Paint.Style.STROKE
            strokeWidth = 6f
            alpha = 100
        }
        
        when (raceTrack) {
            RaceTrack.STRAIGHT -> { /* Pas de tracé visible */ }
            RaceTrack.CURVES -> {
                val path = Path()
                path.moveTo(width * 0.1f, height * 0.5f)
                path.quadTo(width * 0.3f, height * 0.3f, width * 0.5f, height * 0.5f)
                path.quadTo(width * 0.7f, height * 0.7f, width * 0.9f, height * 0.5f)
                canvas.drawPath(path, trackPaint)
            }
            RaceTrack.OVAL -> {
                val oval = RectF(width * 0.2f, height * 0.3f, width * 0.8f, height * 0.7f)
                canvas.drawOval(oval, trackPaint)
            }
            RaceTrack.FIGURE_EIGHT -> {
                val circle1 = RectF(width * 0.1f, height * 0.25f, width * 0.5f, height * 0.55f)
                val circle2 = RectF(width * 0.5f, height * 0.45f, width * 0.9f, height * 0.75f)
                canvas.drawOval(circle1, trackPaint)
                canvas.drawOval(circle2, trackPaint)
            }
            RaceTrack.ZIGZAG -> {
                val path = Path()
                path.moveTo(width * 0.1f, height * 0.5f)
                path.lineTo(width * 0.3f, height * 0.3f)
                path.lineTo(width * 0.5f, height * 0.7f)
                path.lineTo(width * 0.7f, height * 0.3f)
                path.lineTo(width * 0.9f, height * 0.5f)
                canvas.drawPath(path, trackPaint)
            }
        }
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
        // Bord du bateau
        val boatBottom = height * 0.85f
        canvas.drawRect(0f, boatBottom, width, height, boatPaint)
        
        // Cordages
        val ropePaint = Paint().apply {
            color = Color.parseColor("#D2691E")
            strokeWidth = 6f
        }
        canvas.drawLine(width * 0.1f, height * 0.6f, width * 0.15f, height, ropePaint)
        canvas.drawLine(width * 0.9f, height * 0.6f, width * 0.85f, height, ropePaint)
        
        // Barre
        canvas.drawRect(width * 0.45f, height * 0.9f, width * 0.55f, height * 0.95f, boatPaint)
    }
    
    private fun drawFoam(canvas: Canvas, width: Float, height: Float) {
        val foamIntensity = mySpeed
        
        for (i in 0 until (foamIntensity * 15).toInt()) {
            val x = Random.nextFloat() * width
            val y = height * 0.7f + Random.nextFloat() * height * 0.3f
            val radius = Random.nextFloat() * 6f + 2f
            
            foamPaint.alpha = (180 * foamIntensity * Random.nextFloat()).toInt()
            canvas.drawCircle(x, y, radius, foamPaint)
        }
    }
    
    private fun drawWeatherEffects(canvas: Canvas, width: Float, height: Float, visibility: Float) {
        if (visibility < 1.0f) {
            val fogPaint = Paint().apply {
                color = Color.parseColor("#CCCCCC")
                alpha = ((1f - visibility) * 150).toInt()
            }
            canvas.drawRect(0f, 0f, width, height, fogPaint)
            
            if (weatherCondition == WeatherType.STORM) {
                val rainPaint = Paint().apply {
                    color = Color.parseColor("#4682B4")
                    strokeWidth = 2f
                }
                
                for (i in 0 until 30) {
                    val x = Random.nextFloat() * width
                    val y = Random.nextFloat() * height
                    canvas.drawLine(x, y, x, y + 15f, rainPaint)
                }
            }
        }
    }
    
    private fun drawUI(canvas: Canvas, width: Float, height: Float) {
        // Type de trajet
        val trackText = "🏁 ${raceTrack.displayName}"
        canvas.drawText(trackText, width * 0.15f, 35f, textPaint)
        
        // Météo
        val weatherText = "🌊 ${weatherCondition.displayName}"
        canvas.drawText(weatherText, width * 0.15f, 65f, textPaint)
        
        // Souffle
        val windText = "💨 ${(mySpeed * 100).toInt()}%"
        canvas.drawText(windText, width * 0.15f, 95f, textPaint)
        
        // Progression
        val progressText = "${myProgress.toInt()}%"
        canvas.drawText(progressText, width * 0.85f, 35f, textPaint)
        
        // Message de fin
        if (raceFinished) {
            textPaint.textSize = 40f
            canvas.drawText("🏁 FINI!", width / 2f, height / 2f, textPaint)
            textPaint.textSize = 28f
            
            val timeText = String.format("⏱️ %.1fs", raceTime)
            canvas.drawText(timeText, width / 2f, height / 2f + 50f, textPaint)
        }
    }
}
