package com.example.leatherdesignbackend.ui.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.leatherdesignbackend.data.WorkflowStep
import com.example.leatherdesignbackend.databinding.ItemWorkflowStepBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class WorkflowStepAdapter(
    private val onStepChecked: (WorkflowStep) -> Unit,
    private val onViewPhotos: (WorkflowStep) -> Unit,
    private val onTrackTime: (WorkflowStep) -> Unit,
    private val onMenuClicked: (WorkflowStep, View) -> Unit
) : ListAdapter<WorkflowStep, WorkflowStepAdapter.StepViewHolder>(StepDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepViewHolder {
        val binding = ItemWorkflowStepBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StepViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StepViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class StepViewHolder(private val binding: ItemWorkflowStepBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(step: WorkflowStep) {
            binding.apply {
                // Set step title and description
                stepTitleText.text = step.name
                stepDescriptionText.text = step.description
                
                // Set checkbox state
                stepCheckbox.isChecked = step.isCompleted
                
                // Set completion date if completed
                if (step.isCompleted) {
                    completionDateText.text = "Completed" 
                    completionDateText.visibility = View.VISIBLE
                } else {
                    completionDateText.visibility = View.GONE
                }
                
                // Set estimated time
                if (!step.estimatedTime.isNullOrEmpty()) {
                    estimatedTimeText.text = "Est. time: ${step.estimatedTime}"
                } else {
                    estimatedTimeText.text = "Est. time: Not set"
                }
                
                // Set step image if available - hide for now since imageUri doesn't exist
                stepImageView.visibility = View.GONE
                
                // Show photos button if needed (would actually check if photos exist)
                viewPhotosButton.visibility = View.VISIBLE
                
                // Set click listeners
                stepCheckbox.setOnClickListener {
                    onStepChecked(step)
                }
                
                viewPhotosButton.setOnClickListener {
                    onViewPhotos(step)
                }
                
                trackTimeButton.setOnClickListener {
                    onTrackTime(step)
                }
                
                stepMenuButton.setOnClickListener {
                    onMenuClicked(step, it)
                }
            }
        }
    }

    private class StepDiffCallback : DiffUtil.ItemCallback<WorkflowStep>() {
        override fun areItemsTheSame(oldItem: WorkflowStep, newItem: WorkflowStep): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: WorkflowStep, newItem: WorkflowStep): Boolean {
            return oldItem.name == newItem.name &&
                   oldItem.description == newItem.description &&
                   oldItem.isCompleted == newItem.isCompleted &&
                   oldItem.order == newItem.order &&
                   oldItem.estimatedTime == newItem.estimatedTime
        }
    }
} 