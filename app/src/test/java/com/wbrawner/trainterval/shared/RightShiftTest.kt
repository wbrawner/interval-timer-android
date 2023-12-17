package com.wbrawner.trainterval.shared

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class RightShiftTest(
    private val durationString: String,
    private val expectedRightShift: String
) {

    @Test
    fun leftShiftTest() {
        assertEquals(expectedRightShift, durationString.shiftRight())
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "duration string \"{0}\"")
        fun data() = arrayListOf<Array<Any?>>(
            arrayOf("00:00:00", "00:00:00"),
            arrayOf("11:11:1", "01:11:11"),
            arrayOf("00:00:0", "00:00:00"),
            arrayOf("12:34:5", "01:23:45"),
            arrayOf("123:45:6", "12:34:56"),
        )
    }
}