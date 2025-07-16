package com.example.windeye

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.view.animation.AlphaAnimation
import android.view.animation.Animation

class EyeAnimationManager(context: Context) : FrameLayout(context) {

    private val eyeStages = arrayOf(
        R.drawable.eye_stage_01,
        R.drawable.eye_stage_02,
        R.drawable.eye_stage_03,
        R.drawable.eye_stage_04,
        R.drawable.eye_stage_05,
        R.drawable.eye_stage_06,
        R.drawable.eye_stage_07,
        R.drawable.eye_stage_08,
        R.drawable.eye_stage_09,
        R.drawable.eye_stage_10
    )

    // Plusieurs couches d'images pour superposition
    private val imageLayers = mutableListOf<ImageView>()
    private var currentStage = 0
    private var peakStage = 0 // Garder trace du pic
    private var lastPeakTime = 0L // Timestamp du dernier pic
    private val handler = Handler(Looper.getMainLooper())
    private val PEAK_HOLD_DURATION = 250L // 1/4 de seconde en millisecondes

    init {
        // Créer 3 couches d'images superposées
        repeat(3) { layerIndex ->
            val imageView = ImageView(context).apply {
                layoutParams = LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.FIT_CENTER
                alpha = 0f // Commencer transparent
            }
            imageLayers.add(imageView)
            addView(imageView)
        }
        
        // Afficher la première image sur la première couche
        imageLayers[0].apply {
            setImageResource(eyeStages[currentStage])
            alpha = 0.8f // Semi-transparent
        }
    }

    fun updateFromBreath(strength: Float) {
        val targetStage = mapStrengthToStage(strength)
        val currentTime = System.currentTimeMillis()
        
        // Détecter si c'est un nouveau pic
        if (targetStage > peakStage) {
            peakStage = targetStage
            lastPeakTime = currentTime
        }
        
        // Choisir le stage à afficher : soit le pic qui persiste, soit la valeur actuelle
        val displayStage = if (currentTime - lastPeakTime < PEAK_HOLD_DURATION) {
            // On est encore dans la période de maintien du pic
            peakStage
        } else {
            // La période de maintien est finie, utiliser la valeur actuelle
            peakStage = targetStage // Réinitialiser le pic
            targetStage
        }
        
        if (displayStage != currentStage) {
            animateLayeredTransition(currentStage, displayStage, strength)
            currentStage = displayStage
        }
    }

    private fun mapStrengthToStage(strength: Float): Int {
        val clamped = (strength * 20).coerceIn(0f, 10f)
        return clamped.toInt()
    }

    private fun animateLayeredTransition(fromStage: Int, toStage: Int, strength: Float) {
        if (fromStage == toStage) return

        // Calculer les transparences basées sur la force du souffle
        val baseAlpha = 0.6f + (strength * 0.4f) // Entre 0.6 et 1.0
        val secondaryAlpha = strength * 0.5f // Entre 0 et 0.5
        val tertiaryAlpha = strength * 0.3f // Entre 0 et 0.3

        // Utiliser différentes couches pour créer un effet de superposition
        val availableLayer = findAvailableLayer()
        
        availableLayer?.apply {
            setImageResource(eyeStages[toStage])
            alpha = 0f
            
            // Animer l'apparition de la nouvelle couche
            val fadeIn = AlphaAnimation(0f, baseAlpha).apply {
                duration = 150
                fillAfter = true
                setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}
                    override fun onAnimationRepeat(animation: Animation?) {}
                    override fun onAnimationEnd(animation: Animation?) {
                        alpha = baseAlpha
                    }
                })
            }
            startAnimation(fadeIn)
        }

        // Faire disparaître progressivement les autres couches
        handler.postDelayed({
            fadeOutOtherLayers(availableLayer, secondaryAlpha, tertiaryAlpha)
        }, 100)
    }

    private fun findAvailableLayer(): ImageView? {
        // Trouver une couche avec peu ou pas d'alpha pour la réutiliser
        return imageLayers.minByOrNull { it.alpha }
    }

    private fun fadeOutOtherLayers(keepLayer: ImageView?, secondaryAlpha: Float, tertiaryAlpha: Float) {
        imageLayers.forEachIndexed { index, layer ->
            if (layer != keepLayer) {
                val targetAlpha = when (index) {
                    0 -> secondaryAlpha
                    1 -> tertiaryAlpha
                    else -> 0f
                }
                
                val fadeOut = AlphaAnimation(layer.alpha, targetAlpha).apply {
                    duration = 200
                    fillAfter = true
                    setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation?) {}
                        override fun onAnimationRepeat(animation: Animation?) {}
                        override fun onAnimationEnd(animation: Animation?) {
                            layer.alpha = targetAlpha
                        }
                    })
                }
                layer.startAnimation(fadeOut)
            }
        }
    }
}
