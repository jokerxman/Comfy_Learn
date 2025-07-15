package com.hompimpa.comfylearn.ui.games.puzzle

import android.content.ContentValues.TAG
import android.content.res.Configuration
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGParseException
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.databinding.ActivityPuzzleBinding
import com.hompimpa.comfylearn.helper.AppConstants
import com.hompimpa.comfylearn.helper.BaseActivity
import com.hompimpa.comfylearn.helper.GameContentProvider
import com.hompimpa.comfylearn.helper.SoundManager
import com.hompimpa.comfylearn.ui.games.DifficultySelectionActivity
import java.io.IOException
import java.io.InputStream
import java.util.Locale
import kotlin.math.abs
import kotlin.random.Random as KotlinRandom

class PuzzleActivity : BaseActivity() {

    private lateinit var binding: ActivityPuzzleBinding
    private lateinit var currentWord: String
    private lateinit var currentDifficulty: String
    private lateinit var currentCategory: String

    // Helper data class to hold both the display word and the English base word for the image
    private data class WordPair(val display: String, val base: String)

    private val targetSlots = mutableListOf<TextView>()
    private val optionTileViews = mutableListOf<TextView>()

    private val slotFilledBy = mutableMapOf<Int, TextView?>()
    private val touchSlop by lazy { ViewConfiguration.get(this).scaledTouchSlop }

    private var currentlyDraggedView: View? = null
    private var dXTouch: Float = 0f
    private var dYTouch: Float = 0f
    private var isCurrentlyDragging: Boolean = false
    private var initialTouchXRaw: Float = 0f
    private var initialTouchYRaw: Float = 0f

    private var originalTileX: Float = 0f
    private var originalTileY: Float = 0f


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPuzzleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentDifficulty = intent.getStringExtra("DIFFICULTY")
            ?: DifficultySelectionActivity.DIFFICULTY_MEDIUM
        currentCategory = intent.getStringExtra("CATEGORY")
            ?: "animal"

        title =
            "Puzzle: ${currentCategory.replaceFirstChar { it.titlecase(Locale.getDefault()) }} ($currentDifficulty)"

        loadNextWord()

        binding.buttonCheckWord.setOnClickListener {
            SoundManager.playSound(SoundManager.Sound.BUTTON_CLICK)
            checkWord()
        }
        binding.buttonPlayAgain.setOnClickListener {
            SoundManager.playSound(SoundManager.Sound.BUTTON_CLICK)
            binding.layoutFeedback.visibility = View.GONE
            GameContentProvider.resetUsedWordsForCategory(this, currentCategory) // Pass context
            loadNextWord()
        }
        binding.buttonNextWord.setOnClickListener {
            SoundManager.playSound(SoundManager.Sound.BUTTON_CLICK)
            binding.layoutFeedback.visibility = View.GONE
            loadNextWord()
        }
        updateInstructionsForDifficulty()
    }

    private fun updateInstructionsForDifficulty() {
        binding.textViewPuzzleInstructions.text = when (currentDifficulty) {
            DifficultySelectionActivity.DIFFICULTY_HARD -> getString(R.string.puzzle_instructions_hard)
            else -> getString(R.string.puzzle_instructions_default)
        }
    }

    /**
     * *** MODIFIED FUNCTION ***
     * Loads word lists for both English and the current locale, then selects an unused word pair.
     */
    private fun loadNextWord() {
        binding.layoutWordConstruction.visibility = View.VISIBLE
        binding.buttonCheckWord.visibility = View.VISIBLE
        binding.textViewFeedback.visibility = View.INVISIBLE
        currentlyDraggedView = null

        // 1. Load both localized and English word arrays
        val categoryResId = GameContentProvider.getCategoryResourceId(currentCategory)
        if (categoryResId == 0) {
            handleNoMoreWords()
            return
        }

        val currentConfig = resources.configuration
        val localizedContext = createConfigurationContext(Configuration(currentConfig).apply {
            setLocale(Locale.getDefault())
        })
        val localizedWords = localizedContext.resources.getStringArray(categoryResId)

        val englishContext = createConfigurationContext(Configuration(currentConfig).apply {
            setLocale(Locale.ENGLISH)
        })
        val englishBaseWords = englishContext.resources.getStringArray(categoryResId)

        if (localizedWords.size != englishBaseWords.size) {
            Log.e(TAG, "Word list mismatch for category $currentCategory")
            handleNoMoreWords()
            return
        }

        // 2. Get used words and find an available word pair
        val usedWords = GameContentProvider.getUsedWords(this, currentCategory)
        val availableWordPairs = localizedWords.mapIndexedNotNull { index, displayWord ->
            if (usedWords.contains(displayWord)) {
                null
            } else {
                WordPair(display = displayWord, base = englishBaseWords[index])
            }
        }

        val wordPair = availableWordPairs.randomOrNull()

        if (wordPair == null) {
            handleNoMoreWords()
            return
        }

        // 3. Setup the game with the selected word pair
        currentWord = wordPair.display.uppercase(Locale.getDefault())
        displayImageFromAssets(wordPair.base, currentCategory) // Use the English base word for the image

        slotFilledBy.clear()
        setupTargetSlots(currentWord)
        setupCharacterOptions(currentWord)

        binding.layoutCharacterOptions.post {
            binding.layoutCharacterOptions.rescatterChildren()
        }
    }


    private fun handleNoMoreWords() {
        val allUsed = GameContentProvider.allWordsUsed(this, currentCategory, currentDifficulty)
        val message = if (allUsed) {
            savePuzzleProgress(currentCategory, currentDifficulty, isCompleted = true)
            getString(R.string.congratulations_all_words_category, currentCategory)
        } else {
            getString(R.string.no_more_words_puzzle, currentCategory, currentDifficulty)
        }
        showFeedback(message, allUsed, isGameEnd = true)
        binding.layoutWordConstruction.visibility = View.GONE
        binding.buttonCheckWord.visibility = View.GONE
    }

    private fun setupTargetSlots(word: String) {
        binding.layoutTargetSlots.removeAllViews()
        targetSlots.clear()
        for (i in word.indices) {
            val slotView = LayoutInflater.from(this)
                .inflate(R.layout.item_target_slot, binding.layoutTargetSlots, false) as TextView
            slotView.background =
                ContextCompat.getDrawable(this, R.drawable.target_slot_background_selector)
            slotView.tag = i
            binding.layoutTargetSlots.addView(slotView)
            targetSlots.add(slotView)
            slotFilledBy[i] = null // Initialize all slots as empty

            slotView.setOnTouchListener(TargetSlotTouchListener(slotView))
        }
    }

    private fun setupCharacterOptions(word: String) {
        binding.layoutCharacterOptions.removeAllViews()
        optionTileViews.clear()

        val alphabetSource = GameContentProvider.getAlphabet(this)
        val optionPool = generateOptionPool(word.uppercase(Locale.getDefault()), alphabetSource)

        if (optionPool.isEmpty()) {
            showTemporaryFeedback("Error: Could not generate character options.", false)
            return
        }

        optionPool.forEach { char ->
            val tileView = LayoutInflater.from(this).inflate(
                R.layout.item_character_option,
                binding.layoutCharacterOptions,
                false
            ) as TextView

            tileView.text = char.toString()
            tileView.visibility = View.VISIBLE
            tileView.setOnTouchListener(OptionTileTouchListener(tileView))

            binding.layoutCharacterOptions.addView(tileView)
            optionTileViews.add(tileView)
        }

        binding.layoutCharacterOptions.requestLayout()
    }

    private fun generateOptionPool(word: String, alphabet: List<Char>): List<Char> {
        val distractorsToAddBasedOnDifficulty = when (currentDifficulty) {
            DifficultySelectionActivity.DIFFICULTY_EASY -> 1
            DifficultySelectionActivity.DIFFICULTY_MEDIUM -> 2
            DifficultySelectionActivity.DIFFICULTY_HARD -> 3
            else -> 2
        }
        val distinctCharsInWord = word.toSet().toList()
        val essentialCharacters = word.toList()
        val desiredPoolSize = essentialCharacters.size + distractorsToAddBasedOnDifficulty
        val finalPoolSize =
            desiredPoolSize.coerceAtMost(alphabet.size.coerceAtLeast(essentialCharacters.size))
        val currentPool = essentialCharacters.toMutableList()

        if (currentPool.size < finalPoolSize) {
            val numDistractorsStillNeeded = finalPoolSize - currentPool.size
            val potentialDistractors = alphabet.filterNot { distinctCharsInWord.contains(it) }
                .shuffled(KotlinRandom(System.nanoTime()))
            currentPool.addAll(potentialDistractors.take(numDistractorsStillNeeded))
        }
        return currentPool.take(finalPoolSize).shuffled(KotlinRandom(System.nanoTime()))
    }

    private inner class OptionTileTouchListener(private val tileView: TextView) :
        View.OnTouchListener {
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            if (view != tileView || tileView.isInvisible) return false

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isCurrentlyDragging = false
                    initialTouchXRaw = event.rawX
                    initialTouchYRaw = event.rawY

                    originalTileX = tileView.x
                    originalTileY = tileView.y

                    currentlyDraggedView = tileView
                    binding.layoutCharacterOptions.bringChildToFrontZ(tileView)
                    dXTouch = tileView.x - event.rawX
                    dYTouch = tileView.y - event.rawY

                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    if (currentlyDraggedView != tileView) return false

                    if (!isCurrentlyDragging) {
                        if (abs(event.rawX - initialTouchXRaw) > touchSlop ||
                            abs(event.rawY - initialTouchYRaw) > touchSlop
                        ) {
                            isCurrentlyDragging = true
                        } else {
                            return true
                        }
                    }

                    tileView.x = event.rawX + dXTouch
                    tileView.y = event.rawY + dYTouch

                    return true
                }

                MotionEvent.ACTION_UP -> {
                    if (currentlyDraggedView != tileView) return false
                    isCurrentlyDragging = false
                    currentlyDraggedView = null

                    var droppedOnSlot = false
                    for (slotView in targetSlots) {
                        if (isViewOverView(slotView, event.rawX, event.rawY)) {
                            val slotIndex = slotView.tag as Int
                            placeTileInSlot(tileView, slotView, slotIndex)
                            droppedOnSlot = true
                            break
                        }
                    }

                    if (droppedOnSlot) {
                        return true
                    }

                    val optionsPile = binding.layoutCharacterOptions
                    if (isViewOverView(optionsPile, event.rawX, event.rawY)) {
                        letTileStayAtNewPosition(tileView)
                    } else {
                        returnTileToPile(tileView)
                    }

                    return true
                }
            }
            return false
        }
    }

    private fun letTileStayAtNewPosition(tile: TextView) {
        val state = binding.layoutCharacterOptions.getChildState(tile)
        state?.let {
            it.x = tile.x
            it.y = tile.y
            it.initialized = true
        }
    }

    private inner class TargetSlotTouchListener(private val slotView: TextView) :
        View.OnTouchListener {
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            if (event.action == MotionEvent.ACTION_DOWN && slotView.text.isNotEmpty()) {
                val slotIndex = slotView.tag as Int
                val tileToMove = slotFilledBy[slotIndex] ?: return false

                tileToMove.x = slotView.x + binding.layoutTargetSlots.x
                tileToMove.y = slotView.y + binding.layoutTargetSlots.y
                tileToMove.isVisible = true

                slotView.text = ""
                slotFilledBy[slotIndex] = null

                val downEvent = MotionEvent.obtain(event)
                downEvent.action = MotionEvent.ACTION_DOWN
                tileToMove.dispatchTouchEvent(downEvent)
                downEvent.recycle()
                return true
            }
            return false
        }
    }

    private fun placeTileInSlot(tileView: TextView, slotView: TextView, slotIndex: Int) {
        slotFilledBy[slotIndex]?.let { existingTile ->
            returnTileToPile(existingTile)
        }

        slotView.text = tileView.text
        tileView.isInvisible = true
        slotFilledBy[slotIndex] = tileView

        if (targetSlots.all { it.text.isNotEmpty() }) {
            checkWord()
        }
    }

    private fun returnTileToPile(tile: TextView) {
        tile.isVisible = true

        val parent = tile.parent as View
        val maxX = parent.width - tile.width
        val maxY = parent.height - tile.height

        tile.x = originalTileX.coerceIn(0f, maxX.toFloat())
        tile.y = originalTileY.coerceIn(0f, maxY.toFloat())

        val state = binding.layoutCharacterOptions.getChildState(tile)
        state?.let {
            it.x = tile.x
            it.y = tile.y
            it.initialized = true
        }
    }

    private fun isViewOverView(
        targetView: View,
        touchRawX: Float,
        touchRawY: Float
    ): Boolean {
        val targetLocation = IntArray(2)
        targetView.getLocationOnScreen(targetLocation)
        val targetRect = Rect(
            targetLocation[0],
            targetLocation[1],
            targetLocation[0] + targetView.width,
            targetLocation[1] + targetView.height
        )
        return targetRect.contains(touchRawX.toInt(), touchRawY.toInt())
    }

    private fun checkWord() {
        val formedWordBuilder = StringBuilder()
        var allSlotsFilled = true
        if (!::currentWord.isInitialized || currentWord.isEmpty()) {
            showFeedback(
                "Error: No word is currently loaded for checking!",
                false,
                isGameEnd = false
            )
            return
        }

        for (i in 0 until currentWord.length) {
            val charInSlot = targetSlots[i].text.firstOrNull()
            if (charInSlot != null) {
                formedWordBuilder.append(charInSlot)
            } else {
                allSlotsFilled = false
            }
        }

        if (!allSlotsFilled) {
            SoundManager.playSound(SoundManager.Sound.INCORRECT_ANSWER)
            showTemporaryFeedback(getString(R.string.feedback_incomplete_puzzle), false)
            return
        }

        val formedWord = formedWordBuilder.toString()
        if (formedWord.equals(currentWord, ignoreCase = true)) {
            SoundManager.playSound(SoundManager.Sound.CORRECT_ANSWER)
            // *** MODIFIED: Add the successfully solved word to the used list ***
            GameContentProvider.addUsedWord(this, currentCategory, currentWord)
            savePuzzleProgress(
                currentCategory,
                currentDifficulty,
                isCompleted = false,
                wordsActuallySolvedThisTime = 1
            )
            val allWordsNowUsed =
                GameContentProvider.allWordsUsed(this, currentCategory, currentDifficulty)
            if (allWordsNowUsed) {
                savePuzzleProgress(currentCategory, currentDifficulty, isCompleted = true)
            }
            showFeedback(getString(R.string.feedback_correct), true, isGameEnd = allWordsNowUsed)
        } else {
            SoundManager.playSound(SoundManager.Sound.INCORRECT_ANSWER)
            showTemporaryFeedback(getString(R.string.feedback_incorrect_try_again), false)
        }
    }

    private fun savePuzzleProgress(
        categoryName: String,
        difficulty: String,
        isCompleted: Boolean,
        wordsActuallySolvedThisTime: Int = 0
    ) {
        val prefs = getSharedPreferences(AppConstants.PREFS_PROGRESSION, MODE_PRIVATE)
        val baseKey = AppConstants.getPuzzleProgressKey(categoryName, difficulty)

        prefs.edit {
            if (isCompleted) {
                putBoolean(baseKey + "_completed", true)
            }
            if (wordsActuallySolvedThisTime > 0) {
                val currentSolved = prefs.getInt(baseKey + "_words_solved", 0)
                putInt(baseKey + "_words_solved", currentSolved + wordsActuallySolvedThisTime)
            }
        }
    }

    private fun showTemporaryFeedback(message: String, isCorrect: Boolean) {
        binding.textViewFeedback.text = message
        binding.textViewFeedback.setBackgroundColor(
            ContextCompat.getColor(
                this,
                if (isCorrect) R.color.feedback_correct_bg else R.color.feedback_incorrect_bg
            )
        )
        binding.textViewFeedback.visibility = View.VISIBLE
        Handler(Looper.getMainLooper()).postDelayed({
            binding.textViewFeedback.visibility = View.INVISIBLE
        }, 2000)
    }

    private fun showFeedback(
        message: String,
        isCorrectOrGameEndReason: Boolean,
        isGameEnd: Boolean
    ) {
        binding.textViewFeedbackPopup.text = message
        val bgColorRes = when {
            isGameEnd && isCorrectOrGameEndReason -> R.color.feedback_game_end_bg
            isGameEnd && !isCorrectOrGameEndReason -> R.color.feedback_game_end_bg
            isCorrectOrGameEndReason -> R.color.feedback_correct_bg
            else -> R.color.feedback_incorrect_bg
        }
        binding.textViewFeedbackPopup.setBackgroundColor(ContextCompat.getColor(this, bgColorRes))
        binding.layoutFeedback.visibility = View.VISIBLE

        binding.buttonNextWord.visibility =
            if (isCorrectOrGameEndReason && !isGameEnd) View.VISIBLE else View.GONE
        binding.buttonPlayAgain.visibility = if (isGameEnd) View.VISIBLE else View.GONE
    }

    /**
     * *** MODIFIED FUNCTION ***
     * Now takes the English base word to construct the path.
     * @param baseItemName The English word for the filename.
     * @param categoryName The category folder name.
     */
    private fun displayImageFromAssets(baseItemName: String, categoryName: String): Boolean {
        val normalizedImageName = baseItemName.lowercase(Locale.ROOT).replace(" ", "_")
        val imagePath = "en/${categoryName}_${normalizedImageName}.svg"
        Log.d(TAG, "Attempting to load image from assets: $imagePath")

        try {
            val inputStream: InputStream = assets.open(imagePath)
            val svg: SVG = SVG.getFromInputStream(inputStream)
            inputStream.close()

            if (svg.documentWidth != -1f) {
                val drawable: Drawable = PictureDrawable(svg.renderToPicture())
                binding.itemImageView.setImageDrawable(drawable)
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
}
