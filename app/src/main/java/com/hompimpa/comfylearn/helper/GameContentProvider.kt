package com.hompimpa.comfylearn.helper

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.ui.games.DifficultySelectionActivity
import java.util.Locale

object GameContentProvider {

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
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    fun getNextWord(context: Context, category: String, difficulty: String): String? {
        val categoryKey = category.lowercase(Locale.getDefault())
        val allWordsForCategory = getWordsForCategory(context, category)

        if (allWordsForCategory.isEmpty()) {
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

        return if (availableWords.isNotEmpty()) {
            val word = availableWords.random().uppercase()
            usedWordsInCategory.getOrPut(categoryKey) { mutableSetOf() }.add(word)
            word
        } else {
            null
        }
    }

    fun allWordsUsed(context: Context, category: String, difficulty: String): Boolean {
        val categoryKey = category.lowercase(Locale.getDefault())
        val allWordsForCategory = getWordsForCategory(context, category)

        if (allWordsForCategory.isEmpty()) return true

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

        return totalRelevantWords > 0 && usedCount >= totalRelevantWords
    }

    fun resetUsedWordsForCategory(category: String) {
        val categoryKey = category.lowercase(Locale.getDefault())
        usedWordsInCategory[categoryKey]?.clear()
    }

    fun getAlphabet(context: Context): List<Char> {
        val arrayResId = context.resources.getIdentifier("alphabets", "array", context.packageName)
        return if (arrayResId != 0) {
            try {
                context.resources.getStringArray(arrayResId).mapNotNull { it.firstOrNull() }
            } catch (e: Resources.NotFoundException) {
                ('A'..'Z').toList()
            }
        } else {
            ('A'..'Z').toList()
        }
    }

    fun getAllSpellingCategories(context: Context): List<String> {
        return listOf("Animal", "Fruit", "Color", "Object")
    }

    fun getAllPuzzleCategories(context: Context): List<String> {
        return listOf("Animal", "Fruit", "Color", "Object")
    }

    fun getAllPuzzleDifficulties(): List<String> {
        return listOf("Easy", "Medium", "Hard")
    }

    fun getTotalWordsForPuzzleCategory(context: Context, category: String, difficulty: String): Int {
        val categoryResourceId = when (category.lowercase(Locale.getDefault())) {
            "animal" -> R.array.animal
            "objek" -> R.array.objek
            else -> 0
        }
        if (categoryResourceId == 0) return 0

        return try {
            val appContext = context.applicationContext
            val currentConfig = appContext.resources.configuration
            val englishConfig = Configuration(currentConfig)
            englishConfig.setLocale(Locale.ENGLISH)
            val englishContext = appContext.createConfigurationContext(englishConfig)
            englishContext.resources.getStringArray(categoryResourceId).size
        } catch (e: Exception) {
            0
        }
    }
}