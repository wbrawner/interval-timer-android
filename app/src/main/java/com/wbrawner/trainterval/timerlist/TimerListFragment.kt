package com.wbrawner.trainterval.timerlist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialElevationScale
import com.wbrawner.trainterval.R
import com.wbrawner.trainterval.shared.IntervalTimer
import com.wbrawner.trainterval.timerlist.IntervalTimerListState.*
import kotlinx.android.synthetic.main.fragment_timer_list.*
import org.koin.android.ext.android.inject

class TimerListFragment : Fragment() {

    private val timerListViewModel: TimerListViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_timer_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        exitTransition = MaterialElevationScale(false)
        reenterTransition = MaterialElevationScale(true)
        allowReturnTransitionOverlap = false
        (activity as? AppCompatActivity)?.let {
            it.setSupportActionBar(toolbar)
            it.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        }
        timerList.layoutManager = LinearLayoutManager(view.context)
        timerList.addItemDecoration(
            DividerItemDecoration(
                view.context,
                LinearLayoutManager.VERTICAL
            )
        )
        timerList.adapter = TimerListAdapter(timerListViewModel)
        addTimerButton.setOnClickListener {
            timerListViewModel.addTimer()
        }
        timerListViewModel.timerState.observe(viewLifecycleOwner, Observer { state ->
            Log.d("TimerListFragment", "Received state $state")
            when (state) {
                is LoadingState -> renderLoading()
                is EmptyListState -> renderEmptyList()
                is SuccessListState -> renderSuccessState(state.timers)
                is ErrorState -> renderErrorState(state.message)
                is CreateTimer -> findNavController().navigate(
                    R.id.timerFormFragment,
                    null,
                    null,
                    FragmentNavigatorExtras(addTimerButton to "fabContainer")
                )
                is EditTimer -> findNavController().navigate(R.id.timerFormFragment)
                is OpenTimer -> findNavController().navigate(
                    R.id.activeTimerFragment,
                    Bundle().apply { putLong("timerId", state.timerId) }
                )
            }
        })
        timerListViewModel.init()
    }

    private fun renderLoading() {
        addTimerButton.hide()
        error.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        timerList.visibility = View.GONE
    }

    private fun renderEmptyList() {
        addTimerButton.show()
        error.visibility = View.VISIBLE
        error.text = "Add a new timer to get started."
        progressBar.visibility = View.GONE
        timerList.visibility = View.GONE
    }

    private fun renderSuccessState(timers: List<IntervalTimer>) {
        addTimerButton.show()
        error.visibility = View.GONE
        progressBar.visibility = View.GONE
        timerList.visibility = View.VISIBLE
        (timerList.adapter as? TimerListAdapter)?.submitList(timers)
    }

    private fun renderErrorState(message: String) {
        addTimerButton.hide()
        error.visibility = View.VISIBLE
        error.text = message
        progressBar.visibility = View.GONE
        timerList.visibility = View.GONE
    }
}
