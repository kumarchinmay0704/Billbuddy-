package com.example.billbudddy.util

import android.content.Context
import android.graphics.Canvas
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs
import kotlin.math.min

class FlipboardLayoutManager(context: Context) : LinearLayoutManager(context) {
    
    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        super.onLayoutChildren(recycler, state)
        scrollVerticallyBy(0, recycler, state)
    }

    override fun scrollVerticallyBy(dy: Int, recycler: RecyclerView.Recycler?, state: RecyclerView.State?): Int {
        val scrolled = super.scrollVerticallyBy(dy, recycler, state)
        val midpoint = height / 2f
        val d0 = 0f
        val d1 = midpoint * 0.9f
        
        for (i in 0 until childCount) {
            val child = getChildAt(i) ?: continue
            val childMidpoint = (getDecoratedBottom(child) + getDecoratedTop(child)) / 2f
            val d = min(d1, abs(midpoint - childMidpoint))
            val scale = 1f - 0.15f * d/d1
            val alpha = 1f - 0.4f * d/d1
            
            // Calculate rotation based on position
            val rotation = when {
                childMidpoint > midpoint -> -45f * (d/d1)  // Above midpoint
                else -> 45f * (d/d1)  // Below midpoint
            }
            
            // Apply transformations
            child.apply {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
                rotationX = rotation
                translationZ = -d // Push further items back
            }
        }
        return scrolled
    }

    override fun canScrollVertically(): Boolean = true
} 