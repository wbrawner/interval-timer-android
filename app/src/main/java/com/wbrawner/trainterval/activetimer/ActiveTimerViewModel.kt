package com.wbrawner.trainterval.activetimer

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wbrawner.trainterval.Logger
import com.wbrawner.trainterval.R
import com.wbrawner.trainterval.activetimer.IntervalTimerActiveState.LoadingState
import com.wbrawner.trainterval.activetimer.IntervalTimerActiveState.TimerRunningState
import com.wbrawner.trainterval.model.IntervalTimer
import com.wbrawner.trainterval.model.IntervalTimerDao
import com.wbrawner.trainterval.model.Phase
import com.wbrawner.trainterval.toIntervalDuration
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ActiveTimerViewModel : ViewModel() {
    val timerState: MutableLiveData<IntervalTimerActiveState> = MutableLiveData(LoadingState)
    private var timerJob: Job? = null
    private lateinit var timer: IntervalTimer
    private lateinit var logger: Logger
    private var timerComplete = false
    private var currentPhase = Phase.WARM_UP
    private var currentSet = 1
    private var currentRound = 1
    private var timeRemaining: Long = 0

    suspend fun init(
        logger: Logger,
        timerDao: IntervalTimerDao,
        timerId: Long
    ) {
        this.logger = logger
        if (timerJob == null || timer.id != timerId) {
            logger.d(message = "Initializing with Timer id $timerId")
            timer = timerDao.getById(timerId)
            timeRemaining = timer.warmUpDuration
            timerState.postValue(
                TimerRunningState(
                    timer,
                    timeRemaining,
                    currentSet,
                    currentRound,
                    currentPhase,
                    timerJob != null
                )
            )
        }
    }

    fun toggleTimer() {
        if (timerJob != null) {
            timerJob?.cancel()
            timerJob = null
            timerState.postValue(
                TimerRunningState(
                    timer,
                    timeRemaining,
                    currentSet,
                    currentRound,
                    currentPhase,
                    timerJob != null
                )
            )
        } else {
            viewModelScope.launch {
                startTimer()
            }
        }
    }

    private fun startTimer() {
        viewModelScope.launch {
            timerJob = launch {
                updateTimer()
                while (coroutineContext.isActive && timerJob != null) {
                    delay(1_000)
                    timeRemaining -= 1_000
                    if (timeRemaining <= 0) {
                        goForward()
                    }
                    updateTimer()
                }
            }
        }
    }

    fun skipAhead() {
        timerJob?.cancel()
        when (currentPhase) {
            Phase.COOL_DOWN -> {
                timeRemaining = 0
            }
            else -> {
                goForward()
            }
        }
        if (timerJob != null) {
            startTimer()
        } else {
            updateTimer()
        }
    }

    private fun updateTimer() {
        timerState.postValue(
            TimerRunningState(
                timer,
                timeRemaining,
                currentSet,
                currentRound,
                currentPhase,
                timerJob != null
            )
        )
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
                    currentSet < timer.sets -> {
                        currentSet++
                        currentPhase = Phase.LOW_INTENSITY
                        timeRemaining = timer.lowIntensityDuration
                    }
                    currentRound < timer.cycles -> {
                        currentRound++
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
                currentSet = 1
                currentPhase = Phase.LOW_INTENSITY
                timeRemaining = timer.lowIntensityDuration
            }
            Phase.COOL_DOWN -> {
                timeRemaining = 0
                timerJob?.cancel()
                timerJob = null
            }
        }
    }

    fun goBack() {
        timerJob?.cancel()
        when (currentPhase) {
            Phase.WARM_UP -> {
                timeRemaining = timer.warmUpDuration
            }
            Phase.LOW_INTENSITY -> {
                when {
                    currentSet == 1 && currentRound == 1 -> {
                        currentPhase = Phase.WARM_UP
                        timeRemaining = timer.warmUpDuration
                    }
                    currentSet == 1 && currentRound > 1 -> {
                        currentPhase = Phase.REST
                        timeRemaining = timer.restDuration
                    }
                    else -> {
                        currentSet--
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
                currentRound--
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
            startTimer()
        } else {
            updateTimer()
        }
    }
}

/**
 * Used to represent the state while a user has a specific timer open.
 */
sealed class IntervalTimerActiveState {
    object LoadingState : IntervalTimerActiveState()
    class TimerRunningState(
        val timerName: String,
        val timeRemaining: String,
        val currentSet: Int,
        val totalSets: Int,
        val currentRound: Int,
        val totalRounds: Int,
        @ColorRes val timerBackground: Int,
        @DrawableRes val playPauseIcon: Int
    ) : IntervalTimerActiveState() {
        constructor(
            timer: IntervalTimer,
            timeRemaining: Long,
            currentSet: Int,
            currentRound: Int,
            phase: Phase,
            timerRunning: Boolean
        ) : this(
            timerName = timer.name,
            timeRemaining = timeRemaining.toIntervalDuration().toString(),
            currentSet = currentSet,
            currentRound = currentRound,
            totalSets = timer.sets,
            totalRounds = timer.cycles,
            timerBackground = phase.colorRes,
            playPauseIcon = if (timerRunning) R.drawable.ic_pause else R.drawable.ic_play_arrow
        )
    }

    object ExitState : IntervalTimerActiveState()
}
