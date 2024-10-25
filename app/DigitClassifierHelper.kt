package com.hompimpa.comfylearn.ui.study.tracing

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.tasks.vision.core.BaseVisionTaskApi
import com.google.mediapipe.tasks.vision.core.ImageProcessingOptions
import com.google.mediapipe.tasks.vision.classification.ClassificationResult
import com.google.mediapipe.tasks.vision.classification.ImageClassifier
import com.google.mediapipe.tasks.vision.classification.ImageClassifierOptions

class DigitClassifierHelper(context: Context) {

    private lateinit var imageClassifier: ImageClassifier

    init {
        setupDigitClassifier(context)
    }

    private fun setupDigitClassifier(context: Context) {
        val options = ImageClassifierOptions.builder()
            .setBaseOptions(
                BaseVisionTaskApi.BaseOptions.builder()
                    .setModelAssetPath("digit_classifier.tflite")
                    .build()
            )
            .build()
        imageClassifier = ImageClassifier.createFromOptions(context, options)
    }

    fun classify(bitmap: Bitmap): String {
        val result: ClassificationResult = imageClassifier.classify(bitmap)
        return result.classifications.first().categories.first().categoryName
    }
}
