package com.wbrawner.trainterval.timerform

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wbrawner.trainterval.Logger
import com.wbrawner.trainterval.shared.IntervalTimer
import com.wbrawner.trainterval.shared.IntervalTimerDao
import com.wbrawner.trainterval.timerform.IntervalTimerEditState.*

class TimerFormViewModel(
    private val logger: Logger,
    private val timerDao: IntervalTimerDao
) : ViewModel() {
    private lateinit var timer: IntervalTimer
    val timerState: MutableLiveData<IntervalTimerEditState> = MutableLiveData(LoadingState)

    suspend fun init(timerId: Long? = null) {
        timer = if (timerId == null) {
            IntervalTimer()
        } else {
            timerDao.getById(timerId)
        }
        logger.v(message = "Timer: ")
        logger.v(message = timer.toString())
        timerState.postValue(EditTimerState(timer))
    }

    suspend fun saveTimer(
        name: String,
        description: String,
        warmUpDuration: Long,
        lowIntensityDuration: Long,
        highIntensityDuration: Long,
        restDuration: Long,
        coolDownDuration: Long,
        sets: Int,
        cycles: Int
    ) {
        timerState.postValue(LoadingState)
        timer = timer.copy(
            name = name,
            description = description,
            warmUpDuration = warmUpDuration,
            lowIntensityDuration = lowIntensityDuration,
            highIntensityDuration = highIntensityDuration,
            restDuration = restDuration,
            coolDownDuration = coolDownDuration,
            sets = sets,
            cycles = cycles
        )
        if (timer.id != null) {
            timerDao.update(timer)
        } else {
            timer = timer.copy(id = timerDao.insert(timer))
        }
        timerState.postValue(EditTimerSavedState(timer))
    }
}

/**
 * Used to represent each state while a user is creating or editing a timer.
 */
sealed class IntervalTimerEditState {
    object LoadingState : IntervalTimerEditState()
    class EditTimerState(
        val timer: IntervalTimer,
        val title: String = if (timer.id != null) "Edit Timer" else "Add Timer",
        val showDeleteButton: Boolean = timer.id != null
    ) : IntervalTimerEditState()

    class EditTimerSavedState(val timer: IntervalTimer) : IntervalTimerEditState()
    class ErrorState(val message: String) : IntervalTimerEditState()
}
