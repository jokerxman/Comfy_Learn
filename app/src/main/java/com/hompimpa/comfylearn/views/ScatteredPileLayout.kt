package com.hompimpa.comfylearn.views

import android.content.Context
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
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

    private val random = Random(System.currentTimeMillis()) // Or System.nanoTime() for more variance
    private val tempCurrentChildRect = RectF()
    private val tempCheckRectWithPadding = RectF()
    private val tempFallbackRect = RectF()
    private val placedChildRects = mutableListOf<RectF>()

    // Child state management (as you had it)
    data class ChildState(
        var x: Float = 0f,
        var y: Float = 0f,
        var rotation: Float = 0f,
        var initialized: Boolean = false, // True if layout calculation has set this state
        var lastLayoutWidth: Int = 0,    // To detect if parent width changed significantly for this child
        var lastLayoutHeight: Int = 0,    // To detect if parent height changed significantly for this child
        var isUsed: Boolean = false
    )

    private val childStates = mutableMapOf<View, ChildState>()

    // --- Configurable Properties for Layout ---
    var maxRotationDegrees: Float = 25f
    var positionSpreadFactor: Float = 0.8f // Now from 0.0 to 1.0. Higher means more spread.
    // Was 0.6f, increasing for more spread.
    var paddingBetweenViews: Int = 10     // In pixels. Minimum distance between view bounds.
    // Adjust this value as needed.
    private val maxPlacementAttempts: Int = 100 // Attempts to place a child without overlap

    init {
        attrs?.let {
            context.withStyledAttributes(
                it,
                R.styleable.ScatteredPileLayout,
                defStyleAttr,
                0
            ) {
                maxRotationDegrees = getFloat(
                    R.styleable.ScatteredPileLayout_maxRotationDegrees,
                    25f
                )
                positionSpreadFactor = getFloat(
                    R.styleable.ScatteredPileLayout_positionSpreadFactor,
                    0.8f // Default to new spread factor
                ).coerceIn(0.1f, 1.0f) // Ensure it's within a reasonable range

                // You could add paddingBetweenViews to styleable if you want
                // paddingBetweenViews = typedArray.getDimensionPixelSize(
                //     R.styleable.ScatteredPileLayout_paddingBetweenViews,
                //     10
                // )

            }
        }
    }

    // --- LayoutParams Handling (remains the same) ---
    override fun generateDefaultLayoutParams(): LayoutParams {
        return MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }

    override fun generateLayoutParams(p: LayoutParams?): LayoutParams {
        return MarginLayoutParams(p)
    }

    override fun checkLayoutParams(p: LayoutParams?): Boolean {
        return p is MarginLayoutParams
    }

    // --- Child State Management (remains the same) ---
    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)
        child?.let {
            if (!childStates.containsKey(it)) {
                childStates[it] = ChildState()
            }
        }
    }

    override fun onViewRemoved(child: View?) {
        child?.let {
            childStates.remove(it)
        }
        super.onViewRemoved(child)
    }

    // --- Measurement (remains largely the same, but ensure children are measured) ---
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var maxChildWidthWithPadding = 0
        var maxChildHeightWithPadding = 0

        // Measure all children
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.isGone) continue

            // Ensure child is measured. This is important before onLayout.
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0)

            val lp = child.layoutParams as MarginLayoutParams
            maxChildWidthWithPadding =
                maxOf(maxChildWidthWithPadding, child.measuredWidth + lp.leftMargin + lp.rightMargin)
            maxChildHeightWithPadding =
                maxOf(maxChildHeightWithPadding, child.measuredHeight + lp.topMargin + lp.bottomMargin)
        }

        val desiredWidth = resolveSize(paddingLeft + paddingRight + maxChildWidthWithPadding, widthMeasureSpec)
        // For height, ensure enough space for at least one item, or use the parent's suggestion.
        // The old totalChildrenHeight logic might not be ideal for a scattered layout.
        val desiredHeight = resolveSize(
            paddingTop + paddingBottom + maxChildHeightWithPadding, // Enough for at least the tallest child
            heightMeasureSpec
        )
        setMeasuredDimension(desiredWidth, desiredHeight)

        // After measurement, if the layout size has changed, mark children for re-positioning
        // This is a more granular check than just 'changed' in onLayout
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED &&
            MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.UNSPECIFIED) {
            val currentLayoutWidth = MeasureSpec.getSize(widthMeasureSpec)
            val currentLayoutHeight = MeasureSpec.getSize(heightMeasureSpec)
            childStates.values.forEach { state ->
                // If layout size changed meaningfully, re-initialize
                if (state.lastLayoutWidth != currentLayoutWidth || state.lastLayoutHeight != currentLayoutHeight) {
                    state.initialized = false
                    state.lastLayoutWidth = currentLayoutWidth
                    state.lastLayoutHeight = currentLayoutHeight
                }
            }
        }
    }


    // --- Layout (THIS IS THE CORE MODIFICATION) ---
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val parentLeft = paddingLeft
        val parentTop = paddingTop
        val parentRight = r - l - paddingRight
        val parentBottom = b - t - paddingBottom
        val availableWidth = parentRight - parentLeft
        val availableHeight = parentBottom - parentTop

        if (availableWidth <= 0 || availableHeight <= 0) {
            return // No space to layout
        }

        // Clear previously placed rects for this layout pass
        // If using a pool for these RectF objects, release them here before clearing.
        placedChildRects.clear()

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.isGone) continue

            val lp = child.layoutParams as MarginLayoutParams
            var childWidth = child.measuredWidth
            var childHeight = child.measuredHeight

            // Ensure child is measured (though ideally done in onMeasure)
            if (childWidth == 0 || childHeight == 0) {
                child.measure(
                    MeasureSpec.makeMeasureSpec(availableWidth, MeasureSpec.AT_MOST),
                    MeasureSpec.makeMeasureSpec(availableHeight, MeasureSpec.AT_MOST)
                )
                childWidth = child.measuredWidth
                childHeight = child.measuredHeight
                if (childWidth == 0 || childHeight == 0) continue // Skip if still unmeasurable
            }

            val childState = childStates.getOrPut(child) {
                Log.w("ScatteredPileLayout", "ChildState created during onLayout for $child.")
                ChildState()
            }

            // Determine if this child needs its position recalculated
            if (changed || !childState.initialized) {
                calculateAndSetChildPosition(
                    child,
                    childState,
                    lp,
                    childWidth,
                    childHeight,
                    parentLeft,
                    parentTop,
                    availableWidth,
                    availableHeight
                )
            }

            // Apply the final state (position and rotation) to the child view
            applyChildState(child, childState, parentLeft + lp.leftMargin, parentTop + lp.topMargin)
        }
    }

    private fun calculateAndSetChildPosition(
        child: View, // For logging
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
            // Step 1: Propose a random position
            val effectiveSpreadWidth = (availableWidth * positionSpreadFactor).toInt()
            val effectiveSpreadHeight = (availableHeight * positionSpreadFactor).toInt()

            val constrainedPlacementWidth = (effectiveSpreadWidth - childWidth).coerceAtLeast(1)
            val constrainedPlacementHeight = (effectiveSpreadHeight - childHeight).coerceAtLeast(1)

            val randomOffsetX = if (constrainedPlacementWidth > 1) random.nextInt(constrainedPlacementWidth) else 0
            val randomOffsetY = if (constrainedPlacementHeight > 1) random.nextInt(constrainedPlacementHeight) else 0

            val baseOffsetX = ((availableWidth - effectiveSpreadWidth) / 2f).toInt().coerceAtLeast(0)
            val baseOffsetY = ((availableHeight - effectiveSpreadHeight) / 2f).toInt().coerceAtLeast(0)

            val proposedX = (parentLeft + lp.leftMargin + baseOffsetX + randomOffsetX).toFloat()
            val proposedY = (parentTop + lp.topMargin + baseOffsetY + randomOffsetY).toFloat()

            // Reuse tempCurrentChildRect for the proposed position
            tempCurrentChildRect.set(proposedX, proposedY, proposedX + childWidth, proposedY + childHeight)

            // Step 2: Check for overlaps
            var overlaps = false
            for (placedRect in placedChildRects) {
                // Reuse tempCheckRectWithPadding
                tempCheckRectWithPadding.set(
                    placedRect.left - paddingBetweenViews,
                    placedRect.top - paddingBetweenViews,
                    placedRect.right + paddingBetweenViews,
                    placedRect.bottom + paddingBetweenViews
                )
                if (RectF.intersects(tempCurrentChildRect, tempCheckRectWithPadding)) {
                    overlaps = true
                    break
                }
            }

            // Step 3: If no overlap, accept position
            if (!overlaps) {
                childState.x = proposedX
                childState.y = proposedY
                childState.rotation = (random.nextFloat() * maxRotationDegrees * 2) - maxRotationDegrees
                childState.initialized = true
                childState.lastLayoutWidth = availableWidth + this.paddingLeft + this.paddingRight // Use 'this' for ViewGroup padding
                childState.lastLayoutHeight = availableHeight + this.paddingTop + this.paddingBottom

                placedChildRects.add(RectF(tempCurrentChildRect)) // Create a copy for the list
                positionFound = true
            }
            attempt++
        }

        // Step 4: Fallback if no position found
        if (!positionFound) {
            Log.w("ScatteredPileLayout", "Could not place child '$child' without overlap after $maxPlacementAttempts attempts. Placing at default (centered).")
            childState.x = (parentLeft + lp.leftMargin + (availableWidth - childWidth) / 2f).toFloat()
            childState.y = (parentTop + lp.topMargin + (availableHeight - childHeight) / 2f).toFloat()
            childState.rotation = (random.nextFloat() * maxRotationDegrees * 2) - maxRotationDegrees
            childState.initialized = true

            tempFallbackRect.set(childState.x, childState.y, childState.x + childWidth, childState.y + childHeight)
            placedChildRects.add(RectF(tempFallbackRect)) // Create a copy for the list
        }
    }

    /**
     * Applies the stored state (translation, rotation, and layout bounds) to the child view.
     */
    private fun applyChildState(child: View, state: ChildState, layoutX: Int, layoutY: Int) {
        child.translationX = state.x - layoutX // Adjust for child's layout origin
        child.translationY = state.y - layoutY   // Adjust for child's layout origin
        child.rotation = state.rotation

        // The child.layout() positions its bounds within the parent.
        // Since we are using translationX/Y, the layout coords are relative to where it would be without translation.
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

    // --- Public Methods to Control Layout ---
    fun rescatterChildren() {
        childStates.values.forEach { it.initialized = false } // Mark all for re-calculation
        requestLayout() // Triggers onMeasure and onLayout
        invalidate()    // Triggers redraw
    }

    fun requestChildLayoutUpdate(child: View, forceRecalculatePosition: Boolean = false) {
        val childState = childStates[child]
        if (childState != null) {
            if (forceRecalculatePosition) {
                childState.initialized = false // Mark for re-calculation of position
            }
            // If not forcing recalculate, we assume its current x, y, rotation are what we want to keep
            // or have been set externally (e.g., by animation returning it).
            // The layout pass will then use these values.
            requestLayout()
            invalidate()
        } else {
            // Child not found in states, likely needs a full rescatter if it's supposed to be here
            Log.w("ScatteredPileLayout", "requestChildLayoutUpdate called for a view not in childStates. Rescattering all.")
            rescatterChildren()
        }
    }

    // Bring to front (remains the same)
    fun bringChildToFrontVisually(child: View) {
        if (indexOfChild(child) < 0) {
            Log.w("ScatteredPileLayout", "Attempted to bring a view to front that is not a child.")
            return
        }
        super.bringChildToFront(child)
        invalidate() // Good to have invalidate here too
    }
}