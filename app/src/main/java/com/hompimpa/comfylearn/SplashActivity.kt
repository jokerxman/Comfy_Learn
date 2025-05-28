package com.hompimpa.comfylearn

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import com.hompimpa.comfylearn.helper.MainViewModel
import com.hompimpa.comfylearn.helper.SettingPreferences
import com.hompimpa.comfylearn.helper.ViewModelFactory
import com.hompimpa.comfylearn.helper.dataStore
import com.hompimpa.comfylearn.ui.auth.LoginActivity
import java.util.Locale

class SplashActivity : AppCompatActivity() {
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val pref = SettingPreferences.getInstance(dataStore)
        mainViewModel = ViewModelProvider(this, ViewModelFactory(pref))[MainViewModel::class.java]

        mainViewModel.getThemeSettings().observe(this) { isDarkModeActive: Boolean ->
            if (isDarkModeActive) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        mainViewModel.getLanguageSetting().observe(this) { languageCode ->
            updateLocale(languageCode)
        }

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@SplashActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }, 1000)
    }

    private fun updateLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources = resources
        val configuration = resources.configuration.apply {
            setLocale(locale)
            setLayoutDirection(locale)
        }

        resources.updateConfiguration(configuration, resources.displayMetrics)
    }
}