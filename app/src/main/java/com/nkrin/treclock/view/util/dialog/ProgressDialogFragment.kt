package com.nkrin.treclock.view.util.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.widget.ProgressBar
import com.nkrin.treclock.R
import com.nkrin.treclock.databinding.FragmentProgressDialogBinding
import com.nkrin.treclock.util.rx.RxLauncher
import io.reactivex.Completable
import org.jetbrains.anko.find
import java.util.concurrent.TimeUnit


class ProgressDialogFragment: DialogFragment() {

    private val rxLauncher = RxLauncher()
    private var progressBar: ProgressBar? = null
    private var startedTime: Long = 0
    private var showing = false

    private var message: String = ""

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(activity)
        val binding = DataBindingUtil.inflate<FragmentProgressDialogBinding>(
            inflater, R.layout.fragment_progress_dialog, null, false)

        val args = arguments
        if (args != null) {
            message = args.getString("message", "")
        }
        binding.message = message

        val builder = AlertDialog.Builder(activity)
        builder.setView(binding.root)
        return builder.create()
    }

    override fun onStart() {
        super.onStart()
        showing = true
        startedTime = System.currentTimeMillis()
        progressBar = dialog.find(R.id.progress)
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
            rxLauncher.launch {
                Completable.timer(delayedTime, TimeUnit.MILLISECONDS)
                    .subscribe { finish() }
            }
        } else {
            finish()
        }
    }

    private fun cancelWhenNotShowing() {
        rxLauncher.launch {
            Completable.timer(SHOW_MIN_MILLISECOND, TimeUnit.MILLISECONDS)
                .subscribe { finish() }
        }
    }

    private fun finish() {
        dismissAllowingStateLoss()
        showing = false
    }

    override fun onDestroy() {
        super.onDestroy()
        rxLauncher.dispose()
    }

    companion object {
        private const val SHOW_MIN_MILLISECOND = 500L

        fun create(message: String): ProgressDialogFragment {
            val dialog = ProgressDialogFragment()
            val args = Bundle()
            args.putString("message", message)
            dialog.arguments = args
            return dialog
        }
    }
}
