package com.example.leatherdesignbackend.data

import android.graphics.Color
import android.graphics.Path
import java.util.UUID

/**
 * Data model representing a path in a design layer
 * Contains path data and styling properties
 */
class DesignPath(
    val id: String = UUID.randomUUID().toString(),
    var path: Path = Path(),
    var svgPathData: String = "",
    var fillColor: Int = Color.TRANSPARENT,
    var strokeColor: Int = Color.BLACK,
    var strokeWidth: Float = 2f,
    var selected: Boolean = false,
    var name: String = "Path"
) {
    /**
     * Update the SVG path data and convert it to an Android Path
     */
    fun updateSvgPath(svgData: String) {
        this.svgPathData = svgData
        // In a real implementation, this would parse the SVG path data
        // and convert it to an Android Path object
    }
    
    /**
     * Toggle the selection state of the path
     */
    fun toggleSelection() {
        selected = !selected
    }
    
    /**
     * Duplicate the path with a new ID
     */
    fun duplicate(): DesignPath {
        val newPath = Path(path)
        return DesignPath(
            path = newPath,
            svgPathData = svgPathData,
            fillColor = fillColor,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth,
            name = "$name (copy)"
        )
    }
    
    /**
     * Check if the path is empty
     */
    fun isEmpty(): Boolean {
        return path.isEmpty
    }
    
    /**
     * Convert the path to SVG path data
     */
    fun toSvgPathData(): String {
        // In a real implementation, this would convert the Android Path
        // to SVG path data format
        return svgPathData
    }
}