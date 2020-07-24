package com.wbrawner.trainterval.shared

import com.google.android.gms.wearable.DataMap
import java.io.Serializable

/**
 * Used to represent the state while a user has a specific timer open.
 */
sealed class IntervalTimerState : Serializable {
    object LoadingState : IntervalTimerState()
    class TimerRunningState(
        val timerName: String,
        val timeRemaining: String,
        val currentSet: Int,
        val currentRound: Int,
        val soundId: Int?,
        val phase: Phase,
        val isRunning: Boolean
    ) : IntervalTimerState() {
        constructor(
            timer: IntervalTimer,
            timeRemaining: Long,
            currentSet: Int,
            currentRound: Int,
            phase: Phase,
            timerRunning: Boolean
        ) : this(
            timerName = timer.name,
            phase = phase,
            timeRemaining = timeRemaining.toIntervalDuration().toString(),
            currentSet = currentSet,
            currentRound = currentRound,
            soundId = if (timerRunning && timeRemaining == timer.durationForPhase(phase))
                phase.ordinal
            else null,
            isRunning = timerRunning
        )
    }

    object ExitState : IntervalTimerState()

    companion object {
        const val TIMER_STATE = "/timer/state"
        const val TIMER_ACTIONS_TOGGLE = "/timer/actions/toggle"
    }
}

const val KEY_STATE = "com.wbrawner.trainterval.timerState"
const val STATE_LOADING = "com.wbrawner.trainterval.timerLoading"
const val STATE_ACTIVE = "com.wbrawner.trainterval.timerActive"
const val STATE_EXIT = "com.wbrawner.trainterval.timerExit"
const val KEY_TIMER_NAME = "com.wbrawner.trainterval.timerName"
const val KEY_TIME_REMAINING = "com.wbrawner.trainterval.timeRemaining"
const val KEY_CURRENT_SET = "com.wbrawner.trainterval.currentSet"
const val KEY_CURRENT_ROUND = "com.wbrawner.trainterval.currentRound"
const val KEY_SOUND_ID = "com.wbrawner.trainterval.soundId"
const val KEY_PHASE = "com.wbrawner.trainterval.phase"
const val KEY_RUNNING = "com.wbrawner.trainterval.timerRunning"

fun IntervalTimerState.toDataMap(): DataMap {
    val dataMap = DataMap()
    when (this) {
        is IntervalTimerState.LoadingState -> dataMap.putString(KEY_STATE, STATE_LOADING)
        is IntervalTimerState.TimerRunningState -> {
            dataMap.putAll(this.toDataMap())
            dataMap.putString(KEY_STATE, STATE_ACTIVE)
        }
        is IntervalTimerState.ExitState -> dataMap.putString(KEY_STATE, STATE_EXIT)
    }
    return dataMap
}

fun DataMap.toIntervalTimerState(): IntervalTimerState? = when (getString(KEY_STATE)) {
    STATE_LOADING -> IntervalTimerState.LoadingState
    STATE_EXIT -> IntervalTimerState.ExitState
    STATE_ACTIVE -> IntervalTimerState.TimerRunningState(
        getString(KEY_TIMER_NAME),
        getString(KEY_TIME_REMAINING),
        getInt(KEY_CURRENT_SET),
        getInt(KEY_CURRENT_ROUND),
        getInt(KEY_SOUND_ID),
        Phase.valueOf(getString(KEY_PHASE)),
        getBoolean(KEY_RUNNING)
    )
    else -> null
}

fun IntervalTimerState.TimerRunningState.toDataMap(): DataMap {
    val state = this
    return DataMap().apply {
        putString(KEY_TIMER_NAME, state.timerName)
        putString(KEY_TIME_REMAINING, state.timeRemaining)
        putInt(KEY_CURRENT_SET, state.currentSet)
        putInt(KEY_CURRENT_ROUND, state.currentRound)
        state.soundId?.let {
            putInt(KEY_SOUND_ID, it)
        }
        putString(KEY_PHASE, state.phase.name)
        putBoolean(KEY_RUNNING, state.isRunning)
    }
}
