package com.wbrawner.trainterval.activetimer

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.media.AudioManager
import android.media.SoundPool
import android.view.WindowManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.smallTopAppBarColors
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.wbrawner.trainterval.shared.IntervalTimerEffects
import com.wbrawner.trainterval.shared.IntervalTimerState
import com.wbrawner.trainterval.shared.Phase
import com.wbrawner.trainterval.shared.theme.TraintervalColors
import com.wbrawner.trainterval.shared.theme.TraintervalTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ActiveTimerScreen(
    viewModel: ActiveTimerViewModel,
    onUpNavigation: () -> Unit,
    timerId: Long
) {
    val context = LocalContext.current
    LaunchedEffect(timerId) {
        viewModel.loadTimer(timerId)
        val soundIds = mutableMapOf<Phase, Int>()
        val soundPool = SoundPool.Builder()
            .setMaxStreams(Phase.values().size)
            .build()
        val audioManager = ContextCompat.getSystemService(context, AudioManager::class.java)
            ?: return@LaunchedEffect
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
        val volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / maxVolume

        withContext(Dispatchers.IO) {
            Phase.values().forEach { phase ->
                soundIds[phase] = soundPool.load(
                    context.assets.openFd("audio/${phase.soundFile}"), 1
                )
            }
        }

        viewModel.effects.collect { effect ->
            when (effect) {
                is IntervalTimerEffects.PlaySound -> soundIds[effect.phase]?.let { soundId ->
                    soundPool.play(soundId, volume, volume, 1, 0, 1f)
                }

                IntervalTimerEffects.Close -> onUpNavigation()
            }
        }
    }
    val state by viewModel.timerState.collectAsState()
    ActiveTimerScreen(
        state,
        onUpNavigation,
        viewModel::goBack,
        viewModel::toggleTimer,
        viewModel::skipAhead
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveTimerScreen(
    state: IntervalTimerState,
    onUpNavigation: () -> Unit,
    onSkipBack: () -> Unit,
    onToggleTimer: () -> Unit,
    onSkipForward: () -> Unit
) {
    val timerRunning = remember(state) { state is IntervalTimerState.TimerRunningState }
    val activity = LocalContext.current.activity
    LaunchedEffect(timerRunning) {
        if (timerRunning) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
    val color = if (state is IntervalTimerState.TimerRunningState) {
        state.phase.color
    } else {
        TraintervalColors.warmUp
    }
    val containerColor by if (isSystemInDarkTheme()) {
        animateColorAsState(MaterialTheme.colorScheme.background, label = "containerColor")
    } else {
        animateColorAsState(color, label = "containerColor")
    }
    val contentColor by if (isSystemInDarkTheme()) {
        animateColorAsState(color, label = "containerColor")
    } else {
        animateColorAsState(MaterialTheme.colorScheme.onBackground, label = "containerColor")
    }
    Scaffold(
        containerColor = containerColor,
        contentColor = contentColor,
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    val text = if (state is IntervalTimerState.TimerRunningState) {
                        state.timerName
                    } else {
                        ""
                    }
                    Text(text = text, color = contentColor)
                },
                colors = topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(
                        onClick = onUpNavigation
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Exit timer",
                            tint = contentColor
                        )
                    }
                }
            )
        },
    ) { padding ->
        when (state) {
            is IntervalTimerState.TimerRunningState ->
                RunningTimer(
                    modifier = Modifier.padding(padding),
                    state.timeRemaining,
                    state.currentRound,
                    state.currentSet,
                    state.isRunning,
                    state.phase,
                    onSkipBack = onSkipBack,
                    onToggleTimer = onToggleTimer,
                    onSkipForward = onSkipForward
                )

            is IntervalTimerState.LoadingState -> Box(
                modifier = Modifier.padding(padding), contentAlignment =
                Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun RunningTimer(
    modifier: Modifier = Modifier,
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
        modifier = modifier
            .fillMaxSize()
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
            Text(
                text = LocalContext.current.getString(phase.stringRes),
                style = MaterialTheme.typography.headlineSmall,
//                color = textColor
            )
            AutoSizeText(
                text = timeRemaining,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 200.sp
                ),
            )
            Row(horizontalArrangement = spacedBy(16.dp)) {
                val buttonModifier = Modifier.size(64.dp)
                val iconModifier = Modifier.fillMaxSize()
                val rippleSize = 32.dp
                Box(
                    modifier = buttonModifier
                        .clickable(
                            onClick = onSkipBack,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = rememberRipple(bounded = false, radius = rippleSize)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        modifier = iconModifier,
                        imageVector = Icons.Default.FastRewind,
                        contentDescription = "Go back",
                    )
                }
                Box(
                    modifier = buttonModifier
                        .clickable(
                            onClick = onToggleTimer,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = rememberRipple(bounded = false, radius = rippleSize)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        modifier = iconModifier,
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Play",
                    )
                }
                Box(
                    modifier = buttonModifier
                        .clickable(
                            onClick = onSkipForward,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = rememberRipple(bounded = false, radius = rippleSize)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        modifier = iconModifier,
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Go forward",
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LabeledCounter(label = "Set", value = set)
            LabeledCounter(label = "Round", value = round)
        }
    }
}

@Composable
fun LabeledCounter(
    label: String,
    value: Int,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.displaySmall.copy(fontFamily = FontFamily.Monospace),
        )
    }
}

@Composable
fun AutoSizeText(modifier: Modifier = Modifier, text: String, style: TextStyle) {
    var textStyle by remember { mutableStateOf(style) }
    var readyToDraw by remember { mutableStateOf(false) }
    Text(
        text = text,
        style = textStyle,
        maxLines = 1,
        softWrap = false,
        modifier = modifier.drawWithContent {
            if (readyToDraw) drawContent()
        },
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.didOverflowWidth) {
                textStyle = textStyle.copy(fontSize = textStyle.fontSize * 0.9)
            } else {
                readyToDraw = true
            }
        }
    )
}

val Context.activity: Activity?
    get() = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.activity
        else -> null
    }

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun RunningTimerWarm_Preview() {
    TraintervalTheme {
        RunningTimer(
            timeRemaining = "99:99:99",
            round = 3,
            set = 4,
            isRunning = true,
            phase = Phase.WARM_UP,
            onSkipBack = { },
            onToggleTimer = { },
            onSkipForward = { }
        )
    }
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun RunningTimerLow_Preview() {
    TraintervalTheme {
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
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun RunningTimerHigh_Preview() {
    TraintervalTheme {
        RunningTimer(
            timeRemaining = "99:99:99",
            round = 3,
            set = 4,
            isRunning = true,
            phase = Phase.HIGH_INTENSITY,
            onSkipBack = { },
            onToggleTimer = { },
            onSkipForward = { }
        )
    }
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun RunningTimerRest_Preview() {
    TraintervalTheme {
        RunningTimer(
            timeRemaining = "99:99:99",
            round = 3,
            set = 4,
            isRunning = true,
            phase = Phase.REST,
            onSkipBack = { },
            onToggleTimer = { },
            onSkipForward = { }
        )
    }
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun RunningTimerCool_Preview() {
    TraintervalTheme {
        RunningTimer(
            timeRemaining = "99:99:99",
            round = 3,
            set = 4,
            isRunning = true,
            phase = Phase.COOL_DOWN,
            onSkipBack = { },
            onToggleTimer = { },
            onSkipForward = { }
        )
    }
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun LabeledCounter_Preview() {
    TraintervalTheme {
        LabeledCounter("Label", 99)
    }
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun LabeledCounter_Preview2() {
    TraintervalTheme {
        LabeledCounter("Label", 9)
    }
}