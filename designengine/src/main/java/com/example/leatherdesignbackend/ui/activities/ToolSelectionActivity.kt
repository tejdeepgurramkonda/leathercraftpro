package com.example.leatherdesignbackend.ui.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.leatherdesignbackend.data.DesignProject
import com.example.leatherdesignbackend.databinding.ActivityToolSelectionBinding
import com.example.leatherdesignbackend.models.Tool
import com.example.leatherdesignbackend.models.ToolCategory
import com.example.leatherdesignbackend.ui.adapters.SelectableToolAdapter
import com.example.leatherdesignbackend.utils.ProjectRepository
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

/**
 * Activity for selecting tools for a leather project
 * Uses the Phase 1 tool data and allows filtering and search
 */
class ToolSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityToolSelectionBinding
    private lateinit var projectRepository: ProjectRepository
    private var currentProject: DesignProject? = null
    private lateinit var toolAdapter: SelectableToolAdapter
    private val selectedTools = mutableListOf<Tool>()
    
    // Tool repository could be replaced with a shared data source from Phase 1
    private val allTools = mutableListOf<Tool>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityToolSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // Initialize repositories
        projectRepository = ProjectRepository(this)
        
        // Get project from intent
        val projectId = intent.getStringExtra("PROJECT_ID")
        if (projectId != null) {
            currentProject = projectRepository.getProject(projectId)
            displayProjectInfo()
        } else {
            finish() // Exit if no project ID
            return
        }
        
        // Setup UI components
        setupToolsList()
        setupCategoryFilters()
        setupSearchField()
        
        // Set up preview button
        binding.previewButton.setOnClickListener {
            saveSelectedToolsAndPreview()
        }
    }
    
    private fun displayProjectInfo() {
        currentProject?.let { project ->
            binding.projectNameText.text = "Project Name: ${project.name}"
            binding.projectTypeText.text = "Type: ${project.type}"
        }
    }
    
    private fun setupToolsList() {
        // Load tools - in a real implementation, this would come from a repository shared with Phase 1
        loadDummyTools()
        
        // Setup adapter
        toolAdapter = SelectableToolAdapter(allTools) { tool, isSelected ->
            onToolSelectionChanged(tool, isSelected)
        }
        
        // Setup RecyclerView
        binding.toolsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ToolSelectionActivity)
            adapter = toolAdapter
        }
        
        // Update selected count
        updateSelectedToolsCount()
    }
    
    private fun setupCategoryFilters() {
        // Add category chips
        val chipGroup = binding.categoryChipGroup
        
        // Add all categories from enum
        ToolCategory.values().forEach { category ->
            addCategoryChip(chipGroup, category)
        }
        
        // Set category selection listener
        chipGroup.setOnCheckedChangeListener { _, checkedId ->
            filterToolsByCategory(checkedId)
        }
    }
    
    private fun addCategoryChip(chipGroup: ChipGroup, category: ToolCategory) {
        val chip = Chip(this)
        chip.id = View.generateViewId()
        chip.text = category.getDisplayName()
        chip.isCheckable = true
        chip.setChipBackgroundColorResource(com.google.android.material.R.color.mtrl_chip_background_color)
        chipGroup.addView(chip)
    }
    
    private fun setupSearchField() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                filterToolsBySearchQuery(s.toString())
            }
        })
    }
    
    private fun filterToolsByCategory(chipId: Int) {
        val selectedCategory = if (chipId == binding.allCategoryChip.id) {
            null // All categories
        } else {
            // Find the selected category from the chip text
            val selectedChip = findViewById<Chip>(chipId)
            val categoryName = selectedChip?.text?.toString() ?: return
            ToolCategory.values().find { it.getDisplayName() == categoryName }
        }
        
        // Apply category filter
        if (selectedCategory == null) {
            toolAdapter.updateTools(allTools)
        } else {
            val filteredTools = allTools.filter { 
                it.category == selectedCategory.getDisplayName()
            }
            toolAdapter.updateTools(filteredTools)
        }
        
        // Also apply any existing search filter
        val searchQuery = binding.searchEditText.text.toString()
        if (searchQuery.isNotBlank()) {
            filterToolsBySearchQuery(searchQuery)
        }
    }
    
    private fun filterToolsBySearchQuery(query: String) {
        if (query.isBlank()) {
            // If search is cleared, reapply only the category filter
            val selectedChipId = binding.categoryChipGroup.checkedChipId
            filterToolsByCategory(selectedChipId)
            return
        }
        
        // Get the current category filter
        val selectedChipId = binding.categoryChipGroup.checkedChipId
        val selectedCategory = if (selectedChipId == binding.allCategoryChip.id) {
            null // All categories
        } else {
            val selectedChip = findViewById<Chip>(selectedChipId)
            val categoryName = selectedChip?.text?.toString() ?: return
            ToolCategory.values().find { it.getDisplayName() == categoryName }
        }
        
        // Apply both filters
        val filteredTools = allTools.filter { tool ->
            // Apply category filter
            (selectedCategory == null || 
             tool.category == selectedCategory.getDisplayName()) &&
            // Apply search filter
            (tool.name.contains(query, ignoreCase = true) ||
             tool.description.contains(query, ignoreCase = true))
        }
        
        toolAdapter.updateTools(filteredTools)
    }
    
    private fun onToolSelectionChanged(tool: Tool, isSelected: Boolean) {
        if (isSelected) {
            if (!selectedTools.contains(tool)) {
                selectedTools.add(tool)
            }
        } else {
            selectedTools.remove(tool)
        }
        
        updateSelectedToolsCount()
    }
    
    private fun updateSelectedToolsCount() {
        binding.selectedToolsCount.text = "Selected: ${selectedTools.size} tools"
    }
    
    private fun saveSelectedToolsAndPreview() {
        // In a real implementation, we would save the selected tools to the project
        // For now, we'll just pass them to the preview activity
        
        // Convert to IDs to pass through intent
        val selectedToolIds = selectedTools.map { it.id }.toIntArray()
        
        val intent = Intent(this, ProjectPreviewActivity::class.java).apply {
            putExtra("PROJECT_ID", currentProject?.id)
            putExtra("SELECTED_TOOL_IDS", selectedToolIds)
        }
        startActivity(intent)
    }
    
    // This method would be replaced with actual repository calls in a real implementation
    private fun loadDummyTools() {
        // Sample tools for demonstration purposes
        allTools.add(Tool(1, "Round Knife", "A curved knife for cutting leather", 
                     imageResource = android.R.drawable.ic_menu_crop, category = "Cutting"))
        allTools.add(Tool(2, "Stitching Chisel", "Used to punch holes for stitching", 
                     imageResource = android.R.drawable.ic_menu_edit, category = "Punching"))
        allTools.add(Tool(3, "Awl", "A pointed tool for making holes", 
                     imageResource = android.R.drawable.ic_menu_add, category = "Punching"))
        allTools.add(Tool(4, "Edge Beveler", "For beveling and finishing edges", 
                     imageResource = android.R.drawable.ic_menu_manage, category = "Edge Work"))
        allTools.add(Tool(5, "Mallet", "Used for striking other tools", 
                     imageResource = android.R.drawable.ic_menu_gallery, category = "Stamping"))
        allTools.add(Tool(6, "Wing Divider", "For measuring and marking", 
                     imageResource = android.R.drawable.ic_menu_compass, category = "Measuring"))
        allTools.add(Tool(7, "Stitching Needles", "For hand-stitching leather", 
                     imageResource = android.R.drawable.ic_menu_add, category = "Stitching"))
        allTools.add(Tool(8, "Burnisher", "For smoothing and polishing edges", 
                     imageResource = android.R.drawable.ic_menu_crop, category = "Finishing"))
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}