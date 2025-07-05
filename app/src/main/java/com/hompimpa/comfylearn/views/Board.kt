package com.hompimpa.comfylearn.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class Board @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    enum class Mode {
        DRAW,
        SHAPE_RECTANGLE
    }

    private var currentMode = Mode.DRAW
    private var drawingBitmap: Bitmap? = null
    private var drawingCanvas: Canvas? = null
    private var backgroundBitmap: Bitmap? = null
    private var mPaint: Paint = createPaint(Color.BLACK)
    private var mPath: Path? = null
    private var currentColor = Color.BLACK
    private var lastPenColor = Color.BLACK
    private var brushSize = 20f
    private var canvasBackgroundColor = Color.WHITE
    private var startX = 0f
    private var startY = 0f

    init {
        if (background == null) {
            setBackgroundColor(canvasBackgroundColor)
        }
    }

    fun setPenColor(newColor: Int) {
        currentMode = Mode.DRAW
        currentColor = newColor
        lastPenColor = newColor
    }

    fun getPenColor(): Int {
        return lastPenColor
    }

    fun setBrushSize(newSize: Float) {
        brushSize = newSize
    }

    fun setEraserMode(isErasing: Boolean) {
        currentMode = Mode.DRAW
        currentColor = if (isErasing) {
            Color.TRANSPARENT
        } else {
            lastPenColor
        }
    }

    fun setDrawingMode(mode: Mode) {
        currentMode = mode
    }

    fun setBackgroundImage(bitmap: Bitmap) {
        backgroundBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)
        clearDrawing()
        invalidate()
    }

    fun getDrawingBitmap(): Bitmap {
        val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val resultCanvas = Canvas(resultBitmap)
        resultCanvas.drawColor(canvasBackgroundColor)
        drawingBitmap?.let { resultCanvas.drawBitmap(it, 0f, 0f, null) }
        backgroundBitmap?.let { resultCanvas.drawBitmap(it, 0f, 0f, null) }
        return resultBitmap
    }

    fun clear() {
        backgroundBitmap = null
        clearDrawing()
    }

    private fun clearDrawing() {
        drawingBitmap?.eraseColor(Color.TRANSPARENT)
        invalidate()
    }

    private fun createPaint(color: Int): Paint {
        return Paint().apply {
            isAntiAlias = true
            isDither = true
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = brushSize
            this.color = color
            xfermode = if (color == Color.TRANSPARENT) PorterDuffXfermode(PorterDuff.Mode.SRC) else null
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            drawingBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            drawingCanvas = Canvas(drawingBitmap!!)
        }
        backgroundBitmap?.let {
            backgroundBitmap = Bitmap.createScaledBitmap(it, w, h, false)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawingBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }
        mPath?.let {
            canvas.drawPath(it, mPaint)
        }
        backgroundBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (currentMode) {
            Mode.DRAW -> handleDrawEvent(event, x, y)
            Mode.SHAPE_RECTANGLE -> handleShapeEvent(event, x, y)
        }
        invalidate()
        return true
    }

    private fun handleDrawEvent(event: MotionEvent, x: Float, y: Float) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mPath = Path().apply { moveTo(x, y) }
                mPaint = createPaint(currentColor)
            }
            MotionEvent.ACTION_MOVE -> {
                mPath?.lineTo(x, y)
            }
            MotionEvent.ACTION_UP -> {
                mPath?.let {
                    drawingCanvas?.drawPath(it, mPaint)
                }
                mPath = null
            }
        }
    }

    private fun handleShapeEvent(event: MotionEvent, x: Float, y: Float) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = x
                startY = y
            }
            MotionEvent.ACTION_UP -> {
                val rectPath = Path()
                rectPath.addRect(startX, startY, x, y, Path.Direction.CW)
                drawingCanvas?.drawPath(rectPath, createPaint(currentColor))
                currentMode = Mode.DRAW
            }
        }
    }
}
