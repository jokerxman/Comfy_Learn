package com.hompimpa.comfylearn.helper

object AppConstants {
    const val PREFS_PROGRESSION = "user_progression_prefs"

    fun getSpellingCategoryProgressKey(categoryName: String): String {
        return "progress_spelling_${categoryName.replace(" ", "_").lowercase()}"
    }

    fun getPuzzleProgressKey(categoryName: String, difficulty: String): String {
        return "progress_puzzle_${
            categoryName.replace(" ", "_").lowercase()
        }_${difficulty.lowercase()}"
    }
}