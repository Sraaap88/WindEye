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

    private val stageImage = ImageView(context).apply {
        layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        scaleType = ImageView.ScaleType.FIT_CENTER
        alpha = 1f
    }

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

    private var currentStage = 0
    private val handler = Handler(Looper.getMainLooper())

    init {
        addView(stageImage)
        stageImage.setImageResource(eyeStages[currentStage])
    }

    fun updateFromBreath(strength: Float) {
        val targetStage = mapStrengthToStage(strength)
        if (targetStage != currentStage) {
            animateTransition(currentStage, targetStage)
            currentStage = targetStage
        }
    }

    private fun mapStrengthToStage(strength: Float): Int {
        val clamped = (strength * 20).coerceIn(0f, 10f) // 50% plus sensible
        return clamped.toInt()
    }

    private fun animateTransition(fromStage: Int, toStage: Int) {
        if (fromStage == toStage) return

        val fadeOut = AlphaAnimation(1f, 0f).apply {
            duration = 100
            fillAfter = true
        }

        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 100
            fillAfter = true
        }

        stageImage.startAnimation(fadeOut)
        handler.postDelayed({
            stageImage.setImageResource(eyeStages[toStage])
            stageImage.startAnimation(fadeIn)
        }, 100)
    }
}
