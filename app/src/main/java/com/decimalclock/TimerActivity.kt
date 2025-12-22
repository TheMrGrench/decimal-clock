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
                openTimerAlert()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        overridePendingTransition(R.anim.slide_in_up, android.R.anim.fade_out)

        applyThemeColors()
        setupTimeInput()
        setupPresets()
        setupButtons()
        setupCloseButton()
    }

    private fun applyThemeColors() {
        val prefs = getSharedPreferences("DecimalClockPrefs", MODE_PRIVATE)
        val currentHue = prefs.getFloat("colorHue", 270f)
        val pastelColor = SettingsActivity.generatePastelColor(currentHue)

        // Применяем цвет к кнопкам
        val colorStateList = android.content.res.ColorStateList.valueOf(pastelColor)
        binding.startButton.backgroundTintList = colorStateList
        binding.resetButton.backgroundTintList = colorStateList
        binding.closeButton.backgroundTintList = colorStateList

        // Применяем к пресетам
        binding.preset1.backgroundTintList = colorStateList
        binding.preset2.backgroundTintList = colorStateList
        binding.preset5.backgroundTintList = colorStateList
        binding.preset20.backgroundTintList = colorStateList
        binding.preset1h.backgroundTintList = colorStateList
    }

    private fun setupTimeInput() {
        // NumberPicker'ы для десятичного времени
        binding.timerHoursPicker.minValue = 0
        binding.timerHoursPicker.maxValue = 9
        binding.timerHoursPicker.value = 0

        binding.timerMinutesPicker.minValue = 0
        binding.timerMinutesPicker.maxValue = 99
        binding.timerMinutesPicker.value = 0
        binding.timerMinutesPicker.setFormatter { String.format("%02d", it) }

        binding.timerSecondsPicker.minValue = 0
        binding.timerSecondsPicker.maxValue = 99
        binding.timerSecondsPicker.value = 0
        binding.timerSecondsPicker.setFormatter { String.format("%02d", it) }
    }

    private fun setupPresets() {
        // Presets in decimal minutes
        binding.preset1.setOnClickListener {
            binding.timerHoursPicker.value = 0
            binding.timerMinutesPicker.value = 1
            binding.timerSecondsPicker.value = 0
            setTimer(0, 1, 0)
        }
        binding.preset2.setOnClickListener {
            binding.timerHoursPicker.value = 0
            binding.timerMinutesPicker.value = 2
            binding.timerSecondsPicker.value = 0
            setTimer(0, 2, 0)
        }
        binding.preset5.setOnClickListener {
            binding.timerHoursPicker.value = 0
            binding.timerMinutesPicker.value = 5
            binding.timerSecondsPicker.value = 0
            setTimer(0, 5, 0)
        }
        binding.preset20.setOnClickListener {
            binding.timerHoursPicker.value = 0
            binding.timerMinutesPicker.value = 20
            binding.timerSecondsPicker.value = 0
            setTimer(0, 20, 0)
        }
        binding.preset1h.setOnClickListener {
            binding.timerHoursPicker.value = 1
            binding.timerMinutesPicker.value = 0
            binding.timerSecondsPicker.value = 0
            setTimer(1, 0, 0)
        }
    }

    private fun setupButtons() {
        binding.startButton.setOnClickListener {
            if (isRunning) {
                pauseTimer()
            } else {
                // Получаем значения из NumberPicker'ов
                val hours = binding.timerHoursPicker.value
                val minutes = binding.timerMinutesPicker.value
                val seconds = binding.timerSecondsPicker.value

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
