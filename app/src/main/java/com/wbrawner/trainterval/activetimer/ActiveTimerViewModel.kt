package com.wbrawner.trainterval.activetimer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wbrawner.trainterval.shared.IntervalTimer
import com.wbrawner.trainterval.shared.IntervalTimerDao
import com.wbrawner.trainterval.shared.IntervalTimerEffects
import com.wbrawner.trainterval.shared.IntervalTimerState
import com.wbrawner.trainterval.shared.IntervalTimerState.LoadingState
import com.wbrawner.trainterval.shared.IntervalTimerState.TimerRunningState
import com.wbrawner.trainterval.shared.Phase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ActiveTimerViewModel @Inject constructor(
    private val timerDao: IntervalTimerDao
) : ViewModel() {
    val timerState: MutableStateFlow<IntervalTimerState> = MutableStateFlow(LoadingState)
    val effects: MutableSharedFlow<IntervalTimerEffects> = MutableSharedFlow()
    private var timerJob: Job? = null
    private lateinit var timer: IntervalTimer
    private var timerComplete = false
    private var currentPhase = Phase.WARM_UP
    private var currentSet = 1
    private var currentRound = 1
    private var timeRemaining: Long = 0
    fun loadTimer(timerId: Long) {
        if (timerJob == null || timer.id != timerId) {
            Timber.d("Initializing with Timer id $timerId")
            viewModelScope.launch {
                timer = timerDao.getById(timerId)
                currentSet = timer.sets
                currentRound = timer.cycles
                timeRemaining = timer.warmUpDuration
                currentPhase = Phase.WARM_UP
                updateTimer(null)
            }
        }
    }

    fun toggleTimer() {
        if (timerJob != null) {
            timerJob?.cancel()
            timerJob = null
            updateTimer(null)
        } else {
            viewModelScope.launch {
                startTimer(null)
            }
        }
    }

    private fun startTimer(previousPhase: Phase?) {
        viewModelScope.launch {
            if (currentPhase == Phase.WARM_UP && timeRemaining == timer.warmUpDuration) {
                effects.emit(IntervalTimerEffects.PlaySound(currentPhase))
            }
            timerJob = launch {
                updateTimer(previousPhase)
                while (coroutineContext.isActive && timerJob != null) {
                    delay(1_000)
                    timeRemaining -= 1
                    // We need to recalculate the previous phase on each iteration
                    val previousPhaseOngoing = currentPhase
                    if (timeRemaining <= 0) {
                        goForward()
                    }
                    updateTimer(if (previousPhaseOngoing != currentPhase) previousPhaseOngoing else null)
                }
            }
        }
    }

    fun skipAhead() {
        timerJob?.cancel()
        var previousPhase: Phase? = null
        when (currentPhase) {
            Phase.COOL_DOWN -> {
                timeRemaining = 0
            }

            else -> {
                previousPhase = currentPhase
                goForward()
            }
        }
        if (timerJob != null) {
            startTimer(previousPhase)
        } else {
            updateTimer(previousPhase)
        }
    }

    private fun updateTimer(previousPhase: Phase?) {
        viewModelScope.launch {
            val state = TimerRunningState(
                timer,
                timeRemaining,
                currentSet,
                currentRound,
                currentPhase,
                previousPhase,
                timerJob != null
            )
            timerState.emit(state)
        }
    }

    private fun goForward() {
        timerComplete = currentPhase == Phase.COOL_DOWN
        when (currentPhase) {
            Phase.WARM_UP -> {
                currentPhase = Phase.LOW_INTENSITY
                timeRemaining = timer.lowIntensityDuration
            }

            Phase.LOW_INTENSITY -> {
                currentPhase = Phase.HIGH_INTENSITY
                timeRemaining = timer.highIntensityDuration
            }

            Phase.HIGH_INTENSITY -> {
                when {
                    currentSet > 1 -> {
                        currentSet--
                        currentPhase = Phase.LOW_INTENSITY
                        timeRemaining = timer.lowIntensityDuration
                    }

                    currentRound > 1 -> {
                        currentRound--
                        currentPhase = Phase.REST
                        timeRemaining = timer.restDuration
                    }

                    else -> {
                        currentPhase = Phase.COOL_DOWN
                        timeRemaining = timer.coolDownDuration
                    }
                }
            }

            Phase.REST -> {
                currentSet = timer.sets
                currentPhase = Phase.LOW_INTENSITY
                timeRemaining = timer.lowIntensityDuration
            }

            Phase.COOL_DOWN -> {
                timeRemaining = 0
                timerJob?.cancel()
                timerJob = null
            }
        }
        if ((timerState.value as? TimerRunningState)?.isRunning == true) {
            viewModelScope.launch {
                effects.emit(IntervalTimerEffects.PlaySound(currentPhase))
            }
        }
    }

    fun goBack() {
        timerJob?.cancel()
        var previousPhase: Phase = currentPhase
        when (currentPhase) {
            Phase.WARM_UP -> {
                timeRemaining = timer.warmUpDuration
            }

            Phase.LOW_INTENSITY -> {
                when {
                    currentSet == timer.sets && currentRound == timer.cycles -> {
                        currentPhase = Phase.WARM_UP
                        timeRemaining = timer.warmUpDuration
                    }

                    currentSet == timer.sets && currentRound < timer.cycles -> {
                        currentPhase = Phase.REST
                        timeRemaining = timer.restDuration
                    }

                    else -> {
                        currentSet++
                        currentPhase = Phase.HIGH_INTENSITY
                        timeRemaining = timer.highIntensityDuration
                    }
                }
            }

            Phase.HIGH_INTENSITY -> {
                currentPhase = Phase.LOW_INTENSITY
                timeRemaining = timer.lowIntensityDuration
            }

            Phase.REST -> {
                currentRound++
                currentPhase = Phase.HIGH_INTENSITY
                currentSet = timer.sets
                timeRemaining = timer.highIntensityDuration
            }

            Phase.COOL_DOWN -> {
                currentPhase = Phase.HIGH_INTENSITY
                timeRemaining = timer.highIntensityDuration
            }
        }
        if (timerJob != null) {
            startTimer(previousPhase)
        } else {
            updateTimer(previousPhase)
        }
        if ((timerState.value as? TimerRunningState)?.isRunning == true) {
            viewModelScope.launch {
                effects.emit(IntervalTimerEffects.PlaySound(currentPhase))
            }
        }
    }
}
