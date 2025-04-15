package com.example.leatherdesignbackend.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.leatherdesignbackend.data.DesignProject
import com.example.leatherdesignbackend.databinding.ItemSavedProjectBinding

/**
 * Adapter for displaying saved projects in a RecyclerView
 * Includes action buttons for viewing tools, editing, and designing
 */
class SavedProjectAdapter(
    private val onViewTools: (DesignProject) -> Unit,
    private val onEditProject: (DesignProject) -> Unit,
    private val onDesignProject: (DesignProject) -> Unit,
    private val onWorkflowProject: ((DesignProject) -> Unit)? = null
) : RecyclerView.Adapter<SavedProjectAdapter.ProjectViewHolder>() {

    private var projects = listOf<DesignProject>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val binding = ItemSavedProjectBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProjectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        val project = projects[position]
        holder.bind(project)
    }

    override fun getItemCount(): Int = projects.size

    /**
     * Update the list of projects displayed in the adapter
     */
    fun updateProjects(newProjects: List<DesignProject>) {
        projects = newProjects
        notifyDataSetChanged()
    }

    inner class ProjectViewHolder(private val binding: ItemSavedProjectBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(project: DesignProject) {
            // Set project details
            binding.projectNameText.text = project.name
            binding.projectTypeText.text = "Type: ${project.type}"
            
            // Set dimensions if available
            binding.projectDimensionsText.text = if (project.width > 0 && project.height > 0) {
                "Dimensions: ${project.width}cm x ${project.height}cm"
            } else {
                "Dimensions: Not specified"
            }
            
            // Set tools count (in a real app, this would be from a project-tools relationship)
            binding.projectToolsCount.text = "5 Tools" // Placeholder
            
            // Set action button listeners
            binding.viewToolsButton.setOnClickListener { onViewTools(project) }
            binding.editProjectButton.setOnClickListener { onEditProject(project) }
            binding.designButton.setOnClickListener { onDesignProject(project) }
            
            // Make the entire card clickable
            binding.root.setOnClickListener { onViewTools(project) }
        }
    }
} 