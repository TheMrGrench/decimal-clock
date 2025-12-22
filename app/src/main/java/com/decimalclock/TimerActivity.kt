package com.decimalclock

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
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
                openTimerAlert()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        overridePendingTransition(R.anim.slide_in_up, android.R.anim.fade_out)

        setupTimeInput()
        setupPresets()
        setupButtons()
        setupCloseButton()
    }

    private fun setupTimeInput() {
        binding.customTimeInput.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return

                isUpdating = true

                val input = s.toString().replace(":", "").filter { it.isDigit() }

                if (input.isEmpty()) {
                    s?.clear()
                } else {
                    val formatted = when (input.length) {
                        1 -> "$input:"
                        2 -> "${input[0]}:${input[1]}"
                        3 -> "${input[0]}:${input.substring(1)}"
                        4 -> "${input[0]}:${input.substring(1, 3)}:${input[3]}"
                        5, 6 -> "${input[0]}:${input.substring(1, 3)}:${input.substring(3)}"
                        else -> "${input[0]}:${input.substring(1, 3)}:${input.substring(3, 5)}"
                    }

                    if (formatted != s.toString()) {
                        s?.replace(0, s.length, formatted)
                    }
                }

                isUpdating = false
            }
        })
    }

    private fun setupPresets() {
        // Presets in decimal minutes
        binding.preset1.setOnClickListener { setTimer(0, 1, 0) }
        binding.preset2.setOnClickListener { setTimer(0, 2, 0) }
        binding.preset5.setOnClickListener { setTimer(0, 5, 0) }
        binding.preset20.setOnClickListener { setTimer(0, 20, 0) }
        binding.preset1h.setOnClickListener { setTimer(1, 0, 0) }
    }

    private fun setupButtons() {
        binding.startButton.setOnClickListener {
            if (isRunning) {
                pauseTimer()
            } else {
                // Check for custom input
                val text = binding.customTimeInput.text.toString()
                val parts = text.split(":")

                var hours = 0
                var minutes = 0
                var seconds = 0

                when (parts.size) {
                    3 -> {
                        hours = parts[0].toIntOrNull() ?: 0
                        minutes = parts[1].toIntOrNull() ?: 0
                        seconds = parts[2].toIntOrNull() ?: 0
                    }
                    2 -> {
                        hours = parts[0].toIntOrNull() ?: 0
                        minutes = parts[1].toIntOrNull() ?: 0
                    }
                    1 -> {
                        hours = parts[0].toIntOrNull() ?: 0
                    }
                }

                if (hours in 0..9 && minutes in 0..99 && seconds in 0..99) {
                    if (hours > 0 || minutes > 0 || seconds > 0) {
                        setTimer(hours, minutes, seconds)
                    }
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

    private fun openTimerAlert() {
        val intent = android.content.Intent(this, TimerAlertActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(timerRunnable)
    }
}
