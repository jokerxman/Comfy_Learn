package com.hompimpa.comfylearn.helper

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hompimpa.comfylearn.databinding.ItemLetterOptionBinding

// Create RecyclerView Adapter
class LetterOptionsAdapter(
    private val letters: List<String>, // List of letters (strings)
    private val onLetterClick: (String) -> Unit // Function to handle click
) : RecyclerView.Adapter<LetterOptionsAdapter.LetterViewHolder>() {

    // Inflate the layout for each item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LetterViewHolder {
        val binding = ItemLetterOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LetterViewHolder(binding)
    }

    // Bind data to the ViewHolder
    override fun onBindViewHolder(holder: LetterViewHolder, position: Int) {
        val letter = letters[position]
        holder.binding.letterText.text = letter
        holder.binding.letterText.setOnClickListener {
            // Call the provided click function when a letter is clicked
            onLetterClick(letter)
        }
    }

    // Return the total number of items in the list
    override fun getItemCount(): Int = letters.size

    // ViewHolder class to hold references to the views in the layout
    class LetterViewHolder(val binding: ItemLetterOptionBinding) : RecyclerView.ViewHolder(binding.root)
}
