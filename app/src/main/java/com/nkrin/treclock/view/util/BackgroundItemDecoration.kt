package com.nkrin.treclock.view.util

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.support.annotation.DrawableRes
import android.view.View


class BackgroundItemDecoration(
    private val oddBackground: Int,
    private val evenBackground: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        val position = parent.getChildAdapterPosition(view)
        view.setBackgroundResource(if (position % 2 == 0) evenBackground else oddBackground)
    }
}