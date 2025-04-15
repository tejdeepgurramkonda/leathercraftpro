package com.example.leatherdesignbackend.data

import java.util.Date
import java.util.UUID

/**
 * Represents a progress photo for a leather crafting project
 */
data class ProgressPhoto(
    val id: String = UUID.randomUUID().toString(),
    var projectId: String,
    var imageUri: String,
    var caption: String = "",
    var captureDate: Date = Date(),
    var stepId: String? = null,
    var sortOrder: Int = 0
) 