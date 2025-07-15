package com.hompimpa.comfylearn.ui.learnProg

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.databinding.FragmentLearnprogBinding
import com.hompimpa.comfylearn.helper.AppConstants
import com.hompimpa.comfylearn.helper.GameContentProvider
import com.hompimpa.comfylearn.ui.games.DifficultySelectionActivity
import com.hompimpa.comfylearn.ui.games.drawing.DrawingActivity
import com.hompimpa.comfylearn.ui.games.fillIn.FillInActivity
import com.hompimpa.comfylearn.ui.games.mathgame.MathGameActivity
import com.hompimpa.comfylearn.ui.games.puzzle.PuzzleActivity
import com.hompimpa.comfylearn.ui.study.alphabet.AlphabetActivity
import com.hompimpa.comfylearn.ui.study.arithmetic.ArithmeticActivity
import com.hompimpa.comfylearn.ui.study.number.NumberActivity
import com.hompimpa.comfylearn.ui.study.spelling.SpellingActivity
import java.util.Locale

data class ProgressionItem(
    val gameType: String,
    val category: String,
    val difficulty: String?,
    val activityName: String,
    val status: String
)

class LearnProgFragment : Fragment() {

    private var _binding: FragmentLearnprogBinding? = null
    private val binding get() = _binding!!

    private val gameLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            loadAndDisplayProgression()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLearnprogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadAndDisplayProgression()
    }

    override fun onResume() {
        super.onResume()
        loadAndDisplayProgression()
    }

    private fun loadAndDisplayProgression() {
        binding.layoutProgressionContainer.removeAllViews()
        val progressionItems = loadProgressionData()
        updateProgressionUI(progressionItems)
    }

    private fun loadProgressionData(): List<ProgressionItem> {
        val prefs = requireActivity().getSharedPreferences(AppConstants.PREFS_PROGRESSION, Context.MODE_PRIVATE)
        val gameCategories = GameContentProvider.getGameCategories(requireContext())
        val puzzleDifficulties = GameContentProvider.getPuzzleDifficulties(requireContext())
        val items = mutableListOf<ProgressionItem>()
        val arithmeticPrefs = requireActivity().getSharedPreferences("ArithmeticProgress", Context.MODE_PRIVATE)
        val arithCurrentLevel = arithmeticPrefs.getInt("currentLevel", 1)
        val isArithVisited = arithCurrentLevel > 1
        // Study Activities
        items.add(createSimpleProgressItem("ALPHABET", "Alphabet", prefs.getBoolean(AppConstants.getAlphabetVisitedKey(), false)))
        items.add(createSimpleProgressItem("NUMBER", "Number", prefs.getBoolean(AppConstants.getNumberVisitedKey(), false)))
        items.add(createSimpleProgressItem("ARITHMETIC", "Arithmetic", isArithVisited))

        // Game Activities
        items.add(createSimpleProgressItem("DRAWING", "Drawing", prefs.getBoolean(AppConstants.getDrawingVisitedKey(), false)))
        val mathProblemsSolved = prefs.getInt(AppConstants.getMathGameProgressKey(), 0)
        items.add(ProgressionItem("MATH", "Math Game", null, "Math Game", if (mathProblemsSolved > 0) "$mathProblemsSolved problems solved" else "Not Started"))

        gameCategories.forEach { category ->
            val spellingKey = AppConstants.getSpellingCategoryProgressKey(category)
            items.add(
                ProgressionItem(
                    gameType = "SPELLING", category = category, difficulty = null,
                    activityName = "Spelling: ${category.replaceFirstChar { it.titlecase(Locale.getDefault()) }}",
                    status = if (prefs.getBoolean(spellingKey, false)) "Visited" else "Not Started"
                )
            )
        }

        gameCategories.forEach { category ->
            puzzleDifficulties.forEach { difficulty ->
                val progressKeyBase = AppConstants.getPuzzleProgressKey(category, difficulty)
                val isCompleted = prefs.getBoolean(progressKeyBase + "_completed", false)
                val wordsSolved = prefs.getInt(progressKeyBase + "_words_solved", 0)
                val totalWords = GameContentProvider.getTotalWordsForPuzzleCategory(requireContext(), category, difficulty)
                val status = when {
                    isCompleted -> "Completed"
                    wordsSolved > 0 -> "$wordsSolved / $totalWords words"
                    else -> "Not Started"
                }
                items.add(
                    ProgressionItem(
                        gameType = "PUZZLE", category = category, difficulty = difficulty,
                        activityName = "Puzzle: ${category.replaceFirstChar { it.titlecase(Locale.getDefault()) }} - $difficulty",
                        status = status
                    )
                )
            }
        }
        return items
    }

    private fun createSimpleProgressItem(gameType: String, name: String, isVisited: Boolean): ProgressionItem {
        return ProgressionItem(
            gameType = gameType, category = name, difficulty = null,
            activityName = name, status = if (isVisited) "Visited" else "Not Started"
        )
    }

    private fun updateProgressionUI(items: List<ProgressionItem>) {
        binding.layoutProgressionContainer.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        if (items.isEmpty()) {
            val noDataView = TextView(context).apply {
                text = getString(R.string.no_progress)
                gravity = Gravity.CENTER
            }
            binding.layoutProgressionContainer.addView(noDataView)
            return
        }

        items.forEach { item ->
            val itemView = inflater.inflate(R.layout.item_progression, binding.layoutProgressionContainer, false)
            val itemLayout: LinearLayout = itemView.findViewById(R.id.progression_item_layout)
            val nameTextView: TextView = itemView.findViewById(R.id.activityNameTextView)
            val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)

            nameTextView.text = item.activityName
            statusTextView.text = item.status
            itemLayout.setOnClickListener { startGame(item) }
            binding.layoutProgressionContainer.addView(itemView)
        }
    }

    private fun startGame(item: ProgressionItem) {
        val intent: Intent? = when (item.gameType) {
            "ALPHABET" -> Intent(activity, AlphabetActivity::class.java)
            "NUMBER" -> Intent(activity, NumberActivity::class.java)
            "ARITHMETIC" -> Intent(activity, ArithmeticActivity::class.java)
            "DRAWING" -> Intent(activity, DrawingActivity::class.java)
            "SPELLING" -> Intent(activity, SpellingActivity::class.java).apply { putExtra("category", item.category) }
            "PUZZLE" -> Intent(activity, PuzzleActivity::class.java).apply {
                putExtra("CATEGORY", item.category)
                putExtra("DIFFICULTY", item.difficulty)
            }
            "MATH" -> Intent(activity, MathGameActivity::class.java) // Math game might need a difficulty selector first
            else -> null
        }
        intent?.let { gameLauncher.launch(it) } ?: Toast.makeText(requireContext(), "Coming soon!", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}