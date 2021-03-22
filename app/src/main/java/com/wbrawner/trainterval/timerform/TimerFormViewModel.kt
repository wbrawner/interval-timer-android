package com.wbrawner.trainterval.timerform

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wbrawner.trainterval.shared.IntervalTimer
import com.wbrawner.trainterval.shared.IntervalTimerDao
import com.wbrawner.trainterval.timerform.IntervalTimerEditState.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TimerFormViewModel(
    private val timerDao: IntervalTimerDao,
    private val logger: Timber.Tree
) : ViewModel() {
    private lateinit var timer: IntervalTimer
    val timerState: MutableLiveData<IntervalTimerEditState> = MutableLiveData(LoadingState)

    @Inject
    constructor(timerDao: IntervalTimerDao) : this(timerDao, Timber.tag("TimerFormViewModel"))

    fun loadTimer(timerId: Long? = null) {
        viewModelScope.launch {
            timer = if (timerId == null) {
                IntervalTimer()
            } else {
                timerDao.getById(timerId)
            }
            logger.v("Timer: \n$timer")
            timerState.postValue(EditTimerState(timer))
        }
    }

    fun saveTimer(
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
        viewModelScope.launch {
            if (timer.id != null) {
                timerDao.update(timer)
            } else {
                timer = timer.copy(id = timerDao.insert(timer))
            }
            timerState.postValue(EditTimerSavedState(timer))
        }
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
