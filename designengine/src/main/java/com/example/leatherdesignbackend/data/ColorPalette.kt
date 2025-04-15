package com.example.leatherdesignbackend.data

import android.graphics.Color
import java.util.UUID

/**
 * Data model representing a color palette for leather designs
 * Contains a collection of colors and palette metadata
 */
class ColorPalette(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var isDefault: Boolean = false,
    var colors: MutableList<Int> = mutableListOf()
) {
    /**
     * Add a color to the palette
     */
    fun addColor(color: Int) {
        if (!colors.contains(color)) {
            colors.add(color)
        }
    }
    
    /**
     * Remove a color from the palette
     */
    fun removeColor(color: Int): Boolean {
        return colors.remove(color)
    }
    
    /**
     * Get the color at the specified position
     */
    fun getColorAt(position: Int): Int? {
        return if (position >= 0 && position < colors.size) {
            colors[position]
        } else {
            null
        }
    }
    
    /**
     * Clear all colors from the palette
     */
    fun clearColors() {
        colors.clear()
    }
    
    /**
     * Get the number of colors in the palette
     */
    fun getColorCount(): Int {
        return colors.size
    }
    
    /**
     * Create a duplicate of this palette with a new ID
     */
    fun duplicate(): ColorPalette {
        return ColorPalette(
            name = "$name (copy)",
            isDefault = false,
            colors = colors.toMutableList()
        )
    }
    
    companion object {
        /**
         * Create a default leather color palette
         */
        fun createDefaultLeatherPalette(): ColorPalette {
            return ColorPalette(
                name = "Leather Classics",
                isDefault = true,
                colors = mutableListOf(
                    Color.parseColor("#8B4513"), // Saddle Brown
                    Color.parseColor("#A0522D"), // Sienna
                    Color.parseColor("#D2691E"), // Chocolate
                    Color.parseColor("#CD853F"), // Peru
                    Color.parseColor("#DEB887"), // Burlywood
                    Color.parseColor("#F5DEB3"), // Wheat
                    Color.parseColor("#3C280D"), // Dark Brown
                    Color.parseColor("#000000"), // Black
                    Color.parseColor("#8B0000"), // Dark Red
                    Color.parseColor("#191970")  // Midnight Blue
                )
            )
        }
    }
}