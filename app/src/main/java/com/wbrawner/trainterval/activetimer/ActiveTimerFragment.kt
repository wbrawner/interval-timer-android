package com.wbrawner.trainterval.activetimer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.wbrawner.trainterval.Logger
import com.wbrawner.trainterval.R
import com.wbrawner.trainterval.model.IntervalTimerDao
import kotlinx.android.synthetic.main.fragment_active_timer.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class ActiveTimerFragment : Fragment() {

    private var coroutineScope: CoroutineScope? = null
    private val activeTimerViewModel: ActiveTimerViewModel by activityViewModels()
    private val logger: Logger by inject(parameters = { parametersOf("ActiveTimerStore") })
    private val timerDao: IntervalTimerDao by inject()
    private var timerId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        timerId = requireArguments().getLong(EXTRA_TIMER_ID)
        setHasOptionsMenu(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == android.R.id.home) {
            findNavController().navigateUp()
        } else {
            super.onOptionsItemSelected(item)
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_active_timer, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.let {
            it.setSupportActionBar(toolbar)
            it.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope!!.launch {
            activeTimerViewModel.timerState.observe(viewLifecycleOwner, Observer { state ->
                when (state) {
                    is IntervalTimerActiveState.LoadingState -> renderLoading()
                    is IntervalTimerActiveState.TimerRunningState -> renderTimer(state)
                    is IntervalTimerActiveState.ExitState -> findNavController().navigateUp()
                }
            })
            activeTimerViewModel.init(logger, timerDao, timerId)
        }
        skipPreviousButton.setOnClickListener {
            activeTimerViewModel.goBack()
        }
        playPauseButton.setOnClickListener {
            activeTimerViewModel.toggleTimer()
        }
        skipNextButton.setOnClickListener {
            activeTimerViewModel.skipAhead()
        }
    }

    private fun renderLoading() {
        progressBar.visibility = View.VISIBLE
        timerLayout.referencedIds.forEach {
            view?.findViewById<View>(it)?.visibility = View.GONE
        }
    }

    private fun renderTimer(state: IntervalTimerActiveState.TimerRunningState) {
        progressBar.visibility = View.GONE
        timerLayout.referencedIds.forEach {
            view?.findViewById<View>(it)?.visibility = View.VISIBLE
        }
        (activity as? AppCompatActivity)?.supportActionBar?.title = state.timerName
        val backgroundColor = resources.getColor(state.timerBackground, context?.theme)
        timerBackground.setBackgroundColor(backgroundColor)
        playPauseButton.setImageDrawable(requireContext().getDrawable(state.playPauseIcon))
        timeRemaining.text = state.timeRemaining
        timerSets.text = state.currentSet.toString()
        timerRounds.text = state.currentRound.toString()
    }

    override fun onDestroyView() {
        coroutineScope?.cancel()
        coroutineScope = null
        super.onDestroyView()
    }

    companion object {
        private const val EXTRA_TIMER_ID = "timerId"
    }
}
