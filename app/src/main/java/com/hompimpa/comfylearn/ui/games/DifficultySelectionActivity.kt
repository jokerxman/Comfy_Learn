package com.hompimpa.comfylearn.ui.games

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.Nullable
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.databinding.ActivityDifficultySelectionBinding
import com.hompimpa.comfylearn.helper.BaseActivity
import com.hompimpa.comfylearn.ui.games.fillIn.FillInActivity
import com.hompimpa.comfylearn.ui.games.puzzle.PuzzleActivity

private const val FILL_IN_GAME_REQUEST_CODE_INTERNAL = 202

class DifficultySelectionActivity : BaseActivity() {

    private lateinit var binding: ActivityDifficultySelectionBinding
    private val PREFS_NAME = "GameSettingsPrefs"
    private val KEY_LAST_UNIVERSAL_DIFFICULTY = "last_selected_universal_difficulty"
    private lateinit var sharedPreferences: SharedPreferences
    private var gameCategory: String? = null
    private var gameType: String? = null //

    companion object {
        const val EXTRA_SELECTED_DIFFICULTY = "com.hompimpa.comfylearn.SELECTED_DIFFICULTY"
        const val EXTRA_CURRENT_DIFFICULTY = "com.hompimpa.comfylearn.CURRENT_DIFFICULTY"
        const val EXTRA_GAME_CATEGORY = "com.hompimpa.comfylearn.GAME_CATEGORY"
        const val EXTRA_GAME_TYPE = "com.hompimpa.comfylearn.GAME_TYPE" // New
        const val GAME_TYPE_FILL_IN = "FILL_IN"
        const val GAME_TYPE_PUZZLE = "PUZZLE"
        const val GAME_TYPE_MATH = "MATH"
        const val DIFFICULTY_EASY = "EASY"
        const val DIFFICULTY_MEDIUM = "MEDIUM"
        const val DIFFICULTY_HARD = "HARD"

        fun newIntent(context: Context, category: String, gameType: String, currentDifficulty: String? = null): Intent { // Added gameType
            val intent = Intent(context, DifficultySelectionActivity::class.java)
            intent.putExtra(EXTRA_GAME_CATEGORY, category)
            intent.putExtra(EXTRA_GAME_TYPE, gameType) // Pass the game type
            currentDifficulty?.let {
                intent.putExtra(EXTRA_CURRENT_DIFFICULTY, it)
            }
            return intent
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILL_IN_GAME_REQUEST_CODE_INTERNAL) { // This is specific to FillIn
            if (resultCode == Activity.RESULT_OK && data != null) {
                setResult(Activity.RESULT_OK, data) // Relay FillIn's result
            } else {
                setResult(Activity.RESULT_CANCELED, data)
            }
            finish()
        }
    }

    // Inside DifficultySelectionActivity.kt

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDifficultySelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = getString(R.string.select_difficulty)
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        // Retrieve game category and game type from the intent
        gameCategory = intent.getStringExtra(EXTRA_GAME_CATEGORY)
        gameType = intent.getStringExtra(EXTRA_GAME_TYPE) // Retrieve the game type

        // Validate that both gameCategory and gameType were passed
        if (gameCategory == null || gameType == null) {
            Toast.makeText(this, "Error: Game category or type missing.", Toast.LENGTH_SHORT).show()
            finish() // Exit if essential data is missing
            return
        }

        // Load current or last selected difficulty for the radio buttons
        val currentDifficultyFromIntent = intent.getStringExtra(EXTRA_CURRENT_DIFFICULTY)
        if (currentDifficultyFromIntent != null) {
            setSelectedDifficultyRadio(currentDifficultyFromIntent)
        } else {
            loadLastSelectedDifficulty()
        }

        // Set the click listener for the confirm button
        binding.buttonConfirmDifficulty.setOnClickListener {
            val selectedDifficulty = getSelectedDifficultyAndSaveChoice()

            // Decide which game to launch or how to return the result based on gameType
            when (gameType) {
                GAME_TYPE_FILL_IN -> {
                    // For FillIn, launch FillInActivity directly and wait for its result
                    val fillInIntent = Intent(this, FillInActivity::class.java).apply {
                        putExtra("CATEGORY", gameCategory) // Pass category to FillInActivity
                        putExtra("DIFFICULTY", selectedDifficulty) // Pass selected difficulty
                    }
                    startActivityForResult(fillInIntent, FILL_IN_GAME_REQUEST_CODE_INTERNAL)
                    // The onActivityResult method will handle finishing this activity and relaying FillInActivity's result.
                }
                GAME_TYPE_PUZZLE, GAME_TYPE_MATH -> {
                    // For Puzzle and Math, return the selected difficulty (and other info)
                    // to GamesFragment, which will then launch the actual game.
                    val resultIntent = Intent().apply {
                        putExtra(EXTRA_SELECTED_DIFFICULTY, selectedDifficulty)
                        putExtra(EXTRA_GAME_CATEGORY, gameCategory) // Pass back the category
                        putExtra(EXTRA_GAME_TYPE, gameType)         // Pass back the game type
                    }
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish() // Close DifficultySelectionActivity
                }
                else -> {
                    // Handle unknown game type
                    Toast.makeText(this, "Error: Unknown game type.", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_CANCELED) // Indicate an error or cancellation
                    finish() // Close DifficultySelectionActivity
                }
            }
        }
    }

    private fun loadLastSelectedDifficulty() {
        val lastDifficulty =
            sharedPreferences.getString(KEY_LAST_UNIVERSAL_DIFFICULTY, DIFFICULTY_MEDIUM)
        setSelectedDifficultyRadio(lastDifficulty)
    }

    private fun setSelectedDifficultyRadio(difficulty: String?) {
        when (difficulty) {
            DIFFICULTY_EASY -> binding.difficultySelectorRadioGroupActivity.check(R.id.easyRadioButtonActivity)
            DIFFICULTY_MEDIUM -> binding.difficultySelectorRadioGroupActivity.check(R.id.mediumRadioButtonActivity)
            DIFFICULTY_HARD -> binding.difficultySelectorRadioGroupActivity.check(R.id.hardRadioButtonActivity)
            else -> binding.difficultySelectorRadioGroupActivity.check(R.id.mediumRadioButtonActivity)
        }
    }

    private fun getSelectedDifficultyAndSaveChoice(): String {
        val selectedRadioButtonId =
            binding.difficultySelectorRadioGroupActivity.checkedRadioButtonId
        val difficulty = when (selectedRadioButtonId) {
            R.id.easyRadioButtonActivity -> DIFFICULTY_EASY
            R.id.mediumRadioButtonActivity -> DIFFICULTY_MEDIUM
            R.id.hardRadioButtonActivity -> DIFFICULTY_HARD
            else -> DIFFICULTY_MEDIUM
        }
        with(sharedPreferences.edit()) {
            putString(KEY_LAST_UNIVERSAL_DIFFICULTY, difficulty)
            apply()
        }
        return difficulty
    }
}