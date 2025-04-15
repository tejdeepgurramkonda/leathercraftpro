package com.example.leatherdesignengine

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.leatherdesignbackend.databinding.ActivityProjectDetailsBinding

class ProjectDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProjectDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProjectDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize views
        setupViews()
    }

    private fun setupViews() {
        binding.toolbar.setNavigationOnClickListener { finish() }
        
        // Example of setting data
        binding.textProjectName.text = "Sample Project"
        binding.textProjectDescription.text = "This is a sample project description"
        binding.textCreationDate.text = "Created: ${getCurrentDate()}"
        binding.textLastModified.text = "Last modified: ${getCurrentDate()}"
        
        binding.buttonEditDesign.setOnClickListener {
            // Handle edit design click
        }
        
        binding.buttonDeleteProject.setOnClickListener {
            // Handle delete project click
        }
    }

    private fun getCurrentDate(): String {
        return android.text.format.DateFormat.format("yyyy-MM-dd", java.util.Date()).toString()
    }
}