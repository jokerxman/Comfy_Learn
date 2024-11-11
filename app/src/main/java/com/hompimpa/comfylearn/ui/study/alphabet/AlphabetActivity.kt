package com.hompimpa.comfylearn.ui.study.alphabet

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.hompimpa.comfylearn.HomeActivity
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.ui.study.number.NumberActivity

class AlphabetActivity : AppCompatActivity() {

    private lateinit var nextButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var homeButton: ImageButton
    private lateinit var changeButton: ImageButton
    private var currentLetter: Char = 'A' // Default starting letter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alphabet)

        nextButton = findViewById(R.id.nextButton)
        backButton = findViewById(R.id.backButton)
        homeButton = findViewById(R.id.homeButton)
        changeButton = findViewById(R.id.changeButton)

        // Retrieve the letter passed from the intent
        currentLetter = intent.getCharExtra("letter", 'A')

        loadAlphabetFragment(currentLetter)

        nextButton.setOnClickListener { navigateToLetter(currentLetter + 1) }
        backButton.setOnClickListener { navigateToLetter(currentLetter - 1) }
        homeButton.setOnClickListener { navigateToHome() }
        changeButton.setOnClickListener { navigateToOther() }
    }

    private fun loadAlphabetFragment(letter: Char) {
        supportFragmentManager.commit {
            replace(R.id.fragment_container, AlphabetFragment.newInstance(letter))
        }
    }

    private fun navigateToLetter(letter: Char) {
        if (letter in 'A'..'Z') {
            currentLetter = letter
            loadAlphabetFragment(letter)
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish() // Close AlphabetActivity to prevent back stack issues
    }

    private fun navigateToOther() {
        val intent = Intent(this, NumberActivity::class.java)
        intent.putExtra("number", 1)
        startActivity(intent)
        finish()
    }
}