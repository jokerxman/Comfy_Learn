package com.hompimpa.comfylearn.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hompimpa.comfylearn.helper.ImgButtons
import com.hompimpa.comfylearn.R

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val _imageResources = MutableLiveData<List<ImgButtons>>()
    val imageResources: LiveData<List<ImgButtons>> get() = _imageResources

    init {
        val resources = application.resources
        val dataPhoto = resources.obtainTypedArray(R.array.data_buttonStudy) // Replace with your array ID
        val imageResourcesList =
            (0 until dataPhoto.length()).mapNotNull { dataPhoto.getResourceId(it, -1) }
                .filter { it != -1 }.map { ImgButtons(it) }.toList()
        dataPhoto.recycle()
        _imageResources.value = imageResourcesList
    }
}