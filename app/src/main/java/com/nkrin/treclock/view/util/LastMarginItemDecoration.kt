package com.nkrin.treclock.view.util

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

class LastMarginItemDecoration : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        val position = parent.getChildAdapterPosition(view)
        if (position == parent.adapter?.itemCount?.minus(1)) {
            outRect.bottom = 250
        }
        else {
            outRect.bottom = 0
        }
    }
}
