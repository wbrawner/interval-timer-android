package com.wbrawner.trainterval.timerform

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.wbrawner.trainterval.R
import com.wbrawner.trainterval.shared.IntervalTimer
import com.wbrawner.trainterval.timerform.IntervalTimerEditState.*
import kotlinx.android.synthetic.main.fragment_timer_form.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class TimerFormFragment : Fragment() {

    private var coroutineScope: CoroutineScope? = null
    private val timerFormViewModel: TimerFormViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    ): View? = inflater.inflate(R.layout.fragment_timer_form, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.let {
            it.setSupportActionBar(toolbar)
            it.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope!!.launch {
            timerFormViewModel.timerState.observe(viewLifecycleOwner, Observer { state ->
                when (state) {
                    is LoadingState -> renderLoading()
                    is EditTimerState -> renderEditingState(state.timer, state.title)
                    is EditTimerSavedState -> findNavController().navigate(R.id.timerListFragment)
                    is ErrorState -> renderErrorState(state.message)
                }
            })
        }
        coroutineScope!!.launch {
            timerFormViewModel.init(arguments?.getLong(EXTRA_TIMER_ID))
        }
        timerSets.minValue = 1
        timerSets.maxValue = 99
        timerRepeat.minValue = 1
        timerRepeat.maxValue = 99
        saveButton.setOnClickListener {
            coroutineScope?.launch {
                timerFormViewModel.saveTimer(
                    timerName.text.toString(),
                    timerDescription.text.toString(),
                    warmUpDuration.duration,
                    lowIntensityDuration.duration,
                    highIntensityDuration.duration,
                    restDuration.duration,
                    coolDownDuration.duration,
                    timerSets.value,
                    timerRepeat.value
                )
            }
        }
    }

    private fun renderLoading() {
        timerForm.visibility = View.GONE
        error.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
    }

    private fun renderEditingState(timer: IntervalTimer, title: String) {
        toolbar.title = title
        timerForm.visibility = View.VISIBLE
        timerName.setText(timer.name)
        timerDescription.setText(timer.description)
        warmUpDuration.duration = timer.warmUpDuration
        lowIntensityDuration.duration = timer.lowIntensityDuration
        highIntensityDuration.duration = timer.highIntensityDuration
        restDuration.duration = timer.restDuration
        coolDownDuration.duration = timer.coolDownDuration
        timerSets.value = timer.sets
        timerRepeat.value = timer.cycles
        error.visibility = View.GONE
        progressBar.visibility = View.GONE
    }

    private fun renderErrorState(message: String) {
        timerForm.visibility = View.GONE
        error.visibility = View.VISIBLE
        error.text = message
        progressBar.visibility = View.GONE
    }

    override fun onDestroyView() {
        coroutineScope?.cancel()
        coroutineScope = null
        super.onDestroyView()
    }

    companion object {
        const val EXTRA_TIMER_ID = "timerId"
    }
}
