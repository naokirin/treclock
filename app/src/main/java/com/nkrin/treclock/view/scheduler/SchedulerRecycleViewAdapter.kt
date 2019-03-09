package com.nkrin.treclock.view.scheduler

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nkrin.treclock.R
import com.nkrin.treclock.domain.entity.Schedule
import com.nkrin.treclock.util.time.TimeProvider


class SchedulerRecycleViewAdapter(
    private val viewModel: SchedulerViewModel,
    private val playingViewModel: SchedulerPlayingViewModel,
    private val timeProvider: TimeProvider,
    private val rowListener: RowListener
) : RecyclerView.Adapter<SchedulerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SchedulerViewHolder {
        val inflate = LayoutInflater.from(parent.context)
            .inflate(R.layout.scheduler_list_row, parent, false)
        return SchedulerViewHolder(inflate)
    }

    override fun onBindViewHolder(holder: SchedulerViewHolder, position: Int) {
        val item = viewModel.schedules[position]
        playingViewModel.stopSchedule(item.id)
        holder.titleView.text = item.name
        holder.detailView.text = item.comment

        if (item.played(timeProvider.now())) {
            enablePlaying(holder)
            playingViewModel.resumeSchedule(item.id) {
                disablePlaying(holder)
            }
        } else {
            disablePlaying(holder)
            playingViewModel.stopSchedule(item.id)
        }

        holder.itemView.setOnClickListener {
            rowListener.onClickRow(it, item)
        }
        holder.actionButton.setOnClickListener {
            rowListener.onClickAction(it, item)
        }
    }

    override fun getItemCount(): Int {
        return viewModel.schedules.size
    }

    private fun enablePlaying(holder: SchedulerViewHolder) {
        holder.actionButton.visibility = View.GONE
        holder.playingText.visibility = View.VISIBLE
    }

    private fun disablePlaying(holder: SchedulerViewHolder) {
        holder.actionButton.visibility = View.VISIBLE
        holder.playingText.visibility = View.GONE
    }

    interface RowListener {
        fun onClickRow(tappedView: View, schedule: Schedule)
        fun onClickAction(tappedView: View, schedule: Schedule)
    }
}