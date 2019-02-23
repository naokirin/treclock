package com.nkrin.treclock.view.util.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.databinding.BaseObservable
import android.databinding.Bindable
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import com.nkrin.treclock.BR
import com.nkrin.treclock.R
import com.nkrin.treclock.databinding.FragmentNewScheduleDialogBinding

class NewScheduleDialogFragment : DialogFragment() {

    private var data: BindingData = BindingData()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        var id = 0
        val args = arguments
        if (args != null) {
            data.title = args.getString("title", "")
            data.comment = args.getString("comment", "")
            id = args.getInt("id", 0)
        }

        val builder = AlertDialog.Builder(activity)
        val inflater = LayoutInflater.from(activity)
        val binding = DataBindingUtil.inflate<FragmentNewScheduleDialogBinding>(
            inflater, R.layout.fragment_new_schedule_dialog, null, false)
        val view = binding.root
        binding.data = data

        builder.setView(view)
            .setPositiveButton(if (id == 0) "作成" else "更新") { dialog, _ ->
                if (dialog is Dialog) {
                    val listener = activity
                    if (listener is Listener) {
                        listener.onClickedScheduleDialogPositive(id, data.title, data.comment)
                    }
                }
            }
            .setNegativeButton("キャンセル") { dialog, _ -> dialog?.cancel() }
        return builder.create()
    }

    interface Listener {
        fun onClickedScheduleDialogPositive(id: Int, title: String, comment: String)
    }

    class BindingData: BaseObservable() {

        var title: String = ""
            @Bindable get() = field
            @Bindable set(value) {
                field = value
                notifyPropertyChanged(BR.title)
            }

        var comment: String = ""
            @Bindable get() = field
            @Bindable set(value) {
                field = value
                notifyPropertyChanged(BR.comment)
            }
    }

    companion object {
        fun create(id: Int, title: String, comment: String): NewScheduleDialogFragment {
            val dialog = NewScheduleDialogFragment()
            dialog.arguments = Bundle().apply {
                putInt("id", id)
                putString("title", title)
                putString("comment", comment)
            }
            return dialog
        }
    }
}
