package com.decimalclock

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.decimalclock.databinding.ActivityTimerBinding

class TimerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTimerBinding
    private val handler = Handler(Looper.getMainLooper())

    private var totalDecimalSeconds = 0
    private var remainingDecimalSeconds = 0
    private var isRunning = false

    private val timerRunnable = object : Runnable {
        override fun run() {
            if (remainingDecimalSeconds > 0) {
                remainingDecimalSeconds--
                updateDisplay()
                handler.postDelayed(this, 864) // 86400ms / 100 = 864ms per decimal second
            } else {
                stopTimer()
                // TODO: Play alarm sound
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        overridePendingTransition(R.anim.slide_in_up, android.R.anim.fade_out)

        setupPresets()
        setupButtons()
        setupCloseButton()
    }

    private fun setupPresets() {
        // Presets in decimal minutes
        binding.preset1.setOnClickListener { setTimer(0, 1, 0) }
        binding.preset2.setOnClickListener { setTimer(0, 2, 0) }
        binding.preset3.setOnClickListener { setTimer(0, 3, 0) }
        binding.preset5.setOnClickListener { setTimer(0, 5, 0) }
        binding.preset10.setOnClickListener { setTimer(0, 10, 0) }
        binding.preset15.setOnClickListener { setTimer(0, 15, 0) }
        binding.preset20.setOnClickListener { setTimer(0, 20, 0) }
        binding.preset30.setOnClickListener { setTimer(0, 30, 0) }
        binding.preset1h.setOnClickListener { setTimer(1, 0, 0) }
        binding.preset2h.setOnClickListener { setTimer(2, 0, 0) }
        binding.preset3h.setOnClickListener { setTimer(3, 0, 0) }
    }

    private fun setupButtons() {
        binding.startButton.setOnClickListener {
            if (isRunning) {
                pauseTimer()
            } else {
                // Check for custom input
                val hours = binding.customHours.text.toString().toIntOrNull() ?: 0
                val minutes = binding.customMinutes.text.toString().toIntOrNull() ?: 0
                val seconds = binding.customSeconds.text.toString().toIntOrNull() ?: 0

                if (hours > 0 || minutes > 0 || seconds > 0) {
                    setTimer(hours, minutes, seconds)
                }

                startTimer()
            }
        }

        binding.resetButton.setOnClickListener {
            resetTimer()
        }
    }

    private fun setupCloseButton() {
        binding.closeButton.setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.fade_in, R.anim.slide_out_down)
        }
    }

    private fun setTimer(hours: Int, minutes: Int, seconds: Int) {
        // Convert to decimal seconds
        totalDecimalSeconds = hours * 10000 + minutes * 100 + seconds
        remainingDecimalSeconds = totalDecimalSeconds
        updateDisplay()
    }

    private fun startTimer() {
        if (remainingDecimalSeconds > 0) {
            isRunning = true
            binding.startButton.text = "Пауза"
            handler.post(timerRunnable)
        }
    }

    private fun pauseTimer() {
        isRunning = false
        binding.startButton.text = "Старт"
        handler.removeCallbacks(timerRunnable)
    }

    private fun stopTimer() {
        isRunning = false
        binding.startButton.text = "Старт"
        handler.removeCallbacks(timerRunnable)
    }

    private fun resetTimer() {
        stopTimer()
        remainingDecimalSeconds = totalDecimalSeconds
        updateDisplay()
    }

    private fun updateDisplay() {
        val hours = remainingDecimalSeconds / 10000
        val minutes = (remainingDecimalSeconds % 10000) / 100
        val seconds = remainingDecimalSeconds % 100

        binding.timerDisplay.text = String.format("%d:%02d:%02d", hours, minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(timerRunnable)
    }
}
