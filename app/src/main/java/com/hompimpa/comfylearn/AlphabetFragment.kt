package com.hompimpa.comfylearn

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hompimpa.comfylearn.databinding.ItemAlphabetBinding

class AlphabetFragment : Fragment() {

    private var _binding: ItemAlphabetBinding? = null
    private val binding get() = _binding!!
    private var letter: Char? = null

    companion object {
        fun newInstance(letter: Char): AlphabetFragment {
            val fragment = AlphabetFragment()
            val args = Bundle()
            args.putChar("letter", letter)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = ItemAlphabetBinding.inflate(inflater, container, false)
        letter = arguments?.getChar("letter")

        // Set the letter and corresponding image
        binding.letterTextView.text = letter.toString()
        binding.letterImageView.setImageResource(getLetterImage(letter))

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getLetterImage(letter: Char?): Int {
        return when (letter) {
            'A' -> R.drawable.apple // Replace with your actual image resources
            'B' -> R.drawable.ball
            'C' -> R.drawable.cat
            else -> R.drawable.ic_no_image
        }
    }
}
