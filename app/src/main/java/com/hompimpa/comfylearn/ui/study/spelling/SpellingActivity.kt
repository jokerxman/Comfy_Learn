package com.hompimpa.comfylearn.ui.study.spelling

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hompimpa.comfylearn.R

class SpellingActivity : AppCompatActivity() {

    private lateinit var homeButton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    private val mainCategories = listOf("Animals", "Objects") // Main categories
    private lateinit var consonantCategories: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spelling)

        // Load consonant categories from strings.xml
        consonantCategories = resources.getStringArray(R.array.consonants).toList()

        homeButton = findViewById(R.id.homeButton)
        homeButton.setOnClickListener {
            finish() // Close the activity or navigate to home
        }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        // Combine main categories and consonants into one list for the adapter
        val combinedCategories = mainCategories + consonantCategories

        categoryAdapter = CategoryAdapter(combinedCategories) { selectedCategory ->
            onCategorySelected(selectedCategory)
        }
        recyclerView.adapter = categoryAdapter
    }

    private fun onCategorySelected(category: String) {
        // Hide the RecyclerView
        recyclerView.visibility = View.GONE

        // Create a new instance of SpellingFragment based on the selected category
        val fragment = when {
            mainCategories.contains(category) -> {
                // Handle main categories
                SpellingFragment.newInstanceForCategory(category)
            }

            consonantCategories.contains(category) -> {
                // Handle consonant categories
                SpellingFragment.newInstanceForLetter(category)
            }

            else -> {
                // Handle unknown category (optional)
                return
            }
        }

        // Replace the current fragment with the new fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onBackPressed() {
        if (recyclerView.visibility != View.VISIBLE) {
            recyclerView.visibility = View.VISIBLE
        }
        super.onBackPressed()
    }
}