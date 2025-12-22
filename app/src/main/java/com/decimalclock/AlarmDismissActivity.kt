package com.decimalclock

import android.graphics.drawable.GradientDrawable
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.decimalclock.databinding.ActivityAlarmDismissBinding

class AlarmDismissActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmDismissBinding
    private var ringtone: Ringtone? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show on lock screen
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        binding = ActivityAlarmDismissBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set background gradient
        setupBackground()

        // Play alarm sound
        playAlarmSound()

        // Get alarm time from intent
        val decimalTime = intent.getStringExtra("ALARM_TIME") ?: "0:00"
        binding.alarmTimeText.text = decimalTime

        setupButtons()
    }

    private fun setupBackground() {
        val prefs = getSharedPreferences("DecimalClockPrefs", MODE_PRIVATE)
        val colorHue = prefs.getFloat("colorHue", 270f)
        val colors = SettingsActivity.generateGradientColors(colorHue)

        val gradient = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(colors.first, colors.second)
        )
        binding.rootLayout.background = gradient
    }

    private fun playAlarmSound() {
        val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        ringtone = RingtoneManager.getRingtone(this, alarmUri)
        ringtone?.play()
    }

    private fun setupButtons() {
        binding.dismissButton.setOnClickListener {
            stopAlarm()
            finish()
        }

        binding.snoozeButton.setOnClickListener {
            stopAlarm()
            // Snooze for 10 decimal minutes (about 14.4 standard minutes)
            snoozeAlarm()
            finish()
        }
    }

    private fun stopAlarm() {
        ringtone?.stop()
    }

    private fun snoozeAlarm() {
        // 10 decimal minutes = 1000 decimal seconds = 864 standard seconds
        val snoozeMillis = 864000L

        val prefs = getSharedPreferences("DecimalClockPrefs", MODE_PRIVATE)
        val currentTime = System.currentTimeMillis()
        val snoozeTime = currentTime + snoozeMillis

        prefs.edit().apply {
            putBoolean("alarmSet", true)
            putLong("alarmTimeMillis", snoozeTime)
            apply()
        }

        // Re-schedule alarm
        val alarmManager = getSystemService(ALARM_SERVICE) as android.app.AlarmManager
        val intent = android.content.Intent(this, AlarmReceiver::class.java)
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            this,
            0,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            android.app.AlarmManager.RTC_WAKEUP,
            snoozeTime,
            pendingIntent
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarm()
    }
}
