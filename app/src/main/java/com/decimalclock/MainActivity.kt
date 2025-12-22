package com.decimalclock

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.decimalclock.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: SharedPreferences

    private var timeVisible = true
    private var currentHue = 270f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences("DecimalClockPrefs", Context.MODE_PRIVATE)

        setupFAB()
        loadSettings()
    }

    override fun onResume() {
        super.onResume()
        // Перезагружаем настройки при возврате из Settings
        loadSettings()
    }

    private fun setupFAB() {
        binding.fabSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.fabAlarm.setOnClickListener {
            startActivity(Intent(this, AlarmActivity::class.java))
        }

        binding.fabTimer.setOnClickListener {
            startActivity(Intent(this, TimerActivity::class.java))
        }
    }

    private fun loadSettings() {
        // Загружаем цветовой оттенок
        currentHue = prefs.getFloat("colorHue", 270f)
        val colors = SettingsActivity.generateGradientColors(currentHue)
        updateBackgroundGradient(colors.first, colors.second)

        // Применяем пастельный цвет к кнопкам
        val pastelColor = SettingsActivity.generatePastelColor(currentHue)
        binding.fabAlarm.backgroundTintList = android.content.res.ColorStateList.valueOf(pastelColor)
        binding.fabTimer.backgroundTintList = android.content.res.ColorStateList.valueOf(pastelColor)
        binding.fabSettings.backgroundTintList = android.content.res.ColorStateList.valueOf(pastelColor)

        // Загружаем видимость времени
        timeVisible = prefs.getBoolean("timeVisible", true)
        binding.standardTimeView.visibility = if (timeVisible) View.VISIBLE else View.INVISIBLE

        // Загружаем тип часов (аналоговый/цифровой)
        val isDigital = prefs.getBoolean("clockTypeDigital", false)
        if (isDigital) {
            binding.clockView.visibility = View.GONE
            binding.digitalClockView.visibility = View.VISIBLE
            binding.digitalClockView.setHue(currentHue)
        } else {
            binding.clockView.visibility = View.VISIBLE
            binding.digitalClockView.visibility = View.GONE
            binding.clockView.setHue(currentHue)
        }
    }

    private fun updateBackgroundGradient(startColor: Int, endColor: Int) {
        val gradient = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(startColor, endColor)
        )
        binding.rootLayout.background = gradient
    }
}
