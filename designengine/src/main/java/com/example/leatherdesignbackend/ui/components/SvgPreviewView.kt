package com.example.leatherdesignbackend.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.view.GestureDetectorCompat

/**
 * Custom view for previewing SVG paths before adding them to the design canvas
 * Supports zooming, panning, and path selection
 */
class SvgPreviewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // List of paths to display
    private val paths = mutableListOf<Path>()
    
    // Selected paths
    private val selectedPaths = mutableSetOf<Int>()
    
    // Paint objects
    private val pathPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    }
    
    private val selectedPathPaint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.STROKE
        strokeWidth = 3f
        isAntiAlias = true
    }
    
    private val backgroundPaint = Paint().apply {
        color = Color.WHITE
    }
    
    // Transformation matrix for zoom and pan
    private val matrix = Matrix()
    private val inverseMatrix = Matrix()
    
    // Bounds of all paths combined
    private val pathsBounds = RectF()
    
    // Gesture detectors for zoom and pan
    private val scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
    private val gestureDetector = GestureDetectorCompat(context, GestureListener())
    
    // Callback for when a path is selected
    private var onPathSelectedListener: ((Int) -> Unit)? = null
    
    // Current scale factor
    private var scaleFactor = 1f
    
    init {
        // Enable focus for accessibility
        isFocusable = true
    }
    
    /**
     * Set the paths to display
     */
    fun setPaths(newPaths: List<Path>) {
        paths.clear()
        paths.addAll(newPaths)
        selectedPaths.clear()
        calculatePathsBounds()
        resetTransformation()
        invalidate()
    }
    
    /**
     * Add a single path to the view
     */
    fun addPath(path: Path) {
        paths.add(path)
        calculatePathsBounds()
        invalidate()
    }
    
    /**
     * Clear all paths
     */
    fun clearPaths() {
        paths.clear()
        selectedPaths.clear()
        invalidate()
    }
    
    /**
     * Set a listener for path selection events
     */
    fun setOnPathSelectedListener(listener: (Int) -> Unit) {
        onPathSelectedListener = listener
    }
    
    /**
     * Get the list of selected path indices
     */
    fun getSelectedPaths(): Set<Int> {
        return selectedPaths.toSet()
    }
    
    /**
     * Calculate the combined bounds of all paths
     */
    private fun calculatePathsBounds() {
        if (paths.isEmpty()) {
            pathsBounds.set(0f, 0f, width.toFloat(), height.toFloat())
            return
        }
        
        pathsBounds.setEmpty()
        val tempBounds = RectF()
        
        for (path in paths) {
            path.computeBounds(tempBounds, true)
            pathsBounds.union(tempBounds)
        }
    }
    
    /**
     * Reset the transformation to fit all paths in the view
     */
    fun resetTransformation() {
        if (width == 0 || height == 0 || pathsBounds.isEmpty) return
        
        matrix.reset()
        
        // Calculate scale to fit the paths in the view with some padding
        val padding = 50f
        val scaleX = (width - 2 * padding) / pathsBounds.width()
        val scaleY = (height - 2 * padding) / pathsBounds.height()
        scaleFactor = Math.min(scaleX, scaleY)
        
        // Scale around the center of the paths
        matrix.postScale(scaleFactor, scaleFactor)
        
        // Center the paths in the view
        val scaledWidth = pathsBounds.width() * scaleFactor
        val scaledHeight = pathsBounds.height() * scaleFactor
        val translateX = (width - scaledWidth) / 2f - pathsBounds.left * scaleFactor
        val translateY = (height - scaledHeight) / 2f - pathsBounds.top * scaleFactor
        matrix.postTranslate(translateX, translateY)
        
        // Update inverse matrix for touch handling
        matrix.invert(inverseMatrix)
        
        invalidate()
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        resetTransformation()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw background
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
        
        // Save canvas state before applying transformations
        canvas.save()
        
        // Apply zoom and pan transformations
        canvas.concat(matrix)
        
        // Draw each path
        for (i in paths.indices) {
            val path = paths[i]
            val paint = if (i in selectedPaths) selectedPathPaint else pathPaint
            canvas.drawPath(path, paint)
        }
        
        // Restore canvas to original state
        canvas.restore()
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Let the gesture detectors handle the event
        scaleGestureDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        return true
    }
    
    /**
     * Handle path selection
     */
    private fun handlePathSelection(x: Float, y: Float) {
        // Convert screen coordinates to path coordinates
        val touchPoint = floatArrayOf(x, y)
        inverseMatrix.mapPoints(touchPoint)
        
        // Check if any path contains the touch point
        for (i in paths.indices.reversed()) { // Reverse to select top-most path first
            val path = paths[i]
            val bounds = RectF()
            path.computeBounds(bounds, true)
            
            // Expand bounds slightly to make selection easier
            bounds.inset(-10f, -10f)
            
            if (bounds.contains(touchPoint[0], touchPoint[1])) {
                // Toggle selection
                if (i in selectedPaths) {
                    selectedPaths.remove(i)
                } else {
                    selectedPaths.add(i)
                }
                
                // Notify listener
                onPathSelectedListener?.invoke(i)
                invalidate()
                return
            }
        }
        
        // If no path was touched, clear selection
        if (selectedPaths.isNotEmpty()) {
            selectedPaths.clear()
            invalidate()
        }
    }
    
    /**
     * Scale gesture listener for zoom functionality
     */
    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            // Calculate new scale factor
            scaleFactor *= detector.scaleFactor
            
            // Limit scale factor to reasonable bounds
            scaleFactor = scaleFactor.coerceIn(0.1f, 10.0f)
            
            // Scale around the focus point
            matrix.postScale(
                detector.scaleFactor,
                detector.scaleFactor,
                detector.focusX,
                detector.focusY
            )
            
            // Update inverse matrix for touch handling
            matrix.invert(inverseMatrix)
            
            invalidate()
            return true
        }
    }
    
    /**
     * Gesture listener for pan and tap functionality
     */
    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            // Pan the view
            matrix.postTranslate(-distanceX, -distanceY)
            
            // Update inverse matrix for touch handling
            matrix.invert(inverseMatrix)
            
            invalidate()
            return true
        }
        
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            // Handle path selection on tap
            handlePathSelection(e.x, e.y)
            return true
        }
        
        override fun onDoubleTap(e: MotionEvent): Boolean {
            // Reset transformation on double tap
            resetTransformation()
            return true
        }
    }
}