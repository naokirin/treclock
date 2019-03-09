package com.nkrin.treclock.view.detail

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.PopupMenu
import com.nkrin.treclock.R
import com.nkrin.treclock.domain.entity.Step
import com.nkrin.treclock.util.time.TimeProvider
import com.nkrin.treclock.view.detail.dialog.NewStepDialogFragment
import com.nkrin.treclock.view.util.BackgroundItemDecoration
import com.nkrin.treclock.view.util.LastMarginItemDecoration
import org.jetbrains.anko.find
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel


class DetailListFragment : Fragment() {

    val detailViewModel: DetailViewModel by sharedViewModel()
    val sharedViewModel: SharedDetailViewModel by sharedViewModel()
    val timeProvider: TimeProvider by inject()

    private lateinit var detailList: RecyclerView

    private fun removeStep(id: Int) {
        detailViewModel.removeStep(id)
        sharedViewModel.removeOnPlayStep(id)
        sharedViewModel.removeOnPlay(id)
        sharedViewModel.removeOnStopStep(id)
        sharedViewModel.removeOnStop(id)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_detail_list, container, false)
    }

    override fun onStart() {
        super.onStart()
        detailList = activity!!.find(R.id.detail_list)
        detailList.addItemDecoration(
            BackgroundItemDecoration(R.drawable.item_grey_background, R.drawable.item_white_background)
        )
        detailList.addItemDecoration(LastMarginItemDecoration())

        sharedViewModel.insertedStepEvent.observe(this, Observer {
            if (it != null) {
                notifyItemInserted(it)
            }
        })
        sharedViewModel.removedStepEvent.observe(this, Observer {
            if (it != null) {
                notifyItemRemoved(it)
            }
        })
        sharedViewModel.changedStepEvent.observe(this, Observer {
            if (it != null) {
                notifyItemChanged(it)
            }
        })

        sharedViewModel.loadingEvent.observe(this, Observer { onLoaded() })
    }

    private fun notifyItemInserted(position: Int) {
        detailList.adapter?.notifyItemInserted(position)

        // Note: update ItemDecoration
        val count = detailList.adapter?.itemCount
        if (count == position + 1 && count > 1) {
            detailList.adapter?.notifyItemChanged(position - 1)
        }
    }
    private fun notifyItemRemoved(position: Int) {
        detailList.adapter?.notifyItemRemoved(position)

        // Note: update ItemDecoration
        val count = detailList.adapter?.itemCount
        if (count == position && count >= 1) {
            detailList.adapter?.notifyItemChanged(count - 1)
        }
    }

    private fun notifyItemChanged(position: Int) = detailList.adapter?.notifyItemChanged(position)

    private fun onLoaded() {
        val adapter = DetailRecycleViewAdapter(
            detailViewModel,
            timeProvider,
            object : DetailRecycleViewAdapter.RowListener {
                override fun onBindRow(holder: DetailViewHolder, tappedView: View, step: Step) {
                    sharedViewModel.addOnPlayStep(step.id) {
                        val anim = AnimationUtils.loadAnimation(context, R.anim.repeated_blinking_animation)
                        holder.playingIcon.visibility = View.VISIBLE
                        holder.playingIcon.startAnimation(anim)
                    }

                    sharedViewModel.addOnStopStep(step.id) {
                        tappedView.find<ImageView>(R.id.detail_row_playing_icon).run {
                            clearAnimation()
                            animate().cancel()
                            animation = null
                            visibility = View.INVISIBLE
                        }
                    }

                    sharedViewModel.addOnPlay(step.id) {
                        tappedView.find<ImageView>(R.id.detail_row_draggable_icon).run {
                            visibility = View.INVISIBLE
                        }
                    }

                    sharedViewModel.addOnStop(step.id) {
                        tappedView.find<ImageView>(R.id.detail_row_playing_icon).run {
                            visibility = View.INVISIBLE
                        }
                        tappedView.find<ImageView>(R.id.detail_row_draggable_icon).run {
                            visibility = View.VISIBLE
                        }
                    }
                }
                override fun onClickRow(tappedView: View, step: Step) {
                    val s = detailViewModel.schedule
                    if (s != null && s.played(timeProvider.now())) {
                        return
                    }

                    val newStepDialog = NewStepDialogFragment.create(step.id, step.title, step.duration)
                    newStepDialog.show(fragmentManager, "NewStepDialog")
                }
                override fun onClickAction(tappedView: View, step: Step) {
                    with(PopupMenu(context, tappedView)) {
                        val inflater = menuInflater
                        inflater.inflate(R.menu.menu_detail_item, menu)
                        val deleteItem = menu.findItem(R.id.menu_detail_item_delete)
                        deleteItem.isVisible = detailViewModel.schedule?.played(timeProvider.now()) == false
                        setOnMenuItemClickListener {
                            when (it?.itemId) {
                                R.id.menu_detail_item_delete -> {
                                    this@DetailListFragment.removeStep(step.id)
                                }
                                R.id.menu_detail_item_play -> {
                                    detailViewModel.startStep(step.id)
                                }
                            }
                            return@setOnMenuItemClickListener true
                        }
                        show()
                    }
                }
            })
        val llm = LinearLayoutManager(context)
        detailList.run {
            setHasFixedSize(true)
            layoutManager = llm
            this.adapter = adapter
        }

        val ith = ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: ViewHolder, target: ViewHolder
                ): Boolean {
                    val s = detailViewModel.schedule
                    if (s != null && s.played(timeProvider.now())) {
                        return false
                    }
                    val fromPos = viewHolder.adapterPosition
                    val toPos = target.adapterPosition
                    val steps = detailViewModel.schedule?.steps
                    if (steps != null) {
                        detailViewModel.updateStepOrder(steps[fromPos].id, toPos)
                        detailList.adapter?.notifyItemMoved(fromPos, toPos)
                        return true
                    }
                    return false
                }

                override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
                    val s = detailViewModel.schedule
                    if (s != null && s.played(timeProvider.now())) {
                        return
                    }
                    val fromPos = viewHolder.adapterPosition
                    val steps = detailViewModel.schedule?.steps
                    if (steps != null && steps.size > fromPos) {
                        removeStep(steps[fromPos].id)
                    }
                }

                override fun getSwipeDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                    val s = detailViewModel.schedule
                    if (s != null && s.played(timeProvider.now())) {
                        return 0
                    }
                    return super.getSwipeDirs(recyclerView, viewHolder)
                }

                override fun isLongPressDragEnabled(): Boolean {
                    return false
                }
            })
        adapter.itemTouchHelper = ith
        ith.attachToRecyclerView(detailList)

        detailList.viewTreeObserver.addOnGlobalLayoutListener (
            object: ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    val step = detailViewModel.schedule?.playingStep(timeProvider.now())
                    if (step != null) {
                        detailViewModel.resumePlaying()
                        val index = detailViewModel.schedule?.steps?.indexOfFirst { it.id == step.id }
                        if (index != null && index >= 0) {
                            detailList.layoutManager?.smoothScrollToPosition(
                                detailList,
                                RecyclerView.State(),
                                index
                            )
                        }
                    }
                    detailList.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            }
        )
    }
}
