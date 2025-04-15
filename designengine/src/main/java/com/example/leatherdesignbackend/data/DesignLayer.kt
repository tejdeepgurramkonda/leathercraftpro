package com.example.leatherdesignbackend.data

import java.util.UUID

/**
 * Data model representing a layer in a design project
 * Contains layer properties and a list of paths
 */
class DesignLayer(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var visible: Boolean = true,
    var locked: Boolean = false,
    var position: Int = 0,
    var paths: MutableList<DesignPath> = mutableListOf()
) {
    /**
     * Add a path to the layer
     */
    fun addPath(path: DesignPath) {
        paths.add(path)
    }
    
    /**
     * Remove a path from the layer
     */
    fun removePath(path: DesignPath): Boolean {
        return paths.remove(path)
    }
    
    /**
     * Get the path at the specified position
     */
    fun getPathAt(position: Int): DesignPath? {
        return if (position >= 0 && position < paths.size) {
            paths[position]
        } else {
            null
        }
    }
    
    /**
     * Toggle the visibility of the layer
     */
    fun toggleVisibility() {
        visible = !visible
    }
    
    /**
     * Toggle the locked state of the layer
     */
    fun toggleLock() {
        locked = !locked
    }
    
    /**
     * Clear all paths from the layer
     */
    fun clearPaths() {
        paths.clear()
    }
    
    /**
     * Duplicate the layer with a new ID
     */
    fun duplicate(): DesignLayer {
        val duplicatedPaths = paths.map { it.duplicate() }.toMutableList()
        return DesignLayer(
            name = "$name (copy)",
            visible = visible,
            locked = locked,
            position = position,
            paths = duplicatedPaths
        )
    }
    
    /**
     * Check if the layer contains any paths
     */
    fun isEmpty(): Boolean {
        return paths.isEmpty()
    }
    
    /**
     * Get the number of paths in the layer
     */
    fun getPathCount(): Int {
        return paths.size
    }
}