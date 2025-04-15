package com.example.leatherdesignbackend.ui.viewmodels

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
 * Delegates to the implementation in viewmodel package
 */
class DesignCanvasViewModel : ViewModel() {
    
    private val delegate = com.example.leatherdesignbackend.viewmodel.DesignCanvasViewModel()
    
    // Current project being edited
    val currentProject: LiveData<DesignProject> get() = delegate.currentProject
    
    // Active layer being edited
    val activeLayer: LiveData<DesignLayer?> get() = delegate.activeLayer
    
    // List of all layers in the project
    val layers: LiveData<List<DesignLayer>> get() = delegate.layers
    
    // Selected path for editing
    val selectedPath: LiveData<DesignPath?> get() = delegate.selectedPath
    
    // Current color palette
    val currentPalette: LiveData<ColorPalette> get() = delegate.currentPalette
    
    // Current stroke color
    val strokeColor: LiveData<Int> get() = delegate.strokeColor
    
    // Current fill color
    val fillColor: LiveData<Int> get() = delegate.fillColor
    
    // Current stroke width
    val strokeWidth: LiveData<Float> get() = delegate.strokeWidth
    
    // Canvas zoom level
    val zoomLevel: LiveData<Float> get() = delegate.zoomLevel
    
    // Canvas offset for panning
    val canvasOffsetX: LiveData<Float> get() = delegate.canvasOffsetX
    val canvasOffsetY: LiveData<Float> get() = delegate.canvasOffsetY
    
    // Grid visibility
    val gridVisible: LiveData<Boolean> get() = delegate.gridVisible
    
    // Undo availability
    val canUndo: LiveData<Boolean> get() = delegate.canUndo
    
    // Redo availability
    val canRedo: LiveData<Boolean> get() = delegate.canRedo
    
    /**
     * Set the current project
     */
    fun setProject(project: DesignProject) {
        delegate.setProject(project)
    }
    
    /**
     * Add a new layer to the project
     */
    fun addLayer(name: String = "New Layer") {
        delegate.addLayer(name)
    }
    
    /**
     * Remove a layer from the project
     */
    fun removeLayer(layer: DesignLayer) {
        delegate.removeLayer(layer)
    }
    
    /**
     * Set the active layer
     */
    fun setActiveLayer(layer: DesignLayer) {
        delegate.setActiveLayer(layer)
    }
    
    /**
     * Add a path to the active layer
     */
    fun addPath(path: Path, svgPathData: String) {
        delegate.addPath(path, svgPathData)
    }
    
    /**
     * Remove a path from its layer
     */
    fun removePath(path: DesignPath) {
        delegate.removePath(path)
    }
    
    /**
     * Select a path for editing
     */
    fun selectPath(path: DesignPath?) {
        delegate.selectPath(path)
    }
} 