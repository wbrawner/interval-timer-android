package com.wbrawner.trainterval.activetimer

import androidx.annotation.DrawableRes
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wbrawner.trainterval.Logger
import com.wbrawner.trainterval.R
import com.wbrawner.trainterval.activetimer.IntervalTimerActiveState.LoadingState
import com.wbrawner.trainterval.activetimer.IntervalTimerActiveState.TimerRunningState
import com.wbrawner.trainterval.model.IntervalTimer
import com.wbrawner.trainterval.model.IntervalTimerDao
import com.wbrawner.trainterval.model.Phase
import com.wbrawner.trainterval.toIntervalDuration
import kotlinx.coroutines.*

class ActiveTimerViewModel(
    private val logger: Logger,
    private val timerDao: IntervalTimerDao
) : ViewModel() {
    val timerState: MutableLiveData<IntervalTimerActiveState> = MutableLiveData(LoadingState)
    private var timerJob: Job? = null
    private lateinit var timer: IntervalTimer
    private var timerComplete = false
    private var timerRunning = false
    private var currentPhase = Phase.WARM_UP
    private var currentSet = 1
    private var currentRound = 1
    private var timeRemaining: Long = 0

    suspend fun init(timerId: Long) {
        logger.d(message = "Initializing with Timer id $timerId")
        timer = timerDao.getById(timerId)
        timeRemaining = timer.warmUpDuration
        timerState.postValue(
            TimerRunningState(
                timerRunning,
                timeRemaining.toIntervalDuration().toString(),
                currentSet,
                timer.sets,
                currentRound,
                timer.cycles
            )
        )
    }

    suspend fun toggleTimer() {
        if (timerRunning) {
            timerJob?.cancel()
            timerRunning = false
            timerState.postValue(
                TimerRunningState(
                    timerRunning,
                    timeRemaining.toIntervalDuration().toString(),
                    currentSet,
                    timer.sets,
                    currentRound,
                    timer.cycles
                )
            )
        } else {
            startTimer()
        }
    }

    private suspend fun startTimer() {
        coroutineScope {
            timerJob = launch {
                timerRunning = true
                timerState.postValue(
                    TimerRunningState(
                        timerRunning,
                        timeRemaining.toIntervalDuration().toString(),
                        currentSet,
                        timer.sets,
                        currentRound,
                        timer.cycles
                    )
                )
                while (coroutineContext.isActive && timerRunning) {
                    delay(1_000)
                    timeRemaining -= 1_000
                    if (timeRemaining <= 0) {
                        goForward()
                    }
                    timerState.postValue(
                        TimerRunningState(
                            timerRunning,
                            timeRemaining.toIntervalDuration().toString(),
                            currentSet,
                            timer.sets,
                            currentRound,
                            timer.cycles
                        )
                    )
                }
            }
        }
    }

    suspend fun skipAhead() {
        timerJob?.cancel()
        when (currentPhase) {
            Phase.COOL_DOWN -> {
                timeRemaining = 0
            }
            else -> {
                goForward()
            }
        }
        if (timerRunning) {
            startTimer()
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
                timerRunning = false
            }
        }
    }

    suspend fun goBack() {
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
                        currentPhase = Phase.HIGH_INTENSITY
                        timeRemaining = timer.highIntensityDuration
                    }
                }
                timeRemaining = timer.highIntensityDuration
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
        if (timerRunning) {
            startTimer()
        }
    }
}

/**
 * Used to represent the state while a user has a specific timer open.
 */
sealed class IntervalTimerActiveState {
    object LoadingState : IntervalTimerActiveState()
    class TimerRunningState(
        timerRunning: Boolean,
        val timeRemaining: String,
        val currentSet: Int,
        val totalSets: Int,
        val currentRound: Int,
        val totalRounds: Int,
        val timerComplete: Boolean = false,
        @DrawableRes val playPauseIcon: Int = if (timerRunning) R.drawable.ic_pause else R.drawable.ic_play_arrow
    ) : IntervalTimerActiveState()
    object ExitState : IntervalTimerActiveState()
}
