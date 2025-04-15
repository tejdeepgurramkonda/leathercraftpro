package com.example.leatherdesignbackend.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.leatherdesignbackend.data.DesignProject
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ProjectRepository(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "leather_design_projects",
        Context.MODE_PRIVATE
    )
    private val gson = Gson()

    companion object {
        const val KEY_PROJECTS = "projects"
    }

    /**
     * Save a project
     */
    fun saveProject(project: DesignProject) {
        val projects = getAllProjects().toMutableList()
        val existingIndex = projects.indexOfFirst { it.id == project.id }
        
        if (existingIndex >= 0) {
            projects[existingIndex] = project
        } else {
            projects.add(project)
        }
        
        saveAllProjects(projects)
    }

    /**
     * Get a project by ID
     */
    fun getProject(id: String): DesignProject? {
        return getAllProjects().find { it.id == id }
    }

    /**
     * Get all projects
     */
    fun getAllProjects(): List<DesignProject> {
        val projectsJson = sharedPreferences.getString(KEY_PROJECTS, "[]")
        val type = object : TypeToken<List<DesignProject>>() {}.type
        return gson.fromJson(projectsJson, type)
    }

    /**
     * Delete a project
     */
    fun deleteProject(id: String) {
        val projects = getAllProjects().toMutableList()
        projects.removeAll { it.id == id }
        saveAllProjects(projects)
    }

    /**
     * Update a project
     */
    fun updateProject(project: DesignProject) {
        val projects = getAllProjects().toMutableList()
        val index = projects.indexOfFirst { it.id == project.id }
        if (index >= 0) {
            projects[index] = project
            saveAllProjects(projects)
        }
    }

    private fun saveAllProjects(projects: List<DesignProject>) {
        val projectsJson = gson.toJson(projects)
        sharedPreferences.edit().putString(KEY_PROJECTS, projectsJson).apply()
    }
}