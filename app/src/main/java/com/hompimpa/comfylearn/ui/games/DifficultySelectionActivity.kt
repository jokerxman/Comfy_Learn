package com.hompimpa.comfylearn.ui.games

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.edit
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.databinding.ActivityDifficultySelectionBinding
import com.hompimpa.comfylearn.helper.BaseActivity
import com.hompimpa.comfylearn.helper.SoundManager
import com.hompimpa.comfylearn.ui.games.fillIn.FillInActivity

private const val FILL_IN_GAME_REQUEST_CODE_INTERNAL = 202

class DifficultySelectionActivity : BaseActivity() {

    private lateinit var binding: ActivityDifficultySelectionBinding
    private val PREFS_NAME = "GameSettingsPrefs"
    private val KEY_LAST_UNIVERSAL_DIFFICULTY = "last_selected_universal_difficulty"
    private lateinit var sharedPreferences: SharedPreferences
    private var gameCategory: String? = null
    private var gameType: String? = null

    companion object {
        const val EXTRA_SELECTED_DIFFICULTY = "com.hompimpa.comfylearn.SELECTED_DIFFICULTY"
        const val EXTRA_CURRENT_DIFFICULTY = "com.hompimpa.comfylearn.CURRENT_DIFFICULTY"
        const val EXTRA_GAME_CATEGORY = "com.hompimpa.comfylearn.GAME_CATEGORY"
        const val EXTRA_GAME_TYPE = "com.hompimpa.comfylearn.GAME_TYPE"
        const val GAME_TYPE_FILL_IN = "FILL_IN"
        const val GAME_TYPE_PUZZLE = "PUZZLE"
        const val GAME_TYPE_MATH = "MATH"
        const val DIFFICULTY_EASY = "EASY"
        const val DIFFICULTY_MEDIUM = "MEDIUM"
        const val DIFFICULTY_HARD = "HARD"

        fun newIntent(
            context: Context,
            category: String,
            gameType: String,
            currentDifficulty: String? = null
        ): Intent {
            val intent = Intent(context, DifficultySelectionActivity::class.java)
            intent.putExtra(EXTRA_GAME_CATEGORY, category)
            intent.putExtra(EXTRA_GAME_TYPE, gameType)
            currentDifficulty?.let {
                intent.putExtra(EXTRA_CURRENT_DIFFICULTY, it)
            }
            return intent
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILL_IN_GAME_REQUEST_CODE_INTERNAL) {
            if (resultCode == RESULT_OK && data != null) {
                setResult(RESULT_OK, data)
            } else {
                setResult(RESULT_CANCELED, data)
            }
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDifficultySelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = getString(R.string.select_difficulty)
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        gameCategory = intent.getStringExtra(EXTRA_GAME_CATEGORY)
        gameType = intent.getStringExtra(EXTRA_GAME_TYPE)

        if (gameCategory == null || gameType == null) {
            Toast.makeText(this, "Error: Game category or type missing.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val currentDifficultyFromIntent = intent.getStringExtra(EXTRA_CURRENT_DIFFICULTY)
        if (currentDifficultyFromIntent != null) {
            setSelectedDifficultyRadio(currentDifficultyFromIntent)
        } else {
            loadLastSelectedDifficulty()
        }

        binding.buttonConfirmDifficulty.setOnClickListener {
            SoundManager.playSound(SoundManager.Sound.BUTTON_CLICK)
            val selectedDifficulty = getSelectedDifficultyAndSaveChoice()

            when (gameType) {
                GAME_TYPE_FILL_IN -> {
                    val fillInIntent = Intent(this, FillInActivity::class.java).apply {
                        putExtra("CATEGORY", gameCategory)
                        putExtra("DIFFICULTY", selectedDifficulty)
                    }
                    startActivityForResult(fillInIntent, FILL_IN_GAME_REQUEST_CODE_INTERNAL)
                }

                GAME_TYPE_PUZZLE, GAME_TYPE_MATH -> {
                    val resultIntent = Intent().apply {
                        putExtra(EXTRA_SELECTED_DIFFICULTY, selectedDifficulty)
                        putExtra(EXTRA_GAME_CATEGORY, gameCategory)
                        putExtra(EXTRA_GAME_TYPE, gameType)
                    }
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }

                else -> {
                    Toast.makeText(this, "Error: Unknown game type.", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_CANCELED)
                    finish()
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
        sharedPreferences.edit {
            putString(KEY_LAST_UNIVERSAL_DIFFICULTY, difficulty)
        }
        return difficulty
    }
}
