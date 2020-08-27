package com.wbrawner.trainterval.wear

import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.wearable.activity.WearableActivity
import android.util.Log
import android.view.View
import com.google.android.gms.wearable.*
import com.wbrawner.trainterval.R
import com.wbrawner.trainterval.shared.IntervalTimerState
import com.wbrawner.trainterval.shared.IntervalTimerState.Companion.TIMER_ACTIONS_TOGGLE
import com.wbrawner.trainterval.shared.IntervalTimerState.Companion.TIMER_STATE
import com.wbrawner.trainterval.shared.toIntervalTimerState
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : WearableActivity(), DataClient.OnDataChangedListener {

    private lateinit var dataClient: DataClient
    private lateinit var messageClient: MessageClient
    private lateinit var nodeClient: NodeClient
    private lateinit var vibrator: Vibrator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dataClient = Wearable.getDataClient(this)
        messageClient = Wearable.getMessageClient(this)
        nodeClient = Wearable.getNodeClient(this)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        setAmbientEnabled()
    }

    override fun onResume() {
        super.onResume()
        dataClient.addListener(this)
    }

    override fun onPause() {
        super.onPause()
        dataClient.removeListener(this)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach {
            if (it.dataItem.uri.path?.compareTo(TIMER_STATE) != 0) return@forEach
            val intervalTimerState = DataMapItem.fromDataItem(it.dataItem)
                .dataMap
                .toIntervalTimerState()
                ?: return@forEach
            when (intervalTimerState) {
                is IntervalTimerState.LoadingState -> timeRemaining.text = "Loading"
                is IntervalTimerState.TimerRunningState -> {
                    val backgroundColor =
                        resources.getColor(intervalTimerState.phase.colorRes, theme)
                    timerRoot.setBackgroundColor(backgroundColor)
                    timeRemaining.text = intervalTimerState.timeRemaining
                    toggleButton.setImageDrawable(
                        getDrawable(
                            if (intervalTimerState.isRunning) R.drawable.ic_pause_inset
                            else R.drawable.ic_play_inset
                        )
                    )
                    if (intervalTimerState.vibrate) {
                        vibrator.vibrate(
                            VibrationEffect.createWaveform(
                                longArrayOf(0L, 100L, 50L, 100L),
                                -1
                            )
                        )
                    }
                }
                is IntervalTimerState.ExitState -> timeRemaining.text = "Exit"
            }
        }
    }

    fun toggleTimer(@Suppress("UNUSED_PARAMETER") view: View) {
        nodeClient.connectedNodes.addOnSuccessListener { nodes ->
            nodes.map { it.id }
                .forEach { nodeId ->
                    messageClient.sendMessage(nodeId, TIMER_ACTIONS_TOGGLE, null)
                        .addOnSuccessListener {
                            Log.d("Wearable", "Sent message to $nodeId")
                        }
                        .addOnFailureListener {
                            Log.d("Wearable", "Failed to send message to $nodeId")
                        }
                }
        }
    }

    override fun onEnterAmbient(ambientDetails: Bundle?) {
        super.onEnterAmbient(ambientDetails)
    }

    override fun onExitAmbient() {
        super.onExitAmbient()

    }
}