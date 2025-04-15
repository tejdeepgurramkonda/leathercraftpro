package com.example.leatherdesignbackend.data

import java.util.Date
import java.util.UUID

/**
 * Represents a single time tracking session for a project
 */
data class TimeTrackingSession(
    val id: String = UUID.randomUUID().toString(),
    var projectId: String,
    var startTime: Date = Date(),
    var endTime: Date? = null,
    var durationMinutes: Int = 0,
    var notes: String = "",
    var stepId: String? = null
) {
    var isActive: Boolean = endTime == null
    
    fun getDurationInMillis(): Long {
        return if (endTime != null) {
            endTime!!.time - startTime.time
        } else {
            System.currentTimeMillis() - startTime.time
        }
    }
    
    fun stop() {
        if (endTime == null) {
            endTime = Date()
            durationMinutes = ((endTime!!.time - startTime.time) / (1000 * 60)).toInt()
            isActive = false
        }
    }
} 