package com.hompimpa.comfylearn.helper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hompimpa.comfylearn.R // Your R file

class LetterOptionsAdapter(
    private val letters: List<String>, // Expects a List of Strings
    private val onLetterClick: (String) -> Unit // Expects a function that takes a String and returns Unit
) : RecyclerView.Adapter<LetterOptionsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val letterTextView: TextView = itemView.findViewById(R.id.letterTextView) // ID from your item_letter_option.xml

        fun bind(letter: String) {
            letterTextView.text = letter
            itemView.setOnClickListener {
                onLetterClick(letter)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_letter_option, parent, false) // Ensure you have this layout
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(letters[position])
    }

    override fun getItemCount(): Int = letters.size
}