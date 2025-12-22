package com.decimalclock

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
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

        applyThemeColors()
        setupTimeInput()
        setupButtons()
        setupCloseButton()
        loadSavedAlarm()
    }

    private fun applyThemeColors() {
        val prefs = getSharedPreferences("DecimalClockPrefs", MODE_PRIVATE)
        val currentHue = prefs.getFloat("colorHue", 270f)
        val pastelColor = SettingsActivity.generatePastelColor(currentHue)

        // Применяем цвет к кнопкам
        val colorStateList = android.content.res.ColorStateList.valueOf(pastelColor)
        binding.setAlarmButton.backgroundTintList = colorStateList
        binding.cancelAlarmButton.backgroundTintList = colorStateList
        binding.closeButton.backgroundTintList = colorStateList
    }

    private fun setupTimeInput() {
        // TimePicker для выбора времени
        binding.alarmTimePicker.setIs24HourView(true)
    }

    private fun setupButtons() {
        binding.setAlarmButton.setOnClickListener {
            val hours = binding.alarmTimePicker.hour
            val minutes = binding.alarmTimePicker.minute

            // Конвертируем в десятичное время
            val totalMinutes = hours * 60 + minutes
            val decimalHours = (totalMinutes * 10.0 / 1440.0).toInt()
            val decimalMinutes = ((totalMinutes * 10.0 / 1440.0 - decimalHours) * 100).toInt()

            setAlarm(decimalHours, decimalMinutes)
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
            val decimalHours = prefs.getInt("alarmDecimalHours", 0)
            val decimalMinutes = prefs.getInt("alarmDecimalMinutes", 0)
            val alarmTime = prefs.getLong("alarmTimeMillis", 0)

            // Конвертируем десятичное время обратно в обычное
            val decimalTime = decimalHours * 10000 + decimalMinutes * 100
            val totalMinutes = (decimalTime / 10.0 * 1440.0 / 10000.0).toInt()
            val standardHours = totalMinutes / 60
            val standardMinutes = totalMinutes % 60

            binding.alarmTimePicker.hour = standardHours
            binding.alarmTimePicker.minute = standardMinutes
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
