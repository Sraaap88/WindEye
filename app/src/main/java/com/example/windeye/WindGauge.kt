package com.example.windeye

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import kotlin.concurrent.thread
import kotlin.math.abs

class WindGauge(private val context: Context) {

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var listener: ((Float) -> Unit)? = null

    private val handler = Handler(Looper.getMainLooper())

    fun setOnWindChangeListener(l: (Float) -> Unit) {
        listener = l
    }

    fun startListening() {
        val bufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        // Vérification d'état avant de démarrer
        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            audioRecord?.release()
            return
        }

        audioRecord?.startRecording()
        isRecording = true

        thread {
            val buffer = ShortArray(bufferSize)
            while (isRecording) {
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (read > 0) {
                    val amplitude = buffer.take(read).map { abs(it.toInt()) }.average().toFloat()
                    val normalized = (amplitude / MAX_AMPLITUDE).coerceIn(0f, 1f)
                    handler.post {
                        listener?.invoke(normalized)
                    }
                }
                Thread.sleep(50)
            }
        }
    }

    fun stopListening() {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }

    companion object {
        private const val SAMPLE_RATE = 44100
        private const val MAX_AMPLITUDE = 32767f
    }
}
