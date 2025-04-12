package com.example.leathercraftpro

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class Task(
    val title: String,
    val description: String,
    val date: Date,
    var isCompleted: Boolean = false
)

class TaskAdapter(
    private val taskList: List<Task>,
    private val onTaskCompleted: (Int) -> Unit,
    private val onTaskDeleted: (Int) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.task_title)
        val descriptionTextView: TextView = view.findViewById(R.id.task_description)
        val dateTextView: TextView = view.findViewById(R.id.task_date)
        val completedCheckBox: CheckBox = view.findViewById(R.id.task_completed)
        val deleteButton: ImageButton = view.findViewById(R.id.delete_task_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]

        holder.titleTextView.text = task.title
        holder.descriptionTextView.text = task.description

        // Format date
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        holder.dateTextView.text = dateFormat.format(task.date)

        // Set completed status
        holder.completedCheckBox.isChecked = task.isCompleted

        // Apply strikethrough to completed tasks
        if (task.isCompleted) {
            holder.titleTextView.paintFlags = holder.titleTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.titleTextView.paintFlags = holder.titleTextView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        // Set listeners
        holder.completedCheckBox.setOnClickListener {
            onTaskCompleted(position)
        }

        holder.deleteButton.setOnClickListener {
            onTaskDeleted(position)
        }
    }

    override fun getItemCount() = taskList.size
}