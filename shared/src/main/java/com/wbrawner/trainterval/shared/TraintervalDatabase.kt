package com.wbrawner.trainterval.shared

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [IntervalTimer::class], version = 1)
abstract class TraintervalDatabase : RoomDatabase() {
    abstract fun timerDao(): IntervalTimerDao
}