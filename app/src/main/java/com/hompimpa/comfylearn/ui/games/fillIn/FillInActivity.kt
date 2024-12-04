package com.hompimpa.comfylearn.ui.games.fillIn

import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.caverock.androidsvg.SVG
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.databinding.ActivityFillInBinding
import com.hompimpa.comfylearn.helper.LetterOptionsAdapter
import com.hompimpa.comfylearn.helper.Question

class FillInActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFillInBinding
    private lateinit var questions: List<Question>
    private lateinit var currentQuestion: Question
    private lateinit var letterSlots: MutableList<TextView>
    private var category: String = "animal" // Default category

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFillInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get category from intent or use default
        category = intent.getStringExtra("CATEGORY") ?: "animal"

        loadQuestions()
        setupQuestion()
    }

    private fun loadQuestions() {
        val wordArray = resources.getStringArray(getCategoryResourceId())
        questions = wordArray.map { word ->
            // Dynamically set the SVG path based on the category
            val svgPath = "en/${category.lowercase()}_${word.lowercase()}.svg"
            Question(word, svgPath)
        }
    }

    private fun getCategoryResourceId(): Int {
        return when (category) {
            "animal" -> R.array.animal
            "object" -> R.array.`object`
            else -> R.array.animal // Default to animal if category not found
        }
    }

    private fun setupQuestion() {
        // Pick a random question
        currentQuestion = questions.random()
        binding.imagePrompt.setImageDrawable(loadSvgFromAssets(currentQuestion.imageUrl))

        // Setup slots for letters
        setupLetterSlots()

        // Setup letter options
        setupLetterOptions()

        // Setup undo button
        setupUndoButton()
    }

    private fun setupLetterSlots() {
        letterSlots = mutableListOf()
        binding.letterSlots.removeAllViews()
        currentQuestion.word.forEach { _ ->
            val slot = TextView(this).apply {
                text = "_"
                textSize = 24f
                gravity = Gravity.CENTER
                setPadding(0, 0, 8, 0)
            }
            letterSlots.add(slot)
            binding.letterSlots.addView(slot)
        }
    }

    private val availableLetters = listOf(
        "Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P",
        "A", "S", "D", "F", "G", "H", "J", "K", "L",
        "Z", "X", "C", "V", "B", "N", "M"
    )

    private fun setupLetterOptions() {
        val adapter = LetterOptionsAdapter(availableLetters) { letter ->
            fillSlotWithLetter(letter)
        }

        // Use GridLayoutManager with the desired number of columns
        binding.letterOptions.layoutManager = GridLayoutManager(this, 10)
        binding.letterOptions.adapter = adapter
    }


    private fun setupUndoButton() {
        binding.deleteButton.setOnClickListener {
            for (i in letterSlots.size - 1 downTo 0) {
                val slot = letterSlots[i]
                if (slot.text != "_") {
                    slot.text = "_"
                    break
                }
            }
        }
    }

    private fun fillSlotWithLetter(letter: String) {
        for (slot in letterSlots) {
            if (slot.text == "_") {
                slot.text = letter
                checkAnswer()
                break
            }
        }
    }

    private fun checkAnswer() {
        val userAnswer = letterSlots.joinToString("") { it.text.toString() }
        if (userAnswer.equals(currentQuestion.word, true)) {
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show()
            setupQuestion()
        }
    }

    private fun loadSvgFromAssets(path: String): PictureDrawable? {
        return try {
            val svg = SVG.getFromInputStream(assets.open(path))
            PictureDrawable(svg.renderToPicture())
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
