package com.nkrin.treclock.view.detail

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.support.v7.widget.Toolbar
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.nkrin.treclock.R
import com.nkrin.treclock.domain.entity.Schedule
import com.nkrin.treclock.domain.entity.Step
import com.nkrin.treclock.util.mvvm.Error
import com.nkrin.treclock.util.mvvm.Success
import com.nkrin.treclock.view.scheduler.DetailRecycleViewAdapter
import com.nkrin.treclock.view.util.BackgroundItemDecoration
import com.nkrin.treclock.view.util.ProgressDialogFragment
import com.nkrin.treclock.view.util.dialog.NewScheduleDialogFragment
import com.nkrin.treclock.view.util.dialog.NewStepDialogFragment
import com.nkrin.treclock.view.util.dialog.YesNoDialogFragment
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*
import org.koin.android.viewmodel.ext.android.viewModel
import java.time.Duration

class DetailActivity :
    AppCompatActivity(),
    NewStepDialogFragment.Listener, NewScheduleDialogFragment.Listener, YesNoDialogFragment.Listener {

    private val detailViewModel: DetailViewModel by viewModel()
    private var progressDialog: ProgressDialogFragment? = null
    private lateinit var detailList: RecyclerView

    private var scheduleId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        detailList = findViewById(R.id.detail_list)
        detailList.addItemDecoration(
            BackgroundItemDecoration(R.drawable.item_grey_background, R.drawable.item_white_background)
        )

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        detailViewModel.loadingEvents.observe(this, Observer {
            when (it) {
                is Success -> onLoaded(it.value)
                is Error -> onLoadedError()
            }
        })

        detailViewModel.addingEvents.observe(this, Observer {
            when (it) {
                is Success -> onAdded(it.value)
                is Error -> onAddedError()
            }
        })

        detailViewModel.removingEvents.observe(this, Observer {
            when (it) {
                is Success -> onRemoved(it.value)
                is Error -> onRemovedError()
            }
        })

        detailViewModel.updatingEvents.observe(this, Observer {
            when (it) {
                is Success -> onUpdated(it.value)
                is Error -> onUpdatedError()
            }
        })

        detailViewModel.removingScheduleEvents.observe(this, Observer {
            onRemovedSchedule()
        })
    }

    override fun onResume() {
        super.onResume()
        scheduleId = intent?.getIntExtra("schedule_id", 0) ?: 0
        progressDialog = ProgressDialogFragment.create("Loading...")
        progressDialog?.show(supportFragmentManager, null)

        detailViewModel.loadSchedule(scheduleId)
    }

    override fun onPause() {
        super.onPause()
        detailViewModel.storeSchedule()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_detail_edit -> {
                val schedule = detailViewModel.schedule
                if (schedule != null) {
                    val dialog = NewScheduleDialogFragment.create(schedule.id, schedule.name, schedule.comment)
                    dialog.show(supportFragmentManager, null)
                    return true
                }
                return false
            }
            R.id.menu_detail_delete -> {
                val schedule = detailViewModel.schedule
                if (schedule != null) {
                    val dialog = YesNoDialogFragment.create(
                        "removing_schedule_dialog",
                        "スケジュールを削除します",
                        "はい",
                        "いいえ"
                    )
                    dialog.show(supportFragmentManager, null)
                    return true
                }
                return false
            }
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClickedScheduleDialogPositive(id: Int, title: String, comment: String) {
        if (title.isEmpty()) {
            Toast.makeText(this , "スケジュールを無名にはできません", Toast.LENGTH_SHORT)
                .show()
        } else {
            updateSchedule(title, comment)
        }
    }
    override fun onClickedStepDialogPositive(id: Int, title: String, duration: Duration) {
        if (title.isEmpty()) {
            Toast.makeText(this , "無名のステップは作成できません", Toast.LENGTH_SHORT)
                .show()
        } else if (id == 0) {
            addNewStep(title, duration)
        } else {
            updateStep(id, title, duration)
        }
    }

    override fun onClickedYesNoDialogNegative(dialogId: String) {
    }

    override fun onClickedYesNoDialogPositive(dialogId: String) {
        if (dialogId == "removing_schedule_dialog") {
            detailViewModel.removeSchedule()
            progressDialog = ProgressDialogFragment.create("Deleting...")
            progressDialog?.show(supportFragmentManager, null)
        }
    }

    private fun updateSchedule(title: String, comment: String) {
        detailViewModel.updateSchedule(title, comment)
        progressDialog = ProgressDialogFragment.create("Saving...")
        progressDialog?.show(supportFragmentManager, null)
    }

    private fun addNewStep(title: String, duration: Duration) {
        detailViewModel.addStep(title, duration)
        progressDialog = ProgressDialogFragment.create("Saving...")
        progressDialog?.show(supportFragmentManager, null)
    }

    private fun updateStep(id: Int, title: String, duration: Duration) {
        val adaptor = detailList.adapter
        if (adaptor is DetailRecycleViewAdapter) {
            detailViewModel.updateStep(id, title, duration)
            progressDialog = ProgressDialogFragment.create("Saving...")
            progressDialog?.show(supportFragmentManager, null)
        }
    }

    private fun removeStep(id: Int) {
        val adapter = detailList.adapter
        if (adapter is DetailRecycleViewAdapter) {
            detailViewModel.removeStep(id)
            progressDialog = ProgressDialogFragment.create("Saving...")
            progressDialog?.show(supportFragmentManager, null)
        }
    }

    private fun onLoaded(schedule: Any?) {
        if (schedule is Schedule) {
            title = schedule.name

            val adapter = DetailRecycleViewAdapter(
                detailViewModel,
            object : DetailRecycleViewAdapter.RowListener {
                override fun onClickRow(tappedView: View, step: Step) {
                    val newStepDialog = NewStepDialogFragment.create(step.id, step.title, step.duration)
                    newStepDialog.show(supportFragmentManager, "NewStepDialog")
                }
            },
            object: DetailRecycleViewAdapter.RemovingListener {
                override fun onClickRemoving(step: Step) {
                    this@DetailActivity.removeStep(schedule.id)
                    Toast.makeText(this@DetailActivity, "ステップを削除しました", Toast.LENGTH_SHORT)
                        .show()
                }
            })
            val llm = LinearLayoutManager(this)
            detailList.setHasFixedSize(true)
            detailList.layoutManager = llm
            detailList.adapter = adapter

            val ith = ItemTouchHelper(
                object : ItemTouchHelper.SimpleCallback(
                    ItemTouchHelper.UP or ItemTouchHelper.DOWN,
                    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                ) {
                    override fun onMove(
                        recyclerView: RecyclerView,
                        viewHolder: ViewHolder, target: ViewHolder
                    ): Boolean {
                        val fromPos = viewHolder.adapterPosition
                        val toPos = target.adapterPosition
                        val steps = detailViewModel.schedule?.steps
                        if (steps != null) {
                            detailViewModel.updateStepOrder(steps[fromPos].id, toPos)
                            detailList.adapter?.notifyItemMoved(fromPos, toPos)
                            return true
                        }
                        return false
                    }

                    override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
                        val fromPos = viewHolder.adapterPosition
                        val steps = detailViewModel.schedule?.steps
                        if (steps != null) {
                            if (steps.size > fromPos)
                            removeStep(steps[fromPos].id)
                        }
                    }
                })
            ith.attachToRecyclerView(detailList)

            step_add.setOnClickListener {
                val newStepDialog = NewStepDialogFragment.create(0,"", null)
                newStepDialog.show(supportFragmentManager, "NewStepDialog")
            }
        }
        progressDialog?.cancel()
    }

    private fun onLoadedError() {
        progressDialog?.cancel()
        Snackbar.make(detail_list, "データが読み込めませんでした。再読込します。", Snackbar.LENGTH_LONG)
            .setAction("OK") { detailViewModel.loadSchedule(scheduleId) }.show()
    }

    private fun onAdded(index: Any?) {
        if (index is Int) {
            if (index != -1) {
                detailList.adapter?.notifyItemInserted(index)
                progressDialog?.cancel()
                Toast.makeText(this, "ステップを追加しました", Toast.LENGTH_SHORT)
                    .show()
                return
            }
        }
        onAddedError()
    }

    private fun onAddedError() {
        progressDialog?.cancel()
        Snackbar.make(detail_list, "ステップを追加できませんでした。", Snackbar.LENGTH_LONG)
            .setAction("OK") { detailViewModel.loadSchedule(scheduleId) }.show()
    }

    private fun onRemoved(index: Any?) {
        if (index is Int) {
            if (index != -1) {
                detailList.adapter?.notifyItemRemoved(index)
                progressDialog?.cancel()
                Toast.makeText(this, "ステップを削除しました", Toast.LENGTH_SHORT)
                    .show()
                return
            }
        }
        onAddedError()
    }

    private fun onRemovedError() {
        progressDialog?.cancel()
        Snackbar.make(detail_list, "ステップを削除できませんでした。", Snackbar.LENGTH_LONG)
            .setAction("OK") { detailViewModel.loadSchedule(scheduleId) }.show()
    }

    private fun onUpdated(index: Any?) {
         if (index is Int) {
            if (index != -1) {
                detailList.adapter?.notifyItemChanged(index)
                progressDialog?.cancel()
                Toast.makeText(this, "ステップを更新しました", Toast.LENGTH_SHORT)
                    .show()
                return
            }
         } else if (index == null) {
             progressDialog?.cancel()
             Toast.makeText(this, "スケジュールを更新しました", Toast.LENGTH_SHORT)
                 .show()
             return
         }
        onUpdatedError()
    }

    private fun onUpdatedError() {
        progressDialog?.cancel()
        Snackbar.make(detail_list, "ステップを更新できませんでした。", Snackbar.LENGTH_LONG)
            .setAction("OK") { detailViewModel.loadSchedule(scheduleId) }.show()
    }

    private fun onRemovedSchedule() {
        progressDialog?.cancel()
        finish()
    }
}
