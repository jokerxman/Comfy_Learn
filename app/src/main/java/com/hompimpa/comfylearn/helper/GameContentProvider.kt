package com.hompimpa.comfylearn.helper

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.PictureDrawable
import com.caverock.androidsvg.SVG
import com.hompimpa.comfylearn.R
import java.io.IOException
import java.util.Locale

object GameContentProvider {

    // Use SharedPreferences for persistent storage of used words.
    private const val PREFS_USED_WORDS = "GameContentProvider_UsedWords"

    /**
     * *** NEW FUNCTION ***
     * Gets the resource ID for a given category string (e.g., "animal" -> R.array.animal).
     * This is used to dynamically load the correct word list.
     */
    fun getCategoryResourceId(category: String): Int {
        return when (category.lowercase(Locale.ROOT)) {
            "animal" -> R.array.animal
            "objek" -> R.array.objek
            // Add other categories here as needed
            else -> 0
        }
    }

    /**
     * *** NEW FUNCTION ***
     * Gets the set of used words for a specific category from persistent storage.
     */
    fun getUsedWords(context: Context, category: String): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_USED_WORDS, Context.MODE_PRIVATE)
        val categoryKey = category.lowercase(Locale.ROOT)
        // Return the stored set, or an empty set if none exists.
        return prefs.getStringSet(categoryKey, emptySet()) ?: emptySet()
    }

    /**
     * *** NEW FUNCTION ***
     * Adds a successfully solved word to the used list in persistent storage.
     */
    fun addUsedWord(context: Context, category: String, word: String) {
        val prefs = context.getSharedPreferences(PREFS_USED_WORDS, Context.MODE_PRIVATE)
        val categoryKey = category.lowercase(Locale.ROOT)
        // Get the current set, add the new word, and save it back.
        val usedWords = getUsedWords(context, category).toMutableSet()
        usedWords.add(word.uppercase(Locale.ROOT)) // Store in uppercase for consistent checking
        prefs.edit().putStringSet(categoryKey, usedWords).apply()
    }

    fun getWordsForCategory(context: Context, category: String): List<String> {
        // Now uses the new helper function to get the ID.
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

    // This function is now effectively replaced by the logic in PuzzleActivity,
    // but we can keep it for other potential uses. It now correctly uses the persistent storage.
    fun getNextWord(context: Context, category: String, difficulty: String): String? {
        val allWords = getWordsForCategory(context, category)
        if (allWords.isEmpty()) return null

        val (minLength, maxLength) = getDifficultyBounds(difficulty)
        val usedWords = getUsedWords(context, category)
        val availableWords = allWords
            .filter { it.length in minLength..maxLength }
            .filterNot { usedWords.contains(it.uppercase(Locale.ROOT)) }

        return availableWords.randomOrNull()?.let { word ->
            addUsedWord(context, category, word)
            word.uppercase(Locale.ROOT)
        }
    }

    fun allWordsUsed(context: Context, category: String, difficulty: String): Boolean {
        val allWords = getWordsForCategory(context, category)
        if (allWords.isEmpty()) return true

        val (minLength, maxLength) = getDifficultyBounds(difficulty)
        val relevantWords = allWords.filter { it.length in minLength..maxLength }
        // Check against persistent storage
        val usedWords = getUsedWords(context, category)
        val usedCount = relevantWords.count { usedWords.contains(it.uppercase(Locale.ROOT)) }

        return relevantWords.isNotEmpty() && usedCount >= relevantWords.size
    }

    /**
     * *** MODIFIED FUNCTION ***
     * Now takes a context and clears the used words from SharedPreferences for the given category.
     */
    fun resetUsedWordsForCategory(context: Context, category: String) {
        val prefs = context.getSharedPreferences(PREFS_USED_WORDS, Context.MODE_PRIVATE)
        val categoryKey = category.lowercase(Locale.ROOT)
        prefs.edit().remove(categoryKey).apply()
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
        } catch (e: IOException) {
            // Fails silently if the image is not found
            null
        }
    }
}
