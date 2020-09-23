package com.wbrawner.trainterval.wear

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.wear.ambient.AmbientModeSupport
import com.google.android.gms.wearable.*
import com.wbrawner.trainterval.R
import com.wbrawner.trainterval.shared.IntervalTimerState
import com.wbrawner.trainterval.shared.IntervalTimerState.Companion.TIMER_ACTIONS_TOGGLE
import com.wbrawner.trainterval.shared.IntervalTimerState.Companion.TIMER_STATE
import com.wbrawner.trainterval.shared.toIntervalTimerState
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : FragmentActivity(),
    AmbientModeSupport.AmbientCallbackProvider,
    DataClient.OnDataChangedListener {

    private lateinit var dataClient: DataClient
    private lateinit var messageClient: MessageClient
    private lateinit var nodeClient: NodeClient
    private lateinit var vibrator: Vibrator
    private lateinit var ambientController: AmbientModeSupport.AmbientController
    private var lastState: IntervalTimerState? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dataClient = Wearable.getDataClient(this)
        messageClient = Wearable.getMessageClient(this)
        nodeClient = Wearable.getNodeClient(this)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        ambientController = AmbientModeSupport.attach(this)
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
            lastState = intervalTimerState
            renderState()
        }
    }

    private fun renderState() {
        val intervalTimerState = lastState ?: return
        when (intervalTimerState) {
            is IntervalTimerState.LoadingState -> timeRemaining.text = "Loading"
            is IntervalTimerState.TimerRunningState -> {
                val backgroundColor = if (ambientController.isAmbient) Color.BLACK
                else resources.getColor(intervalTimerState.phase.colorRes, theme)
                timerRoot.setBackgroundColor(backgroundColor)
                timeRemaining.text = intervalTimerState.timeRemaining
                val textColor = if (ambientController.isAmbient) resources.getColor(
                    intervalTimerState.phase.colorRes,
                    theme
                )
                else Color.BLACK
                timeRemaining.setTextColor(textColor)
                if (ambientController.isAmbient) {
                    toggleButton.visibility = View.GONE
                } else {
                    toggleButton.visibility = View.VISIBLE
                    toggleButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            if (intervalTimerState.isRunning) R.drawable.ic_pause
                            else R.drawable.ic_play_arrow
                        )
                    )
                }
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

    override fun getAmbientCallback(): AmbientModeSupport.AmbientCallback =
        object : AmbientModeSupport.AmbientCallback() {
            override fun onEnterAmbient(ambientDetails: Bundle?) {
                super.onEnterAmbient(ambientDetails)
                renderState()
            }

            override fun onExitAmbient() {
                super.onExitAmbient()
                renderState()
            }
        }
}