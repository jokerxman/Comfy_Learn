package com.hompimpa.comfylearn.ui.study.number

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.fragment.app.commit
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.helper.BaseActivity
import com.hompimpa.comfylearn.ui.HomeActivity
import com.hompimpa.comfylearn.ui.study.alphabet.AlphabetActivity

class NumberActivity : BaseActivity() {

    private lateinit var nextButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var homeButton: ImageButton
    private lateinit var changeButton: ImageButton
    private var currentLetter: Int = 0 // Default starting letter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alphabet)

        nextButton = findViewById(R.id.nextButton)
        backButton = findViewById(R.id.backButton)
        homeButton = findViewById(R.id.homeButton)
        changeButton = findViewById(R.id.changeButton)

        // Retrieve the letter passed from the intent
        currentLetter = intent.getIntExtra("number", 0)

        loadAlphabetFragment(currentLetter)

        nextButton.setOnClickListener { navigateToLetter(currentLetter + 1) }
        backButton.setOnClickListener { navigateToLetter(currentLetter - 1) }
        homeButton.setOnClickListener { navigateToHome() }
        changeButton.setOnClickListener { navigateToOther() }
    }

    private fun loadAlphabetFragment(letter: Int) {
        supportFragmentManager.commit {
            replace(R.id.fragment_container, NumberFragment.newInstance(letter))
        }
    }

    private fun navigateToLetter(number: Int) {
        if (number in 0..9) {
            currentLetter = number
            loadAlphabetFragment(number)
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish() // Close AlphabetActivity to prevent back stack issues
    }

    private fun navigateToOther() {
        val intent = Intent(this, AlphabetActivity::class.java)
        intent.putExtra("letter", 'A')
        startActivity(intent)
        finish()
    }
}