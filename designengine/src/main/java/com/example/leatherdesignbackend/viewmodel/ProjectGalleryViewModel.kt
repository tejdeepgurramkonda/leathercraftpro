package com.example.leatherdesignbackend.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.leatherdesignbackend.data.DesignProject
import com.example.leatherdesignbackend.data.ProgressPhoto
import com.example.leatherdesignbackend.data.WorkflowStep
import com.example.leatherdesignbackend.utils.ProjectRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProjectGalleryViewModel(application: Application) : AndroidViewModel(application) {
    
    private val projectRepository = ProjectRepository(application)
    
    private val _currentProject = MutableLiveData<DesignProject>()
    val currentProject: LiveData<DesignProject> = _currentProject
    
    private val _photos = MutableLiveData<List<ProgressPhoto>>()
    val photos: LiveData<List<ProgressPhoto>> = _photos
    
    private val _workflowSteps = MutableLiveData<List<WorkflowStep>>()
    val workflowSteps: LiveData<List<WorkflowStep>> = _workflowSteps
    
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
                _photos.value = getProjectPhotos(it)
                _workflowSteps.value = getProjectWorkflowSteps(it)
            }
            
            _isLoading.value = false
        }
    }
    
    /**
     * Get progress photos for a project
     * In a real app, these would be stored in the project or in a database
     */
    private fun getProjectPhotos(project: DesignProject): List<ProgressPhoto> {
        // This would typically retrieve photos from a database
        // For the demo, we'll return an empty list
        return emptyList()
    }
    
    /**
     * Get workflow steps for a project
     * In a real app, these would be stored in the project or in a database
     */
    private fun getProjectWorkflowSteps(project: DesignProject): List<WorkflowStep> {
        // This would typically retrieve workflow steps from a database
        // For the demo, we'll return an empty list
        return emptyList()
    }
    
    /**
     * Add a new progress photo
     */
    fun addProgressPhoto(imageUri: Uri, caption: String, stepId: String? = null) {
        val currentProject = _currentProject.value ?: return
        
        viewModelScope.launch {
            // Save the photo to app storage and get a persistent URI
            val savedUri = withContext(Dispatchers.IO) {
                saveImageToStorage(imageUri, currentProject.id)
            }
            
            // Create and add a new progress photo
            val progressPhoto = ProgressPhoto(
                projectId = currentProject.id,
                imageUri = savedUri.toString(),
                caption = caption,
                stepId = stepId
            )
            
            // Add to the current list of photos
            val currentPhotos = _photos.value?.toMutableList() ?: mutableListOf()
            currentPhotos.add(progressPhoto)
            _photos.value = currentPhotos
            
            // Save changes to the project
            // In a real implementation, this would update the project's photos in the database
            withContext(Dispatchers.IO) {
                projectRepository.saveProject(currentProject)
                // In a real app: projectRepository.saveProgressPhoto(progressPhoto)
            }
        }
    }
    
    /**
     * Remove a progress photo
     */
    fun removeProgressPhoto(photo: ProgressPhoto) {
        viewModelScope.launch {
            // Remove the photo from the list
            val currentPhotos = _photos.value?.toMutableList() ?: mutableListOf()
            currentPhotos.remove(photo)
            _photos.value = currentPhotos
            
            // Delete the image file
            withContext(Dispatchers.IO) {
                val uri = Uri.parse(photo.imageUri)
                deleteImageFile(uri)
                
                // In a real app: projectRepository.deleteProgressPhoto(photo.id)
            }
            
            // Save changes to the project
            val currentProject = _currentProject.value ?: return@launch
            withContext(Dispatchers.IO) {
                projectRepository.saveProject(currentProject)
            }
        }
    }
    
    /**
     * Update a photo's caption
     */
    fun updatePhotoCaption(photo: ProgressPhoto, newCaption: String) {
        viewModelScope.launch {
            // Update caption
            val updatedPhoto = photo.copy(caption = newCaption)
            
            // Update in the list
            val currentPhotos = _photos.value?.toMutableList() ?: mutableListOf()
            val index = currentPhotos.indexOfFirst { it.id == photo.id }
            if (index >= 0) {
                currentPhotos[index] = updatedPhoto
                _photos.value = currentPhotos
            }
            
            // Save changes to the project
            val currentProject = _currentProject.value ?: return@launch
            withContext(Dispatchers.IO) {
                projectRepository.saveProject(currentProject)
                // In a real app: projectRepository.updateProgressPhoto(updatedPhoto)
            }
        }
    }
    
    /**
     * Get photos for a specific workflow step
     */
    fun getPhotosForStep(stepId: String): List<ProgressPhoto> {
        return _photos.value?.filter { it.stepId == stepId } ?: emptyList()
    }
    
    /**
     * Save an image to storage
     */
    private suspend fun saveImageToStorage(sourceUri: Uri, projectId: String): Uri {
        val context = getApplication<Application>()
        val contentResolver = context.contentResolver
        
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Project_${projectId}_${timestamp}.jpg"
        
        val projectFolder = File(context.filesDir, "project_${projectId}")
        if (!projectFolder.exists()) {
            projectFolder.mkdirs()
        }
        
        val targetFile = File(projectFolder, fileName)
        
        // Copy image data
        contentResolver.openInputStream(sourceUri)?.use { inputStream ->
            FileOutputStream(targetFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        
        return Uri.fromFile(targetFile)
    }
    
    /**
     * Delete an image file
     */
    private suspend fun deleteImageFile(uri: Uri) {
        val context = getApplication<Application>()
        val file = File(uri.path ?: return)
        if (file.exists()) {
            file.delete()
        }
    }
} 