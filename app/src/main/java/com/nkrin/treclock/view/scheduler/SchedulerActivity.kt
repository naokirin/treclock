package com.nkrin.treclock.view.scheduler

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Toast
import com.nkrin.treclock.R
import com.nkrin.treclock.domain.entity.Schedule
import com.nkrin.treclock.util.mvvm.Error
import com.nkrin.treclock.util.mvvm.Pending
import com.nkrin.treclock.util.mvvm.Success
import com.nkrin.treclock.view.scheduler.dialog.NewScheduleDialogFragment
import com.nkrin.treclock.view.util.ProgressDialogFragment
import kotlinx.android.synthetic.main.activity_scheduler.*
import org.koin.android.viewmodel.ext.android.viewModel

class SchedulerActivity : AppCompatActivity(), NewScheduleDialogFragment.Listener {

    private val schedulerViewModel: SchedulerViewModel by viewModel()
    private lateinit var schedulerList: RecyclerView

    private var progressDialog: ProgressDialogFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scheduler)
        setSupportActionBar(toolbar)
        setTitle(R.string.title_activity_scheduler)

        schedulerList = findViewById(R.id.scheduler_list)
        schedulerList.addItemDecoration(
            BackgroundItemDecoration(R.drawable.item_grey_background, R.drawable.item_white_background))

        schedulerViewModel.loadingEvents.observe(this, Observer {
            when(it) {
                is Success -> onLoaded()
                is Error -> onLoadedError()
            }
            if (it !is Pending) {
                progressDialog?.cancel()
            }
        })

        schedulerViewModel.addingEvents.observe(this, Observer {
            when(it) {
                is Success -> onAdding(it.value)
                is Error -> onAddingError()
            }

            if (it !is Pending) {
                progressDialog?.cancel()
            }
        })

        schedulerViewModel.removingEvents.observe(this, Observer {
            when(it) {
                is Success -> onRemoving(it.value)
                is Error -> onRemovingError()
            }
            if (it !is Pending) {
                progressDialog?.cancel()
            }
        })

        progressDialog = ProgressDialogFragment.create("Loading...")
        progressDialog?.show(supportFragmentManager, null)
        schedulerViewModel.load()
    }

    override fun onClickedPositive(title: String, comment: String) {
        if (title.isEmpty()) {
            Toast.makeText(this , "無名のスケジュールは作成できません", Toast.LENGTH_SHORT)
                .show()
        } else {
            addNewSchedule(title, comment)
            Toast.makeText(this , "スケジュールを追加しました", Toast.LENGTH_SHORT)
                .show()
        }
    }

    fun openDetail(tappedView: View, schedule: Schedule) {
        Snackbar.make(tappedView, "${schedule.name}", Snackbar.LENGTH_LONG)
            .setAction("Action", null).show()
    }

    private fun addNewSchedule(title: String, comment: String) {
        val adapter = schedulerList.adapter
        if (adapter is SchedulerRecycleViewAdapter) {
            schedulerViewModel.addNewSchedule(title, comment)
            progressDialog = ProgressDialogFragment.create("Saving...")
            progressDialog?.show(supportFragmentManager, null)
        }
    }

    private fun deleteSchedule(id: Int) {
        val adapter = schedulerList.adapter
        if (adapter is SchedulerRecycleViewAdapter) {
            schedulerViewModel.removeSchedule(id)
            progressDialog = ProgressDialogFragment.create("Saving...")
            progressDialog?.show(supportFragmentManager, null)
        }
    }

    private fun onLoaded() {
        val adapter = SchedulerRecycleViewAdapter(
            schedulerViewModel,
            object : SchedulerRecycleViewAdapter.RowListener {
                override fun onClickRow(tappedView: View, schedule: Schedule) {
                    this@SchedulerActivity.openDetail(tappedView, schedule)
                }
            },
            object: SchedulerRecycleViewAdapter.RemovingListener {
                override fun onClickRemoving(schedule: Schedule) {
                    this@SchedulerActivity.deleteSchedule(schedule.id)
                    Toast.makeText(this@SchedulerActivity, "スケジュールを削除しました", Toast.LENGTH_SHORT)
                        .show()
                }
            })
        val llm = LinearLayoutManager(this)

        schedulerList.setHasFixedSize(true)
        schedulerList.layoutManager = llm
        schedulerList.adapter = adapter

        scheduler_add.setOnClickListener {
            val newScheduleDialog = NewScheduleDialogFragment()
            newScheduleDialog.show(supportFragmentManager, "NewScheduleDialog")
        }
    }

    private fun onLoadedError() {
        Snackbar.make(schedulerList, "データが読み込めませんでした。再読込します。", Snackbar.LENGTH_LONG)
            .setAction("OK") { schedulerViewModel.load() }.show()
    }

    private fun onAdding(index: Any?) {
        if (index is Int) {
            schedulerList.adapter?.notifyItemInserted(index)
        }
    }

    private fun onAddingError() {
         Snackbar.make(schedulerList, "追加できませんでした。", Snackbar.LENGTH_LONG)
            .setAction("OK") { schedulerViewModel.load() }.show()
    }

    private fun onRemoving(index: Any?) {
        if (index is Int) {
            schedulerList.adapter?.notifyItemRemoved(index)
        }
    }

    private fun onRemovingError() {
        Snackbar.make(schedulerList, "削除できませんでした。", Snackbar.LENGTH_LONG)
            .setAction("OK") { schedulerViewModel.load() }.show()
    }
}
