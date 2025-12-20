package com.decimalclock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Play alarm sound
        val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val ringtone = RingtoneManager.getRingtone(context, alarmUri)
        ringtone.play()

        // Clear alarm from preferences
        val prefs = context.getSharedPreferences("DecimalClockPrefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("alarmSet", false)
            apply()
        }

        // TODO: Show notification or open alarm dismiss activity
    }
}
