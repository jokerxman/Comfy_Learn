package com.hompimpa.comfylearn.helper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.hompimpa.comfylearn.R

class LetterOptionsAdapter(
    private val letters: List<String>,
    private val onLetterClick: (String) -> Unit
) : RecyclerView.Adapter<LetterOptionsAdapter.ViewHolder>() {

    private var itemSize: Int = 0

    /**
     * This new function allows the Activity to tell the adapter the correct size for each item.
     * It will trigger a redraw of the list to apply the new sizes.
     */
    fun updateItemSize(size: Int) {
        if (itemSize != size) {
            itemSize = size
            notifyDataSetChanged()
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val letterTextView: TextView = itemView.findViewById(R.id.letterTextView)

        fun bind(letter: String) {
            // Apply the calculated size if it has been set
            if (itemSize > 0) {
                val params = itemView.layoutParams
                params.width = itemSize
                params.height = itemSize
                itemView.layoutParams = params
            }

            letterTextView.text = letter
            // Ensure text auto-sizes to fit within the tile
            TextViewCompat.setAutoSizeTextTypeWithDefaults(letterTextView, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM)

            itemView.setOnClickListener {
                onLetterClick(letter)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_letter_option, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(letters[position])
    }

    override fun getItemCount(): Int = letters.size
}