package com.nkrin.treclock.view.util.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.widget.TextView
import com.nkrin.treclock.R

class YesNoDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?) : Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity?.layoutInflater
        val view = inflater?.inflate(R.layout.fragment_yes_no_dialog, null)
        val messageView = view?.findViewById<TextView>(R.id.yes_no_dialog_message)

        var dialogId = ""
        var yesMessage = ""
        var noMessage = ""
        val args = arguments
        if (args != null) {
            dialogId = args.getString("dialog_id", "")
            messageView?.setText(args.getString("message", ""), TextView.BufferType.NORMAL)
            yesMessage = args.getString("yes_message", "はい")
            noMessage = args.getString("no_message", "いいえ")
        }

        builder.setView(view)
            .setPositiveButton(yesMessage) { _, _ ->
                val listener = activity
                if (listener is Listener) {
                    listener.onClickedYesNoDialogPositive(dialogId)
                }
            }
            .setNegativeButton(noMessage) { _, _ ->
                val listener = activity
                if (listener is Listener) {
                    listener.onClickedYesNoDialogNegative(dialogId)
                }
            }
        return builder.create()
    }

    interface Listener {
        fun onClickedYesNoDialogPositive(dialogId: String)
        fun onClickedYesNoDialogNegative(dialogId: String)
    }

    companion object {
        fun create(dialogId: String, message: String, yesMessage: String, noMessage: String): YesNoDialogFragment {
            val dialog = YesNoDialogFragment()
            val args = Bundle()
            args.putString("dialog_id", dialogId)
            args.putString("message", message)
            args.putString("yes_message", yesMessage)
            args.putString("no_message", noMessage)
            dialog.arguments = args
            return dialog
        }
    }}
