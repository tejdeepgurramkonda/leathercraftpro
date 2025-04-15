package com.example.leatherdesignbackend.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.leatherdesignbackend.R
import com.example.leatherdesignbackend.adapter.ExportFormatAdapter
import com.example.leatherdesignbackend.data.DesignProject
import com.example.leatherdesignbackend.data.ExportFormat
import com.example.leatherdesignbackend.databinding.ActivityFabricationPrepBinding
import com.example.leatherdesignbackend.models.LeatherTool
import com.example.leatherdesignbackend.utils.ExportUtils
import com.example.leatherdesignbackend.utils.ProjectRepository
import com.example.leatherdesignbackend.viewmodel.DesignCanvasViewModel
import java.io.File
import java.util.ArrayList

/**
 * Activity for preparing designs for fabrication and exporting in different formats
 */
class FabricationPrepActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFabricationPrepBinding
    private lateinit var projectRepository: ProjectRepository
    private lateinit var exportUtils: ExportUtils
    private lateinit var currentProject: DesignProject

    // List of available export formats
    private val exportFormats = listOf(
        ExportFormat("PDF", "Print-ready PDF with cutting and fold lines", R.drawable.ic_pdf),
        ExportFormat("SVG", "Vector format for digital editing", R.drawable.ic_svg),
        ExportFormat("DXF", "Format for laser cutters and CNC machines", R.drawable.ic_dxf),
        ExportFormat("PNG", "Image format for digital sharing", R.drawable.ic_png),
        ExportFormat("Cut List", "Text file with all pieces and required tools", R.drawable.ic_list)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFabricationPrepBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize repositories
        projectRepository = ProjectRepository(this)
        exportUtils = ExportUtils(this)

        // Get project ID from intent
        val projectId = intent.getStringExtra("PROJECT_ID") ?: return
        loadProject(projectId)

        // Setup export formats recycler view
        setupExportFormatsList()

        // Setup buttons
        setupButtons()
    }

    private fun loadProject(projectId: String) {
        projectRepository.getProject(projectId)?.let {
            currentProject = it
            title = "${it.name} - Export Options"

            // Update project info
            binding.projectName.text = it.name
            binding.projectType.text = it.type
            
            // Load the design preview
            loadDesignPreview()
        } ?: run {
            Toast.makeText(this, "Could not load project", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadDesignPreview() {
        // In a real implementation, this would load the SVG preview
        // For now, just show a placeholder
        binding.designPreview.setImageResource(R.drawable.placeholder_design)
    }

    private fun setupExportFormatsList() {
        binding.exportRecyclerView.layoutManager = LinearLayoutManager(this)
        
        val adapter = ExportFormatAdapter(this, exportFormats) { format ->
            exportDesign(format)
        }
        
        binding.exportRecyclerView.adapter = adapter
    }

    private fun setupButtons() {
        binding.btnGenerateAll.setOnClickListener {
            generateAllExports()
        }
        
        // Using the toolbar's back button instead of a separate btnBack
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun exportDesign(format: ExportFormat) {
        // Show loading indicator
        binding.progressBar.visibility = android.view.View.VISIBLE
        
        try {
            val exportedFileUri = when (format.name) {
                "PDF" -> exportPdf()
                "SVG" -> exportSvg()
                "DXF" -> exportDxf()
                "PNG" -> exportPng()
                "Cut List" -> generateCutList()
                else -> null
            }
            
            // Hide loading indicator
            binding.progressBar.visibility = android.view.View.GONE
            
            if (exportedFileUri != null) {
                showShareDialog(format.name, exportedFileUri)
            } else {
                Toast.makeText(this, "Export failed", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            // Hide loading indicator and show error
            binding.progressBar.visibility = android.view.View.GONE
            Toast.makeText(this, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun exportPdf(): Uri? {
        // In a real implementation, this would use the actual project design
        // Getting design elements from the project data
        return exportUtils.exportPdf(currentProject, emptyList())
    }
    
    private fun exportSvg(): Uri? {
        return exportUtils.exportSvg(currentProject, emptyList())
    }
    
    private fun exportDxf(): Uri? {
        return exportUtils.exportDxf(currentProject, emptyList())
    }
    
    private fun exportPng(): Uri? {
        // In a real implementation, this would capture the canvas view
        // For now, we're passing a placeholder view
        return exportUtils.exportPng(currentProject, binding.designPreview)
    }
    
    private fun generateCutList(): Uri? {
        // Sample pieces and tools
        val pieces = listOf(
            "Main Body (20cm x 15cm)",
            "Front Pocket (10cm x 8cm)",
            "Strap (30cm x 2cm)",
            "Flap (12cm x 15cm)"
        )
        
        val tools = listOf(
            LeatherTool("Round Knife", "For cutting straight lines"),
            LeatherTool("Stitching Chisel", "For punching stitch holes"),
            LeatherTool("Edge Beveler", "For finishing edges"),
            LeatherTool("Mallet", "For use with chisels and punches")
        )
        
        return exportUtils.generateCutList(currentProject, tools, pieces)
    }
    
    private fun generateAllExports() {
        // Show loading indicator
        binding.progressBar.visibility = android.view.View.VISIBLE
        
        // Export each format sequentially
        Thread {
            val results = mutableListOf<Pair<String, Uri?>>()
            
            results.add(Pair("PDF", exportPdf()))
            results.add(Pair("SVG", exportSvg()))
            results.add(Pair("DXF", exportDxf()))
            results.add(Pair("PNG", exportPng()))
            results.add(Pair("Cut List", generateCutList()))
            
            runOnUiThread {
                // Hide loading indicator
                binding.progressBar.visibility = android.view.View.GONE
                
                // Check results
                val successCount = results.count { it.second != null }
                
                if (successCount == results.size) {
                    Toast.makeText(this, "All exports completed successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "$successCount/${results.size} exports completed", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
    
    private fun showShareDialog(formatName: String, fileUri: Uri) {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, fileUri)
            when (formatName) {
                "PDF" -> type = "application/pdf"
                "SVG" -> type = "image/svg+xml"
                "DXF" -> type = "application/dxf"
                "PNG" -> type = "image/png"
                "Cut List" -> type = "text/plain"
                else -> type = "*/*"
            }
        }
        
        startActivity(Intent.createChooser(intent, "Share $formatName"))
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}