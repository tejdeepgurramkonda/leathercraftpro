package com.example.leatherdesignbackend.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Model class representing a leather crafting tool
 */
@Parcelize
data class Tool(
    val id: Int,
    val name: String,
    val description: String,
    val imageResource: Int = 0,
    val category: String = "General",
    val skillLevel: String = "Beginner",
    val usage: String = "",
    val specifications: Map<String, String> = emptyMap()
) : Parcelable

/**
 * Enum representing different categories of leather crafting tools.
 * Now automatically parcelable via @Parcelize.
 */
@Parcelize
enum class ToolCategory : Parcelable {
    CUTTING,
    PUNCHING,
    STITCHING,
    FINISHING,
    MEASURING,
    STAMPING,
    EDGE_WORK,
    MISCELLANEOUS;

    fun getDisplayName(): String {
        return when (this) {
            CUTTING -> "Cutting"
            PUNCHING -> "Punching"
            STITCHING -> "Stitching"
            FINISHING -> "Finishing"
            MEASURING -> "Measuring"
            STAMPING -> "Stamping"
            EDGE_WORK -> "Edge Work"
            MISCELLANEOUS -> "Miscellaneous"
        }
    }

    fun getIconResource(): Int {
        return when (this) {
            CUTTING -> android.R.drawable.ic_menu_crop
            PUNCHING -> android.R.drawable.ic_menu_edit
            STITCHING -> android.R.drawable.ic_menu_add
            FINISHING -> android.R.drawable.ic_menu_compass
            MEASURING -> android.R.drawable.ic_menu_zoom
            STAMPING -> android.R.drawable.ic_menu_gallery
            EDGE_WORK -> android.R.drawable.ic_menu_manage
            MISCELLANEOUS -> android.R.drawable.ic_menu_more
        }
    }

    companion object {
        fun getCategories(): List<ToolCategory> = values().toList()
    }
}
