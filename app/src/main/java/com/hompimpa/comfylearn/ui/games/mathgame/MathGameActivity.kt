package com.hompimpa.comfylearn.ui.games.mathgame

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.res.Configuration
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGParseException
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.databinding.ActivityMathGameBinding
import com.hompimpa.comfylearn.helper.BaseActivity
import com.hompimpa.comfylearn.helper.GameManager
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.io.IOException
import kotlin.random.Random

class MathGameActivity : BaseActivity() {

    private lateinit var binding: ActivityMathGameBinding
    private lateinit var gameManager: GameManager

    private var currentItemAssetPath: String = ""
    private var expectedAnswer: Int = 0
    private var operand1Value: Int = 0
    private var operand2Value: Int = 0
    private var isAddition: Boolean = true

    private val allAnswerButtons = mutableListOf<Button>()
    private val activeAnswerButtons = mutableListOf<Button>()

    private var availableCountingItemFilenames = listOf<String>()
    private val assetSubfolder = "en/"

    private var currentDifficultyStr: String = DIFFICULTY_MEDIUM_STR

    companion object {
        private const val TAG = "MathGameActivity"
        const val EXTRA_SELECTED_DIFFICULTY = "com.hompimpa.comfylearn.SELECTED_DIFFICULTY"
        const val DIFFICULTY_EASY_STR = "EASY"
        const val DIFFICULTY_MEDIUM_STR = "MEDIUM"
        const val DIFFICULTY_HARD_STR = "HARD"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMathGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentDifficultyStr =
            intent.getStringExtra(EXTRA_SELECTED_DIFFICULTY) ?: DIFFICULTY_MEDIUM_STR

        gameManager = GameManager(applicationContext)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        loadCountingItemFilenames()

        allAnswerButtons.addAll(
            listOf(
                binding.answerButton1,
                binding.answerButton2,
                binding.answerButton3,
                binding.answerButton4
            )
        )
        allAnswerButtons.forEach { button ->
            button.setOnClickListener { onAnswerChoiceClicked(it as Button) }
        }

        binding.nextProblemButton.setOnClickListener { generateNewProblem() }

        applyDifficultySettings()
        generateNewProblem()

        lifecycleScope.launch {
            gameManager.problemsSolvedFlow.collect { solvedCount ->
                supportActionBar?.subtitle = getString(R.string.problems_solved_format, solvedCount)
            }
        }
    }

    /**
     * This function has been updated to be compatible with older Android versions.
     * It creates a temporary context with the "en" locale to fetch the correct string array.
     */
    private fun loadCountingItemFilenames() {
        try {
            val config = Configuration(applicationContext.resources.configuration)
            config.setLocale(java.util.Locale.ENGLISH)
            val englishContext = applicationContext.createConfigurationContext(config)
            availableCountingItemFilenames = englishContext.resources.getStringArray(R.array.animal).toList()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading animal names: ${e.message}", e)
            availableCountingItemFilenames = listOf("apple") // Fallback
            Toast.makeText(this, "Error loading items, using fallback.", Toast.LENGTH_SHORT)
                .show()
        }

        if (availableCountingItemFilenames.isEmpty()) {
            Log.e(TAG, "Critical: No counting items available. Fallback to 'apple'.")
            availableCountingItemFilenames = listOf("apple")
            Toast.makeText(this, "Item list empty, using fallback.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun applyDifficultySettings() {
        binding.problemTextView.visibility = when (currentDifficultyStr) {
            DIFFICULTY_EASY_STR -> View.VISIBLE
            DIFFICULTY_MEDIUM_STR, DIFFICULTY_HARD_STR -> View.GONE
            else -> View.VISIBLE
        }

        activeAnswerButtons.clear()
        val numChoices = when (currentDifficultyStr) {
            DIFFICULTY_EASY_STR -> 2
            DIFFICULTY_MEDIUM_STR -> 3
            DIFFICULTY_HARD_STR -> 4
            else -> 3
        }

        allAnswerButtons.forEachIndexed { index, button ->
            if (index < numChoices) {
                button.visibility = View.VISIBLE
                activeAnswerButtons.add(button)
            } else {
                button.visibility = View.GONE
            }
        }
    }

    private fun generateNewProblem() {
        if (availableCountingItemFilenames.isEmpty()) {
            Log.e(TAG, "Cannot generate problem, no counting items available.")
            binding.problemTextView.text = getString(R.string.error_exclamation)
            Toast.makeText(this, "Error generating problem.", Toast.LENGTH_SHORT).show()
            return
        }

        applyDifficultySettings()

        binding.firstOperandObjectsGrid.removeAllViews()
        binding.secondOperandObjectsGrid.removeAllViews()

        activeAnswerButtons.forEach {
            it.isEnabled = true
            it.alpha = 1f
            it.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent))
        }
        binding.nextProblemButton.visibility = View.GONE

        val randomItemName = availableCountingItemFilenames.random()
        currentItemAssetPath =
            "${assetSubfolder}animal_${randomItemName.lowercase().replace(" ", "_")}.svg"

        val maxOperandEasy = 5
        val maxOperandMedium = 7
        val maxOperandHard = 9

        isAddition = Random.nextBoolean()
        if (isAddition) {
            val maxVal = when (currentDifficultyStr) {
                DIFFICULTY_EASY_STR -> maxOperandEasy
                DIFFICULTY_MEDIUM_STR -> maxOperandMedium
                DIFFICULTY_HARD_STR -> maxOperandHard
                else -> maxOperandMedium
            }
            operand1Value = Random.nextInt(1, maxVal + 1)
            operand2Value = Random.nextInt(1, maxVal + 1)
            expectedAnswer = operand1Value + operand2Value
            if (binding.problemTextView.isVisible) {
                binding.problemTextView.text = getString(R.string.addition_problem_format, operand1Value, operand2Value)
            }
            binding.operatorTextView.text = "+"
        } else {
            val maxMinuend = when (currentDifficultyStr) {
                DIFFICULTY_EASY_STR -> maxOperandEasy + 2
                DIFFICULTY_MEDIUM_STR -> maxOperandMedium + 2
                DIFFICULTY_HARD_STR -> maxOperandHard + 2
                else -> maxOperandMedium + 2
            }
            operand1Value = Random.nextInt(2, maxMinuend + 1)
            operand2Value = Random.nextInt(1, operand1Value + 1)
            expectedAnswer = operand1Value - operand2Value

            if (binding.problemTextView.isVisible) {
                binding.problemTextView.text = getString(R.string.subtraction_problem_format, operand1Value, operand2Value)
            }
            binding.operatorTextView.text = "-"
        }

        populateObjectsGrid(binding.firstOperandObjectsGrid, operand1Value, currentItemAssetPath)
        populateObjectsGrid(binding.secondOperandObjectsGrid, operand2Value, currentItemAssetPath)
        setupAnswerChoices()
    }

    private fun populateObjectsGrid(gridLayout: GridLayout, count: Int, fullAssetPath: String) {
        gridLayout.removeAllViews()
        if (count <= 0) return

        gridLayout.columnCount = when {
            count > 6 -> 3
            count > 1 -> 2
            else -> 1
        }.coerceAtMost(count.coerceAtLeast(1))

        gridLayout.post {
            val containerWidth = gridLayout.width
            if (containerWidth == 0) return@post

            val marginPx = dpToPx(2)
            val numColumns = gridLayout.columnCount
            val itemSizePx = (containerWidth / numColumns) - (marginPx * 2)

            gridLayout.removeAllViews()
            gridLayout.rowCount = (count + numColumns - 1) / numColumns

            repeat(count) {
                val imageView = createCountingObjectView(fullAssetPath, itemSizePx, marginPx)
                gridLayout.addView(imageView)
            }
        }
    }

    private fun createCountingObjectView(
        fullAssetPath: String,
        itemSizePx: Int,
        marginPx: Int
    ): ImageView {
        val imageView = ImageView(this).apply {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            scaleType = ImageView.ScaleType.FIT_CENTER
            layoutParams = GridLayout.LayoutParams().apply {
                width = itemSizePx
                height = itemSizePx
                setMargins(marginPx, marginPx, marginPx, marginPx)
            }
        }

        val pictureDrawable = loadSvgFromAssets(fullAssetPath)
        if (pictureDrawable != null) {
            imageView.setImageDrawable(pictureDrawable)
        } else {
            imageView.setImageResource(R.drawable.ic_placeholder_image)
            Log.w(TAG, "Failed to load SVG: $fullAssetPath. Using placeholder.")
        }
        return imageView
    }

    private fun loadSvgFromAssets(path: String?): PictureDrawable? {
        if (path.isNullOrEmpty()) {
            return null
        }
        try {
            assets.open(path).use { inputStream ->
                val svg = SVG.getFromInputStream(inputStream)
                if (svg?.documentWidth == -1f || svg?.documentHeight == -1f) {
                    Log.w(TAG, "SVG document dimensions invalid for: $path")
                }
                val picture = svg?.renderToPicture()
                return if (picture != null) PictureDrawable(picture) else null
            }
        } catch (e: Exception) {
            handleSvgLoadingException(e, path)
        }
        return null
    }

    private fun handleSvgLoadingException(e: Exception, path: String) {
        val errorMessage = when (e) {
            is FileNotFoundException -> "SVG file not found: $path"
            is SVGParseException -> "SVG parsing error: $path"
            is IOException -> "IOException reading SVG: $path"
            else -> "Unexpected error loading SVG: $path"
        }
        Log.e(TAG, errorMessage, e)
    }

    private fun setupAnswerChoices() {
        val numChoices = activeAnswerButtons.size
        if (numChoices == 0) return

        val choices = mutableSetOf<Int>()
        choices.add(expectedAnswer)

        val maxWrongAnswerOffset = when (currentDifficultyStr) {
            DIFFICULTY_EASY_STR -> 2
            DIFFICULTY_MEDIUM_STR -> 3
            DIFFICULTY_HARD_STR -> 4
            else -> 3
        }

        var attemptsToFindUnique = 0
        val maxAttemptsForUnique = 20 * numChoices

        while (choices.size < numChoices && attemptsToFindUnique < maxAttemptsForUnique) {
            var wrongAnswerOffset = Random.nextInt(-maxWrongAnswerOffset, maxWrongAnswerOffset + 1)
            if (wrongAnswerOffset == 0 && maxWrongAnswerOffset != 0) {
                wrongAnswerOffset = Random.nextInt(1, maxWrongAnswerOffset + 1) * if (Random.nextBoolean()) 1 else -1
            }
            val potentialWrongAnswer = (expectedAnswer + wrongAnswerOffset).coerceAtLeast(0)
            choices.add(potentialWrongAnswer)
            attemptsToFindUnique++
        }

        if (choices.size < numChoices) {
            Log.w(TAG, "Could not generate enough unique choices, filling deterministically.")
            for (i in 1..(numChoices + maxWrongAnswerOffset)) {
                if (choices.size >= numChoices) break
                choices.add((expectedAnswer + i).coerceAtLeast(0))
                if (choices.size >= numChoices) break
                choices.add((expectedAnswer - i).coerceAtLeast(0))
            }
        }
        while (choices.size < numChoices) {
            choices.add(Random.nextInt(0, expectedAnswer + maxWrongAnswerOffset + 5))
            Log.w(TAG, "Forced to add potentially non-ideal choices to meet button count.")
        }

        val shuffledChoices = choices.toList().shuffled()
        activeAnswerButtons.forEachIndexed { index, button ->
            if (index < shuffledChoices.size) {
                button.text = shuffledChoices[index].toString()
                button.tag = shuffledChoices[index]
            } else {
                button.visibility = View.GONE
            }
        }
    }

    private fun onAnswerChoiceClicked(button: Button) {
        val chosenAnswer = button.tag as? Int ?: return

        activeAnswerButtons.forEach { it.isEnabled = false }

        if (chosenAnswer == expectedAnswer) {
            Toast.makeText(this, getString(R.string.feedback_correct), Toast.LENGTH_SHORT).show()
            lifecycleScope.launch { gameManager.incrementProblemsSolved() }
            button.setBackgroundColor(ContextCompat.getColor(this, R.color.green_balloon))
            animateCorrectAnswer(button)
            binding.nextProblemButton.visibility = View.VISIBLE
        } else {
            Toast.makeText(this, getString(R.string.feedback_incorrect_try_again), Toast.LENGTH_SHORT).show()
            button.setBackgroundColor(ContextCompat.getColor(this, R.color.feedback_incorrect_bg))
            Handler(Looper.getMainLooper()).postDelayed({
                activeAnswerButtons.find { (it.tag as? Int) == expectedAnswer }
                    ?.setBackgroundColor(ContextCompat.getColor(this, R.color.green_balloon_border))
                animateIncorrectAnswer(button)
                binding.nextProblemButton.visibility = View.VISIBLE
            }, 1000)
        }
    }

    private fun animateCorrectAnswer(button: Button) {
        val scaleX = ObjectAnimator.ofFloat(button, View.SCALE_X, 1f, 1.2f, 1f)
        val scaleY = ObjectAnimator.ofFloat(button, View.SCALE_Y, 1f, 1.2f, 1f)
        scaleX.interpolator = OvershootInterpolator()
        scaleY.interpolator = OvershootInterpolator()
        AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = 500
            start()
        }
    }

    private fun animateIncorrectAnswer(button: Button) {
        ObjectAnimator.ofFloat(
            button, View.TRANSLATION_X, 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f
        ).apply {
            duration = 500
            start()
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
