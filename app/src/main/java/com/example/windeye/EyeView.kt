
package com.example.windeye.R

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.FrameLayout
import com.example.windeye.R
import com.example.windeye.EyeState

class EyeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val eyeBase = ImageView(context)
    private val layerPlisse = ImageView(context)
    private val layerRouge = ImageView(context)
    private val layerLarme = ImageView(context)
    private val layerSang = ImageView(context)
    private val layerFissure = ImageView(context)

    init {
        // Appliquer les images (à placer dans drawable)
        eyeBase.setImageResource(R.drawable.eye_base)
        layerPlisse.setImageResource(R.drawable.layer_plisse)
        layerRouge.setImageResource(R.drawable.layer_rouge)
        layerLarme.setImageResource(R.drawable.layer_larme)
        layerSang.setImageResource(R.drawable.layer_sang)
        layerFissure.setImageResource(R.drawable.layer_fissure)

        // Ajouter dans l’ordre d’empilement
        addView(eyeBase)
        addView(layerPlisse)
        addView(layerRouge)
        addView(layerLarme)
        addView(layerSang)
        addView(layerFissure)

        // Initialement, tout sauf l'œil de base est invisible
        resetLayers()
    }

    private fun resetLayers() {
        layerPlisse.alpha = 0f
        layerRouge.alpha = 0f
        layerLarme.alpha = 0f
        layerSang.alpha = 0f
        layerFissure.alpha = 0f
    }

    fun updateState(state: EyeState) {
        resetLayers()
        when (state) {
            EyeState.PLISSE -> layerPlisse.alpha = 1f
            EyeState.ROUGE -> layerRouge.alpha = 1f
            EyeState.LARME -> layerLarme.alpha = 1f
            EyeState.SANG -> layerSang.alpha = 1f
            EyeState.FISSURE -> layerFissure.alpha = 1f
            else -> {} // NEUTRAL = aucune couche
        }
    }
}
