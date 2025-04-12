package com.example.leathercraftpro

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * A custom view that displays a palette of colors for the user to select
 */
class ColorPickerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val colors = listOf(
        Color.BLACK,
        Color.DKGRAY,
        Color.GRAY,
        Color.WHITE,
        Color.RED,
        Color.rgb(139, 69, 19), // Brown
        Color.rgb(160, 82, 45), // Sienna
        Color.rgb(210, 105, 30), // Chocolate
        Color.rgb(222, 184, 135), // Burlywood
        Color.rgb(245, 222, 179)  // Wheat
    )

    private val paint = Paint()
    private val rectF = RectF()
    private var colorSize = 0f
    private var selectedColorIndex = 0

    private var onColorSelectedListener: ((Int) -> Unit)? = null

    fun setOnColorSelectedListener(listener: (Int) -> Unit) {
        onColorSelectedListener = listener
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        colorSize = w.toFloat() / colors.size
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (i in colors.indices) {
            val left = i * colorSize
            val right = left + colorSize

            paint.color = colors[i]
            rectF.set(left, 0f, right, height.toFloat())
            canvas.drawRect(rectF, paint)

            if (i == selectedColorIndex) {
                paint.color = Color.WHITE
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 4f
                canvas.drawRect(rectF, paint)
                paint.style = Paint.Style.FILL
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
            val index = (event.x / colorSize).toInt().coerceIn(0, colors.size - 1)
            if (index != selectedColorIndex) {
                selectedColorIndex = index
                onColorSelectedListener?.invoke(colors[selectedColorIndex])
                invalidate()
            }
            return true
        }
        return super.onTouchEvent(event)
    }
}
