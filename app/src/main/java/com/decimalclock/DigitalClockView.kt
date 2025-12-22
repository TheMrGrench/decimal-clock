package com.decimalclock

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import java.util.Calendar

class DigitalClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val timePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val separatorPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val handler = Handler(Looper.getMainLooper())

    private var decimalHours = 0
    private var decimalMinutes = 0
    private var decimalSeconds = 0

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
        // Time digits paint
        timePaint.apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            setShadowLayer(12f, 0f, 4f, Color.parseColor("#80000000"))
        }

        // Label paint (for "hours", "minutes", "seconds")
        labelPaint.apply {
            color = Color.parseColor("#CCFFFFFF")
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            setShadowLayer(6f, 0f, 2f, Color.parseColor("#80000000"))
        }

        // Separator paint (for ":")
        separatorPaint.apply {
            color = Color.parseColor("#E6FFFFFF")
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            setShadowLayer(8f, 0f, 3f, Color.parseColor("#80000000"))
        }

        setLayerType(LAYER_TYPE_SOFTWARE, timePaint)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // Scale text sizes based on view size
        val baseSize = minOf(w, h)
        timePaint.textSize = baseSize * 0.25f
        separatorPaint.textSize = baseSize * 0.20f
        labelPaint.textSize = baseSize * 0.06f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f

        // Draw background card
        drawBackground(canvas)

        // Format time
        val timeStr = String.format("%d:%02d:%02d", decimalHours, decimalMinutes, decimalSeconds)

        // Draw time
        canvas.drawText(timeStr, centerX, centerY, timePaint)

        // Draw labels below
        val labelY = centerY + timePaint.textSize * 0.6f
        val sectionWidth = width / 3f

        labelPaint.textSize = width * 0.04f
        canvas.drawText("часы", sectionWidth * 0.5f, labelY, labelPaint)
        canvas.drawText("минуты", sectionWidth * 1.5f, labelY, labelPaint)
        canvas.drawText("секунды", sectionWidth * 2.5f, labelY, labelPaint)
    }

    private fun drawBackground(canvas: Canvas) {
        // Semi-transparent background for better readability
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#33FFFFFF")
            style = Paint.Style.FILL
        }

        val rect = RectF(
            width * 0.05f,
            height * 0.3f,
            width * 0.95f,
            height * 0.7f
        )
        canvas.drawRoundRect(rect, 32f, 32f, paint)

        // Border
        paint.apply {
            color = Color.parseColor("#66FFFFFF")
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRoundRect(rect, 32f, 32f, paint)
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

        decimalHours = (frac / 10000.0).toInt()
        decimalMinutes = ((frac % 10000) / 100.0).toInt()
        decimalSeconds = (frac % 100).toInt()
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
        // Digital clock uses white for readability
        // Could optionally tint the background or border based on hue
        invalidate()
    }
}
