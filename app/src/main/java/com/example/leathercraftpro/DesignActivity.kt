package com.example.leathercraftpro

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class DesignActivity : AppCompatActivity() {

    private lateinit var designCanvas: DesignCanvasView
    private lateinit var saveButton: Button
    private lateinit var clearButton: Button
    private lateinit var colorPicker: ColorPickerView
    private lateinit var toolSelector: ToolSelectorView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_design)

        // Enable the back button in the action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle("Leather Design Canvas")

        // Initialize UI components
        designCanvas = findViewById(R.id.design_canvas)
        saveButton = findViewById(R.id.save_button)
        clearButton = findViewById(R.id.clear_button)
        colorPicker = findViewById(R.id.color_picker)
        toolSelector = findViewById(R.id.tool_selector)

        // Set up color picker listener
        colorPicker.setOnColorSelectedListener { color ->
            designCanvas.setColor(color)
        }

        // Set up tool selector listener
        toolSelector.setOnToolSelectedListener { tool ->
            when (tool) {
                ToolSelectorView.Tool.BRUSH -> designCanvas.setTool(DesignCanvasView.Tool.BRUSH)
                ToolSelectorView.Tool.ERASER -> designCanvas.setTool(DesignCanvasView.Tool.ERASER)
                ToolSelectorView.Tool.LINE -> designCanvas.setTool(DesignCanvasView.Tool.LINE)
                ToolSelectorView.Tool.RECTANGLE -> designCanvas.setTool(DesignCanvasView.Tool.RECTANGLE)
                ToolSelectorView.Tool.CIRCLE -> designCanvas.setTool(DesignCanvasView.Tool.CIRCLE)
            }
        }

        // Set up save button listener
        saveButton.setOnClickListener {
            saveDesign()
        }

        // Set up clear button listener
        clearButton.setOnClickListener {
            designCanvas.clearCanvas()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveDesign() {
        // Implementation for saving the design
        // Could save to internal storage or a cloud service
        Toast.makeText(this, "Design saved successfully", Toast.LENGTH_SHORT).show()
    }
}