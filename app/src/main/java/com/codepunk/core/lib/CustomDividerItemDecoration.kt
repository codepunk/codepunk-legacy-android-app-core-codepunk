/*
 * Copyright (C) 2018 Codepunk, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.codepunk.core.lib

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView

/**
 * A custom [DividerItemDecoration] that optionally omits the divider item decoration
 * for the last item in the [RecyclerView].
 */
class CustomDividerItemDecoration(
    context: Context,
    private val orientation: Int,
    private val showAfterLastItem: Boolean = true
) :
    DividerItemDecoration(context, orientation) {

    // region Properties

    /**
     * A reusable bucket for storing view bounds.
     */
    private val bounds = Rect()

    /**
     * A drawable representing the divider to draw in between recycler view items.
     */
    private var divider: Drawable? = null

    // endregion Properties

    // region Constructors

    /**
     * Gets the list divider drawable if set in the current theme.
     */
    init {
        val a: TypedArray = context.obtainStyledAttributes(ATTRS)
        divider = a.getDrawable(0)
        a.recycle()
    }

    // endregion Constructors

    // region Inherited methods

    /**
     * Captures when the drawable is set so we can access it later.
     */
    override fun setDrawable(drawable: Drawable) {
        super.setDrawable(drawable)
        divider = drawable
    }

    /**
     * If [showAfterLastItem] is false, then use empty item offsets. Otherwise, calculate
     * item offsets as normal.
     */
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val adapterPosition = parent.getChildAdapterPosition(view)
        if (adapterPosition < state.itemCount - 1 || showAfterLastItem) {
            super.getItemOffsets(outRect, view, parent, state)
        } else {
            outRect.setEmpty()
        }
    }

    /**
     * Draw a vertical or horizontal divider item decoration depending on the [orientation].
     */
    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.layoutManager == null) {
            return
        }

        divider?.run {
            if (orientation == VERTICAL) {
                drawVertical(canvas, parent, this)
            } else {
                drawHorizontal(canvas, parent, this)
            }
        }
    }

    // endregion Inherited methods

    // region Methods

    /**
     * Draws horizontal divider item decorations, optionally drawing any divider item decoration
     * for the last item in the [RecyclerView] depending on whether [showAfterLastItem] is set.
     */
    private fun drawHorizontal(canvas: Canvas, parent: RecyclerView, divider: Drawable) {
        canvas.save()
        val top: Int
        val bottom: Int
        if (parent.clipToPadding) {
            val left = parent.paddingLeft
            val right = parent.width - parent.paddingRight
            top = parent.paddingTop
            bottom = parent.height - parent.paddingBottom
            canvas.clipRect(left, top, right, bottom)
        } else {
            top = 0
            bottom = parent.height
        }

        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val itemCount = parent.adapter?.itemCount ?: 0
            val adapterPosition = parent.getChildAdapterPosition(child)
            if (adapterPosition < itemCount - 1) {
                parent.layoutManager?.getDecoratedBoundsWithMargins(child, bounds)
                val right = bounds.right + Math.round(child.translationX)
                val left = right - divider.intrinsicWidth
                divider.setBounds(left, top, right, bottom)
                divider.draw(canvas)
            }
        }

        canvas.restore()
    }

    /**
     * Draws vertical divider item decorations, optionally drawing any divider item decoration
     * for the last item in the [RecyclerView] depending on whether [showAfterLastItem] is set.
     */
    private fun drawVertical(canvas: Canvas, parent: RecyclerView, divider: Drawable) {
        canvas.save()
        val left: Int
        val right: Int
        if (parent.clipToPadding) {
            val top = parent.paddingTop
            val bottom = parent.height - parent.paddingBottom
            left = parent.paddingLeft
            right = parent.width - parent.paddingRight
            canvas.clipRect(left, top, right, bottom)
        } else {
            left = 0
            right = parent.width
        }

        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val itemCount = parent.adapter?.itemCount ?: 0
            val adapterPosition = parent.getChildAdapterPosition(child)
            if (adapterPosition < itemCount - 1) {
                parent.layoutManager?.getDecoratedBoundsWithMargins(child, bounds)
                val bottom = bounds.bottom + Math.round(child.translationY)
                val top = bottom - divider.intrinsicHeight
                divider.setBounds(left, top, right, bottom)
                divider.draw(canvas)
            }
        }

        canvas.restore()
    }

    // endregion Methods

    // region Companion object

    companion object {

        // region Properties

        /**
         * Integer array representing an attribute set containing [android.R.attr.listDivider].
         */
        @JvmStatic
        private val ATTRS: IntArray = IntArray(1) { android.R.attr.listDivider }

        // endregion Properties

    }

    // endregion Companion object
}
