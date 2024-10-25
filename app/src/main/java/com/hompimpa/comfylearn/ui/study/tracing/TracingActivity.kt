package com.hompimpa.comfylearn.ui.study.tracing

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hompimpa.comfylearn.R

class TracingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // Add this line
        setContentView(R.layout.activity_tracing)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DigitCanvasFragment())
                .commit()
        }
    }
}
