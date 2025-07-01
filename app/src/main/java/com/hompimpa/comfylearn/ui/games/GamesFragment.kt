package com.hompimpa.comfylearn.ui.games

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.hompimpa.comfylearn.databinding.FragmentGamesBinding
import com.hompimpa.comfylearn.helper.AppConstants
import com.hompimpa.comfylearn.helper.GameContentProvider
import com.hompimpa.comfylearn.ui.games.drawing.DrawingActivity
import com.hompimpa.comfylearn.ui.games.fillIn.FillInActivity
import com.hompimpa.comfylearn.ui.games.mathgame.MathGameActivity
import com.hompimpa.comfylearn.ui.games.puzzle.PuzzleActivity

class GamesFragment : Fragment() {

    private var _binding: FragmentGamesBinding? = null
    private val binding get() = _binding!!

    private lateinit var fillInDifficultyLauncher: ActivityResultLauncher<Intent>
    private lateinit var puzzleDifficultyLauncher: ActivityResultLauncher<Intent>
    private lateinit var mathGameDifficultyLauncher: ActivityResultLauncher<Intent>

    private var lastSelectedDifficultyFillIn: String = DifficultySelectionActivity.DIFFICULTY_MEDIUM
    private var lastSelectedDifficultyPuzzle: String = DifficultySelectionActivity.DIFFICULTY_MEDIUM
    private var lastSelectedDifficultyMath: String = DifficultySelectionActivity.DIFFICULTY_MEDIUM

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // This onActivityResult might not be strictly needed for FillIn if handled by launcher,
        // but keep if other activities launched with older startActivityForResult use it.
    }

    private fun updatePuzzleProgressInPrefsFromGamesFragment(
        category: String,
        difficulty: String,
        newWordsSolvedCount: Int
    ) {
        val prefs = requireActivity().getSharedPreferences(
            AppConstants.PREFS_PROGRESSION,
            Context.MODE_PRIVATE
        )
        val editor = prefs.edit()
        val progressKeyBase = AppConstants.getPuzzleProgressKey(category, difficulty)
        val wordsSolvedKey = progressKeyBase + "_words_solved"
        val completedKey = progressKeyBase + "_completed"
        val previouslySolved = prefs.getInt(wordsSolvedKey, 0)
        val currentTotalSolved = previouslySolved + newWordsSolvedCount
        editor.putInt(wordsSolvedKey, currentTotalSolved)
        val totalWordsInLevel = GameContentProvider.getTotalWordsForPuzzleCategory(
            requireContext().applicationContext,
            category,
            difficulty
        )
        if (totalWordsInLevel > 0 && currentTotalSolved >= totalWordsInLevel) {
            editor.putBoolean(completedKey, true)
        } else {
            editor.putBoolean(completedKey, false)
        }
        editor.apply()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fillInDifficultyLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                if (data != null) {
                    val categoryPlayed = data.getStringExtra(FillInActivity.EXTRA_CATEGORY_PLAYED)
                    val questionsCompleted = data.getIntExtra(FillInActivity.EXTRA_QUESTIONS_COMPLETED, 0)
                    val difficultyPlayed = data.getStringExtra("DIFFICULTY_PLAYED_BACK")

                    if (categoryPlayed != null && difficultyPlayed != null) {
                        lastSelectedDifficultyFillIn = difficultyPlayed
                        if (questionsCompleted > 0) {
                            updatePuzzleProgressInPrefsFromGamesFragment(
                                categoryPlayed,
                                difficultyPlayed,
                                questionsCompleted
                            )
                        }
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Game cancelled or error.", Toast.LENGTH_SHORT).show()
            }
        }

        puzzleDifficultyLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedDifficulty = result.data?.getStringExtra(DifficultySelectionActivity.EXTRA_SELECTED_DIFFICULTY)
                if (selectedDifficulty != null) {
                    lastSelectedDifficultyPuzzle = selectedDifficulty
                    val category = binding.buttonOpenGamePuzzle.tag as? String ?: "default_puzzle_category"
                    launchPuzzleGame(category, selectedDifficulty)
                } else {
                    Toast.makeText(requireContext(), "Difficulty not selected.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Difficulty selection cancelled.", Toast.LENGTH_SHORT).show()
            }
        }

        mathGameDifficultyLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedDifficulty = result.data?.getStringExtra(DifficultySelectionActivity.EXTRA_SELECTED_DIFFICULTY)
                if (selectedDifficulty != null) {
                    lastSelectedDifficultyMath = selectedDifficulty
                    launchMathGame(selectedDifficulty)
                } else {
                    Toast.makeText(requireContext(), "Difficulty not selected for Math Game.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Math Game difficulty selection cancelled.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGamesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.buttonOpenGameDrawing.setOnClickListener {
            val intent = Intent(requireContext(), DrawingActivity::class.java)
            startActivity(intent)
        }

// For FillIn Game
        binding.buttonOpenGameFill.setOnClickListener {
            val categoryForFillInGame = "animal"
            it.tag = categoryForFillInGame
            val intent = DifficultySelectionActivity.newIntent(
                requireContext(),
                categoryForFillInGame,
                DifficultySelectionActivity.GAME_TYPE_FILL_IN, // Specify FillIn game
                lastSelectedDifficultyFillIn
            )
            fillInDifficultyLauncher.launch(intent)
        }

// For Puzzle Game
        binding.buttonOpenGamePuzzle.setOnClickListener {
            val categoryForPuzzleGame = "animal" // Or get from tag
            it.tag = categoryForPuzzleGame
            val intent = DifficultySelectionActivity.newIntent(
                requireContext(),
                categoryForPuzzleGame,
                DifficultySelectionActivity.GAME_TYPE_PUZZLE, // Specify Puzzle game
                lastSelectedDifficultyPuzzle
            )
            puzzleDifficultyLauncher.launch(intent) // Use the correct launcher
        }

// For Math Game
        binding.buttonOpenGameArithmetic.setOnClickListener {
            val categoryForMathGame = "arithmetic" // Or appropriate category for Math
            val intent = DifficultySelectionActivity.newIntent(
                requireContext(),
                categoryForMathGame, // Use a relevant category or a placeholder
                DifficultySelectionActivity.GAME_TYPE_MATH, // Specify Math game
                lastSelectedDifficultyMath
            )
            mathGameDifficultyLauncher.launch(intent) // Use the correct launcher
        }
        return root
    }

    // Removed launchFillInGame as it's handled by DifficultySelectionActivity now

    private fun launchPuzzleGame(category: String, difficulty: String) {
        val intent = Intent(requireContext(), PuzzleActivity::class.java).apply {
            putExtra("CATEGORY", category)
            putExtra("DIFFICULTY", difficulty)
        }
        startActivity(intent)
    }

    private fun launchMathGame(difficulty: String) {
        val intent = Intent(requireContext(), MathGameActivity::class.java).apply {
            putExtra(MathGameActivity.EXTRA_SELECTED_DIFFICULTY, difficulty)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}