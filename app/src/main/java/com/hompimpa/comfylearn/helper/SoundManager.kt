package com.hompimpa.comfylearn.helper

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.hompimpa.comfylearn.R
import java.util.Locale

object SoundManager {

    enum class Sound {
        CORRECT_ANSWER,
        INCORRECT_ANSWER,
        BUTTON_CLICK
    }

    private lateinit var soundPool: SoundPool
    private val soundMap = mutableMapOf<String, Int>()
    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        val appContext = context.applicationContext
        soundMap[Sound.CORRECT_ANSWER.name] = soundPool.load(appContext, R.raw.correct_answer, 1)
        soundMap[Sound.BUTTON_CLICK.name] = soundPool.load(appContext, R.raw.button_click, 1)
        soundMap[Sound.INCORRECT_ANSWER.name] = soundPool.load(appContext, R.raw.incorrect_answer, 1)

        isInitialized = true
    }

    fun playSound(sound: Sound) {
        if (!isInitialized) return
        soundMap[sound.name]?.let { soundId ->
            soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
        }
    }

    fun playSoundByName(context: Context, itemName: String) {
        if (!isInitialized) return

        val soundKey = itemName.lowercase(Locale.ROOT)

        if (soundMap.containsKey(soundKey)) {
            val soundId = soundMap[soundKey]!!
            if (soundId > 0) {
                soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
            }
            return
        }

        val resourceName = "item_" + soundKey.replace(" ", "_")
        val resourceId = context.resources.getIdentifier(resourceName, "raw", context.packageName)

        if (resourceId != 0) {
            soundMap[soundKey] = 0
            soundPool.setOnLoadCompleteListener { _, sampleId, status ->
                if (status == 0) {
                    if (soundMap[soundKey] == 0) {
                        soundMap[soundKey] = sampleId
                    }
                } else {
                    soundMap.remove(soundKey)
                }
            }
        }
    }
}
