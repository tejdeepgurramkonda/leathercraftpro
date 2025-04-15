package com.example.leatherdesignbackend.viewmodel

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.leatherdesignbackend.data.DesignProject
import com.example.leatherdesignbackend.data.TimeTrackingSession
import com.example.leatherdesignbackend.data.WorkflowStep
import com.example.leatherdesignbackend.utils.ProjectRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.concurrent.TimeUnit

class TimeTrackerViewModel(application: Application) : AndroidViewModel(application) {
    
    private val projectRepository = ProjectRepository(application)
    
    private val _currentProject = MutableLiveData<DesignProject>()
    val currentProject: LiveData<DesignProject> = _currentProject
    
    private val _timeTrackingSessions = MutableLiveData<List<TimeTrackingSession>>()
    val timeTrackingSessions: LiveData<List<TimeTrackingSession>> = _timeTrackingSessions
    
    private val _workflowSteps = MutableLiveData<List<WorkflowStep>>()
    val workflowSteps: LiveData<List<WorkflowStep>> = _workflowSteps
    
    private val _activeSession = MutableLiveData<TimeTrackingSession?>()
    val activeSession: LiveData<TimeTrackingSession?> = _activeSession
    
    private val _totalTimeSpent = MutableLiveData<Long>()
    val totalTimeSpent: LiveData<Long> = _totalTimeSpent
    
    private val _elapsedTime = MutableLiveData<Long>()
    val elapsedTime: LiveData<Long> = _elapsedTime
    
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val handler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            updateElapsedTime()
            handler.postDelayed(this, 1000) // Update every second
        }
    }
    
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
                _workflowSteps.value = it.workflowSteps.sortedBy { step -> step.order }
                updateSessions(it.timeTrackingSessions)
                _totalTimeSpent.value = it.totalTimeSpentMinutes * 60 * 1000L // Convert to milliseconds
                
                // Check for active session
                val activeSession = it.timeTrackingSessions.find { session -> session.isActive }
                _activeSession.value = activeSession
                
                if (activeSession != null) {
                    startTimer()
                }
            }
            
            _isLoading.value = false
        }
    }
    
    /**
     * Start a new time tracking session
     */
    fun startTimeTracking(stepId: String? = null, notes: String = "") {
        val currentProject = _currentProject.value ?: return
        
        // Stop any active session first
        stopTimeTracking()
        
        viewModelScope.launch {
            // Create and add a new session
            val newSession = TimeTrackingSession(
                projectId = currentProject.id,
                startTime = Date(),
                notes = notes,
                stepId = stepId
            )
            
            currentProject.addTimeTrackingSession(newSession)
            _activeSession.value = newSession
            updateSessions(currentProject.timeTrackingSessions)
            
            // Save changes to the project
            withContext(Dispatchers.IO) {
                projectRepository.saveProject(currentProject)
            }
            
            // Start timer
            startTimer()
        }
    }
    
    /**
     * Stop the active time tracking session
     */
    fun stopTimeTracking() {
        val currentProject = _currentProject.value ?: return
        val currentSession = _activeSession.value ?: return
        
        viewModelScope.launch {
            // Stop the session
            currentSession.stop()
            _activeSession.value = null
            
            // Update total time
            _totalTimeSpent.value = currentProject.totalTimeSpentMinutes * 60 * 1000L
            updateSessions(currentProject.timeTrackingSessions)
            
            // Stop timer
            stopTimer()
            
            // Save changes to the project
            withContext(Dispatchers.IO) {
                projectRepository.saveProject(currentProject)
            }
        }
    }
    
    /**
     * Add notes to the active session
     */
    fun updateSessionNotes(notes: String) {
        val currentProject = _currentProject.value ?: return
        val currentSession = _activeSession.value ?: return
        
        currentSession.notes = notes
        
        viewModelScope.launch {
            // Save changes to the project
            withContext(Dispatchers.IO) {
                projectRepository.saveProject(currentProject)
            }
        }
    }
    
    /**
     * Get formatted time string (HH:MM:SS)
     */
    fun getFormattedTime(timeMillis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(timeMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeMillis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeMillis) % 60
        
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
    
    /**
     * Start the timer for tracking elapsed time
     */
    private fun startTimer() {
        stopTimer() // Ensure no duplicate timers
        updateElapsedTime()
        handler.post(timerRunnable)
    }
    
    /**
     * Stop the timer
     */
    private fun stopTimer() {
        handler.removeCallbacks(timerRunnable)
    }
    
    /**
     * Update the elapsed time of the active session
     */
    private fun updateElapsedTime() {
        val activeSession = _activeSession.value ?: return
        _elapsedTime.value = activeSession.getDurationInMillis()
    }
    
    /**
     * Update the time tracking sessions LiveData
     */
    private fun updateSessions(sessions: List<TimeTrackingSession>) {
        _timeTrackingSessions.value = sessions.sortedByDescending { it.startTime }
    }
    
    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
} 