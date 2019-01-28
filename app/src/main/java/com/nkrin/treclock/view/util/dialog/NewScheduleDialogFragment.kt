package com.nkrin.treclock.view.util.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.widget.EditText
import android.widget.TextView
import com.nkrin.treclock.R

class NewScheduleDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity?.layoutInflater
        val view = inflater?.inflate(R.layout.fragment_new_schedule_dialog, null)
        val nameView = view?.findViewById<EditText>(R.id.schedule_title)
        val commentView = view?.findViewById<EditText>(R.id.schedule_comment)

        var id = 0
        val args = arguments
        if (args != null) {
            nameView?.setText(args.getString("title", ""), TextView.BufferType.NORMAL)
            commentView?.setText(args.getString("comment", ""), TextView.BufferType.NORMAL)
            id = args.getInt("id", 0)
        }

        builder.setView(view)
            .setPositiveButton(if (id == 0) "作成" else "更新") { dialog, _ ->
                if (dialog is Dialog) {
                    val titleTextView = dialog.findViewById<EditText>(R.id.schedule_title)
                    val commentTextView = dialog.findViewById<EditText>(R.id.schedule_comment)
                    val title = titleTextView.text.toString()
                    val comment = commentTextView.text.toString()
                    val listener = activity
                    if (listener is Listener) {
                        listener.onClickedScheduleDialogPositive(id, title, comment)
                    }
                }
            }
            .setNegativeButton("キャンセル") { dialog, _ -> dialog?.cancel() }
        return builder.create()
    }

    interface Listener {
        fun onClickedScheduleDialogPositive(id: Int, title: String, comment: String)
    }

    companion object {
        fun create(id: Int, title: String, comment: String): NewScheduleDialogFragment {
            val dialog = NewScheduleDialogFragment()
            val args = Bundle()
            args.putInt("id", id)
            args.putString("title", title)
            args.putString("comment", comment)
            dialog.arguments = args
            return dialog
        }
    }
}
