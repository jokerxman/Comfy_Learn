package com.hompimpa.comfylearn.ui.study.arithmetic

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import com.hompimpa.comfylearn.databinding.ActivityArithmeticBinding
import com.hompimpa.comfylearn.helper.BaseActivity
import kotlin.random.Random

class ArithmeticActivity : BaseActivity() {

    private lateinit var binding: ActivityArithmeticBinding // Declare the binding variable
    private var correctAnswer = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityArithmeticBinding.inflate(layoutInflater)
        setContentView(binding.root)

        generateQuestion()

        binding.submitButton.setOnClickListener {
            checkAnswer()
        }
    }

    private fun generateQuestion() {
        val num1: Int
        val num2: Int

        if (Random.nextBoolean()) { // Randomly decide between addition and subtraction
            // Addition case: Ensure that num1 + num2 <= 9 and at least 1
            num1 = Random.nextInt(1, 10) // Random number between 1 and 9
            num2 = Random.nextInt(1, 10 - num1 + 1) // Ensure sum does not exceed 9
            correctAnswer = num1 + num2
            binding.questionText.text = "What is $num1 + $num2?"
        } else {
            // Subtraction case: Ensure that num1 - num2 >= 1 and num1 <= 9
            num1 = Random.nextInt(1, 10) // Random number between 1 and 9
            num2 =
                Random.nextInt(0, num1) // Ensure that num2 is less than num1 to avoid zero result
            correctAnswer = num1 - num2
            binding.questionText.text = "What is $num1 - $num2?"
        }

        // Generate answer options including the correct answer and some incorrect ones
        generateAnswerOptions(correctAnswer)
    }

    private fun generateAnswerOptions(correctAnswer: Int) {
        val options = mutableSetOf(correctAnswer)

        // Generate two additional incorrect answers
        while (options.size < 3) {
            val incorrectAnswer = Random.nextInt(0, 10) // Generate random numbers from 0 to 9
            if (incorrectAnswer != correctAnswer) {
                options.add(incorrectAnswer)
            }
        }

        // Clear previous RadioButtons if any exist
        binding.answerOptions.removeAllViews()

        // Create RadioButtons for each option and add them to the RadioGroup
        for (option in options.shuffled()) { // Shuffle options for randomness
            val radioButton = RadioButton(this).apply {
                text = option.toString()
                id = View.generateViewId() // Generate a unique ID for each RadioButton
            }
            binding.answerOptions.addView(radioButton)
        }
    }

    private fun checkAnswer() {
        val selectedRadioButtonId = binding.answerOptions.checkedRadioButtonId

        if (selectedRadioButtonId == -1) {
            binding.feedbackText.text = "Please select an answer."
            return
        }

        val selectedRadioButton = findViewById<RadioButton>(selectedRadioButtonId)
        val userAnswer = selectedRadioButton.text.toString().toInt()

        if (userAnswer == correctAnswer) {
            binding.feedbackText.text = "Correct! Well done!"
            generateQuestion() // Generate a new question after answering correctly
        } else {
            binding.feedbackText.text = "Incorrect. Try again!"
        }
    }
}