package com.example.leatherdesignbackend.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.leatherdesignbackend.data.TimeTrackingSession
import com.example.leatherdesignbackend.databinding.ItemTimeSessionBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class TimeSessionAdapter : ListAdapter<TimeTrackingSession, TimeSessionAdapter.SessionViewHolder>(SessionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val binding = ItemTimeSessionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SessionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SessionViewHolder(private val binding: ItemTimeSessionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(session: TimeTrackingSession) {
            binding.apply {
                // Format date
                val dateFormat = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault())
                sessionDateText.text = dateFormat.format(session.startTime)
                
                // Format duration
                val durationText = if (session.durationMinutes > 0) {
                    formatDuration(session.durationMinutes)
                } else {
                    if (session.isActive) "Active" else "Unknown"
                }
                
                sessionDurationText.text = durationText
                
                // Set step info if available
                if (session.stepId != null) {
                    sessionStepText.text = "Step: [Step name would be here]" // In a real app, we'd get the step name
                    sessionStepText.visibility = View.VISIBLE
                } else {
                    sessionStepText.visibility = View.GONE
                }
                
                // Set notes if available
                if (session.notes.isNotBlank()) {
                    sessionNotesText.text = session.notes
                    sessionNotesText.visibility = View.VISIBLE
                } else {
                    sessionNotesText.visibility = View.GONE
                }
            }
        }
        
        private fun formatDuration(minutes: Int): String {
            val hours = minutes / 60
            val mins = minutes % 60
            
            return if (hours > 0) {
                "${hours}h ${mins}m"
            } else {
                "${mins}m"
            }
        }
    }

    private class SessionDiffCallback : DiffUtil.ItemCallback<TimeTrackingSession>() {
        override fun areItemsTheSame(oldItem: TimeTrackingSession, newItem: TimeTrackingSession): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TimeTrackingSession, newItem: TimeTrackingSession): Boolean {
            return oldItem.startTime == newItem.startTime &&
                   oldItem.endTime == newItem.endTime &&
                   oldItem.durationMinutes == newItem.durationMinutes &&
                   oldItem.notes == newItem.notes &&
                   oldItem.stepId == newItem.stepId
        }
    }
} 