package com.nkrin.treclock.view.detail

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.nkrin.treclock.R
import com.nkrin.treclock.view.util.TouchedImageView
import org.jetbrains.anko.find


class DetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var titleView: TextView = itemView.find(R.id.title)
    var durationView: TextView = itemView.find(R.id.duration)
    val playingIcon: ImageView = itemView.find(R.id.detail_row_playing_icon)
    var actionButton: View = itemView.find(R.id.action_button)
    var reorderIcon: TouchedImageView = itemView.find(R.id.detail_row_draggable_icon)
}