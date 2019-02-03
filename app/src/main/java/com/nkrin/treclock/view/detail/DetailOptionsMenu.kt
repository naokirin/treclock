package com.nkrin.treclock.view.detail

import android.view.Menu
import android.view.MenuItem
import com.nkrin.treclock.R
import com.nkrin.treclock.util.time.TimeProvider
import com.nkrin.treclock.view.util.dialog.NewScheduleDialogFragment
import com.nkrin.treclock.view.util.dialog.YesNoDialogFragment

class DetailOptionsMenu(
    private val activity: DetailActivity,
    private val detailViewModel: DetailViewModel,
    private val timeProvider: TimeProvider
) {

    fun onCreateOptionsMenu(menu: Menu, superMethod: (Menu) -> Boolean): Boolean {
        val inflater = activity.menuInflater
        inflater.inflate(R.menu.menu_detail, menu)
        return true
    }

    fun onPrepareOptionsMenu(menu: Menu, superMethod: (Menu) -> Boolean): Boolean {
        val item = menu.findItem(R.id.menu_detail_delete)
        item?.isVisible = detailViewModel.schedule?.played(timeProvider.now()) == false
        return superMethod(menu)
    }

    fun onOptionsItemSelected(item: MenuItem, superMethod: (MenuItem) -> Boolean): Boolean {
        return when (item.itemId) {
            R.id.menu_detail_edit -> onSelectEdit()
            R.id.menu_detail_delete -> onSelectDelete()
            android.R.id.home -> onSelectHome()
            else -> superMethod(item)
        }
    }

    private fun onSelectEdit(): Boolean {
        val schedule = detailViewModel.schedule
        if (schedule != null) {
            val dialog = NewScheduleDialogFragment.create(schedule.id, schedule.name, schedule.comment)
            dialog.show(activity.supportFragmentManager, null)
            return true
        }
        return false
    }

    private fun onSelectDelete(): Boolean {
        val schedule = detailViewModel.schedule
        if (schedule != null) {
            val dialog = YesNoDialogFragment.create(
                "removing_schedule_dialog",
                "スケジュールを削除します",
                "はい",
                "いいえ"
            )
            dialog.show(activity.supportFragmentManager, null)
            return true
        }
        return false
    }

    private fun onSelectHome(): Boolean {
        activity.finish()
        return true
    }
}