package com.wbrawner.trainterval.shared

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * Used to represent the state while a user has a specific timer open.
 */
sealed class IntervalTimerState {
    object LoadingState : IntervalTimerState()
    class TimerRunningState(
        val timerName: String,
        val timeRemaining: String,
        val currentSet: Int,
        val currentRound: Int,
        val soundId: Int?,
        @StringRes val phase: Int,
        @ColorRes val timerBackground: Int,
        @DrawableRes val playPauseIcon: Int
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
            phase = phase.stringRes,
            timeRemaining = timeRemaining.toIntervalDuration().toString(),
            currentSet = currentSet,
            currentRound = currentRound,
            timerBackground = phase.colorRes,
            soundId = if (timerRunning && timeRemaining == timer.durationForPhase(phase))
                phase.ordinal
            else null,
            playPauseIcon = if (timerRunning) R.drawable.ic_pause else R.drawable.ic_play_arrow
        )
    }

    object ExitState : IntervalTimerState()
}
