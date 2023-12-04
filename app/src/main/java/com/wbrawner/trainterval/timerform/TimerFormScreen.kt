package com.wbrawner.trainterval.timerform

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wbrawner.trainterval.shared.IntervalDuration
import com.wbrawner.trainterval.shared.IntervalTimer
import com.wbrawner.trainterval.shared.toIntervalDuration
import com.wbrawner.trainterval.shared.toSeconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerFormScreen(
    viewModel: TimerFormViewModel,
    navController: NavController,
    id: Long? = null
) {
    LaunchedEffect(id) {
        viewModel.loadTimer(id)
    }
    val state by viewModel.timerState.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    Text(if (id == null) "New Timer" else "Edit Timer")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Cancel")
                    }
                }
            )
        }
    ) { padding ->
        when (state) {
            is IntervalTimerEditState.EditTimerState -> TimerForm(
                modifier = Modifier.padding(padding),
                (state as IntervalTimerEditState.EditTimerState).timer,
                viewModel::saveTimer
            )

            is IntervalTimerEditState.ErrorState -> navController.navigate("new")
            is IntervalTimerEditState.EditTimerSavedState -> navController.navigateUp()
            else -> Box(
                modifier = Modifier.padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerForm(
    modifier: Modifier,
    timer: IntervalTimer,
    saveTimer: (
        name: String,
        description: String,
        warmUpDuration: Long,
        lowIntensityDuration: Long,
        highIntensityDuration: Long,
        restDuration: Long,
        coolDownDuration: Long,
        sets: Int,
        cycles: Int
    ) -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(8.dp)
            .imePadding(),
        verticalArrangement = spacedBy(8.dp)
    ) {
        val (title: String, setTitle: (String) -> Unit) = remember { mutableStateOf(timer.name) }
        val (description, setDescription) = remember { mutableStateOf(timer.description) }
        val (warmUp, setWarmUp) = remember { mutableStateOf(timer.warmUpDuration) }
        val (lowIntensity, setLowIntensity) = remember { mutableStateOf(timer.lowIntensityDuration) }
        val (highIntensity, setHighIntensity) = remember { mutableStateOf(timer.highIntensityDuration) }
        val (coolDown, setCoolDown) = remember { mutableStateOf(timer.coolDownDuration) }
        val (rest, setRest) = remember { mutableStateOf(timer.restDuration) }
        val (sets, setSets) = remember { mutableStateOf(timer.sets) }
        val (cycles, setCycles) = remember { mutableStateOf(timer.cycles) }
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = title,
            onValueChange = setTitle,
            label = { Text("Title") },
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = description,
            onValueChange = setDescription,
            label = { Text("Description") },
        )
        DurationInput(
            modifier = Modifier.fillMaxWidth(),
            value = warmUp,
            onValueChange = setWarmUp,
            label = "Warm-Up",
        )
        DurationInput(
            modifier = Modifier.fillMaxWidth(),
            value = lowIntensity,
            onValueChange = setLowIntensity,
            label = "Low Intensity",
        )
        DurationInput(
            modifier = Modifier.fillMaxWidth(),
            value = highIntensity,
            onValueChange = setHighIntensity,
            label = "High Intensity",
        )
        DurationInput(
            modifier = Modifier.fillMaxWidth(),
            value = rest,
            onValueChange = setRest,
            label = "Rest",
        )
        DurationInput(
            modifier = Modifier.fillMaxWidth(),
            value = coolDown,
            onValueChange = setCoolDown,
            label = "Cool Down",
        )
        NumberInput(
            modifier = Modifier.fillMaxWidth(),
            value = sets,
            onValueChange = setSets,
            label = "Sets",
        )
        NumberInput(
            modifier = Modifier.fillMaxWidth(),
            value = cycles,
            onValueChange = setCycles,
            label = "Cycles",
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                saveTimer(
                    title,
                    description,
                    warmUp,
                    lowIntensity,
                    highIntensity,
                    rest,
                    coolDown,
                    sets,
                    cycles
                )
            }
        ) {
            Text("Save")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DurationInput(
    modifier: Modifier,
    label: String,
    value: Long,
    onValueChange: (Long) -> Unit
) {
    val (input, setInput) = remember { mutableStateOf(value.toIntervalDuration().toString()) }
    val (isError, setError) = remember { mutableStateOf(false) }
    // TODO: Use a triple spinner instead of the text field
    OutlinedTextField(
        modifier = modifier,
        value = input,
        onValueChange = {
            setInput(it)
            IntervalDuration.parse(it)
                ?.let { duration ->
                    setError(false)
                    onValueChange(duration.toSeconds())
                }
                ?: setError(true)
        },
        label = { Text(label) },
        isError = isError,
        supportingText = {
            if (isError) {
                Text(
                    text = "Invalid duration format. Try HH:mm:ss or mm:ss instead.",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberInput(
    modifier: Modifier,
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    val (input, setInput) = remember { mutableStateOf(value.toString()) }
    val (isError, setError) = remember { mutableStateOf(false) }
    // TODO: Use a spinner instead of the text field
    OutlinedTextField(
        modifier = modifier,
        value = input,
        onValueChange = {
            setInput(it)
            it.toIntOrNull()
                ?.let { number ->
                    setError(false)
                    onValueChange(number)
                }
                ?: setError(true)
        },
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
        label = { Text(label) },
        isError = isError,
        supportingText = {
            if (isError) {
                Text(
                    text = "Invalid input. Please enter a number",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    )
}

@Preview
@Composable
fun TimerForm_Preview() {
    Surface {
        val timer = IntervalTimer()
        TimerForm(modifier = Modifier, timer) { _, _, _, _, _, _, _, _, _ -> }
    }
}
