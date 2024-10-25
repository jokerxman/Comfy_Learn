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