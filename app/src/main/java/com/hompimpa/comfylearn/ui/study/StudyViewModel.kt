package com.hompimpa.comfylearn.ui.study

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hompimpa.comfylearn.helper.ImgButtons

class StudyViewModel(application: Application) : AndroidViewModel(application) {

    private val _imageResources = MutableLiveData<List<ImgButtons>>()
    val imageResources: LiveData<List<ImgButtons>> get() = _imageResources

}