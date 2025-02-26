package com.example.billbudddy.Utils

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import kotlin.math.abs

class FlipboardLayoutManager(context: Context) : LinearLayoutManager(context) {
    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        super.onLayoutChildren(recycler, state)
        scrollHorizontallyBy(0, recycler, state)
    }

    override fun scrollHorizontallyBy(dx: Int, recycler: RecyclerView.Recycler?, state: RecyclerView.State?): Int {
        val orientation = orientation
        if (orientation == HORIZONTAL) {
            val scrolled = super.scrollHorizontallyBy(dx, recycler, state)
            val midpoint = width / 2f
            val d0 = 0f
            val d1 = 0.9f * midpoint
            val s0 = 1f
            val s1 = 0.8f
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                child?.let {
                    val childMidpoint = (getDecoratedRight(child) + getDecoratedLeft(child)) / 2f
                    val d = abs(midpoint - childMidpoint)
                    val scale = if (d <= d1) {
                        s0 + (s1 - s0) * (d - d0) / (d1 - d0)
                    } else {
                        s1
                    }
                    child.scaleX = scale
                    child.scaleY = scale
                }
            }
            return scrolled
        }
        return 0
    }

    override fun checkLayoutParams(lp: RecyclerView.LayoutParams?): Boolean {
        lp?.width = width - 2 * (width / 10)
        return true
    }
} 