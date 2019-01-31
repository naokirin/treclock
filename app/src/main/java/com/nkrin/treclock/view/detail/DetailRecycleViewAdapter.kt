package com.nkrin.treclock.view.scheduler

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nkrin.treclock.R
import com.nkrin.treclock.domain.entity.Step
import com.nkrin.treclock.view.detail.DetailViewModel


class DetailRecycleViewAdapter(
    private val viewModel: DetailViewModel,
    private val rowListener: RowListener
) : RecyclerView.Adapter<DetailViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        val inflate = LayoutInflater.from(parent.context)
            .inflate(R.layout.detail_list_row, parent, false)
        return DetailViewHolder(inflate)
    }

    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        val schedule = viewModel.schedule
        if (schedule != null) {
            val item = schedule.steps[position]
            with(holder) {
                titleView.text = item.title
                durationView.text = "${item.duration.toMinutes()} 分"

                itemView.setOnClickListener {
                    rowListener.onClickRow(it, item)
                }
                actionButton.setOnClickListener {
                    rowListener.onClickAction(it, item)
                }

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