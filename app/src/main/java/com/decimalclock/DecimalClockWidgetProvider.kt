package com.decimalclock

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.RemoteViews
import java.util.Calendar

class DecimalClockWidgetProvider : AppWidgetProvider() {

    companion object {
        private var updateHandler: Handler? = null
        private var updateRunnable: Runnable? = null
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }

        // Start periodic updates
        startPeriodicUpdates(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        startPeriodicUpdates(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        stopPeriodicUpdates()
    }

    private fun startPeriodicUpdates(context: Context) {
        if (updateHandler == null) {
            updateHandler = Handler(Looper.getMainLooper())
            updateRunnable = object : Runnable {
                override fun run() {
                    updateAllWidgets(context)
                    updateHandler?.postDelayed(this, 1000) // Update every second
                }
            }
            updateHandler?.post(updateRunnable!!)
        }
    }

    private fun stopPeriodicUpdates() {
        updateRunnable?.let { updateHandler?.removeCallbacks(it) }
        updateHandler = null
        updateRunnable = null
    }

    private fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val thisWidget = ComponentName(context, DecimalClockWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)

        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_decimal_clock)

        // Calculate decimal time
        val now = Calendar.getInstance()
        val h = now.get(Calendar.HOUR_OF_DAY)
        val m = now.get(Calendar.MINUTE)
        val s = now.get(Calendar.SECOND)

        val totalSec = h * 3600 + m * 60 + s
        val frac = totalSec / 86400.0 * 100000.0

        val dh = (frac / 10000.0).toInt()
        val dm = ((frac % 10000) / 100.0).toInt()
        val ds = (frac % 100).toInt()

        val decimalTime = String.format("%d:%02d:%02d", dh, dm, ds)
        val standardTime = String.format("%02d:%02d:%02d", h, m, s)

        views.setTextViewText(R.id.widgetDecimalTime, decimalTime)
        views.setTextViewText(R.id.widgetStandardTime, standardTime)

        // Set up click intent to open the app
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widgetDecimalTime, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
