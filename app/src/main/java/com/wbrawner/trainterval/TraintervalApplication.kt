package com.wbrawner.trainterval

import android.app.Application
import androidx.room.Room
import com.wbrawner.trainterval.timerform.TimerFormViewModel
import com.wbrawner.trainterval.timerlist.TimerListViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

class TraintervalApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@TraintervalApplication)
            modules(traintervalModule)
        }

        val lifecycleCallbacks =
            TraintervalActivityLifecycleCallbacks(AndroidLogger("LifecycleCallbacks"))
        registerActivityLifecycleCallbacks(lifecycleCallbacks)
    }
}

val traintervalModule = module {
    single {
        Room.databaseBuilder(get(), TraintervalDatabase::class.java, "trainterval")
            .build()
    }

    single {
        get<TraintervalDatabase>().timerDao()
    }

    single {
        TimerListViewModel(get(parameters = { parametersOf("TimerListStore") }), get())
    }

    factory {
        TimerFormViewModel(get(parameters = { parametersOf("TimerFormStore") }), get())
    }

    factory<Logger> { params ->
        AndroidLogger(params.component1())
    }
}