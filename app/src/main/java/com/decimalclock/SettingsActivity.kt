package com.decimalclock

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.decimalclock.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private var currentTheme = 1

    private val colorThemes = arrayOf(
        Pair(R.color.theme1_start, R.color.theme1_end),
        Pair(R.color.theme2_start, R.color.theme2_end),
        Pair(R.color.theme3_start, R.color.theme3_end),
        Pair(R.color.theme4_start, R.color.theme4_end),
        Pair(R.color.theme5_start, R.color.theme5_end),
        Pair(R.color.theme6_start, R.color.theme6_end)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Настройка ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Настройки"

        loadSettings()
        setupColorButtons()
        setupTimeVisibilitySwitch()
    }

    private fun setupColorButtons() {
        val colorButtons = listOf(
            binding.colorBtn1,
            binding.colorBtn2,
            binding.colorBtn3,
            binding.colorBtn4,
            binding.colorBtn5,
            binding.colorBtn6
        )

        val gradients = listOf(
            R.drawable.gradient_theme1,
            R.drawable.gradient_theme2,
            R.drawable.gradient_theme3,
            R.drawable.gradient_theme4,
            R.drawable.gradient_theme5,
            R.drawable.gradient_theme6
        )

        colorButtons.forEachIndexed { index, button ->
            button.setBackgroundResource(gradients[index])
            button.foreground = ContextCompat.getDrawable(this, R.drawable.color_button_background)

            button.setOnClickListener {
                selectColorTheme(index + 1)
                updateButtonStates()
            }
        }

        updateButtonStates()
    }

    private fun setupTimeVisibilitySwitch() {
        val prefs = getSharedPreferences("DecimalClockPrefs", MODE_PRIVATE)
        val timeVisible = prefs.getBoolean("timeVisible", true)

        binding.timeVisibilitySwitch.isChecked = timeVisible

        binding.timeVisibilitySwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("timeVisible", isChecked).apply()
            Toast.makeText(this,
                if (isChecked) "Время будет отображаться" else "Время будет скрыто",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun selectColorTheme(theme: Int) {
        currentTheme = theme
        val prefs = getSharedPreferences("DecimalClockPrefs", MODE_PRIVATE)
        prefs.edit().putInt("selectedTheme", theme).apply()

        Toast.makeText(this, "Тема ${theme} выбрана", Toast.LENGTH_SHORT).show()
    }

    private fun updateButtonStates() {
        val colorButtons = listOf(
            binding.colorBtn1,
            binding.colorBtn2,
            binding.colorBtn3,
            binding.colorBtn4,
            binding.colorBtn5,
            binding.colorBtn6
        )

        colorButtons.forEachIndexed { index, button ->
            button.isSelected = (index + 1) == currentTheme
        }
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences("DecimalClockPrefs", MODE_PRIVATE)
        currentTheme = prefs.getInt("selectedTheme", 1)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
