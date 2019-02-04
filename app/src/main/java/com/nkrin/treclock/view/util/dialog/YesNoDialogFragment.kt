package com.nkrin.treclock.view.util.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import com.nkrin.treclock.R
import com.nkrin.treclock.databinding.FragmentYesNoDialogBinding

class YesNoDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?) : Dialog {

        var dialogId = ""
        var message = ""
        var yesMessage = getString(R.string.yes_button)
        var noMessage = getString(R.string.no_button)
        val args = arguments
        if (args != null) {
            dialogId = args.getString("dialog_id", "")
            message = args.getString("message", "")
            yesMessage = args.getString("yes_message", yesMessage)
            noMessage = args.getString("no_message", noMessage)
        }

        val builder = AlertDialog.Builder(activity)
        val inflater = LayoutInflater.from(activity)
        val binding = DataBindingUtil.inflate<FragmentYesNoDialogBinding>(
            inflater, R.layout.fragment_yes_no_dialog, null, false)
        val view = binding.root
        binding.message = message

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
