package com.nkrin.treclock.view.detail.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.databinding.BaseObservable
import android.databinding.Bindable
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import com.nkrin.treclock.BR
import com.nkrin.treclock.R
import com.nkrin.treclock.databinding.FragmentNewStepDialogBinding
import java.time.Duration


class NewStepDialogFragment : DialogFragment() {

    private var dataNewStepDialog: NewStepDialogBindingData = NewStepDialogBindingData()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var id = 0
        val args = arguments
        if (args != null) {
            id = args.getInt("id", 0)
            dataNewStepDialog.title = args.getString("title", "")
            dataNewStepDialog.duration = "${args.getInt("minutes", 0)}"
        }

        val builder = AlertDialog.Builder(activity)
        val inflater = LayoutInflater.from(activity)
        val binding = DataBindingUtil.inflate<FragmentNewStepDialogBinding>(
            inflater, R.layout.fragment_new_step_dialog, null, false)
        val view = binding.root
        binding.data = dataNewStepDialog

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
            val title = dataNewStepDialog.title
            val durationMinutes = dataNewStepDialog.duration
            val duration = if (!durationMinutes.isEmpty()) {
                Duration.ofMinutes(java.lang.Long.parseLong(durationMinutes))
            } else {
                Duration.ZERO
            }
            val listener = activity
            if (listener is Listener) {
                listener.onClickedStepDialogPositive(id, title, duration)
            }
        }
    }

    interface Listener {
        fun onClickedStepDialogPositive(id: Int, title: String, duration: Duration?)
    }

    class NewStepDialogBindingData: BaseObservable() {
        var title: String = ""
            @Bindable get() = field
            @Bindable set(value) {
                field = value
                notifyPropertyChanged(BR.title)
            }

        var duration: String = ""
            @Bindable get() = field
            @Bindable set(value) {
                field = value
                notifyPropertyChanged(BR.duration)
            }
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
