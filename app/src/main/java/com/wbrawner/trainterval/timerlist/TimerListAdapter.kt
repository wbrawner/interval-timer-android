package com.wbrawner.trainterval.timerlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wbrawner.trainterval.R
import com.wbrawner.trainterval.model.IntervalTimer
import com.wbrawner.trainterval.toIntervalDuration

class TimerListAdapter(
    private val timerListViewModel: TimerListViewModel
) : ListAdapter<IntervalTimer, TimerListAdapter.ViewHolder>(IntervalTimerDiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_timer, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val timer = getItem(position)
        holder.title.text = timer.name
        if (timer.description.isBlank()) {
            holder.description.visibility = View.GONE
        } else {
            holder.description.visibility = View.VISIBLE
            holder.description.text = timer.description
        }
        holder.duration.text = timer.totalDuration.toIntervalDuration().toString()
        holder.itemView.setOnClickListener {
            timerListViewModel.openTimer(timer)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val description: TextView = itemView.findViewById(R.id.description)
        val duration: TextView = itemView.findViewById(R.id.duration)
    }
}

class IntervalTimerDiffUtilCallback : DiffUtil.ItemCallback<IntervalTimer>() {
    override fun areItemsTheSame(oldItem: IntervalTimer, newItem: IntervalTimer): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: IntervalTimer, newItem: IntervalTimer): Boolean {
        return oldItem == newItem
    }
}
