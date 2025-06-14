/*
 * Copyright 2023 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hompimpa.comfylearn.ui.study.tracing

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.mediapipe.tasks.vision.imageclassifier.ImageClassifierResult
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.databinding.FragmentDigitCanvasBinding

class DigitCanvasFragment : Fragment(), DigitClassifierHelper.DigitClassifierListener {
    private var _fragmentDigitCanvasBinding: FragmentDigitCanvasBinding? = null
    private val fragmentDigitCanvasBinding get() = _fragmentDigitCanvasBinding!!

    // STEP 7a Initialize classifier.
    private lateinit var digitClassifierHelper: DigitClassifierHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentDigitCanvasBinding = FragmentDigitCanvasBinding.inflate(
            inflater,
            container, false
        )
        return fragmentDigitCanvasBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // STEP 7b Initialize classifier
        // Initialize the digit classifier helper, which does all of the
        // ML work. This uses the default values for the classifier.
        digitClassifierHelper = DigitClassifierHelper(
            context = context, digitClassifierListener = this
        )

        setupDigitCanvas()

        fragmentDigitCanvasBinding.btnClear.setOnClickListener {
            fragmentDigitCanvasBinding.digitCanvas.clearCanvas()
            fragmentDigitCanvasBinding.tvResults.text = ""
            fragmentDigitCanvasBinding.tvInferenceTime.text = ""
        }
    }

    override fun onDestroyView() {
        _fragmentDigitCanvasBinding = null
        super.onDestroyView()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupDigitCanvas() {
        with(fragmentDigitCanvasBinding.digitCanvas) {
            setStrokeWidth(70f)
            setColor(Color.WHITE)
            setBackgroundColor(Color.BLACK)
            setOnTouchListener { _, event ->
                // As we have interrupted DrawView's touch event, we first
                // need to pass touch events through to the instance for the drawing to show up
                onTouchEvent(event)

                // Then if user finished a touch event, run classification
                if (event.action == MotionEvent.ACTION_UP) {
                    // STEP 8a: classify
                    classifyDrawing()
                }
                true
            }
        }
    }

    // STEP 8b classify
    private fun classifyDrawing() {
        val bitmap = fragmentDigitCanvasBinding.digitCanvas.getBitmap()
        if (bitmap != null) { // Add null check
            digitClassifierHelper.classify(bitmap)
        }
    }

    // STEP 6 Set up listener
    override fun onError(error: String) {
        activity?.runOnUiThread {
            Toast.makeText(requireActivity(), error, Toast.LENGTH_SHORT).show()
            fragmentDigitCanvasBinding.tvResults.text = ""
        }
    }

    override fun onResults(
        results: ImageClassifierResult,
        inferenceTime: Long
    ) {
        activity?.runOnUiThread {
            fragmentDigitCanvasBinding.tvResults.text = results
                .classificationResult()
                .classifications()[0]
                .categories()[0]
                .categoryName()

            fragmentDigitCanvasBinding.tvInferenceTime.text = requireActivity()
                .getString(R.string.inference_time, inferenceTime.toString())
        }
    }
}