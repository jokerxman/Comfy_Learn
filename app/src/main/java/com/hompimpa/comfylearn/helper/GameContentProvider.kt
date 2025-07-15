package com.hompimpa.comfylearn.helper

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.PictureDrawable
import androidx.core.content.edit
import com.caverock.androidsvg.SVG
import com.hompimpa.comfylearn.R
import java.io.IOException
import java.util.Locale

object GameContentProvider {

    private const val PREFS_USED_WORDS = "GameContentProvider_UsedWords"

    fun getCategoryResourceId(category: String): Int {
        return when (category.lowercase(Locale.ROOT)) {
            "animal" -> R.array.animal
            "objek" -> R.array.objek
            else -> 0
        }
    }

    fun getUsedWords(context: Context, category: String): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_USED_WORDS, Context.MODE_PRIVATE)
        val categoryKey = category.lowercase(Locale.ROOT)
        return prefs.getStringSet(categoryKey, emptySet()) ?: emptySet()
    }

    fun addUsedWord(context: Context, category: String, word: String) {
        val prefs = context.getSharedPreferences(PREFS_USED_WORDS, Context.MODE_PRIVATE)
        val categoryKey = category.lowercase(Locale.ROOT)
        val usedWords = getUsedWords(context, category).toMutableSet()
        usedWords.add(word.uppercase(Locale.ROOT))
        prefs.edit { putStringSet(categoryKey, usedWords) }
    }

    fun getWordsForCategory(context: Context, category: String): List<String> {
        val arrayResId = getCategoryResourceId(category)
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

    fun allWordsUsed(context: Context, category: String, difficulty: String): Boolean {
        val allWords = getWordsForCategory(context, category)
        if (allWords.isEmpty()) return true

        val (minLength, maxLength) = getDifficultyBounds(difficulty)
        val relevantWords = allWords.filter { it.length in minLength..maxLength }
        val usedWords = getUsedWords(context, category)
        val usedCount = relevantWords.count { usedWords.contains(it.uppercase(Locale.ROOT)) }

        return relevantWords.isNotEmpty() && usedCount >= relevantWords.size
    }

    fun resetUsedWordsForCategory(context: Context, category: String) {
        val prefs = context.getSharedPreferences(PREFS_USED_WORDS, Context.MODE_PRIVATE)
        val categoryKey = category.lowercase(Locale.ROOT)
        prefs.edit { remove(categoryKey) }
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

    fun loadSvgFromAssets(context: Context, path: String): PictureDrawable? {
        return try {
            context.assets.open(path).use { stream ->
                SVG.getFromInputStream(stream)?.renderToPicture()?.let { PictureDrawable(it) }
            }
        } catch (_: IOException) {
            null
        }
    }
}
