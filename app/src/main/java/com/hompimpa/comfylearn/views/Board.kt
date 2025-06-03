package com.hompimpa.comfylearn.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

private const val PENCIL_STROKE_WIDTH = 8f

class Board @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val mDrawnPaths =
        mutableListOf<Pair<Path, Paint>>()  // Pair of Path and Paint for color retention
    private var mPath: Path? = null
    private var mPaint: Paint = createPaint(Color.BLACK)
    private var currentColor = Color.BLACK

    // Set the current color for new paths
    fun setPenColor(newColor: Int) {
        currentColor = newColor
    }

    fun getPenColor(): Int {
        return currentColor
    }

    // Create a new Paint object with the current color
    private fun createPaint(color: Int): Paint {
        return Paint().apply {
            isAntiAlias = true
            isDither = true
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = PENCIL_STROKE_WIDTH
            this.color = color
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for ((path, paint) in mDrawnPaths) {
            canvas.drawPath(path, paint)  // Draw each path with its associated paint
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> touchDown(x, y)
            MotionEvent.ACTION_MOVE -> touchMove(x, y)
            MotionEvent.ACTION_UP -> touchUp(x, y)
        }
        invalidate()
        return true
    }

    private fun touchDown(x: Float, y: Float) {
        mPath = Path().apply {
            moveTo(x, y)
        }
        mPaint = createPaint(currentColor)  // Create a new Paint for each new path
        mDrawnPaths.add(Pair(mPath!!, mPaint))  // Add path and paint pair to list
    }

    private fun touchMove(x: Float, y: Float) {
        mPath?.lineTo(x, y)
    }

    private fun touchUp(x: Float, y: Float) {
        mPath?.lineTo(x, y)
        mPath = null  // Reset path after drawing
    }
}
