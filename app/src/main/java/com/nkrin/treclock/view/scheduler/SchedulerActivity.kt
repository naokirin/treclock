package com.nkrin.treclock.view.scheduler

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import com.nkrin.treclock.R
import com.nkrin.treclock.domain.entity.Schedule
import com.nkrin.treclock.util.mvvm.Error
import com.nkrin.treclock.util.mvvm.Pending
import com.nkrin.treclock.util.mvvm.Success
import com.nkrin.treclock.util.time.TimeProvider
import com.nkrin.treclock.view.detail.DetailActivity
import com.nkrin.treclock.view.util.dialog.NewScheduleDialogFragment
import com.nkrin.treclock.view.util.BackgroundItemDecoration
import com.nkrin.treclock.view.util.ProgressDialogFragment
import kotlinx.android.synthetic.main.activity_scheduler.*
import org.jetbrains.anko.intentFor
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class SchedulerActivity : AppCompatActivity(), NewScheduleDialogFragment.Listener {

    private val schedulerViewModel: SchedulerViewModel by viewModel()
    private val timeProvider: TimeProvider by inject()
    private lateinit var schedulerList: RecyclerView

    private var progressDialog: ProgressDialogFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scheduler)
        setSupportActionBar(toolbar)
        setTitle(R.string.title_activity_scheduler)

        schedulerList = findViewById(R.id.scheduler_list)
        schedulerList.addItemDecoration(
            BackgroundItemDecoration(
                R.drawable.item_grey_background,
                R.drawable.item_white_background
            )
        )

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
                is Success -> onAdded(it.value)
                is Error -> onAddedError()
            }

            if (it !is Pending) {
                progressDialog?.cancel()
            }
        })

        schedulerViewModel.removingEvents.observe(this, Observer {
            when(it) {
                is Success -> onRemoved(it.value)
                is Error -> onRemovedError()
            }
            if (it !is Pending) {
                progressDialog?.cancel()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        progressDialog = ProgressDialogFragment.create("Loading...")
        progressDialog?.show(supportFragmentManager, null)
        schedulerViewModel.load()
    }

    override fun onClickedScheduleDialogPositive(id: Int, title: String, comment: String) {
        if (title.isEmpty()) {
            Toast.makeText(this , "無名のスケジュールは作成できません", Toast.LENGTH_SHORT)
                .show()
        } else {
            addNewSchedule(title, comment)
        }
    }

    fun openDetail(schedule: Schedule) {
        overridePendingTransition(R.anim.child_activity_enter_anim, R.anim.child_activity_exit_anim)
        startActivity(intentFor<DetailActivity>().putExtra("schedule_id", schedule.id))
    }

    private fun addNewSchedule(title: String, comment: String) {
        val adapter = schedulerList.adapter
        if (adapter is SchedulerRecycleViewAdapter) {
            schedulerViewModel.addNewSchedule(title, comment)
            progressDialog = ProgressDialogFragment.create("Saving...")
            progressDialog?.show(supportFragmentManager, null)
        }
    }

    private fun removeSchedule(id: Int) {
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
            timeProvider,
            object : SchedulerRecycleViewAdapter.RowListener {
                override fun onClickRow(tappedView: View, schedule: Schedule) {
                    this@SchedulerActivity.openDetail(schedule)
                }

                override fun onClickAction(tappedView: View, schedule: Schedule) {
                    with(PopupMenu(applicationContext, tappedView)) {
                        val inflater = menuInflater
                        inflater.inflate(R.menu.menu_scheduler_item, menu)
                        val deleteItem = menu.findItem(R.id.menu_scheduler_item_delete)
                        deleteItem.isVisible = !schedule.played(timeProvider.now())
                        setOnMenuItemClickListener {
                            when (it.itemId) {
                                R.id.menu_scheduler_item_delete -> {
                                    this@SchedulerActivity.removeSchedule(schedule.id)
                                }
                            }
                            return@setOnMenuItemClickListener true
                        }
                        show()
                    }
                }
            }
        )
        val llm = LinearLayoutManager(this)

        schedulerList.let {
            it.setHasFixedSize(true)
            it.layoutManager = llm
            it.adapter = adapter
        }

        val ith = ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: ViewHolder, target: ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
                    val fromPos = viewHolder.adapterPosition
                    val schedules = schedulerViewModel.list
                    if (schedules != null && schedules.size > fromPos) {
                        if (schedulerViewModel.list[fromPos].played(timeProvider.now())) {
                            return
                        }
                        removeSchedule(schedules[fromPos].id)
                    }
                }

                override fun getSwipeDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                    val pos = viewHolder.adapterPosition
                    val schedules = schedulerViewModel.list
                    if (schedules.size > pos && schedules[pos].played(timeProvider.now())) {
                        return 0
                    }
                    return super.getSwipeDirs(recyclerView, viewHolder)
                }
            })
            ith.attachToRecyclerView(schedulerList)

        schedulerList.setHasFixedSize(true)
        schedulerList.layoutManager = llm
        schedulerList.adapter = adapter

        scheduler_add.setOnClickListener {
            val newScheduleDialog = NewScheduleDialogFragment.create(0, "", "")
            newScheduleDialog.show(supportFragmentManager, "NewScheduleDialog")
        }
    }

    private fun onLoadedError() {
        Snackbar.make(schedulerList, "データが読み込めませんでした。再読込します。", Snackbar.LENGTH_LONG)
            .setAction("OK") { schedulerViewModel.load() }.show()
    }

    private fun onAdded(index: Any?) {
        if (index is Int) {
            schedulerList.adapter?.notifyItemInserted(index)
            Toast.makeText(this , "スケジュールを追加しました", Toast.LENGTH_SHORT)
                .show()
        } else {
            onAddedError()
        }
    }

    private fun onAddedError() {
         Snackbar.make(schedulerList, "追加できませんでした。", Snackbar.LENGTH_LONG)
            .setAction("OK") { schedulerViewModel.load() }.show()
    }

    private fun onRemoved(index: Any?) {
        if (index is Int) {
            schedulerList.adapter?.notifyItemRemoved(index)
            Toast.makeText(this@SchedulerActivity, "スケジュールを削除しました", Toast.LENGTH_SHORT)
                .show()
        } else {
            onRemovedError()
        }
    }

    private fun onRemovedError() {
        Snackbar.make(schedulerList, "削除できませんでした。", Snackbar.LENGTH_LONG)
            .setAction("OK") { schedulerViewModel.load() }.show()
    }
}
