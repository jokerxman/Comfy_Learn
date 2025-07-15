package com.hompimpa.comfylearn.ui.study.spelling

import android.content.Context.MODE_PRIVATE
import android.content.res.Configuration
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button // Import Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGParseException
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.helper.AppConstants
import com.hompimpa.comfylearn.helper.SoundManager
import java.io.IOException
import java.io.InputStream
import java.util.Locale

class SpellingFragment : Fragment() {

    private lateinit var itemImageView: ImageView
    private lateinit var itemsContainer: LinearLayout
    private lateinit var errorTextView: TextView
    private lateinit var nextWordButton: Button // 1. Declare the button

    private var currentCategoryName: String? = null
    private var isConsonantCategory: Boolean = false

    companion object {
        private const val TAG = "SpellingFragment"
        private const val ARG_CATEGORY_NAME = "category_name"
        private const val ARG_IS_CONSONANT_CATEGORY = "is_consonant_category"

        private const val DISPLAY_TYPE_IMAGE_WITH_WORD = 1
        private const val DISPLAY_TYPE_SYLLABLES = 2

        fun newInstance(categoryName: String, isConsonant: Boolean): SpellingFragment {
            return SpellingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CATEGORY_NAME, categoryName)
                    putBoolean(ARG_IS_CONSONANT_CATEGORY, isConsonant)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentCategoryName = it.getString(ARG_CATEGORY_NAME)
            isConsonantCategory = it.getBoolean(ARG_IS_CONSONANT_CATEGORY, false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_spelling, container, false)
        itemImageView = view.findViewById(R.id.itemImageView)
        itemsContainer = view.findViewById(R.id.syllableContainer)
        errorTextView = view.findViewById(R.id.errorTextView)
        nextWordButton = view.findViewById(R.id.nextWordButton) // 2. Initialize the button
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadAndDisplayCategory()
        currentCategoryName?.let { saveSpellingProgress(it) }
    }

    private fun saveSpellingProgress(categoryName: String) {
        activity?.getSharedPreferences(AppConstants.PREFS_PROGRESSION, MODE_PRIVATE)?.edit()
            ?.putBoolean(AppConstants.getSpellingCategoryProgressKey(categoryName), true)
            ?.apply()
    }

    private fun loadAndDisplayCategory() {
        val category = currentCategoryName ?: run {
            displayError("No category selected.")
            return
        }
        resetUI()

        // 3. Set the click listener to simply re-run this method
        nextWordButton.setOnClickListener {
            loadAndDisplayCategory()
        }

        if (isConsonantCategory) {
            nextWordButton.visibility = View.GONE // 4. Hide button for syllables
            val syllables = getItemsForConsonantSyllables(category)
            if (syllables.isNotEmpty()) {
                displayImageFromAssets(category, category)
                setupItemTextViews(syllables, DISPLAY_TYPE_SYLLABLES)
            } else {
                displayError("No syllables found for '$category'.")
            }
        } else {
            nextWordButton.visibility = View.VISIBLE // 4. Show button for words
            val displayWords = getItemsForGeneralCategory(category)
            if (displayWords.isNotEmpty()) {
                val randomWord = displayWords.random()
                val englishWord = getEnglishEquivalent(randomWord, category)

                if(englishWord != null){
                    displayImageFromAssets(englishWord, category)
                    setupItemTextViews(listOf(randomWord), DISPLAY_TYPE_IMAGE_WITH_WORD)
                } else {
                    displayError("Could not map '$randomWord' to an English image name.")
                }
            } else {
                displayError("No items found for '$category'.")
            }
        }
    }

    private fun setupItemTextViews(items: List<String>, displayType: Int) {
        itemsContainer.removeAllViews()
        items.forEach { itemText ->
            val textView = TextView(requireContext()).apply {
                text = itemText
                textSize = if (displayType == DISPLAY_TYPE_SYLLABLES) 24f else 30f
                gravity = Gravity.CENTER
                isClickable = true
                isFocusable = true
                setBackgroundResource(R.drawable.syllable_background)
                setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
                setOnClickListener {
                    SoundManager.playSoundByName(requireContext(), itemText)
                }
            }
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                val margin = if (displayType == DISPLAY_TYPE_SYLLABLES && items.size > 1) dpToPx(4) else 0
                setMargins(margin, dpToPx(8), margin, 0)
            }
            itemsContainer.addView(textView, layoutParams)
        }
        itemsContainer.visibility = View.VISIBLE
    }

    private fun getCategoryResourceId(categoryName: String): Int {
        return when (categoryName.lowercase(Locale.ROOT)) {
            "animal" -> R.array.animal
            "objek" -> R.array.objek
            else -> 0
        }
    }

    private fun getItemsForGeneralCategory(categoryName: String): List<String> {
        val resourceId = getCategoryResourceId(categoryName)
        if (resourceId == 0) return emptyList()
        return resources.getStringArray(resourceId).toList()
    }

    private fun getEnglishEquivalent(localizedWord: String, categoryName: String): String? {
        val resourceId = getCategoryResourceId(categoryName)
        if (resourceId == 0) return null

        val localizedArray = resources.getStringArray(resourceId)
        val wordIndex = localizedArray.indexOf(localizedWord)

        if (wordIndex == -1) return null

        val englishConfig = Configuration(resources.configuration).apply {
            setLocale(Locale.ENGLISH)
        }
        val englishResources = requireContext().createConfigurationContext(englishConfig).resources
        val englishArray = englishResources.getStringArray(resourceId)

        return if (wordIndex < englishArray.size) englishArray[wordIndex] else null
    }

    private fun getItemsForConsonantSyllables(consonant: String): List<String> {
        val consonantLower = consonant.lowercase(Locale.ROOT)
        val consonantOrder = listOf("b", "c", "d", "f", "g", "h", "j", "k", "l", "m", "n", "p", "q", "r", "s", "t", "v", "w", "x", "y", "z")
        val index = consonantOrder.indexOf(consonantLower)

        if (index == -1) return emptyList()

        return try {
            val spellArray = resources.getStringArray(R.array.spell)
            if (index < spellArray.size) {
                spellArray[index].split(",").map { it.trim() }.filter { it.isNotEmpty() }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error accessing spell array for consonant '$consonantLower'", e)
            emptyList()
        }
    }

    private fun resetUI() {
        itemImageView.visibility = View.GONE
        itemsContainer.visibility = View.GONE
        errorTextView.visibility = View.GONE
        itemsContainer.removeAllViews()
    }

    private fun displayError(message: String) {
        resetUI()
        errorTextView.text = message
        errorTextView.visibility = View.VISIBLE
        Log.e(TAG, "Displaying error: $message")
    }

    private fun displayImageFromAssets(imageName: String, categoryName: String): Boolean {
        if (imageName.isBlank()) return false

        val normalizedImageName = imageName.lowercase(Locale.ROOT).replace(" ", "_")
        val imagePath = "en/${categoryName}_${normalizedImageName}.svg"
        Log.d(TAG, "Attempting to load image from assets: $imagePath")

        try {
            val inputStream: InputStream = requireContext().assets.open(imagePath)
            val svg: SVG = SVG.getFromInputStream(inputStream)
            inputStream.close()

            if (svg.documentWidth != -1f) {
                itemImageView.setImageDrawable(PictureDrawable(svg.renderToPicture()))
                itemImageView.visibility = View.VISIBLE
                return true
            }
        } catch (e: Exception) {
            when (e) {
                is IOException, is SVGParseException -> Log.e(TAG, "Error loading SVG '$imagePath': ${e.message}")
                else -> throw e
            }
        }
        return false
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }
}