package com.hompimpa.comfylearn.ui.study.spelling

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.databinding.FragmentSpellingBinding
import com.hompimpa.comfylearn.helper.AppConstants
import com.hompimpa.comfylearn.helper.GameContentProvider
import com.hompimpa.comfylearn.helper.SoundManager
import java.util.Locale

class SpellingFragment : Fragment() {

    private var _binding: FragmentSpellingBinding? = null
    private val binding get() = _binding!!

    private var currentCategoryName: String? = null
    private var isConsonantCategory: Boolean = false

    companion object {
        private const val DISPLAY_TYPE_SYLLABLES = 2
        private const val DISPLAY_TYPE_WORD = 1

        fun newInstance(categoryName: String, isConsonant: Boolean): SpellingFragment {
            return SpellingFragment().apply {
                arguments = Bundle().apply {
                    putString("category_name", categoryName)
                    putBoolean("is_consonant_category", isConsonant)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentCategoryName = it.getString("category_name")
            isConsonantCategory = it.getBoolean("is_consonant_category", false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSpellingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadAndDisplayCategory()
        currentCategoryName?.let { saveSpellingProgress(it) }
    }

    private fun saveSpellingProgress(categoryName: String) {
        activity?.getSharedPreferences(AppConstants.PREFS_PROGRESSION, Context.MODE_PRIVATE)?.edit {
            putBoolean(AppConstants.getSpellingCategoryProgressKey(categoryName), true)
        }
    }

    private fun loadAndDisplayCategory() {
        val category = currentCategoryName ?: return displayError("No category selected.")
        resetUI()
        binding.nextWordButton.setOnClickListener { loadAndDisplayCategory() }

        if (isConsonantCategory) {
            displayConsonantCategory(category)
        } else {
            displayWordCategory(category)
        }
    }

    private fun displayConsonantCategory(category: String) {
        binding.nextWordButton.visibility = View.GONE
        val syllables = getItemsForConsonantSyllables(category)
        if (syllables.isNotEmpty()) {
            displayImage(category, category)
            setupItemTextViews(syllables, DISPLAY_TYPE_SYLLABLES)
        } else {
            displayError("No syllables found for '$category'.")
        }
    }

    private fun displayWordCategory(category: String) {
        binding.nextWordButton.visibility = View.VISIBLE
        val displayWords = GameContentProvider.getWordsForCategory(requireContext(), category)
        if (displayWords.isNotEmpty()) {
            val randomWord = displayWords.random()
            val englishWord = getEnglishEquivalent(randomWord, category)

            if (englishWord != null) {
                displayImage(englishWord, category)
                setupItemTextViews(listOf(randomWord), DISPLAY_TYPE_WORD)
            } else {
                displayError("Could not find image for '$randomWord'.")
            }
        } else {
            displayError("No items found for '$category'.")
        }
    }

    private fun setupItemTextViews(items: List<String>, displayType: Int) {
        binding.syllableContainer.removeAllViews()
        items.forEach { itemText ->
            val textView = TextView(requireContext()).apply {
                text = itemText
                textSize = if (displayType == DISPLAY_TYPE_SYLLABLES) 24f else 30f
                gravity = Gravity.CENTER
                setBackgroundResource(R.drawable.syllable_background)
                setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
                setOnClickListener { SoundManager.playSoundByName(requireContext(), itemText) }
            }
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                val margin = if (displayType == DISPLAY_TYPE_SYLLABLES && items.size > 1) dpToPx(4) else 0
                setMargins(margin, dpToPx(8), margin, 0)
            }
            binding.syllableContainer.addView(textView, layoutParams)
        }
        binding.syllableContainer.visibility = View.VISIBLE
    }

    private fun getEnglishEquivalent(localizedWord: String, categoryName: String): String? {
        val resId = resources.getIdentifier(categoryName.lowercase(), "array", requireContext().packageName)
        if (resId == 0) return null

        val localizedArray = resources.getStringArray(resId)
        val wordIndex = localizedArray.indexOfFirst { it.equals(localizedWord, ignoreCase = true) }
        if (wordIndex == -1) return null

        val englishConfig = Configuration(resources.configuration).apply { setLocale(Locale.ENGLISH) }
        val englishContext = requireContext().createConfigurationContext(englishConfig)
        val englishArray = englishContext.resources.getStringArray(resId)

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
            } else { emptyList() }
        } catch (_: Exception) { emptyList() }
    }

    private fun resetUI() {
        binding.itemImageView.visibility = View.GONE
        binding.syllableContainer.visibility = View.GONE
        binding.errorTextView.visibility = View.GONE
        binding.nextWordButton.visibility = View.GONE
        binding.syllableContainer.removeAllViews()
    }

    private fun displayError(message: String) {
        resetUI()
        binding.errorTextView.text = message
        binding.errorTextView.visibility = View.VISIBLE
    }

    private fun displayImage(imageName: String, categoryName: String) {
        val imagePath = GameContentProvider.getImagePath(categoryName.lowercase(), imageName)
        val drawable = GameContentProvider.loadSvgFromAssets(requireContext(), imagePath)

        if (drawable != null) {
            binding.itemImageView.setImageDrawable(drawable)
            binding.itemImageView.visibility = View.VISIBLE
        } else {
            binding.itemImageView.visibility = View.GONE
        }
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics
        ).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
