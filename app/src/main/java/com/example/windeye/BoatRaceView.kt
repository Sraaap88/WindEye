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
    private var raceType: RaceType = RaceType.CLASSIC
    
    // État de la course
    private var playerProgress: Float = 0f // 0-100%
    private var playerSpeed: Float = 0f // 0-1
    private var ia1Progress: Float = 0f
    private var ia2Progress: Float = 0f 
    private var ia3Progress: Float = 0f
    
    // Timer de course
    private var raceStartTime: Long = 0
    private var raceTime: Float = 0f
    private var raceFinished: Boolean = false
    private var maxRaceTime: Float = 40f
    
    // IA
    private var ia1Speed: Float = 0f
    private var ia2Speed: Float = 0f
    private var ia3Speed: Float = 0f
    private val ia1Strategy = AIStrategy.STEADY
    private val ia2Strategy = AIStrategy.BURST
    private val ia3Strategy = AIStrategy.SPRINT_FINISH
    
    // Animation
    private var frameCount: Long = 0
    private var waveOffset: Float = 0f
    
    // Callbacks
    private var onRaceFinished: ((Int, Float) -> Unit)? = null
    private var onTimerUpdate: ((Float) -> Unit)? = null
    private var onPositionUpdate: ((Int) -> Unit)? = null
    
    enum class AIStrategy {
        STEADY,      // Vitesse constante
        BURST,       // Pics de vitesse
        SPRINT_FINISH // Lent début, rapide fin
    }
    
    // Couleurs des bateaux
    private val playerColor = Color.parseColor("#FF4444") // Rouge
    private val ia1Color = Color.parseColor("#4444FF")    // Bleu
    private val ia2Color = Color.parseColor("#44FF44")    // Vert
    private val ia3Color = Color.parseColor("#FFAA00")    // Orange
    
    // Paint objects
    private val waterPaint = Paint().apply {
        color = Color.parseColor("#006994")
        style = Paint.Style.FILL
    }
    
    private val lanePaint = Paint().apply {
        color = Color.parseColor("#FFFFFF")
        style = Paint.Style.STROKE
        strokeWidth = 4f
        alpha = 150
    }
    
    private val finishLinePaint = Paint().apply {
        color = Color.parseColor("#FFD700")
        style = Paint.Style.FILL
    }
    
    private val startLinePaint = Paint().apply {
        color = Color.parseColor("#FFFFFF")
        style = Paint.Style.FILL
    }
    
    private val boatPaint = Paint().apply {
        style = Paint.Style.FILL
    }
    
    private val boatShadowPaint = Paint().apply {
        color = Color.parseColor("#333333")
        style = Paint.Style.FILL
        alpha = 150
    }
    
    private val namePaint = Paint().apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 16f
        isAntiAlias = true
        setShadowLayer(2f, 1f, 1f, Color.BLACK)
    }
    
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        textSize = 20f
        setShadowLayer(2f, 1f, 1f, Color.BLACK)
    }
    
    fun setPlayerName(name: String) {
        playerName = name
    }
    
    fun setRaceType(type: RaceType) {
        raceType = type
        maxRaceTime = type.duration
    }
    
    fun setRecordsManager(manager: RecordsManager?) {
        recordsManager = manager
    }
    
    fun setOnRaceFinished(callback: (Int, Float) -> Unit) {
        onRaceFinished = callback
    }
    
    fun setOnTimerUpdate(callback: (Float) -> Unit) {
        onTimerUpdate = callback
    }
    
    fun setOnPositionUpdate(callback: (Int) -> Unit) {
        onPositionUpdate = callback
    }
    
    fun startRace() {
        // Reset état
        playerProgress = 0f
        ia1Progress = 0f
        ia2Progress = 0f
        ia3Progress = 0f
        playerSpeed = 0f
        ia1Speed = 0f
        ia2Speed = 0f
        ia3Speed = 0f
        raceFinished = false
        
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
    
    private fun handleWindInput(amplitude: Float) {
        // Convertir amplitude en vitesse (0-1)
        playerSpeed = amplitude.coerceIn(0f, 1f)
        
        // Vitesse d'avancement ajustée selon le type de course
        val speedMultiplier = when (raceType) {
            RaceType.SPRINT -> 2.0f      // Course rapide
            RaceType.CLASSIC -> 1.5f     // Vitesse normale
            RaceType.ENDURANCE -> 1.2f   // Plus lent, plus stratégique
        }
        
        // Avancer le joueur
        playerProgress += playerSpeed * speedMultiplier
        
        invalidate()
    }
    
    private fun updateAI() {
        val timeProgress = raceTime / maxRaceTime
        
        // IA 1 - Steady (vitesse constante)
        ia1Speed = when (raceType) {
            RaceType.SPRINT -> 0.6f + Random.nextFloat() * 0.1f
            RaceType.CLASSIC -> 0.5f + Random.nextFloat() * 0.1f
            RaceType.ENDURANCE -> 0.4f + Random.nextFloat() * 0.1f
        }
        
        // IA 2 - Burst (pics de vitesse)
        ia2Speed = if (Random.nextFloat() < 0.3f) {
            when (raceType) {
                RaceType.SPRINT -> 0.9f + Random.nextFloat() * 0.1f
                RaceType.CLASSIC -> 0.8f + Random.nextFloat() * 0.1f
                RaceType.ENDURANCE -> 0.7f + Random.nextFloat() * 0.1f
            }
        } else {
            when (raceType) {
                RaceType.SPRINT -> 0.4f + Random.nextFloat() * 0.1f
                RaceType.CLASSIC -> 0.3f + Random.nextFloat() * 0.1f
                RaceType.ENDURANCE -> 0.25f + Random.nextFloat() * 0.1f
            }
        }
        
        // IA 3 - Sprint finish (lent début, rapide fin)
        ia3Speed = if (timeProgress < 0.7f) {
            when (raceType) {
                RaceType.SPRINT -> 0.3f + Random.nextFloat() * 0.1f
                RaceType.CLASSIC -> 0.25f + Random.nextFloat() * 0.1f
                RaceType.ENDURANCE -> 0.2f + Random.nextFloat() * 0.1f
            }
        } else {
            when (raceType) {
                RaceType.SPRINT -> 0.8f + Random.nextFloat() * 0.2f
                RaceType.CLASSIC -> 0.7f + Random.nextFloat() * 0.2f
                RaceType.ENDURANCE -> 0.6f + Random.nextFloat() * 0.2f
            }
        }
        
        // Avancer les IA avec vitesse selon type de course
        when (raceType) {
            RaceType.SPRINT -> {
                ia1Progress += ia1Speed * 2.0f
                ia2Progress += ia2Speed * 2.0f
                ia3Progress += ia3Speed * 2.0f
            }
            RaceType.CLASSIC -> {
                ia1Progress += ia1Speed * 1.5f
                ia2Progress += ia2Speed * 1.5f
                ia3Progress += ia3Speed * 1.5f
            }
            RaceType.ENDURANCE -> {
                ia1Progress += ia1Speed * 1.2f
                ia2Progress += ia2Speed * 1.2f
                ia3Progress += ia3Speed * 1.2f
            }
        }
    }
    
    private fun checkRaceEnd() {
        if (raceFinished) return
        
        // Vérifier si quelqu'un a gagné (atteint 100%) ou si le temps est écoulé
        val positions = listOf(
            Pair("Player", playerProgress),
            Pair("IA1", ia1Progress),
            Pair("IA2", ia2Progress),
            Pair("IA3", ia3Progress)
        ).sortedByDescending { it.second }
        
        val hasWinner = positions[0].second >= 100f
        val timeUp = raceTime >= maxRaceTime
        
        if (hasWinner || timeUp) {
            raceFinished = true
            
            // Trouver la position du joueur
            val playerPosition = positions.indexOfFirst { it.first == "Player" } + 1
            
            onRaceFinished?.invoke(playerPosition, raceTime)
            windGauge?.stopListening()
        } else {
            // Mettre à jour la position actuelle du joueur
            val currentPosition = positions.indexOfFirst { it.first == "Player" } + 1
            onPositionUpdate?.invoke(currentPosition)
        }
    }
    
    private fun startAnimation() {
        post(object : Runnable {
            override fun run() {
                if (!raceFinished) {
                    frameCount++
                    
                    // Mettre à jour le timer
                    raceTime = (System.currentTimeMillis() - raceStartTime) / 1000f
                    onTimerUpdate?.invoke(raceTime)
                    
                    // Mettre à jour les IA
                    updateAI()
                    
                    // Vérifier fin de course
                    checkRaceEnd()
                    
                    // Animation des vagues
                    waveOffset += 3f
                    
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
        
        // Fond d'eau
        canvas.drawRect(0f, 0f, width, height, waterPaint)
        
        // Vagues d'arrière-plan
        drawWaves(canvas, width, height)
        
        // Couloirs de course
        drawLanes(canvas, width, height)
        
        // Ligne de départ (en bas)
        canvas.drawRect(0f, height - 20f, width, height, startLinePaint)
        
        // Ligne d'arrivée (en haut)
        drawFinishLine(canvas, width, height)
        
        // Bateaux
        drawBoats(canvas, width, height)
        
        // Noms des coureurs
        drawNames(canvas, width, height)
        
        // Interface
        drawUI(canvas, width, height)
    }
    
    private fun drawWaves(canvas: Canvas, width: Float, height: Float) {
        val wavePaint = Paint().apply {
            color = Color.parseColor("#4682B4")
            style = Paint.Style.STROKE
            strokeWidth = 2f
            alpha = 100
        }
        
        for (i in 0 until 15) {
            val y = (i * height / 15 + waveOffset) % (height + 30f)
            
            val path = Path()
            path.moveTo(0f, y)
            
            for (x in 0..width.toInt() step 20) {
                val waveY = y + sin(x * 0.03f + frameCount * 0.05f) * 8f
                path.lineTo(x.toFloat(), waveY)
            }
            
            canvas.drawPath(path, wavePaint)
        }
    }
    
    private fun drawLanes(canvas: Canvas, width: Float, height: Float) {
        val laneWidth = width / 4f
        
        // Lignes de séparation des couloirs
        for (i in 1 until 4) {
            val x = i * laneWidth
            val dashPath = DashPathEffect(floatArrayOf(15f, 10f), waveOffset * 0.5f)
            lanePaint.pathEffect = dashPath
            canvas.drawLine(x, 0f, x, height, lanePaint)
        }
    }
    
    private fun drawFinishLine(canvas: Canvas, width: Float, height: Float) {
        // Ligne d'arrivée damier
        val stripeWidth = width / 20f
        for (i in 0 until 20) {
            val paint = if (i % 2 == 0) finishLinePaint else Paint().apply {
                color = Color.BLACK
            }
            canvas.drawRect(i * stripeWidth, 0f, (i + 1) * stripeWidth, 25f, paint)
        }
        
        // Texte "ARRIVÉE"
        textPaint.textSize = 16f
        canvas.drawText("🏁 ARRIVÉE 🏁", width / 2f, 40f, textPaint)
    }
    
    private fun drawBoats(canvas: Canvas, width: Float, height: Float) {
        val laneWidth = width / 4f
        val boatSize = 25f
        val raceHeight = height - 100f // Zone de course (sans ligne départ/arrivée)
        
        // Positions Y des bateaux selon leur progression
        val playerY = height - 60f - (playerProgress / 100f * raceHeight)
        val ia1Y = height - 60f - (ia1Progress / 100f * raceHeight)
        val ia2Y = height - 60f - (ia2Progress / 100f * raceHeight)
        val ia3Y = height - 60f - (ia3Progress / 100f * raceHeight)
        
        // Positions X des couloirs
        val playerX = laneWidth * 0.5f
        val ia1X = laneWidth * 1.5f
        val ia2X = laneWidth * 2.5f
        val ia3X = laneWidth * 3.5f
        
        // Dessiner les bateaux avec ombres
        drawBoat(canvas, playerX, playerY, boatSize, playerColor, playerSpeed)
        drawBoat(canvas, ia1X, ia1Y, boatSize, ia1Color, ia1Speed)
        drawBoat(canvas, ia2X, ia2Y, boatSize, ia2Color, ia2Speed)
        drawBoat(canvas, ia3X, ia3Y, boatSize, ia3Color, ia3Speed)
    }
    
    private fun drawBoat(canvas: Canvas, x: Float, y: Float, size: Float, color: Int, speed: Float) {
        // Ombre
        canvas.drawOval(x - size + 3f, y + 3f, x + size + 3f, y + size + 3f, boatShadowPaint)
        
        // Corps du bateau
        boatPaint.color = color
        canvas.drawOval(x - size, y, x + size, y + size, boatPaint)
        
        // Sillage si en mouvement
        if (speed > 0.1f) {
            val wakePaint = Paint().apply {
                color = Color.WHITE
                alpha = (150 * speed).toInt()
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }
            
            for (i in 1..3) {
                canvas.drawLine(
                    x, y + size + i * 8f,
                    x, y + size + i * 8f + 12f,
                    wakePaint
                )
            }
        }
        
        // Effet de vitesse
        if (speed > 0.5f) {
            val speedPaint = Paint().apply {
                color = Color.YELLOW
                alpha = (200 * speed).toInt()
            }
            canvas.drawCircle(x, y + size / 2f, size * 0.3f, speedPaint)
        }
    }
    
    private fun drawNames(canvas: Canvas, width: Float, height: Float) {
        val laneWidth = width / 4f
        
        // Noms en bas de chaque couloir
        namePaint.color = playerColor
        canvas.drawText(playerName, laneWidth * 0.5f, height - 25f, namePaint)
        
        namePaint.color = ia1Color
        canvas.drawText("Capitaine Bleu", laneWidth * 1.5f, height - 25f, namePaint)
        
        namePaint.color = ia2Color
        canvas.drawText("Marin Vert", laneWidth * 2.5f, height - 25f, namePaint)
        
        namePaint.color = ia3Color
        canvas.drawText("Amiral Orange", laneWidth * 3.5f, height - 25f, namePaint)
    }
    
    private fun drawUI(canvas: Canvas, width: Float, height: Float) {
        // Temps restant
        val timeLeft = maxOf(0f, maxRaceTime - raceTime)
        textPaint.textSize = 18f
        textPaint.color = if (timeLeft < 10f) Color.RED else Color.WHITE
        canvas.drawText("⏰ ${String.format("%.1f", timeLeft)}s", width - 100f, 30f, textPaint)
        
        // Progression du joueur
        textPaint.textSize = 16f
        textPaint.color = Color.WHITE
        canvas.drawText("💨 ${(playerSpeed * 100).toInt()}%", 80f, 30f, textPaint)
        
        // Message de fin
        if (raceFinished) {
            val overlayPaint = Paint().apply {
                color = Color.parseColor("#AA000000")
            }
            canvas.drawRect(0f, 0f, width, height, overlayPaint)
            
            textPaint.textSize = 36f
            textPaint.color = Color.parseColor("#FFD700")
            canvas.drawText("COURSE TERMINÉE!", width / 2f, height / 2f, textPaint)
        }
    }
}
