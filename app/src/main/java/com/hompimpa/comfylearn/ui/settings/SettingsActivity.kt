package com.hompimpa.comfylearn.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.databinding.ActivitySettingsBinding
import com.hompimpa.comfylearn.helper.BaseActivity
import com.hompimpa.comfylearn.helper.SettingPreferences
import com.hompimpa.comfylearn.helper.dataStore
import com.hompimpa.comfylearn.ui.auth.LoginActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsActivity : BaseActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var settingPreferences: SettingPreferences
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        settingPreferences = SettingPreferences.getInstance(dataStore)
        auth = FirebaseAuth.getInstance()

        setupLanguageSpinner()
        observeSettings()
        setupListeners()
    }

    private fun setupLanguageSpinner() {
        val languages = arrayOf(getString(R.string.language_english), getString(R.string.language_indonesian))
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerLanguage.adapter = adapter
    }

    private fun observeSettings() {
        lifecycleScope.launch {
            // Set the switch to its correct initial state
            val isDarkMode = settingPreferences.getThemeSetting().first()
            binding.switchTheme.isChecked = isDarkMode

            // Set the spinner to its correct initial state
            val languageCode = settingPreferences.getLanguageSetting().first()
            val languagePosition = if (languageCode == "in") 1 else 0
            binding.spinnerLanguage.setSelection(languagePosition, false)
        }
    }

    private fun setupListeners() {
        binding.btnApply.setOnClickListener {
            applyChanges()
        }

        binding.btnSignOut.setOnClickListener {
            signOut()
        }
    }

    private fun applyChanges() {
        val selectedLanguageCode = if (binding.spinnerLanguage.selectedItemPosition == 0) "en" else "in"
        val isDarkModeEnabled = binding.switchTheme.isChecked

        // Apply and save the theme
        lifecycleScope.launch {
            settingPreferences.saveThemeSetting(isDarkModeEnabled)
            val mode = if (isDarkModeEnabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(mode)
        }

        // Apply and save the language
        lifecycleScope.launch {
            settingPreferences.saveLanguageSetting(selectedLanguageCode)
            val appLocale = LocaleListCompat.forLanguageTags(selectedLanguageCode)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }

        Toast.makeText(this, getString(R.string.apply_changes), Toast.LENGTH_SHORT).show()
        // The changes will be applied automatically by the system.
        // We can simply finish the activity.
        finish()
    }

    private fun signOut() {
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
