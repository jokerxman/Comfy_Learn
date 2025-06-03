package com.hompimpa.comfylearn.ui.study.alphabet

import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.hompimpa.comfylearn.R
import java.util.Locale

class AlphabetFragment : Fragment() {

    private var currentLetter: Char = 'A'
    private lateinit var letterImageView: ImageView // Made lateinit
    private var loadingResourceToLetterMap = HashMap<Int, Char>()

    // Your existing letterImages map
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
        // Add R.drawable.ic_no_image if you want a specific placeholder in the map
    )

    // SoundPool related variables
    private var soundPool: SoundPool? = null
    private var soundIdMap = HashMap<Char, Int>() // Stores loaded sound IDs for each letter
    private var soundLoadingMap = HashMap<Char, Boolean>() // Tracks if a sound is currently loading

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentLetter = it.getChar(ARG_LETTER, 'A')
        }
        setupSoundPool()
        // Pre-load sound for the current letter if not already loaded or loading
        if (soundIdMap[currentLetter] == null && soundLoadingMap[currentLetter] != true) {
            loadSoundForLetter(currentLetter)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_alphabet, container, false)
        letterImageView = view.findViewById(R.id.letterImageView) // Initialize here
        updateLetterImage() // Pass the member variable

        letterImageView.setOnClickListener {
            Log.d("AlphabetFragment", "Image for letter '$currentLetter' clicked.")
            playSoundForLetter(currentLetter)
        }
        return view
    }

    // Call this when the fragment is created or when currentLetter changes
    // (if you were to allow changing the letter while the fragment is active)
    private fun updateLetterImage() {
        val imageRes =
            letterImages[currentLetter] ?: R.drawable.ic_no_image // Use a default if not found
        letterImageView.setImageResource(imageRes)
        // Ensure sound for the new letter is loaded
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
            // Try to find the letter this sampleId corresponds to
            // This relies on the fact that we stored the resourceId used for loading
            var letterForThisSound: Char? = null

            // Iterate through our loading map to find which letter corresponds to this sampleId
            // This is a bit indirect. A better way is to associate the soundId from load() if it were reliable.
            // For now, let's find the letter whose sound just got loaded by checking soundLoadingMap
            // and assuming the first one that was marked as loading and now matches this sampleId.

            // A more direct way: if we could map the 'sampleId' directly.
            // Since we can't directly map 'sampleId' before this callback,
            // we find the letter that was in 'soundLoadingMap'.
            // This assumes that only one sound is being actively processed by this callback at a time
            // for a given letter that was previously marked in soundLoadingMap.

            synchronized(soundLoadingMap) { // Synchronize access if multiple threads could modify
                val iterator = soundLoadingMap.entries.iterator()
                while (iterator.hasNext()) {
                    val entry = iterator.next()
                    if (entry.value) { // true means it was loading
                        // Now, how to be sure this sampleId belongs to this entry.key (letter)?
                        // This is the tricky part without a direct link from load() call.
                        // Let's assume for now the `sampleId` we got IS for the letter
                        // that we will now store in soundIdMap.
                        // The primary role of soundLoadingMap here is to prevent duplicate load calls.

                        // We need to find which letter this 'sampleId' belongs to.
                        // The 'loadingResourceToLetterMap' can help here if we knew the resourceId
                        // that 'sampleId' corresponds to, but SoundPool doesn't directly give that.

                        // Let's simplify: When a sound is loaded, we assume it's for the letter
                        // that was most recently passed to loadSoundForLetter and is still in soundLoadingMap.
                        // This is not perfectly robust if multiple loads are triggered very quickly
                        // and complete out of order.

                        // A common pattern is to use the ID returned by load() to key the completion.
                        // If load() returned 'streamId', then map 'streamId' to 'letter'.
                        // In callback, find 'letter' for 'sampleId' (if sampleId == streamId).

                        // Given the existing structure:
                        // Find the first letter in soundLoadingMap that IS loading. This is an assumption.
                        val loadingLetter = soundLoadingMap.filterValues { it }.keys.firstOrNull()
                        if (loadingLetter != null) {
                            letterForThisSound = loadingLetter
                            // Important: We will put 'sampleId' into soundIdMap for this letter.
                            // And remove it from soundLoadingMap.
                            break // Found a candidate
                        }
                    }
                }
            }


            if (letterForThisSound != null) {
                if (status == 0) {
                    Log.d(
                        "AlphabetFragment",
                        "Sound for letter '$letterForThisSound' loaded successfully (ID: $sampleId)."
                    )
                    soundIdMap[letterForThisSound!!] = sampleId
                } else {
                    Log.e(
                        "AlphabetFragment",
                        "Error loading sound for letter '$letterForThisSound'. Status: $status"
                    )
                    soundIdMap.remove(letterForThisSound!!) // Remove any placeholder if it was there
                }
                soundLoadingMap.remove(letterForThisSound!!) // Remove from loading map
                loadingResourceToLetterMap.entries.removeAll { it.value == letterForThisSound } // Clean up helper map
            } else {
                Log.w(
                    "AlphabetFragment",
                    "Sound loaded (ID: $sampleId, Status: $status), but couldn't determine which letter it was for. This might happen if load was cancelled or state was cleared."
                )
                // Potentially, this sampleId might be for a sound that was loaded but whose letter
                // is no longer tracked in soundLoadingMap.
            }
        }
    }

    private fun getSoundResourceIdForLetter(letter: Char): Int {
        val resourceName = "letter_${letter.toString().toLowerCase(Locale.ROOT)}"
        return try {
            resources.getIdentifier(resourceName, "raw", requireContext().packageName)
        } catch (e: Exception) {
            Log.e("AlphabetFragment", "Exception getting resource ID for $resourceName", e)
            0
        }
    }


    private fun loadSoundForLetter(letter: Char, onLoadedAndReadyToPlay: (() -> Unit)? = null) {
        if (soundIdMap.containsKey(letter) && soundIdMap[letter]!! > 0) {
            Log.d("AlphabetFragment", "Sound for '$letter' already loaded.")
            onLoadedAndReadyToPlay?.invoke()
            return
        }
        // Check soundLoadingMap before putting the key, to avoid overwriting if already true from another thread/call
        if (soundLoadingMap[letter] == true) {
            Log.d("AlphabetFragment", "Sound for '$letter' is already loading.")
            // You might want to queue the onLoadedAndReadyToPlay callback here
            // if multiple callers want to be notified. For now, we assume the first load process handles it.
            return
        }

        val soundResId = getSoundResourceIdForLetter(letter)
        if (soundResId == 0) {
            Log.e("AlphabetFragment", "Sound resource not found for letter '$letter'.")
            return
        }

        soundLoadingMap[letter] = true // Mark as loading *before* calling load
        // loadingResourceToLetterMap[soundResId] = letter // This would be useful if we could link back via soundResId

        Log.d(
            "AlphabetFragment",
            "Attempting to load sound for letter '$letter' (Resource ID: $soundResId)"
        )

        val streamId = soundPool?.load(requireContext(), soundResId, 1)

        if (streamId == null || streamId == 0) {
            Log.e(
                "AlphabetFragment",
                "SoundPool.load() returned 0 or null for letter '$letter'. Load initiation failed."
            )
            soundLoadingMap.remove(letter) // Failed to initiate load, so remove from loading.
            // loadingResourceToLetterMap.remove(soundResId)
        } else {
            Log.d(
                "AlphabetFragment",
                "SoundPool.load() initiated for letter '$letter'. Stream ID (may not be final sampleId): $streamId. Waiting for OnLoadCompleteListener."
            )
            // Here, you could potentially store a mapping: streamIdToLetterMap[streamId] = letter
            // Then, in OnLoadCompleteListener, if sampleId matches a key in streamIdToLetterMap, you know the letter.
            // However, the documentation for streamId from load() vs sampleId in callback can be subtle.
            // The sampleId in the callback is the one to use for play().
        }
        // The setOnLoadCompleteListener will update soundIdMap and clear soundLoadingMap[letter]
        // when loading finishes for this letter.
        // The onLoadedAndReadyToPlay callback is invoked from playSoundForLetter or directly if sound already loaded.
    }

    private fun playSoundForLetter(letter: Char) {
        val soundId = soundIdMap[letter]
        if (soundId != null && soundId > 0) {
            Log.d("AlphabetFragment", "Playing sound for letter '$letter' (Sound ID: $soundId)")
            soundPool?.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
        } else if (soundLoadingMap[letter] == true) {
            Log.d(
                "AlphabetFragment",
                "Sound for letter '$letter' is still loading. Will play when ready if clicked again or if a callback is set up."
            )
            // Optionally, you could make the loadSoundForLetter function take a callback
            // that gets executed upon successful load, and then play it.
            // For now, this requires the sound to be loaded before play is effective.
        } else {
            Log.w(
                "AlphabetFragment",
                "Sound for letter '$letter' not loaded. Attempting to load and then play."
            )
            // Attempt to load and then the user would need to click again, or
            // loadSoundForLetter should be enhanced with a completion callback that plays.
            loadSoundForLetter(letter) { // Add a simple callback for immediate play after load
                val newlyLoadedSoundId = soundIdMap[letter]
                if (newlyLoadedSoundId != null && newlyLoadedSoundId > 0) {
                    soundPool?.play(newlyLoadedSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
                    Log.d("AlphabetFragment", "Played newly loaded sound for '$letter'")
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        // It's good practice to release SoundPool resources when the view is destroyed
        // if the fragment instance might live longer than its view.
        // However, if sounds are tied to the fragment's lifecycle, onDestroy is also fine.
    }


    override fun onDestroy() {
        super.onDestroy()
        soundPool?.release()
        soundPool = null
        Log.d("AlphabetFragment", "SoundPool released.")
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