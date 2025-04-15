package com.example.leatherdesignbackend.models

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Path
import java.util.UUID

/**
 * Represents an element in the design canvas, which could be a drawing path or a component.
 */
data class DesignElement(
    // Common properties
    val id: String = UUID.randomUUID().toString(),
    val path: Path = Path(),
    val paint: Paint,
    val type: Type,
    var name: String = "Element",
    
    // Position properties (used mainly for components)
    var x: Float = 0f,
    var y: Float = 0f,
    
    // Component properties
    val bitmap: Bitmap? = null,
    val componentType: String = "",
    
    // Selection state
    var selected: Boolean = false
) {
    /**
     * Types of design elements
     */
    enum class Type {
        LINE,
        RECTANGLE,
        CIRCLE,
        FREEHAND,
        ERASER,
        COMPONENT,
        TEMPLATE
    }
    
    /**
     * Check if this element is a component
     */
    fun isComponent(): Boolean {
        return type == Type.COMPONENT
    }
    
    /**
     * Check if this element is a drawing path
     */
    fun isDrawing(): Boolean {
        return type == Type.LINE || type == Type.RECTANGLE || 
               type == Type.CIRCLE || type == Type.FREEHAND
    }
    
    /**
     * Check if this element is a template
     */
    fun isTemplate(): Boolean {
        return type == Type.TEMPLATE
    }
    
    /**
     * Move the element to a new position
     */
    fun moveTo(newX: Float, newY: Float) {
        val deltaX = newX - x
        val deltaY = newY - y
        
        x = newX
        y = newY
        
        // If it's a drawing element, translate the path
        if (isDrawing() || isTemplate()) {
            path.offset(deltaX, deltaY)
        }
    }
    
    /**
     * Duplicate this element
     */
    fun duplicate(): DesignElement {
        val newPath = Path(path)
        return DesignElement(
            path = newPath,
            paint = Paint(paint),
            type = type,
            name = "$name (copy)",
            x = x + 20f,  // Offset a bit so they don't overlap
            y = y + 20f,
            bitmap = bitmap,
            componentType = componentType
        )
    }
    
    /**
     * Toggle the selection state of this element
     */
    fun toggleSelection() {
        selected = !selected
    }
    
    /**
     * Get the width of this element
     */
    fun getWidth(): Float {
        return bitmap?.width?.toFloat() ?: 100f
    }
    
    /**
     * Get the height of this element
     */
    fun getHeight(): Float {
        return bitmap?.height?.toFloat() ?: 100f
    }
}