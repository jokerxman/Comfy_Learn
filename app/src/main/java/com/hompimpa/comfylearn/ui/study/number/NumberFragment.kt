package com.hompimpa.comfylearn.ui.study.number

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.ui.Word

class NumberFragment : Fragment() {

    private lateinit var word: Word

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_number, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve the Word object passed as an argument
        word = arguments?.getParcelable("WORD_KEY") ?: return

        // Get the spell array for a specific index
        val spellIndex = 0 // Change this to the index of the spell array you want
        val spellArray = word.getSpellArray(spellIndex)

        // Set up the TextView to display the spell array
        val spellTextView: TextView = view.findViewById(R.id.numberTextView)
        if (spellArray != null) {
            spellTextView.text =
                spellArray.joinToString(", ") // Display the spell array as a comma-separated string
        } else {
            spellTextView.text = "No spell array found for index $spellIndex"
        }

        // Set up the ImageView to display the image
        val imageView: ImageView =
            view.findViewById(R.id.numberImageView) // Make sure this ID matches your layout
        imageView.setImageResource(word.imageResId) // Set the image resource from the Word object
    }

    companion object {
        fun newInstance(number: Int): NumberFragment {
            val fragment = NumberFragment()
            val args = Bundle()
            args.putInt("NUMBER_KEY", number)
            fragment.arguments = args
            return fragment
        }
    }
}