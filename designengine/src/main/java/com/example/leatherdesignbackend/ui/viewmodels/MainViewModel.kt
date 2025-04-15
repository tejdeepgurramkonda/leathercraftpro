package com.example.leatherdesignbackend.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.leatherdesignbackend.data.DesignProject
import java.util.*

/**
 * ViewModel for the Main Activity
 * Handles project listing and management
 */
class MainViewModel : ViewModel() {
    
    // List of projects
    val projects = MutableLiveData<List<DesignProject>>(emptyList())
    
    // Loading state
    val isLoading = MutableLiveData<Boolean>(false)
    
    // Error state
    val errorMessage = MutableLiveData<String?>(null)
    
    /**
     * Load all projects
     */
    fun loadProjects() {
        // In a real implementation, this would load from repository
        isLoading.value = true
        // Simulate loading delay
        // In actual implementation, this would be a repository call
        isLoading.value = false
    }
    
    /**
     * Delete a project
     */
    fun deleteProject(project: DesignProject) {
        // In a real implementation, this would delete from repository
        val currentList = projects.value?.toMutableList() ?: mutableListOf()
        currentList.remove(project)
        projects.value = currentList
    }
    
    /**
     * Create a new project
     */
    fun createProject(name: String, type: String, width: Float, height: Float, description: String = ""): DesignProject {
        // In a real implementation, this would create in repository
        val project = DesignProject(
            id = generateProjectId(),
            name = name,
            type = type,
            width = width,
            height = height,
            description = description,
            creationDate = Date(System.currentTimeMillis())
        )
        
        val currentList = projects.value?.toMutableList() ?: mutableListOf()
        currentList.add(project)
        projects.value = currentList
        
        return project
    }
    
    /**
     * Generate a unique project ID
     * In a real implementation, this would be handled by the database
     */
    private fun generateProjectId(): String {
        return "project_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
} 