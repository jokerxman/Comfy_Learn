package com.hompimpa.comfylearn.helper

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.hompimpa.comfylearn.ui.settings.SettingPreferences
import kotlinx.coroutines.launch

class MainViewModel(private val pref: SettingPreferences) : ViewModel() {

    // Existing theme methods
    fun getThemeSettings(): LiveData<Boolean> {
        return pref.getThemeSetting().asLiveData()
    }

    fun saveThemeSetting(isDarkModeActive: Boolean) {
        viewModelScope.launch {
            pref.saveThemeSetting(isDarkModeActive)
        }
    }

    // New language methods
    fun getLanguageSetting(): LiveData<String> {
        return pref.getLanguageSetting().asLiveData()
    }

    fun saveLanguageSetting(language: String) {
        viewModelScope.launch {
            pref.saveLanguageSetting(language)
        }
    }
}
