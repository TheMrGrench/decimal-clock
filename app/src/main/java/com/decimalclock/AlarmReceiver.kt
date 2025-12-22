package com.decimalclock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Get saved alarm info
        val prefs = context.getSharedPreferences("DecimalClockPrefs", Context.MODE_PRIVATE)
        val decimalHours = prefs.getInt("alarmDecimalHours", 0)
        val decimalMinutes = prefs.getInt("alarmDecimalMinutes", 0)
        val decimalTime = String.format("%d:%02d", decimalHours, decimalMinutes)

        // Clear alarm from preferences
        prefs.edit().apply {
            putBoolean("alarmSet", false)
            apply()
        }

        // Open AlarmDismissActivity
        val alarmIntent = Intent(context, AlarmDismissActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("ALARM_TIME", decimalTime)
        }
        context.startActivity(alarmIntent)
    }
}
