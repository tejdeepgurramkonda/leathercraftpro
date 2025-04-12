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
 * A custom view that displays tool options for the design canvas
 */
class ToolSelectorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    enum class Tool {
        BRUSH, ERASER, LINE, RECTANGLE, CIRCLE
    }

    private val tools = listOf(Tool.BRUSH, Tool.ERASER, Tool.LINE, Tool.RECTANGLE, Tool.CIRCLE)
    private val toolNames = listOf("Brush", "Eraser", "Line", "Rectangle", "Circle")

    private val paint = Paint().apply {
        isAntiAlias = true
        textSize = 30f
        textAlign = Paint.Align.CENTER
    }
    private val rectF = RectF()
    private var toolWidth = 0f
    private var selectedToolIndex = 0

    private var onToolSelectedListener: ((Tool) -> Unit)? = null

    fun setOnToolSelectedListener(listener: (Tool) -> Unit) {
        onToolSelectedListener = listener
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        toolWidth = w.toFloat() / tools.size
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (i in tools.indices) {
            val left = i * toolWidth
            val right = left + toolWidth

            paint.color = if (i == selectedToolIndex) Color.LTGRAY else Color.WHITE
            rectF.set(left, 0f, right, height.toFloat())
            canvas.drawRect(rectF, paint)

            paint.color = Color.GRAY
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRect(rectF, paint)
            paint.style = Paint.Style.FILL

            paint.color = Color.BLACK
            val centerX = left + toolWidth / 2
            val centerY = height / 2f + paint.textSize / 3
            canvas.drawText(toolNames[i], centerX, centerY, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val index = (event.x / toolWidth).toInt().coerceIn(0, tools.size - 1)
            if (index != selectedToolIndex) {
                selectedToolIndex = index
                onToolSelectedListener?.invoke(tools[selectedToolIndex])
                invalidate()
            }
            return true
        }
        return super.onTouchEvent(event)
    }
}
