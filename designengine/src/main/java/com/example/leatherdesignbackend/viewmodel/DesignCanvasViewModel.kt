package com.example.leatherdesignbackend.viewmodel

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Path
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.leatherdesignbackend.data.ColorPalette
import com.example.leatherdesignbackend.data.DesignLayer
import com.example.leatherdesignbackend.data.DesignPath
import com.example.leatherdesignbackend.data.DesignProject

/**
 * ViewModel for the Design Canvas screen
 * Manages the state and operations of the design canvas
 */
class DesignCanvasViewModel : ViewModel() {
    
    // Current project being edited
    private val _currentProject = MutableLiveData<DesignProject>()
    val currentProject: LiveData<DesignProject> = _currentProject
    
    // Active layer being edited
    private val _activeLayer = MutableLiveData<DesignLayer?>()
    val activeLayer: LiveData<DesignLayer?> = _activeLayer
    
    // List of all layers in the project
    private val _layers = MutableLiveData<List<DesignLayer>>(emptyList())
    val layers: LiveData<List<DesignLayer>> = _layers
    
    // Selected path for editing
    private val _selectedPath = MutableLiveData<DesignPath?>()
    val selectedPath: LiveData<DesignPath?> = _selectedPath
    
    // Current color palette
    private val _currentPalette = MutableLiveData<ColorPalette>()
    val currentPalette: LiveData<ColorPalette> = _currentPalette
    
    // Current stroke color
    private val _strokeColor = MutableLiveData<Int>(Color.BLACK)
    val strokeColor: LiveData<Int> = _strokeColor
    
    // Current fill color
    private val _fillColor = MutableLiveData<Int>(Color.TRANSPARENT)
    val fillColor: LiveData<Int> = _fillColor
    
    // Current stroke width
    private val _strokeWidth = MutableLiveData<Float>(2f)
    val strokeWidth: LiveData<Float> = _strokeWidth
    
    // Canvas zoom level
    private val _zoomLevel = MutableLiveData<Float>(1f)
    val zoomLevel: LiveData<Float> = _zoomLevel
    
    // Canvas offset for panning
    private val _canvasOffsetX = MutableLiveData<Float>(0f)
    val canvasOffsetX: LiveData<Float> = _canvasOffsetX
    
    private val _canvasOffsetY = MutableLiveData<Float>(0f)
    val canvasOffsetY: LiveData<Float> = _canvasOffsetY
    
    // Grid visibility
    private val _gridVisible = MutableLiveData<Boolean>(true)
    val gridVisible: LiveData<Boolean> = _gridVisible
    
    // Undo/redo history
    private val undoStack = mutableListOf<DesignAction>()
    private val redoStack = mutableListOf<DesignAction>()
    
    // Undo availability
    private val _canUndo = MutableLiveData<Boolean>(false)
    val canUndo: LiveData<Boolean> = _canUndo
    
    // Redo availability
    private val _canRedo = MutableLiveData<Boolean>(false)
    val canRedo: LiveData<Boolean> = _canRedo
    
    /**
     * Set the current project
     */
    fun setProject(project: DesignProject) {
        _currentProject.value = project
        _layers.value = project.layers
        
        // Set active layer to the first layer or create one if none exists
        if (project.layers.isNotEmpty()) {
            _activeLayer.value = project.layers[0]
        } else {
            val newLayer = DesignLayer(name = "Layer 1")
            project.addLayer(newLayer)
            _layers.value = project.layers
            _activeLayer.value = newLayer
        }
        
        // Initialize with default color palette if none exists
        if (_currentPalette.value == null) {
            _currentPalette.value = ColorPalette.createDefaultLeatherPalette()
        }
    }
    
    /**
     * Add a new layer to the project
     */
    fun addLayer(name: String = "New Layer") {
        val project = _currentProject.value ?: return
        val newLayer = DesignLayer(name = name)
        project.addLayer(newLayer)
        _layers.value = project.layers
        _activeLayer.value = newLayer
    }
    
    /**
     * Remove a layer from the project
     */
    fun removeLayer(layer: DesignLayer) {
        val project = _currentProject.value ?: return
        val isActiveLayer = _activeLayer.value == layer
        
        if (project.removeLayer(layer)) {
            _layers.value = project.layers
            
            // If we removed the active layer, set a new active layer
            if (isActiveLayer && project.layers.isNotEmpty()) {
                _activeLayer.value = project.layers[0]
            } else if (project.layers.isEmpty()) {
                _activeLayer.value = null
            }
            
            // Add to undo stack
            undoStack.add(DesignAction.RemoveLayer(layer))
            _canUndo.value = true
            redoStack.clear()
            _canRedo.value = false
        }
    }
    
    /**
     * Set the active layer
     */
    fun setActiveLayer(layer: DesignLayer) {
        if (_currentProject.value?.layers?.contains(layer) == true) {
            _activeLayer.value = layer
        }
    }
    
    /**
     * Add a path to the active layer
     */
    fun addPath(path: Path, svgPathData: String) {
        val layer = _activeLayer.value ?: return
        
        val designPath = DesignPath(
            path = path,
            svgPathData = svgPathData,
            strokeColor = _strokeColor.value ?: Color.BLACK,
            fillColor = _fillColor.value ?: Color.TRANSPARENT,
            strokeWidth = _strokeWidth.value ?: 2f
        )
        
        layer.addPath(designPath)
        
        // Add to undo stack
        undoStack.add(DesignAction.AddPath(layer, designPath))
        _canUndo.value = true
        redoStack.clear()
        _canRedo.value = false
    }
    
    /**
     * Remove a path from its layer
     */
    fun removePath(path: DesignPath) {
        val project = _currentProject.value ?: return
        
        // Find which layer contains this path
        for (layer in project.layers) {
            if (layer.removePath(path)) {
                // Add to undo stack
                undoStack.add(DesignAction.RemovePath(layer, path))
                _canUndo.value = true
                redoStack.clear()
                _canRedo.value = false
                
                // Deselect the path if it was selected
                if (_selectedPath.value == path) {
                    _selectedPath.value = null
                }
                
                break
            }
        }
    }
    
    /**
     * Select a path for editing
     */
    fun selectPath(path: DesignPath?) {
        _selectedPath.value = path
    }
    
    /**
     * Set the stroke color
     */
    fun setStrokeColor(color: Int) {
        _strokeColor.value = color
        
        // Update selected path if any
        _selectedPath.value?.let { path ->
            path.strokeColor = color
        }
    }
    
    /**
     * Set the fill color
     */
    fun setFillColor(color: Int) {
        _fillColor.value = color
        
        // Update selected path if any
        _selectedPath.value?.let { path ->
            path.fillColor = color
        }
    }
    
    /**
     * Set the stroke width
     */
    fun setStrokeWidth(width: Float) {
        _strokeWidth.value = width
        
        // Update selected path if any
        _selectedPath.value?.let { path ->
            path.strokeWidth = width
        }
    }
    
    /**
     * Set the zoom level
     */
    fun setZoomLevel(zoom: Float) {
        _zoomLevel.value = zoom.coerceIn(0.1f, 5f) // Limit zoom range
    }
    
    /**
     * Set the canvas offset for panning
     */
    fun setCanvasOffset(offsetX: Float, offsetY: Float) {
        _canvasOffsetX.value = offsetX
        _canvasOffsetY.value = offsetY
    }
    
    /**
     * Toggle grid visibility
     */
    fun toggleGridVisibility() {
        _gridVisible.value = _gridVisible.value != true
    }
    
    /**
     * Undo the last action
     */
    fun undo() {
        if (undoStack.isEmpty()) return
        
        val action = undoStack.removeAt(undoStack.size - 1)
        redoStack.add(action)
        
        when (action) {
            is DesignAction.AddPath -> {
                action.layer.removePath(action.path)
            }
            is DesignAction.RemovePath -> {
                action.layer.addPath(action.path)
            }
            is DesignAction.AddLayer -> {
                _currentProject.value?.removeLayer(action.layer)
                _layers.value = _currentProject.value?.layers ?: emptyList()
                if (_activeLayer.value == action.layer) {
                    _activeLayer.value = _currentProject.value?.layers?.firstOrNull()
                }
            }
            is DesignAction.RemoveLayer -> {
                _currentProject.value?.addLayer(action.layer)
                _layers.value = _currentProject.value?.layers ?: emptyList()
            }
        }
        
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
    }
    
    /**
     * Redo the last undone action
     */
    fun redo() {
        if (redoStack.isEmpty()) return
        
        val action = redoStack.removeAt(redoStack.size - 1)
        undoStack.add(action)
        
        when (action) {
            is DesignAction.AddPath -> {
                action.layer.addPath(action.path)
            }
            is DesignAction.RemovePath -> {
                action.layer.removePath(action.path)
                if (_selectedPath.value == action.path) {
                    _selectedPath.value = null
                }
            }
            is DesignAction.AddLayer -> {
                _currentProject.value?.addLayer(action.layer)
                _layers.value = _currentProject.value?.layers ?: emptyList()
            }
            is DesignAction.RemoveLayer -> {
                _currentProject.value?.removeLayer(action.layer)
                _layers.value = _currentProject.value?.layers ?: emptyList()
                if (_activeLayer.value == action.layer) {
                    _activeLayer.value = _currentProject.value?.layers?.firstOrNull()
                }
            }
        }
        
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
    }
    
    /**
     * Clear the canvas by removing all paths from the active layer
     */
    fun clearActiveLayer() {
        val layer = _activeLayer.value ?: return
        
        // Store paths for undo
        val paths = layer.paths.toList()
        
        // Clear paths
        layer.clearPaths()
        
        // Add to undo stack
        for (path in paths) {
            undoStack.add(DesignAction.RemovePath(layer, path))
        }
        
        _canUndo.value = undoStack.isNotEmpty()
        redoStack.clear()
        _canRedo.value = false
    }
    
    /**
     * Generate a thumbnail for the current project
     */
    fun generateThumbnail(bitmap: Bitmap) {
        _currentProject.value?.let {
            it.generateThumbnail(bitmap)
        }
    }
    
    /**
     * Update design data in the current project
     * This is used when saving SVG data from the canvas
     */
    fun updateDesignData(svgData: String) {
        _currentProject.value?.let {
            it.designData = svgData
            it.updateLastModified()
        }
    }
    
    /**
     * Save the current project
     */
    fun saveProject() {
        _currentProject.value?.updateLastModified()
        // In a real app, this would save to a database or file
    }
    
    /**
     * Actions that can be undone/redone
     */
    sealed class DesignAction {
        data class AddPath(val layer: DesignLayer, val path: DesignPath) : DesignAction()
        data class RemovePath(val layer: DesignLayer, val path: DesignPath) : DesignAction()
        data class AddLayer(val layer: DesignLayer) : DesignAction()
        data class RemoveLayer(val layer: DesignLayer) : DesignAction()
    }
}