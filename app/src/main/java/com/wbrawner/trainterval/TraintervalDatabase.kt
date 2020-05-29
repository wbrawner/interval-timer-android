package com.wbrawner.trainterval

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wbrawner.trainterval.model.IntervalTimer
import com.wbrawner.trainterval.model.IntervalTimerDao

@Database(entities = [IntervalTimer::class], version = 1)
abstract class TraintervalDatabase : RoomDatabase() {
    abstract fun timerDao(): IntervalTimerDao
}