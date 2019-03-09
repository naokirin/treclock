package com.nkrin.treclock.view.detail

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nkrin.treclock.R
import com.nkrin.treclock.util.mvvm.Success
import com.nkrin.treclock.util.time.TimeProvider
import kotlinx.android.synthetic.main.fragment_detail_playing.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.threeten.bp.Duration
import org.threeten.bp.OffsetDateTime

class DetailPlayingFragment : Fragment() {

    private val timeProvider: TimeProvider by inject()
    private val detailViewModel: DetailViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detail_playing, container, false)
    }

    private fun remainingText(actualStart: OffsetDateTime, duration: Duration) : String {
        val progress = timeProvider.now().toEpochSecond() - actualStart.toEpochSecond()
        val remainingTotalSeconds = duration.seconds - progress
        val remainingHour = remainingTotalSeconds / 3600
        val remainingMinutes = (remainingTotalSeconds % 3600) / 60
        val remainingSeconds = remainingTotalSeconds % 60
        return "%02d:%02d:%02d".format(remainingHour, remainingMinutes, remainingSeconds)
    }

    override fun onStart() {
        super.onStart()
        detailViewModel.playingStepEvents.observe(this, Observer { event ->
            if (event is Success && event.value is Int) {
                detailViewModel.tickingSecondsEvents.removeObservers(this)
                val id = event.value
                val step = detailViewModel.schedule?.steps?.firstOrNull { it.id == id }
                if (step != null) {
                    val actualStart = step.actualStart
                    if (actualStart != null) {
                        title.text = step.title
                        remaining_time.text = "残り ${remainingText(actualStart, step.duration)}"
                        detailViewModel.tickingSecondsEvents.observe(this, Observer {
                            remaining_time.text = "残り ${remainingText(actualStart, step.duration)}"
                        })
                    }
                }
            }
        })

        detailViewModel.playingEvents.observe(this, Observer {
            if (it is Success && it.value is Boolean) {
                if (!it.value) {
                    title.text = null
                    remaining_time.text = null
                    detailViewModel.tickingSecondsEvents.removeObservers(this)
                }
            }
        })
    }
}
