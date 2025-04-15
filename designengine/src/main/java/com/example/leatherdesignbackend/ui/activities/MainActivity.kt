package com.example.leatherdesignbackend.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.leatherdesignbackend.R
import com.example.leatherdesignbackend.data.DesignProject
import com.example.leatherdesignbackend.databinding.ActivityMainBinding
import com.example.leatherdesignbackend.ui.adapters.ProjectAdapter
import com.example.leatherdesignbackend.utils.ProjectRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

/**
 * Main activity for the Leather Design Engine application
 * Displays a grid of projects and provides navigation to other activities
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var projectAdapter: ProjectAdapter
    private lateinit var projectRepository: ProjectRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        // Initialize project repository
        projectRepository = ProjectRepository(this)

        // Setup project grid
        setupProjectGrid()

        // Setup FAB for creating new projects
        binding.fabAddProject.setOnClickListener {
            showCreateProjectDialog()
        }
    }

    private fun setupProjectGrid() {
        // Configure the RecyclerView with a GridLayoutManager
        val gridLayoutManager = GridLayoutManager(this, 2) // 2 columns
        binding.projectsRecyclerView.layoutManager = gridLayoutManager

        // Initialize the adapter with an empty list
        projectAdapter = ProjectAdapter(emptyList()) { project ->
            // Handle project click - open project details
            openProjectDetails(project)
        }

        binding.projectsRecyclerView.adapter = projectAdapter

        // Load projects
        loadProjects()
    }

    private fun loadProjects() {
        // Show loading indicator
        binding.progressBar.visibility = View.VISIBLE

        // Load projects from repository
        val projects = projectRepository.getAllProjects()

        // Update UI on main thread
        runOnUiThread {
            // Hide loading indicator
            binding.progressBar.visibility = View.GONE

            // Update adapter with projects
            projectAdapter.updateProjects(projects)

            // Show empty state if no projects
            if (projects.isEmpty()) {
                binding.emptyStateLayout.visibility = View.VISIBLE
            } else {
                binding.emptyStateLayout.visibility = View.GONE
            }
        }
    }

    private fun showCreateProjectDialog() {
        // Inflate the dialog layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_project, null)
        val nameEditText = dialogView.findViewById<TextInputEditText>(R.id.projectNameEditText)
        val descriptionEditText = dialogView.findViewById<TextInputEditText>(R.id.projectDescriptionEditText)

        // Create and show the dialog
        MaterialAlertDialogBuilder(this)
            .setTitle("Create New Project")
            .setView(dialogView)
            .setPositiveButton("Create") { dialog, _ ->
                val name = nameEditText.text.toString()
                val description = descriptionEditText.text.toString()

                if (name.isNotBlank()) {
                    createNewProject(name, description)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun createNewProject(name: String, description: String) {
        // Create a new project
        val project = DesignProject(
            name = name,
            description = description,
            type = "Leather" // Adding default project type
        )

        // Save the project
        projectRepository.saveProject(project)

        // Open the design canvas with the new project
        val intent = Intent(this, DesignCanvasActivity::class.java).apply {
            putExtra("PROJECT_ID", project.id)
        }
        startActivity(intent)
    }

    private fun openProjectDetails(project: DesignProject) {
        // Open the project details activity
        val intent = Intent(this, ProjectDetailsActivity::class.java).apply {
            putExtra("PROJECT_ID", project.id)
        }
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        // Reload projects when returning to this activity
        loadProjects()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                // Open settings activity
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_tool_library -> {
                // Open tool library activity
                val intent = Intent(this, ToolLibraryActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_tutorial -> {
                // Open tutorial activity
                val intent = Intent(this, TutorialActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}