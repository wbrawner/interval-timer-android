package com.wbrawner.trainterval

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.platform.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wbrawner.trainterval.shared.theme.TraintervalTheme
import com.wbrawner.trainterval.timerlist.TimerList
import com.wbrawner.trainterval.timerlist.TimerListViewModel
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {
    private val timerListViewModel: TimerListViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            TraintervalTheme {
                Surface(color = MaterialTheme.colors.background) {
                    NavHost(navController, startDestination = "timers") {
                        composable("new") { BasicText("new timer") }
                        composable("timers") {
                            TimerList(timerListViewModel, navController)
                        }
                        composable("timer/{timerId") { backStackEntry ->
                            BasicText(
                                "viewing timer with ID: ${backStackEntry.arguments?.getString("userId")}"
                            )
                        }
                        composable("edit/{timerId") { backStackEntry ->
                            BasicText(
                                "editing timer with ID: ${backStackEntry.arguments?.getString("userId")}"
                            )
                        }
                    }
                }
            }
        }
    }
}
