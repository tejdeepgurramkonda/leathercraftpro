package com.example.leatherdesignbackend.models

/**
 * Data model representing a leather crafting tutorial
 */
data class Tutorial(
    val id: String,
    val title: String,
    val summary: String,
    val imageResource: Int,
    val level: String,
    val content: String
)
