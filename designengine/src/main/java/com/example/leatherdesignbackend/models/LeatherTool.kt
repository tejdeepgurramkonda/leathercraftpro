package com.example.leatherdesignbackend.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents a leather crafting tool with its usage description
 */
@Parcelize
data class LeatherTool(
    val name: String,
    val description: String,
    val imageResource: Int = 0,
    val shortDescription: String = "",
    val category: String = "General",
    val skillLevel: String = "Beginner"
) : Parcelable
