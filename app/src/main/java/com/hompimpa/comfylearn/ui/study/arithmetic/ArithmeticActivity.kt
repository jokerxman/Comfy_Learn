package com.hompimpa.comfylearn.ui.study.arithmetic

import android.content.Context
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.Toast
import com.caverock.androidsvg.SVG
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.databinding.ActivityArithmeticBinding
import com.hompimpa.comfylearn.helper.BaseActivity
import java.io.IOException
import kotlin.random.Random

class ArithmeticActivity : BaseActivity() {

    private lateinit var binding: ActivityArithmeticBinding

    private var currentLevel = 1
    private var problemsCompletedInLevel = 0
    private val problemsPerLevel = 5 // User needs to see 5 examples to level up

    // List of SVG filenames in your assets folder
    private val countingItemAssets = listOf(
        "en/animal_apple.svg",
        "en/animal_cat.svg",
        "en/animal_dog.svg"
        // Add more SVG asset paths here
    )
    private lateinit var currentItemAssetPath: String

    // Define the rules for each level
    private val levels = listOf(
        Level(name = "Level 1: Simple Addition", isAddition = true, maxNumber = 5),
        Level(name = "Level 2: Simple Subtraction", isAddition = false, maxNumber = 5),
        Level(name = "Level 3: Addition up to 10", isAddition = true, maxNumber = 10),
        Level(name = "Level 4: Subtraction up to 10", isAddition = false, maxNumber = 10)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArithmeticBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadProgress()
        updateProgressUI()
        generateQuestion()

        binding.nextProblemButton.setOnClickListener {
            problemsCompletedInLevel++
            if (problemsCompletedInLevel >= problemsPerLevel) {
                levelUp()
            }
            updateProgressUI()
            generateQuestion()
        }
    }

    private fun generateQuestion() {
        if (currentLevel > levels.size) {
            binding.questionText.text = "Congratulations!"
            binding.visualProblemContainer.removeAllViews()
            binding.nextProblemButton.text = "Start Over"
            binding.nextProblemButton.setOnClickListener {
                currentLevel = 1
                problemsCompletedInLevel = 0
                saveProgress()
                updateProgressUI()
                generateQuestion()
            }
            return
        }

        // Randomly select a new SVG for this question
        currentItemAssetPath = countingItemAssets.random()

        val level = levels[currentLevel - 1]
        val num1: Int
        val num2: Int
        val answer: Int

        if (level.isAddition) {
            num1 = Random.nextInt(1, level.maxNumber + 1)
            num2 = Random.nextInt(1, level.maxNumber + 1)
            answer = num1 + num2
            binding.questionText.text = "$num1 + $num2 = $answer"
            binding.operatorTextView.text = "+"
        } else { // Subtraction
            num1 = Random.nextInt(2, level.maxNumber + 1)
            num2 = Random.nextInt(1, num1)
            answer = num1 - num2
            binding.questionText.text = "$num1 - $num2 = $answer"
            binding.operatorTextView.text = "-"
        }

        populateObjectsGrid(binding.firstOperandObjectsGrid, num1)
        populateObjectsGrid(binding.secondOperandObjectsGrid, num2)
    }

    private fun populateObjectsGrid(gridLayout: GridLayout, count: Int) {
        gridLayout.removeAllViews()
        if (count <= 0) return

        gridLayout.columnCount = when {
            count > 4 -> 3
            else -> 2
        }

        for (i in 1..count) {
            val imageView = ImageView(this).apply {
                // Set layer type to software for SVG rendering
                setLayerType(View.LAYER_TYPE_SOFTWARE, null)

                // Load the SVG from assets
                val svgDrawable = loadSvgFromAssets(currentItemAssetPath)
                if (svgDrawable != null) {
                    setImageDrawable(svgDrawable)
                } else {
                    setImageResource(R.drawable.ic_placeholder_image) // Fallback
                }

                layoutParams = GridLayout.LayoutParams().apply {
                    width = 100
                    height = 100
                    setMargins(8, 8, 8, 8)
                }
            }
            gridLayout.addView(imageView)
        }
    }

    private fun loadSvgFromAssets(path: String): PictureDrawable? {
        return try {
            val inputStream = assets.open(path)
            val svg = SVG.getFromInputStream(inputStream)
            PictureDrawable(svg.renderToPicture())
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun levelUp() {
        currentLevel++
        problemsCompletedInLevel = 0
        Toast.makeText(this, "Level Up!", Toast.LENGTH_SHORT).show()
        saveProgress()
    }

    private fun updateProgressUI() {
        if (currentLevel <= levels.size) {
            val level = levels[currentLevel - 1]
            binding.levelNameText.text = level.name
            binding.levelProgressBar.max = problemsPerLevel
            binding.levelProgressBar.progress = problemsCompletedInLevel
        } else {
            // Handle UI for when all levels are complete
            binding.levelNameText.text = "All Levels Completed!"
            binding.levelProgressBar.progress = binding.levelProgressBar.max
        }
    }

    private fun saveProgress() {
        val prefs = getSharedPreferences("ArithmeticProgress", Context.MODE_PRIVATE)
        prefs.edit().putInt("currentLevel", currentLevel).apply()
    }

    private fun loadProgress() {
        val prefs = getSharedPreferences("ArithmeticProgress", Context.MODE_PRIVATE)
        currentLevel = prefs.getInt("currentLevel", 1)
    }

    data class Level(val name: String, val isAddition: Boolean, val maxNumber: Int)
}
