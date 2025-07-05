package com.hompimpa.comfylearn.ui.study.number

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.hompimpa.comfylearn.HomeActivity
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.ui.study.alphabet.AlphabetActivity

class NumberActivity : AppCompatActivity() {

    private lateinit var nextButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var homeButton: ImageButton
    private lateinit var changeButton: ImageButton
    private var currentNumber: Int = 1 // Default starting number
    val fragment = NumberFragment.newInstance(currentNumber)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_number)

        nextButton = findViewById(R.id.nextButton)
        backButton = findViewById(R.id.backButton)
        homeButton = findViewById(R.id.homeButton)
        changeButton = findViewById(R.id.changeButton)

        // Retrieve the number passed from the intent
        currentNumber = intent.getIntExtra("number", 1)

        loadNumberFragment(currentNumber)

        nextButton.setOnClickListener { navigateToNumber(currentNumber + 1) }
        backButton.setOnClickListener { navigateToNumber(currentNumber - 1) }
        homeButton.setOnClickListener { navigateToHome() }
        changeButton.setOnClickListener { navigateToOther() }
    }

    private fun loadNumberFragment(number: Int) {
        supportFragmentManager.commit {
            replace(R.id.fragment_container, NumberFragment.newInstance(number))
        }
    }

    private fun navigateToNumber(number: Int) {
        if (number in 1..10) {
            currentNumber = number
            loadNumberFragment(number)
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToOther() {
        val intent = Intent(this, AlphabetActivity::class.java)
        startActivity(intent)
        finish()
    }
}