package com.hompimpa.comfylearn

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class AlphabetActivity : AppCompatActivity() {

    private val letterImages = mapOf(
        'A' to R.drawable.huruf_a,
        'B' to R.drawable.huruf_b,
        'C' to R.drawable.huruf_c,
        'D' to R.drawable.huruf_d,
        'E' to R.drawable.huruf_e,
        'F' to R.drawable.huruf_f,
        'G' to R.drawable.huruf_g,
        'H' to R.drawable.huruf_h,
        'I' to R.drawable.huruf_i,
        'J' to R.drawable.huruf_j,
        'K' to R.drawable.huruf_k,
        'L' to R.drawable.huruf_l,
        'M' to R.drawable.huruf_m,
        'N' to R.drawable.huruf_n,
        'O' to R.drawable.huruf_o,
        'P' to R.drawable.huruf_p,
        'Q' to R.drawable.huruf_q,
        'R' to R.drawable.huruf_r,
        'S' to R.drawable.huruf_s,
        'T' to R.drawable.huruf_t,
        'U' to R.drawable.huruf_u,
        'V' to R.drawable.huruf_v,
        'W' to R.drawable.huruf_w,
        'X' to R.drawable.huruf_x,
        'Y' to R.drawable.huruf_y,
        'Z' to R.drawable.huruf_z
    )

    private lateinit var letterImageView: ImageView
    private lateinit var nextButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var homeButton: ImageButton

    private var currentLetter: Char = 'A' // Default starting letter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alphabet)

        letterImageView = findViewById(R.id.letterImageView)
        nextButton = findViewById(R.id.nextButton)
        backButton = findViewById(R.id.backButton)
        homeButton = findViewById(R.id.homeButton)

        // Retrieve the letter passed from the intent
        currentLetter = intent.getCharExtra("letter", 'A')

        updateLetterImage()

        nextButton.setOnClickListener { navigateToLetter(currentLetter + 1) }
        backButton.setOnClickListener { navigateToLetter(currentLetter - 1) }
        homeButton.setOnClickListener { navigateToHome() }
    }

    private fun updateLetterImage() {
        val imageRes = letterImages[currentLetter] ?: R.drawable.ic_no_image
        letterImageView.setImageResource(imageRes)

        // Disable the back button if on 'A' and next button if on 'Z'
        backButton.isEnabled = currentLetter > 'A'
        nextButton.isEnabled = currentLetter < 'Z'
    }

    private fun navigateToLetter(letter: Char) {
        if (letter in 'A'..'Z') {
            val intent = Intent(this, AlphabetActivity::class.java)
            intent.putExtra("letter", letter)
            startActivity(intent)
            finish() // Close current activity to prevent back stack build-up
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish() // Close AlphabetActivity to prevent back stack issues
    }
}
