package com.example.leatherdesignbackend.ui.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.leatherdesignbackend.data.WorkflowStep
import com.example.leatherdesignbackend.databinding.ActivityProjectGalleryBinding
import com.example.leatherdesignbackend.ui.adapters.ProgressPhotoAdapter
import com.example.leatherdesignbackend.viewmodel.ProjectGalleryViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class ProjectGalleryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProjectGalleryBinding
    private lateinit var viewModel: ProjectGalleryViewModel
    private lateinit var photoAdapter: ProgressPhotoAdapter
    private var projectId: String? = null
    private var stepId: String? = null
    
    private val imagePicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                showAddCaptionDialog(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProjectGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize view model
        viewModel = ViewModelProvider(this)[ProjectGalleryViewModel::class.java]
        
        // Get project ID and optional step ID from intent
        projectId = intent.getStringExtra("PROJECT_ID")
        stepId = intent.getStringExtra("STEP_ID")
        
        if (projectId == null) {
            finish()
            return
        }
        
        // Setup UI components
        setupPhotoGrid()
        setupAddButton()
        
        // Load project data
        loadProject()
        
        // Observe view model data
        observeViewModel()
    }
    
    private fun setupPhotoGrid() {
        photoAdapter = ProgressPhotoAdapter(
            onPhotoClick = { photo ->
                // Show photo in full screen viewer
            },
            onMenuClick = { photo, view ->
                showPhotoOptions(photo)
            }
        )
        
        binding.photosRecyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = photoAdapter
        }
    }
    
    private fun setupAddButton() {
        binding.addPhotoFab.setOnClickListener {
            openImagePicker()
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
            setupStepFilters(steps)
        }
        
        viewModel.photos.observe(this) { photos ->
            // Apply step filter if needed
            val filteredPhotos = if (stepId != null) {
                photos.filterTo(ArrayList()) { photo -> photo.stepId == stepId }
            } else {
                photos
            }
            
            photoAdapter.submitList(filteredPhotos)
            updateEmptyState(filteredPhotos.isNullOrEmpty())
        }
    }
    
    private fun setupStepFilters(steps: List<WorkflowStep>) {
        // Clear existing step chips (except the "All Photos" chip)
        val chipGroup = binding.stepFilterChipGroup
        val allPhotosChip = binding.allPhotosChip
        chipGroup.removeAllViews()
        chipGroup.addView(allPhotosChip)
        
        // Add chip for each step
        steps.forEach { step ->
            val chip = Chip(this).apply {
                text = step.name
                isCheckable = true
                id = View.generateViewId()
                
                // Check this chip if it matches the stepId from intent
                isChecked = step.id == stepId
            }
            
            chipGroup.addView(chip)
            
            // Handle chip click
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // Filter photos by this step
                    filterPhotosByStep(step.id)
                }
            }
        }
        
        // Handle "All Photos" chip
        allPhotosChip.isChecked = stepId == null
        allPhotosChip.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Show all photos
                filterPhotosByStep(null)
            }
        }
    }
    
    private fun filterPhotosByStep(filteredStepId: String?) {
        stepId = filteredStepId
        
        // Filter the photos
        viewModel.photos.value?.let { photos ->
            val filteredPhotos = if (filteredStepId != null) {
                photos.filterTo(ArrayList()) { photo -> photo.stepId == filteredStepId }
            } else {
                photos
            }
            
            photoAdapter.submitList(filteredPhotos)
            updateEmptyState(filteredPhotos.isNullOrEmpty())
        }
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyStateText.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.photosRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
    
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePicker.launch(intent)
    }
    
    private fun showAddCaptionDialog(imageUri: Uri) {
        val dialogView = layoutInflater.inflate(
            android.R.layout.simple_list_item_1, null
        )
        
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Add Photo Caption")
            .setView(dialogView)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val caption = dialogView.findViewById<TextInputEditText>(android.R.id.text1).text.toString()
                viewModel.addProgressPhoto(imageUri, caption, stepId)
                dialog.dismiss()
            }
        }
        
        dialog.show()
    }
    
    private fun showPhotoOptions(photo: com.example.leatherdesignbackend.data.ProgressPhoto) {
        val options = arrayOf("Edit Caption", "Delete")
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Photo Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditCaptionDialog(photo)
                    1 -> showDeleteConfirmation(photo)
                }
            }
            .show()
    }
    
    private fun showEditCaptionDialog(photo: com.example.leatherdesignbackend.data.ProgressPhoto) {
        val dialogView = layoutInflater.inflate(
            android.R.layout.simple_list_item_1, null
        )
        
        dialogView.findViewById<TextInputEditText>(android.R.id.text1).setText(photo.caption)
        
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Edit Caption")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newCaption = dialogView.findViewById<TextInputEditText>(android.R.id.text1).text.toString()
                viewModel.updatePhotoCaption(photo, newCaption)
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.show()
    }
    
    private fun showDeleteConfirmation(photo: com.example.leatherdesignbackend.data.ProgressPhoto) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Photo")
            .setMessage("Are you sure you want to delete this photo?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.removeProgressPhoto(photo)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
} 