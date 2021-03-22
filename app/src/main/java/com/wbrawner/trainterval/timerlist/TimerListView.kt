package com.wbrawner.trainterval.timerlist

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import com.wbrawner.trainterval.shared.IntervalTimer
import com.wbrawner.trainterval.shared.toIntervalDuration
import dev.chrisbanes.accompanist.insets.navigationBarsPadding
import dev.chrisbanes.accompanist.insets.statusBarsPadding

@Composable
fun TimerList(
    timerListViewModel: TimerListViewModel,
    navController: NavController,
    onTimerClicked: (IntervalTimer) -> Unit
) {
    val observedState = timerListViewModel.timerState.observeAsState()
    val scaffoldState = rememberScaffoldState()
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    BasicText(
                        text = "Timers",
                        style = MaterialTheme.typography.h6.merge(
                            TextStyle(color = MaterialTheme.colors.onBackground)
                        )
                    )
                },
                backgroundColor = MaterialTheme.colors.background,
                elevation = 0.dp
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.navigationBarsPadding(),
                onClick = { navController.navigate("new") }
            ) {
                Image(imageVector = Icons.Default.Add, "Add")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { padding ->
        when (val state = observedState.value) {
            is IntervalTimerListState.EmptyListState -> BasicText(
                "Add a new timer to get started.",
                modifier = Modifier.padding(padding),
                style = TextStyle(color = MaterialTheme.colors.onSurface)
            )
            is IntervalTimerListState.SuccessListState -> LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .navigationBarsPadding()
            ) {
                items(state.timers.size) { i ->
                    val timer = state.timers[i]
                    TimerListItem(
                        timer = timer,
                        onTimerClicked = onTimerClicked
                    )
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
    }
}


@Composable
fun TimerListItem(timer: IntervalTimer, onTimerClicked: (IntervalTimer) -> Unit) {
    Row(
        modifier = Modifier
            .requiredHeightIn(min = 64.dp)
            .clickable { onTimerClicked(timer) }
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            BasicText(text = timer.name, style = MaterialTheme.typography.body1)
            if (timer.description.isNotBlank()) {
                BasicText(text = timer.description, style = MaterialTheme.typography.body2)
            }
        }
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
    Surface {
        TimerListItem(timer) {}
    }
}
