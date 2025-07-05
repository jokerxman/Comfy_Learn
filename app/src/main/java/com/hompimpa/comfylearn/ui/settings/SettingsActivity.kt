package com.hompimpa.comfylearn.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.helper.BaseActivity
import com.hompimpa.comfylearn.helper.SettingPreferences
import com.hompimpa.comfylearn.helper.dataStore // Ensure this is your correct DataStore instance
import com.hompimpa.comfylearn.ui.auth.LoginActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Locale

class SettingsActivity : BaseActivity() {

    private lateinit var settingPreferences: SettingPreferences
    private lateinit var auth: FirebaseAuth

    // Views from your layout
    private lateinit var switchTheme: SwitchMaterial
    private lateinit var languageSpinner: Spinner
    private lateinit var btnApply: Button
    private lateinit var btnSignOut: Button

    private var currentLanguageCodeFromPrefs: String = "en" // Default, will be updated from prefs
    private var initialThemeIsDark: Boolean = false // Default, will be updated from prefs

    override fun attachBaseContext(newBase: Context) {
        // Initialize preferences here to get language for initial context setup
        // This ensures the Activity starts with the correct locale
        val prefs = SettingPreferences.getInstance(newBase.dataStore)

        // Using runBlocking here for simplicity to synchronously fetch initial language.
        // Ensure getLanguageSetting().first() is a fast operation.
        val languageCode = runBlocking { prefs.getLanguageSetting().first() }
        currentLanguageCodeFromPrefs = languageCode // Store it for later comparison

        val locale = Locale(languageCode)
        Locale.setDefault(locale) // Set for the entire app process

        val configuration = Configuration(newBase.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale) // For API 17+

        val context = newBase.createConfigurationContext(configuration)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize SettingPreferences for use in onCreate & beyond
        settingPreferences = SettingPreferences.getInstance(applicationContext.dataStore)

        // Apply theme BEFORE setContentView to ensure the layout inflates with the correct theme
        lifecycleScope.launch {
            initialThemeIsDark = settingPreferences.getThemeSetting().first()
            if (initialThemeIsDark) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            // Now that the theme is set, inflate the layout
            setContentView(R.layout.activity_settings)

            // Initialize views and setup listeners
            initializeViews()
            setupListeners()
            loadAndObserveSettings() // Load initial settings and observe changes
        }
    }

    private fun initializeViews() {
        switchTheme = findViewById(R.id.switch_theme)
        languageSpinner = findViewById(R.id.spinner_language)
        btnApply = findViewById(R.id.btn_apply)
        btnSignOut = findViewById(R.id.btn_sign_out)

        setupLanguageSpinner()
    }

    private fun setupLanguageSpinner() {
        // Consider using string resources for "English" and "Indonesian" for localization
        val languages = arrayOf(getString(R.string.language_english), getString(R.string.language_indonesian))
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = adapter
    }

    private fun loadAndObserveSettings() {
        // Set initial state of the Switch based on the theme applied in onCreate
        switchTheme.isChecked = initialThemeIsDark

        // Set spinner to the current language from preferences (already fetched in attachBaseContext)
        val initialLangPosition = if (currentLanguageCodeFromPrefs == "en") 0 else 1
        languageSpinner.setSelection(initialLangPosition, false) // false to prevent onItemSelected listener firing

        // If you need to observe live changes to preferences *while* SettingsActivity is open
        // (e.g., if settings could be changed from somewhere else, which is rare for this screen),
        // you would add collectors here. For now, we load initial values and act on "Apply".
    }

    private fun setupListeners() {
        auth = FirebaseAuth.getInstance()

        btnApply.setOnClickListener {
            applyChanges()
        }

        btnSignOut.setOnClickListener {
            signOut()
        }
    }

    private fun applyChanges() {
        val selectedLanguagePosition = languageSpinner.selectedItemPosition
        val selectedLanguageCodeOnSpinner = if (selectedLanguagePosition == 0) "en" else "id"
        val newThemeIsDarkFromSwitch = switchTheme.isChecked

        var languageActuallyChanged = false
        var themeActuallyChanged = false

        // 1. Process Language Change
        if (selectedLanguageCodeOnSpinner != currentLanguageCodeFromPrefs) {
            lifecycleScope.launch {
                settingPreferences.saveLanguageSetting(selectedLanguageCodeOnSpinner)
            }
            // Update currentLanguageCodeFromPrefs immediately for consistency if the user
            // were to interact further before recreate (though we finish soon).
            currentLanguageCodeFromPrefs = selectedLanguageCodeOnSpinner
            languageActuallyChanged = true
        }

        // 2. Process Theme Change
        if (newThemeIsDarkFromSwitch != initialThemeIsDark) {
            lifecycleScope.launch {
                settingPreferences.saveThemeSetting(newThemeIsDarkFromSwitch)
            }
            // Apply theme globally. The recreate will make the current activity reflect it.
            AppCompatDelegate.setDefaultNightMode(
                if (newThemeIsDarkFromSwitch) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
            initialThemeIsDark = newThemeIsDarkFromSwitch // Update initial state
            themeActuallyChanged = true
        }

        if (languageActuallyChanged || themeActuallyChanged) {
            Toast.makeText(this, getString(R.string.apply_changes), Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK) // Indicate that settings were changed

            // Recreate is important for the system to pick up changes, especially locale.
            // When this activity finishes, the previous one might also need to recreate
            // if it was started for result and observes RESULT_OK.
            recreate()
            finish() // Return to the previous activity
        } else {
            Toast.makeText(this, getString(R.string.no_changes_made), Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_CANCELED) // Indicate no changes were made
            finish() // Return to the previous activity
        }
    }

    private fun signOut() {
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish() // Finish SettingsActivity after starting LoginActivity
    }

    // This is called when the configuration changes while the activity is running.
    // With recreate() on apply, this might not be strictly needed for settings application,
    // but it's good practice to have it.
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // You could re-bind resources here if needed, but recreate() usually handles it
        // for theme/language changes originating from this activity.
    }
}