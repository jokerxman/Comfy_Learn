package com.hompimpa.comfylearn.ui.study.spelling

import android.content.Context.MODE_PRIVATE
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGParseException
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.helper.AppConstants
import java.io.IOException
import java.io.InputStream
import java.util.Locale

class SpellingFragment : Fragment() {

    private lateinit var itemImageView: ImageView
    private lateinit var itemsContainer: LinearLayout
    private lateinit var errorTextView: TextView

    private var currentCategoryName: String? = null
    private var isConsonantCategory: Boolean = false
    private var soundPool: SoundPool? = null

    private val soundIdMap = HashMap<String, Int>()
    private val soundsCurrentlyLoading = mutableListOf<String>()

    companion object {
        private const val TAG = "SpellingFragment" // For logging
        private const val ARG_CATEGORY_NAME = "category_name"
        private const val ARG_IS_CONSONANT_CATEGORY = "is_consonant_category"

        const val DISPLAY_TYPE_IMAGE_WITH_WORD =
            1 // For animals, objects (shows image + full word text)
        const val DISPLAY_TYPE_SYLLABLES =
            2       // For consonant groups (shows image + syllable texts)

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
            Log.d(
                TAG,
                "onCreate: Category='$currentCategoryName', IsConsonant=$isConsonantCategory"
            )
        }
        setupSoundPool()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_spelling, container, false)
        itemImageView = view.findViewById(R.id.itemImageView)
        itemsContainer =
            view.findViewById(R.id.syllableContainer) // Ensure this ID matches your XML
        errorTextView = view.findViewById(R.id.errorTextView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadAndDisplayCategory()

        currentCategoryName?.let { category ->
            if (category.isNotBlank()) { // Ensure category name is valid
                saveSpellingProgress(category)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: Releasing SoundPool.")
        soundPool?.release()
        soundPool = null
        soundIdMap.clear()
        soundsCurrentlyLoading.clear()
    }

    private fun saveSpellingProgress(categoryName: String) {
        activity?.getSharedPreferences(AppConstants.PREFS_PROGRESSION, MODE_PRIVATE)?.edit()
            ?.putBoolean(AppConstants.getSpellingCategoryProgressKey(categoryName), true)
            ?.apply()
        Log.d(TAG, "Saved spelling progress for category: $categoryName")
    }

    //region Sound Pool Management
    private fun setupSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(3) // Max simultaneous streams
            .setAudioAttributes(audioAttributes)
            .build()
        soundPool?.setOnLoadCompleteListener { _, sampleId, status ->
            // This listener is called when a sound is loaded
            Log.d(
                TAG,
                "OnLoadComplete: sampleId=$sampleId, status=$status. LoadingQueue: $soundsCurrentlyLoading"
            )

            val soundKeyProcessed: String? = synchronized(soundsCurrentlyLoading) {
                if (soundsCurrentlyLoading.isNotEmpty()) {
                    soundsCurrentlyLoading.removeAt(0) // Assume FIFO for sounds that returned 0 from load()
                } else {
                    null
                }
            }

            if (soundKeyProcessed != null) {
                if (status == 0) { // Success
                    Log.i(
                        TAG,
                        "Sound loaded successfully: '$soundKeyProcessed' -> sampleId=$sampleId"
                    )
                    soundIdMap[soundKeyProcessed] = sampleId
                } else { // Error
                    Log.e(
                        TAG,
                        "Error loading sound '$soundKeyProcessed'. Status: $status. Removing from map."
                    )
                    soundIdMap.remove(soundKeyProcessed) // Remove the 'loading' (0) state
                }
            } else {
                Log.w(
                    TAG,
                    "OnLoadComplete for sampleId $sampleId, but no key was pending in soundsCurrentlyLoading. Sound might have been pre-cached or logic error."
                )
                // Check if any key in soundIdMap points to this sampleId already (e.g. if loaded synchronously by initial load call)
                val existingKeyForSampleId = soundIdMap.entries.find { it.value == sampleId }?.key
                if (existingKeyForSampleId != null) {
                    Log.i(
                        TAG,
                        "Sound $existingKeyForSampleId (sampleId $sampleId) likely loaded synchronously or was already processed."
                    )
                } else if (status != 0) {
                    Log.e(
                        TAG,
                        "A sound load failed (sampleId $sampleId, status $status) and no key was in soundsCurrentlyLoading."
                    )
                }
            }
        }
    }

    private fun getSoundResourceName(itemText: String): String? {
        // Must match your R.raw.filename (lowercase, no spaces, no extension)
        val normalizedItemName = "item_" + itemText.lowercase(Locale.ROOT).replace(" ", "_")
        val resId = resources.getIdentifier(normalizedItemName, "raw", requireContext().packageName)
        return if (resId != 0) {
            Log.d(TAG, "Sound resource found: '$normalizedItemName' for item '$itemText'")
            normalizedItemName
        } else {
            Log.w(
                TAG,
                "Sound resource NOT FOUND in R.raw: '$normalizedItemName' for item '$itemText'"
            )
            null
        }
    }

    private fun loadSound(itemText: String) {
        val soundKey = itemText.lowercase(Locale.ROOT)

        if (soundIdMap.containsKey(soundKey) && soundIdMap[soundKey]!! > 0) {
            Log.d(TAG, "Sound for '$soundKey' already loaded (ID: ${soundIdMap[soundKey]}).")
            return
        }
        if (soundsCurrentlyLoading.contains(soundKey)) {
            Log.d(TAG, "Sound for '$soundKey' is already in soundsCurrentlyLoading. Not re-adding.")
            return
        }

        val soundResourceName = getSoundResourceName(itemText)
        if (soundResourceName == null) {
            Log.e(TAG, "No sound resource name for '$itemText'. Cannot load.")
            soundIdMap.remove(soundKey) // Ensure it's not marked as loading if previously failed
            return
        }

        val soundResId =
            resources.getIdentifier(soundResourceName, "raw", requireContext().packageName)
        if (soundResId == 0) { // Should not happen if getSoundResourceName found it, but defensive check
            Log.e(TAG, "Sound resource ID is 0 for '$soundResourceName'. Cannot load.")
            soundIdMap.remove(soundKey)
            return
        }

        Log.d(
            TAG,
            "Attempting to load sound: '$soundKey' (Resource: $soundResourceName, ID: $soundResId)"
        )
        val initialLoadId = soundPool!!.load(requireContext(), soundResId, 1)

        if (initialLoadId == 0) { // Sound is loading asynchronously
            Log.d(
                TAG,
                "Sound '$soundKey' is loading asynchronously (initialLoadId 0). Added to soundsCurrentlyLoading."
            )
            soundIdMap[soundKey] = 0 // Mark as loading
            synchronized(soundsCurrentlyLoading) {
                soundsCurrentlyLoading.add(soundKey)
            }
        } else { // Sound was loaded synchronously (e.g., already in SoundPool's cache)
            Log.i(TAG, "Sound '$soundKey' loaded synchronously or was cached. ID: $initialLoadId.")
            soundIdMap[soundKey] = initialLoadId
        }
    }

    private fun playSound(itemText: String) {
        val soundKey = itemText.lowercase(Locale.ROOT)
        val soundId = soundIdMap[soundKey]

        when (soundId) {
            null -> {
                Log.w(
                    TAG,
                    "Sound for '$soundKey' not loaded yet. Attempting to load. Click again to play."
                )
                loadSound(itemText) // Load it if not even an attempt was made
            }

            0 -> {
                Log.i(TAG, "Sound for '$soundKey' is currently loading. Please wait.")
                // Optionally: Toast(context, "Sound loading...", Toast.LENGTH_SHORT).show()
            }

            else -> { // soundId > 0
                Log.i(TAG, "Playing sound for '$soundKey' with ID: $soundId")
                soundPool?.play(soundId, 1f, 1f, 1, 0, 1f)
            }
        }
    }
    //endregion

    //region UI Display and Interaction
    private fun loadAndDisplayCategory() {
        val category = currentCategoryName
        if (category == null) {
            displayError("No category selected.")
            return
        }

        Log.i(TAG, "Loading category: '$category', isConsonant: $isConsonantCategory")
        resetUI()

        val displayType =
            if (isConsonantCategory) DISPLAY_TYPE_SYLLABLES else DISPLAY_TYPE_IMAGE_WITH_WORD
        var primaryWordForImage: String? = null // For image display logic
        val itemsToDisplayAsText: List<String>

        when (displayType) {
            DISPLAY_TYPE_IMAGE_WITH_WORD -> {
                val allItemsInCategory =
                    getItemsForGeneralCategory(category) // Get all items like ["Dog", "Cat", "Bird"]
                if (allItemsInCategory.isNotEmpty()) {
                    // Select a random item from the list
                    primaryWordForImage = allItemsInCategory.random() // <--- CHANGE THIS LINE
                    itemsToDisplayAsText =
                        listOf(primaryWordForImage) // Display this random word as clickable text
                    Log.d(TAG, "Randomly selected '$primaryWordForImage' from category '$category'")
                } else {
                    displayError("No items found for $category.")
                    return
                }
            }

            DISPLAY_TYPE_SYLLABLES -> {
                // Assuming categoryName implies a consonant (e.g., "b" for "ba, be, bi, bo, bu")
                primaryWordForImage = category // Image might be of the letter "B" itself
                itemsToDisplayAsText = getItemsForConsonantSyllables(category)
                if (itemsToDisplayAsText.isEmpty()) {
                    displayError("No syllables found for $category.")
                    return
                }
            }

            else -> {
                displayError("Unknown display type.")
                return
            }
        }

        // Display Image
        if (primaryWordForImage != null) {
            if (!displayImageFromAssets(primaryWordForImage, "en")) { // Assuming "en" language code
                Log.w(
                    TAG,
                    "Image for '$primaryWordForImage' not displayed, but proceeding with text items."
                )
                // Optionally hide itemImageView if image loading fails but text is available
                // itemImageView.visibility = View.GONE
            } else {
                itemImageView.visibility = View.VISIBLE
            }
        } else {
            itemImageView.visibility = View.GONE
        }

        // Display Text Items (Word or Syllables)
        if (itemsToDisplayAsText.isNotEmpty()) {
            itemsContainer.visibility = View.VISIBLE
            setupItemTextViews(itemsToDisplayAsText, displayType)
        } else if (itemImageView.visibility == View.GONE) { // Only show error if image also failed
            displayError("No content to display for $category.")
        }
    }

    private fun setupItemTextViews(items: List<String>, displayType: Int) {
        itemsContainer.removeAllViews()
        for (itemText in items) {
            val textView = TextView(requireContext()).apply {
                text = itemText
                textSize = if (displayType == DISPLAY_TYPE_SYLLABLES) 24f else 30f // Example sizes
                setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
                gravity = Gravity.CENTER
                isClickable = true
                isFocusable = true
                setBackgroundResource(R.drawable.syllable_background) // Add a simple background drawable

                setOnClickListener {
                    Log.d(TAG, "Item clicked: '$itemText'")
                    playSound(itemText) // Main interaction point for playing sound
                }
            }
            val textLp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            if (displayType == DISPLAY_TYPE_SYLLABLES && items.size > 1) {
                textLp.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
            } else { // Single word below image
                textLp.setMargins(0, dpToPx(8), 0, 0)
            }
            itemsContainer.addView(textView, textLp)

            // Pre-load sound for this item
            loadSound(itemText)
        }
    }

    private fun getItemsForGeneralCategory(categoryName: String): List<String> {
        // This needs to align with how your data is structured.
        // If categoryName is "animals", it should return ["Dog", "Cat", ...].
        // If categoryName is "Dog", it should return ["Dog"].
        Log.d(TAG, "getItemsForGeneralCategory for: $categoryName")
        return when (categoryName.lowercase(Locale.ROOT)) {
            "animal" -> resources.getStringArray(R.array.animal)
                .toList() // Ensure R.array.animal exists
            "objek" -> resources.getStringArray(R.array.objek)
                .toList() // Ensure R.array.object_array exists (XML uses "object" which is keyword)
            else -> listOf(categoryName) // Assume categoryName is the item itself
        }.also { Log.d(TAG, "Items for '$categoryName': $it") }
    }

    private fun getItemsForConsonantSyllables(consonant: String): List<String> {
        Log.d(TAG, "getItemsForConsonantSyllables for: $consonant")
        val consonantLower = consonant.lowercase(Locale.ROOT)
        val consonantOrder = listOf(
            "b", "c", "d", "f", "g", "h", "j", "k", "l", "m", "n",
            "p", "q", "r", "s", "t", "v", "w", "x", "y", "z"
        )

        val index = consonantOrder.indexOf(consonantLower)

        if (index == -1) {
            Log.w(
                TAG,
                "Consonant '$consonantLower' not found in defined order. Returning empty list."
            )
            return emptyList()
        }

        return try {
            val spellArray = resources.getStringArray(R.array.spell)
            if (index < spellArray.size) {
                val syllablesString = spellArray[index] // Get "ba,bi,bu,be,bo"
                val syllableList =
                    syllablesString.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                Log.d(TAG, "Syllables for '$consonantLower' (index $index): $syllableList")
                syllableList
            } else {
                Log.w(
                    TAG,
                    "Index $index out of bounds for spell array (size ${spellArray.size}). Consonant: '$consonantLower'"
                )
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error accessing or parsing spell array for consonant '$consonantLower'", e)
            emptyList()
        }
    }

    private fun resetUI() {
        itemImageView.visibility = View.GONE
        itemsContainer.visibility = View.GONE
        itemsContainer.removeAllViews()
        errorTextView.visibility = View.GONE
    }

    private fun displayError(message: String) {
        resetUI() // Ensure other elements are hidden
        errorTextView.text = message
        errorTextView.visibility = View.VISIBLE
        Log.e(TAG, "Displaying error: $message")
    }

    private fun displayImageFromAssets(itemName: String, languageCode: String): Boolean {
        // Assuming your image names are like "dog.svg", "cat.svg", "b.svg"
        val normalizedImageName = itemName.lowercase(Locale.ROOT).replace(" ", "_")
        // Path might be just "images/en/dog.svg" or "en/dog.svg" depending on your assets structure
        // If your assets are directly in "assets/en/dog.svg", then path is "en/normalizedImageName.svg"
        val imagePath =
            "$languageCode/$normalizedImageName.svg" // Adjusted path based on typical assets structure
        Log.d(TAG, "Attempting to load image from assets: $imagePath")

        try {
            val inputStream: InputStream = requireContext().assets.open(imagePath)
            val svg: SVG = SVG.getFromInputStream(inputStream)
            inputStream.close()

            if (svg.documentWidth != -1f) {
                val drawable: Drawable = PictureDrawable(svg.renderToPicture())
                itemImageView.setImageDrawable(drawable)
                // itemImageView.visibility = View.VISIBLE // Visibility handled by caller
                Log.i(TAG, "SVG Image loaded successfully: $imagePath")
                return true
            } else {
                Log.e(TAG, "SVG parsing error or invalid SVG for: $imagePath")
                return false
            }
        } catch (e: IOException) {
            Log.e(TAG, "IOException: Image not found or error reading: $imagePath", e)
        } catch (e: SVGParseException) {
            Log.e(TAG, "SVGParseException: Error parsing SVG: $imagePath", e)
        } catch (e: Exception) {
            Log.e(TAG, "General Exception loading image: $imagePath", e)
        }
        return false
    }
    //endregion

    //region Helper Functions
    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }
    //endregion
}