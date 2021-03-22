package com.wbrawner.trainterval

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.core.view.WindowCompat
import androidx.navigation.NavType
import androidx.navigation.compose.*
import com.wbrawner.trainterval.activetimer.ActiveTimerScreen
import com.wbrawner.trainterval.activetimer.ActiveTimerViewModel
import com.wbrawner.trainterval.shared.theme.TraintervalTheme
import com.wbrawner.trainterval.timerform.TimerFormScreen
import com.wbrawner.trainterval.timerform.TimerFormViewModel
import com.wbrawner.trainterval.timerlist.TimerList
import com.wbrawner.trainterval.timerlist.TimerListViewModel
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.accompanist.insets.ProvideWindowInsets

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val timerListViewModel: TimerListViewModel by viewModels()
    private val timerFormViewModel: TimerFormViewModel by viewModels()
    private val activeTimerViewModel: ActiveTimerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val navController = rememberNavController()
            TraintervalTheme {
                ProvideWindowInsets {
                    Surface(color = MaterialTheme.colors.background) {
                        NavHost(navController, startDestination = "timers") {
                            composable("new") {
                                TimerFormScreen(timerFormViewModel, navController)
                            }
                            composable("timers") {
                                TimerList(timerListViewModel, navController) { timer ->
                                    navController.navigate("timers/${timer.id}")
                                }
                            }
                            composable(
                                "timers/{timerId}",
                                arguments = listOf(navArgument("timerId") {
                                    type = NavType.LongType
                                })
                            ) { backStackEntry ->
                                val timerId = backStackEntry.arguments?.getLong("timerId")
                                LaunchedEffect(timerId) {
                                    activeTimerViewModel.loadTimer(timerId!!)
                                }
                                val observedState = activeTimerViewModel.timerState.observeAsState()
                                ActiveTimerScreen(
                                    observedState.value,
                                    navController::navigateUp,
                                    activeTimerViewModel::goBack,
                                    activeTimerViewModel::toggleTimer,
                                    activeTimerViewModel::skipAhead
                                )
                            }
                            composable(
                                "edit/{timerId}",
                                arguments = listOf(navArgument("timerId") {
                                    type = NavType.LongType
                                })
                            ) { backStackEntry ->
                                TimerFormScreen(
                                    timerFormViewModel,
                                    navController,
                                    backStackEntry.arguments?.getLong("timerId")
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
