package com.example.leatherdesignbackend.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.leatherdesignbackend.data.DesignProject
import com.example.leatherdesignbackend.databinding.ActivityProjectPreviewBinding
import com.example.leatherdesignbackend.models.Tool
import com.example.leatherdesignbackend.ui.adapters.ToolPreviewAdapter
import com.example.leatherdesignbackend.utils.ProjectRepository
import com.example.leatherdesignbackend.utils.ToolRepository

/**
 * Activity for previewing a project before saving
 * Shows project details and selected tools
 */
class ProjectPreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProjectPreviewBinding
    private lateinit var projectRepository: ProjectRepository
    private lateinit var toolRepository: ToolRepository
    private var currentProject: DesignProject? = null
    private var selectedTools = listOf<Tool>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProjectPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // Initialize repositories
        projectRepository = ProjectRepository(this)
        toolRepository = ToolRepository(this)
        
        // Load project and selected tools
        loadProjectAndTools()
        
        // Display project info
        displayProjectInfo()
        
        // Setup selected tools list
        setupSelectedToolsList()
        
        // Setup button listeners
        setupButtonListeners()
    }
    
    private fun loadProjectAndTools() {
        // Get project from intent
        val projectId = intent.getStringExtra("PROJECT_ID")
        if (projectId != null) {
            currentProject = projectRepository.getProject(projectId)
        }
        
        // Get selected tool IDs from intent
        val selectedToolIds = intent.getIntArrayExtra("SELECTED_TOOL_IDS") ?: intArrayOf()
        
        // Load tools from repository (in a real implementation, this would be a repository method)
        selectedTools = selectedToolIds.toList().mapNotNull { id -> toolRepository.getTool(id) }
    }
    
    private fun displayProjectInfo() {
        currentProject?.let { project ->
            binding.projectNameText.text = project.name
            binding.projectTypeText.text = project.type
            
            // Display dimensions if available
            if (project.width > 0 && project.height > 0) {
                binding.projectDimensionsText.text = "${project.width}cm x ${project.height}cm"
            } else {
                binding.projectDimensionsText.text = "Not specified"
            }
            
            // Display notes if available
            binding.projectNotesText.text = if (project.description.isNotBlank()) {
                project.description
            } else {
                "No notes"
            }
        }
        
        // Update selected tools count
        binding.selectedToolsCount.text = "${selectedTools.size} tools selected"
    }
    
    private fun setupSelectedToolsList() {
        val adapter = ToolPreviewAdapter(selectedTools)
        binding.selectedToolsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ProjectPreviewActivity)
            this.adapter = adapter
        }
    }
    
    private fun setupButtonListeners() {
        // Edit tools button
        binding.editToolsButton.setOnClickListener {
            onBackPressed() // Go back to tool selection
        }
        
        // Save project button
        binding.saveProjectButton.setOnClickListener {
            saveProjectAndGoToDesignCanvas()
        }
    }
    
    private fun saveProjectAndGoToDesignCanvas() {
        currentProject?.let { project ->
            // Save the project to the repository
            projectRepository.saveProject(project)
            
            // In a real implementation, we would associate the selected tools with the project
            // For this demo, we'll pretend this happens in the background
            
            // Navigate to the design canvas activity
            val intent = Intent(this, DesignCanvasActivity::class.java)
            intent.putExtra("PROJECT_ID", project.id)
            startActivity(intent)
            finish()
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}