package com.wbrawner.trainterval.timerform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wbrawner.trainterval.shared.IntervalTimer
import com.wbrawner.trainterval.shared.IntervalTimerDao
import com.wbrawner.trainterval.timerform.IntervalTimerEditState.EditTimerSavedState
import com.wbrawner.trainterval.timerform.IntervalTimerEditState.EditTimerState
import com.wbrawner.trainterval.timerform.IntervalTimerEditState.LoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TimerFormViewModel(
    private val timerDao: IntervalTimerDao,
    private val logger: Timber.Tree
) : ViewModel() {
    private lateinit var timer: IntervalTimer
    private val _timerState: MutableStateFlow<IntervalTimerEditState> = MutableStateFlow(LoadingState)
    val timerState: StateFlow<IntervalTimerEditState> = _timerState.asStateFlow()

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
            _timerState.emit(EditTimerState(timer))
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
        viewModelScope.launch {
            _timerState.emit(LoadingState)
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
            _timerState.emit(EditTimerSavedState(timer))
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
