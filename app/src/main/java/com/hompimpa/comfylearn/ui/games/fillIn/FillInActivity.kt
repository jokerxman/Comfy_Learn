package com.hompimpa.comfylearn.ui.games.fillIn

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGParseException
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.databinding.ActivityFillInBinding
import com.hompimpa.comfylearn.helper.BaseActivity
import com.hompimpa.comfylearn.helper.LetterOptionsAdapter
import com.hompimpa.comfylearn.helper.Question
import com.hompimpa.comfylearn.ui.games.DifficultySelectionActivity
import java.io.FileNotFoundException
import java.io.IOException
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

private const val STATE_CURRENT_WORD = "currentWord"
private const val STATE_CURRENT_IMAGE_URL = "currentImageUrl"
private const val STATE_LETTER_SLOTS_TEXT = "letterSlotsText"
private const val STATE_HINTS_AVAILABLE = "hintsAvailable"
private const val STATE_QUESTIONS_ANSWERED_THIS_SESSION = "questionsAnsweredThisSession"
private const val STATE_ANSWERED_WORDS = "answeredWordsThisSession"

class FillInActivity : BaseActivity() {

    private lateinit var binding: ActivityFillInBinding
    private var questions: MutableList<Question> = mutableListOf()
    private var answeredWordsThisSession: MutableSet<String> = mutableSetOf()
    private var currentQuestion: Question? = null
    private var letterSlots: MutableList<TextView> = mutableListOf()
    private var category: String = "animal"
    private var difficulty: String = DifficultySelectionActivity.DIFFICULTY_MEDIUM
    private var currentKeyboardLetters: MutableList<String> = mutableListOf()
    private val fullAlphabet: List<String> = listOf(
        "Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P",
        "A", "S", "D", "F", "G", "H", "J", "K", "L",
        "Z", "X", "C", "V", "B", "N", "M"
    )
    private val maxKeyboardSize = 15
    private val baseNumberOfDistractorLetters = 5
    private var hintsAvailable = 0
    private var questionsSuccessfullyAnsweredThisSession = 0

    companion object {
        const val DIFFICULTY_EASY = "EASY"
        const val DIFFICULTY_MEDIUM = "MEDIUM"
        const val DIFFICULTY_HARD = "HARD"
        private const val TAG = "FillInActivity"
        const val EXTRA_CATEGORY_PLAYED = "com.hompimpa.comfylearn.CATEGORY_PLAYED"
        const val EXTRA_QUESTIONS_COMPLETED = "com.hompimpa.comfylearn.QUESTIONS_COMPLETED"
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        currentQuestion?.let {
            outState.putString(STATE_CURRENT_WORD, it.word)
            outState.putString(STATE_CURRENT_IMAGE_URL, it.imageUrl)
        }
        val slotTexts = letterSlots.map { it.text.toString() }.toTypedArray()
        outState.putStringArray(STATE_LETTER_SLOTS_TEXT, slotTexts)
        outState.putInt(STATE_HINTS_AVAILABLE, hintsAvailable)
        outState.putInt(
            STATE_QUESTIONS_ANSWERED_THIS_SESSION,
            questionsSuccessfullyAnsweredThisSession
        )
        outState.putStringArrayList(STATE_ANSWERED_WORDS, ArrayList(answeredWordsThisSession))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFillInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        category = intent.getStringExtra("CATEGORY") ?: "animal"
        difficulty = intent.getStringExtra("DIFFICULTY") ?: DIFFICULTY_MEDIUM
        Log.d(TAG, "Game Started - Category: $category, Difficulty: $difficulty")

        binding.customBackButton.setOnClickListener {
            setGameResultAndFinish()
        }

        if (savedInstanceState != null) {
            val savedWord = savedInstanceState.getString(STATE_CURRENT_WORD)
            val savedImageUrl = savedInstanceState.getString(STATE_CURRENT_IMAGE_URL)
            hintsAvailable = savedInstanceState.getInt(
                STATE_HINTS_AVAILABLE,
                if (difficulty == DIFFICULTY_EASY) 1 else 0
            )
            questionsSuccessfullyAnsweredThisSession =
                savedInstanceState.getInt(STATE_QUESTIONS_ANSWERED_THIS_SESSION, 0)
            val savedAnsweredWords = savedInstanceState.getStringArrayList(STATE_ANSWERED_WORDS)
            if (savedAnsweredWords != null) {
                answeredWordsThisSession.addAll(savedAnsweredWords) // Restore
            }

            if (savedWord != null && savedImageUrl != null) {
                currentQuestion = Question(savedWord, savedImageUrl)
                loadQuestions()
                questions.removeAll { it.word == savedWord }
                setupLetterSlots()
                val savedSlotTexts = savedInstanceState.getStringArray(STATE_LETTER_SLOTS_TEXT)
                if (savedSlotTexts != null && savedSlotTexts.size == letterSlots.size) {
                    letterSlots.forEachIndexed { index, textView ->
                        textView.text = savedSlotTexts[index]
                    }
                }
                val imageDrawable = loadSvgFromAssets(currentQuestion?.imageUrl)
                if (imageDrawable != null) {
                    binding.imagePrompt.setImageDrawable(imageDrawable)
                } else {
                    binding.imagePrompt.setImageResource(R.drawable.ic_placeholder_image)
                }
                generateKeyboardLetters()
                if (currentKeyboardLetters.isNotEmpty()) currentKeyboardLetters.shuffle()
                setupLetterOptions()
                setupHintButton()
                setupUndoButton()
                checkAnswer(isRestoring = true)
                updateHintButtonState()
            } else {
                loadQuestionsAndSetup()
            }
        } else {
            loadQuestionsAndSetup()
        }
    }

    private fun loadQuestionsAndSetup() {
        questionsSuccessfullyAnsweredThisSession = 0
        loadQuestions()
        if (questions.isNotEmpty()) {
            setupQuestion()
        } else {
            Toast.makeText(this, "No questions found for category: $category", Toast.LENGTH_LONG)
                .show()
            setGameResultAndFinish()
        }
    }

    private fun checkAnswer(isRestoring: Boolean = false) {
        val cq = currentQuestion ?: return
        val userAnswerBuilder = StringBuilder()
        var allRequiredLettersFilled = true

        letterSlots.forEachIndexed { index, slot ->
            val slotText = slot.text.toString()
            userAnswerBuilder.append(slotText)
            if (index < cq.word.length && cq.word[index].isLetter() && slotText == "_") {
                allRequiredLettersFilled = false
            }
        }
        val userAnswer = userAnswerBuilder.toString()

        if (allRequiredLettersFilled) {
            if (userAnswer.equals(cq.word, ignoreCase = true)) {
                if (!isRestoring) {
                    questionsSuccessfullyAnsweredThisSession++
                    answeredWordsThisSession.add(cq.word)
                    Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show()
                }
                if (questions.isNotEmpty()) {
                    setupQuestion()
                } else {
                    Toast.makeText(this, "Congratulations! You've completed all questions!", Toast.LENGTH_LONG).show()
                    setGameResultAndFinish()
                }
            } else {
                if (!isRestoring) {
                    Toast.makeText(this, "Not quite, try again!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        updateHintButtonState()
    }

    override fun onBackPressed() {
        setGameResultAndFinish()
    }

    private fun setGameResultAndFinish() {
        val resultIntent = Intent()
        resultIntent.putExtra(EXTRA_CATEGORY_PLAYED, category)
        resultIntent.putExtra(EXTRA_QUESTIONS_COMPLETED, questionsSuccessfullyAnsweredThisSession)
        resultIntent.putExtra("DIFFICULTY_PLAYED_BACK", difficulty)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    private fun loadQuestions() {
        questions.clear()
        val categoryResourceId = getCategoryResourceId()
        try {
            val appContext = applicationContext
            val currentConfig = appContext.resources.configuration
            val englishConfig = Configuration(currentConfig)
            englishConfig.setLocale(Locale.ENGLISH)
            val englishContext = appContext.createConfigurationContext(englishConfig)
            val englishResources = englishContext.resources
            val wordArray = englishResources.getStringArray(categoryResourceId)

            if (wordArray.isEmpty()) {
                return
            }

            wordArray.forEach { wordWithSpaces ->
                if (!answeredWordsThisSession.contains(wordWithSpaces)) {
                    val filenameWord = wordWithSpaces.replace(" ", "_").lowercase(Locale.ENGLISH)
                    val svgPath = "en/${category.lowercase(Locale.ENGLISH)}_${filenameWord}.svg"
                    questions.add(Question(wordWithSpaces, svgPath))
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading questions for $category.", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun getCategoryResourceId(): Int {
        return when (category.lowercase(Locale.getDefault())) {
            "animal" -> R.array.animal
            "objek" -> R.array.objek
            else -> R.array.animal
        }
    }

    private fun setupQuestion() {
        if (questions.isEmpty()) {
            Toast.makeText(
                this,
                "Congratulations! You've completed all questions in this category!",
                Toast.LENGTH_LONG
            ).show()
            setGameResultAndFinish()
            return
        }

        val randomIndex = Random.nextInt(questions.size)
        currentQuestion = questions.removeAt(randomIndex)
        val cq = currentQuestion ?: return

        val imageDrawable = loadSvgFromAssets(cq.imageUrl)
        if (imageDrawable != null) {
            binding.imagePrompt.setImageDrawable(imageDrawable)
        } else {
            binding.imagePrompt.setImageResource(R.drawable.ic_placeholder_image)
            Toast.makeText(this, "Could not load image for ${cq.word}", Toast.LENGTH_SHORT).show()
        }
        hintsAvailable = if (difficulty == DIFFICULTY_EASY) 1 else 0
        setupLetterSlots()
        generateKeyboardLetters()
        if (currentKeyboardLetters.isNotEmpty()) currentKeyboardLetters.shuffle()
        setupLetterOptions()
        setupHintButton()
        setupUndoButton()
        updateHintButtonState()
    }

    private fun setupLetterSlots() {
        letterSlots.clear()
        binding.letterSlots.removeAllViews()
        val currentQ = currentQuestion ?: return
        val wordChars = currentQ.word.toList()
        val revealedIndices = determineRevealedIndices(currentQ.word, difficulty)

        // Use post to wait for the layout to be measured
        binding.letterSlots.post {
            val containerWidth = binding.letterSlots.width
            if (containerWidth == 0 || wordChars.isEmpty()) return@post

            // Calculate the size for each slot
            val slotMargin = resources.getDimensionPixelSize(R.dimen.slot_margin)
            val totalMargin = slotMargin * (wordChars.size - 1)
            val slotSize = (containerWidth - totalMargin) / wordChars.size

            wordChars.forEachIndexed { index, charAtIndex ->
                val slot = createLetterSlot(charAtIndex, revealedIndices.contains(index), slotSize)
                letterSlots.add(slot)
                binding.letterSlots.addView(slot)
            }
        }
    }

    private fun determineRevealedIndices(word: String, currentDifficulty: String): Set<Int> {
        if (currentDifficulty == DIFFICULTY_HARD) return emptySet()
        val wordChars = word.toList()
        val numLettersInWord = wordChars.count { it.isLetter() }
        if (numLettersInWord == 0) return emptySet()
        val revealPercentage = when (currentDifficulty) {
            DIFFICULTY_EASY -> 0.50
            DIFFICULTY_MEDIUM -> 0.25
            else -> 0.0
        }
        var numberOfLettersToReveal =
            calculateInitialLettersToReveal(numLettersInWord, revealPercentage)
        numberOfLettersToReveal =
            adjustLettersToReveal(numberOfLettersToReveal, numLettersInWord, currentDifficulty)
        if (numberOfLettersToReveal <= 0) return emptySet()
        return wordChars.indices
            .filter { wordChars[it].isLetter() }
            .shuffled()
            .take(numberOfLettersToReveal)
            .toSet()
    }

    private fun calculateInitialLettersToReveal(numLettersInWord: Int, percentage: Double): Int {
        return (numLettersInWord * percentage).toInt()
    }

    private fun adjustLettersToReveal(
        currentRevealCount: Int,
        numLettersInWord: Int,
        currentDifficulty: String
    ): Int {
        var adjustedCount = currentRevealCount
        if (numLettersInWord > 1 && adjustedCount >= numLettersInWord) adjustedCount =
            numLettersInWord - 1
        else if (numLettersInWord == 1 && adjustedCount > 0) adjustedCount = 0
        if (currentDifficulty == DIFFICULTY_EASY && numLettersInWord > 2 && adjustedCount == 0) adjustedCount =
            1
        return adjustedCount
    }

    private fun createLetterSlot(charToShow: Char, isRevealed: Boolean, size: Int): TextView {
        return TextView(this).apply {
            // Set LayoutParams to control size
            val margin = resources.getDimensionPixelSize(R.dimen.slot_margin)
            val params = LinearLayout.LayoutParams(size, size).also {
                it.setMargins(0, 0, margin, 0)
            }
            layoutParams = params

            gravity = Gravity.CENTER
            background = ContextCompat.getDrawable(this@FillInActivity, R.drawable.letter_slot_bg)

            // Enable auto-sizing text
            TextViewCompat.setAutoSizeTextTypeWithDefaults(this, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM)

            text = when {
                charToShow.isWhitespace() -> " "
                isRevealed -> charToShow.uppercaseChar().toString()
                else -> "_"
            }
        }
    }

    private fun generateKeyboardLetters() {
        currentKeyboardLetters.clear()
        val cq = currentQuestion ?: return
        val wordLettersList = cq.word.filter { it.isLetter() }.map { it.uppercaseChar().toString() }
        currentKeyboardLetters.addAll(wordLettersList)

        if (currentKeyboardLetters.size >= maxKeyboardSize) {
            currentKeyboardLetters = currentKeyboardLetters.take(maxKeyboardSize).toMutableList()
            return
        }

        val uniqueWordLetterTypes = wordLettersList.toSet()
        val potentialDistractors: List<String> =
            fullAlphabet.filterNot { uniqueWordLetterTypes.contains(it) }.shuffled()
        val initialDistractorsToAttempt = baseNumberOfDistractorLetters
        val actualInitialDistractorsCount =
            min(initialDistractorsToAttempt, potentialDistractors.size)

        if (potentialDistractors.isNotEmpty() && actualInitialDistractorsCount > 0) {
            currentKeyboardLetters.addAll(potentialDistractors.take(actualInitialDistractorsCount))
        }

        if (currentKeyboardLetters.size < maxKeyboardSize) {
            val remainingSlots = maxKeyboardSize - currentKeyboardLetters.size
            if (remainingSlots > 0) {
                val lettersAlreadyOnKeyboard = currentKeyboardLetters.toSet()
                val morePotentialDistractors: List<String> =
                    fullAlphabet.filterNot { lettersAlreadyOnKeyboard.contains(it) }.shuffled()
                val additionalDistractorsCount = min(remainingSlots, morePotentialDistractors.size)
                if (morePotentialDistractors.isNotEmpty() && additionalDistractorsCount > 0) {
                    currentKeyboardLetters.addAll(
                        morePotentialDistractors.take(
                            additionalDistractorsCount
                        )
                    )
                }
            }
        }
        if (currentKeyboardLetters.size > maxKeyboardSize) currentKeyboardLetters =
            currentKeyboardLetters.take(maxKeyboardSize).toMutableList()
        if (currentKeyboardLetters.isEmpty() && cq.word.any { it.isLetter() }) {
            currentKeyboardLetters.addAll(cq.word.filter { it.isLetter() }
                .map { it.uppercaseChar().toString() }.distinct().take(maxKeyboardSize))
        }
    }

    private fun setupLetterOptions() {
        val cq = currentQuestion ?: return
        if (currentKeyboardLetters.isEmpty()) {
            if (cq.word.any { it.isLetter() }) generateKeyboardLetters()
            if (currentKeyboardLetters.isEmpty()) {
                binding.letterOptions.adapter = null
                return
            }
        }
        currentKeyboardLetters.shuffle()

        val adapter = LetterOptionsAdapter(ArrayList(currentKeyboardLetters)) { letter ->
            fillSlotWithLetter(letter)
        }
        val columns = when {
            currentKeyboardLetters.size <= 5 -> max(1, currentKeyboardLetters.size)
            currentKeyboardLetters.size <= 10 -> 5
            else -> 5
        }
        binding.letterOptions.layoutManager = GridLayoutManager(this, columns)
        binding.letterOptions.adapter = adapter

        // Add a listener to calculate item size after layout
        binding.letterOptions.doOnLayout { view ->
            val rv = view as RecyclerView
            val totalWidth = rv.width
            val totalHeight = rv.height
            if (totalWidth == 0 || totalHeight == 0 || adapter.itemCount == 0) return@doOnLayout

            val rows = (adapter.itemCount + columns - 1) / columns // Calculate rows needed

            // Calculate size based on width and height, choose the smaller to ensure fit
            val sizeFromWidth = totalWidth / columns
            val sizeFromHeight = totalHeight / rows
            val itemSize = min(sizeFromWidth, sizeFromHeight)

            // Update the adapter with the new size
            (rv.adapter as? LetterOptionsAdapter)?.updateItemSize(itemSize)
        }
    }

    private fun setupHintButton() {
        binding.hintButton.visibility =
            if (difficulty == DIFFICULTY_EASY) View.VISIBLE else View.GONE
        if (difficulty == DIFFICULTY_EASY) {
            binding.hintButton.setOnClickListener { giveHint() }
        }
        updateHintButtonState()
    }

    private fun updateHintButtonState() {
        if (difficulty == DIFFICULTY_EASY) {
            val hasEmpty = hasEmptySlots()
            binding.hintButton.isEnabled = hintsAvailable > 0 && hasEmpty
            binding.hintButton.alpha = if (binding.hintButton.isEnabled) 1.0f else 0.5f
        } else {
            binding.hintButton.isEnabled = false
            binding.hintButton.alpha = 0.5f
            binding.hintButton.visibility = View.GONE
        }
    }

    private fun hasEmptySlots(): Boolean {
        val cqWord = currentQuestion?.word ?: return false
        for (index in letterSlots.indices) {
            val slot = letterSlots[index]
            if (index < cqWord.length && cqWord[index].isLetter() && slot.text.toString() == "_") {
                return true
            }
        }
        return false
    }

    private fun giveHint() {
        val cq = currentQuestion ?: return
        if (hintsAvailable <= 0 || difficulty != DIFFICULTY_EASY) {
            updateHintButtonState(); return
        }

        val emptyLetterSlotIndices = letterSlots.indices.filter { index ->
            index < cq.word.length && cq.word[index].isLetter() && letterSlots[index].text.toString() == "_"
        }

        if (emptyLetterSlotIndices.isNotEmpty()) {
            val actualSlotIndexToReveal = emptyLetterSlotIndices.random()
            val targetSlot = letterSlots[actualSlotIndexToReveal]
            val correctLetterForSlot = cq.word[actualSlotIndexToReveal]
            targetSlot.text = correctLetterForSlot.uppercaseChar().toString()
            hintsAvailable--
            Toast.makeText(this, "Hint given!", Toast.LENGTH_SHORT).show()
            checkAnswer()
        } else {
            Toast.makeText(this, "No more slots to hint or word is complete!", Toast.LENGTH_SHORT)
                .show()
        }
        updateHintButtonState()
    }

    private fun setupUndoButton() {
        binding.deleteButton.setOnClickListener {
            for (i in letterSlots.indices.reversed()) {
                val slot = letterSlots[i]
                if (currentQuestion != null && i < currentQuestion!!.word.length &&
                    currentQuestion!!.word[i].isLetter() &&
                    slot.text.toString() != "_" && slot.text.toString() != " "
                ) {
                    slot.text = "_"
                    updateHintButtonState()
                    break
                }
            }
        }
    }

    private fun fillSlotWithLetter(letter: String) {
        val slotToFill = letterSlots.firstOrNull { slot ->
            val index = letterSlots.indexOf(slot)
            currentQuestion != null && index < currentQuestion!!.word.length &&
                    currentQuestion!!.word[index].isLetter() && slot.text.toString() == "_"
        }

        slotToFill?.let {
            it.text = letter.uppercase(Locale.getDefault())
            checkAnswer()
        }
        updateHintButtonState()
    }

    private fun loadSvgFromAssets(path: String?): PictureDrawable? {
        if (path.isNullOrEmpty()) {
            return null
        }
        try {
            applicationContext.assets.open(path).use { inputStream ->
                val svg = SVG.getFromInputStream(inputStream)
                if (svg == null || svg.documentWidth == -1f) {
                    return null
                }
                val picture = svg.renderToPicture()
                if (picture == null) {
                    return null
                }
                return PictureDrawable(picture)
            }
        } catch (e: Exception) {
            handleSvgLoadingException(e, path)
        }
        return null
    }

    private fun handleSvgLoadingException(e: Exception, path: String) {
        when (e) {
            is FileNotFoundException -> Log.e(TAG, "SVG not found: assets/$path")
            is SVGParseException -> Log.e(TAG, "SVG parse error: assets/$path")
            is IOException -> Log.e(TAG, "SVG IO error: assets/$path")
            else -> Log.e(TAG, "SVG unexpected error: assets/$path", e)
        }
    }
}
