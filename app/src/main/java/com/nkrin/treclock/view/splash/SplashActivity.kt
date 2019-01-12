package com.nkrin.treclock.view.splash

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.AnimationUtils
import com.nkrin.treclock.R
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        showLoading()
    }

    private fun showLoading() {
        val animation =
            AnimationUtils.loadAnimation(
                applicationContext,
                R.anim.repeated_blinking_animation
            )
        splashIcon.startAnimation(animation)
    }
}
