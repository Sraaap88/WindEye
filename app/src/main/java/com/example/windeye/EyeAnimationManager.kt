
package com.example.windeye

enum class EyeState {
    NEUTRAL, PLISSE, ROUGE, LARME, SANG, FISSURE
}

class EyeAnimationManager {

    private var windForce: Float = 0f
    var currentState: EyeState = EyeState.NEUTRAL
        private set

    fun updateWindForce(force: Float) {
        windForce = force
        currentState = when {
            windForce < 0.2f -> EyeState.NEUTRAL
            windForce < 0.4f -> EyeState.PLISSE
            windForce < 0.6f -> EyeState.ROUGE
            windForce < 0.75f -> EyeState.LARME
            windForce < 0.9f -> EyeState.SANG
            else -> EyeState.FISSURE
        }
    }

    fun getCurrentWindForce(): Float = windForce
}
