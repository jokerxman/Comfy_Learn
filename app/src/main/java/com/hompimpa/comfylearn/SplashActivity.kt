package com.hompimpa.comfylearn

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash) // Set the splash layout containing your full-screen image

        // Delay for 3 seconds (3000 milliseconds) and then start MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@SplashActivity, LoginActivity::class.java)
            startActivity(intent)
            finish() // Finish the SplashActivity so it doesn't stay in the back stack
        }, 3000) // Adjust the delay time if needed
    }
}