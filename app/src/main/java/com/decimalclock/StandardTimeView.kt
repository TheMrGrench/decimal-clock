package com.decimalclock

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import java.util.Calendar

class StandardTimeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val digitPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val handler = Handler(Looper.getMainLooper())

    private var hours = 0
    private var minutes = 0
    private var seconds = 0

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
        // Digit paint - bright white with subtle shadow
        digitPaint.apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            setShadowLayer(4f, 0f, 2f, Color.parseColor("#40000000"))
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
        digitPaint.textSize = baseSize * 0.5f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerY = height / 2f
        val cardWidth = width * 0.28f
        val cardHeight = height * 0.9f
        val cardSpacing = width * 0.04f

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
            String.format("%02d", hours)
        )

        // Draw colon separator
        val colonX1 = startX + cardWidth + cardSpacing / 2f
        canvas.drawText(":", colonX1, centerY + digitPaint.textSize * 0.15f, digitPaint)

        // Draw minute card
        drawTimeCard(
            canvas,
            startX + cardWidth + cardSpacing,
            centerY - cardHeight / 2f,
            cardWidth,
            cardHeight,
            String.format("%02d", minutes)
        )

        // Draw colon separator
        val colonX2 = startX + (cardWidth + cardSpacing) * 2 - cardSpacing / 2f
        canvas.drawText(":", colonX2, centerY + digitPaint.textSize * 0.15f, digitPaint)

        // Draw second card
        drawTimeCard(
            canvas,
            startX + (cardWidth + cardSpacing) * 2,
            centerY - cardHeight / 2f,
            cardWidth,
            cardHeight,
            String.format("%02d", seconds)
        )
    }

    private fun drawTimeCard(
        canvas: Canvas,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        digit: String
    ) {
        val rect = RectF(x, y, x + width, y + height)
        val cornerRadius = 16f

        // Draw card background
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, cardPaint)

        // Draw digit
        val digitY = y + height / 2f + digitPaint.textSize * 0.35f
        canvas.drawText(digit, x + width / 2f, digitY, digitPaint)
    }

    private fun updateTime() {
        val now = Calendar.getInstance()
        hours = now.get(Calendar.HOUR_OF_DAY)
        minutes = now.get(Calendar.MINUTE)
        seconds = now.get(Calendar.SECOND)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        handler.post(updateRunnable)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacks(updateRunnable)
    }
}
