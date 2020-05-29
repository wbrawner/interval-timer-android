package com.wbrawner.trainterval

import java.util.concurrent.TimeUnit


data class IntervalDuration(
    val hours: Long = 0,
    val minutes: Long = 0,
    val seconds: Long = 0
) {
    override fun toString(): String {
        return "%02d:%02d:%02d".format(hours, minutes, seconds)
    }
}

fun IntervalDuration.toMillis(): Long {
    return TimeUnit.HOURS.toMillis(hours) +
            TimeUnit.MINUTES.toMillis(minutes) +
            TimeUnit.SECONDS.toMillis(seconds)
}

fun Long.toIntervalDuration(): IntervalDuration {
    val SECONDS_IN_HOUR = 3600
    val SECONDS_IN_MINUTE = 60

    if (this < 1000) {
        return IntervalDuration(0, 0, 0)
    }

    var seconds: Long = this / 1000
    var hours: Long = 0
    if (seconds >= SECONDS_IN_HOUR) {
        hours = seconds / SECONDS_IN_HOUR
        seconds -= hours * SECONDS_IN_HOUR
    }

    var minutes: Long = 0
    if (seconds >= SECONDS_IN_MINUTE) {
        minutes = seconds / SECONDS_IN_MINUTE
        seconds -= minutes * SECONDS_IN_MINUTE
    }

    return IntervalDuration(hours, minutes, seconds)
}