package com.example.leatherdesignbackend.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.leatherdesignbackend.R
import com.example.leatherdesignbackend.data.DesignProject
import com.example.leatherdesignbackend.databinding.ActivityProjectDetailsBinding
import com.example.leatherdesignbackend.utils.ProjectRepository

/**
 * Activity for showing project details and allowing the user to edit or open the project
 */
class ProjectDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProjectDetailsBinding
    private lateinit var projectRepository: ProjectRepository
    private var projectId: String? = null
    private var project: DesignProject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProjectDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize repository
        projectRepository = ProjectRepository(this)

        // Get project ID from intent
        projectId = intent.getStringExtra("PROJECT_ID")
        if (projectId == null) {
            Toast.makeText(this, "Error: Project not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Load project details
        loadProjectDetails()

        // Set up button click listeners
        binding.buttonEditDesign.setOnClickListener {
            openDesignCanvas()
        }

        binding.buttonDeleteProject.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun loadProjectDetails() {
        // Load project from repository
        project = projectRepository.getProject(projectId!!)
        
        if (project == null) {
            Toast.makeText(this, "Error: Project not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Set project data to views
        binding.textProjectName.text = project!!.name
        binding.textProjectDescription.text = project!!.description
        binding.textCreationDate.text = "Created: ${project!!.creationDate}"
        binding.textLastModified.text = "Last Modified: ${project!!.lastModified}"

        // For thumbnail, we'll set a placeholder since it's not implemented in DesignProject
        binding.imageProjectThumbnail?.setImageResource(R.drawable.placeholder_design)
    }

    private fun openDesignCanvas() {
        // Open design canvas with current project
        val intent = Intent(this, DesignCanvasActivity::class.java).apply {
            putExtra("PROJECT_ID", projectId)
        }
        startActivity(intent)
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Project")
            .setMessage("Are you sure you want to delete this project? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteProject()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteProject() {
        if (projectId != null) {
            // Delete the project from repository
            projectRepository.deleteProject(projectId!!)
            
            // Show confirmation and return to main activity
            Toast.makeText(this, "Project deleted", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_project_details, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Handle back button in action bar
                onBackPressed()
                true
            }
            R.id.action_export -> {
                // Handle export action
                showExportOptionsDialog()
                true
            }
            R.id.action_share -> {
                // Handle share action
                shareProject()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showExportOptionsDialog() {
        // Show dialog with export options (PDF, SVG, etc.)
        val options = arrayOf("PDF", "SVG", "Image (PNG)")
        
        AlertDialog.Builder(this)
            .setTitle("Export Project")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> exportAsPdf()
                    1 -> exportAsSvg()
                    2 -> exportAsImage()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun exportAsPdf() {
        // Placeholder for PDF export
        Toast.makeText(this, "PDF export not implemented yet", Toast.LENGTH_SHORT).show()
    }

    private fun exportAsSvg() {
        // Placeholder for SVG export
        Toast.makeText(this, "SVG export not implemented yet", Toast.LENGTH_SHORT).show()
    }

    private fun exportAsImage() {
        // Placeholder for image export
        Toast.makeText(this, "Image export not implemented yet", Toast.LENGTH_SHORT).show()
    }

    private fun shareProject() {
        // Placeholder for sharing functionality
        Toast.makeText(this, "Sharing not implemented yet", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        // Reload project details in case they were updated
        loadProjectDetails()
    }
}
