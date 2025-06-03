package com.hompimpa.comfylearn.ui.games

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hompimpa.comfylearn.databinding.FragmentGamesBinding
import com.hompimpa.comfylearn.ui.games.drawing.DrawingActivity
import com.hompimpa.comfylearn.ui.games.fillIn.FillInActivity

class GamesFragment : Fragment() {

    private var _binding: FragmentGamesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Initialize the ViewModel

        // Inflate the fragment layout
        _binding = FragmentGamesBinding.inflate(inflater, container, false)

        binding.buttonOpenGameFill.setOnClickListener {
            val intent = Intent(requireContext(), FillInActivity::class.java)
            intent.putExtra("CATEGORY", "animal")
            startActivity(intent)
        }

        binding.buttonOpenGameDrawing.setOnClickListener {
            val intent = Intent(requireContext(), DrawingActivity::class.java)
            startActivity(intent)
        }

        val root: View = binding.root

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}