package com.example.leatherdesignbackend.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.leatherdesignbackend.R
import com.example.leatherdesignbackend.data.ColorPalette
import com.example.leatherdesignbackend.ui.components.ColorPickerView

/**
 * Dialog for selecting colors in the design canvas
 * Provides a color picker interface with presets and recent colors
 */
class ColorPickerDialog(context: Context, initialColor: Int = Color.BLACK) : Dialog(context) {

    private lateinit var colorPickerView: ColorPickerView
    private lateinit var colorPreview: View
    private lateinit var hexValueText: TextView
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    
    // Current selected color
    private var selectedColor: Int = initialColor
    
    // Callback for when a color is selected
    private var onColorSelectedListener: ((Int) -> Unit)? = null
    
    // Current color palette
    private var colorPalette: ColorPalette? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_color_picker)
        
        // Set dialog title
        setTitle("Select Color")
        
        // Initialize views
        colorPickerView = findViewById(R.id.colorPickerView)
        colorPreview = findViewById(R.id.colorPreview)
        hexValueText = findViewById(R.id.hexValueText)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
        
        // Set initial color
        colorPickerView.setColor(selectedColor)
        updateColorPreview(selectedColor)
        
        // Set color change listener
        colorPickerView.setOnColorChangedListener { color ->
            selectedColor = color
            updateColorPreview(color)
        }
        
        // Set save button click listener
        btnSave.setOnClickListener {
            // Add color to palette if available
            colorPalette?.addColor(selectedColor)
            
            // Notify listener
            onColorSelectedListener?.invoke(selectedColor)
            dismiss()
        }
        
        // Set cancel button click listener
        btnCancel.setOnClickListener {
            dismiss()
        }
    }
    
    /**
     * Set the listener for color selection
     */
    fun setOnColorSelectedListener(listener: (Int) -> Unit) {
        onColorSelectedListener = listener
    }
    
    /**
     * Set the color palette to use for presets and to save selected colors
     */
    fun setColorPalette(palette: ColorPalette) {
        colorPalette = palette
        colorPickerView.setPresetColors(palette.colors)
    }
    
    /**
     * Update the color preview and hex value text
     */
    private fun updateColorPreview(color: Int) {
        colorPreview.setBackgroundColor(color)
        hexValueText.text = String.format("#%06X", (0xFFFFFF and color))
    }
}