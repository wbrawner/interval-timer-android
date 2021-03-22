package com.wbrawner.trainterval.activetimer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wbrawner.trainterval.shared.IntervalTimerState
import com.wbrawner.trainterval.shared.Phase
import com.wbrawner.trainterval.shared.theme.TraintervalColors
import dev.chrisbanes.accompanist.insets.navigationBarsPadding
import dev.chrisbanes.accompanist.insets.statusBarsPadding

@Composable
fun ActiveTimerScreen(
    state: IntervalTimerState?,
    onUpNavigation: () -> Unit,
    onSkipBack: () -> Unit,
    onToggleTimer: () -> Unit,
    onSkipForward: () -> Unit
) {
    val scaffoldState = rememberScaffoldState()
    val backgroundColor = if (state is IntervalTimerState.TimerRunningState) {
        state.phase.color
    } else {
        TraintervalColors.warmUp
    }
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    val text = if (state is IntervalTimerState.TimerRunningState) {
                        state.timerName
                    } else {
                        ""
                    }
                    BasicText(
                        text = text,
                        style = MaterialTheme.typography.h6.merge(
                            TextStyle(color = MaterialTheme.colors.onBackground)
                        )
                    )
                },
                backgroundColor = backgroundColor,
                elevation = 0.dp,
                navigationIcon = {
                    IconButton(
                        onClick = onUpNavigation
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Exit timer")
                    }
                }
            )
        },
        backgroundColor = backgroundColor
    ) {
        when (state) {
            is IntervalTimerState.TimerRunningState ->
                RunningTimer(
                    state.timeRemaining,
                    state.currentRound,
                    state.currentSet,
                    state.isRunning,
                    state.phase,
                    onSkipBack = onSkipBack,
                    onToggleTimer = onToggleTimer,
                    onSkipForward = onSkipForward
                )
            is IntervalTimerState.ExitState -> onUpNavigation
            else -> CircularProgressIndicator()
        }
    }
}

@Composable
fun RunningTimer(
    timeRemaining: String,
    round: Int,
    set: Int,
    isRunning: Boolean,
    phase: Phase,
    onSkipBack: () -> Unit,
    onToggleTimer: () -> Unit,
    onSkipForward: () -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BasicText(
                text = LocalContext.current.getString(phase.stringRes),
                style = MaterialTheme.typography.h5
            )
            BasicText(
                text = timeRemaining,
                style = MaterialTheme.typography.h1.copy(fontFamily = FontFamily.Monospace),
            )
            Row {
                IconButton(onClick = onSkipBack) {
                    Icon(imageVector = Icons.Default.FastRewind, contentDescription = "Go back")
                }
                IconButton(onClick = onToggleTimer) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Play"
                    )
                }
                IconButton(onClick = onSkipForward) {
                    Icon(imageVector = Icons.Default.FastForward, contentDescription = "Go forward")
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LabeledCounter(label = "Set", value = set)
            LabeledCounter(label = "Round", value = round)
        }
    }
}

@Composable
fun LabeledCounter(label: String, value: Int) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BasicText(
            text = label,
            style = MaterialTheme.typography.caption
        )
        BasicText(
            text = value.toString(),
            style = MaterialTheme.typography.h4.copy(fontFamily = FontFamily.Monospace)
        )
    }
}

@Preview
@Composable
fun RunningTimer_Preview() {
    Surface {
        RunningTimer(
            timeRemaining = "99:99:99",
            round = 3,
            set = 4,
            isRunning = true,
            phase = Phase.LOW_INTENSITY,
            onSkipBack = { },
            onToggleTimer = { },
            onSkipForward = { }
        )
    }
}

@Preview
@Composable
fun LabeledCounter_Preview() {
    Surface {
        LabeledCounter("Label", 99)
    }
}

@Preview
@Composable
fun LabeledCounter_Preview2() {
    Surface {
        LabeledCounter("Label", 9)
    }
}