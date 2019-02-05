package com.nkrin.treclock.view.detail

import android.app.AlarmManager
import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
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
import android.view.ViewTreeObserver
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import com.nkrin.treclock.R
import com.nkrin.treclock.domain.entity.Schedule
import com.nkrin.treclock.domain.entity.Step
import com.nkrin.treclock.util.mvvm.Error
import com.nkrin.treclock.util.mvvm.Pending
import com.nkrin.treclock.util.mvvm.Success
import com.nkrin.treclock.util.time.TimeProvider
import com.nkrin.treclock.view.detail.dialog.NewStepDialogFragment
import com.nkrin.treclock.view.util.BackgroundItemDecoration
import com.nkrin.treclock.view.util.dialog.NewScheduleDialogFragment
import com.nkrin.treclock.view.util.dialog.ProgressDialogFragment
import com.nkrin.treclock.view.util.dialog.YesNoDialogFragment
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*
import org.jetbrains.anko.find
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import java.time.Duration
import java.time.OffsetDateTime


class DetailActivity :
    AppCompatActivity(),
    NewStepDialogFragment.Listener, NewScheduleDialogFragment.Listener, YesNoDialogFragment.Listener {

    private val detailViewModel: DetailViewModel by viewModel()
    private val timeProvider: TimeProvider by inject()
    private var progressDialog: ProgressDialogFragment? = null
    private lateinit var detailList: RecyclerView
    private lateinit var detailOptionsMenu: DetailOptionsMenu
    private lateinit var detailAlarmManager: DetailAlarmManager

    private val playingStepsCallbacks: MutableMap<Int, () -> Unit> = mutableMapOf()
    private val stoppingStepsCallbacks: MutableMap<Int, () -> Unit> = mutableMapOf()

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

        detailViewModel.scheduleId = intent?.getIntExtra("schedule_id", 0) ?: 0

        detailList = detail_list
        detailList.addItemDecoration(
            BackgroundItemDecoration(R.drawable.item_grey_background, R.drawable.item_white_background)
        )

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

    private fun removeStep(id: Int) {
        detailViewModel.removeStep(id)
        playingStepsCallbacks.remove(id)
        stoppingStepsCallbacks.remove(id)
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

            val adapter = DetailRecycleViewAdapter(
                detailViewModel,
            object : DetailRecycleViewAdapter.RowListener {
                override fun onBindRow(holder: DetailViewHolder, tappedView: View, step: Step) {
                    playingStepsCallbacks[step.id] = {
                        val anim = AnimationUtils.loadAnimation(this@DetailActivity, R.anim.repeated_blinking_animation)
                        val image = tappedView.find<ImageView>(R.id.detail_list_row_icon)
                        image.startAnimation(anim)
                    }

                    stoppingStepsCallbacks[step.id] = {
                        val image = tappedView.find<ImageView>(R.id.detail_list_row_icon)
                        image.clearAnimation()
                        image.animate().cancel()
                        image.animation = null
                    }
                }
                override fun onClickRow(tappedView: View, step: Step) {
                    val s = detailViewModel.schedule
                    if (s != null && s.played(timeProvider.now())) {
                        return
                    }

                    val newStepDialog = NewStepDialogFragment.create(step.id, step.title, step.duration)
                    newStepDialog.show(supportFragmentManager, "NewStepDialog")
                }
                override fun onClickAction(tappedView: View, step: Step) {
                    with(PopupMenu(applicationContext, tappedView)) {
                        val inflater = menuInflater
                        inflater.inflate(R.menu.menu_detail_item, menu)
                        val deleteItem = menu.findItem(R.id.menu_detail_item_delete)
                        deleteItem.isVisible = detailViewModel.schedule?.played(timeProvider.now()) == false
                        setOnMenuItemClickListener {
                            when (it?.itemId) {
                                R.id.menu_detail_item_delete -> {
                                    this@DetailActivity.removeStep(step.id)
                                }
                                R.id.menu_detail_item_play -> {
                                    detailViewModel.startStep(step.id)
                                }
                            }
                            return@setOnMenuItemClickListener true
                        }
                        show()
                    }
                }
            })
            val llm = LinearLayoutManager(this)
            detailList.let {
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
                        val s = detailViewModel.schedule
                        if (s != null && s.played(timeProvider.now())) {
                            return false
                        }
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
                        val s = detailViewModel.schedule
                        if (s != null && s.played(timeProvider.now())) {
                            return
                        }
                        val fromPos = viewHolder.adapterPosition
                        val steps = detailViewModel.schedule?.steps
                        if (steps != null && steps.size > fromPos) {
                            removeStep(steps[fromPos].id)
                        }
                    }

                    override fun getSwipeDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                        val s = detailViewModel.schedule
                        if (s != null && s.played(timeProvider.now())) {
                            return 0
                        }
                        return super.getSwipeDirs(recyclerView, viewHolder)
                    }
                })
            ith.attachToRecyclerView(detailList)

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
                Toast.makeText(this, "スケジュール開始にはステップが一つ以上必要です", Toast.LENGTH_LONG)
                    .show()
            }

            stop_button.setOnClickListener {
                detailAlarmManager.stopAllAlarms()
                detailViewModel.stopSchedule()
            }

            detailList.viewTreeObserver.addOnGlobalLayoutListener (
                object: ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        if (detailViewModel.schedule?.played(timeProvider.now()) == true) {
                            detailViewModel.resumePlayingTimer()
                        }
                        detailList.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                }
            )
        }
        onProgressCompleted()
    }

    private fun onLoadedError() = onErrorWithRetrySnackbar("データが読み込めませんでした。再読込します。")

    private fun onAdded(index: Any?) {
        if (index is Int) {
            if (index != -1) {
                detailList.adapter?.notifyItemInserted(index)
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
                detailList.adapter?.notifyItemRemoved(index)
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
                detailList.adapter?.notifyItemChanged(index)
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
            } else {
                stop.hide()
                play.show()
                stoppingStepsCallbacks.forEach { _, callback -> callback() }
            }
        }
        onProgressCompleted()
    }

    private fun onPlayedOrStoppedError() {
    }

    private fun onPlayedStep(stepId: Any?) {
        if (stepId is Int) {
            playingStepsCallbacks[stepId]?.invoke()
            stoppingStepsCallbacks.forEach { id, callback ->
                if (id != stepId) {
                    callback()
                }
            }
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
