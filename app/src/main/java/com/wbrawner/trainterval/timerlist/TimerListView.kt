package com.wbrawner.trainterval.timerlist

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.imageFromResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import com.wbrawner.trainterval.R
import com.wbrawner.trainterval.shared.IntervalTimer
import com.wbrawner.trainterval.shared.toIntervalDuration

@Composable
fun TimerList(timerListViewModel: TimerListViewModel, navController: NavController) {
    val observedState = timerListViewModel.timerState.observeAsState()
    val scaffoldState = rememberScaffoldState()
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = {
                    BasicText(
                        text = "Timers",
                        style = MaterialTheme.typography.h6.merge(
                            TextStyle(color = MaterialTheme.colors.onBackground)
                        )
                    )
                },
                backgroundColor = MaterialTheme.colors.background
            )
        },
        bodyContent = { padding ->
            when (val state = observedState.value) {
                is IntervalTimerListState.EmptyListState -> BasicText(
                    "Add a new timer to get started.",
                    modifier = Modifier.padding(padding),
                    style = TextStyle(color = MaterialTheme.colors.onSurface)
                )
                is IntervalTimerListState.SuccessListState -> LazyColumn(Modifier.padding(padding)) {
                    items(state.timers) { timer ->
                        TimerListItem(timer)

                    }
                }
                is IntervalTimerListState.ErrorState -> BasicText(
                    state.message,
                    modifier = Modifier.padding(padding),
                    style = TextStyle(color = MaterialTheme.colors.onSurface)
                )
                is IntervalTimerListState.CreateTimer -> navController.navigate("new")
                is IntervalTimerListState.EditTimer -> navController.navigate("edit/${state.timerId}")
                is IntervalTimerListState.OpenTimer -> navController.navigate("timer/${state.timerId}")
                else -> CircularProgressIndicator()
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("new") }) {
                Image(imageVector = Icons.Default.Add)
            }
        },
        floatingActionButtonPosition = FabPosition.End
    )
}


@Composable
fun TimerListItem(timer: IntervalTimer) {
    Row(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            BasicText(text = timer.name, style = MaterialTheme.typography.body1)
            BasicText(text = timer.description, style = MaterialTheme.typography.body2)
        }
        Spacer(Modifier.fillMaxWidth())
        BasicText(
            text = timer.totalDuration.toIntervalDuration().toString(), style =
            MaterialTheme.typography.body2
        )
    }
}

@Preview
@Composable
fun TimerListItem_Preview() {
    val timer = IntervalTimer(name = "Tabata", description = "A short, high-intensity workout")
    TimerListItem(timer)
}
