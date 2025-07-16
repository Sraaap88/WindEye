
package com.example.windeye

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
    private val layerPlisseRouge = ImageView(context)
    private val layerRougeLarme = ImageView(context)
    private val layerLarmeSang = ImageView(context)
    private val layerSangFissure = ImageView(context)

    init {
        // Appliquer les images (à placer dans drawable)
        eyeBase.setImageResource(R.drawable.eye_base)
        layerPlisse.setImageResource(R.drawable.layer_plisse)
        layerRouge.setImageResource(R.drawable.layer_rouge)
        layerLarme.setImageResource(R.drawable.layer_larme)
        layerSang.setImageResource(R.drawable.layer_sang)
        layerFissure.setImageResource(R.drawable.layer_fissure)
        layerPlisseRouge.setImageResource(R.drawable.layer_plisse_rouge)
        layerRougeLarme.setImageResource(R.drawable.layer_rouge_larme)
        layerLarmeSang.setImageResource(R.drawable.layer_larme_sang)
        layerSangFissure.setImageResource(R.drawable.layer_sang_fissure)

        // Ajouter dans l’ordre d’empilement
        addView(eyeBase)
        addView(layerPlisse)
        addView(layerRouge)
        addView(layerLarme)
        addView(layerSang)
        addView(layerFissure)
        addView(layerPlisseRouge)
        addView(layerRougeLarme)
        addView(layerLarmeSang)
        addView(layerSangFissure)

        // Initialement, tout sauf l'œil de base est invisible
        resetLayers()
    }

    private fun resetLayers() {
        layerPlisse.alpha = 0f
        layerRouge.alpha = 0f
        layerLarme.alpha = 0f
        layerSang.alpha = 0f
        layerFissure.alpha = 0f
        layerPlisseRouge.alpha = 0f
        layerRougeLarme.alpha = 0f
        layerLarmeSang.alpha = 0f
        layerSangFissure.alpha = 0f
    }

   fun updateState(state: EyeState) {
            resetLayers()
        
            fun fade(view: ImageView) {
                view.animate().alpha(1f).setDuration(300).start()
            }
        
            when (state) {
                EyeState.PLISSE -> fade(layerPlisse)
                EyeState.ROUGE -> fade(layerRouge)
                EyeState.LARME -> fade(layerLarme)
                EyeState.SANG -> fade(layerSang)
                EyeState.FISSURE -> fade(layerFissure)
        
                // Intermédiaires :
                EyeState.PLISSE_ROUGE -> fade(layerPlisseRouge)
                EyeState.ROUGE_LARME -> fade(layerRougeLarme)
                EyeState.LARME_SANG -> fade(layerLarmeSang)
                EyeState.SANG_FISSURE -> fade(layerSangFissure)
        
                else -> {} // NEUTRAL
            }
        }
}
