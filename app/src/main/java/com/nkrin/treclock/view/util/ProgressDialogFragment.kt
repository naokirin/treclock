package com.nkrin.treclock.view.util

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.widget.ProgressBar
import android.widget.TextView
import com.nkrin.treclock.R
import io.reactivex.Completable
import java.util.concurrent.TimeUnit


class ProgressDialogFragment: DialogFragment() {

    private var progressBar: ProgressBar? = null
    private var progressMessage: TextView? = null
    private var startedTime: Long = 0
    private var showing = false

    private var message: String = ""

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity?.layoutInflater
        builder.setView(inflater?.inflate(R.layout.fragment_progress_dialog, null))
        return builder.create()
    }

    override fun onStart() {
        super.onStart()
        showing = true
        startedTime = System.currentTimeMillis()
        progressBar = dialog.findViewById(R.id.progress)
        progressMessage = dialog.findViewById(R.id.progress_message)
        setMessage(message)
    }

    fun cancel() {
        if (showing) {
            if (progressBar != null) {
                cancelWhenShowing()
            } else {
                cancelWhenNotShowing()
            }
        }
    }

    private fun cancelWhenShowing() {
        val delayedTime = startedTime + SHOW_MIN_MILLISECOND - System.currentTimeMillis()
        if (delayedTime > 0) {
            Completable.timer(delayedTime, TimeUnit.MILLISECONDS)
                .subscribe { finish() }
        } else {
            finish()
        }
    }

    private fun cancelWhenNotShowing() {
        Completable.timer(SHOW_MIN_MILLISECOND, TimeUnit.MILLISECONDS)
            .subscribe { finish() }
    }

    private fun finish() {
        dismissAllowingStateLoss()
        showing = false
    }

    fun setMessage(m: String) {
        if (!showing) {
            message = m
        } else {
            if (progressMessage == null) {
                progressMessage = dialog.findViewById(R.id.progress_message)
            }
            progressMessage?.text = m
        }
    }

    companion object {
        private const val SHOW_MIN_MILLISECOND = 500L

        fun create(message: String): ProgressDialogFragment {
            val dialog = ProgressDialogFragment()
            dialog.setMessage(message)
            return dialog
        }
    }
}
