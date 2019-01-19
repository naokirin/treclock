package com.nkrin.treclock.view.scheduler.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.widget.EditText
import com.nkrin.treclock.R

class NewScheduleDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity?.layoutInflater

        builder.setView(inflater?.inflate(R.layout.fragment_new_schedule_dialog, null))
            .setPositiveButton("作成") { dialog, _ ->
                if (dialog is Dialog) {
                    val titleTextView = dialog.findViewById<EditText>(R.id.schedule_title)
                    val commentTextView = dialog.findViewById<EditText>(R.id.schedule_comment)
                    val title = titleTextView.text.toString()
                    val comment = commentTextView.text.toString()
                    val listener = activity
                    if (listener is Listener) {
                        listener.onClickedPositive(title, comment)
                    }
                }
            }
            .setNegativeButton("キャンセル") { dialog, _ -> dialog?.cancel() }
        return builder.create()
    }

    interface Listener {
        fun onClickedPositive(title: String, comment: String)
    }
}
