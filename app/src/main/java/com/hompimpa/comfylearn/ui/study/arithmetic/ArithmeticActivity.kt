package com.hompimpa.comfylearn.ui.study.arithmetic

import android.content.res.Configuration
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.edit
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.databinding.ActivityArithmeticBinding
import com.hompimpa.comfylearn.helper.BaseActivity
import com.hompimpa.comfylearn.helper.GameContentProvider
import java.io.IOException
import java.util.Locale
import kotlin.random.Random

class ArithmeticActivity : BaseActivity() {

    private lateinit var binding: ActivityArithmeticBinding
    private var currentLevel = 1
    private var problemsCompletedInLevel = 0

    private val problemsPerLevel = 10
    private val levels = listOf(
        Level(R.string.simple_addition, isAddition = true, maxNumber = 5),
        Level(R.string.addition_up, isAddition = true, maxNumber = 10),
        Level(R.string.simple_substraction, isAddition = false, maxNumber = 10),
        Level(R.string.substraction_up, isAddition = false, maxNumber = 20),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArithmeticBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadProgress()
        setupNewQuestion()

        binding.nextProblemButton.setOnClickListener {
            handleNextProblem()
        }
    }

    private fun setupNewQuestion() {
        if (currentLevel > levels.size) {
            handleGameCompletion()
            return
        }
        binding.nextProblemButton.isEnabled = true
        updateProgressUI()
        generateQuestion()
    }

    private fun handleGameCompletion() {
        Toast.makeText(this, getString(R.string.all_levels_completed), Toast.LENGTH_LONG).show()
        binding.nextProblemButton.isEnabled = false
    }

    private fun handleNextProblem() {
        problemsCompletedInLevel++
        if (problemsCompletedInLevel >= problemsPerLevel) {
            levelUp()
        }
        setupNewQuestion()
    }

    private fun generateQuestion() {
        val visualCategory = GameContentProvider.getGameCategories(this).random()
        val localizedWords = GameContentProvider.getWordsForCategory(this, visualCategory)
        if (localizedWords.isEmpty()) return

        val randomLocalizedWord = localizedWords.random()
        val englishEquivalent = getEnglishEquivalent(randomLocalizedWord, visualCategory)
        if (englishEquivalent == null) return

        val imagePath = GameContentProvider.getImagePath(visualCategory, englishEquivalent)
        val level = levels[currentLevel - 1]
        val isAddition = level.isAddition ?: Random.nextBoolean()

        val num1: Int
        val num2: Int
        val answer: Int

        if (isAddition) {
            num1 = Random.nextInt(1, level.maxNumber + 1)
            num2 = Random.nextInt(1, level.maxNumber + 1)
            answer = num1 + num2
            binding.questionText.text = "$num1 + $num2 = $answer"
            binding.operatorTextView.text = "+"
        } else {
            num1 = Random.nextInt(2, level.maxNumber + 1)
            num2 = Random.nextInt(1, num1)
            answer = num1 - num2
            binding.questionText.text = "$num1 - $num2 = $answer"
            binding.operatorTextView.text = "-"
        }

        populateObjectsGrid(binding.firstOperandObjectsGrid, num1, imagePath)
        populateObjectsGrid(binding.secondOperandObjectsGrid, num2, imagePath)
        populateObjectsGrid(binding.answerObjectsGrid, answer, imagePath)
    }

    private fun populateObjectsGrid(gridLayout: GridLayout, count: Int, imagePath: String) {
        gridLayout.removeAllViews()
        if (count <= 0) return

        gridLayout.columnCount = when {
            count > 9 -> 5
            count > 4 -> 4
            else -> count.coerceAtLeast(1)
        }
        val itemSize = resources.getDimensionPixelSize(R.dimen.arithmetic_item_size)
        val itemMargin = resources.getDimensionPixelSize(R.dimen.arithmetic_item_margin)

        repeat(count) {
            val imageView = ImageView(this).apply {
                setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                setImageDrawable(GameContentProvider.loadSvgFromAssets(this@ArithmeticActivity, imagePath))
                layoutParams = GridLayout.LayoutParams().apply {
                    width = itemSize
                    height = itemSize
                    setMargins(itemMargin, itemMargin, itemMargin, itemMargin)
                }
            }
            gridLayout.addView(imageView)
        }
    }

    private fun getEnglishEquivalent(localizedWord: String, categoryName: String): String? {
        val resId = resources.getIdentifier(categoryName, "array", packageName)
        if (resId == 0) return null

        val localizedArray = resources.getStringArray(resId)
        val wordIndex = localizedArray.indexOfFirst { it.equals(localizedWord, ignoreCase = true) }
        if (wordIndex == -1) return null

        val englishConfig = Configuration(resources.configuration).apply { setLocale(Locale.ENGLISH) }
        val englishContext = createConfigurationContext(englishConfig)
        val englishArray = englishContext.resources.getStringArray(resId)

        return if (wordIndex < englishArray.size) englishArray[wordIndex] else null
    }

    private fun levelUp() {
        if (currentLevel < levels.size) {
            currentLevel++
            problemsCompletedInLevel = 0
            Toast.makeText(this, getString(R.string.level_up), Toast.LENGTH_SHORT).show()
            saveProgress()
        } else {
            handleGameCompletion()
        }
    }

    private fun updateProgressUI() {
        val level = levels[currentLevel - 1]
        val levelName = getString(level.nameResId)
        binding.levelNameText.text = getString(R.string.level_name_format, currentLevel, levelName)
        binding.levelProgressBar.max = problemsPerLevel
        binding.levelProgressBar.progress = problemsCompletedInLevel
    }

    private fun saveProgress() {
        getSharedPreferences("ArithmeticProgress", MODE_PRIVATE).edit {
            putInt("currentLevel", currentLevel)
            putInt("problemsCompletedInLevel", problemsCompletedInLevel)
        }
    }

    private fun loadProgress() {
        val prefs = getSharedPreferences("ArithmeticProgress", MODE_PRIVATE)
        currentLevel = prefs.getInt("currentLevel", 1)
        problemsCompletedInLevel = prefs.getInt("problemsCompletedInLevel", 0)
    }

    data class Level(@StringRes val nameResId: Int, val isAddition: Boolean?, val maxNumber: Int)
}