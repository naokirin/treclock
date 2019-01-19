package com.nkrin.treclock.view.scheduler

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nkrin.treclock.R
import com.nkrin.treclock.domain.entity.Schedule


class SchedulerRecycleViewAdapter(
    private val viewModel: SchedulerViewModel,
    private val rowListener: RowListener,
    private val removingListener: RemovingListener
) : RecyclerView.Adapter<SchedulerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SchedulerViewHolder {
        val inflate = LayoutInflater.from(parent.context)
            .inflate(R.layout.scheduler_list_row, parent, false)
        return SchedulerViewHolder(inflate)
    }

    override fun onBindViewHolder(holder: SchedulerViewHolder, position: Int) {
        val item = viewModel.list[position]
        holder.titleView.text = item.name
        holder.detailView.text = item.comment

        holder.itemView.setOnClickListener {
            rowListener.onClickRow(it, item)
        }
        holder.removingButton.setOnClickListener {
            removingListener.onClickRemoving(item)
        }
    }

    override fun getItemCount(): Int {
        return viewModel.list.size
    }

    interface RowListener {
        fun onClickRow(tappedView: View, schedule: Schedule)
    }

    interface RemovingListener {
        fun onClickRemoving(schedule: Schedule)
    }
}