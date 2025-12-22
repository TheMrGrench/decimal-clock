package com.decimalclock

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.decimalclock.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private var currentHue = 270f // Default purple

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply entrance animation
        overridePendingTransition(R.anim.slide_in_up, android.R.anim.fade_out)

        loadSettings()
        applyThemeColors()
        setupColorHueSeekBar()
        setupTimeVisibilitySwitch()
        setupClockTypeSwitch()
        setupCloseButton()
    }

    private fun applyThemeColors() {
        val pastelColor = generatePastelColor(currentHue)
        binding.closeButton.backgroundTintList = android.content.res.ColorStateList.valueOf(pastelColor)

        // Применяем цвет к переключателям
        // Когда включен - цвет темы, когда выключен - цвет карточек (#50000000)
        val cardColor = Color.parseColor("#50000000")
        val switchThumbColor = android.content.res.ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            ),
            intArrayOf(pastelColor, cardColor)
        )
        val switchTrackColor = android.content.res.ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            ),
            intArrayOf(Color.argb(128, Color.red(pastelColor), Color.green(pastelColor), Color.blue(pastelColor)), cardColor)
        )

        binding.timeVisibilitySwitch.thumbTintList = switchThumbColor
        binding.timeVisibilitySwitch.trackTintList = switchTrackColor
        binding.clockTypeSwitch.thumbTintList = switchThumbColor
        binding.clockTypeSwitch.trackTintList = switchTrackColor
    }

    private fun setupColorHueSeekBar() {
        binding.colorHueSeekBar.progress = currentHue.toInt()
        updateColorPreview(currentHue)
        updateBackgroundGradient(currentHue)

        binding.colorHueSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentHue = progress.toFloat()
                binding.colorHueText.text = "Оттенок: ${progress}°"
                updateColorPreview(currentHue)
                updateBackgroundGradient(currentHue)
                applyThemeColors() // Обновляем цвета элементов
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                saveColorHue(currentHue)
            }
        })
    }

    private fun updateColorPreview(hue: Float) {
        val color = Color.HSVToColor(floatArrayOf(hue, 0.7f, 0.9f))
        val drawable = binding.colorPreview.background as GradientDrawable
        drawable.setColor(color)
    }

    private fun updateBackgroundGradient(hue: Float) {
        val colors = generateGradientColors(hue)
        val gradient = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(colors.first, colors.second)
        )
        binding.root.background = gradient
    }

    private fun setupTimeVisibilitySwitch() {
        val prefs = getSharedPreferences("DecimalClockPrefs", MODE_PRIVATE)
        val timeVisible = prefs.getBoolean("timeVisible", true)

        binding.timeVisibilitySwitch.isChecked = timeVisible

        binding.timeVisibilitySwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("timeVisible", isChecked).apply()
        }
    }

    private fun setupClockTypeSwitch() {
        val prefs = getSharedPreferences("DecimalClockPrefs", MODE_PRIVATE)
        val isDigital = prefs.getBoolean("clockTypeDigital", false)

        binding.clockTypeSwitch.isChecked = isDigital

        binding.clockTypeSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("clockTypeDigital", isChecked).apply()
        }
    }

    private fun setupCloseButton() {
        binding.closeButton.setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.fade_in, R.anim.slide_out_down)
        }
    }

    private fun saveColorHue(hue: Float) {
        val prefs = getSharedPreferences("DecimalClockPrefs", MODE_PRIVATE)
        prefs.edit().putFloat("colorHue", hue).apply()
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences("DecimalClockPrefs", MODE_PRIVATE)
        currentHue = prefs.getFloat("colorHue", 270f)
    }

    companion object {
        fun generateGradientColors(hue: Float): Pair<Int, Int> {
            // Lighter color (higher value, lower saturation)
            val startColor = Color.HSVToColor(floatArrayOf(hue, 0.6f, 0.95f))
            // Darker color (lower value, higher saturation)
            val endColor = Color.HSVToColor(floatArrayOf(hue, 0.8f, 0.7f))
            return Pair(startColor, endColor)
        }

        fun generatePastelColor(hue: Float): Int {
            // Pastel: low saturation, high brightness, more opacity
            return Color.HSVToColor(200, floatArrayOf(hue, 0.45f, 0.95f))
        }
    }
}
