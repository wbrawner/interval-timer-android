package com.wbrawner.trainterval.shared

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class IntervalDurationParseTest(
    private val durationString: String,
    private val expectedDuration: IntervalDuration?
) {

    @Test
    fun parseTest() {
        assertEquals(expectedDuration, IntervalDuration.parse(durationString))
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "duration string \"{0}\"")
        fun data() = arrayListOf<Array<Any?>>(
            arrayOf("", IntervalDuration.create()),
            arrayOf(":", IntervalDuration.create()),
            arrayOf("a", null),
            arrayOf("a:b", null),
            arrayOf("0", IntervalDuration.create()),
            arrayOf("00", IntervalDuration.create()),
            arrayOf(":00", IntervalDuration.create()),
            arrayOf("000", IntervalDuration.create()),
            arrayOf("00:00", IntervalDuration.create()),
            arrayOf("00:00:00", IntervalDuration.create()),
            arrayOf("1", IntervalDuration.create(seconds = 1)),
            arrayOf("01", IntervalDuration.create(seconds = 1)),
            arrayOf(":01", IntervalDuration.create(seconds = 1)),
            arrayOf("10:01", IntervalDuration.create(minutes = 10, seconds = 1)),
            arrayOf(":10:01", IntervalDuration.create(minutes = 10, seconds = 1)),
            arrayOf("01:01:01", IntervalDuration.create(hours = 1, minutes = 1, seconds = 1)),
            arrayOf("1:1:1", IntervalDuration.create(hours = 1, minutes = 1, seconds = 1)),
            arrayOf("001:001:001", IntervalDuration.create(hours = 1, minutes = 1, seconds = 1)),
            arrayOf("999:10:01", IntervalDuration.create(hours = 999, minutes = 10, seconds = 1)),
            arrayOf("999:10:999", null),
            arrayOf("999:999:999", null),
        )
    }
}