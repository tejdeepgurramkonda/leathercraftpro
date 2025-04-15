package com.example.leatherdesignbackend.ui.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.leatherdesignbackend.data.ProgressPhoto
import com.example.leatherdesignbackend.databinding.ItemProgressPhotoBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ProgressPhotoAdapter(
    private val onPhotoClick: (ProgressPhoto) -> Unit,
    private val onMenuClick: (ProgressPhoto, View) -> Unit
) : ListAdapter<ProgressPhoto, ProgressPhotoAdapter.PhotoViewHolder>(PhotoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = ItemProgressPhotoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PhotoViewHolder(private val binding: ItemProgressPhotoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(photo: ProgressPhoto) {
            binding.apply {
                // Load image
                photoImageView.setImageURI(Uri.parse(photo.imageUri))
                
                // Set caption and date
                captionText.text = photo.caption
                captionText.visibility = if (photo.caption.isBlank()) View.GONE else View.VISIBLE
                
                val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                dateText.text = dateFormat.format(photo.captureDate)
                
                // Set step chip if a step is associated
                if (photo.stepId != null) {
                    stepChip.visibility = View.VISIBLE
                    // In a real app, we'd get the step name from the repository
                    stepChip.text = "Step"
                } else {
                    stepChip.visibility = View.GONE
                }
                
                // Set click listeners
                root.setOnClickListener { onPhotoClick(photo) }
                photoMenuButton.setOnClickListener { onMenuClick(photo, it) }
            }
        }
    }

    private class PhotoDiffCallback : DiffUtil.ItemCallback<ProgressPhoto>() {
        override fun areItemsTheSame(oldItem: ProgressPhoto, newItem: ProgressPhoto): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ProgressPhoto, newItem: ProgressPhoto): Boolean {
            return oldItem.imageUri == newItem.imageUri &&
                   oldItem.caption == newItem.caption &&
                   oldItem.stepId == newItem.stepId &&
                   oldItem.captureDate == newItem.captureDate
        }
    }
}