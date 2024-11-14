package com.hompimpa.comfylearn.ui.games.fillIn

import android.os.Bundle
import android.view.Gravity
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.hompimpa.comfylearn.R

class FillInActivity : AppCompatActivity() {

    private lateinit var questions: List<Question>
    private lateinit var currentQuestion: Question
    private lateinit var letterSlots: MutableList<TextView>
    private lateinit var letterOptions: MutableList<TextView>
    private var currentQuestionIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fill_in)

        loadQuestions()
        setupQuestion()
    }

    private fun loadQuestions() {
        val words = resources.getStringArray(R.array.animal)

        questions = words.map { word ->
            // Construct the image URL based on the word
            val imageUrl =
                "https://juankevinr.rf.gd/animals/${word.lowercase()}.png" // Replace with your actual URL pattern
            Question(word, imageUrl) // Store the URL instead of a drawable resource ID
        }
    }

    private fun setupQuestion() {
        currentQuestion = questions[currentQuestionIndex]
        letterSlots = mutableListOf()
        letterOptions = mutableListOf()

        // Set image for the question if applicable
        Glide.with(this)
            .load(currentQuestion.imageUrl) // Load the image from the URL
            .into(findViewById<ImageView>(R.id.imagePrompt)) // Set the loaded image into the ImageView

        // Initialize letter slots dynamically based on the word length
        val slotContainer = findViewById<LinearLayout>(R.id.letterSlots)
        slotContainer.removeAllViews()
        currentQuestion.word.forEach { _ ->
            val slot = TextView(this).apply {
                text = "_" // Placeholder character
                textSize = 24f
                setBackgroundResource(R.color.colorAccent) // Use a suitable background
                setPadding(16, 16, 16, 16)
                gravity = Gravity.CENTER // Center the placeholder character
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            letterSlots.add(slot)
            slotContainer.addView(slot)
        }

        // Initialize letter options with the full alphabet
        val buttonContainer = findViewById<GridLayout>(R.id.letterOptions)
        buttonContainer.removeAllViews()
        val alphabet = ('A'..'Z').toList()

        alphabet.forEach { letter ->
            val option = TextView(this).apply {
                text = letter.toString()
                textSize = 24f
                setBackgroundResource(R.drawable.button_option)
                setPadding(16, 16, 16, 16)
                gravity = Gravity.CENTER // Center the letter in the button
                setOnClickListener {
                    fillSlotWithLetter(letter.toString())
                }
            }
            letterOptions.add(option)
            buttonContainer.addView(option)
        }
    }

    private fun fillSlotWithLetter(letter: String) {
        // Find the first empty slot and fill it with the selected letter
        for (slot in letterSlots) {
            if (slot.text == "_") { // Check for the placeholder
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
            // Move to the next question
            currentQuestionIndex = (currentQuestionIndex + 1) % questions.size
            setupQuestion()
        } else {
            // Optionally, provide feedback for incorrect answers
            Toast.makeText(this, "Try again!", Toast.LENGTH_SHORT).show()
        }
    }
}