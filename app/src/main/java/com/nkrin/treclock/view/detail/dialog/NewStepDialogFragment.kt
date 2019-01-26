package com.nkrin.treclock.view.scheduler.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.widget.EditText
import android.widget.TextView
import com.nkrin.treclock.R
import java.time.Duration


class NewStepDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity?.layoutInflater
        val view = inflater?.inflate(R.layout.fragment_new_step_dialog, null)
        val nameView = view?.findViewById<EditText>(R.id.step_title)
        val durationView = view?.findViewById<EditText>(R.id.duration_minutes)

        var id = 0
        val args = arguments
        if (args != null) {
            nameView?.setText(args.getString("title", ""), TextView.BufferType.NORMAL)
            val durationMinutes = args.getInt("minutes", 0)
            if (durationMinutes != 0) {
                durationView?.setText("$durationMinutes", TextView.BufferType.NORMAL)
            }
            id = args.getInt("id", 0)
        }

        builder.setView(view)
            .setPositiveButton(if (id == 0) "作成" else "更新") { dialog, _ ->
                save(dialog, id)
                dialog?.cancel()
            }
            .setNegativeButton("キャンセル") { dialog, _ -> dialog?.cancel() }
        return builder.create()
    }

    private fun save(dialog: DialogInterface, id: Int) {
        if (dialog is Dialog) {
            val titleTextView = dialog.findViewById<EditText>(R.id.step_title)
            val durationTextView = dialog.findViewById<EditText>(R.id.duration_minutes)
            val title = titleTextView.text.toString()
            val duration = Duration.ofMinutes(java.lang.Long.parseLong(durationTextView.text.toString()))
            val listener = activity
            if (listener is Listener) {
                listener.onClickedDialogPositive(id, title, duration)
            }
        }
    }

    interface Listener {
        fun onClickedDialogPositive(id: Int, title: String, duration: Duration)
    }

    companion object {
        fun create(id: Int, title: String, minutes: Duration?): NewStepDialogFragment {
            val dialog = NewStepDialogFragment()
            val args = Bundle()
            args.putInt("id", id)
            args.putString("title", title)
            args.putInt("minutes", minutes?.toMinutes()?.toInt() ?: 0)
            dialog.arguments = args
            return dialog
        }
    }
}
