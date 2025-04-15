package com.example.leatherdesignbackend.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.leatherdesignbackend.models.ComponentItem
import com.example.leatherdesignbackend.models.DesignElement
import com.example.leatherdesignbackend.utils.SvgParser
import java.io.ByteArrayOutputStream
import java.util.Stack

class CanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Define enums for modes and tools
    enum class Mode {
        DRAWING,
        COMPONENT_PLACEMENT
    }

    enum class Tool {
        LINE,
        RECTANGLE,
        CIRCLE,
        FREEHAND,
        ERASER
    }

    // Current state
    var currentMode = Mode.DRAWING
    var currentTool = Tool.FREEHAND

    // Drawing attributes
    private var strokeWidth = 5f
    private var strokeColor = Color.BLACK

    // Touch tracking
    private var startX = 0f
    private var startY = 0f
    private var lastX = 0f
    private var lastY = 0f

    // Canvas elements
    private val pathList = mutableListOf<DesignElement>()
    private val componentsList = mutableListOf<DesignElement>()

    // Current path being drawn
    private var currentPath = Path()
    private var currentPaint = Paint().apply {
        color = strokeColor
        strokeWidth = this@CanvasView.strokeWidth
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    // Selected component for moving
    private var selectedComponent: DesignElement? = null

    // Undo/Redo stacks
    private val undoStack = Stack<Action>()
    private val redoStack = Stack<Action>()

    // Action types for undo/redo
    sealed class Action {
        data class AddPath(val element: DesignElement) : Action()
        data class AddComponent(val element: DesignElement) : Action()
        data class MoveComponent(
            val element: DesignElement,
            val oldX: Float,
            val oldY: Float,
            val newX: Float,
            val newY: Float
        ) : Action()
        data class RemovePath(val element: DesignElement) : Action()
    }

    init {
        // Set default mode to drawing
        currentMode = Mode.DRAWING
        currentTool = Tool.FREEHAND
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw background (grid or template)
        drawBackground(canvas)

        // Draw all paths
        for (element in pathList) {
            canvas.drawPath(element.path, element.paint)
        }

        // Draw current path being created
        canvas.drawPath(currentPath, currentPaint)

        // Draw all components
        for (component in componentsList) {
            // Draw component
            component.bitmap?.let { bitmap ->
                canvas.drawBitmap(bitmap, component.x, component.y, Paint())

                // Draw selection border if this component is selected
                if (component == selectedComponent) {
                    val borderPaint = Paint().apply {
                        color = Color.BLUE
                        style = Paint.Style.STROKE
                        strokeWidth = 3f
                    }

                    val rect = RectF(
                        component.x,
                        component.y,
                        component.x + bitmap.width,
                        component.y + bitmap.height
                    )

                    canvas.drawRect(rect, borderPaint)
                }
            }
        }
    }

    private fun drawBackground(canvas: Canvas) {
        // Draw grid or pattern background
        val gridPaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        // Draw grid lines at fixed intervals
        val gridSize = 50
        val width = width
        val height = height

        // Draw vertical lines
        for (i in 0..width step gridSize) {
            canvas.drawLine(i.toFloat(), 0f, i.toFloat(), height.toFloat(), gridPaint)
        }

        // Draw horizontal lines
        for (i in 0..height step gridSize) {
            canvas.drawLine(0f, i.toFloat(), width.toFloat(), i.toFloat(), gridPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (currentMode) {
            Mode.DRAWING -> handleDrawingTouch(event, x, y)
            Mode.COMPONENT_PLACEMENT -> handleComponentTouch(event, x, y)
        }

        // Invalidate to redraw
        invalidate()
        return true
    }

    private fun handleDrawingTouch(event: MotionEvent, x: Float, y: Float) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Start a new path
                startX = x
                startY = y
                lastX = x
                lastY = y

                currentPath = Path()

                when (currentTool) {
                    Tool.FREEHAND, Tool.ERASER -> {
                        currentPath.moveTo(x, y)
                    }
                    else -> {
                        // For geometric shapes, we'll wait for ACTION_UP to create the shape
                    }
                }

                // Update paint based on selected tool
                currentPaint = Paint(currentPaint).apply {
                    color = if (currentTool == Tool.ERASER) Color.WHITE else strokeColor
                    strokeWidth = this@CanvasView.strokeWidth
                }
            }

            MotionEvent.ACTION_MOVE -> {
                when (currentTool) {
                    Tool.FREEHAND, Tool.ERASER -> {
                        // Add line to the path
                        currentPath.quadTo(
                            lastX, lastY,
                            (lastX + x) / 2, (lastY + y) / 2
                        )

                        lastX = x
                        lastY = y
                    }
                    else -> {
                        // For other tools, we'll preview the shape
                        currentPath = createShapePath(startX, startY, x, y)
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                when (currentTool) {
                    Tool.LINE, Tool.RECTANGLE, Tool.CIRCLE -> {
                        // Create final shape
                        currentPath = createShapePath(startX, startY, x, y)
                    }
                    else -> {
                        // For freehand and eraser, path is already created
                    }
                }

                // Save the path
                val element = DesignElement(
                    path = Path(currentPath),
                    paint = Paint(currentPaint),
                    type = when (currentTool) {
                        Tool.LINE -> DesignElement.Type.LINE
                        Tool.RECTANGLE -> DesignElement.Type.RECTANGLE
                        Tool.CIRCLE -> DesignElement.Type.CIRCLE
                        Tool.FREEHAND -> DesignElement.Type.FREEHAND
                        Tool.ERASER -> DesignElement.Type.ERASER
                    }
                )

                pathList.add(element)
                undoStack.push(Action.AddPath(element))
                redoStack.clear()

                // Reset current path
                currentPath = Path()
            }
        }
    }

    private fun createShapePath(startX: Float, startY: Float, endX: Float, endY: Float): Path {
        val path = Path()

        when (currentTool) {
            Tool.LINE -> {
                path.moveTo(startX, startY)
                path.lineTo(endX, endY)
            }
            Tool.RECTANGLE -> {
                path.moveTo(startX, startY)
                path.lineTo(endX, startY)
                path.lineTo(endX, endY)
                path.lineTo(startX, endY)
                path.close()
            }
            Tool.CIRCLE -> {
                val radius = Math.hypot(
                    (endX - startX).toDouble(),
                    (endY - startY).toDouble()
                ).toFloat()

                path.addCircle(startX, startY, radius, Path.Direction.CW)
            }
            else -> {
                // Not applicable for other tools
            }
        }

        return path
    }

    private fun handleComponentTouch(event: MotionEvent, x: Float, y: Float) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Check if touch is on an existing component
                selectedComponent = findComponentAt(x, y)

                if (selectedComponent != null) {
                    // Remember the offset from component origin to touch point
                    startX = x - selectedComponent!!.x
                    startY = y - selectedComponent!!.y
                }
            }

            MotionEvent.ACTION_MOVE -> {
                // Move the selected component if any
                selectedComponent?.let { component ->
                    val oldX = component.x
                    val oldY = component.y

                    // Calculate new position
                    component.x = x - startX
                    component.y = y - startY

                    // Record move for undo
                    if (event.historySize > 0) {
                        val action = Action.MoveComponent(
                            component,
                            oldX, oldY,
                            component.x, component.y
                        )
                        undoStack.push(action)
                        redoStack.clear()
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                // Component placement is complete
                selectedComponent = null
            }
        }
    }

    private fun findComponentAt(x: Float, y: Float): DesignElement? {
        // Iterate backwards to check top-most components first
        for (i in componentsList.size - 1 downTo 0) {
            val component = componentsList[i]
            component.bitmap?.let { bitmap ->
                val right = component.x + bitmap.width
                val bottom = component.y + bitmap.height

                if (x >= component.x && x <= right && y >= component.y && y <= bottom) {
                    return component
                }
            }
        }

        return null
    }

    /**
     * Add a component to the canvas
     */
    fun addComponent(component: ComponentItem) {
        // Load the component bitmap
        val bitmap = BitmapFactory.decodeResource(resources, component.imageRes)

        // Create design element for component
        val element = DesignElement(
            type = DesignElement.Type.COMPONENT,
            x = (width / 2 - bitmap.width / 2).toFloat(),
            y = (height / 2 - bitmap.height / 2).toFloat(),
            bitmap = bitmap,
            name = component.name,
            paint = Paint() // Add the required paint parameter
        )

        componentsList.add(element)
        undoStack.push(Action.AddComponent(element))
        redoStack.clear()

        invalidate()
    }

    /**
     * Import an SVG template from a URI
     */
    fun importSvgTemplate(uri: Uri) {
        // Implement SVG parsing and display
        val svgParser = SvgParser(context)
        val paths = svgParser.parseSvgFromUri(uri)

        for (path in paths) {
            val element = DesignElement(
                path = path,
                paint = Paint(currentPaint).apply {
                    color = Color.BLACK
                    style = Paint.Style.STROKE
                },
                type = DesignElement.Type.TEMPLATE
            )

            pathList.add(element)
            undoStack.push(Action.AddPath(element))
        }

        invalidate()
    }

    /**
     * Import a DXF template from a URI
     */
    fun importDxfTemplate(uri: Uri) {
        // Implementation for DXF import would go here
        // This is a more complex task that would require a DXF parser library
        // For now, we'll show a placeholder implementation

        // Create a placeholder rectangular template
        val element = DesignElement(
            path = Path().apply {
                addRect(
                    RectF(100f, 100f, width - 100f, height - 100f),
                    Path.Direction.CW
                )
            },
            paint = Paint().apply {
                color = Color.GRAY
                style = Paint.Style.STROKE
                strokeWidth = 2f
                pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
            },
            type = DesignElement.Type.TEMPLATE
        )

        pathList.add(element)
        undoStack.push(Action.AddPath(element))

        invalidate()
    }

    /**
     * Import from SVG string data
     */
    fun importFromSvg(svgData: String) {
        // Use the SvgParser to create paths from SVG content
        val svgParser = SvgParser(context)
        val paths = svgParser.parseSvgFromString(svgData)

        for (path in paths) {
            val element = DesignElement(
                path = path,
                paint = Paint(currentPaint).apply {
                    color = Color.BLACK
                    style = Paint.Style.STROKE
                },
                type = DesignElement.Type.TEMPLATE
            )

            pathList.add(element)
        }

        invalidate()
    }

    /**
     * Export the canvas design to SVG format
     */
    fun exportToSvg(): String {
        // Create SVG content
        val svgBuilder = StringBuilder()
        svgBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>")
        svgBuilder.append("<svg xmlns=\"http://www.w3.org/2000/svg\" ")
        svgBuilder.append("width=\"${width}\" height=\"${height}\" ")
        svgBuilder.append("viewBox=\"0 0 $width $height\">")

        // Add paths
        for (element in pathList) {
            svgBuilder.append(convertPathToSvg(element))
        }

        // Add components (as rectangles or images for SVG)
        for (component in componentsList) {
            component.bitmap?.let { bitmap ->
                svgBuilder.append("<rect ")
                svgBuilder.append("x=\"${component.x}\" y=\"${component.y}\" ")
                svgBuilder.append("width=\"${bitmap.width}\" height=\"${bitmap.height}\" ")
                svgBuilder.append("fill=\"none\" stroke=\"black\" ")
                svgBuilder.append("stroke-width=\"1\" />")

                // Add component name as text
                svgBuilder.append("<text ")
                svgBuilder.append("x=\"${component.x + bitmap.width/2}\" ")
                svgBuilder.append("y=\"${component.y + bitmap.height/2}\" ")
                svgBuilder.append("text-anchor=\"middle\" ")
                svgBuilder.append("font-size=\"12\" fill=\"black\">")
                svgBuilder.append(component.name)
                svgBuilder.append("</text>")
            }
        }

        svgBuilder.append("</svg>")

        return svgBuilder.toString()
    }

    private fun convertPathToSvg(element: DesignElement): String {
        val path = element.path
        val paint = element.paint

        // Convert Android Path to SVG path string
        val pathMeasure = PathMeasure(path, false)
        val pathData = StringBuilder()

        // Path extraction coordinates
        val coordinates = FloatArray(2)

        // Get first point
        pathMeasure.getPosTan(0f, coordinates, null)
        pathData.append("M ${coordinates[0]} ${coordinates[1]} ")

        // Sample points along the path
        val length = pathMeasure.length
        val step = length / 50 // Sample 50 points

        for (i in 1..50) {
            val distance = i * step
            if (distance < length) {
                pathMeasure.getPosTan(distance, coordinates, null)
                pathData.append("L ${coordinates[0]} ${coordinates[1]} ")
            }
        }

        // Convert color to SVG format
        val colorString = String.format("#%06X", (0xFFFFFF and paint.color))

        // Build SVG path element
        val svgPath = StringBuilder()
        svgPath.append("<path ")
        svgPath.append("d=\"$pathData\" ")
        svgPath.append("stroke=\"$colorString\" ")
        svgPath.append("stroke-width=\"${paint.strokeWidth}\" ")
        svgPath.append("fill=\"none\" ")

        // Add path effects if any
        if (paint.pathEffect != null) {
            svgPath.append("stroke-dasharray=\"5,5\" ")
        }

        // Add stroke properties
        when (paint.strokeJoin) {
            Paint.Join.MITER -> svgPath.append("stroke-linejoin=\"miter\" ")
            Paint.Join.ROUND -> svgPath.append("stroke-linejoin=\"round\" ")
            Paint.Join.BEVEL -> svgPath.append("stroke-linejoin=\"bevel\" ")
        }

        when (paint.strokeCap) {
            Paint.Cap.BUTT -> svgPath.append("stroke-linecap=\"butt\" ")
            Paint.Cap.ROUND -> svgPath.append("stroke-linecap=\"round\" ")
            Paint.Cap.SQUARE -> svgPath.append("stroke-linecap=\"square\" ")
        }

        svgPath.append("/>")

        return svgPath.toString()
    }

    /**
     * Export the canvas design to PDF format
     */
    fun exportToPdf(): ByteArray {
        // Create a PDF document using PdfDocument API
        val document = PdfDocument()

        // Create a page of the same size as our canvas
        val pageInfo = PdfDocument.PageInfo.Builder(width, height, 1).create()
        val page = document.startPage(pageInfo)

        // Draw everything onto the page's canvas
        val canvas = page.canvas
        drawBackground(canvas)

        // Draw all paths
        for (element in pathList) {
            canvas.drawPath(element.path, element.paint)
        }

        // Draw all components
        for (component in componentsList) {
            component.bitmap?.let { bitmap ->
                canvas.drawBitmap(bitmap, component.x, component.y, Paint())
            }
        }

        // Finish the page
        document.finishPage(page)

        // Write the PDF content to a byte array
        val outputStream = ByteArrayOutputStream()
        document.writeTo(outputStream)
        document.close()

        return outputStream.toByteArray()
    }

    /**
     * Export the canvas design to a bitmap
     */
    fun exportToBitmap(): Bitmap {
        // Create a bitmap with the same dimensions as the canvas
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw white background
        canvas.drawColor(Color.WHITE)

        // Draw grid
        drawBackground(canvas)

        // Draw all paths
        for (element in pathList) {
            canvas.drawPath(element.path, element.paint)
        }

        // Draw all components
        for (component in componentsList) {
            component.bitmap?.let { componentBitmap ->
                canvas.drawBitmap(componentBitmap, component.x, component.y, Paint())
            }
        }

        return bitmap
    }

    /**
     * Clear the canvas
     */
    fun clearCanvas() {
        // Clear all paths and components
        for (element in pathList) {
            undoStack.push(Action.RemovePath(element))
        }

        pathList.clear()
        componentsList.clear()
        currentPath = Path()
        redoStack.clear()

        invalidate()
    }

    /**
     * Undo the last action
     */
    fun undo() {
        if (undoStack.isNotEmpty()) {
            val action = undoStack.pop()

            when (action) {
                is Action.AddPath -> {
                    // Remove the last added path
                    pathList.remove(action.element)
                    redoStack.push(action)
                }
                is Action.AddComponent -> {
                    // Remove the last added component
                    componentsList.remove(action.element)
                    redoStack.push(action)
                }
                is Action.MoveComponent -> {
                    // Restore component to previous position
                    action.element.x = action.oldX
                    action.element.y = action.oldY

                    // Create reverse action for redo
                    redoStack.push(
                        Action.MoveComponent(
                            action.element,
                            action.newX, action.newY,
                            action.oldX, action.oldY
                        )
                    )
                }
                is Action.RemovePath -> {
                    // Add back the removed path
                    pathList.add(action.element)
                    redoStack.push(Action.AddPath(action.element))
                }
            }

            invalidate()
        }
    }

    /**
     * Redo the last undone action
     */
    fun redo() {
        if (redoStack.isNotEmpty()) {
            val action = redoStack.pop()

            when (action) {
                is Action.AddPath -> {
                    // Re-add the path
                    pathList.add(action.element)
                    undoStack.push(action)
                }
                is Action.AddComponent -> {
                    // Re-add the component
                    componentsList.add(action.element)
                    undoStack.push(action)
                }
                is Action.MoveComponent -> {
                    // Move component to the position
                    action.element.x = action.newX
                    action.element.y = action.newY

                    // Create reverse action for undo
                    undoStack.push(
                        Action.MoveComponent(
                            action.element,
                            action.oldX, action.oldY,
                            action.newX, action.newY
                        )
                    )
                }
                is Action.RemovePath -> {
                    // Remove the path again
                    pathList.remove(action.element)
                    undoStack.push(Action.RemovePath(action.element))
                }
            }

            invalidate()
        }
    }

    /**
     * Set the stroke width
     */
    fun setStrokeWidth(width: Float) {
        strokeWidth = width
        currentPaint.strokeWidth = width
    }

    /**
     * Set the stroke color
     */
    fun setStrokeColor(color: Int) {
        strokeColor = color
        if (currentTool != Tool.ERASER) {
            currentPaint.color = color
        }
    }
}