package com.hompimpa.comfylearn.helper // Adjust package to your project structure

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hompimpa.comfylearn.R // Make sure this R import is correct for your project

class AlphabetCharacterAdapter(
    private val characters: List<String>,
    private val onItemClick: (String) -> Unit // Lambda to handle item clicks
) : RecyclerView.Adapter<AlphabetCharacterAdapter.ViewHolder>() {

    // ViewHolder holds the views for each item
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Assuming your item_alphabet_character.xml has a TextView with this ID
        val characterTextView: TextView = itemView.findViewById(R.id.characterTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate your item_alphabet_character.xml layout
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alphabet_character, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val character = characters[position]
        holder.characterTextView.text = character // Set the letter text

        // Set the click listener for the whole item view
        holder.itemView.setOnClickListener {
            onItemClick(character) // Execute the lambda passed from SpellingActivity
        }
    }

    override fun getItemCount(): Int {
        return characters.size // Return the total number of alphabet characters
    }
}