package com.hompimpa.comfylearn.ui.games.puzzle

import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.isInvisible
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.databinding.ActivityPuzzleBinding
import com.hompimpa.comfylearn.helper.AppConstants
import com.hompimpa.comfylearn.helper.BaseActivity
import com.hompimpa.comfylearn.helper.GameContentProvider
import com.hompimpa.comfylearn.ui.games.DifficultySelectionActivity
import java.util.Locale
import kotlin.math.abs
import kotlin.random.Random as KotlinRandom

class PuzzleActivity : BaseActivity() {

    private lateinit var binding: ActivityPuzzleBinding
    private lateinit var currentWord: String
    private lateinit var currentDifficulty: String
    private lateinit var currentCategory: String

    private val targetSlots = mutableListOf<TextView>()
    private val optionTileViews = mutableListOf<TextView>()
    private val slotFilledBy = mutableMapOf<Int, Char?>()
    private val optionTileDragStartRotations = mutableMapOf<View, Float>()
    private val touchSlop by lazy { ViewConfiguration.get(this).scaledTouchSlop }

    private var currentlyDraggedView: View? = null
    private var originalXOfDraggedView: Float = 0f
    private var originalYOfDraggedView: Float = 0f
    private var dXTouch: Float = 0f
    private var dYTouch: Float = 0f
    private var isCurrentlyDragging: Boolean = false
    private var initialTouchXRaw: Float = 0f
    private var initialTouchYRaw: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPuzzleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentDifficulty = intent.getStringExtra("DIFFICULTY")
            ?: DifficultySelectionActivity.DIFFICULTY_MEDIUM
        currentCategory = intent.getStringExtra("CATEGORY")
            ?: "animal"

        title =
            "Puzzle: ${currentCategory.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }} ($currentDifficulty)"

        loadNextWord()

        binding.buttonCheckWord.setOnClickListener { checkWord() }
        binding.buttonPlayAgain.setOnClickListener {
            binding.layoutFeedback.visibility = View.GONE
            GameContentProvider.resetUsedWordsForCategory(currentCategory)
            loadNextWord()
        }
        binding.buttonNextWord.setOnClickListener {
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

    private fun loadNextWord() {
        binding.layoutWordConstruction.visibility = View.VISIBLE
        binding.buttonCheckWord.visibility = View.VISIBLE
        binding.textViewFeedback.visibility = View.INVISIBLE
        currentlyDraggedView = null

        val word = GameContentProvider.getNextWord(this, currentCategory, currentDifficulty)

        if (word == null) {
            handleNoMoreWords()
            return
        }
        currentWord = word.uppercase(Locale.getDefault())

        slotFilledBy.clear()
        setupTargetSlots(currentWord)
        setupCharacterOptions(currentWord, currentDifficulty)

        binding.layoutCharacterOptions.post {
            binding.layoutCharacterOptions.rescatterChildren()
        }
    }

    private fun handleNoMoreWords() {
        val allUsed = GameContentProvider.allWordsUsed(this, currentCategory, currentDifficulty)
        val message = if (allUsed) {
            savePuzzleProgress(currentCategory, currentDifficulty, isCompleted = true)
            getString(
                R.string.congratulations_all_words_category,
                currentCategory.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() })
        } else {
            getString(
                R.string.no_more_words_puzzle,
                currentCategory.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                currentDifficulty
            )
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
            slotFilledBy[i] = null
        }
    }

    private fun setupCharacterOptions(word: String, difficulty: String) {
        binding.layoutCharacterOptions.removeAllViews()
        optionTileViews.clear()

        val alphabetSource = GameContentProvider.getAlphabet(this)
        val optionPool =
            generateOptionPool(word.uppercase(Locale.getDefault()), alphabetSource)

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

        binding.layoutCharacterOptions.post {
            binding.layoutCharacterOptions.rescatterChildren()
        }
    }

    private fun generateOptionPool(
        word: String,
        alphabet: List<Char>
    ): List<Char> {
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

    private fun returnCharToOptionsManual(charToReturn: Char) {
        val tileToMakeVisible = optionTileViews.find {
            it.text.firstOrNull() == charToReturn && it.isInvisible
        }

        if (tileToMakeVisible != null) {
            tileToMakeVisible.visibility = View.VISIBLE
            tileToMakeVisible.setOnTouchListener(OptionTileTouchListener(tileToMakeVisible))

            val originalState = binding.layoutCharacterOptions.getChildState(tileToMakeVisible)

            if (originalState != null && originalState.initialized) {
                tileToMakeVisible.animate()
                    .x(originalState.x)
                    .y(originalState.y)
                    .rotation(originalState.rotation)
                    .setDuration(150)
                    .withEndAction {
                        binding.layoutCharacterOptions.requestLayout()
                        binding.layoutCharacterOptions.invalidate()
                    }
                    .start()
            } else {
                originalState?.initialized = false
                binding.layoutCharacterOptions.requestChildLayoutUpdate(tileToMakeVisible, true)
            }
        } else {
            binding.layoutCharacterOptions.post {
                binding.layoutCharacterOptions.rescatterChildren()
            }
        }
    }

    private inner class OptionTileTouchListener(private val tileView: TextView) :
        View.OnTouchListener {
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            if (view != tileView || tileView.isInvisible) return false

            val charOfTile = tileView.text.firstOrNull() ?: return false

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isCurrentlyDragging = false
                    initialTouchXRaw = event.rawX
                    initialTouchYRaw = event.rawY

                    currentlyDraggedView = tileView
                    val locationOnScreen = IntArray(2)
                    tileView.getLocationOnScreen(locationOnScreen)
                    dXTouch = locationOnScreen[0] - event.rawX
                    dYTouch = locationOnScreen[1] - event.rawY

                    originalXOfDraggedView = tileView.x
                    originalYOfDraggedView = tileView.y
                    optionTileDragStartRotations[tileView] = tileView.rotation

                    if (tileView.parent == binding.layoutCharacterOptions) {
                        binding.layoutCharacterOptions.bringChildToFrontVisually(tileView)
                    }
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
                    val parentLocation = IntArray(2)
                    (tileView.parent as View).getLocationOnScreen(parentLocation)
                    tileView.x = (event.rawX + dXTouch) - parentLocation[0]
                    tileView.y = (event.rawY + dYTouch) - parentLocation[1]
                    return true
                }

                MotionEvent.ACTION_UP -> {
                    if (currentlyDraggedView != tileView) return false

                    if (!isCurrentlyDragging) {
                        currentlyDraggedView = null
                        return true
                    }

                    var successfullyDropped = false
                    for (i in targetSlots.indices) {
                        val targetSlotView = targetSlots[i]
                        if (isViewOverView(targetSlotView, event.rawX, event.rawY)) {
                            handleDropOnSlot(charOfTile, i, tileView)
                            successfullyDropped = true
                            break
                        }
                    }

                    if (!successfullyDropped) {
                        tileView.animate()
                            .x(originalXOfDraggedView)
                            .y(originalYOfDraggedView)
                            .rotation(optionTileDragStartRotations[tileView] ?: 0f)
                            .setDuration(200)
                            .withEndAction {
                                val state = binding.layoutCharacterOptions.getChildState(tileView)
                                state?.let {
                                    it.x = tileView.x
                                    it.y = tileView.y
                                    it.rotation = tileView.rotation
                                    it.initialized = true
                                }
                                binding.layoutCharacterOptions.requestLayout()
                                binding.layoutCharacterOptions.invalidate()
                            }.start()
                    }
                    isCurrentlyDragging = false
                    currentlyDraggedView = null
                    return true
                }
            }
            return false
        }
    }

    private fun handleDropOnSlot(
        droppedChar: Char,
        targetSlotIndex: Int,
        fromOptionTile: TextView?
    ) {
        val targetSlotView = targetSlots[targetSlotIndex]
        val charCurrentlyInTargetSlot = slotFilledBy[targetSlotIndex]

        if (charCurrentlyInTargetSlot != null && charCurrentlyInTargetSlot != droppedChar) {
            returnCharToOptionsManual(charCurrentlyInTargetSlot)
        }

        targetSlotView.text = droppedChar.toString()
        slotFilledBy[targetSlotIndex] = droppedChar
        targetSlotView.background =
            ContextCompat.getDrawable(this, R.drawable.target_slot_background_selector)
        targetSlotView.setOnTouchListener(null)

        fromOptionTile?.let {
            it.visibility = View.INVISIBLE
            it.setOnTouchListener(null)
            val state = binding.layoutCharacterOptions.getChildState(it)
            state?.let { s -> s.isUsed = true }
        }
        checkWord()
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
            val charInSlot = slotFilledBy[i]
            if (charInSlot != null) {
                formedWordBuilder.append(charInSlot)
            } else {
                allSlotsFilled = false
            }
        }

        if (!allSlotsFilled && formedWordBuilder.length != currentWord.length) {
            showTemporaryFeedback(getString(R.string.feedback_incomplete_puzzle), false)
            return
        }

        val formedWord = formedWordBuilder.toString()
        if (formedWord.equals(currentWord, ignoreCase = true)) {
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
}
