package com.wbrawner.trainterval.activetimer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.wbrawner.trainterval.R
import kotlinx.android.synthetic.main.fragment_active_timer.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class ActiveTimerFragment : Fragment() {

    private var coroutineScope: CoroutineScope? = null
    private val activeTimerViewModel: ActiveTimerViewModel by inject()
    private var timerId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        timerId = requireArguments().getLong("timerId")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_active_timer, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope!!.launch {
            activeTimerViewModel.timerState.observe(viewLifecycleOwner, Observer { state ->
                when (state) {
                    is IntervalTimerActiveState.LoadingState -> renderLoading()
                    is IntervalTimerActiveState.TimerRunningState -> renderTimer(state)
                    is IntervalTimerActiveState.ExitState -> findNavController().navigateUp()
                }
            })
        }
        coroutineScope!!.launch {
            activeTimerViewModel.init(timerId)
        }
        playPauseButton.setOnClickListener {
            coroutineScope!!.launch {
                activeTimerViewModel.toggleTimer()
            }
        }
    }

    private fun renderLoading() {
        progressBar.visibility = View.VISIBLE
        timerLayout.visibility = View.GONE
    }

    private fun renderTimer(state: IntervalTimerActiveState.TimerRunningState) {
        progressBar.visibility = View.GONE
        timerLayout.visibility = View.VISIBLE
        playPauseButton.setImageDrawable(requireContext().getDrawable(state.playPauseIcon))
        timeRemaining.text = state.timeRemaining
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
