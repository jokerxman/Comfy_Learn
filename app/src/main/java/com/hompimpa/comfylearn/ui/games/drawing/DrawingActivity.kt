package com.hompimpa.comfylearn.ui.games.drawing

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button // Import Button
import androidx.appcompat.app.AppCompatActivity
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.views.Board

class DrawingActivity : AppCompatActivity() {
    private lateinit var boardView: Board
    private lateinit var btnOpenColorPicker: Button // Declare the new button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawing)
        boardView = findViewById(R.id.board)

        val btnClear: Button = findViewById(R.id.btn_clear) // Assuming you have a clear button
        btnClear.setOnClickListener { clearBoard() }

        // --- New Button for Color Picker Dialog ---
        btnOpenColorPicker =
            findViewById(R.id.btn_pick_color) // Make sure this ID exists in your XML
        btnOpenColorPicker.setOnClickListener {
            showColorPickerDialog()
        }

        // Set an initial color for the board if desired
        boardView.setPenColor(Color.BLACK)
    }

    // Helper method to simplify setting color for existing buttons if you keep them
    private fun changeColorById(color: Int) {
        boardView.setPenColor(color)
    }


    // --- Deprecated if you fully switch to the dialog for color changes ---
    // You can remove this 'changeColor(view: View)' if you only use the dialog
    // or the more specific button listeners above.
    /*
    fun changeColor(view: View) { // Called by android:onClick in XML
        val newColor = when (view.id) {
            R.id.btn_red -> Color.RED
            R.id.btn_blue -> Color.BLUE
            R.id.btn_green -> Color.GREEN
            // R.id.btn_black -> Color.BLACK // If you had a black button
            else -> boardView.getPenColor() // Keep current color or default to black
        }
        boardView.setPenColor(newColor)
    }
    */

    private fun showColorPickerDialog() {
        // Get the current color from the board to set as the initial color in the picker
        // Make sure your Board view has a getPenColor() method.
        // If not, you can pass a default like Color.BLACK
        val initialColor = try {
            boardView.getPenColor() // Assuming Board.kt has this method
        } catch (e: Exception) {
            // Log.e("DrawingActivity", "Board has no getPenColor method or other error", e)
            Color.BLACK // Default if getPenColor() is not available or fails
        }

        ColorPickerDialog
            .Builder(this)                        // Pass Activity Instance
            .setTitle("Pick Color")                // Default "Choose Color"
            .setColorShape(ColorShape.SQAURE)    // Default ColorShape.CIRCLE
            .setDefaultColor(initialColor)        // Pass Default Color
            .setColorListener { color, colorHex ->    // Pass Listener
                boardView.setPenColor(color)
            }
            .setDismissListener {
                // Handle dialog dismiss event
                // Log.d("DrawingActivity", "Color Picker Dialog Dismissed")
            }
            .show()
    }


    // Make sure clearBoard() is accessible if btnClear has android:onClick="clearBoard"
    // Or call it directly from the listener like above.
    // fun clearBoard(view: View) { // Keep if using android:onClick
    //    boardView.clear()
    // }
    private fun clearBoard() { // More direct if using setOnClickListener
        boardView.clear()
    }
}