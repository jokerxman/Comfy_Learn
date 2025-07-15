package com.hompimpa.comfylearn.ui.study.alphabet

import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.hompimpa.comfylearn.R
import java.util.Locale

class AlphabetFragment : Fragment() {

    private var currentLetter: Char = 'A'
    private lateinit var letterImageView: ImageView
    private var loadingResourceToLetterMap = HashMap<Int, Char>()

    private val letterImages = mapOf(
        'A' to R.drawable.letter_a,
        'B' to R.drawable.letter_b,
        'C' to R.drawable.letter_c,
        'D' to R.drawable.letter_d,
        'E' to R.drawable.letter_e,
        'F' to R.drawable.letter_f,
        'G' to R.drawable.letter_g,
        'H' to R.drawable.letter_h,
        'I' to R.drawable.letter_i,
        'J' to R.drawable.letter_j,
        'K' to R.drawable.letter_k,
        'L' to R.drawable.letter_l,
        'M' to R.drawable.letter_m,
        'N' to R.drawable.letter_n,
        'O' to R.drawable.letter_o,
        'P' to R.drawable.letter_p,
        'Q' to R.drawable.letter_q,
        'R' to R.drawable.letter_r,
        'S' to R.drawable.letter_s,
        'T' to R.drawable.letter_t,
        'U' to R.drawable.letter_u,
        'V' to R.drawable.letter_v,
        'W' to R.drawable.letter_w,
        'X' to R.drawable.letter_x,
        'Y' to R.drawable.letter_y,
        'Z' to R.drawable.letter_z
    )

    private var soundPool: SoundPool? = null
    private var soundIdMap = HashMap<Char, Int>()
    private var soundLoadingMap = HashMap<Char, Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentLetter = it.getChar(ARG_LETTER, 'A')
        }
        setupSoundPool()
        if (soundIdMap[currentLetter] == null && soundLoadingMap[currentLetter] != true) {
            loadSoundForLetter(currentLetter)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_alphabet, container, false)
        letterImageView = view.findViewById(R.id.letterImageView)
        updateLetterImage()

        letterImageView.setOnClickListener {
            playSoundForLetter(currentLetter)
        }
        return view
    }

    private fun updateLetterImage() {
        val imageRes = letterImages[currentLetter] ?: R.drawable.ic_no_image
        letterImageView.setImageResource(imageRes)
        if (soundIdMap[currentLetter] == null && soundLoadingMap[currentLetter] != true) {
            loadSoundForLetter(currentLetter)
        }
    }

    private fun setupSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder().setMaxStreams(3).setAudioAttributes(audioAttributes).build()

        soundPool?.setOnLoadCompleteListener { _, sampleId, status ->
            var letterForThisSound: Char? = null

            synchronized(soundLoadingMap) {
                val iterator = soundLoadingMap.entries.iterator()
                while (iterator.hasNext()) {
                    val entry = iterator.next()
                    if (entry.value) {
                        val loadingLetter = soundLoadingMap.filterValues { it }.keys.firstOrNull()
                        if (loadingLetter != null) {
                            letterForThisSound = loadingLetter
                            break
                        }
                    }
                }
            }

            if (letterForThisSound != null) {
                if (status == 0) {
                    soundIdMap[letterForThisSound] = sampleId
                } else {
                    soundIdMap.remove(letterForThisSound)
                }
                soundLoadingMap.remove(letterForThisSound)
                loadingResourceToLetterMap.entries.removeAll { it.value == letterForThisSound }
            }
        }
    }

    private fun getSoundResourceIdForLetter(letter: Char): Int {
        val resourceName = "letter_${letter.toString().lowercase(Locale.ROOT)}"
        return try {
            resources.getIdentifier(resourceName, "raw", requireContext().packageName)
        } catch (_: Exception) {
            0
        }
    }

    private fun loadSoundForLetter(letter: Char) {
        val soundResId = getSoundResourceIdForLetter(letter)
        if (soundResId == 0) {
            return
        }

        soundLoadingMap[letter] = true

        val streamId = soundPool?.load(requireContext(), soundResId, 1)

        if (streamId == null || streamId == 0) {
            soundLoadingMap.remove(letter)
        }
    }

    private fun playSoundForLetter(letter: Char) {
        val soundId = soundIdMap[letter]
        if (soundId != null && soundId > 0) {
            soundPool?.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
        } else if (soundLoadingMap[letter] == true) {
            // Sound is still loading
        } else {
            loadSoundForLetter(letter)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool?.release()
        soundPool = null
    }

    companion object {
        private const val ARG_LETTER = "letter"

        @JvmStatic
        fun newInstance(letter: Char) =
            AlphabetFragment().apply {
                arguments = Bundle().apply {
                    putChar(ARG_LETTER, letter)
                }
            }
    }
}
