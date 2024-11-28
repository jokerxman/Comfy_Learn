package com.hompimpa.comfylearn.ui.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.hompimpa.comfylearn.LoginActivity
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.helper.MainViewModel
import com.hompimpa.comfylearn.helper.ViewModelFactory
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var switchTheme: SwitchMaterial
    private lateinit var languageSpinner: Spinner
    private lateinit var btnSignOut: Button
    private lateinit var btnApply: Button // Button to apply changes

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        switchTheme = findViewById(R.id.switch_theme)
        languageSpinner = findViewById(R.id.spinner_language)
        btnSignOut = findViewById(R.id.btn_sign_out)
        btnApply = findViewById(R.id.btn_apply) // Initialize the apply button

        val pref = SettingPreferences.getInstance(dataStore)
        mainViewModel = ViewModelProvider(this, ViewModelFactory(pref))[MainViewModel::class.java]

        mainViewModel.getThemeSettings().observe(this) { isDarkModeActive ->
            switchTheme.isChecked = isDarkModeActive
        }

        switchTheme.setOnCheckedChangeListener { _, isChecked ->
            mainViewModel.saveThemeSetting(isChecked) // Save the setting, but apply it on button click
        }

        setupLanguageSpinner()

        auth = FirebaseAuth.getInstance()
        btnSignOut.setOnClickListener {
            signOut()
        }

        btnApply.setOnClickListener {
            applyChanges()
        }
    }

    private fun setupLanguageSpinner() {
        val languages = arrayOf("English", "Indonesian")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = adapter

        mainViewModel.getLanguageSetting().observe(this) { languageCode ->
            val position = if (languageCode == "en") 0 else 1
            languageSpinner.setSelection(position)
        }
    }

    private fun applyChanges() {
        // Get the selected language and theme
        val languageCode = if (languageSpinner.selectedItemPosition == 0) "en" else "id"
        mainViewModel.saveLanguageSetting(languageCode)
        updateLocale(languageCode)

        // Apply the theme setting
        val isDarkModeActive = switchTheme.isChecked
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkModeActive) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

        // Notify the user
        Toast.makeText(this, "Settings applied", Toast.LENGTH_SHORT).show()
        setResult(Activity.RESULT_OK) // Notify parent activity, if needed
        finish() // Close the settings activity
        startActivity(Intent(this, SettingsActivity::class.java)) // Restart activity
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

    private fun signOut() {
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}