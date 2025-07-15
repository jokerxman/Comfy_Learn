package com.hompimpa.comfylearn.helper

import android.content.Context
import android.content.res.Resources
import com.hompimpa.comfylearn.R
import java.util.Locale

object GameContentProvider {

    private val usedWordsInCategory = mutableMapOf<String, MutableSet<String>>()

    fun getWordsForCategory(context: Context, category: String): List<String> {
        val resourceName = category.lowercase(Locale.ROOT)
        val arrayResId = context.resources.getIdentifier(resourceName, "array", context.packageName)
        return if (arrayResId != 0) {
            try {
                context.resources.getStringArray(arrayResId).toList()
            } catch (_: Resources.NotFoundException) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    private fun getDifficultyBounds(difficulty: String): Pair<Int, Int> {
        return when (difficulty.lowercase(Locale.ROOT)) {
            "easy", "mudah" -> 1 to 4
            "medium", "sedang" -> 4 to 7
            "hard", "sulit" -> 7 to Int.MAX_VALUE
            else -> 1 to Int.MAX_VALUE
        }
    }

    fun getNextWord(context: Context, category: String, difficulty: String): String? {
        val categoryKey = category.lowercase(Locale.ROOT)
        val allWords = getWordsForCategory(context, category)
        if (allWords.isEmpty()) return null

        val (minLength, maxLength) = getDifficultyBounds(difficulty)
        val availableWords = allWords
            .filter { it.length in minLength..maxLength }
            .filterNot { usedWordsInCategory[categoryKey]?.contains(it.uppercase(Locale.ROOT)) == true }

        return availableWords.randomOrNull()?.let { word ->
            val upperWord = word.uppercase(Locale.ROOT)
            usedWordsInCategory.getOrPut(categoryKey) { mutableSetOf() }.add(upperWord)
            upperWord
        }
    }

    fun allWordsUsed(context: Context, category: String, difficulty: String): Boolean {
        val allWords = getWordsForCategory(context, category)
        if (allWords.isEmpty()) return true

        val (minLength, maxLength) = getDifficultyBounds(difficulty)
        val relevantWords = allWords.filter { it.length in minLength..maxLength }
        val usedCount = usedWordsInCategory[category.lowercase(Locale.ROOT)]?.size ?: 0

        return relevantWords.isNotEmpty() && usedCount >= relevantWords.size
    }

    fun resetUsedWordsForCategory(category: String) {
        usedWordsInCategory[category.lowercase(Locale.ROOT)]?.clear()
    }

    fun getAlphabet(context: Context): List<Char> {
        val resId = context.resources.getIdentifier("alphabets", "array", context.packageName)
        return if (resId != 0) {
            context.resources.getStringArray(resId).mapNotNull { it.firstOrNull() }
        } else {
            ('A'..'Z').toList()
        }
    }

    fun getGameCategories(context: Context): List<String> {
        return context.resources.getStringArray(R.array.game_category_keys).toList()
    }

    fun getPuzzleDifficulties(context: Context): List<String> {
        return context.resources.getStringArray(R.array.puzzle_difficulties).toList()
    }

    fun getTotalWordsForPuzzleCategory(context: Context, category: String, difficulty: String): Int {
        val allWords = getWordsForCategory(context, category)
        val (minLength, maxLength) = getDifficultyBounds(difficulty)
        return allWords.count { it.length in minLength..maxLength }
    }

    fun getImagePath(category: String, itemName: String): String {
        val normalizedCategory = category.lowercase(Locale.ROOT)
        val normalizedItemName = itemName.lowercase(Locale.ROOT).replace(" ", "_")
        return "en/${normalizedCategory}_${normalizedItemName}.svg"
    }
}