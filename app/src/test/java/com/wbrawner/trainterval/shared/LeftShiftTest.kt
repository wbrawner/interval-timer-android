package com.wbrawner.trainterval.shared

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class LeftShiftTest(
    private val durationString: String,
    private val expectedLeftShift: String
) {

    @Test
    fun leftShiftTest() {
        assertEquals(expectedLeftShift, durationString.shiftLeft())
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "duration string \"{0}\"")
        fun data() = arrayListOf<Array<Any?>>(
            arrayOf("00:00:00", "00:00:00"),
            arrayOf("11:11:11", "11:11:11"),
            arrayOf("00:00:001", "00:00:01"),
            arrayOf("01:23:456", "12:34:56"),
            arrayOf("12:34:567", "123:45:67"),
        )
    }
}