package com.hompimpa.comfylearn.ui.games

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hompimpa.comfylearn.helper.ImgButtons

class GamesViewModel(application: Application) : AndroidViewModel(application) {

    private val _imageResources = MutableLiveData<List<ImgButtons>>()
    val imageResources: LiveData<List<ImgButtons>> get() = _imageResources

}