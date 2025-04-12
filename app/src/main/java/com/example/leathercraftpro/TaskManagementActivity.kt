package com.example.leathercraftpro

import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

class TaskManagementActivity : AppCompatActivity() {

    private lateinit var taskRecyclerView: RecyclerView
    private lateinit var addTaskButton: FloatingActionButton
    private lateinit var taskAdapter: TaskAdapter
    private val taskList = mutableListOf<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_management)

        // Enable the back button in the action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle("Task Management")

        // Initialize UI components
        taskRecyclerView = findViewById(R.id.task_recycler_view)
        addTaskButton = findViewById(R.id.add_task_button)

        // Set up RecyclerView
        taskAdapter = TaskAdapter(taskList,
            onTaskCompleted = { position ->
                taskList[position].isCompleted = !taskList[position].isCompleted
                taskAdapter.notifyItemChanged(position)
            },
            onTaskDeleted = { position ->
                taskList.removeAt(position)
                taskAdapter.notifyItemRemoved(position)
            }
        )

        taskRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@TaskManagementActivity)
            adapter = taskAdapter
        }

        // Add sample tasks
        addSampleTasks()

        // Set up add task button
        addTaskButton.setOnClickListener {
            showAddTaskDialog()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun addSampleTasks() {
        taskList.add(Task("Cut leather for wallet project", "Cut 4 pieces: 2 at 8x4 inches, 2 at 3x4 inches", Date(), false))
        taskList.add(Task("Order new tools", "Need new stitching needles and edge beveler", Date(), false))
        taskList.add(Task("Finish messenger bag", "Add strap and hardware, then polish edges", Date(), false))
        taskAdapter.notifyDataSetChanged()
    }

    private fun showAddTaskDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)
        val titleEditText = dialogView.findViewById<EditText>(R.id.task_title_edit_text)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.task_description_edit_text)
        val saveButton = dialogView.findViewById<Button>(R.id.save_task_button)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancel_button)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        saveButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val description = descriptionEditText.text.toString()

            if (title.isNotEmpty()) {
                val newTask = Task(title, description, Date(), false)
                taskList.add(0, newTask)
                taskAdapter.notifyItemInserted(0)
                taskRecyclerView.scrollToPosition(0)
                dialog.dismiss()
            }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}