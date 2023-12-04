package com.wbrawner.trainterval.shared

import java.util.concurrent.TimeUnit


data class IntervalDuration(
    val hours: Long = 0,
    val minutes: Long = 0,
    val seconds: Long = 0
) {
    override fun toString(): String {
        return if (hours > 0) "%02d:%02d:%02d".format(hours, minutes, seconds)
        else "%02d:%02d".format(minutes, seconds)
    }

    companion object {
        fun parse(s: String): IntervalDuration? {
            val parts = s.split(":").map { it.toLongOrNull() }
            if (parts.size !in 1..3 || parts.any { it == null }) {
                return null
            }
            return when (parts.size) {
                1 -> IntervalDuration(seconds = parts[0]!!)
                2 -> IntervalDuration(minutes = parts[0]!!, seconds = parts[1]!!)
                else -> IntervalDuration(
                    hours = parts[0]!!,
                    minutes = parts[1]!!,
                    seconds = parts[2]!!,
                )
            }
        }
    }
}

fun IntervalDuration.toMillis(): Long {
    return TimeUnit.HOURS.toMillis(hours) +
            TimeUnit.MINUTES.toMillis(minutes) +
            TimeUnit.SECONDS.toMillis(seconds)
}

fun IntervalDuration.toSeconds(): Long {
    return TimeUnit.HOURS.toSeconds(hours) +
            TimeUnit.MINUTES.toSeconds(minutes) +
            seconds
}

private const val SECONDS_IN_HOUR = 3600
private const val SECONDS_IN_MINUTE = 60

fun Long.toIntervalDuration(): IntervalDuration {
    var seconds: Long = this
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

    return IntervalDuration(
        hours,
        minutes,
        seconds
    )
}