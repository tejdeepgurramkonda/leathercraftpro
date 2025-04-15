package com.example.leatherdesignbackend.ui.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.leatherdesignbackend.R
import com.example.leatherdesignbackend.adapter.ComponentAdapter
import com.example.leatherdesignbackend.data.DesignLayer
import com.example.leatherdesignbackend.data.DesignProject
import com.example.leatherdesignbackend.databinding.ActivityDesignCanvasBinding
import com.example.leatherdesignbackend.models.ComponentItem
import com.example.leatherdesignbackend.ui.dialogs.ColorPickerDialog
import com.example.leatherdesignbackend.ui.dialogs.LayerManagerDialog
import com.example.leatherdesignbackend.utils.ProjectRepository
import com.example.leatherdesignbackend.utils.SvgParser
import com.example.leatherdesignbackend.viewmodel.DesignCanvasViewModel
import com.example.leatherdesignbackend.views.CanvasView
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.Date

/**
 * Activity for the design canvas screen
 * Provides interface for creating and editing leather designs
 */
class DesignCanvasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDesignCanvasBinding
    private lateinit var canvasView: CanvasView
    private lateinit var viewModel: DesignCanvasViewModel
    private lateinit var projectRepository: ProjectRepository
    
    // File picker for importing templates
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { processImportedFile(it) }
    }

    // List of available components to drag onto canvas
    private val componentItems = listOf(
        ComponentItem("Strap", R.drawable.ic_strap),
        ComponentItem("Buckle", R.drawable.ic_buckle),
        ComponentItem("Flap", R.drawable.ic_flap),
        ComponentItem("Pocket", R.drawable.ic_pocket),
        ComponentItem("Corner", R.drawable.ic_corner)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDesignCanvasBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())
        setSupportActionBar(binding.toolbar)
        
        // Initialize view model
        viewModel = ViewModelProvider(this)[DesignCanvasViewModel::class.java]
        
        // Initialize project repository
        projectRepository = ProjectRepository(this)

        // Get project ID from intent if editing existing project
        val projectId = intent.getStringExtra("PROJECT_ID")
        if (projectId != null) {
            loadProject(projectId)
        } else {
            // Create a new design project
            val newProject = DesignProject(
                name = intent.getStringExtra("PROJECT_NAME") ?: "Untitled Design",
                type = intent.getStringExtra("PROJECT_TYPE") ?: "Custom",
                creationDate = Date()
            )
            viewModel.setProject(newProject)
        }

        setupViews()
        setupObservers()
        setupComponentPalette()
        setupDrawingTools()
    }

    private fun setupViews() {
        canvasView = binding.canvasView

        // Setup toolbar title with project name
        viewModel.currentProject.observe(this) { project ->
            title = project.name
        }

        // Setup save button
        binding.btnSave.setOnClickListener {
            saveDesign()
        }

        // Setup template upload
        binding.btnImportTemplate.setOnClickListener {
            openFilePicker()
        }

        // Setup tools visibility toggle
        binding.toggleTools.setOnClickListener {
            binding.toolsPanel.isVisible = !binding.toolsPanel.isVisible
        }

        // Setup mode switching (draw vs. component placement)
        binding.toggleMode.setOnClickListener {
            if (canvasView.currentMode == CanvasView.Mode.DRAWING) {
                canvasView.currentMode = CanvasView.Mode.COMPONENT_PLACEMENT
                binding.toggleMode.text = "Switch to Drawing Mode"
                binding.toolsPanel.visibility = View.GONE
                binding.componentPalette.visibility = View.VISIBLE
            } else {
                canvasView.currentMode = CanvasView.Mode.DRAWING
                binding.toggleMode.text = "Switch to Component Mode"
                binding.toolsPanel.visibility = View.VISIBLE
                binding.componentPalette.visibility = View.GONE
            }
        }
        
        // Setup layer manager button
        binding.btnManageLayers.setOnClickListener {
            showLayerManagerDialog()
        }
    }
    
    private fun setupObservers() {
        // Observe stroke color changes
        viewModel.strokeColor.observe(this) { color ->
            canvasView.setStrokeColor(color)
            binding.currentColorPreview.setBackgroundColor(color)
        }
        
        // Observe stroke width changes
        viewModel.strokeWidth.observe(this) { width ->
            canvasView.setStrokeWidth(width)
            // Since there's no direct strokeWidthSeekBar, we'll skip this
            // or we could add the SeekBar to the layout if needed
        }
        
        // Observe active layer changes
        viewModel.activeLayer.observe(this) { layer ->
            layer?.let {
                binding.activeLayerName.text = it.name
                // Update canvas to show only paths from active layer
                // This would require changes to CanvasView to support layers
            }
        }
    }

    private fun setupComponentPalette() {
        val componentAdapter = ComponentAdapter(this, componentItems) { component ->
            // When component is clicked, add it to canvas
            canvasView.addComponent(component)
            Toast.makeText(this, "Added ${component.name} to canvas", Toast.LENGTH_SHORT).show()
        }

        binding.componentRecyclerView.adapter = componentAdapter
    }

    private fun setupDrawingTools() {
        // Setup stroke width slider functionality
        // Comment out since the seekbar doesn't exist in the layout yet
        // Add the SeekBar to your layout if needed
        /*
        val strokeWidthSeekBar = binding.toolsPanel.findViewById<SeekBar>(R.id.strokeWidthSeekBar)
        strokeWidthSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    viewModel.setStrokeWidth(progress.toFloat())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        */

        // Setup color picker
        binding.btnColorPicker.setOnClickListener {
            showColorPickerDialog()
        }

        // Setup drawing tools
        binding.btnLine.setOnClickListener {
            canvasView.currentTool = CanvasView.Tool.LINE
            updateToolSelection(binding.btnLine)
        }

        binding.btnRectangle.setOnClickListener {
            canvasView.currentTool = CanvasView.Tool.RECTANGLE
            updateToolSelection(binding.btnRectangle)
        }

        binding.btnCircle.setOnClickListener {
            canvasView.currentTool = CanvasView.Tool.CIRCLE
            updateToolSelection(binding.btnCircle)
        }

        binding.btnFreehand.setOnClickListener {
            canvasView.currentTool = CanvasView.Tool.FREEHAND
            updateToolSelection(binding.btnFreehand)
        }

        binding.btnEraser.setOnClickListener {
            canvasView.currentTool = CanvasView.Tool.ERASER
            updateToolSelection(binding.btnEraser)
        }

        // Set initial tool selection
        updateToolSelection(binding.btnFreehand)
    }

    private fun updateToolSelection(selectedButton: View) {
        // Reset all buttons
        binding.btnLine.isSelected = false
        binding.btnRectangle.isSelected = false
        binding.btnCircle.isSelected = false
        binding.btnFreehand.isSelected = false
        binding.btnEraser.isSelected = false

        // Set selected button
        selectedButton.isSelected = true
    }

    private fun openFilePicker() {
        filePickerLauncher.launch("*/*")
    }

    private fun processImportedFile(uri: Uri) {
        val fileExtension = contentResolver.getType(uri)?.substringAfterLast('/')

        when {
            fileExtension?.contains("svg") == true -> {
                // Process SVG file
                canvasView.importSvgTemplate(uri)
                Toast.makeText(this, "SVG template imported", Toast.LENGTH_SHORT).show()
            }
            fileExtension?.contains("dxf") == true -> {
                // Process DXF file
                canvasView.importDxfTemplate(uri)
                Toast.makeText(this, "DXF template imported", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(this, "Unsupported file format", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveDesign() {
        viewModel.currentProject.value?.let { project ->
            // Generate thumbnail from canvas
            val thumbnail = canvasView.exportToBitmap()
            viewModel.generateThumbnail(thumbnail)
            
            // Convert canvas to SVG
            val designData = canvasView.exportToSvg()
            viewModel.updateDesignData(designData)
            
            // Save project
            projectRepository.saveProject(project)
            
            Toast.makeText(this, "Design saved successfully", Toast.LENGTH_SHORT).show()
            
            // Return to project preview
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun loadProject(projectId: String) {
        val project = projectRepository.getProject(projectId)
        if (project != null) {
            viewModel.setProject(project)
            
            // Load existing design if available
            if (project.designData.isNotEmpty()) {
                canvasView.importFromSvg(project.designData)
            }
        }
    }
    
    /**
     * Show the layer manager dialog
     */
    private fun showLayerManagerDialog() {
        viewModel.currentProject.value?.let { project ->
            val dialog = LayerManagerDialog(this, project.layers)
            
            // Set the currently selected layer
            dialog.setSelectedLayer(viewModel.activeLayer.value)
            
            // Set listener for layer modifications
            dialog.setOnLayersModifiedListener { layers, selectedLayer ->
                // Update active layer if changed
                selectedLayer?.let { viewModel.setActiveLayer(it) }
            }
            
            dialog.show()
        }
    }
    
    /**
     * Show the color picker dialog
     */
    private fun showColorPickerDialog() {
        val dialog = ColorPickerDialog(this, viewModel.strokeColor.value ?: Color.BLACK)
        
        // Set color palette
        viewModel.currentPalette.value?.let { palette ->
            dialog.setColorPalette(palette)
        }
        
        // Set listener for color selection
        dialog.setOnColorSelectedListener { color ->
            viewModel.setStrokeColor(color)
        }
        
        dialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.design_canvas_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_undo -> {
                viewModel.undo()
                canvasView.undo()
                true
            }
            R.id.action_redo -> {
                viewModel.redo()
                canvasView.redo()
                true
            }
            R.id.action_clear -> {
                showClearCanvasConfirmation()
                true
            }
            R.id.action_export -> {
                exportDesign()
                true
            }
            R.id.action_next_step -> {
                navigateToWorkflow()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showClearCanvasConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Clear Canvas")
            .setMessage("Are you sure you want to clear the entire canvas? This action cannot be undone.")
            .setPositiveButton("Clear") { _, _ ->
                viewModel.clearActiveLayer()
                canvasView.clearCanvas()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun exportDesign() {
        val options = arrayOf("SVG", "PDF", "PNG")

        AlertDialog.Builder(this)
            .setTitle("Export Design")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> exportAsSvg()
                    1 -> exportAsPdf()
                    2 -> exportAsPng()
                }
            }
            .show()
    }

    private fun exportAsSvg() {
        val svgData = canvasView.exportToSvg()
        // Implement file saving logic for SVG
        saveFileToDownloads("${viewModel.currentProject.value?.name ?: "design"}.svg", svgData.toByteArray(Charsets.UTF_8))
    }

    private fun exportAsPdf() {
        val pdfData = canvasView.exportToPdf()
        // Implement file saving logic for PDF
        saveFileToDownloads("${viewModel.currentProject.value?.name ?: "design"}.pdf", pdfData)
    }

    private fun exportAsPng() {
        val bitmap = canvasView.exportToBitmap()
        // Implement file saving logic for PNG
        saveBitmapToDownloads("${viewModel.currentProject.value?.name ?: "design"}.png", bitmap)
    }

    private fun saveFileToDownloads(fileName: String, data: ByteArray) {
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            FileOutputStream(file).use { it.write(data) }
            Toast.makeText(this, "Saved to Downloads/$fileName", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving file: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun saveBitmapToDownloads(fileName: String, bitmap: Bitmap) {
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
            Toast.makeText(this, "Saved to Downloads/$fileName", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving file: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    /**
     * Navigate to the workflow activity
     */
    private fun navigateToWorkflow() {
        // First save the current design
        viewModel.currentProject.value?.let { project ->
            // Generate thumbnail from canvas
            val thumbnail = canvasView.exportToBitmap()
            viewModel.generateThumbnail(thumbnail)
            
            // Convert canvas to SVG
            val designData = canvasView.exportToSvg()
            viewModel.updateDesignData(designData)
            
            // Save project
            projectRepository.saveProject(project)
            
            // Navigate to workflow
            val intent = Intent(this, WorkflowActivity::class.java)
            intent.putExtra("PROJECT_ID", project.id)
            startActivity(intent)
        }
    }
}