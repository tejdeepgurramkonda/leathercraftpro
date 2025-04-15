package com.example.leatherdesignbackend.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.leatherdesignbackend.R
import com.example.leatherdesignbackend.data.DesignProject
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Adapter for displaying design projects in a RecyclerView
 */
class ProjectAdapter(
    private var projects: List<DesignProject>,
    private val onProjectClickListener: (DesignProject) -> Unit
) : RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_project, parent, false)
        return ProjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        val project = projects[position]
        holder.bind(project)
    }

    override fun getItemCount(): Int = projects.size

    /**
     * Update the list of projects and refresh the adapter
     */
    fun updateProjects(newProjects: List<DesignProject>) {
        projects = newProjects
        notifyDataSetChanged()
    }

    inner class ProjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val thumbnailImageView: ImageView = itemView.findViewById(R.id.projectThumbnail)
        private val nameTextView: TextView = itemView.findViewById(R.id.projectName)
        private val dateTextView: TextView = itemView.findViewById(R.id.projectDate)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onProjectClickListener(projects[position])
                }
            }
        }

        fun bind(project: DesignProject) {
            nameTextView.text = project.name
            dateTextView.text = "Created: ${dateFormat.format(project.creationDate)}"

            // Set a placeholder image since DesignProject doesn't have a thumbnail property
            thumbnailImageView.setImageResource(R.drawable.ic_launcher_background)
        }
    }
}