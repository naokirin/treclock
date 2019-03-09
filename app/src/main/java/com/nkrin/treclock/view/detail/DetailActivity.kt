package com.nkrin.treclock.view.detail

import android.app.AlarmManager
import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.nkrin.treclock.R
import com.nkrin.treclock.domain.entity.Schedule
import com.nkrin.treclock.util.mvvm.Error
import com.nkrin.treclock.util.mvvm.Pending
import com.nkrin.treclock.util.mvvm.Success
import com.nkrin.treclock.util.time.TimeProvider
import com.nkrin.treclock.view.detail.dialog.NewStepDialogFragment
import com.nkrin.treclock.view.util.dialog.NewScheduleDialogFragment
import com.nkrin.treclock.view.util.dialog.ProgressDialogFragment
import com.nkrin.treclock.view.util.dialog.YesNoDialogFragment
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import org.threeten.bp.Duration
import org.threeten.bp.OffsetDateTime


class DetailActivity :
    AppCompatActivity(),
    NewStepDialogFragment.Listener, NewScheduleDialogFragment.Listener, YesNoDialogFragment.Listener {

    private val detailViewModel: DetailViewModel by viewModel()
    private val sharedDetailViewModel: SharedDetailViewModel by viewModel()
    private val timeProvider: TimeProvider by inject()
    private var progressDialog: ProgressDialogFragment? = null
    private lateinit var detailOptionsMenu: DetailOptionsMenu
    private lateinit var detailAlarmManager: DetailAlarmManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycle.addObserver(detailViewModel)
        detailOptionsMenu = DetailOptionsMenu(this, detailViewModel, timeProvider)
        detailAlarmManager = DetailAlarmManager(
            detailViewModel,
            getSystemService(Context.ALARM_SERVICE) as AlarmManager,
            timeProvider,
            applicationContext
        )

        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        detailViewModel.initScheduleId(intent?.getIntExtra("schedule_id", 0) ?: 0)

        val toolbar = toolbar as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        detailViewModel.loadingEvents.observe(this, Observer {
            when (it) {
                is Pending -> onPendingWithProgress("Loading...")
                is Success -> onLoaded(it.value)
                is Error -> onLoadedError()
            }
        })

        detailViewModel.addingEvents.observe(this, Observer {
            when (it) {
                is Pending -> onPendingWithProgress("Saving...")
                is Success -> onAdded(it.value)
                is Error -> onAddedError()
            }
        })

        detailViewModel.removingEvents.observe(this, Observer {
            when (it) {
                is Pending -> onPendingWithProgress("Saving...")
                is Success -> onRemoved(it.value)
                is Error -> onRemovedError()
            }
        })

        detailViewModel.updatingEvents.observe(this, Observer {
            when (it) {
                is Pending -> onPendingWithProgress("Saving...")
                is Success -> onUpdated(it.value)
                is Error -> onUpdatedError()
            }
        })

        detailViewModel.removingScheduleEvents.observe(this, Observer {
            onRemovedSchedule()
        })

        detailViewModel.playingEvents.observe(this, Observer {
            when(it) {
                is Success -> onPlayedOrStopped(it.value)
                is Error -> onPlayedOrStoppedError()
            }
        })

        detailViewModel.playingStepEvents.observe(this, Observer {
            when(it) {
                is Success -> onPlayedStep(it.value)
                is Error -> onPlayedStepError()
            }
        })

        detailViewModel.settingStepTimerEvents.observe(this, Observer {
            if (it is Success && it.value is Triple<*, *, *>) {
                val title = it.value.first
                val duration = it.value.second as? Duration
                val actualStart = it.value.third as? OffsetDateTime
                if (title is String) detailAlarmManager.setAlarm(title, duration, actualStart)
            }
        })

        step_add.setOnClickListener {
            val newStepDialog = NewStepDialogFragment.create(0,"", null)
            newStepDialog.show(supportFragmentManager, "NewStepDialog")
        }

        play_button.setOnClickListener {
            val s = detailViewModel.schedule
            if (s != null && !s.steps.isEmpty()) {
                detailViewModel.startSchedule()
                return@setOnClickListener
            }
            Toast.makeText(applicationContext, "スケジュール開始にはステップが一つ以上必要です", Toast.LENGTH_LONG)
                .show()
        }

        stop_button.setOnClickListener {
            detailAlarmManager.stopAllAlarms()
            detailViewModel.stopSchedule()
        }
    }

    override fun onClickedScheduleDialogPositive(id: Int, title: String, comment: String) {
        if (title.isEmpty()) {
            Toast.makeText(this, "スケジュールを無名にはできません", Toast.LENGTH_SHORT)
                .show()
        } else {
            detailViewModel.updateSchedule(title, comment)
        }
    }
    override fun onClickedStepDialogPositive(id: Int, title: String, duration: Duration?) {
        when {
            title.isEmpty() -> Toast.makeText(this , "無名のステップは作成できません", Toast.LENGTH_SHORT)
                .show()
            duration == null || duration.isZero || duration.isNegative ->
                Toast.makeText(this , "時間指定のないステップは作成できません", Toast.LENGTH_SHORT)
                    .show()
            id == 0 -> detailViewModel.addStep(title, duration)
            else -> detailViewModel.updateStep(id, title, duration)
        }
    }

    override fun onClickedYesNoDialogNegative(dialogId: String) { }
    override fun onClickedYesNoDialogPositive(dialogId: String) {
        if (dialogId == "removing_schedule_dialog") {
            detailViewModel.removeSchedule()
        }
    }


    private fun onPendingWithProgress(message: String) {
        progressDialog = ProgressDialogFragment.create(message)
        progressDialog?.show(supportFragmentManager, null)
    }

    private fun onProgressCompleted() {
        progressDialog?.cancel()
    }

    private fun onErrorWithRetrySnackbar(message: String) {
        onProgressCompleted()
        Snackbar.make(detail_list, message, Snackbar.LENGTH_LONG)
            .setAction("OK") { detailViewModel.loadSchedule() }.show()
    }

    private fun onLoaded(schedule: Any?) {
        if (schedule is Schedule) {
            supportActionBar?.title = schedule.name

            if (schedule.played(timeProvider.now())) {
                play_button.hide()
                stop_button.show()
            }
            sharedDetailViewModel.onLoaded()
        }
        onProgressCompleted()
    }

    private fun onLoadedError() = onErrorWithRetrySnackbar("データが読み込めませんでした。再読込します。")

    private fun onAdded(index: Any?) {
        if (index is Int) {
            if (index != -1) {
                sharedDetailViewModel.onStepInserted(index)
                onProgressCompleted()
                Toast.makeText(this, "ステップを追加しました", Toast.LENGTH_SHORT)
                    .show()
                return
            }
        }
        onAddedError()
    }

    private fun onAddedError() = onErrorWithRetrySnackbar("ステップを追加できませんでした。")

    private fun onRemoved(index: Any?) {
        if (index is Int) {
            if (index != -1) {
                sharedDetailViewModel.onStepRemoved(index)
                onProgressCompleted()
                Toast.makeText(this, "ステップを削除しました", Toast.LENGTH_SHORT)
                    .show()
                return
            }
        }
        onAddedError()
    }

    private fun onRemovedError() = onErrorWithRetrySnackbar("ステップを削除できませんでした。")

    private fun onUpdated(index: Any?) {
         if (index is Int) {
            if (index != -1) {
                sharedDetailViewModel.onStepChanged(index)
                onProgressCompleted()
                Toast.makeText(this, "ステップを更新しました", Toast.LENGTH_SHORT)
                    .show()
                return
            }
         } else if (index == null) {
             val schedule = detailViewModel.schedule
             if (schedule != null) {
                 supportActionBar?.title = schedule.name
             }
             onProgressCompleted()
             Toast.makeText(this, "スケジュールを更新しました", Toast.LENGTH_SHORT)
                 .show()
             return
         }
        onUpdatedError()
    }

    private fun onUpdatedError() = onErrorWithRetrySnackbar("ステップを更新できませんでした。")

    private fun onRemovedSchedule() {
        onProgressCompleted()
        finish()
    }

    private fun onPlayedOrStopped(playing: Any?) {
        val play = play_button as FloatingActionButton
        val stop = stop_button as FloatingActionButton
        if (playing is Boolean) {
            if (playing) {
                play.hide()
                stop.show()
                sharedDetailViewModel.onPlay()
            } else {
                stop.hide()
                play.show()
                sharedDetailViewModel.onStop()
                sharedDetailViewModel.onStopAllSteps()
            }
        }
        onProgressCompleted()
    }

    private fun onPlayedOrStoppedError() {
    }

    private fun onPlayedStep(stepId: Any?) {
        if (stepId is Int) {
            sharedDetailViewModel.onStopExcluded(stepId)
            sharedDetailViewModel.onPlayStep(stepId)
        }
    }

    private fun onPlayedStepError() = onErrorWithRetrySnackbar("ステップを開始できませんでした。")

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return detailOptionsMenu.onCreateOptionsMenu(menu) { super.onCreateOptionsMenu(it) }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        return detailOptionsMenu.onPrepareOptionsMenu(menu) { super.onPrepareOptionsMenu(menu) }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return detailOptionsMenu.onOptionsItemSelected(item) { super.onOptionsItemSelected(item) }
    }
}
