package com.example.leatherdesignbackend.data

import android.graphics.Bitmap
import java.util.Date
import java.util.UUID

/**
 * Represents a leather design project
 */
data class DesignProject(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: String,
    val creationDate: Date = Date(),
    val description: String = "",
    var lastModified: Date = Date(),
    var designData: String = "",
    val layers: MutableList<DesignLayer> = mutableListOf(),
    val workflowSteps: MutableList<WorkflowStep> = mutableListOf(),
    var lastWorkflowActivity: Date = Date(),
    val timeTrackingSessions: MutableList<TimeTrackingSession> = mutableListOf(),
    var width: Float = 0f,
    var height: Float = 0f,
    var notes: MutableList<ProjectNote> = mutableListOf()
) {
    /**
     * Add a layer to the project
     */
    fun addLayer(layer: DesignLayer) {
        layers.add(layer)
        updateLastModified()
    }

    /**
     * Remove a layer from the project
     * Returns true if the layer was found and removed
     */
    fun removeLayer(layer: DesignLayer): Boolean {
        val result = layers.remove(layer)
        if (result) updateLastModified()
        return result
    }

    /**
     * Update the last modified date to now
     */
    fun updateLastModified() {
        lastModified = Date()
    }

    /**
     * Generate a thumbnail for the project
     * In a real app, this would save the bitmap to storage
     */
    fun generateThumbnail(bitmap: Bitmap) {
        // Implementation would store the bitmap or a reference to it
        updateLastModified()
    }
    
    /**
     * Add a workflow step to the project
     */
    fun addWorkflowStep(step: WorkflowStep) {
        // Set the order to be the next position
        if (step.order <= 0) {
            step.order = workflowSteps.size + 1
        }
        workflowSteps.add(step)
        updateLastWorkflowActivity()
        updateLastModified()
    }
    
    /**
     * Remove a workflow step from the project
     */
    fun removeWorkflowStep(step: WorkflowStep): Boolean {
        val result = workflowSteps.remove(step)
        if (result) {
            // Reorder remaining steps
            workflowSteps.sortedBy { it.order }.forEachIndexed { index, workflowStep ->
                workflowStep.order = index + 1
            }
            updateLastWorkflowActivity()
            updateLastModified()
        }
        return result
    }
    
    /**
     * Update the last workflow activity timestamp
     */
    fun updateLastWorkflowActivity() {
        lastWorkflowActivity = Date()
        updateLastModified()
    }
    
    /**
     * Calculate and return workflow progress (0.0 to 1.0)
     */
    val workflowProgress: Float
        get() {
            if (workflowSteps.isEmpty()) return 0f
            val completedSteps = workflowSteps.count { it.isCompleted }
            return completedSteps.toFloat() / workflowSteps.size
        }
    
    /**
     * Add a time tracking session
     */
    fun addTimeTrackingSession(session: TimeTrackingSession) {
        // If this is an active session, ensure no other sessions are active
        if (session.isActive) {
            timeTrackingSessions.forEach { existingSession ->
                if (existingSession.isActive && existingSession.id != session.id) {
                    existingSession.stop()
                }
            }
        }
        
        timeTrackingSessions.add(session)
        updateLastModified()
    }
    
    /**
     * Remove a time tracking session
     */
    fun removeTimeTrackingSession(session: TimeTrackingSession): Boolean {
        val result = timeTrackingSessions.remove(session)
        if (result) updateLastModified()
        return result
    }
    
    /**
     * Calculate the total time spent on this project in minutes
     */
    val totalTimeSpentMinutes: Int
        get() = timeTrackingSessions.sumOf { it.durationMinutes }
}