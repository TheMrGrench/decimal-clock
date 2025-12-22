package com.decimalclock

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.decimalclock.databinding.ActivityAlarmBinding
import java.util.Calendar

class AlarmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmBinding
    private var isAlarmSet = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        overridePendingTransition(R.anim.slide_in_up, android.R.anim.fade_out)

        setupTimeInput()
        setupButtons()
        setupCloseButton()
        loadSavedAlarm()
    }

    private fun setupTimeInput() {
        binding.alarmTimeInput.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private var cursorPosition = 0

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!isUpdating) {
                    cursorPosition = start + count
                }
            }

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating || s == null) return

                isUpdating = true

                val input = s.toString().replace(":", "").filter { it.isDigit() }

                val formatted = when {
                    input.isEmpty() -> ""
                    input.length == 1 -> "$input:"
                    input.length == 2 -> "${input[0]}:${input[1]}"
                    input.length >= 3 -> "${input[0]}:${input.substring(1, minOf(3, input.length))}"
                    else -> input
                }

                if (formatted != s.toString()) {
                    s.replace(0, s.length, formatted)

                    // Set cursor position
                    val newCursorPos = when {
                        formatted.isEmpty() -> 0
                        cursorPosition <= 1 -> minOf(cursorPosition, formatted.length)
                        cursorPosition == 2 && formatted.length >= 2 -> 2
                        else -> formatted.length
                    }

                    try {
                        binding.alarmTimeInput.setSelection(minOf(newCursorPos, formatted.length))
                    } catch (e: Exception) {
                        // Ignore cursor positioning errors
                    }
                }

                isUpdating = false
            }
        })
    }

    private fun setupButtons() {
        binding.setAlarmButton.setOnClickListener {
            val text = binding.alarmTimeInput.text.toString()
            val parts = text.split(":")

            if (parts.size == 2) {
                val hours = parts[0].toIntOrNull()
                val minutes = parts[1].toIntOrNull() ?: 0

                if (hours != null && hours in 0..9 && minutes in 0..99) {
                    setAlarm(hours, minutes)
                }
            }
        }

        binding.cancelAlarmButton.setOnClickListener {
            cancelAlarm()
        }
    }

    private fun setupCloseButton() {
        binding.closeButton.setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.fade_in, R.anim.slide_out_down)
        }
    }

    private fun setAlarm(decimalHours: Int, decimalMinutes: Int) {
        // Convert decimal time to standard time
        val decimalTime = decimalHours * 10000 + decimalMinutes * 100
        val fractionOfDay = decimalTime / 100000.0
        val totalSeconds = (fractionOfDay * 86400).toLong()

        val targetHours = (totalSeconds / 3600).toInt()
        val targetMinutes = ((totalSeconds % 3600) / 60).toInt()
        val targetSeconds = (totalSeconds % 60).toInt()

        // Set alarm for target time
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, targetHours)
            set(Calendar.MINUTE, targetMinutes)
            set(Calendar.SECOND, targetSeconds)
            set(Calendar.MILLISECOND, 0)

            // If time has passed today, set for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

        // Save alarm info
        val prefs = getSharedPreferences("DecimalClockPrefs", MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("alarmSet", true)
            putInt("alarmDecimalHours", decimalHours)
            putInt("alarmDecimalMinutes", decimalMinutes)
            putLong("alarmTimeMillis", calendar.timeInMillis)
            apply()
        }

        isAlarmSet = true
        binding.cancelAlarmButton.isEnabled = true
        updateTimeUntilAlarm(calendar.timeInMillis)
    }

    private fun cancelAlarm() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)

        val prefs = getSharedPreferences("DecimalClockPrefs", MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("alarmSet", false)
            apply()
        }

        isAlarmSet = false
        binding.cancelAlarmButton.isEnabled = false
        hideTimeUntilAlarm()
    }

    private fun loadSavedAlarm() {
        val prefs = getSharedPreferences("DecimalClockPrefs", MODE_PRIVATE)
        isAlarmSet = prefs.getBoolean("alarmSet", false)

        if (isAlarmSet) {
            val hours = prefs.getInt("alarmDecimalHours", 0)
            val minutes = prefs.getInt("alarmDecimalMinutes", 0)
            val alarmTime = prefs.getLong("alarmTimeMillis", 0)

            binding.alarmTimeInput.setText(String.format("%d:%02d", hours, minutes))
            binding.cancelAlarmButton.isEnabled = true

            if (alarmTime > System.currentTimeMillis()) {
                updateTimeUntilAlarm(alarmTime)
            } else {
                // Alarm time has passed, cancel it
                cancelAlarm()
            }
        }
    }

    private fun updateTimeUntilAlarm(alarmTimeMillis: Long) {
        val timeUntilAlarmMs = alarmTimeMillis - System.currentTimeMillis()
        val timeUntilAlarmSeconds = timeUntilAlarmMs / 1000

        // Convert to decimal time
        val decimalFraction = timeUntilAlarmSeconds / 86400.0 * 100000.0
        val dh = (decimalFraction / 10000).toInt()
        val dm = ((decimalFraction % 10000) / 100).toInt()
        val ds = (decimalFraction % 100).toInt()

        // Standard time
        val sh = (timeUntilAlarmSeconds / 3600).toInt()
        val sm = ((timeUntilAlarmSeconds % 3600) / 60).toInt()

        // Check if standard time should be shown
        val prefs = getSharedPreferences("DecimalClockPrefs", MODE_PRIVATE)
        val showStandardTime = prefs.getBoolean("timeVisible", true)

        binding.timeUntilAlarmLabel.visibility = View.VISIBLE
        binding.timeUntilAlarmDecimal.visibility = View.VISIBLE
        binding.timeUntilAlarmDecimal.text = String.format("Десятичное: %d:%02d:%02d", dh, dm, ds)

        if (showStandardTime) {
            binding.timeUntilAlarmStandard.visibility = View.VISIBLE
            binding.timeUntilAlarmStandard.text = String.format("Обычное: %d ч %d мин", sh, sm)
        } else {
            binding.timeUntilAlarmStandard.visibility = View.GONE
        }

        binding.divider2.visibility = View.VISIBLE
    }

    private fun hideTimeUntilAlarm() {
        binding.timeUntilAlarmLabel.visibility = View.GONE
        binding.timeUntilAlarmDecimal.visibility = View.GONE
        binding.timeUntilAlarmStandard.visibility = View.GONE
        binding.divider2.visibility = View.GONE
    }
}
