package com.example.leathercraftpro

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class DesignCanvasView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    enum class Tool {
        BRUSH, ERASER, LINE, RECTANGLE, CIRCLE
    }

    private var currentTool = Tool.BRUSH
    private var currentColor = Color.BLACK
    private var brushSize = 10f

    private val paint = Paint().apply {
        color = currentColor
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = brushSize
    }

    private val paths = mutableListOf<Pair<Path, Paint>>()
    private var currentPath = Path()

    private var startX = 0f
    private var startY = 0f

    init {
        // Add background leather texture color
        setBackgroundColor(Color.rgb(210, 180, 140))
    }

    fun setColor(color: Int) {
        currentColor = color
        paint.color = color
    }

    fun setTool(tool: Tool) {
        currentTool = tool
        when (tool) {
            Tool.ERASER -> {
                paint.color = Color.rgb(210, 180, 140) // Match background
            }
            else -> {
                paint.color = currentColor
            }
        }
    }

    fun clearCanvas() {
        paths.clear()
        currentPath = Path()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw all saved paths
        for ((path, pathPaint) in paths) {
            canvas.drawPath(path, pathPaint)
        }

        // Draw current path
        canvas.drawPath(currentPath, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = x
                startY = y
                currentPath = Path()

                when (currentTool) {
                    Tool.BRUSH, Tool.ERASER -> {
                        currentPath.moveTo(x, y)
                    }
                    else -> {
                        // For other tools, we'll draw on ACTION_UP
                        currentPath.moveTo(x, y)
                    }
                }
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                when (currentTool) {
                    Tool.BRUSH, Tool.ERASER -> {
                        currentPath.lineTo(x, y)
                    }
                    else -> {
                        // For other tools, we'll draw the preview
                        currentPath.reset()
                        when (currentTool) {
                            Tool.LINE -> {
                                currentPath.moveTo(startX, startY)
                                currentPath.lineTo(x, y)
                            }
                            Tool.RECTANGLE -> {
                                currentPath.addRect(
                                    startX, startY, x, y, Path.Direction.CW
                                )
                            }
                            Tool.CIRCLE -> {
                                val radius = Math.hypot(
                                    (x - startX).toDouble(),
                                    (y - startY).toDouble()
                                ).toFloat()
                                currentPath.addCircle(startX, startY, radius, Path.Direction.CW)
                            }
                            else -> {}
                        }
                    }
                }
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                when (currentTool) {
                    Tool.BRUSH, Tool.ERASER -> {
                        currentPath.lineTo(x, y)
                    }
                    Tool.LINE -> {
                        currentPath.reset()
                        currentPath.moveTo(startX, startY)
                        currentPath.lineTo(x, y)
                    }
                    Tool.RECTANGLE -> {
                        currentPath.reset()
                        currentPath.addRect(
                            startX, startY, x, y, Path.Direction.CW
                        )
                    }
                    Tool.CIRCLE -> {
                        currentPath.reset()
                        val radius = Math.hypot(
                            (x - startX).toDouble(),
                            (y - startY).toDouble()
                        ).toFloat()
                        currentPath.addCircle(startX, startY, radius, Path.Direction.CW)
                    }
                }

                // Save the current path with its paint
                val currentPaintCopy = Paint(paint)
                paths.add(Pair(currentPath, currentPaintCopy))

                // Reset current path
                currentPath = Path()
                invalidate()
                return true
            }
            else -> return false
        }
    }
}