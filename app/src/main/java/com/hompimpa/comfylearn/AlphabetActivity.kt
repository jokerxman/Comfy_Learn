package com.hompimpa.comfylearn

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.hompimpa.comfylearn.helper.AlphabetPagerAdapter

class AlphabetActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alphabet)

        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        viewPager.adapter = AlphabetPagerAdapter(this)
    }
}