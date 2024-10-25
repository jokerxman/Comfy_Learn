package com.hompimpa.comfylearn.ui.custom

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class DrawingCanvas(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private val paint = Paint()
    private val path = Path()
    private var bitmap: Bitmap? = null
    private var canvasBitmap: Canvas? = null

    init {
        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 12f
        paint.isAntiAlias = true
    }

    fun setStrokeWidth(strokeWidth: Float) {
        paint.strokeWidth = strokeWidth
    }

    fun setColor(color: Int) {
        paint.color = color
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Initialize the bitmap and canvas when the view size changes
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvasBitmap = Canvas(bitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Draw the path onto both the view's canvas and the bitmap's canvas
        canvas.drawPath(path, paint)
        canvasBitmap?.drawPath(path, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(x, y)
                return true
            }

            MotionEvent.ACTION_MOVE -> path.lineTo(x, y)
            MotionEvent.ACTION_UP -> {}
        }
        invalidate()
        return true
    }

    fun clearCanvas() {
        path.reset()
        invalidate()
    }

    fun getBitmap(): Bitmap? {
        // Return a copy of the bitmap to avoid external modifications
        return bitmap?.copy(Bitmap.Config.ARGB_8888, false)
    }
}
