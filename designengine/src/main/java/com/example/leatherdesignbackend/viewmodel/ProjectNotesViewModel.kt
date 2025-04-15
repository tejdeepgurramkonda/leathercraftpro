package com.example.leatherdesignbackend.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.leatherdesignbackend.data.DesignProject
import com.example.leatherdesignbackend.data.ProjectNote
import com.example.leatherdesignbackend.data.ProjectNote.NoteCategory
import com.example.leatherdesignbackend.utils.ProjectRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ProjectNotesViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = ProjectRepository(application)
    
    private val _projectData = MutableLiveData<DesignProject>()
    val projectData: LiveData<DesignProject> = _projectData
    
    private val _allNotes = MutableLiveData<List<ProjectNote>>(emptyList())
    val allNotes: LiveData<List<ProjectNote>> = _allNotes
    
    private val _filteredNotes = MutableLiveData<List<ProjectNote>>(emptyList())
    val filteredNotes: LiveData<List<ProjectNote>> = _filteredNotes
    
    private var selectedCategory: NoteCategory? = null
    
    /**
     * Load a project by ID
     */
    fun loadProject(projectId: String) {
        viewModelScope.launch {
            val project = repository.getProject(projectId)
            project?.let {
                _projectData.postValue(project)
                
                // For demonstration purposes, create some sample notes if none exist
                if (project.notes == null || project.notes.isEmpty()) {
                    createSampleNotes(projectId)
                } else {
                    _allNotes.postValue(project.notes.sortedByDescending { it.timestamp })
                    applyFilter()
                }
            }
        }
    }
    
    private fun createSampleNotes(projectId: String) {
        val sampleNotes = listOf(
            ProjectNote(
                title = "Material Selection",
                content = "Using 3oz veg-tan leather for the main body and 2oz for the pockets",
                category = NoteCategory.MATERIAL
            ),
            ProjectNote(
                title = "Stitching Technique",
                content = "Planning to use saddle stitch with 0.8mm waxed thread, 5mm spacing",
                category = NoteCategory.TECHNIQUE
            ),
            ProjectNote(
                title = "Measurements",
                content = "Wallet dimensions: 9cm x 11cm when folded",
                category = NoteCategory.MEASUREMENT
            )
        )
        
        // Add sample notes
        for (note in sampleNotes) {
            addNote(note.title, note.content, note.category, null)
        }
    }
    
    /**
     * Filter notes by category
     */
    fun filterByCategory(category: NoteCategory?) {
        selectedCategory = category
        applyFilter()
    }
    
    /**
     * Apply category filter to notes
     */
    private fun applyFilter() {
        val notes = _allNotes.value ?: emptyList()
        _filteredNotes.value = if (selectedCategory == null) {
            notes
        } else {
            notes.filter { it.category == selectedCategory }
        }
    }
    
    /**
     * Add a new note
     */
    fun addNote(title: String, content: String, category: NoteCategory?, imageUri: String?) {
        val project = _projectData.value ?: return
        
        val newNote = ProjectNote(
            id = UUID.randomUUID().toString(),
            title = title,
            content = content,
            category = category ?: NoteCategory.GENERAL,
            imageUri = imageUri
        )
        
        viewModelScope.launch {
            // Add note to project
            if (project.notes == null) {
                project.notes = mutableListOf()
            }
            project.notes.add(newNote)
            project.updateLastModified()
            
            // Update repository
            repository.saveProject(project)
            
            // Update LiveData
            _projectData.postValue(project)
            _allNotes.postValue(project.notes.sortedByDescending { it.timestamp })
            applyFilter()
        }
    }
    
    /**
     * Update an existing note
     */
    fun updateNote(noteId: String, title: String, content: String, category: NoteCategory?, imageUri: String?) {
        val project = _projectData.value ?: return
        
        viewModelScope.launch {
            // Find and update the note
            val notes = project.notes ?: mutableListOf()
            val noteIndex = notes.indexOfFirst { it.id == noteId }
            
            if (noteIndex >= 0) {
                val updatedNote = notes[noteIndex].copy(
                    title = title,
                    content = content,
                    category = category ?: NoteCategory.GENERAL,
                    imageUri = imageUri
                )
                
                notes[noteIndex] = updatedNote
                project.updateLastModified()
                
                // Update repository
                repository.saveProject(project)
                
                // Update LiveData
                _projectData.postValue(project)
                _allNotes.postValue(notes.sortedByDescending { it.timestamp })
                applyFilter()
            }
        }
    }
    
    /**
     * Remove a note
     */
    fun deleteNote(noteId: String) {
        val project = _projectData.value ?: return
        
        viewModelScope.launch {
            // Find and remove the note
            val notes = project.notes ?: mutableListOf()
            val noteToRemove = notes.find { it.id == noteId }
            
            if (noteToRemove != null) {
                notes.remove(noteToRemove)
                project.updateLastModified()
                
                // Delete associated image if exists
                noteToRemove.imageUri?.let { uri ->
                    deleteImageFile(Uri.parse(uri))
                }
                
                // Update repository
                repository.saveProject(project)
                
                // Update LiveData
                _projectData.postValue(project)
                _allNotes.postValue(notes.sortedByDescending { it.timestamp })
                applyFilter()
            }
        }
    }
    
    /**
     * Save an image to storage
     */
    suspend fun saveImageToStorage(sourceUri: Uri, projectId: String): String {
        val context = getApplication<Application>()
        val contentResolver = context.contentResolver
        
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Note_${projectId}_${timestamp}.jpg"
        
        val projectFolder = File(context.filesDir, "project_${projectId}/notes")
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
        
        return Uri.fromFile(targetFile).toString()
    }
    
    /**
     * Delete an image file
     */
    private fun deleteImageFile(uri: Uri) {
        val file = File(uri.path ?: return)
        if (file.exists()) {
            file.delete()
        }
    }
}