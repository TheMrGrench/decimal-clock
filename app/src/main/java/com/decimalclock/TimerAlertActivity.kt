package com.decimalclock

import android.graphics.drawable.GradientDrawable
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.decimalclock.databinding.ActivityTimerAlertBinding

class TimerAlertActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTimerAlertBinding
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

        binding = ActivityTimerAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set background gradient
        setupBackground()

        // Play alarm sound
        playAlarmSound()

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
    }

    private fun stopAlarm() {
        ringtone?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarm()
    }
}
