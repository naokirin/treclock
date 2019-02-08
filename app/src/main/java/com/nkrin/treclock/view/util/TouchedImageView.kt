package com.nkrin.treclock.view.util

import android.content.Context
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet

class TouchedImageView(context: Context, attrs: AttributeSet)
    : AppCompatImageView(context, attrs) {

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}