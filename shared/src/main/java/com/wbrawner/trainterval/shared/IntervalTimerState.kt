package com.wbrawner.trainterval.shared

import com.google.android.gms.wearable.DataMap
import java.io.Serializable

/**
 * Used to represent the state while a user has a specific timer open.
 */
sealed interface IntervalTimerState : Serializable {
    object LoadingState : IntervalTimerState
    class TimerRunningState(
        val timerName: String,
        val timeRemaining: String,
        val currentSet: Int,
        val currentRound: Int,
        val soundId: Int?,
        val phase: Phase,
        val previousPhase: Phase?,
        val isRunning: Boolean,
        val vibrate: Boolean
    ) : IntervalTimerState {
        constructor(
            timer: IntervalTimer,
            timeRemaining: Long,
            currentSet: Int,
            currentRound: Int,
            phase: Phase,
            previousPhase: Phase?,
            timerRunning: Boolean
        ) : this(
            timerName = timer.name,
            phase = phase,
            previousPhase = previousPhase,
            timeRemaining = timeRemaining.toIntervalDuration().toString(),
            currentSet = currentSet,
            currentRound = currentRound,
            soundId = if (timerRunning && timeRemaining == timer.durationForPhase(phase))
                phase.ordinal
            else null,
            isRunning = timerRunning,
            vibrate = timerRunning && timeRemaining == timer.durationForPhase(phase)
        )
    }

    companion object {
        const val TIMER_STATE = "/timer/state"
        const val TIMER_ACTIONS_TOGGLE = "/timer/actions/toggle"
    }
}

sealed interface IntervalTimerEffects {
    object Close : IntervalTimerEffects
    data class PlaySound(val phase: Phase) : IntervalTimerEffects
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
const val KEY_PREVIOUS_PHASE = "com.wbrawner.trainterval.previousPhase"
const val KEY_RUNNING = "com.wbrawner.trainterval.timerRunning"
const val KEY_VIBRATE = "com.wbrawner.trainterval.vibrate"

fun IntervalTimerState.toDataMap(): DataMap {
    val dataMap = DataMap()
    when (this) {
        is IntervalTimerState.LoadingState -> dataMap.putString(KEY_STATE, STATE_LOADING)
        is IntervalTimerState.TimerRunningState -> {
            dataMap.putAll(this.toDataMap())
            dataMap.putString(KEY_STATE, STATE_ACTIVE)
        }

    }
    return dataMap
}

fun DataMap.toIntervalTimerState(): IntervalTimerState? = when (getString(KEY_STATE)) {
    STATE_LOADING -> IntervalTimerState.LoadingState
    STATE_ACTIVE -> IntervalTimerState.TimerRunningState(
        getString(KEY_TIMER_NAME, ""),
        getString(KEY_TIME_REMAINING, ""),
        getInt(KEY_CURRENT_SET),
        getInt(KEY_CURRENT_ROUND),
        getInt(KEY_SOUND_ID),
        Phase.valueOf(getString(KEY_PHASE, Phase.WARM_UP.name)),
        getString(KEY_PREVIOUS_PHASE)?.let { Phase.valueOf(it) },
        getBoolean(KEY_RUNNING),
        getBoolean(KEY_VIBRATE)
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
        state.previousPhase?.name?.let {
            putString(KEY_PREVIOUS_PHASE, it)
        }
        putBoolean(KEY_RUNNING, state.isRunning)
        putBoolean(KEY_VIBRATE, state.vibrate)
    }
}
