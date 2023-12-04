package com.wbrawner.trainterval.timerlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wbrawner.trainterval.shared.IntervalTimer
import com.wbrawner.trainterval.shared.toIntervalDuration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerList(
    timerListViewModel: TimerListViewModel,
    navController: NavController,
    onTimerClicked: (IntervalTimer) -> Unit
) {
    val state by timerListViewModel.timerState.collectAsState()
    LaunchedEffect(timerListViewModel) {
        timerListViewModel.effects.collect {
            when (it) {
                is IntervalTimerListEffects.OpenTimer -> navController.navigate("timers/${it.timerId}")
                is IntervalTimerListEffects.EditTimer -> navController.navigate("edit/${it.timerId}")
                is IntervalTimerListEffects.CreateTimer -> navController.navigate("new")
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    Text("Timers")
                }
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
        when (state) {
            is IntervalTimerListState.EmptyListState -> Text(
                "Add a new timer to get started.",
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp),
            )

            is IntervalTimerListState.SuccessListState -> Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                val state = state as IntervalTimerListState.SuccessListState
                LazyColumn() {
                    items(state.timers.size) { i ->
                        val timer = state.timers[i]
                        TimerListItem(
                            timer = timer,
                            onTimerClicked = onTimerClicked,
                            editTimer = timerListViewModel::editTimer,
                            deleteTimer = timerListViewModel::deleteTimer
                        )
                    }
                }
                if (state.showConfirmDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { timerListViewModel.confirmDeleteTimer(false) },
                        title = {
                            Text("Are you sure you want to delete this timer?")
                        },
                        text = {
                            Text("This cannot be undone.")
                        },
                        dismissButton = {
                            TextButton({ timerListViewModel.confirmDeleteTimer(false) }) {
                                Text("Cancel")
                            }
                        },
                        confirmButton = {
                            TextButton({ timerListViewModel.confirmDeleteTimer(true) }) {
                                Text("Delete")
                            }
                        }
                    )
                }
            }

            is IntervalTimerListState.ErrorState -> Text(
                (state as IntervalTimerListState.ErrorState).message,
                modifier = Modifier.padding(padding)
            )

            else -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimerListItem(
    timer: IntervalTimer,
    onTimerClicked: (IntervalTimer) -> Unit,
    editTimer: (IntervalTimer) -> Unit,
    deleteTimer: (IntervalTimer) -> Unit,
) {
    val (isMenuShown, showMenu) = remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .requiredHeightIn(min = 64.dp)
            .combinedClickable(
                onClick = { onTimerClicked(timer) },
                onLongClick = { showMenu(true) }
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = timer.name, style = MaterialTheme.typography.bodyLarge)
            if (timer.description.isNotBlank()) {
                Text(text = timer.description, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Text(
            text = timer.totalDuration.toIntervalDuration().toString(), style =
            MaterialTheme.typography.bodySmall
        )
        DropdownMenu(expanded = isMenuShown, onDismissRequest = { showMenu(false) }) {
            DropdownMenuItem(text = { Text("Edit") }, onClick = { editTimer(timer) })
            DropdownMenuItem(text = { Text("Delete") }, onClick = { deleteTimer(timer) })
        }
    }
}

@Preview
@Composable
fun TimerListItem_Preview() {
    val timer = IntervalTimer(name = "Tabata", description = "A short, high-intensity workout")
    Surface {
        TimerListItem(timer, {}, {}, {})
    }
}
