package com.example.leatherdesignbackend.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import com.example.leatherdesignbackend.R
import com.example.leatherdesignbackend.data.DesignProject
import com.example.leatherdesignbackend.databinding.ActivityProjectSetupBinding

/**
 * Activity for setting up a new leathercraft project
 * Allows user to enter project name, type, dimensions, and notes
 */
class ProjectSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProjectSetupBinding
    
    // Pre-defined project types
    private val projectTypes = listOf(
        "Wallet", "Belt", "Bag", "Notebook Cover", "Key Holder", 
        "Coaster", "Watch Strap", "Passport Holder", "Other"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProjectSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // Setup project type dropdown
        setupProjectTypeDropdown()
        
        // Setup next button
        binding.nextButton.setOnClickListener {
            if (validateInputs()) {
                createProjectAndProceed()
            }
        }
    }
    
    private fun setupProjectTypeDropdown() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, projectTypes)
        (binding.projectTypeDropdown as? AutoCompleteTextView)?.setAdapter(adapter)
    }
    
    private fun validateInputs(): Boolean {
        var isValid = true
        
        // Validate project name
        if (binding.projectNameEditText.text.isNullOrBlank()) {
            binding.projectNameLayout.error = "Project name is required"
            isValid = false
        } else {
            binding.projectNameLayout.error = null
        }
        
        // Validate project type
        if (binding.projectTypeDropdown.text.isNullOrBlank()) {
            binding.projectTypeLayout.error = "Project type is required"
            isValid = false
        } else {
            binding.projectTypeLayout.error = null
        }
        
        return isValid
    }
    
    private fun createProjectAndProceed() {
        // Get values from form
        val projectName = binding.projectNameEditText.text.toString()
        val projectType = binding.projectTypeDropdown.text.toString()
        val projectNotes = binding.projectNotesEditText.text.toString()
        
        // Parse dimensions (if provided)
        val width = binding.projectWidthEditText.text.toString().toFloatOrNull() ?: 0f
        val height = binding.projectHeightEditText.text.toString().toFloatOrNull() ?: 0f
        
        // Create project object
        val project = DesignProject(
            name = projectName,
            type = projectType,
            description = projectNotes,
            width = width,
            height = height
        )
        
        // Start tool selection activity with project data
        val intent = Intent(this, ToolSelectionActivity::class.java).apply {
            putExtra("PROJECT_ID", project.id)
        }
        startActivity(intent)
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 