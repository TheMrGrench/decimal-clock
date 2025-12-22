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
        // Time digits paint - clean and bold
        timePaint.apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setShadowLayer(16f, 0f, 0f, Color.parseColor("#50000000"))
        }

        // Label paint - subtle
        labelPaint.apply {
            color = Color.parseColor("#B3FFFFFF")
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            setShadowLayer(4f, 0f, 0f, Color.parseColor("#50000000"))
        }

        // Separator paint
        separatorPaint.apply {
            color = Color.parseColor("#CCFFFFFF")
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setShadowLayer(8f, 0f, 0f, Color.parseColor("#50000000"))
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

        // Format time
        val timeStr = String.format("%d:%02d:%02d", decimalHours, decimalMinutes, decimalSeconds)

        // Draw time - clean, no background
        val textBounds = Rect()
        timePaint.getTextBounds(timeStr, 0, timeStr.length, textBounds)
        canvas.drawText(timeStr, centerX, centerY - textBounds.exactCenterY(), timePaint)

        // Draw subtle labels below
        val labelY = centerY + timePaint.textSize * 0.5f
        val sectionWidth = width / 3f

        canvas.drawText("часы", sectionWidth * 0.5f, labelY, labelPaint)
        canvas.drawText("минуты", sectionWidth * 1.5f, labelY, labelPaint)
        canvas.drawText("секунды", sectionWidth * 2.5f, labelY, labelPaint)
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
