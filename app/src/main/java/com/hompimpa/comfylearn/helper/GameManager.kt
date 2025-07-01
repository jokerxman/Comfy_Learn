package com.hompimpa.comfylearn.helper

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal val Context.mathGameDataStore: DataStore<Preferences> by preferencesDataStore(name = "math_game_progress")

class GameManager(private val context: Context) {

    // A companion object to hold the preferences keys.
    companion object {
        val PROBLEMS_SOLVED_KEY = intPreferencesKey("problems_solved_count")
    }

    /**
     * A Flow that emits the number of problems solved whenever it changes.
     * You can observe this from anywhere in your app to show progress.
     */
    val problemsSolvedFlow: Flow<Int> = context.mathGameDataStore.data
        .map { preferences ->
            preferences[PROBLEMS_SOLVED_KEY] ?: 0
        }

    /**
     * Increments the count of solved problems in DataStore.
     * This is a suspend function, so it must be called from a coroutine.
     */
    suspend fun incrementProblemsSolved() {
        context.mathGameDataStore.edit { settings ->
            val currentCount = settings[PROBLEMS_SOLVED_KEY] ?: 0
            settings[PROBLEMS_SOLVED_KEY] = currentCount + 1
        }
    }

    /**
     * Resets the progress.
     */
    suspend fun resetProgress() {
        context.mathGameDataStore.edit { settings ->
            settings[PROBLEMS_SOLVED_KEY] = 0
        }
    }
}