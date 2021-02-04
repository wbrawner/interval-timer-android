package com.wbrawner.trainterval.timerlist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wbrawner.trainterval.Logger
import com.wbrawner.trainterval.shared.IntervalTimer
import com.wbrawner.trainterval.shared.IntervalTimerDao
import com.wbrawner.trainterval.timerlist.IntervalTimerListState.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class TimerListViewModel(
    private val logger: Logger,
    val timerDao: IntervalTimerDao
) : ViewModel() {
    val timerState: MutableLiveData<IntervalTimerListState> = MutableLiveData(LoadingState)
    private val timers = ArrayList<IntervalTimer>()

    init {
        GlobalScope.launch {
            timerDao.getAll()
                .collect {
                    logger.d(message = "Received updated intervaltimer list")
                    logger.d(message = it.toString())
                    timers.clear()
                    timers.addAll(it)
                    if (timers.isEmpty()) {
                        timerState.postValue(EmptyListState)
                    } else {
                        timerState.postValue(SuccessListState(timers))
                    }
                }
        }
    }

    fun addTimer() {
        timerState.value = CreateTimer
        if (timers.isEmpty()) {
            timerState.postValue(EmptyListState)
        } else {
            timerState.postValue(SuccessListState(timers))
        }
    }

    suspend fun editTimer(timer: IntervalTimer) {

    }

    suspend fun deleteTimer(timer: IntervalTimer) {

    }

    suspend fun confirmDeleteTimer(timer: IntervalTimer) {

    }

    fun openTimer(timer: IntervalTimer) {
        timerState.value = OpenTimer(timer.id!!)
        if (timers.isEmpty()) {
            timerState.postValue(EmptyListState)
        } else {
            timerState.postValue(SuccessListState(timers))
        }
    }
}

/**
 * Used to represent each state on the main list view.
 */
sealed class IntervalTimerListState {
    object LoadingState : IntervalTimerListState()
    object EmptyListState : IntervalTimerListState()
    data class ConfirmDeleteTimerState(val timer: IntervalTimer) : IntervalTimerListState()
    data class SuccessListState(val timers: List<IntervalTimer>) : IntervalTimerListState()
    data class ErrorState(val message: String) : IntervalTimerListState()
    object CreateTimer : IntervalTimerListState()
    data class EditTimer(val timerId: Long) : IntervalTimerListState()
    data class OpenTimer(val timerId: Long) : IntervalTimerListState()
}
