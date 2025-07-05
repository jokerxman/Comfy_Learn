package com.hompimpa.comfylearn.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.VectorDrawable
import android.util.AttributeSet
import android.view.View

class VectorImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private var vectorDrawable: VectorDrawable? = null
    private var desiredWidth: Int = 100 // Desired width for the vector drawable

    fun setVectorDrawable(drawable: VectorDrawable) {
        this.vectorDrawable = drawable
        requestLayout()
        invalidate()
    }

    fun setDesiredWidth(width: Int) {
        this.desiredWidth = width
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height =
            (vectorDrawable?.intrinsicHeight ?: 0) * desiredWidth / (vectorDrawable?.intrinsicWidth
                ?: 1)
        setMeasuredDimension(desiredWidth, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        vectorDrawable?.let {
            // Calculate the top position to align the drawable to the bottom
            val top = height - it.intrinsicHeight * width / it.intrinsicWidth
            it.setBounds(0, top, width, height)
            it.draw(canvas)
        }
    }
}