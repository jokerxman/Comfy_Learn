package com.hompimpa.comfylearn.helper

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.util.Log
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.ui.games.DifficultySelectionActivity
import java.util.Locale

object GameContentProvider {

    private const val TAG = "GameContentProvider"

    private val usedWordsInCategory = mutableMapOf<String, MutableSet<String>>()

    private fun getArrayResourceIdForCategory(context: Context, category: String): Int {
        val resourceName = category.lowercase(Locale.getDefault())
        return context.resources.getIdentifier(resourceName, "array", context.packageName)
    }

    private fun getWordsForCategory(context: Context, category: String): List<String> {
        val arrayResId = getArrayResourceIdForCategory(context, category)
        return if (arrayResId != 0) {
            try {
                context.resources.getStringArray(arrayResId).toList()
            } catch (e: Resources.NotFoundException) {
                Log.e(TAG, "String array resource not found for category: $category (tried $arrayResId)", e)
                emptyList()
            }
        } else {
            Log.w(TAG, "No string array resource ID found for category: $category")
            emptyList()
        }
    }

    fun getNextWord(context: Context, category: String, difficulty: String): String? {
        val categoryKey = category.lowercase(Locale.getDefault())
        val allWordsForCategory = getWordsForCategory(context, category)

        if (allWordsForCategory.isEmpty()) {
            Log.w(TAG, "No words defined in strings.xml for category: $categoryKey")
            return null
        }

        val (minLength, maxLength) = when (difficulty) {
            DifficultySelectionActivity.DIFFICULTY_EASY -> 1 to 4
            DifficultySelectionActivity.DIFFICULTY_MEDIUM -> 4 to 7
            DifficultySelectionActivity.DIFFICULTY_HARD -> 7 to Int.MAX_VALUE
            else -> 1 to Int.MAX_VALUE
        }

        val availableWords = allWordsForCategory
            .filter { it.length in minLength..maxLength }
            .filterNot { usedWordsInCategory[categoryKey]?.contains(it.uppercase()) == true }

        Log.d(TAG, "Category: $categoryKey, Difficulty: $difficulty. Total words: ${allWordsForCategory.size}, Filtered (length & unused): ${availableWords.size}")

        return if (availableWords.isNotEmpty()) {
            val word = availableWords.random().uppercase() // Pick a random word and ensure it's uppercase
            usedWordsInCategory.getOrPut(categoryKey) { mutableSetOf() }.add(word)
            Log.d(TAG, "Selected word: $word for $categoryKey ($difficulty)")
            word
        } else {
            Log.w(TAG, "No suitable unused word found for $categoryKey ($difficulty) with length $minLength-$maxLength. Used: ${usedWordsInCategory[categoryKey]?.joinToString()}")
            null
        }
    }

    fun allWordsUsed(context: Context, category: String, difficulty: String): Boolean {
        val categoryKey = category.lowercase(Locale.getDefault())
        val allWordsForCategory = getWordsForCategory(context, category)

        if (allWordsForCategory.isEmpty()) return true // If no words defined, consider all "used"

        val (minLength, maxLength) = when (difficulty) {
            DifficultySelectionActivity.DIFFICULTY_EASY -> 1 to 4
            DifficultySelectionActivity.DIFFICULTY_MEDIUM -> 4 to 7
            DifficultySelectionActivity.DIFFICULTY_HARD -> 7 to Int.MAX_VALUE
            else -> 1 to Int.MAX_VALUE
        }

        val totalRelevantWords = allWordsForCategory.count { it.length in minLength..maxLength }
        val usedCount = usedWordsInCategory[categoryKey]?.count { word ->
            val wordLength = word.length
            wordLength in minLength..maxLength
        } ?: 0

        Log.d(TAG, "AllWordsUsed check for $categoryKey ($difficulty): Used $usedCount / TotalRelevant $totalRelevantWords")
        return totalRelevantWords > 0 && usedCount >= totalRelevantWords
    }

  fun resetUsedWordsForCategory(category: String) {
        val categoryKey = category.lowercase(Locale.getDefault())
        usedWordsInCategory[categoryKey]?.clear()
        Log.d(TAG, "Used words reset for category: $categoryKey")
    }

  fun getAlphabet(context: Context): List<Char> {
        val arrayResId = context.resources.getIdentifier("alphabets", "array", context.packageName)
        return if (arrayResId != 0) {
            try {
                context.resources.getStringArray(arrayResId).mapNotNull { it.firstOrNull() }
            } catch (e: Resources.NotFoundException) {
                Log.e(TAG, "Alphabet array resource not found", e)
                ('A'..'Z').toList() // Fallback to a generated alphabet
            }
        } else {
            Log.w(TAG, "No alphabet array resource ID found, falling back to generated A-Z.")
            ('A'..'Z').toList() // Fallback
        }
    }

    fun getAllSpellingCategories(context: Context): List<String> {
        return listOf("Animal", "Fruit", "Color", "Object") // Example
    }

    fun getAllPuzzleCategories(context: Context): List<String> {
        return listOf("Animal", "Fruit", "Color", "Object") // Example
    }

    fun getAllPuzzleDifficulties(): List<String> {
        return listOf("Easy", "Medium", "Hard") // As defined in DifficultySelectionActivity
    }

    // Example in GameContentProvider.kt
    // companion object {
    fun getTotalWordsForPuzzleCategory(context: Context, category: String, difficulty: String): Int {
        // This is a simplified example. You should fetch this from your actual data source
        // similar to how FillInActivity loads its questions.
        val categoryResourceId = when (category.lowercase(Locale.getDefault())) {
            "animal" -> R.array.animal // Make sure R is imported correctly
            "objek" -> R.array.objek
            else -> 0 // Or throw an exception for unknown category
        }
        if (categoryResourceId == 0) return 0

        return try {
            // Get application context to avoid issues with Activity context if used directly
            val appContext = context.applicationContext
            val currentConfig = appContext.resources.configuration
            val englishConfig = Configuration(currentConfig)
            englishConfig.setLocale(Locale.ENGLISH)
            val englishContext = appContext.createConfigurationContext(englishConfig)
            englishContext.resources.getStringArray(categoryResourceId).size
        } catch (e: Exception) {
            Log.e("GameContentProvider", "Error getting total words for $category: ${e.message}")
            0
        }
    }
}