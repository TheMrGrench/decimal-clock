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

    private val digitPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG)

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
        // Digit paint - немного менее контрастный
        digitPaint.apply {
            color = Color.WHITE
            alpha = 230
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            setShadowLayer(4f, 0f, 2f, Color.parseColor("#40000000"))
        }

        // Label paint - white subtle
        labelPaint.apply {
            color = Color.WHITE
            alpha = 200
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }

        // Card background paint - dark semi-transparent for contrast
        cardPaint.apply {
            color = Color.parseColor("#50000000")
            style = Paint.Style.FILL
            setShadowLayer(8f, 0f, 3f, Color.parseColor("#60000000"))
        }

        setLayerType(LAYER_TYPE_SOFTWARE, cardPaint)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // Scale text sizes based on view size
        val baseSize = minOf(w, h)
        digitPaint.textSize = baseSize * 0.18f
        labelPaint.textSize = baseSize * 0.045f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerY = height / 2f
        val cardWidth = width * 0.25f
        val cardHeight = height * 0.35f
        val cardSpacing = width * 0.05f

        // Calculate positions for 3 cards
        val totalWidth = cardWidth * 3 + cardSpacing * 2
        val startX = (width - totalWidth) / 2f

        // Draw hour card
        drawTimeCard(
            canvas,
            startX,
            centerY - cardHeight / 2f,
            cardWidth,
            cardHeight,
            decimalHours.toString(),
            "грч"
        )

        // Draw minute card
        drawTimeCard(
            canvas,
            startX + cardWidth + cardSpacing,
            centerY - cardHeight / 2f,
            cardWidth,
            cardHeight,
            String.format("%02d", decimalMinutes),
            "грм"
        )

        // Draw second card
        drawTimeCard(
            canvas,
            startX + (cardWidth + cardSpacing) * 2,
            centerY - cardHeight / 2f,
            cardWidth,
            cardHeight,
            String.format("%02d", decimalSeconds),
            "грс"
        )
    }

    private fun drawTimeCard(
        canvas: Canvas,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        digit: String,
        label: String
    ) {
        val rect = RectF(x, y, x + width, y + height)
        val cornerRadius = 16f

        // Draw card background
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, cardPaint)

        // Draw digit
        val digitY = y + height * 0.45f
        canvas.drawText(digit, x + width / 2f, digitY, digitPaint)

        // Draw label
        val labelY = y + height * 0.75f
        canvas.drawText(label, x + width / 2f, labelY, labelPaint)
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
