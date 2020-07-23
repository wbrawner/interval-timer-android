package com.wbrawner.trainterval.activetimer

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wbrawner.trainterval.Logger
import com.wbrawner.trainterval.shared.IntervalTimer
import com.wbrawner.trainterval.shared.IntervalTimerDao
import com.wbrawner.trainterval.shared.IntervalTimerState
import com.wbrawner.trainterval.shared.IntervalTimerState.LoadingState
import com.wbrawner.trainterval.shared.IntervalTimerState.TimerRunningState
import com.wbrawner.trainterval.shared.Phase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ActiveTimerViewModel : ViewModel() {
    val timerState: MutableLiveData<IntervalTimerState> = MutableLiveData(LoadingState)
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
            currentSet = timer.sets
            currentRound = timer.cycles
            timeRemaining = timer.warmUpDuration
            currentPhase = Phase.WARM_UP
            updateTimer()
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
    }

    fun goBack() {
        timerJob?.cancel()
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
            startTimer()
        } else {
            updateTimer()
        }
    }
}
