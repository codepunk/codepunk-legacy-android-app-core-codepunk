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
import android.util.AttributeSet
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codepunk.core.R

/**
 * A [RecyclerView] that grows based on its content, with an optional [maxHeight] attribute
 * to limit the height that the recycler view can grow.
 */
class AutoHeightRecyclerView :
    RecyclerView,
    OnGlobalLayoutListener {

    // region Properties

    /**
     * The maximum height of this recycler view.
     */
    var maxHeight: Int = Integer.MAX_VALUE
        set(value) {
            field = value
            invalidate()
        }

    // endregion Properties

    // region Constructors

    constructor(context: Context) : super(context) {
        initFromAttributes(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initFromAttributes(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleRes
    ) {
        initFromAttributes(context, attrs, defStyleRes)
    }

    /**
     * Add the global layout listener. We monitor layout via ViewTreeObserver rather than onLayout
     * because we want to be notified when layout is complete rather than in progress.
     */
    init {
        viewTreeObserver.addOnGlobalLayoutListener(this)
    }

    // endregion Constructors

    // region Inherited methods

    /**
     * Ensures that we don't grow larger than the value specified in [maxHeight].
     */
    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val newHeightSpec = when (maxHeight) {
            Integer.MAX_VALUE -> heightSpec
            else -> MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST)
        }
        super.onMeasure(widthSpec, newHeightSpec)
    }

    // endregion Inherited methods

    // region Implemented methods

    /**
     * Adjusts the LayoutParams height based on whether we can scroll vertically. If we can
     * scroll vertically, ensure that layoutParams.height is set to the max height. Otherwise,
     * set layoutParams.height to [WRAP_CONTENT] so that it shrinks and grows depending on the
     * content.
     */
    override fun onGlobalLayout() {
        val lp: ViewGroup.LayoutParams = layoutParams
        when (canScrollVertically()) {
            true -> if (lp.height < maxHeight) {
                lp.height = maxHeight
                layoutParams = lp
            }
            else -> if (lp.height != WRAP_CONTENT) {
                lp.height = WRAP_CONTENT
                layoutParams = lp
            }
        }
    }

    // endregion Implemented methods

    // region Methods

    /**
     * Initializes the AutoHeightRecyclerView based on style attributes.
     */
    private fun initFromAttributes(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = R.attr.autoHeightRecyclerViewStyle,
        defStyleRes: Int = 0
    ) {
        val a: TypedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.AutoHeightRecyclerView,
            defStyleAttr,
            defStyleRes
        )

        maxHeight = a.getDimensionPixelSize(
            R.styleable.AutoHeightRecyclerView_android_maxHeight,
            Integer.MAX_VALUE
        )

        a.recycle()
    }

    /**
     * Returns whether this recycler view can scroll vertically.
     */
    @Suppress("WEAKER_ACCESS")
    fun canScrollVertically(): Boolean {
        val linearLayoutManager: LinearLayoutManager? = layoutManager as? LinearLayoutManager
        val adapter: Adapter<ViewHolder>? = adapter
        return when {
            linearLayoutManager == null || adapter == null -> false
            linearLayoutManager.findLastCompletelyVisibleItemPosition() < adapter.itemCount - 1 ->
                true
            else -> false
        }
    }

}
