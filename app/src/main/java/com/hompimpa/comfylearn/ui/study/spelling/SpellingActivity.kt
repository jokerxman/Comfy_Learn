package com.hompimpa.comfylearn.ui.study.spelling

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.hompimpa.comfylearn.HomeActivity
import com.hompimpa.comfylearn.R

class SpellingActivity : AppCompatActivity() {

    private lateinit var nextButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var homeButton: ImageButton
    private var currentSyllable: Int = 0 // Start from the first syllable

    // List of syllables
    private val syllables = listOf(
        "a", "i", "u", "e", "o",
        "ba", "bi", "bu", "be", "bo",
        "ca", "ci", "cu", "ce", "co",
        "da", "di", "du", "de", "do",
        "fa", "fi", "fu", "fe", "fo",
        "ga", "gi", "gu", "ge", "go",
        "ha", "hi", "hu", "he", "ho",
        "ja", "ji", "ju", "je", "jo",
        "ka", "ki", "ku", "ke", "ko",
        "la", "li", "lu", "le", "lo",
        "ma", "mi", "mu", "me", "mo",
        "na", "ni", "nu", "ne", "no",
        "pa", "pi", "pu", "pe", "po",
        "ra", "ri", "ru", "re", "ro",
        "sa", "si", "su", "se", "so",
        "ta", "ti", "tu", "te", "to",
        "va", "vi", "vu", "ve", "vo",
        "wa", "wi", "wu", "we", "wo",
        "xa", "xi", "xu", "xe", "xo",
        "ya", "yi", "yu", "ye", "yo",
        "za", "zi", "zu", "ze", "zo"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spelling)

        nextButton = findViewById(R.id.nextButton)
        backButton = findViewById(R.id.backButton)
        homeButton = findViewById(R.id.homeButton)

        // Retrieve the syllable passed from the intent
        currentSyllable = intent.getIntExtra("syllable_index", 0)

        loadSyllableFragment(currentSyllable)

        nextButton.setOnClickListener { navigateToSyllable(currentSyllable + 1) }
        backButton.setOnClickListener { navigateToSyllable(currentSyllable - 1) }
        homeButton.setOnClickListener { navigateToHome() }
    }

    private fun loadSyllableFragment(syllableIndex: Int) {
        if (syllableIndex in syllables.indices) {
            supportFragmentManager.commit {
                replace(
                    R.id.fragment_container,
                    SpellingFragment.newInstance(syllables[syllableIndex])
                )
            }
        }
    }

    private fun navigateToSyllable(syllableIndex: Int) {
        if (syllableIndex in syllables.indices) {
            currentSyllable = syllableIndex
            loadSyllableFragment(syllableIndex)
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}
