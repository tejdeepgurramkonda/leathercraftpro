package com.example.leatherdesignbackend.data

import java.util.UUID

/**
 * Data class for storing workflow steps of a leather project
 */
data class WorkflowStep(
    val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val name: String,
    val description: String = "",
    var order: Int,
    var isCompleted: Boolean = false,
    val estimatedTime: String = "",
    val toolIds: List<Int> = emptyList(),
    val materialIds: List<Int> = emptyList()
) 