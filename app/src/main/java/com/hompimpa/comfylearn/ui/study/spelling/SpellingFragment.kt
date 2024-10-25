package com.hompimpa.comfylearn.ui.study.spelling

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.hompimpa.comfylearn.R

class SpellingFragment : Fragment() {

    private var syllable: String = "a"
    private lateinit var syllableImages: Map<String, Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            syllable = it.getString(ARG_SYLLABLE) ?: "ba"
        }

        val syllables = listOf(
            "a", "i", "u", "e", "o",
            "ba", "bi", "bu", "be", "bo",
            "ca", "ci", "cu", "ce", "co",
            "da", "di", "du", "de", "do",
            "fa", "fi", "fu", "fe", "fo",
            "ga", "gi", "gu", "ge", "go",
            "ha", "hi", "hu", "he", "ho",
            "ja", "ji", "ju", "je", "jo",
            "ka", "ki", "ku", "ke", "ko",
            "la", "li", "lu", "le", "lo",
            "ma", "mi", "mu", "me", "mo",
            "na", "ni", "nu", "ne", "no",
            "pa", "pi", "pu", "pe", "po",
            "ra", "ri", "ru", "re", "ro",
            "sa", "si", "su", "se", "so",
            "ta", "ti", "tu", "te", "to",
            "va", "vi", "vu", "ve", "vo",
            "wa", "wi", "wu", "we", "wo",
            "xa", "xi", "xu", "xe", "xo",
            "ya", "yi", "yu", "ye", "yo",
            "za", "zi", "zu", "ze", "zo"
        )

        syllableImages = syllables.associateWith { syllable ->
            val resName = "spell_$syllable"
            resources.getIdentifier(resName, "drawable", requireContext().packageName)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_spelling, container, false)
        val syllableImageView: ImageView = view.findViewById(R.id.spellingImageView)
        updateSyllableImage(syllableImageView)
        return view
    }

    private fun updateSyllableImage(syllableImageView: ImageView) {
        val imageRes = syllableImages[syllable] ?: R.drawable.ic_no_image
        syllableImageView.setImageResource(imageRes)
    }

    companion object {
        private const val ARG_SYLLABLE = "syllable"

        @JvmStatic
        fun newInstance(syllable: String) = SpellingFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_SYLLABLE, syllable)
            }
        }
    }
}