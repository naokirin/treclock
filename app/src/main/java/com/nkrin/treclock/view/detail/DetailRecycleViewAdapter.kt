package com.nkrin.treclock.view.detail

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.nkrin.treclock.R
import com.nkrin.treclock.domain.entity.Step
import com.nkrin.treclock.util.time.TimeProvider


class DetailRecycleViewAdapter(
    private val viewModel: DetailViewModel,
    private val timeProvider: TimeProvider,
    private val rowListener: RowListener
) : RecyclerView.Adapter<DetailViewHolder>() {

    private lateinit var parent: ViewGroup
    private var itemTouchHelper: ItemTouchHelper? = null

    fun setItemTouchHelper(value: ItemTouchHelper?) {
        itemTouchHelper = value
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        this.parent = parent
        val inflate = LayoutInflater.from(parent.context)
            .inflate(R.layout.detail_list_row, parent, false)
        return DetailViewHolder(inflate)
    }

    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        val schedule = viewModel.schedule
        if (schedule != null) {
            val item = schedule.steps[position]
            val durationText = "${item.duration.toMinutes()}${parent.resources.getString(R.string.minutes_text)}"
            with(holder) {
                titleView.text = item.title
                durationView.text = durationText
                if (schedule.played(timeProvider.now())) {
                    val now = timeProvider.now()
                    val actualStart = item.actualStart
                    if (actualStart != null) {
                        if (actualStart <= now && now < actualStart + item.duration)  {
                            val anim = AnimationUtils.loadAnimation(itemView.context, R.anim.repeated_blinking_animation)
                            playingIcon.visibility = View.VISIBLE
                            playingIcon.startAnimation(anim)
                        }
                    }
                    reorderIcon.visibility = View.GONE
                }
                else {
                    reorderIcon.visibility = View.VISIBLE
                    playingIcon.visibility = View.INVISIBLE
                    playingIcon.clearAnimation()
                }

                itemView.setOnClickListener {
                    rowListener.onClickRow(it, item)
                }
                actionButton.setOnClickListener {
                    rowListener.onClickAction(it, item)
                }
                reorderIcon.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        itemTouchHelper?.startDrag(this)
                    }
                    return@setOnTouchListener false
                }

                playingIcon.setHasTransientState(true)

                rowListener.onBindRow(holder, this.itemView, item)
            }
        }
    }

    override fun getItemCount(): Int {
        return viewModel.schedule?.steps?.size ?: 0
    }

    interface RowListener {
        fun onBindRow(holder: DetailViewHolder, tappedView: View, step: Step)
        fun onClickRow(tappedView: View, step: Step)
        fun onClickAction(tappedView: View, step: Step)
    }
}