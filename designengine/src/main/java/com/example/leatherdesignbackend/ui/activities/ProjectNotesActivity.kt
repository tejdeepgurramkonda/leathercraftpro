package com.example.leatherdesignbackend.ui.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.leatherdesignbackend.R
import com.example.leatherdesignbackend.data.ProjectNote
import com.example.leatherdesignbackend.data.ProjectNote.NoteCategory
import com.example.leatherdesignbackend.databinding.ActivityProjectNotesBinding
import com.example.leatherdesignbackend.ui.adapters.ProjectNoteAdapter
import com.example.leatherdesignbackend.ui.dialogs.AddEditNoteDialog
import com.example.leatherdesignbackend.viewmodel.ProjectNotesViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProjectNotesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProjectNotesBinding
    private val viewModel: ProjectNotesViewModel by viewModels()
    private lateinit var adapter: ProjectNoteAdapter
    private val notesList = mutableListOf<ProjectNote>()
    private var projectId: String = ""
    
    companion object {
        const val EXTRA_PROJECT_ID = "extra_project_id"
        const val EXTRA_PROJECT_NAME = "extra_project_name"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProjectNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Set up toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        
        // Get project info from intent
        projectId = intent.getStringExtra(EXTRA_PROJECT_ID) ?: ""
        if (projectId.isEmpty()) {
            finish()
            return
        }
        
        val projectName = intent.getStringExtra(EXTRA_PROJECT_NAME) ?: getString(R.string.project_notes)
        
        // Set title
        supportActionBar?.title = projectName
        
        // Set up recycler view
        setupRecyclerView()
        
        // Set up category filter spinner
        setupCategorySpinner()
        
        // Set up add note button
        binding.addNoteFab.setOnClickListener {
            showAddNoteDialog()
        }
        
        // Load project data
        viewModel.loadProject(projectId)
        
        // Observe filtered notes
        viewModel.filteredNotes.observe(this) { notes ->
            adapter.submitList(notes)
            
            // Show empty view if no notes
            if (notes.isEmpty()) {
                binding.emptyStateText.visibility = View.VISIBLE
                binding.notesRecyclerView.visibility = View.GONE
            } else {
                binding.emptyStateText.visibility = View.GONE
                binding.notesRecyclerView.visibility = View.VISIBLE
            }
        }
    }
    
    private fun setupRecyclerView() {
        adapter = ProjectNoteAdapter(
            onNoteClick = { position ->
                showNoteDetailsDialog(position)
            },
            onEditClick = { position ->
                showEditNoteDialog(position)
            },
            onDeleteClick = { position ->
                showDeleteConfirmation(position)
            },
            onMenuClick = { view, position ->
                showNoteOptionsMenu(view, position)
            }
        )
        
        binding.notesRecyclerView.layoutManager = LinearLayoutManager(this@ProjectNotesActivity)
        binding.notesRecyclerView.adapter = this@ProjectNotesActivity.adapter
    }
    
    private fun showNoteOptionsMenu(view: View, position: Int) {
        val popup = PopupMenu(this, view)
        popup.inflate(R.menu.menu_note_item)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit -> {
                    showEditNoteDialog(position)
                    true
                }
                R.id.action_delete -> {
                    showDeleteConfirmation(position)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
    
    private fun setupCategorySpinner() {
        // Get all category values plus "All" option
        val categories = NoteCategory.values().toList()
        
        val spinnerItems = mutableListOf<Pair<String?, String>>()
        spinnerItems.add(Pair(null, "All Categories"))
        
        // Add all categories with display names
        categories.forEach { category ->
            val displayName = when(category) {
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
            spinnerItems.add(Pair(category.name, displayName))
        }
        
        // Create adapter with display names only
        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            spinnerItems.map { it.second }
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        
        binding.categoryChipGroup.apply {
            // Setup chip group for filtering
            // Code to setup chips would go here
        }
    }
    
    private fun showAddNoteDialog() {
        val dialog = AddEditNoteDialog(
            context = this,
            onSave = { title, content, category, imageUri ->
                viewModel.addNote(title, content, category, imageUri)
            }
        )
        dialog.show()
    }
    
    private fun showEditNoteDialog(position: Int) {
        val note = adapter.currentList[position]
        val dialog = AddEditNoteDialog(
            context = this,
            note = note,
            onSave = { title, content, category, imageUri ->
                viewModel.updateNote(note.id, title, content, category, imageUri)
            }
        )
        dialog.show()
    }
    
    private fun showNoteDetailsDialog(position: Int) {
        val note = adapter.currentList[position]
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_note_details, null)
        
        dialogView.findViewById<TextView>(R.id.tvNoteTitle).text = note.title
        dialogView.findViewById<TextView>(R.id.tvNoteContent).text = note.content
        dialogView.findViewById<TextView>(R.id.tvNoteDate).text = 
            "Created: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(note.timestamp)}"
        
        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .create()
            .show()
    }
    
    private fun showDeleteConfirmation(position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Delete Note")
            .setMessage("Are you sure you want to delete this note?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteNote(adapter.currentList[position].id)
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_project_notes, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}