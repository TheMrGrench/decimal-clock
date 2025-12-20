package com.decimalclock

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
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

        setupColorButtons()
        setupToggleButton()
        loadSettings()

        handler.post(updateTimeRunnable)
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
            // Set gradient background
            button.setBackgroundResource(gradients[index])

            // Apply foreground for border
            button.foreground = ContextCompat.getDrawable(this, R.drawable.color_button_background)

            button.setOnClickListener {
                selectColorTheme(index + 1)
            }
        }
    }

    private fun selectColorTheme(theme: Int) {
        if (theme == currentTheme) return

        val oldTheme = currentTheme
        currentTheme = theme

        // Update button selection states
        updateButtonStates()

        // Animate background gradient
        animateBackground(oldTheme - 1, theme - 1)

        // Save to preferences
        prefs.edit().putInt("selectedTheme", theme).apply()
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

    private fun animateBackground(fromTheme: Int, toTheme: Int) {
        val fromColors = colorThemes[fromTheme]
        val toColors = colorThemes[toTheme]

        val fromStartColor = ContextCompat.getColor(this, fromColors.first)
        val fromEndColor = ContextCompat.getColor(this, fromColors.second)
        val toStartColor = ContextCompat.getColor(this, toColors.first)
        val toEndColor = ContextCompat.getColor(this, toColors.second)

        val startColorAnimator = ValueAnimator.ofObject(
            ArgbEvaluator(),
            fromStartColor,
            toStartColor
        )
        val endColorAnimator = ValueAnimator.ofObject(
            ArgbEvaluator(),
            fromEndColor,
            toEndColor
        )

        startColorAnimator.duration = 600
        endColorAnimator.duration = 600

        startColorAnimator.interpolator = AccelerateDecelerateInterpolator()
        endColorAnimator.interpolator = AccelerateDecelerateInterpolator()

        var currentStartColor = fromStartColor
        var currentEndColor = fromEndColor

        startColorAnimator.addUpdateListener { animator ->
            currentStartColor = animator.animatedValue as Int
            updateBackgroundGradient(currentStartColor, currentEndColor)
        }

        endColorAnimator.addUpdateListener { animator ->
            currentEndColor = animator.animatedValue as Int
            updateBackgroundGradient(currentStartColor, currentEndColor)
        }

        startColorAnimator.start()
        endColorAnimator.start()
    }

    private fun updateBackgroundGradient(startColor: Int, endColor: Int) {
        val gradient = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(startColor, endColor)
        )
        binding.rootLayout.background = gradient
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
        // Load theme
        currentTheme = prefs.getInt("selectedTheme", 1)
        updateButtonStates()

        val theme = colorThemes[currentTheme - 1]
        val startColor = ContextCompat.getColor(this, theme.first)
        val endColor = ContextCompat.getColor(this, theme.second)
        updateBackgroundGradient(startColor, endColor)

        // Load time visibility
        timeVisible = prefs.getBoolean("timeVisible", true)
        if (timeVisible) {
            binding.standardTime.visibility = View.VISIBLE
            binding.toggleButton.text = getString(R.string.hide_time)
        } else {
            binding.standardTime.visibility = View.INVISIBLE
            binding.toggleButton.text = getString(R.string.show_time)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateTimeRunnable)
    }
}
