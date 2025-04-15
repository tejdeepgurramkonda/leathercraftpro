package com.example.leatherdesignbackend.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.leatherdesignbackend.data.DesignProject
import com.example.leatherdesignbackend.data.WorkflowStep
import com.example.leatherdesignbackend.utils.ProjectRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WorkflowViewModel(application: Application) : AndroidViewModel(application) {
    
    private val projectRepository = ProjectRepository(application)
    
    private val _currentProject = MutableLiveData<DesignProject>()
    val currentProject: LiveData<DesignProject> = _currentProject
    
    private val _workflowSteps = MutableLiveData<List<WorkflowStep>>()
    val workflowSteps: LiveData<List<WorkflowStep>> = _workflowSteps
    
    private val _workflowProgress = MutableLiveData<Float>()
    val workflowProgress: LiveData<Float> = _workflowProgress
    
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    /**
     * Load a project by ID
     */
    fun loadProject(projectId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val project = withContext(Dispatchers.IO) {
                projectRepository.getProject(projectId)
            }
            
            project?.let {
                _currentProject.value = it
                updateWorkflowSteps(it.workflowSteps)
                _workflowProgress.value = it.workflowProgress
            }
            
            _isLoading.value = false
        }
    }
    
    /**
     * Add a new workflow step
     */
    fun addWorkflowStep(name: String, description: String, estimatedTime: String = "") {
        val currentProject = _currentProject.value ?: return
        
        val newStep = WorkflowStep(
            projectId = currentProject.id,
            name = name,
            description = description,
            order = currentProject.workflowSteps.size + 1,
            estimatedTime = estimatedTime
        )
        
        viewModelScope.launch {
            currentProject.addWorkflowStep(newStep)
            updateWorkflowSteps(currentProject.workflowSteps)
            _workflowProgress.value = currentProject.workflowProgress
            
            // Save changes to the project
            withContext(Dispatchers.IO) {
                projectRepository.saveProject(currentProject)
            }
        }
    }
    
    /**
     * Update an existing workflow step
     */
    fun updateWorkflowStep(step: WorkflowStep) {
        val currentProject = _currentProject.value ?: return
        
        viewModelScope.launch {
            // Find the step in the project by ID and update it
            val projectStep = currentProject.workflowSteps.find { it.id == step.id } ?: return@launch
            
            // Update step properties
            val updatedStep = projectStep.copy(
                name = step.name,
                description = step.description,
                estimatedTime = step.estimatedTime
            )
            
            // Replace the old step with the updated one
            currentProject.workflowSteps.replaceAll { if (it.id == step.id) updatedStep else it }
            
            // Notify about changes
            updateWorkflowSteps(currentProject.workflowSteps)
            
            // Save changes to the project
            withContext(Dispatchers.IO) {
                projectRepository.saveProject(currentProject)
            }
        }
    }
    
    /**
     * Toggle step completion status
     */
    fun toggleStepCompletion(step: WorkflowStep) {
        val currentProject = _currentProject.value ?: return
        
        viewModelScope.launch {
            // Find the step in the project
            val projectStep = currentProject.workflowSteps.find { it.id == step.id } ?: return@launch
            
            // Toggle completion status
            projectStep.isCompleted = !projectStep.isCompleted
            
            // Update the project's workflow progress
            currentProject.updateLastWorkflowActivity()
            updateWorkflowSteps(currentProject.workflowSteps)
            _workflowProgress.value = currentProject.workflowProgress
            
            // Save changes to the project
            withContext(Dispatchers.IO) {
                projectRepository.saveProject(currentProject)
            }
        }
    }
    
    /**
     * Remove a workflow step
     */
    fun removeWorkflowStep(step: WorkflowStep) {
        val currentProject = _currentProject.value ?: return
        
        viewModelScope.launch {
            currentProject.removeWorkflowStep(step)
            updateWorkflowSteps(currentProject.workflowSteps)
            _workflowProgress.value = currentProject.workflowProgress
            
            // Save changes to the project
            withContext(Dispatchers.IO) {
                projectRepository.saveProject(currentProject)
            }
        }
    }
    
    /**
     * Update the order of workflow steps
     */
    fun reorderWorkflowSteps(steps: List<WorkflowStep>) {
        val currentProject = _currentProject.value ?: return
        
        viewModelScope.launch {
            // Update order values
            steps.forEachIndexed { index, step ->
                step.order = index + 1
            }
            
            // Replace steps in project
            currentProject.workflowSteps.clear()
            currentProject.workflowSteps.addAll(steps)
            currentProject.updateLastWorkflowActivity()
            
            updateWorkflowSteps(currentProject.workflowSteps)
            
            // Save changes to the project
            withContext(Dispatchers.IO) {
                projectRepository.saveProject(currentProject)
            }
        }
    }
    
    /**
     * Update the workflow steps LiveData with a sorted copy
     */
    private fun updateWorkflowSteps(steps: List<WorkflowStep>) {
        _workflowSteps.value = steps.sortedBy { it.order }
    }
} 