package com.nkrin.treclock.view.scheduler

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.nkrin.treclock.R
import org.jetbrains.anko.find


class SchedulerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var titleView: TextView = itemView.find(R.id.title)
    var detailView: TextView = itemView.find(R.id.detail)
    var actionButton: View = itemView.find(R.id.action_button)
    var playingText: TextView = itemView.find(R.id.playing_text)
}