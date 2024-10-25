package com.hompimpa.comfylearn.ui.study

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hompimpa.comfylearn.databinding.FragmentStudyBinding
import com.hompimpa.comfylearn.ui.study.alphabet.AlphabetActivity
import com.hompimpa.comfylearn.ui.study.spelling.SpellingActivity
import com.hompimpa.comfylearn.ui.study.tracing.TracingActivity

class StudyFragment : Fragment() {

    private var _binding: FragmentStudyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudyBinding.inflate(inflater, container, false)

        binding.buttonOpenAlphabet.setOnClickListener {
            val intent = Intent(requireContext(), AlphabetActivity::class.java)
            intent.putExtra("letter", 'A') // Start with letter 'A'
            startActivity(intent)
        }

        binding.buttonOpenTracing.setOnClickListener {
            val intent = Intent(requireContext(), TracingActivity::class.java)
            startActivity(intent)
        }

        binding.buttonOpenSpelling.setOnClickListener {
            val intent = Intent(requireContext(), SpellingActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

