package com.hompimpa.comfylearn.ui.study.alphabet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.hompimpa.comfylearn.R

class AlphabetFragment : Fragment() {

    private var currentLetter: Char = 'A'
    private val letterImages = mapOf(
        'A' to R.drawable.letter_a,
        'B' to R.drawable.letter_b,
        'C' to R.drawable.letter_c,
        'D' to R.drawable.letter_d,
        'E' to R.drawable.letter_e,
        'F' to R.drawable.letter_f,
        'G' to R.drawable.letter_g,
        'H' to R.drawable.letter_h,
        'I' to R.drawable.letter_i,
        'J' to R.drawable.letter_j,
        'K' to R.drawable.letter_k,
        'L' to R.drawable.letter_l,
        'M' to R.drawable.letter_m,
        'N' to R.drawable.letter_n,
        'O' to R.drawable.letter_o,
        'P' to R.drawable.letter_p,
        'Q' to R.drawable.letter_q,
        'R' to R.drawable.letter_r,
        'S' to R.drawable.letter_s,
        'T' to R.drawable.letter_t,
        'U' to R.drawable.letter_u,
        'V' to R.drawable.letter_v,
        'W' to R.drawable.letter_w,
        'X' to R.drawable.letter_x,
        'Y' to R.drawable.letter_y,
        'Z' to R.drawable.letter_z
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentLetter = it.getChar(ARG_LETTER, 'A')
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_alphabet, container, false)
        val letterImageView: ImageView = view.findViewById(R.id.letterImageView)
        updateLetterImage(letterImageView)
        return view
    }

    private fun updateLetterImage(letterImageView: ImageView) {
        val imageRes = letterImages[currentLetter] ?: R.drawable.ic_no_image
        letterImageView.setImageResource(imageRes)
    }

    companion object {
        private const val ARG_LETTER = "letter"

        @JvmStatic
        fun newInstance(letter: Char) =
            AlphabetFragment().apply {
                arguments = Bundle().apply {
                    putChar(ARG_LETTER, letter)
                }
            }
    }
}