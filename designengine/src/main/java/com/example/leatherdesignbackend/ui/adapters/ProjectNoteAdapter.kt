package com.example.leatherdesignbackend.ui.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.leatherdesignbackend.data.ProjectNote
import com.example.leatherdesignbackend.databinding.ItemProjectNoteBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ProjectNoteAdapter(
    private val onNoteClick: (Int) -> Unit,
    private val onEditClick: (Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit,
    private val onMenuClick: (View, Int) -> Unit
) : ListAdapter<ProjectNote, ProjectNoteAdapter.NoteViewHolder>(NoteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemProjectNoteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class NoteViewHolder(private val binding: ItemProjectNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(note: ProjectNote, position: Int) {
            // Set title and content
            binding.tvNoteTitle.text = note.title
            binding.tvNoteContent.text = note.content
            
            // Format date
            val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            binding.tvNoteDate.text = dateFormat.format(note.timestamp)
            
            // Set click listeners
            binding.root.setOnClickListener { onNoteClick(position) }
            binding.ivNoteMenu.setOnClickListener { onMenuClick(it, position) }
        }
        
        private fun getCategoryDisplayName(category: ProjectNote.NoteCategory): String {
            return when (category) {
                ProjectNote.NoteCategory.GENERAL -> "General"
                ProjectNote.NoteCategory.TIP -> "Tip"
                ProjectNote.NoteCategory.MISTAKE -> "Mistake"
                ProjectNote.NoteCategory.IMPROVEMENT -> "Improvement"
                ProjectNote.NoteCategory.MATERIAL -> "Material"
                ProjectNote.NoteCategory.MEASUREMENT -> "Measurement"
                ProjectNote.NoteCategory.TECHNIQUE -> "Technique" 
                ProjectNote.NoteCategory.DESIGN_IDEA -> "Design Idea"
                ProjectNote.NoteCategory.REFERENCE -> "Reference"
                ProjectNote.NoteCategory.OTHER -> "Other"
            }
        }
    }

    private class NoteDiffCallback : DiffUtil.ItemCallback<ProjectNote>() {
        override fun areItemsTheSame(oldItem: ProjectNote, newItem: ProjectNote): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ProjectNote, newItem: ProjectNote): Boolean {
            return oldItem.title == newItem.title &&
                   oldItem.content == newItem.content &&
                   oldItem.category == newItem.category &&
                   oldItem.imageUri == newItem.imageUri &&
                   oldItem.timestamp == newItem.timestamp
        }
    }
}