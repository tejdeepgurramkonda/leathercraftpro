package com.example.leatherdesignbackend.data

import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date
import java.util.UUID

/**
 * Represents a note associated with a leather design project
 */
@Parcelize
data class ProjectNote(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var content: String,
    var category: NoteCategory,
    val timestamp: Date = Date(),
    var imageUri: String? = null
) : Parcelable {

    // Note categories for organization
    enum class NoteCategory {
        GENERAL, 
        MATERIAL, 
        MEASUREMENT, 
        TECHNIQUE, 
        DESIGN_IDEA, 
        REFERENCE, 
        TIP,
        MISTAKE,
        IMPROVEMENT,
        OTHER
    }
}