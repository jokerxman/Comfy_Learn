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
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.helper.MainViewModel
import com.hompimpa.comfylearn.helper.SettingPreferences
import com.hompimpa.comfylearn.helper.ViewModelFactory
import com.hompimpa.comfylearn.helper.dataStore
import com.hompimpa.comfylearn.ui.auth.LoginActivity
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var switchTheme: SwitchMaterial
    private lateinit var languageSpinner: Spinner
    private lateinit var btnSignOut: Button
    private lateinit var btnApply: Button
    private var currentLanguageCode: String = "en"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        switchTheme = findViewById(R.id.switch_theme)
        languageSpinner = findViewById(R.id.spinner_language)
        btnSignOut = findViewById(R.id.btn_sign_out)
        btnApply = findViewById(R.id.btn_apply)

        val pref = SettingPreferences.getInstance(dataStore)
        mainViewModel = ViewModelProvider(this, ViewModelFactory(pref))[MainViewModel::class.java]

        mainViewModel.getThemeSettings().observe(this) { isDarkModeActive ->
            switchTheme.isChecked = isDarkModeActive
        }

        setupLanguageSpinner()

        auth = FirebaseAuth.getInstance()
        btnSignOut.setOnClickListener {
            signOut()
        }

        btnApply.setOnClickListener {
            applyChanges()
        }

        mainViewModel.getLanguageSetting().observe(this) { languageCode ->
            currentLanguageCode = languageCode
            updateLocale(languageCode)
            val position = if (languageCode == "en") 0 else 1
            languageSpinner.setSelection(position)
        }
    }

    private fun setupLanguageSpinner() {
        val languages = arrayOf("English", "Indonesian")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = adapter
    }

    private fun applyChanges() {
        val languageCode = if (languageSpinner.selectedItemPosition == 0) "en" else "id"

        if (languageCode != currentLanguageCode) {
            mainViewModel.saveLanguageSetting(languageCode)
            updateLocale(languageCode)
            currentLanguageCode = languageCode
        }

        val isDarkModeActive = switchTheme.isChecked
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkModeActive) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )

        Toast.makeText(this, "Settings applied", Toast.LENGTH_SHORT).show()

        setResult(Activity.RESULT_OK)

        finish()
    }

    private fun updateLocale(languageCode: String) {
        val locale = Locale(languageCode)

        if (locale != Locale.getDefault()) {
            Locale.setDefault(locale)

            val resources = resources
            val configuration = resources.configuration.apply {
                setLocale(locale)
                setLayoutDirection(locale)
            }

            resources.updateConfiguration(configuration, resources.displayMetrics)

            recreate()
        }
    }

    private fun signOut() {
        auth.signOut()
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}