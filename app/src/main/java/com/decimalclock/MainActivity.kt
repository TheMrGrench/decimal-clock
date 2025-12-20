package com.decimalclock

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.decimalclock.databinding.ActivityMainBinding
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: SharedPreferences
    private val handler = Handler(Looper.getMainLooper())

    private var timeVisible = true
    private var currentTheme = 1

    private val colorThemes = arrayOf(
        Pair(R.color.theme1_start, R.color.theme1_end),
        Pair(R.color.theme2_start, R.color.theme2_end),
        Pair(R.color.theme3_start, R.color.theme3_end),
        Pair(R.color.theme4_start, R.color.theme4_end),
        Pair(R.color.theme5_start, R.color.theme5_end),
        Pair(R.color.theme6_start, R.color.theme6_end)
    )

    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            updateStandardTime()
            handler.postDelayed(this, 100)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences("DecimalClockPrefs", Context.MODE_PRIVATE)

        setupToggleButton()
        loadSettings()

        handler.post(updateTimeRunnable)
    }

    override fun onResume() {
        super.onResume()
        // Перезагружаем настройки при возврате из Settings
        loadSettings()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupToggleButton() {
        binding.toggleButton.setOnClickListener {
            timeVisible = !timeVisible

            if (timeVisible) {
                binding.standardTime.visibility = View.VISIBLE
                binding.toggleButton.text = getString(R.string.hide_time)
            } else {
                binding.standardTime.visibility = View.INVISIBLE
                binding.toggleButton.text = getString(R.string.show_time)
            }

            prefs.edit().putBoolean("timeVisible", timeVisible).apply()
        }
    }

    private fun updateStandardTime() {
        val now = Calendar.getInstance()
        val h = now.get(Calendar.HOUR_OF_DAY)
        val m = now.get(Calendar.MINUTE)
        val s = now.get(Calendar.SECOND)

        binding.standardTime.text = String.format("%02d:%02d:%02d", h, m, s)
    }

    private fun loadSettings() {
        // Загружаем тему
        currentTheme = prefs.getInt("selectedTheme", 1)
        val theme = colorThemes[currentTheme - 1]
        val startColor = ContextCompat.getColor(this, theme.first)
        val endColor = ContextCompat.getColor(this, theme.second)
        updateBackgroundGradient(startColor, endColor)

        // Загружаем видимость времени
        timeVisible = prefs.getBoolean("timeVisible", true)
        if (timeVisible) {
            binding.standardTime.visibility = View.VISIBLE
            binding.toggleButton.text = getString(R.string.hide_time)
        } else {
            binding.standardTime.visibility = View.INVISIBLE
            binding.toggleButton.text = getString(R.string.show_time)
        }
    }

    private fun updateBackgroundGradient(startColor: Int, endColor: Int) {
        val gradient = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(startColor, endColor)
        )
        binding.rootLayout.background = gradient
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateTimeRunnable)
    }
}
