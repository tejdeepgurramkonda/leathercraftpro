package com.example.leatherdesignbackend.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Data model representing a component that can be added to a design
 * Contains component metadata and appearance information
 */
@Parcelize
data class ComponentItem(
    val name: String,
    val imageRes: Int,
    val description: String = "",
    val category: String = "General"
) : Parcelable
