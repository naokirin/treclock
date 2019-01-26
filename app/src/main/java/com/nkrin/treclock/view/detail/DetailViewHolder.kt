package com.nkrin.treclock.view.scheduler

import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.nkrin.treclock.R
import org.jetbrains.anko.find


class DetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var titleView: TextView = itemView.findViewById(R.id.title)
    var durationView: TextView = itemView.findViewById(R.id.duration)
    var removingButton: FloatingActionButton = itemView.find(R.id.removing_button)
}