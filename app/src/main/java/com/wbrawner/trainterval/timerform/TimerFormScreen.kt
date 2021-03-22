package com.wbrawner.trainterval.timerform

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import com.wbrawner.trainterval.shared.IntervalTimer
import dev.chrisbanes.accompanist.insets.navigationBarsPadding
import dev.chrisbanes.accompanist.insets.statusBarsPadding

@Composable
fun TimerFormScreen(
    viewModel: TimerFormViewModel,
    navController: NavController,
    id: Long? = null
) {
    LaunchedEffect(id) {
        viewModel.loadTimer(id)
    }
    val observedState = viewModel.timerState.observeAsState()
    val scaffoldState = rememberScaffoldState()
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    BasicText(
                        text = if (id == null) "New Timer" else "Edit Timer",
                        style = MaterialTheme.typography.h6.merge(
                            TextStyle(color = MaterialTheme.colors.onBackground)
                        )
                    )
                },
                backgroundColor = MaterialTheme.colors.background,
                elevation = 0.dp
            )
        }
    ) {
        when (val state = observedState.value) {
            is IntervalTimerEditState.EditTimerState -> TimerForm(state.timer, viewModel::saveTimer)
            is IntervalTimerEditState.ErrorState -> navController.navigate("new")
            is IntervalTimerEditState.EditTimerSavedState -> navController.navigateUp()
            else -> CircularProgressIndicator()
        }
    }
}

@Composable
fun TimerForm(
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
        modifier = Modifier
            .verticalScroll(scrollState)
            .padding(8.dp)
            .navigationBarsPadding()
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
        val commonModifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
        OutlinedTextField(
            value = title,
            onValueChange = setTitle,
            label = { BasicText("Title") },
            modifier = commonModifier
        )
        OutlinedTextField(
            value = description,
            onValueChange = setDescription,
            label = { BasicText("Description") },
            modifier = commonModifier
        )
        OutlinedTextField(
            value = warmUp.toString(),
            onValueChange = { setWarmUp(it.toLong()) },
            label = { BasicText("Warm-Up") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = commonModifier
        )
        OutlinedTextField(
            value = lowIntensity.toString(),
            onValueChange = { setLowIntensity(it.toLong()) },
            label = { BasicText("Low Intensity") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = commonModifier
        )
        OutlinedTextField(
            value = highIntensity.toString(),
            onValueChange = { setHighIntensity(it.toLong()) },
            label = { BasicText("High Intensity") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = commonModifier
        )
        OutlinedTextField(
            value = rest.toString(),
            onValueChange = { setRest(it.toLong()) },
            label = { BasicText("Rest") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = commonModifier
        )
        OutlinedTextField(
            value = coolDown.toString(),
            onValueChange = { setCoolDown(it.toLong()) },
            label = { BasicText("Cool Down") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = commonModifier
        )
        OutlinedTextField(
            value = sets.toString(),
            onValueChange = { setSets(it.toInt()) },
            label = { BasicText("Sets") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = commonModifier
        )
        OutlinedTextField(
            value = cycles.toString(),
            onValueChange = { setCycles(it.toInt()) },
            label = { BasicText("Cycles") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = commonModifier
        )
        Button(onClick = {
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
        }, modifier = commonModifier) {
            BasicText(
                "Save",
                style = TextStyle.Default.copy(color = MaterialTheme.colors.onPrimary)
            )
        }
    }
}

@Preview
@Composable
fun TimerForm_Preview() {
    Surface {
        val timer = IntervalTimer()
        TimerForm(timer) { _, _, _, _, _, _, _, _, _ -> }
    }
}
