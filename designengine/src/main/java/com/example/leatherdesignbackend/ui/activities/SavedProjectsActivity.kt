package com.example.leatherdesignbackend.ui.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.leatherdesignbackend.data.DesignProject
import com.example.leatherdesignbackend.databinding.ActivitySavedProjectsBinding
import com.example.leatherdesignbackend.ui.adapters.SavedProjectAdapter
import com.example.leatherdesignbackend.utils.ProjectRepository

/**
 * Activity for displaying saved projects
 * Allows viewing, editing, and managing leather projects
 */
class SavedProjectsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySavedProjectsBinding
    private lateinit var projectRepository: ProjectRepository
    private lateinit var projectAdapter: SavedProjectAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySavedProjectsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // Initialize repository
        projectRepository = ProjectRepository(this)
        
        // Setup RecyclerView
        setupProjectsList()
        
        // Setup search functionality
        setupSearch()
        
        // Setup FAB for adding new projects
        binding.fabAddProject.setOnClickListener {
            startActivity(Intent(this, ProjectSetupActivity::class.java))
        }
    }
    
    private fun setupProjectsList() {
        // Initialize adapter with action callbacks
        projectAdapter = SavedProjectAdapter(
            onViewTools = { project -> navigateToToolPreview(project) },
            onEditProject = { project -> navigateToProjectEdit(project) },
            onDesignProject = { project -> navigateToDesignCanvas(project) }
        )
        
        // Setup RecyclerView
        binding.projectsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@SavedProjectsActivity)
            adapter = projectAdapter
        }
    }
    
    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                filterProjects(s.toString())
            }
        })
    }
    
    private fun filterProjects(query: String) {
        val allProjects = projectRepository.getAllProjects()
        if (query.isBlank()) {
            updateProjectsList(allProjects)
        } else {
            val filteredProjects = allProjects.filter { project ->
                project.name.contains(query, ignoreCase = true) ||
                project.type.contains(query, ignoreCase = true) ||
                project.description.contains(query, ignoreCase = true)
            }
            updateProjectsList(filteredProjects)
        }
    }
    
    private fun updateProjectsList(projects: List<DesignProject>) {
        projectAdapter.updateProjects(projects)
        
        // Show/hide empty state
        if (projects.isEmpty()) {
            binding.emptyStateView.visibility = View.VISIBLE
            binding.projectsRecyclerView.visibility = View.GONE
        } else {
            binding.emptyStateView.visibility = View.GONE
            binding.projectsRecyclerView.visibility = View.VISIBLE
        }
    }
    
    private fun navigateToToolPreview(project: DesignProject) {
        // In a real implementation, we would load the tools associated with this project
        // and pass them to the preview activity
        val intent = Intent(this, ProjectPreviewActivity::class.java).apply {
            putExtra("PROJECT_ID", project.id)
            // Normally we would also include the tool IDs
        }
        startActivity(intent)
    }
    
    private fun navigateToProjectEdit(project: DesignProject) {
        val intent = Intent(this, ProjectSetupActivity::class.java).apply {
            putExtra("PROJECT_ID", project.id)
        }
        startActivity(intent)
    }
    
    private fun navigateToDesignCanvas(project: DesignProject) {
        val intent = Intent(this, DesignCanvasActivity::class.java).apply {
            putExtra("PROJECT_ID", project.id)
        }
        startActivity(intent)
    }
    
    override fun onResume() {
        super.onResume()
        // Reload projects when returning to this activity
        loadProjects()
    }
    
    private fun loadProjects() {
        val projects = projectRepository.getAllProjects()
        updateProjectsList(projects)
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}