package com.hompimpa.comfylearn.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hompimpa.comfylearn.helper.ListButtonAdapter
import com.hompimpa.comfylearn.databinding.FragmentGalleryBinding

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    private lateinit var galleryViewModel: GalleryViewModel
    private lateinit var rvHeroes: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Initialize the ViewModel
        galleryViewModel = ViewModelProvider(this)[GalleryViewModel::class.java]

        // Inflate the fragment layout
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Setup RecyclerView
        rvHeroes = binding.rvButton
        rvHeroes.layoutManager = GridLayoutManager(requireContext(), 2)
        rvHeroes.setHasFixedSize(true)

        // Observe the ViewModel's data using lambda
        galleryViewModel.imageResources.observe(viewLifecycleOwner) { list ->
            rvHeroes.adapter = ListButtonAdapter(list)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}