package com.hompimpa.comfylearn.helper

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.ui.study.materi1.materi1Activity

/**
 * Created by JokerManX on 10/9/2024.
 */
class ListButtonAdapter(private val listButtons: List<ImgButtons>) :
    RecyclerView.Adapter<ListButtonAdapter.ButtonViewHolder>() {

    inner class ButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgButton: ImageView = itemView.findViewById(R.id.img_button)
        val cardView: CardView = itemView.findViewById(R.id.card_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ButtonViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_row_button, parent, false)
        return ButtonViewHolder(view)
    }

    override fun onBindViewHolder(holder: ButtonViewHolder, position: Int) {
        val button = listButtons[position]
        holder.imgButton.setImageResource(button.photo)

        // Set OnClickListener for imgButton to launch Materi1Activity
        holder.imgButton.setOnClickListener {
            val context = it.context
            val intent = Intent(context, materi1Activity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = listButtons.size
}
