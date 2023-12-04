package com.wbrawner.trainterval.timerlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wbrawner.trainterval.shared.IntervalTimer
import com.wbrawner.trainterval.shared.IntervalTimerDao
import com.wbrawner.trainterval.timerlist.IntervalTimerListState.EmptyListState
import com.wbrawner.trainterval.timerlist.IntervalTimerListState.LoadingState
import com.wbrawner.trainterval.timerlist.IntervalTimerListState.SuccessListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TimerListViewModel(
    private val timerDao: IntervalTimerDao,
    private val logger: Timber.Tree
) : ViewModel() {
    val timerState: MutableStateFlow<IntervalTimerListState> = MutableStateFlow(LoadingState)
    val effects: MutableSharedFlow<IntervalTimerListEffects> = MutableSharedFlow()

    // Had to separate this constructor like so for Hilt
    @Inject
    constructor(timerDao: IntervalTimerDao) : this(timerDao, Timber.tag("TimerListViewModel"))

    init {
        viewModelScope.launch {
            timerDao.getAll()
                .collect {
                    if (it.isEmpty()) {
                        timerState.emit(EmptyListState)
                    } else {
                        timerState.emit(SuccessListState(it))
                    }
                }
        }
    }

    fun addTimer() {
        viewModelScope.launch {
            effects.emit(IntervalTimerListEffects.CreateTimer)
        }
    }

    fun editTimer(timer: IntervalTimer) {
        viewModelScope.launch {
            effects.emit(IntervalTimerListEffects.EditTimer(timer.id!!))
        }
    }

    fun deleteTimer(timer: IntervalTimer) {
        viewModelScope.launch {
            (timerState.value as SuccessListState).apply {
                timerState.emit(this.copy(showConfirmDeleteDialog = true, deleteTimerId = timer.id))
            }
        }
    }

    fun confirmDeleteTimer(delete: Boolean) {
        viewModelScope.launch {
            (timerState.value as SuccessListState).apply {
                val timers = if (delete) {
                    timerDao.delete(timers.first { it.id == deleteTimerId })
                    timers.filter { it.id == deleteTimerId }
                } else {
                    timers
                }
                timerState.emit(copy(
                    timers = timers,
                    showConfirmDeleteDialog = false,
                    deleteTimerId = null
                ))
            }
        }
    }

    fun openTimer(timer: IntervalTimer) {
        viewModelScope.launch {
            effects.emit(IntervalTimerListEffects.OpenTimer(timer.id!!))
        }
    }
}

/**
 * Used to represent each state on the main list view.
 */
sealed interface IntervalTimerListState {
    object LoadingState : IntervalTimerListState
    object EmptyListState : IntervalTimerListState
    data class SuccessListState(
        val timers: List<IntervalTimer>,
        val showConfirmDeleteDialog: Boolean = false,
        val deleteTimerId: Long? = null
    ) : IntervalTimerListState
    data class ErrorState(val message: String) : IntervalTimerListState
}

sealed interface IntervalTimerListEffects {
    object CreateTimer : IntervalTimerListEffects
    data class EditTimer(val timerId: Long) : IntervalTimerListEffects
    data class OpenTimer(val timerId: Long) : IntervalTimerListEffects
}
