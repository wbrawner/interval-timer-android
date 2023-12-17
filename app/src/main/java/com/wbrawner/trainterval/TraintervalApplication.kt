package com.wbrawner.trainterval

import android.app.Application
import androidx.room.Room
import com.wbrawner.trainterval.shared.IntervalTimerDao
import com.wbrawner.trainterval.shared.TraintervalDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import javax.inject.Singleton

@HiltAndroidApp
class TraintervalApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}

@InstallIn(SingletonComponent::class)
@Module
object MainModule {
    @Singleton
    @Provides
    fun providesDatabase(app: Application): TraintervalDatabase = Room.databaseBuilder(
        app,
        TraintervalDatabase::class.java, "trainterval"
    ).build()

    @Singleton
    @Provides
    fun providesTimerDao(db: TraintervalDatabase): IntervalTimerDao = db.timerDao()
}