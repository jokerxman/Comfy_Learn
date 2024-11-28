package com.hompimpa.comfylearn

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import com.hompimpa.comfylearn.helper.MainViewModel
import com.hompimpa.comfylearn.helper.ViewModelFactory
import com.hompimpa.comfylearn.ui.settings.SettingPreferences
import com.hompimpa.comfylearn.ui.settings.dataStore

class SplashActivity : AppCompatActivity() {
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewModel first
        val pref = SettingPreferences.getInstance(dataStore)
        mainViewModel = ViewModelProvider(this, ViewModelFactory(pref))[MainViewModel::class.java]

        // Observe the theme setting and apply it
        mainViewModel.getThemeSettings().observe(this) { isDarkModeActive: Boolean ->
            if (isDarkModeActive) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        setContentView(R.layout.activity_splash) // Set the splash layout

        // Delay for 3 seconds (3000 milliseconds) and then start LoginActivity
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@SplashActivity, LoginActivity::class.java)
            startActivity(intent)
            finish() // Finish the SplashActivity so it doesn't stay in the back stack
        }, 3000) // Adjust the delay time if needed
    }
}
