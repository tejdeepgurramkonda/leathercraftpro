package com.example.leatherdesignbackend.ui.activities

import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.leatherdesignbackend.databinding.ActivityToolLibraryBinding
import com.example.leatherdesignbackend.models.LeatherTool
import com.example.leatherdesignbackend.ui.adapters.ToolAdapter
import com.example.leatherdesignbackend.R

/**
 * Activity for displaying and browsing leather crafting tools
 */
class ToolLibraryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityToolLibraryBinding
    private lateinit var toolAdapter: ToolAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityToolLibraryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize the RecyclerView
        setupRecyclerView()
        
        // Load tools data
        loadToolsData()
    }

    private fun setupRecyclerView() {
        binding.toolsRecyclerView.layoutManager = LinearLayoutManager(this)
        toolAdapter = ToolAdapter(emptyList()) { tool ->
            // Handle tool click - show tool details
            showToolDetails(tool)
        }
        binding.toolsRecyclerView.adapter = toolAdapter
    }

    private fun loadToolsData() {
        // In a real app, this would come from a database or API
        val toolsList = createSampleToolsList()
        toolAdapter.updateTools(toolsList)
    }

    private fun createSampleToolsList(): List<LeatherTool> {
        return listOf(
            LeatherTool(
                name = "Round Knife",
                description = "Essential cutting tool with a half-moon blade",
                imageResource = R.drawable.ic_tool_placeholder,
                shortDescription = "Used for cutting straight lines and curves in leather",
                category = "Cutting",
                skillLevel = "Beginner"
            ),
            LeatherTool(
                name = "Stitching Chisel",
                description = "Used to create evenly spaced holes for stitching",
                imageResource = R.drawable.ic_tool_placeholder,
                shortDescription = "Available in different prong configurations (2, 4, 6 prongs)",
                category = "Stitching",
                skillLevel = "Beginner"
            ),
            LeatherTool(
                name = "Edge Beveler",
                description = "Used to round the edges of leather pieces",
                imageResource = R.drawable.ic_tool_placeholder,
                shortDescription = "Available in different sizes for various leather thicknesses",
                category = "Edging",
                skillLevel = "Intermediate"
            ),
            LeatherTool(
                name = "Swivel Knife",
                description = "Used for decorative cutting and tooling",
                imageResource = R.drawable.ic_tool_placeholder,
                shortDescription = "The blade rotates freely, allowing for smooth curves",
                category = "Tooling",
                skillLevel = "Intermediate"
            ),
            LeatherTool(
                name = "Mallet",
                description = "Used with stamps and punches",
                imageResource = R.drawable.ic_tool_placeholder,
                shortDescription = "Wooden or rawhide mallets are preferred for leatherwork",
                category = "Tooling",
                skillLevel = "Beginner"
            ),
            LeatherTool(
                name = "Leather Stamps",
                description = "Used to create patterns and textures",
                imageResource = R.drawable.ic_tool_placeholder,
                shortDescription = "Available in hundreds of designs",
                category = "Tooling",
                skillLevel = "Intermediate"
            ),
            LeatherTool(
                name = "Edge Slicker",
                description = "Used to burnish and finish edges",
                imageResource = R.drawable.ic_tool_placeholder,
                shortDescription = "Can be wood, glass, or plastic",
                category = "Finishing",
                skillLevel = "Intermediate"
            ),
            LeatherTool(
                name = "Awl",
                description = "Used for marking and creating pilot holes",
                imageResource = R.drawable.ic_tool_placeholder,
                shortDescription = "Essential for precision work",
                category = "General",
                skillLevel = "Beginner"
            )
        )
    }

    private fun showToolDetails(tool: LeatherTool) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_tool_details, null)
        
        // Set the views in the dialog with tool data
        dialogView.findViewById<ImageView>(R.id.imageToolIcon).setImageResource(tool.imageResource)
        dialogView.findViewById<TextView>(R.id.textToolName).text = tool.name
        dialogView.findViewById<TextView>(R.id.textToolDescription).text = tool.shortDescription
        dialogView.findViewById<TextView>(R.id.textToolCategory).text = "Category: ${tool.category}"
        dialogView.findViewById<TextView>(R.id.textToolSkillLevel).text = "Skill Level: ${tool.skillLevel}"
        
        // Show dialog
        AlertDialog.Builder(this)
            .setTitle(tool.name)
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .create()
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
