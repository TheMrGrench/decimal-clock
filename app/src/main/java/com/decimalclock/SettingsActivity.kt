package com.decimalclock

import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.decimalclock.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private var currentTheme = 1

    private val themeNames = arrayOf(
        "Тема 1: Фиолетовый",
        "Тема 2: Розовый",
        "Тема 3: Голубой",
        "Тема 4: Зелёный",
        "Тема 5: Оранжевый",
        "Тема 6: Синий"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply entrance animation
        overridePendingTransition(R.anim.slide_in_up, android.R.anim.fade_out)

        loadSettings()
        setupThemeSeekBar()
        setupTimeVisibilitySwitch()
        setupCloseButton()
    }

    private fun setupThemeSeekBar() {
        binding.themeSeekBar.progress = currentTheme - 1
        binding.themeNameText.text = themeNames[currentTheme - 1]

        binding.themeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentTheme = progress + 1
                binding.themeNameText.text = themeNames[progress]
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                selectColorTheme(currentTheme)
            }
        })
    }

    private fun setupTimeVisibilitySwitch() {
        val prefs = getSharedPreferences("DecimalClockPrefs", MODE_PRIVATE)
        val timeVisible = prefs.getBoolean("timeVisible", true)

        binding.timeVisibilitySwitch.isChecked = timeVisible

        binding.timeVisibilitySwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("timeVisible", isChecked).apply()
        }
    }

    private fun setupCloseButton() {
        binding.closeButton.setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.fade_in, R.anim.slide_out_down)
        }
    }

    private fun selectColorTheme(theme: Int) {
        val prefs = getSharedPreferences("DecimalClockPrefs", MODE_PRIVATE)
        prefs.edit().putInt("selectedTheme", theme).apply()
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences("DecimalClockPrefs", MODE_PRIVATE)
        currentTheme = prefs.getInt("selectedTheme", 1)
    }
}
