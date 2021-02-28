package com.wbrawner.trainterval.timerform

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import com.wbrawner.trainterval.shared.IntervalTimer

@Composable
fun TimerFormScreen(
    viewModel: TimerFormViewModel,
    navController: NavController,
    id: Int? = null
) {
    val observedState = viewModel.timerState.observeAsState()
    val scaffoldState = rememberScaffoldState()
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = {
                    BasicText(
                        text = if (id == null) "New Timer" else "Edit Timer",
                        style = MaterialTheme.typography.h6.merge(
                            TextStyle(color = MaterialTheme.colors.onBackground)
                        )
                    )
                },
                backgroundColor = MaterialTheme.colors.background
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("new") }) {
                Image(imageVector = Icons.Default.Add, "Add")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) {
        when (val state = observedState.value) {
            is IntervalTimerEditState.EditTimerState -> TimerForm(state.timer)
            is IntervalTimerEditState.ErrorState -> navController.navigate("new")
            else -> CircularProgressIndicator()
        }
    }
}

@Composable
fun TimerForm(timer: IntervalTimer) {
    Column {
        val title = TextFieldValue()
        val description = remember { mutableStateOf("" )}
        TextField(value = title, onValueChange = {})
    }
}

@Preview
@Composable
fun TimerForm_Preview() {
    val timer = IntervalTimer()
    TimerForm(timer)
}