package com.hompimpa.comfylearn.ui.games.fillIn

import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGParseException
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.databinding.ActivityFillInBinding
import com.hompimpa.comfylearn.helper.LetterOptionsAdapter
import com.hompimpa.comfylearn.helper.Question
import java.io.FileNotFoundException
import java.io.IOException

class FillInActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFillInBinding
    private lateinit var questions: List<Question>
    private lateinit var currentQuestion: Question
    private lateinit var letterSlots: MutableList<TextView>
    private var category: String = "animal"

    private var currentKeyboardLetters: MutableList<String> = mutableListOf()
    private val fullAlphabet: List<String> = listOf(
        "Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P",
        "A", "S", "D", "F", "G", "H", "J", "K", "L",
        "Z", "X", "C", "V", "B", "N", "M"
    )
    // --- MODIFIED: Max keyboard size and base number of distractors ---
    private val maxKeyboardSize = 15
    private val baseNumberOfDistractorLetters = 5 // We'll adjust this based on answer length

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFillInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        category = intent.getStringExtra("CATEGORY") ?: "animal"

        loadQuestions()
        if (questions.isNotEmpty()) {
            setupQuestion()
        } else {
            Toast.makeText(this, "No questions found for category: $category", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun loadQuestions() {
        val resourceId = getCategoryResourceId()
        if (resourceId == R.array.animal && category != "animal") {
            Toast.makeText(this, "Category '$category' not found, defaulting to animals.", Toast.LENGTH_LONG).show()
        }
        val wordArray = resources.getStringArray(resourceId)

        if (wordArray.isEmpty()) {
            Log.e("FillInActivity", "Word array for category '$category' is empty.")
            questions = emptyList()
            return
        }

        questions = wordArray.map { wordWithSpaces ->
            val filenameWord = wordWithSpaces.replace(" ", "_").lowercase()
            val svgPath = "en/${category.lowercase()}_${filenameWord}.svg"
            Log.d("FillInActivity", "Mapping word: '$wordWithSpaces' to SVG path: '$svgPath'")
            Question(wordWithSpaces, svgPath)
        }
        Log.d("FillInActivity", "Loaded ${questions.size} questions for category: $category")
    }

    private fun getCategoryResourceId(): Int {
        return when (category.lowercase()) {
            "animal" -> R.array.animal
            "objek" -> R.array.objek
            else -> {
                Log.w("FillInActivity", "Unknown category: '$category'. Defaulting to animal.")
                R.array.animal
            }
        }
    }

    private fun setupQuestion() {
        if (questions.isEmpty()) {
            Toast.makeText(this, "Congratulations! You've completed all questions in this category!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        currentQuestion = questions.random()
        binding.imagePrompt.setImageDrawable(loadSvgFromAssets(currentQuestion.imageUrl))

        setupLetterSlots()

        generateKeyboardLetters() // Generate letters based on the current word and rules
        currentKeyboardLetters.shuffle() // Shuffle the final list for display

        setupLetterOptions()
        setupUndoButton()
    }

    // --- REVISED: generateKeyboardLetters function ---
    private fun generateKeyboardLetters() {
        currentKeyboardLetters.clear()

        // 1. Add all letters from the current word (including duplicates, ignoring spaces and case)
        val wordLetters = currentQuestion.word
            .filter { it.isLetter() } // Only take letters
            .map { it.uppercaseChar().toString() } // Convert to uppercase strings
        currentKeyboardLetters.addAll(wordLetters)

        // If word letters alone already exceed or meet maxKeyboardSize, we might truncate early or just use them
        if (currentKeyboardLetters.size >= maxKeyboardSize) {
            currentKeyboardLetters = currentKeyboardLetters.take(maxKeyboardSize).toMutableList()
            Log.d("FillInActivity", "Keyboard filled by word letters only (${currentKeyboardLetters.size}): $currentKeyboardLetters for word: ${currentQuestion.word}")
            return // No space for any distractors
        }

        // 2. Add a base number of initial distractors (unique types from word)
        val uniqueWordLetterTypes = wordLetters.distinct()
        var potentialDistractors = fullAlphabet.filterNot { it in uniqueWordLetterTypes }

        val initialDistractorsToAttempt = baseNumberOfDistractorLetters
        val actualInitialDistractors = potentialDistractors.shuffled().take(initialDistractorsToAttempt)

        currentKeyboardLetters.addAll(actualInitialDistractors)

        // 3. Fill remaining slots up to maxKeyboardSize with more distractors if needed
        if (currentKeyboardLetters.size < maxKeyboardSize) {
            // Recalculate remaining slots accurately
            val remainingSlots = maxKeyboardSize - currentKeyboardLetters.size

            if (remainingSlots > 0) {
                // Potential additional distractors should not be any of the letters *currently on the keyboard*
                // This ensures we don't try to add a letter that's already there (either from word or initial distractors)
                val lettersAlreadyOnKeyboard = currentKeyboardLetters.distinct() // Get all unique letters currently added
                val morePotentialDistractors = fullAlphabet.filterNot { it in lettersAlreadyOnKeyboard }

                val additionalDistractors = morePotentialDistractors.shuffled().take(remainingSlots)
                currentKeyboardLetters.addAll(additionalDistractors)
            }
        }

        // Final safeguard: if somehow we overshot (e.g. added too many initial distractors because base was high and word short)
        // or if the word letters themselves were very long. This take() ensures we don't exceed.
        if (currentKeyboardLetters.size > maxKeyboardSize) {
            currentKeyboardLetters = currentKeyboardLetters.take(maxKeyboardSize).toMutableList()
        }

        // At this point, currentKeyboardLetters should be at most maxKeyboardSize.
        // It will be less than maxKeyboardSize only if the wordLetters + all unique distractors from fullAlphabet
        // still don't sum up to maxKeyboardSize.

        Log.d("FillInActivity", "Generated keyboard (${currentKeyboardLetters.size} letters): $currentKeyboardLetters for word: ${currentQuestion.word}")
        // The list will be shuffled in setupQuestion()
    }

    private fun setupLetterSlots() {
        letterSlots = mutableListOf()
        binding.letterSlots.removeAllViews()
        currentQuestion.word.forEach { char ->
            val slot = TextView(this).apply {
                text = if (char.isWhitespace()) " " else "_"
                textSize = 24f
                gravity = Gravity.CENTER
                setPadding(0, 0, 8, 0)
            }
            letterSlots.add(slot)
            binding.letterSlots.addView(slot)
        }
    }

    private fun setupLetterOptions() {
        val adapter = LetterOptionsAdapter(currentKeyboardLetters.toList()) { letter ->
            fillSlotWithLetter(letter)
        }
        // Adjust grid layout dynamically or set a max column.
        // For up to 15 items, 5 columns might look good, or adjust based on screen width.
        val columns = when {
            currentKeyboardLetters.size <= 5 -> currentKeyboardLetters.size
            currentKeyboardLetters.size <= 10 -> 5
            else -> 5 // Or calculate based on available width
        }
        binding.letterOptions.layoutManager = GridLayoutManager(this, columns.coerceAtLeast(1))
        binding.letterOptions.adapter = adapter
    }

    private fun setupUndoButton() {
        binding.deleteButton.setOnClickListener {
            for (i in letterSlots.indices.reversed()) {
                val slot = letterSlots[i]
                if (slot.text != "_" && slot.text != " ") {
                    slot.text = "_"
                    break
                }
            }
        }
    }

    private fun fillSlotWithLetter(letter: String) {
        for (slot in letterSlots) {
            if (slot.text == "_") {
                slot.text = letter
                checkAnswer()
                break
            }
        }
    }

    private fun checkAnswer() {
        val userAnswerBuilder = StringBuilder()
        var allSlotsFilled = true
        letterSlots.forEach {
            userAnswerBuilder.append(it.text.toString())
            if (it.text == "_") {
                allSlotsFilled = false
            }
        }
        val userAnswer = userAnswerBuilder.toString()

        if (allSlotsFilled || userAnswer.length == currentQuestion.word.length) {
            if (userAnswer.equals(currentQuestion.word, ignoreCase = true)) {
                Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show()
                questions = questions.filterNot { it == currentQuestion }
                setupQuestion()
            } else {
                if (allSlotsFilled) {
                    Toast.makeText(this, "Not quite, try again!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadSvgFromAssets(path: String): PictureDrawable? {
        Log.d("FillInActivity", "Attempting to load SVG from assets: $path")
        try {
            assets.open(path).use { inputStream ->
                val svg = SVG.getFromInputStream(inputStream)
                if (svg?.documentWidth != -1f) {
                    val picture = svg.renderToPicture()
                    if (picture != null) {
                        Log.i("FillInActivity", "SVG loaded and rendered successfully: $path")
                        return PictureDrawable(picture)
                    } else {
                        Log.e("FillInActivity", "SVG.renderToPicture() returned null for: $path.")
                    }
                } else {
                    Log.e("FillInActivity", "SVG.getFromInputStream failed or SVG document is invalid for: $path. documentWidth: ${svg.documentWidth}")
                }
            }
        } catch (e: FileNotFoundException) {
            Log.e("FillInActivity", "SVG file not found at path: $path", e)
        } catch (e: SVGParseException) {
            Log.e("FillInActivity", "SVG parsing error for path: $path. Check SVG content.", e)
        } catch (e: IOException) {
            Log.e("FillInActivity", "IOException while reading SVG from path: $path", e)
        } catch (e: Exception) {
            Log.e("FillInActivity", "An unexpected error occurred loading SVG from path: $path", e)
        }
        return null
    }
}