package com.wbrawner.trainterval.activetimer

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.wearable.*
import com.robinhood.ticker.TickerUtils
import com.wbrawner.trainterval.Logger
import com.wbrawner.trainterval.R
import com.wbrawner.trainterval.shared.IntervalTimerDao
import com.wbrawner.trainterval.shared.IntervalTimerState
import com.wbrawner.trainterval.shared.IntervalTimerState.Companion.TIMER_ACTIONS_TOGGLE
import com.wbrawner.trainterval.shared.IntervalTimerState.Companion.TIMER_STATE
import com.wbrawner.trainterval.shared.Phase
import com.wbrawner.trainterval.shared.toDataMap
import kotlinx.android.synthetic.main.fragment_active_timer.*
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class ActiveTimerFragment : Fragment(), MessageClient.OnMessageReceivedListener {

    private var coroutineScope: CoroutineScope? = null
    private val activeTimerViewModel: ActiveTimerViewModel by activityViewModels()
    private val logger: Logger by inject(parameters = { parametersOf("ActiveTimerStore") })
    private val timerDao: IntervalTimerDao by inject()
    private var timerId: Long = 0
    private lateinit var soundPool: SoundPool
    private val soundIds = mutableListOf<Int>()
    private lateinit var dataClient: DataClient
    private lateinit var messageClient: MessageClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        timerId = requireArguments().getLong(EXTRA_TIMER_ID)
        setHasOptionsMenu(true)
        soundPool = SoundPool.Builder()
            .setMaxStreams(Phase.values().size)
            .build()
        activeTimerViewModel.viewModelScope.launch {
            if (soundIds.isEmpty()) {
                val assetManager = context?.assets ?: return@launch
                withContext(Dispatchers.IO) {
                    Phase.values().forEachIndexed { index, phase ->
                        soundIds.add(
                            index,
                            soundPool.load(assetManager.openFd("audio/${phase.soundFile}"), 1)
                        )
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == android.R.id.home) {
            findNavController().navigateUp()
        } else {
            super.onOptionsItemSelected(item)
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dataClient = Wearable.getDataClient(context)
        messageClient = Wearable.getMessageClient(context)
        messageClient.addListener(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_active_timer, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.let {
            it.setSupportActionBar(toolbar)
            it.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        timeRemaining.setCharacterLists(TickerUtils.provideNumberList())
        timerSets.setCharacterLists(TickerUtils.provideNumberList())
        timerRounds.setCharacterLists(TickerUtils.provideNumberList())
        coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope!!.launch {
            activeTimerViewModel.timerState.observe(viewLifecycleOwner, Observer { state ->
                when (state) {
                    is IntervalTimerState.LoadingState -> renderLoading()
                    is IntervalTimerState.TimerRunningState -> renderTimer(state)
                    is IntervalTimerState.ExitState -> findNavController().navigateUp()
                }
                val req = PutDataMapRequest.create(TIMER_STATE).run {
                    setUrgent()
                    dataMap.putAll(state.toDataMap())
                    asPutDataRequest()
                }
                dataClient.putDataItem(req).addOnSuccessListener { /* No op*/ }
            })
            activeTimerViewModel.init(logger, timerDao, timerId)
        }
        skipPreviousButton.setOnClickListener {
            activeTimerViewModel.goBack()
        }
        playPauseButton.setOnClickListener {
            activeTimerViewModel.toggleTimer()
        }
        skipNextButton.setOnClickListener {
            activeTimerViewModel.skipAhead()
        }
    }

    private fun renderLoading() {
        progressBar.visibility = View.VISIBLE
        timerLayout.referencedIds.forEach {
            view?.findViewById<View>(it)?.visibility = View.GONE
        }
    }

    private fun renderTimer(state: IntervalTimerState.TimerRunningState) {
        progressBar.visibility = View.GONE
        timerLayout.referencedIds.forEach {
            view?.findViewById<View>(it)?.visibility = View.VISIBLE
        }
        (activity as? AppCompatActivity)?.supportActionBar?.title = state.timerName
        val backgroundColor = resources.getColor(state.phase.colorRes, context?.theme)
        state.previousPhase?.let {
            val previousBackgroundColor = resources.getColor(it.colorRes, context?.theme)
            val colorAnimation =
                ValueAnimator.ofObject(ArgbEvaluator(), previousBackgroundColor, backgroundColor)
            colorAnimation.duration = 250
            colorAnimation.addUpdateListener { animator ->
                timerBackground.setBackgroundColor(
                    animator.animatedValue as Int
                )
            }
            colorAnimation.start()
        } ?: timerBackground.setBackgroundColor(backgroundColor)
        playPauseButton.setImageDrawable(
            requireContext().getDrawable(
                if (state.isRunning) R.drawable.ic_pause
                else R.drawable.ic_play_arrow
            )
        )
        timerPhase.text = getString(state.phase.stringRes)
        timeRemaining.text = state.timeRemaining
        timerSets.text = state.currentSet.toString()
        timerRounds.text = state.currentRound.toString()
        state.soundId?.let {
            playSound(soundIds[it])
        }
    }

    private fun playSound(soundId: Int) {
        val context = context ?: return
        val audioManager = getSystemService(context, AudioManager::class.java) ?: return
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
        val volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / maxVolume
        soundPool.play(soundId, volume, volume, 1, 0, 1f)
    }

    override fun onMessageReceived(event: MessageEvent) {
        Log.d("WearMessage", "Received event: ${event.path}")
        if (event.path?.compareTo(TIMER_ACTIONS_TOGGLE) != 0) return
        activeTimerViewModel.toggleTimer()
    }

    override fun onDestroyView() {
        coroutineScope?.cancel()
        coroutineScope = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        messageClient.removeListener(this)
        super.onDestroy()
    }

    companion object {
        private const val EXTRA_TIMER_ID = "timerId"
    }
}
