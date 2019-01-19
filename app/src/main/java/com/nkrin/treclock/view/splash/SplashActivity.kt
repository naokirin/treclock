package com.nkrin.treclock.view.splash

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.nkrin.treclock.R
import com.nkrin.treclock.util.mvvm.Error
import com.nkrin.treclock.util.mvvm.Pending
import com.nkrin.treclock.util.mvvm.Success
import com.nkrin.treclock.view.scheduler.SchedulerActivity
import kotlinx.android.synthetic.main.activity_splash.*
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.clearTop
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask
import org.koin.android.viewmodel.ext.android.viewModel

class SplashActivity : AppCompatActivity() {

    private val splashViewModel: SplashViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        splashViewModel.events.observe(this, Observer { event ->
            when(event) {
                is Pending -> {}
                is Success -> complete()
                is Error -> showError(event.error)
            }
        })
        showLoading()
    }

    private fun showLoading() {
        val background = findViewById<ImageView>(R.id.splash_back)
        val id = resources.getIdentifier("splash_back${(1..9).random()}", "drawable", packageName)
        background.setImageResource(id)
        val animation =
            AnimationUtils.loadAnimation(
                applicationContext,
                R.anim.repeated_blinking_animation
            )
        splashIcon.startAnimation(animation)
        splashViewModel.load()
    }

    private fun complete() {
        splashIcon.visibility = View.GONE
        //splashIconFail.visibility = View.VISIBLE
        Snackbar.make(splash, "SplashActivity finished", Snackbar.LENGTH_INDEFINITE)
            .show()
    }

    private fun showError(error: Throwable) {
        splashIcon.visibility = View.GONE
        //splashIconFail.visibility = View.VISIBLE
        Snackbar.make(splash, "SplashActivity got error : $error", Snackbar.LENGTH_INDEFINITE)
            .setAction("Retry") {
                splashViewModel.load()
            }
            .show()
    }
}
