package com.hompimpa.comfylearn.helper

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Locale

abstract class BaseActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        val prefs = SettingPreferences.getInstance(newBase.dataStore)
        val languageCode = runBlocking { prefs.getLanguageSetting().first() }

        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val configuration = Configuration(newBase.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        val context = newBase.createConfigurationContext(configuration)
        super.attachBaseContext(context)
    }
}