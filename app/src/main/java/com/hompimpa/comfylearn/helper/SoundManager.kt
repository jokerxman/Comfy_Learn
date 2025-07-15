package com.hompimpa.comfylearn.helper

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import com.hompimpa.comfylearn.R
import java.util.Locale

object SoundManager {

    enum class Sound {
        CORRECT_ANSWER,
        INCORRECT_ANSWER,
        BUTTON_CLICK
    }

    private lateinit var soundPool: SoundPool
    // This map now caches both enum sounds and dynamic sounds
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

        // Pre-load fixed sounds
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

    // NEW FUNCTION: Plays sounds dynamically based on a word/item name
    fun playSoundByName(context: Context, itemName: String) {
        if (!isInitialized) return

        val soundKey = itemName.lowercase(Locale.ROOT)

        // 1. Check if the sound is already loaded in our cache
        if (soundMap.containsKey(soundKey)) {
            val soundId = soundMap[soundKey]!!
            if (soundId > 0) {
                Log.d("SoundManager", "Playing cached sound for '$soundKey'")
                soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
            } else {
                Log.w("SoundManager", "Sound for '$soundKey' is still loading.")
            }
            return
        }

        // 2. If not cached, find the resource ID dynamically
        val resourceName = "item_" + soundKey.replace(" ", "_")
        val resourceId = context.resources.getIdentifier(resourceName, "raw", context.packageName)

        if (resourceId != 0) {
            Log.d("SoundManager", "Loading dynamic sound for '$soundKey' (Resource: $resourceName)")
            // Mark as loading (value 0) and then load it
            soundMap[soundKey] = 0
            val loadedSoundId = soundPool.load(context, resourceId, 1)
            // The onLoadComplete listener will update the map, but we can play immediately if it loads fast
            soundPool.setOnLoadCompleteListener { _, sampleId, status ->
                if (status == 0) {
                    // Update the cache with the real sound ID
                    if (soundMap[soundKey] == 0) { // check if it was the one we just loaded
                        soundMap[soundKey] = sampleId
                    }
                } else {
                    soundMap.remove(soundKey) // Remove on failure
                }
            }

        } else {
            Log.e("SoundManager", "Dynamic sound resource not found: R.raw.$resourceName")
        }
    }

    fun release() {
        if (isInitialized) {
            soundPool.release()
            soundMap.clear()
            isInitialized = false
        }
    }
}