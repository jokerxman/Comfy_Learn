package com.hompimpa.comfylearn.ui

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.caverock.androidsvg.SVG
import com.hompimpa.comfylearn.R
import java.io.InputStream

class ParseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parse)

        // Display a random word from the specified category
        displayWord("object_array", context = this)
    }

    private fun getRandomWordFromCategory(category: String?, context: Context): String? {
        val array = when (category) {
            "spell_array" -> context.resources.getStringArray(R.array.spell)
            "object_array" -> context.resources.getStringArray(R.array.objek)
            // Add more cases for other categories
            else -> null
        }

        return array?.randomOrNull() // Get a random element
    }

    private fun displayWord(category: String?, context: Context) {
        val linearLayout =
            findViewById<LinearLayout>(R.id.linearLayout) // Your LinearLayout in the XML

        // Clear any previous views
        linearLayout.removeAllViews()

        // Get a random word from the specified category
        val randomWord = getRandomWordFromCategory(category, context)

        // Check if a random word was found
        if (randomWord != null) {
            // Create a horizontal LinearLayout to hold the letters
            val wordLayout = LinearLayout(this)
            wordLayout.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            wordLayout.gravity = Gravity.CENTER // Center the word layout

            // Iterate through each character in the random word
            for (char in randomWord) {
                if (char == ' ') {

                }
                // Create an ImageView for the letter
                val imageView = createImageViewForLetter(char)

                // Set layout parameters to align the image to the bottom
                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                layoutParams.gravity = Gravity.BOTTOM // Align to bottom
                imageView.layoutParams = layoutParams

                // Add the ImageView to the word layout
                wordLayout.addView(imageView)
            }

            // Add the word layout to the main linear layout
            linearLayout.addView(wordLayout)
        } else {
            // Handle the case where no word was found (optional)
            val errorTextView = createTextView("No words found for category: $category")
            linearLayout.addView(errorTextView)
        }
    }

    private fun createImageViewForLetter(letter: Char): ImageView {
        val imageView = ImageView(this)

        // Load the SVG for the letter
        val svgFileName =
            "${letter.lowercase()}.svg" // Assuming SVG files are named a.svg, b.svg, etc.
        val svg = loadSvgFromAssets(svgFileName)

        // Set the SVG as the image source
        svg?.let {
            val pictureDrawable = PictureDrawable(it.renderToPicture())
            imageView.setImageDrawable(pictureDrawable)
        }

        // Set layout parameters to ensure vertical stacking
        imageView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        // Optional: Set padding
        imageView.setPadding(0, 0, 0, 0) // Adjust padding as needed

        return imageView
    }


    private fun loadSvgFromAssets(fileName: String): SVG? {
        return try {
            val inputStream: InputStream = assets.open(fileName)
            SVG.getFromInputStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun createTextView(text: String): TextView {
        val textView = TextView(this)
        textView.text = text

        textView.typeface = getCustomFont()

        // Set layout parameters
        textView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        // Optional: Set padding
        textView.setPadding(16, 16, 16, 16) // Adjust padding as needed

        return textView
    }

    private fun getCustomFont(): Typeface? {
        return ResourcesCompat.getFont(this, R.font.mochiypopone) // Load the font from res/font
    }
}