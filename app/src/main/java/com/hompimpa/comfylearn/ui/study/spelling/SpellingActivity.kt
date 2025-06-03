package com.hompimpa.comfylearn.ui.study.spelling

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.helper.CategoryAdapter

class SpellingActivity : AppCompatActivity() {

    private lateinit var homeButton: ImageButton
    private lateinit var recyclerViewCategories: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    private val mainCategories = listOf("Animals", "Objects") // Keep these as defined
    private lateinit var consonantCategories: List<String>
    private lateinit var fragmentContainer: View
    private var isFragmentDisplayed = false

    private val backPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            Log.d(
                "SpellingActivity",
                "Activity's OnBackPressedCallback: handleOnBackPressed. BackStackCount: ${supportFragmentManager.backStackEntryCount}, isFragmentDisplayed: $isFragmentDisplayed"
            )
            if (supportFragmentManager.backStackEntryCount > 0 && isFragmentDisplayed) {
                Log.d("SpellingActivity", "Popping fragment from back stack via callback.")
                supportFragmentManager.popBackStackImmediate()
                showCategoriesView()
                if (supportFragmentManager.backStackEntryCount == 0) {
                    this.isEnabled = false
                }
            } else {
                Log.d(
                    "SpellingActivity",
                    "Callback: No fragment to pop or fragment not displayed, finishing activity."
                )
                this.isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spelling)
        Log.d("SpellingActivity", "onCreate called")

        onBackPressedDispatcher.addCallback(this, backPressedCallback)

        // Ensure consonantCategories is initialized before being used in onCategorySelected
        consonantCategories = resources.getStringArray(R.array.consonants).toList()

        homeButton = findViewById(R.id.homeButton)
        homeButton.setOnClickListener { finish() }
        recyclerViewCategories = findViewById(R.id.recyclerView)
        fragmentContainer = findViewById(R.id.fragment_container)

        recyclerViewCategories.layoutManager = GridLayoutManager(this, 2)
        val combinedCategories = mainCategories + consonantCategories // This is fine
        categoryAdapter = CategoryAdapter(combinedCategories) { selectedCategory ->
            onCategorySelected(selectedCategory)
        }
        recyclerViewCategories.adapter = categoryAdapter
        showCategoriesView()
    }

    private fun onCategorySelected(category: String) {
        Log.d("SpellingActivity", "onCategorySelected: $category")
        showFragmentView()

        val fragment = when {
            mainCategories.contains(category) -> {
                Log.d("SpellingActivity", "Creating fragment for general category: $category")
                // Pass false for isConsonant
                SpellingFragment.newInstance(category, false) // <--- CORRECTED
            }

            consonantCategories.contains(category) -> {
                Log.d("SpellingActivity", "Creating fragment for letter category: $category")
                // Pass true for isConsonant
                SpellingFragment.newInstance(category, true)  // <--- CORRECTED
            }

            else -> {
                Log.w("SpellingActivity", "Unknown category selected: $category")
                showCategoriesView()
                return
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack("SpellingFragmentFor_$category")
            .commit()
    }

    private fun showCategoriesView() {
        Log.d("SpellingActivity", "showCategoriesView called")
        recyclerViewCategories.visibility = View.VISIBLE
        fragmentContainer.visibility = View.GONE
        isFragmentDisplayed = false
        backPressedCallback.isEnabled = false
        Log.d(
            "SpellingActivity",
            "Categories Visible: ${recyclerViewCategories.visibility == View.VISIBLE}, Fragment Container Visible: ${fragmentContainer.visibility == View.VISIBLE}, backPressedCallback enabled: ${backPressedCallback.isEnabled}"
        )
    }

    private fun showFragmentView() {
        Log.d("SpellingActivity", "showFragmentView called")
        recyclerViewCategories.visibility = View.GONE
        fragmentContainer.visibility = View.VISIBLE
        isFragmentDisplayed = true
        backPressedCallback.isEnabled = true
        Log.d(
            "SpellingActivity",
            "Categories Visible: ${recyclerViewCategories.visibility == View.VISIBLE}, Fragment Container Visible: ${fragmentContainer.visibility == View.VISIBLE}, backPressedCallback enabled: ${backPressedCallback.isEnabled}"
        )
    }
}