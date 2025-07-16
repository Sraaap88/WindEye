
package com.example.windeye

enum class EyeState {
    NEUTRAL,
    PLISSE,
    PLISSE_ROUGE,
    ROUGE,
    ROUGE_LARME,
    LARME,
    LARME_SANG,
    SANG,
    SANG_FISSURE,
    FISSURE
}

class EyeAnimationManager {

    private var windForce: Float = 0f
    var currentState: EyeState = EyeState.NEUTRAL
        private set

    fun updateWindForce(force: Float) {
        windForce = force
        currentState = when {
            windForce < 0.2f -> EyeState.NEUTRAL
            windForce < 0.3f -> EyeState.PLISSE
            windForce < 0.4f -> EyeState.PLISSE_ROUGE
            windForce < 0.5f -> EyeState.ROUGE
            windForce < 0.6f -> EyeState.ROUGE_LARME
            windForce < 0.7f -> EyeState.LARME
            windForce < 0.8f -> EyeState.LARME_SANG
            windForce < 0.9f -> EyeState.SANG
            windForce < 1.0f -> EyeState.SANG_FISSURE
            else -> EyeState.FISSURE
        }
    }

    fun getCurrentWindForce(): Float = windForce
}
