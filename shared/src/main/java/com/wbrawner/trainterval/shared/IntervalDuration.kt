package com.wbrawner.trainterval.shared

import timber.log.Timber
import java.util.concurrent.TimeUnit

data class IntervalDuration private constructor(
    val hours: Long,
    val minutes: Long,
    val seconds: Long
) {
    override fun toString(): String {
        return if (hours > 0) "%02d:%02d:%02d".format(hours, minutes, seconds)
        else "%02d:%02d".format(minutes, seconds)
    }

    fun toStringFull() = "%02d:%02d:%02d".format(hours, minutes, seconds)

    companion object {
        fun create(
            hours: Long = 0,
            minutes: Long = 0,
            seconds: Long = 0
        ): IntervalDuration {
            if (minutes > 59) {
                throw IllegalArgumentException("Invalid value for minutes: $minutes")
            }
            if (seconds > 59) {
                throw IllegalArgumentException("Invalid value for seconds: $seconds")
            }
            return IntervalDuration(hours, minutes, seconds)
        }

        fun parse(s: String): IntervalDuration? {
            val parts = s.split(":")
            if (parts.any { it.trim().matches(Regex("\\D")) }) {
                return null
            }
            val numbers = parts.map { it.toLongOrNull() ?: 0 }
            if (numbers.size !in 1..3) {
                return null
            }
            return when (numbers.size) {
                1 -> create(seconds = numbers.first())
                2 -> create(minutes = numbers[0], seconds = numbers[1])
                else -> try {
                    create(
                        hours = numbers[0],
                        minutes = numbers[1],
                        seconds = numbers[2],
                    )
                } catch (e: IllegalArgumentException) {
                    Timber.e("Failed to parse $s", e)
                    null
                }
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

    return requireNotNull(IntervalDuration.create(
        hours,
        minutes,
        seconds
    ))
}

fun String.shiftLeft(): String {
    val chars = toCharArray()
    if (chars.lastIndexOf(':') == chars.lastIndex - 2) {
        return this
    }
    for (i in chars.lastIndex downTo 0) {
        if (chars[i] == ':') {
            chars.swap(i, i + 1)
        }
    }
    return if (chars.first() == '0' && chars.indexOf(':') == 3) {
        chars.sliceArray(1..chars.lastIndex)
    } else {
        chars
    }.concatToString()
}

fun String.shiftRight(): String {
    val chars = toCharArray()
    if (chars.lastIndexOf(':') == chars.lastIndex - 2) {
        return this
    }
    for (i in 0..chars.lastIndex) {
        if (chars[i] == ':') {
            chars.swap(i, i - 1)
        }
    }
    return if (chars.indexOf(':') == 1) {
        CharArray(1) {'0'} + chars
    } else {
        chars
    }.concatToString()
}

fun CharArray.swap(firstIndex: Int, secondIndex: Int) {
    val char = get(firstIndex)
    set(firstIndex, get(secondIndex))
    set(secondIndex, char)
}
