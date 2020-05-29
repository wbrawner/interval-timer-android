package com.wbrawner.trainterval.timerlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wbrawner.trainterval.model.IntervalTimer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class TimerListAdapter(
    private val timerListViewModel: TimerListViewModel,
    private val coroutineScope: CoroutineScope
) : ListAdapter<IntervalTimer, TimerListAdapter.ViewHolder>(IntervalTimerDiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val timer = getItem(position)
        holder.title.text = timer.name
        holder.description.text = timer.description
        holder.itemView.setOnClickListener {
            coroutineScope.launch {
                timerListViewModel.openTimer(timer)
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(android.R.id.text1)
        val description: TextView = itemView.findViewById(android.R.id.text2)
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
