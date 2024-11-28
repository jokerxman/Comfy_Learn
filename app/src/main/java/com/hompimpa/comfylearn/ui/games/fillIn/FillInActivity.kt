package com.hompimpa.comfylearn.ui.games.fillIn

import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View.LAYER_TYPE_SOFTWARE
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.caverock.androidsvg.SVG
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.databinding.ActivityFillInBinding
import kotlin.random.Random

class FillInActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFillInBinding
    private lateinit var questions: List<Question>
    private lateinit var currentQuestion: Question
    private lateinit var letterSlots: MutableList<TextView>
    private lateinit var letterOptions: MutableList<TextView>
    private var previousQuestionIndex = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFillInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadQuestions()
        setupQuestion()
    }

    private fun loadQuestions() {
        val words = resources.getStringArray(R.array.animal)
        questions = words.map { word ->
            val svgPath = "en/animal_${word.lowercase()}.svg"
            Question(word, svgPath)
        }
    }

    private fun setupQuestion() {
        // Pick a random question, avoiding repeats
        var randomIndex: Int
        do {
            randomIndex = Random.nextInt(questions.size)
        } while (randomIndex == previousQuestionIndex)

        previousQuestionIndex = randomIndex
        currentQuestion = questions[randomIndex]

        letterSlots = mutableListOf()
        letterOptions = mutableListOf()

        // Render the SVG from the assets folder
        loadSvgFromAssets(currentQuestion.imageUrl)

        // Initialize letter slots
        val slotContainer = binding.letterSlots
        slotContainer.removeAllViews()
        currentQuestion.word.forEach { _ ->
            val slot = TextView(this).apply {
                text = "_" // Placeholder character
                textSize = 24f
                setPadding(16, 16, 16, 16)
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            letterSlots.add(slot)
            slotContainer.addView(slot)
        }

        // Initialize letter options
        val buttonContainer = binding.letterOptions
        buttonContainer.removeAllViews()
        val alphabet = ('A'..'Z').shuffled()

        alphabet.forEach { letter ->
            val option = TextView(this).apply {
                text = letter.toString()
                textSize = 24f
                setPadding(16, 16, 16, 16)
                gravity = Gravity.CENTER
                setBackgroundResource(R.drawable.button_option)
                setOnClickListener {
                    fillSlotWithLetter(letter.toString())
                    it.isEnabled = false
                }
            }
            letterOptions.add(option)
            buttonContainer.addView(option)
        }
    }

    private fun loadSvgFromAssets(svgPath: String) {
        try {
            val inputStream = assets.open(svgPath)
            val svg = SVG.getFromInputStream(inputStream)
            val drawable = svg.renderToPicture().let { PictureDrawable(it) }
            binding.imagePrompt.apply {
                setLayerType(LAYER_TYPE_SOFTWARE, null)
                setImageDrawable(drawable)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fillSlotWithLetter(letter: String) {
        // Fill the first empty slot
        for (slot in letterSlots) {
            if (slot.text == "_") {
                slot.text = letter
                break
            }
        }
        checkAnswer()
    }

    private fun checkAnswer() {
        val userAnswer = letterSlots.joinToString("") { it.text.toString() }
        if (userAnswer.equals(currentQuestion.word, ignoreCase = true)) {
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show()
            regenerateQuestion()
        }
    }

    private fun regenerateQuestion() {
        letterSlots.forEach { it.text = "_" }
        letterOptions.forEach { it.isEnabled = true }
        setupQuestion()
    }
}
