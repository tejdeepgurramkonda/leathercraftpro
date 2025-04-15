package com.example.leatherdesignbackend.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.leatherdesignbackend.data.WorkflowStep
import com.example.leatherdesignbackend.databinding.ActivityWorkflowBinding
import com.example.leatherdesignbackend.ui.adapters.WorkflowStepAdapter
import com.example.leatherdesignbackend.viewmodel.WorkflowViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import java.text.NumberFormat

class WorkflowActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkflowBinding
    private lateinit var viewModel: WorkflowViewModel
    private lateinit var stepAdapter: WorkflowStepAdapter
    private var projectId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkflowBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize view model
        viewModel = ViewModelProvider(this)[WorkflowViewModel::class.java]
        
        // Get project ID from intent
        projectId = intent.getStringExtra("PROJECT_ID")
        if (projectId == null) {
            finish()
            return
        }
        
        // Setup UI components
        setupStepsList()
        setupAddButton()
        setupTimerFab()
        setupNextStepButton()
        
        // Load project data
        loadProject()
        
        // Observe view model data
        observeViewModel()
    }
    
    private fun setupStepsList() {
        stepAdapter = WorkflowStepAdapter(
            onStepChecked = { step -> viewModel.toggleStepCompletion(step) },
            onViewPhotos = { step -> navigateToPhotos(step) },
            onTrackTime = { step -> navigateToTimeTracker(step) },
            onMenuClicked = { step, view -> showStepOptions(step, view) }
        )
        
        binding.stepsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@WorkflowActivity)
            adapter = stepAdapter
        }
        
        // Add drag-to-reorder functionality
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            0
        ) {
            override fun onMove(
                recyclerView: RecyclerView, 
                source: RecyclerView.ViewHolder, 
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = source.bindingAdapterPosition
                val toPosition = target.bindingAdapterPosition
                
                // Get current steps list
                val steps = viewModel.workflowSteps.value?.toMutableList() ?: return false
                
                // Swap items
                val step = steps[fromPosition]
                steps.removeAt(fromPosition)
                steps.add(toPosition, step)
                
                // Update adapter
                stepAdapter.notifyItemMoved(fromPosition, toPosition)
                
                // Update view model (with debounce to avoid too many updates)
                recyclerView.postDelayed({ 
                    viewModel.reorderWorkflowSteps(steps) 
                }, 500)
                
                return true
            }
            
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Not used
            }
        })
        
        itemTouchHelper.attachToRecyclerView(binding.stepsRecyclerView)
    }
    
    private fun setupAddButton() {
        binding.addStepButton.setOnClickListener {
            showAddStepDialog()
        }
    }
    
    private fun setupTimerFab() {
        binding.timerFab.setOnClickListener {
            navigateToTimeTracker(null)
        }
    }
    
    private fun setupNextStepButton() {
        binding.nextStepButton?.setOnClickListener {
            navigateTo3DPreview()
        }
    }
    
    private fun loadProject() {
        projectId?.let { viewModel.loadProject(it) }
    }
    
    private fun observeViewModel() {
        viewModel.currentProject.observe(this) { project ->
            binding.projectNameText.text = project.name
            binding.projectTypeText.text = "Type: ${project.type}"
        }
        
        viewModel.workflowSteps.observe(this) { steps ->
            stepAdapter.submitList(steps)
            updateEmptyState(steps.isEmpty())
        }
        
        viewModel.workflowProgress.observe(this) { progress ->
            updateProgressUI(progress)
        }
        
        viewModel.isLoading.observe(this) { isLoading ->
            // Could add a loading indicator if needed
        }
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyStateText.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.stepsRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
    
    private fun updateProgressUI(progress: Float) {
        val progressPercentage = (progress * 100).toInt()
        binding.progressIndicator.progress = progressPercentage
        binding.progressText.text = "$progressPercentage% Complete"
    }
    
    private fun showAddStepDialog() {
        val dialogView = layoutInflater.inflate(
            android.R.layout.simple_list_item_2, null
        )
        
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Add Workflow Step")
            .setView(dialogView)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val title = dialogView.findViewById<TextInputEditText>(android.R.id.text1).text.toString()
                val description = dialogView.findViewById<TextInputEditText>(android.R.id.text2).text.toString()
                
                if (title.isNotBlank()) {
                    viewModel.addWorkflowStep(title, description)
                    dialog.dismiss()
                } else {
                    dialogView.findViewById<TextInputEditText>(android.R.id.text1).error = "Title is required"
                }
            }
        }
        
        dialog.show()
    }
    
    private fun showStepOptions(step: WorkflowStep, view: View) {
        val options = arrayOf("Edit", "Delete")
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Step Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditStepDialog(step)
                    1 -> showDeleteConfirmation(step)
                }
            }
            .show()
    }
    
    private fun showEditStepDialog(step: WorkflowStep) {
        val dialogView = layoutInflater.inflate(
            android.R.layout.simple_list_item_2, null
        )
        
        dialogView.findViewById<TextInputEditText>(android.R.id.text1).setText(step.name)
        dialogView.findViewById<TextInputEditText>(android.R.id.text2).setText(step.description)
        
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Edit Workflow Step")
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val newName = dialogView.findViewById<TextInputEditText>(android.R.id.text1).text.toString()
                val newDescription = dialogView.findViewById<TextInputEditText>(android.R.id.text2).text.toString()
                
                if (newName.isNotBlank()) {
                    // Create updated copy of the step
                    val updatedStep = step.copy(
                        name = newName,
                        description = newDescription
                    )
                    
                    // Notify view model
                    viewModel.updateWorkflowStep(updatedStep)
                    dialog.dismiss()
                } else {
                    dialogView.findViewById<TextInputEditText>(android.R.id.text1).error = "Name is required"
                }
            }
        }
        
        dialog.show()
    }
    
    private fun showDeleteConfirmation(step: WorkflowStep) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Step")
            .setMessage("Are you sure you want to delete this step?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.removeWorkflowStep(step)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun navigateToPhotos(step: WorkflowStep? = null) {
        val intent = Intent(this, ProjectGalleryActivity::class.java).apply {
            putExtra("PROJECT_ID", projectId)
            step?.let { putExtra("STEP_ID", it.id) }
        }
        startActivity(intent)
    }
    
    private fun navigateToTimeTracker(step: WorkflowStep? = null) {
        val intent = Intent(this, TimeTrackerActivity::class.java).apply {
            putExtra("PROJECT_ID", projectId)
            step?.let { putExtra("STEP_ID", it.id) }
        }
        startActivity(intent)
    }
    
    private fun navigateTo3DPreview() {
        projectId?.let { id ->
            // Navigate to 3D preview
            val intent = Intent(this, Preview3DActivity::class.java)
            intent.putExtra("PROJECT_ID", id)
            startActivity(intent)
        }
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
} 