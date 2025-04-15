package com.example.leatherdesignbackend.ui.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.leatherdesignbackend.R
import com.example.leatherdesignbackend.data.ProjectNote
import com.example.leatherdesignbackend.data.ProjectNote.NoteCategory
import com.example.leatherdesignbackend.databinding.DialogAddEditNoteBinding

class AddEditNoteDialog(
    context: Context,
    private val note: ProjectNote? = null,
    private val onSave: (title: String, content: String, category: NoteCategory, imageUri: String?) -> Unit
) : Dialog(context) {

    private lateinit var binding: DialogAddEditNoteBinding
    private var imageUri: String? = null
    private val activity = context as AppCompatActivity
    
    // Image selection result launcher
    private val imagePickerLauncher = activity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val uri = result.data?.data
            uri?.let { selectedUri ->
                // Get persistent permission for the URI
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(selectedUri, takeFlags)
                
                // Save the URI
                imageUri = selectedUri.toString()
                
                // Display the image
                updateImagePreview()
                
                // Show remove button
                binding.removeImageButton.visibility = View.VISIBLE
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        
        binding = DialogAddEditNoteBinding.inflate(activity.layoutInflater)
        setContentView(binding.root)
        
        // Setup dialog size
        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.9).toInt(),
            (context.resources.displayMetrics.heightPixels * 0.8).toInt()
        )
        
        // Set dialog style
        window?.setWindowAnimations(R.style.Dialog_Animation)
        
        setupUI()
        setupCategoryInput()
        setupImageControls()
        
        // If editing, populate fields with existing note data
        note?.let {
            populateFields(it)
        }
    }
    
    private fun setupUI() {
        binding.dialogTitle.text = if (note != null) "Edit Note" else "Add Note"
        
        binding.closeButton.setOnClickListener {
            dismiss()
        }
        
        binding.saveButton.setOnClickListener {
            saveNote()
        }
        
        binding.cancelButton.setOnClickListener {
            dismiss()
        }
    }
    
    private fun setupCategoryInput() {
        // Get all category values
        val categories = NoteCategory.values().toList()
        
        // Create list of category names
        val categoryNames = categories.map { getCategoryDisplayName(it) }
        
        // Set categories in the category input
        binding.categoryEditText.setText(categoryNames.first())
    }
    
    private fun setupImageControls() {
        binding.selectImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "image/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            imagePickerLauncher.launch(intent)
        }
        
        binding.removeImageButton.setOnClickListener {
            imageUri = null
            binding.imagePreview.setImageResource(R.drawable.ic_image_placeholder)
            binding.removeImageButton.visibility = View.GONE
            binding.imagePreview.visibility = View.GONE
        }
        
        // Initially hide remove button
        binding.removeImageButton.visibility = View.GONE
    }
    
    private fun populateFields(note: ProjectNote) {
        binding.titleEditText.setText(note.title)
        binding.contentEditText.setText(note.content)
        binding.categoryEditText.setText(getCategoryDisplayName(note.category))
        
        // Set image if available
        imageUri = note.imageUri
        updateImagePreview()
    }
    
    private fun updateImagePreview() {
        if (imageUri != null) {
            try {
                binding.removeImageButton.visibility = View.VISIBLE
                binding.imagePreview.visibility = View.VISIBLE
                Glide.with(context)
                    .load(Uri.parse(imageUri))
                    .into(binding.imagePreview)
            } catch (e: Exception) {
                e.printStackTrace()
                binding.imagePreview.setImageResource(R.drawable.ic_image_placeholder)
            }
        } else {
            binding.removeImageButton.visibility = View.GONE
            binding.imagePreview.visibility = View.GONE
        }
    }
    
    private fun saveNote() {
        val title = binding.titleEditText.text.toString().trim()
        val content = binding.contentEditText.text.toString().trim()
        val categoryText = binding.categoryEditText.text.toString().trim()
        
        // Validate input
        if (title.isEmpty()) {
            binding.titleEditText.error = "Title is required"
            return
        }
        
        if (content.isEmpty()) {
            binding.contentEditText.error = "Content is required"
            return
        }
        
        // Get category from text
        val category = getCategoryFromDisplayName(categoryText)
        
        // Call save callback
        onSave(title, content, category, imageUri)
        dismiss()
    }
    
    private fun getCategoryDisplayName(category: NoteCategory): String {
        return when(category) {
            NoteCategory.GENERAL -> "General"
            NoteCategory.MATERIAL -> "Material"
            NoteCategory.MEASUREMENT -> "Measurement"
            NoteCategory.TECHNIQUE -> "Technique"
            NoteCategory.DESIGN_IDEA -> "Design Idea"
            NoteCategory.REFERENCE -> "Reference"
            NoteCategory.TIP -> "Tip"
            NoteCategory.MISTAKE -> "Mistake"
            NoteCategory.IMPROVEMENT -> "Improvement"
            NoteCategory.OTHER -> "Other"
        }
    }
    
    private fun getCategoryFromDisplayName(displayName: String): NoteCategory {
        return when(displayName) {
            "General" -> NoteCategory.GENERAL
            "Material" -> NoteCategory.MATERIAL
            "Measurement" -> NoteCategory.MEASUREMENT
            "Technique" -> NoteCategory.TECHNIQUE
            "Design Idea" -> NoteCategory.DESIGN_IDEA
            "Reference" -> NoteCategory.REFERENCE
            "Tip" -> NoteCategory.TIP
            "Mistake" -> NoteCategory.MISTAKE
            "Improvement" -> NoteCategory.IMPROVEMENT
            else -> NoteCategory.OTHER
        }
    }
}