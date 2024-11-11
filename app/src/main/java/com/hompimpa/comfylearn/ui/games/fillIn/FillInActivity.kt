package com.hompimpa.comfylearn.ui.games.fillIn

import android.content.ClipData
import android.os.Bundle
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hompimpa.comfylearn.R

class FillInActivity : AppCompatActivity() {

    private val questions = listOf(
        Question("TREE", R.drawable.image_tree)
        // Add more questions as needed
    )

    private lateinit var currentQuestion: Question
    private lateinit var letterSlots: MutableList<TextView>
    private lateinit var letterOptions: MutableList<TextView>
    private var currentQuestionIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fill_in)

        setupQuestion()
    }

    private fun setupQuestion() {
        currentQuestion = questions[currentQuestionIndex]
        letterSlots = mutableListOf()
        letterOptions = mutableListOf()

        // Set image for the question if applicable
        findViewById<ImageView>(R.id.imagePrompt).setImageResource(currentQuestion.imageResId)

        // Initialize letter slots dynamically based on the word length
        val slotContainer = findViewById<LinearLayout>(R.id.letterSlots)
        slotContainer.removeAllViews()
        currentQuestion.word.forEach { _ ->
            val slot = TextView(this).apply {
                text = ""
                textSize = 24f
                setBackgroundResource(R.color.colorAccent) // Use a suitable background
                setPadding(16, 16, 16, 16)
            }
            letterSlots.add(slot)
            slotContainer.addView(slot)
        }

        // Initialize letter options with the correct letters and random other letters
        val gridLayout = findViewById<GridLayout>(R.id.letterOptions)
        gridLayout.removeAllViews()
        val correctLetters = currentQuestion.word.toList()
        val extraLetters =
            ('A'..'Z').filterNot { it in correctLetters }.shuffled().take(10 - correctLetters.size)
        val letters =
            (correctLetters + extraLetters).shuffled()  // Ensure all correct letters are included, then shuffle

        letters.forEach { letter ->
            val option = TextView(this).apply {
                text = letter.toString()
                textSize = 24f
                setBackgroundResource(R.drawable.baseline_circle_24)
                setPadding(16, 16, 16, 16)
                setOnTouchListener { view, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        view.performClick()
                        val clipData = ClipData.newPlainText("", text)
                        val dragShadow = View.DragShadowBuilder(view)
                        view.startDragAndDrop(clipData, dragShadow, view, 0)
                        true
                    } else false
                }
            }
            letterOptions.add(option)
            gridLayout.addView(option)
        }

        // Set drag listeners on slots
        letterSlots.forEach { slot ->
            slot.setOnDragListener(dragListener)
        }
    }


    private val dragListener = View.OnDragListener { view, event ->
        when (event.action) {
            DragEvent.ACTION_DROP -> {
                val draggedView = event.localState as TextView
                val droppedLetter = draggedView.text.toString()

                // Set the dropped letter in the slot and show the letter again in the options
                (view as TextView).text = droppedLetter
                draggedView.visibility = View.VISIBLE

                checkAnswer()
                true
            }

            else -> true
        }
    }

    private fun checkAnswer() {
        val userAnswer = letterSlots.joinToString("") { it.text.toString() }
        if (userAnswer.equals(currentQuestion.word, ignoreCase = true)) {
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show()
            // Move to the next question
            currentQuestionIndex = (currentQuestionIndex + 1) % questions.size
            setupQuestion()
        }
    }
}
