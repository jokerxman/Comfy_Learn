package com.hompimpa.comfylearn.ui.study.spelling

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.helper.BaseActivity
import com.hompimpa.comfylearn.helper.CategoryAdapter
import com.hompimpa.comfylearn.helper.GameContentProvider

class SpellingActivity : BaseActivity() {

    private lateinit var homeButton: ImageButton
    private lateinit var recyclerViewCategories: RecyclerView
    private lateinit var fragmentContainer: View

    private val mainCategories by lazy { GameContentProvider.getGameCategories(this) }
    private val consonantCategories by lazy { resources.getStringArray(R.array.consonants).toList() }

    private val backPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            supportFragmentManager.popBackStack()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spelling)
        onBackPressedDispatcher.addCallback(this, backPressedCallback)

        homeButton = findViewById(R.id.homeButton)
        recyclerViewCategories = findViewById(R.id.recyclerView)
        fragmentContainer = findViewById(R.id.fragment_container)

        homeButton.setOnClickListener { finish() }

        setupRecyclerView()
        setupFragmentListener()
        showCategoriesView()
    }

    private fun setupRecyclerView() {
        recyclerViewCategories.layoutManager = GridLayoutManager(this, 2)
        val combinedCategories = mainCategories + consonantCategories
        recyclerViewCategories.adapter = CategoryAdapter(combinedCategories) { category ->
            onCategorySelected(category)
        }
    }

    private fun setupFragmentListener() {
        supportFragmentManager.addOnBackStackChangedListener {
            val isFragmentVisible = supportFragmentManager.backStackEntryCount > 0
            if (isFragmentVisible) showFragmentView() else showCategoriesView()
        }
    }

    private fun onCategorySelected(category: String) {
        val isConsonant = !mainCategories.contains(category)
        val fragment = SpellingFragment.newInstance(category, isConsonant)

        supportFragmentManager.commit {
            replace(R.id.fragment_container, fragment)
            addToBackStack(null)
        }
    }

    private fun showCategoriesView() {
        recyclerViewCategories.isVisible = true
        fragmentContainer.isVisible = false
        backPressedCallback.isEnabled = false
    }

    private fun showFragmentView() {
        recyclerViewCategories.isVisible = false
        fragmentContainer.isVisible = true
        backPressedCallback.isEnabled = true
    }
}
