package com.hompimpa.comfylearn.ui.study.spelling

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.hompimpa.comfylearn.R
import kotlin.random.Random

class SpellingFragment : Fragment() {

    private lateinit var wordTextView: TextView
    private var selectedCategory: String? = null
    private var selectedLetter: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_spelling, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        wordTextView = view.findViewById(R.id.wordTextView)

        // Get the selected category or letter from arguments
        selectedCategory = arguments?.getString(ARG_CATEGORY)
        selectedLetter = arguments?.getString(ARG_LETTER)

        // Load and display words based on the selected category or letter
        if (selectedCategory != null) {
            displayRandomWordForCategory(selectedCategory)
        } else if (selectedLetter != null) {
            displayWordsForLetter(selectedLetter)
        }
    }

    private fun displayRandomWordForCategory(category: String?) {
        val words = when (category) {
            "Animals" -> resources.getStringArray(R.array.animal).toList()
            "Objects" -> resources.getStringArray(R.array.`object`).toList()
            else -> emptyList()
        }

        // Select a random word from the list
        if (words.isNotEmpty()) {
            val randomWord = words[Random.nextInt(words.size)]
            wordTextView.text = randomWord
        } else {
            wordTextView.text = "No words available"
        }
    }

    private fun displayWordsForLetter(letter: String?) {
        // Define the consonants you are using
        val consonants = listOf(
            'B',
            'C',
            'D',
            'F',
            'G',
            'H',
            'J',
            'K',
            'L',
            'M',
            'N',
            'P',
            'Q',
            'R',
            'S',
            'T',
            'V',
            'W',
            'X',
            'Y',
            'Z'
        )

        // Get the index based on the consonant letter
        val index = letter?.firstOrNull()?.uppercaseChar()?.let { consonants.indexOf(it) }

        // Ensure the index is valid
        val words = if (index != null && index in consonants.indices) {
            resources.getStringArray(R.array.spell)[index].split(",") // Split the string into a list
        } else {
            emptyList()
        }

        // Display the words in the TextView
        wordTextView.text = words.joinToString(", ")
    }

    companion object {
        private const val ARG_CATEGORY = "category"
        private const val ARG_LETTER = "letter"

        // Factory method to create a new instance of this fragment for category
        fun newInstanceForCategory(category: String): SpellingFragment {
            val fragment = SpellingFragment()
            val args = Bundle()
            args.putString(ARG_CATEGORY, category)
            fragment.arguments = args
            return fragment
        }

        // Factory method to create a new instance of this fragment for letter
        fun newInstanceForLetter(letter: String): SpellingFragment {
            val fragment = SpellingFragment()
            val args = Bundle()
            args.putString(ARG_LETTER, letter)
            fragment.arguments = args
            return fragment
        }
    }
}