package com.example.leatherdesignbackend.ui.activities

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.leatherdesignbackend.data.WorkflowStep
import com.example.leatherdesignbackend.databinding.ActivityTimeTrackerBinding
import com.example.leatherdesignbackend.ui.adapters.TimeSessionAdapter
import com.example.leatherdesignbackend.viewmodel.TimeTrackerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TimeTrackerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTimeTrackerBinding
    private lateinit var viewModel: TimeTrackerViewModel
    private lateinit var sessionAdapter: TimeSessionAdapter
    private var projectId: String? = null
    private var stepId: String? = null
    private val steps = mutableListOf<WorkflowStep>()
    private val stepsDisplay = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimeTrackerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize view model
        viewModel = ViewModelProvider(this)[TimeTrackerViewModel::class.java]
        
        // Get project ID and optional step ID from intent
        projectId = intent.getStringExtra("PROJECT_ID")
        stepId = intent.getStringExtra("STEP_ID")
        
        if (projectId == null) {
            finish()
            return
        }
        
        // Setup UI components
        setupSessionsList()
        setupTimerControls()
        setupStepSelection()
        
        // Load project data
        loadProject()
        
        // Observe view model data
        observeViewModel()
    }
    
    private fun setupSessionsList() {
        sessionAdapter = TimeSessionAdapter()
        
        binding.sessionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@TimeTrackerActivity)
            adapter = sessionAdapter
        }
    }
    
    private fun setupTimerControls() {
        binding.startButton.setOnClickListener {
            val selectedStepId = if (binding.stepSelectionDropdown.text.isNotEmpty()) {
                val selectedPosition = stepsDisplay.indexOf(binding.stepSelectionDropdown.text.toString())
                if (selectedPosition >= 0 && selectedPosition < steps.size) {
                    steps[selectedPosition].id
                } else {
                    null
                }
            } else {
                null
            }
            
            val notes = binding.sessionNotesEditText.text.toString()
            viewModel.startTimeTracking(selectedStepId, notes)
        }
        
        binding.stopButton.setOnClickListener {
            viewModel.stopTimeTracking()
        }
    }
    
    private fun setupStepSelection() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, stepsDisplay)
        binding.stepSelectionDropdown.setAdapter(adapter)
        
        binding.stepSelectionDropdown.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            // Just for handling selection visually, actual selection happens when starting timer
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
        
        viewModel.workflowSteps.observe(this) { workflowSteps ->
            steps.clear()
            steps.addAll(workflowSteps)
            
            stepsDisplay.clear()
            stepsDisplay.add("No specific step")
            stepsDisplay.addAll(workflowSteps.map { "${it.order}. ${it.name}" })
            
            (binding.stepSelectionDropdown.adapter as ArrayAdapter<*>).notifyDataSetChanged()
            
            // Preselect the step if stepId was provided in intent
            stepId?.let { id ->
                val stepIndex = workflowSteps.indexOfFirst { it.id == id }
                if (stepIndex >= 0) {
                    // Add 1 because we added "No specific step" at the beginning
                    binding.stepSelectionDropdown.setText(stepsDisplay[stepIndex + 1], false)
                }
            }
        }
        
        viewModel.timeTrackingSessions.observe(this) { sessions ->
            sessionAdapter.submitList(sessions)
            
            if (sessions.isNotEmpty()) {
                val lastSession = sessions.first()
                val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                binding.lastSessionText.text = dateFormat.format(lastSession.startTime)
            }
        }
        
        viewModel.activeSession.observe(this) { session ->
            val isActive = session != null
            binding.startButton.isEnabled = !isActive
            binding.stopButton.isEnabled = isActive
            binding.stepSelectionLayout.isEnabled = !isActive
            binding.sessionNotesEditText.isEnabled = isActive
            
            // Update notes in real-time
            if (isActive) {
                binding.sessionNotesEditText.setText(session?.notes ?: "")
                binding.sessionNotesEditText.addTextChangedListener(object : android.text.TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    override fun afterTextChanged(s: android.text.Editable?) {
                        s?.toString()?.let { viewModel.updateSessionNotes(it) }
                    }
                })
            } else {
                binding.sessionNotesEditText.removeTextChangedListener(null)
                binding.sessionNotesEditText.setText("")
            }
        }
        
        viewModel.totalTimeSpent.observe(this) { timeMillis ->
            binding.totalTimeText.text = viewModel.getFormattedTime(timeMillis)
        }
        
        viewModel.elapsedTime.observe(this) { timeMillis ->
            binding.timeDisplay.text = viewModel.getFormattedTime(timeMillis)
        }
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    
    override fun onBackPressed() {
        // Check if a timer is active
        if (viewModel.activeSession.value != null) {
            // Ask the user if they want to stop the timer before leaving
            android.app.AlertDialog.Builder(this)
                .setTitle("Timer Running")
                .setMessage("Do you want to stop the timer before leaving?")
                .setPositiveButton("Stop Timer") { _, _ ->
                    viewModel.stopTimeTracking()
                    super.onBackPressed()
                }
                .setNegativeButton("Keep Running") { _, _ ->
                    super.onBackPressed()
                }
                .show()
        } else {
            super.onBackPressed()
        }
    }
} 