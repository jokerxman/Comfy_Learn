package com.hompimpa.comfylearn.ui.learnProg

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.databinding.FragmentLearnprogBinding
import com.hompimpa.comfylearn.helper.AppConstants
import com.hompimpa.comfylearn.helper.GameContentProvider
import com.hompimpa.comfylearn.ui.games.fillIn.FillInActivity

data class ProgressionItem(
    val category: String, // Keep original category for starting the game
    val difficulty: String?, // Keep original difficulty
    val activityName: String, // Display name
    val status: String,
    val isFillInGame: Boolean = false // Flag to identify game type
)

class LearnProgFragment : Fragment() {

    private var _binding: FragmentLearnprogBinding? = null
    private val binding get() = _binding!!

    private lateinit var spellingCategories: List<String>
    private lateinit var puzzleCategories: List<String> // Renamed for clarity from fillInCategories
    private lateinit var puzzleDifficulties: List<String>

    companion object {
        const val FILL_IN_GAME_REQUEST_CODE = 101
        private const val TAG = "LearnProgFragment"
        private var instanceCounter = 0 // For debugging
    }
    private val fragmentInstanceId = instanceCounter++

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Instance ID = $fragmentInstanceId")
        val appContext = requireContext().applicationContext
        spellingCategories = GameContentProvider.getAllSpellingCategories(appContext)
        puzzleCategories = GameContentProvider.getAllPuzzleCategories(appContext)
        puzzleDifficulties = GameContentProvider.getAllPuzzleDifficulties()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLearnprogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadAndDisplayProgression()
    }

    private fun loadProgressionData(): List<ProgressionItem> {
        val prefs = requireActivity().getSharedPreferences(
            AppConstants.PREFS_PROGRESSION, Context.MODE_PRIVATE
        )
        val items = mutableListOf<ProgressionItem>()

        // Example: Spelling Games (Assuming they don't use FillInActivity for this example)
        spellingCategories.forEach { category ->
            val key = AppConstants.getSpellingCategoryProgressKey(category)
            val isVisited = prefs.getBoolean(key, false) // Or completed, depending on your logic
            items.add(
                ProgressionItem(
                    category = category,
                    difficulty = null, // No difficulty for this example spelling game
                    activityName = "Spelling: $category",
                    status = if (isVisited) "Visited" else "Not Started"
                    // isFillInGame = false // if you had a different activity for spelling
                )
            )
        }

        // Fill-In (Puzzle) Games
        puzzleCategories.forEach { category ->
            puzzleDifficulties.forEach { difficulty ->
                // Key to store total words available (you'd need to get this from GameContentProvider or similar)
                // For simplicity, let's assume a fixed number or that you handle completion differently
                // val totalWordsKey = AppConstants.getPuzzleTotalWordsKey(category, difficulty)
                // val totalWordsInCategory = prefs.getInt(totalWordsKey, 5) // Example: 5 words per category/difficulty

                val progressKeyBase = AppConstants.getPuzzleProgressKey(category, difficulty)
                val completedKey = progressKeyBase + "_completed" // True if all words are solved
                val wordsSolvedKey =
                    progressKeyBase + "_words_solved" // Number of unique words solved

                val isFullyCompleted = prefs.getBoolean(completedKey, false)
                val wordsSolved = prefs.getInt(wordsSolvedKey, 0)

                val status = when {
                    isFullyCompleted -> "Completed" // This is what you're seeing
                    wordsSolved > 0 -> "$wordsSolved words solved"
                    else -> "Not Started"
                }
                items.add(
                    ProgressionItem(
                        category = category,
                        difficulty = difficulty,
                        activityName = "Fill-In: $category - $difficulty", // Changed "Puzzle" to "Fill-In" for clarity
                        status = status,
                        isFillInGame = true
                    )
                )
            }
        }
        return items.sortedBy { !it.isFillInGame } // Show Fill-In games first or some other order
    }

    private fun loadAndDisplayProgression() {
        val progressionItems = loadProgressionData()
        updateProgressionUI(progressionItems)
    }

    private fun updateProgressionUI(items: List<ProgressionItem>) {
        binding.layoutProgressionContainer.removeAllViews() // Simpler to just remove all and re-add

        if (items.isEmpty()) {
            val noDataTextView = TextView(context).apply {
                text = "No progression data yet. Start learning!"
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dpToPx(16)
                }
                gravity = android.view.Gravity.CENTER
            }
            binding.layoutProgressionContainer.addView(noDataTextView)
            return
        }

        items.forEach { item ->
            val itemLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dpToPx(8)
                    bottomMargin = dpToPx(8)
                }
                setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12)) // Increased padding
                // Make items clickable if they are FillInGame
                if (item.isFillInGame) {
                    isClickable = true
                    isFocusable = true
                    // Add a ripple effect or background selector for better UX
                    setBackgroundResource(R.drawable.clickable_item_background) // Create this drawable
                    setOnClickListener {
                        startGame(
                            item.category,
                            item.difficulty!!
                        ) // item.difficulty should not be null if isFillInGame
                    }
                }
            }

            val activityNameTextView = TextView(requireContext()).apply {
                text = item.activityName
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f
                )
                textSize = 16f
            }

            val statusTextView = TextView(requireContext()).apply {
                text = item.status
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                textSize = 16f
                // Optionally, style based on status
                // if (item.status == "Completed") setTextColor(Color.GREEN)
            }
            itemLayout.addView(activityNameTextView)
            itemLayout.addView(statusTextView)
            binding.layoutProgressionContainer.addView(itemLayout)

            val divider = View(requireContext()).apply {
                layoutParams =
                    LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1))
                setBackgroundColor(android.graphics.Color.LTGRAY)
            }
            binding.layoutProgressionContainer.addView(divider)
        }
    }

    private fun startGame(category: String, difficulty: String) {
        Log.d(TAG, "startGame: Instance ID = $fragmentInstanceId. Starting FillInActivity for $category - $difficulty")
        val intent = Intent(activity, FillInActivity::class.java).apply {
            putExtra("CATEGORY", category)
            putExtra("DIFFICULTY", difficulty)
        }
        startActivityForResult(intent, FILL_IN_GAME_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult: Instance ID = $fragmentInstanceId. requestCode=$requestCode, resultCode=$resultCode")
        if (requestCode == FILL_IN_GAME_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                data?.let {
                    val categoryPlayed = it.getStringExtra(FillInActivity.EXTRA_CATEGORY_PLAYED)
                    val questionsCompletedThisSession =
                        it.getIntExtra(FillInActivity.EXTRA_QUESTIONS_COMPLETED, 0)

                    Log.d(
                        TAG,
                        "Game result: Category '$categoryPlayed', Completed this session $questionsCompletedThisSession questions."
                    )

                    if (categoryPlayed != null && questionsCompletedThisSession > 0) {
                        // Update SharedPreferences
                        val difficulty =
                            getDifficultyFromResult(it) // You might need to pass difficulty back or infer it
                        if (difficulty != null) {
                            updatePuzzleProgressInPrefs(
                                categoryPlayed,
                                difficulty,
                                questionsCompletedThisSession
                            )
                        } else {
                            Log.e(
                                TAG,
                                "Could not determine difficulty from game result for $categoryPlayed"
                            )
                        }
                    } else if (categoryPlayed != null) {
                        Log.d(
                            TAG,
                            "No new questions completed for $categoryPlayed or category was null."
                        )
                    }
                    // Always refresh the UI
                    loadAndDisplayProgression()
                }
            } else {
                Log.d(TAG, "FillInActivity did not finish with RESULT_OK (ResultCode: $resultCode)")
                // Optionally, still refresh if some background state might have changed or user just backed out
                loadAndDisplayProgression()
            }
        }
    }

    // Helper to get difficulty - FillInActivity needs to send this back or you infer it
    // For now, let's assume FillInActivity might send it back if it's not fixed per category
    private fun getDifficultyFromResult(intent: Intent): String? {
        // Option 1: FillInActivity sends it back (modify FillInActivity.setGameResultAndFinish)
        // return intent.getStringExtra(FillInActivity.EXTRA_DIFFICULTY_PLAYED)

        // Option 2: If FillInActivity was launched for a specific difficulty that you can
        // remember or re-derive. This is harder if the request code is generic.
        // For now, returning a placeholder. You MUST implement this correctly.
        // One way is to iterate through your puzzleCategories/Difficulties if the category is unique enough
        // or ensure FillInActivity sends it back.
        // For simplicity, if your AppConstants.getPuzzleProgressKey requires difficulty,
        // it means we MUST have it.
        // A temporary, potentially flawed way if you only have one FillInActivity instance at a time:
        // find the item that was clicked to launch. This is not robust.
        // THE BEST WAY: FillInActivity should return the difficulty it was played with.
        return intent.getStringExtra("DIFFICULTY_PLAYED_BACK") // Assume FillInActivity adds this
    }


    // In LearnProgFragment.kt
    private fun updatePuzzleProgressInPrefs(
        category: String,
        difficulty: String,
        newWordsSolvedCount: Int // This is questionsSuccessfullyAnsweredThisSession from FillInActivity
    ) {
        val prefs = requireActivity().getSharedPreferences(
            AppConstants.PREFS_PROGRESSION, Context.MODE_PRIVATE
        )
        val editor = prefs.edit()

        val progressKeyBase = AppConstants.getPuzzleProgressKey(category, difficulty)
        val wordsSolvedKey = progressKeyBase + "_words_solved"
        val completedKey = progressKeyBase + "_completed"

        // This is the CRUCIAL part for cumulative progress
        val previouslySolved = prefs.getInt(wordsSolvedKey, 0)
        // newWordsSolvedCount is how many were solved *in the last session* from FillInActivity
        val currentTotalSolved = previouslySolved + newWordsSolvedCount // Accumulate

        editor.putInt(wordsSolvedKey, currentTotalSolved)
        Log.d(
            TAG,
            "Updating prefs for $category-$difficulty: Old solved: $previouslySolved, New this session: $newWordsSolvedCount, New Total: $currentTotalSolved"
        )

        // Determine if all words are completed
        // You need to know the total number of words for this category/difficulty
        val totalWordsInLevel = GameContentProvider.getTotalWordsForPuzzleCategory(
            requireContext().applicationContext,
            category,
            difficulty
        )
        Log.d(TAG, "Total words for $category-$difficulty from Provider: $totalWordsInLevel") // Add this log

        if (totalWordsInLevel > 0 && currentTotalSolved >= totalWordsInLevel) {
            editor.putBoolean(completedKey, true)
            Log.d(
                TAG,
                "$category-$difficulty marked as fully completed. Solved: $currentTotalSolved, Total: $totalWordsInLevel"
            )
        } else {
            editor.putBoolean(completedKey, false) // Ensure it's false if not all completed
            Log.d(
                TAG,
                "$category-$difficulty not yet fully completed. Solved: $currentTotalSolved, Total: $totalWordsInLevel. (Is totalWordsInLevel correctly > 0?)"
            )
        }
        editor.apply()
    }


    override fun onResume() {
        super.onResume()
        // loadAndDisplayProgression() // Already called in onViewCreated and after onActivityResult
        // It's good to have it here if other external factors might change SharedPreferences
        // but can lead to multiple loads if not careful. For now, let's keep it.
        Log.d(TAG, "onResume: Reloading progression")
        loadAndDisplayProgression()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}