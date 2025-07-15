package com.hompimpa.comfylearn.ui.study.number

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.hompimpa.comfylearn.R

class NumberFragment : Fragment() {

    private var currentNumber: Int = 0
    private val numberImages = mapOf(
        0 to R.drawable.number_0,
        1 to R.drawable.number_1,
        2 to R.drawable.number_2,
        3 to R.drawable.number_3,
        4 to R.drawable.number_4,
        5 to R.drawable.number_5,
        6 to R.drawable.number_6,
        7 to R.drawable.number_7,
        8 to R.drawable.number_8,
        9 to R.drawable.number_9
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentNumber = it.getInt(ARG_NUMBER, 0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_alphabet, container, false)
        val numberImageView: ImageView = view.findViewById(R.id.letterImageView)
        updateNumberImage(numberImageView)
        return view
    }

    private fun updateNumberImage(numberImageView: ImageView) {
        val imageRes = numberImages[currentNumber] ?: R.drawable.ic_no_image
        numberImageView.setImageResource(imageRes)
    }

    companion object {
        private const val ARG_NUMBER = "number"

        @JvmStatic
        fun newInstance(number: Int) =
            NumberFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_NUMBER, number)
                }
            }
    }
}
