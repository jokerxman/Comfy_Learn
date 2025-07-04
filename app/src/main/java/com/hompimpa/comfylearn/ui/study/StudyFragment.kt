package com.hompimpa.comfylearn.ui.study

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.databinding.FragmentStudyBinding
import com.hompimpa.comfylearn.ui.study.alphabet.AlphabetActivity
import com.hompimpa.comfylearn.ui.study.arithmetic.ArithmeticActivity
import com.hompimpa.comfylearn.ui.study.spelling.SpellingActivity

class StudyFragment : Fragment() {

    private var _binding: FragmentStudyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonOpenAlphabet.setOnClickListener {
            val intent = Intent(requireContext(), AlphabetActivity::class.java)
            intent.putExtra("letter", 'A') // Start with letter 'A'
            startActivity(intent)
        }

        binding.buttonOpenSpelling.setOnClickListener {
            val intent = Intent(requireContext(), SpellingActivity::class.java)
            startActivity(intent)
        }
        binding.buttonOpenArithmetic.setOnClickListener {
            val intent = Intent(requireContext(), ArithmeticActivity::class.java)
            startActivity(intent)
        }

        // Add the scroll indicator logic
        setupScrollIndicator()
    }

    private fun setupScrollIndicator() {
        val scrollView = binding.scrollView
        val scrollIndicator = binding.scrollIndicator
        val contentLayout = scrollView.getChildAt(0)

        contentLayout.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                contentLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)

                if (contentLayout.height > scrollView.height) {
                    scrollIndicator.visibility = View.VISIBLE
                    val bounceAnimation = AnimationUtils.loadAnimation(context, R.anim.bounce)
                    scrollIndicator.startAnimation(bounceAnimation)
                } else {
                    scrollIndicator.visibility = View.GONE
                }
            }
        })

        scrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            if (scrollY > 0 && scrollIndicator.visibility == View.VISIBLE) {
                scrollIndicator.animate().alpha(0f).setDuration(300).withEndAction {
                    scrollIndicator.visibility = View.GONE
                }.start()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
