package com.nkrin.treclock.view.detail

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.nkrin.treclock.R
import org.jetbrains.anko.find


class DetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var titleView: TextView = itemView.find(R.id.title)
    var durationView: TextView = itemView.find(R.id.duration)
    var actionButton: ImageButton = itemView.find(R.id.action_button)
}