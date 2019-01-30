package com.nkrin.treclock.view.scheduler

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.nkrin.treclock.R


class DetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var titleView: TextView = itemView.findViewById(R.id.title)
    var durationView: TextView = itemView.findViewById(R.id.duration)
    var actionButton: ImageButton = itemView.findViewById(R.id.action_button)
}