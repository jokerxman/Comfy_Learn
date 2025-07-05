package com.hompimpa.comfylearn.ui.games

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.hompimpa.comfylearn.R
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonOpenGameDrawing.setOnClickListener {
            val intent = Intent(requireContext(), DrawingActivity::class.java)
            startActivity(intent)
        }

        binding.buttonOpenGameFill.setOnClickListener {
            val categoryForFillInGame = "animal"
            it.tag = categoryForFillInGame
            val intent = DifficultySelectionActivity.newIntent(
                requireContext(),
                categoryForFillInGame,
                DifficultySelectionActivity.GAME_TYPE_FILL_IN,
                lastSelectedDifficultyFillIn
            )
            fillInDifficultyLauncher.launch(intent)
        }

        binding.buttonOpenGamePuzzle.setOnClickListener {
            val categoryForPuzzleGame = "animal"
            it.tag = categoryForPuzzleGame
            val intent = DifficultySelectionActivity.newIntent(
                requireContext(),
                categoryForPuzzleGame,
                DifficultySelectionActivity.GAME_TYPE_PUZZLE,
                lastSelectedDifficultyPuzzle
            )
            puzzleDifficultyLauncher.launch(intent)
        }

        binding.buttonOpenGameArithmetic.setOnClickListener {
            val categoryForMathGame = "arithmetic"
            val intent = DifficultySelectionActivity.newIntent(
                requireContext(),
                categoryForMathGame,
                DifficultySelectionActivity.GAME_TYPE_MATH,
                lastSelectedDifficultyMath
            )
            mathGameDifficultyLauncher.launch(intent)
        }

        // Add the scroll indicator logic
        setupScrollIndicator()
    }

    private fun setupScrollIndicator() {
        val scrollView = binding.scrollView
        val scrollIndicator = binding.scrollIndicator
        val contentLayout = scrollView.getChildAt(0)

        contentLayout.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                contentLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)

                if (contentLayout.height > scrollView.height) {
                    scrollIndicator.visibility = View.VISIBLE
                    val bounceAnimation = AnimationUtils.loadAnimation(context, R.anim.bounce)
                    scrollIndicator.startAnimation(bounceAnimation)
                } else {
                    scrollIndicator.visibility = View.GONE
                }
            }
        })

        scrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            if (scrollY > 0 && scrollIndicator.visibility == View.VISIBLE) {
                scrollIndicator.animate().alpha(0f).setDuration(300).withEndAction {
                    scrollIndicator.visibility = View.GONE
                }.start()
            }
        }
    }

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
