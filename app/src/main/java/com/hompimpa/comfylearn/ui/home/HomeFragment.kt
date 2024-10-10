package com.hompimpa.comfylearn.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hompimpa.comfylearn.helper.ListButtonAdapter
import com.hompimpa.comfylearn.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var rvHeroes: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Initialize the ViewModel
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        // Inflate the fragment layout
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Setup RecyclerView
        rvHeroes = binding.rvButton
        rvHeroes.layoutManager = GridLayoutManager(requireContext(), 2)
        rvHeroes.setHasFixedSize(true)

        // Observe the ViewModel's data using lambda
        homeViewModel.imageResources.observe(viewLifecycleOwner) { list ->
            rvHeroes.adapter = ListButtonAdapter(list)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
