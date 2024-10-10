package com.hompimpa.comfylearn.helper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.hompimpa.comfylearn.R

/**
 * Created by JokerManX on 10/9/2024.
 */
class ListButtonAdapter(private val listImgButtons: List<ImgButtons>) :
    RecyclerView.Adapter<ListButtonAdapter.ListViewHolder>() {
    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgPhoto: ImageView = itemView.findViewById(R.id.img_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_row_button, parent, false)
        return ListViewHolder(view)
    }

    override fun getItemCount(): Int = listImgButtons.size


    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val (photo) = listImgButtons[position]
        holder.imgPhoto.setImageResource(photo)
    }


}