package com.hompimpa.comfylearn.views

import android.content.Context
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.content.withStyledAttributes
import androidx.core.view.isGone
import com.hompimpa.comfylearn.R
import kotlin.random.Random

class ScatteredPileLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    private val random = Random(System.currentTimeMillis())
    private val tempCurrentChildRect = RectF()
    private val placedChildRects = mutableListOf<RectF>()

    data class ChildState(
        var x: Float = 0f,
        var y: Float = 0f,
        var rotation: Float = 0f,
        var initialized: Boolean = false
    )

    private val childStates = mutableMapOf<View, ChildState>()

    var maxRotationDegrees: Float = 25f
    var positionSpreadFactor: Float = 0.8f
    private val maxPlacementAttempts: Int = 100

    init {
        attrs?.let {
            context.withStyledAttributes(
                it,
                R.styleable.ScatteredPileLayout,
                defStyleAttr,
                0
            ) {
                maxRotationDegrees = getFloat(R.styleable.ScatteredPileLayout_maxRotationDegrees, 25f)
                positionSpreadFactor = getFloat(R.styleable.ScatteredPileLayout_positionSpreadFactor, 0.8f).coerceIn(0.1f, 1.0f)
            }
        }
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }

    override fun generateLayoutParams(p: LayoutParams?): LayoutParams {
        return MarginLayoutParams(p)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var maxChildWidthWithPadding = 0
        var maxChildHeightWithPadding = 0

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.isGone) continue

            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0)

            val lp = child.layoutParams as MarginLayoutParams
            maxChildWidthWithPadding = maxOf(maxChildWidthWithPadding, child.measuredWidth + lp.leftMargin + lp.rightMargin)
            maxChildHeightWithPadding = maxOf(maxChildHeightWithPadding, child.measuredHeight + lp.topMargin + lp.bottomMargin)
        }

        val desiredWidth = resolveSize(paddingLeft + paddingRight + maxChildWidthWithPadding, widthMeasureSpec)
        val desiredHeight = resolveSize(paddingTop + paddingBottom + maxChildHeightWithPadding, heightMeasureSpec)
        setMeasuredDimension(desiredWidth, desiredHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val parentLeft = paddingLeft
        val parentTop = paddingTop
        val availableWidth = r - l - paddingLeft - paddingRight
        val availableHeight = b - t - paddingTop - paddingBottom

        if (availableWidth <= 0 || availableHeight <= 0) return

        placedChildRects.clear()

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.isGone) continue

            val lp = child.layoutParams as MarginLayoutParams
            var childWidth = child.measuredWidth
            var childHeight = child.measuredHeight

            if (childWidth == 0 || childHeight == 0) {
                child.measure(
                    MeasureSpec.makeMeasureSpec(availableWidth, MeasureSpec.AT_MOST),
                    MeasureSpec.makeMeasureSpec(availableHeight, MeasureSpec.AT_MOST)
                )
                childWidth = child.measuredWidth
                childHeight = child.measuredHeight
                if (childWidth == 0 || childHeight == 0) continue
            }

            val childState = childStates.getOrPut(child) { ChildState() }

            if (changed || !childState.initialized) {
                calculateAndSetChildPosition(childState, lp, childWidth, childHeight, parentLeft, parentTop, availableWidth, availableHeight)
            }
            applyChildState(child, childState, parentLeft + lp.leftMargin, parentTop + lp.topMargin)
        }
    }

    private fun calculateAndSetChildPosition(
        childState: ChildState,
        lp: MarginLayoutParams,
        childWidth: Int,
        childHeight: Int,
        parentLeft: Int,
        parentTop: Int,
        availableWidth: Int,
        availableHeight: Int
    ) {
        var attempt = 0
        var positionFound = false

        while (attempt < maxPlacementAttempts && !positionFound) {
            val effectiveSpreadWidth = (availableWidth * positionSpreadFactor).toInt()
            val effectiveSpreadHeight = (availableHeight * positionSpreadFactor).toInt()

            val constrainedPlacementWidth = (effectiveSpreadWidth - childWidth).coerceAtLeast(1)
            val constrainedPlacementHeight = (effectiveSpreadHeight - childHeight).coerceAtLeast(1)

            val randomOffsetX = if (constrainedPlacementWidth > 1) random.nextInt(constrainedPlacementWidth) else 0
            val randomOffsetY = if (constrainedPlacementHeight > 1) random.nextInt(constrainedPlacementHeight) else 0

            val proposedX = (parentLeft + lp.leftMargin + randomOffsetX).toFloat()
            val proposedY = (parentTop + lp.topMargin + randomOffsetY).toFloat()

            tempCurrentChildRect[proposedX, proposedY, proposedX + childWidth] = proposedY + childHeight

            // Check for overlaps with already placed children
            if (placedChildRects.none { RectF.intersects(tempCurrentChildRect, it) }) {
                childState.x = proposedX
                childState.y = proposedY
                childState.rotation = (random.nextFloat() * maxRotationDegrees * 2) - maxRotationDegrees
                childState.initialized = true

                placedChildRects.add(RectF(tempCurrentChildRect))
                positionFound = true
            }
            attempt++
        }

        // Fallback position if no valid position was found
        if (!positionFound) {
            childState.x = (parentLeft + lp.leftMargin + (availableWidth - childWidth) / 2f).toFloat()
            childState.y = (parentTop + lp.topMargin + (availableHeight - childHeight) / 2f).toFloat()
            childState.rotation = (random.nextFloat() * maxRotationDegrees * 2) - maxRotationDegrees
            childState.initialized = true
        }
    }

    private fun applyChildState(child: View, state: ChildState, layoutX: Int, layoutY: Int) {
        child.translationX = state.x - layoutX
        child.translationY = state.y - layoutY
        child.rotation = state.rotation

        child.layout(
            layoutX,
            layoutY,
            layoutX + child.measuredWidth,
            layoutY + child.measuredHeight
        )
    }

    fun getChildState(child: View): ChildState? {
        return childStates[child]
    }

    fun rescatterChildren() {
        childStates.values.forEach { it.initialized = false }
        requestLayout()
        invalidate()
    }

    fun bringChildToFrontZ(child: View) {
        removeView(child)
        addView(child)
        invalidate()
    }
}
