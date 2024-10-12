package com.hompimpa.comfylearn.helper

/**
 * Created by JokerManX on 10/9/2024.
 */
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.hompimpa.comfylearn.AlphabetFragment

class AlphabetPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    // List of all letters in the alphabet
    private val alphabet = ('A'..'Z').toList()

    override fun getItemCount(): Int = alphabet.size

    override fun createFragment(position: Int): Fragment {
        val letter = alphabet[position]
        return AlphabetFragment.newInstance(letter)
    }
}
