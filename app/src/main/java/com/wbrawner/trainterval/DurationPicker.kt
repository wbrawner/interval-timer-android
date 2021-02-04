package com.wbrawner.trainterval

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.NumberPicker
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.compose.runtime.Composable
import androidx.core.math.MathUtils
import com.wbrawner.trainterval.shared.IntervalDuration
import com.wbrawner.trainterval.shared.toIntervalDuration
import com.wbrawner.trainterval.shared.toMillis
import java.util.concurrent.TimeUnit

private const val MIN_DURATION = 0L
private val MAX_DURATION = TimeUnit.DAYS.toMillis(1)

@Composable
fun DurationPicker() {
//
//    private var intervalDuration =
//        IntervalDuration()
//    var duration: Long
//        get() = intervalDuration.toMillis()
//        set(value) {
//            intervalDuration =
//                MathUtils.clamp(value, MIN_DURATION, MAX_DURATION).toIntervalDuration()
//            hours.value = intervalDuration.hours.toInt()
//            minutes.value = intervalDuration.minutes.toInt()
//            seconds.value = intervalDuration.seconds.toInt()
//        }
//    private val hours: NumberPicker
//    private val minutes: NumberPicker
//    private val seconds: NumberPicker
//
//    init {
//        val view = LayoutInflater.from(context).inflate(R.layout.duration_picker, this, true)
//        hours = view.findViewById(R.id.hours)
//        hours.minValue = 0
//        hours.maxValue = 23
//        hours.setOnValueChangedListener { _, _, newVal ->
//            intervalDuration = intervalDuration.copy(hours = newVal.toLong())
//            Log.v("DurationPicker", "Updated hours: $newVal")
//        }
//        hours.setFormatter(numberFormatter())
//        minutes = view.findViewById(R.id.minutes)
//        minutes.minValue = 0
//        minutes.maxValue = 59
//        minutes.setOnValueChangedListener { _, _, newVal ->
//            intervalDuration = intervalDuration.copy(minutes = newVal.toLong())
//            Log.v("DurationPicker", "Updated minutes: $newVal")
//        }
//        minutes.setFormatter(numberFormatter())
//        seconds = view.findViewById(R.id.seconds)
//        seconds.minValue = 0
//        seconds.maxValue = 59
//        seconds.setOnValueChangedListener { _, _, newVal ->
//            intervalDuration = intervalDuration.copy(seconds = newVal.toLong())
//            Log.v("DurationPicker", "Updated seconds: $newVal")
//        }
//        seconds.setFormatter(numberFormatter())
//    }
//
//    private fun numberFormatter() = NumberPicker.Formatter { "%02d".format(it) }
}
