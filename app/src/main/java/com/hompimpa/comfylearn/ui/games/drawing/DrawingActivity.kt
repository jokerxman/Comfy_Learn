package com.hompimpa.comfylearn.ui.games.drawing

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.views.Board

class DrawingActivity : AppCompatActivity() {
    private lateinit var boardView: Board

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawing)
        boardView = findViewById(R.id.board)
    }

    fun changeColor(view: View) {
        val newColor = when (view.id) {
            R.id.btn_red -> Color.RED
            R.id.btn_blue -> Color.BLUE
            R.id.btn_green -> Color.GREEN
            else -> Color.BLACK
        }
        boardView.setPenColor(newColor)
    }

    fun clearBoard(view: View) {
        boardView.clear()
    }

}