package com.decimalclock

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class DecimalClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val dialPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val markPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val handPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val centerDotPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val handler = Handler(Looper.getMainLooper())
    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f

    // Color hue for dynamic theming
    private var currentHue = 270f

    // Previous values for step animation (second hand only)
    private var prevDs = -1

    // Current angles (in degrees)
    private var secondAngle = 0f
    private var minuteAngle = 0f
    private var hourAngle = 0f

    private val updateRunnable = object : Runnable {
        override fun run() {
            updateTime()
            invalidate()
            handler.postDelayed(this, 100) // Update every 100ms
        }
    }

    init {
        setupPaints()
    }

    private fun setupPaints() {
        // Dial paint (glassmorphism background)
        dialPaint.apply {
            color = Color.parseColor("#33FFFFFF") // white_20 - more visible
            style = Paint.Style.FILL
        }

        // Number paint with shadow for better readability
        numberPaint.apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setShadowLayer(8f, 0f, 0f, Color.parseColor("#80000000"))
        }

        // Mark paint
        markPaint.apply {
            color = Color.parseColor("#B3FFFFFF") // white_70 - more visible
            style = Paint.Style.FILL
        }

        // Hand paint with shadow
        handPaint.apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            setShadowLayer(6f, 0f, 2f, Color.parseColor("#80000000"))
        }

        // Center dot paint
        centerDotPaint.apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            setShadowLayer(12f, 0f, 2f, Color.parseColor("#80000000"))
        }
        setLayerType(LAYER_TYPE_SOFTWARE, numberPaint)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
        radius = min(w, h) / 2f * 0.95f

        // Update text size based on radius
        numberPaint.textSize = radius * 0.15f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw dial background with glassmorphism effect
        drawDial(canvas)

        // Draw marks
        drawMarks(canvas)

        // Draw numbers (0-9)
        drawNumbers(canvas)

        // Draw hands
        drawHands(canvas)

        // Draw center dot
        canvas.drawCircle(centerX, centerY, radius * 0.04f, centerDotPaint)
    }

    private fun drawDial(canvas: Canvas) {
        // Background circle
        dialPaint.color = Color.parseColor("#1AFFFFFF") // white_10
        dialPaint.style = Paint.Style.FILL
        canvas.drawCircle(centerX, centerY, radius, dialPaint)

        // Border
        dialPaint.color = Color.parseColor("#4DFFFFFF") // white_30
        dialPaint.style = Paint.Style.STROKE
        dialPaint.strokeWidth = 2f
        canvas.drawCircle(centerX, centerY, radius, dialPaint)

        // Inner glow effect
        val gradient = RadialGradient(
            centerX, centerY - radius * 0.3f, radius * 1.2f,
            intArrayOf(
                Color.parseColor("#33FFFFFF"),
                Color.parseColor("#00FFFFFF")
            ),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        dialPaint.shader = gradient
        dialPaint.style = Paint.Style.FILL
        canvas.drawCircle(centerX, centerY, radius, dialPaint)
        dialPaint.shader = null
    }

    private fun drawNumbers(canvas: Canvas) {
        val numberRadius = radius * 0.72f

        for (i in 0..9) {
            val angle = Math.toRadians((i * 36 - 90).toDouble())
            val x = centerX + numberRadius * cos(angle).toFloat()
            val y = centerY + numberRadius * sin(angle).toFloat()

            // Draw number with baseline adjustment
            val textBounds = Rect()
            numberPaint.getTextBounds(i.toString(), 0, 1, textBounds)
            canvas.drawText(
                i.toString(),
                x,
                y + textBounds.height() / 2f,
                numberPaint
            )
        }
    }

    private fun drawMarks(canvas: Canvas) {
        for (i in 0 until 100) {
            if (i % 2 == 0) { // Draw marks every 2 positions
                val isMajor = i % 10 == 0
                val angle = Math.toRadians((i * 3.6 - 90).toDouble())

                val markHeight = if (isMajor) radius * 0.08f else radius * 0.05f
                val markWidth = if (isMajor) 4f else 2f
                val markColor = if (isMajor) Color.parseColor("#B3FFFFFF") else Color.parseColor("#4DFFFFFF")

                markPaint.color = markColor
                markPaint.strokeWidth = markWidth

                val startRadius = radius * 0.95f - markHeight
                val endRadius = radius * 0.95f

                val startX = centerX + startRadius * cos(angle).toFloat()
                val startY = centerY + startRadius * sin(angle).toFloat()
                val endX = centerX + endRadius * cos(angle).toFloat()
                val endY = centerY + endRadius * sin(angle).toFloat()

                canvas.drawLine(startX, startY, endX, endY, markPaint)
            }
        }
    }

    private fun drawHands(canvas: Canvas) {
        // Hour hand (thickest, shortest)
        drawHand(canvas, hourAngle, radius * 0.28f, 10f)

        // Minute hand (medium)
        drawHand(canvas, minuteAngle, radius * 0.38f, 7f)

        // Second hand (thinnest, longest)
        drawHand(canvas, secondAngle, radius * 0.42f, 4f)
    }

    private fun drawHand(canvas: Canvas, angle: Float, length: Float, width: Float) {
        handPaint.strokeWidth = width
        val angleRad = Math.toRadians((angle - 90).toDouble())

        val endX = centerX + length * cos(angleRad).toFloat()
        val endY = centerY + length * sin(angleRad).toFloat()

        // Draw hand with rounded cap
        handPaint.style = Paint.Style.STROKE
        canvas.drawLine(centerX, centerY, endX, endY, handPaint)
    }

    private fun updateTime() {
        val now = Calendar.getInstance()
        val h = now.get(Calendar.HOUR_OF_DAY)
        val m = now.get(Calendar.MINUTE)
        val s = now.get(Calendar.SECOND)
        val ms = now.get(Calendar.MILLISECOND)

        // Convert to decimal time
        val totalSec = h * 3600 + m * 60 + s + ms / 1000.0
        val frac = totalSec / 86400.0 * 100000.0

        // Integer values for step animation (second hand)
        val ds = (frac % 100).toInt()

        // Floating point values for smooth animation (minute and hour hands)
        val dmSmooth = (frac % 10000) / 100.0
        val dhSmooth = frac / 10000.0

        // Second hand: step animation (only update when value changes)
        if (ds != prevDs) {
            secondAngle = ds * 3.6f
            prevDs = ds
        }

        // Minute hand: smooth animation
        minuteAngle = (dmSmooth * 3.6).toFloat()

        // Hour hand: smooth animation
        hourAngle = (dhSmooth * 36).toFloat()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        handler.post(updateRunnable)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacks(updateRunnable)
    }

    fun setHue(hue: Float) {
        currentHue = hue
        updateColorsFromHue()
        invalidate()
    }

    private fun updateColorsFromHue() {
        // Keep white color for maximum readability
        // The shadow provides contrast on any background
    }
}
