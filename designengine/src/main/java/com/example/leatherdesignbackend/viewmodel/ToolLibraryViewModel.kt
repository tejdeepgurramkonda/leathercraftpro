package com.example.leatherdesignbackend.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.leatherdesignbackend.models.Tool
import com.example.leatherdesignbackend.models.ToolCategory

class ToolLibraryViewModel : ViewModel() {
    // LiveData for search query
    val searchQuery = MutableLiveData<String>("")

    // LiveData for current category filter
    val categoryFilter = MutableLiveData<ToolCategory?>(null)

    // LiveData for filtered tools list
    val filteredTools = MutableLiveData<List<Tool>>(emptyList())

    // Method to set search query
    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    // Method to set category filter
    fun setCategoryFilter(category: ToolCategory?) {
        categoryFilter.value = category
    }

    // Method to update filtered tools
    fun updateFilteredTools(tools: List<Tool>) {
        filteredTools.value = tools
    }
}