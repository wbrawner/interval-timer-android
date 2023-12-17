package com.wbrawner.trainterval

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.wbrawner.trainterval.activetimer.ActiveTimerScreen
import com.wbrawner.trainterval.activetimer.ActiveTimerViewModel
import com.wbrawner.trainterval.shared.theme.TraintervalTheme
import com.wbrawner.trainterval.timerform.TimerFormScreen
import com.wbrawner.trainterval.timerform.TimerFormViewModel
import com.wbrawner.trainterval.timerlist.TimerList
import com.wbrawner.trainterval.timerlist.TimerListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val timerListViewModel: TimerListViewModel by viewModels()
    private val timerFormViewModel: TimerFormViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val navController = rememberNavController()
            TraintervalTheme {
                Surface(color = MaterialTheme.colors.background) {
                    NavHost(
                        navController,
                        startDestination = "timers",
                        enterTransition = {
                            fadeIn() + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up)
                        },
                        exitTransition = {
                            fadeOut() + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down)
                        },
                        popEnterTransition = {
                            fadeIn()
                        },
                        popExitTransition = {
                            fadeOut() + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down)
                        }
                    ) {
                        composable("new") {
                            TimerFormScreen(timerFormViewModel, navController)
                        }
                        composable("timers") {
                            TimerList(timerListViewModel, navController) { timer ->
                                navController.navigate("timers/${timer.id}")
                            }
                        }
                        composable(
                            route = "timers/{timerId}",
                            enterTransition = {
                                fadeIn() + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start)
                            },
                            exitTransition = {
                                fadeOut() + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End)
                            },
                            popEnterTransition = {
                                fadeIn()
                            },
                            popExitTransition = {
                                fadeOut() + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End)
                            },
                            arguments = listOf(navArgument("timerId") {
                                type = NavType.LongType
                            })
                        ) { backStackEntry ->
                            ActiveTimerScreen(
                                hiltViewModel(),
                                navController::navigateUp,
                                backStackEntry.arguments?.getLong("timerId") ?: 0L
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
