package com.example.leatherdesignbackend.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.leatherdesignbackend.R
import com.example.leatherdesignbackend.models.Tool
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Repository class for managing leather crafting tools
 */
class ToolRepository(private val context: Context) {
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "tool_repository", Context.MODE_PRIVATE
    )
    private val gson = Gson()
    
    /**
     * Get a tool by ID
     */
    fun getTool(id: Int): Tool? {
        return getDefaultTools().find { it.id == id }
    }
    
    /**
     * Get all tools
     */
    fun getAllTools(): List<Tool> {
        return getDefaultTools()
    }
    
    /**
     * Get tools by category
     */
    fun getToolsByCategory(category: String): List<Tool> {
        return getDefaultTools().filter { it.category == category }
    }
    
    /**
     * Save user tool preferences
     */
    fun saveUserToolPreferences(toolId: Int, preferences: Map<String, Any>) {
        val preferencesJson = gson.toJson(preferences)
        sharedPreferences.edit().putString("tool_prefs_$toolId", preferencesJson).apply()
    }
    
    /**
     * Get user tool preferences
     */
    fun getUserToolPreferences(toolId: Int): Map<String, Any>? {
        val preferencesJson = sharedPreferences.getString("tool_prefs_$toolId", null) ?: return null
        val type = object : TypeToken<Map<String, Any>>() {}.type
        return gson.fromJson(preferencesJson, type)
    }
    
    /**
     * Get default predefined tools
     */
    private fun getDefaultTools(): List<Tool> {
        // In a real app, these would come from a database or API
        return listOf(
            Tool(
                id = 1,
                name = "Round Knife",
                description = "Essential cutting tool with a half-moon blade",
                imageResource = R.drawable.ic_tool_placeholder,
                category = "Cutting",
                skillLevel = "Beginner"
            ),
            Tool(
                id = 2,
                name = "Stitching Chisel",
                description = "Used to create evenly spaced holes for stitching",
                imageResource = R.drawable.ic_tool_placeholder,
                category = "Stitching",
                skillLevel = "Beginner"
            ),
            Tool(
                id = 3,
                name = "Edge Beveler",
                description = "Used to round the edges of leather pieces",
                imageResource = R.drawable.ic_tool_placeholder,
                category = "Edging",
                skillLevel = "Intermediate"
            ),
            Tool(
                id = 4,
                name = "Swivel Knife",
                description = "Used for decorative cutting and tooling",
                imageResource = R.drawable.ic_tool_placeholder,
                category = "Tooling",
                skillLevel = "Intermediate"
            ),
            Tool(
                id = 5,
                name = "Mallet",
                description = "Used with stamps and punches",
                imageResource = R.drawable.ic_tool_placeholder,
                category = "Tooling",
                skillLevel = "Beginner"
            )
        )
    }
} 