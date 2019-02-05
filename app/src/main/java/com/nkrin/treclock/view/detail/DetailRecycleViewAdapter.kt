package com.nkrin.treclock.view.detail

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nkrin.treclock.R
import com.nkrin.treclock.domain.entity.Step


class DetailRecycleViewAdapter(
    private val viewModel: DetailViewModel,
    private val rowListener: RowListener
) : RecyclerView.Adapter<DetailViewHolder>() {

    private lateinit var parent: ViewGroup

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