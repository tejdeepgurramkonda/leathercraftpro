package com.example.leatherdesignbackend.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.leatherdesignbackend.R
import com.example.leatherdesignbackend.models.Tool
import com.example.leatherdesignbackend.models.ToolCategory

class ToolDetailActivity : AppCompatActivity() {

    private val TAG = "ToolDetailActivity"

    private lateinit var toolImage: ImageView
    private lateinit var toolName: TextView
    private lateinit var toolCategory: TextView
    private lateinit var toolDescription: TextView
    private lateinit var toolUsage: TextView
    private lateinit var toolUseCases: TextView
    private lateinit var videoButton: Button
    private lateinit var buyButton: Button
    private lateinit var relatedToolsContainer: LinearLayout

    private var toolId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tool_detail)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initViews()

        try {
            val tool = intent.getParcelableExtra<Tool>("TOOL")
            if (tool != null) {
                toolId = tool.id
                Log.d(TAG, "Successfully retrieved tool: ${tool.name}")
                displayToolDetails(tool)
                setupButtonListeners()
                loadRelatedTools()
            } else {
                Log.e(TAG, "Failed to retrieve tool data from intent")
                Toast.makeText(this, "Error loading tool details", Toast.LENGTH_SHORT).show()
                finish()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Unexpected error loading tool", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initViews() {
        toolImage = findViewById(R.id.tool_image)
        toolName = findViewById(R.id.tool_name)
        toolCategory = findViewById(R.id.tool_category)
        toolDescription = findViewById(R.id.tool_description)
        toolUsage = findViewById(R.id.tool_usage)
        toolUseCases = findViewById(R.id.tool_use_cases)
        videoButton = findViewById(R.id.video_button)
        buyButton = findViewById(R.id.buy_button)
        relatedToolsContainer = findViewById(R.id.related_tools_container)
    }

    private fun displayToolDetails(tool: Tool) {
        toolImage.setImageResource(tool.imageResource)
        toolName.text = tool.name
        toolCategory.text = "Category: ${tool.category.replaceFirstChar { it.uppercase() }}"
        toolDescription.text = tool.description
        toolUsage.text = getToolUsageInstructions(tool.id)
        toolUseCases.text = getToolUseCases(tool.id)
        supportActionBar?.title = tool.name
    }

    private fun setupButtonListeners() {
        videoButton.setOnClickListener {
            val videoUrl = when (toolId) {
                1 -> "https://www.youtube.com/results?search_query=leather+awl+tutorial"
                2 -> "https://www.youtube.com/results?search_query=leather+round+knife+tutorial"
                else -> "https://www.youtube.com/results?search_query=leather+crafting+tools"
            }

            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl)))
            } catch (e: Exception) {
                Toast.makeText(this, "Could not open video link", Toast.LENGTH_SHORT).show()
            }
        }

        buyButton.setOnClickListener {
            val storeUrl = when (toolId) {
                1 -> "https://www.amazon.com/s?k=leather+awl"
                2 -> "https://www.amazon.com/s?k=leather+round+knife"
                else -> "https://www.amazon.com/s?k=leather+crafting+tools"
            }

            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(storeUrl)))
            } catch (e: Exception) {
                Toast.makeText(this, "Could not open store link", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadRelatedTools() {
        val relatedTools = getRelatedTools(toolId)
        relatedToolsContainer.removeAllViews()

        for (tool in relatedTools) {
            val toolView = LayoutInflater.from(this)
                .inflate(R.layout.item_related_tool, relatedToolsContainer, false)

            val toolImageView = toolView.findViewById<ImageView>(R.id.related_tool_image)
            val toolNameView = toolView.findViewById<TextView>(R.id.related_tool_name)

            toolImageView.setImageResource(tool.imageResource)
            toolNameView.text = tool.name

            toolView.setOnClickListener {
                val intent = Intent(this, ToolDetailActivity::class.java).apply {
                    putExtra("TOOL", tool) // pass Tool object itself
                }
                startActivity(intent)
            }

            relatedToolsContainer.addView(toolView)
        }
    }

    private fun getToolUsageInstructions(toolId: Int): String {
        return when (toolId) {
            1 -> "1. Hold the leather piece firmly on a work surface.\n2. Position the awl...\n"
            2 -> "1. Place the leather on a cutting mat...\n2. Hold the knife firmly...\n"
            else -> "Usage instructions not available for this tool."
        }
    }

    private fun getToolUseCases(toolId: Int): String {
        return when (toolId) {
            1 -> "• Creating holes for hand stitching\n• Making pilot holes..."
            2 -> "• Cutting patterns\n• Trimming leather pieces..."
            else -> "Use cases not available for this tool."
        }
    }

    private fun getRelatedTools(toolId: Int): List<Tool> {
        return when (toolId) {
            1 -> listOf(
                Tool(
                    id = 5,
                    name = "Stitching Pony",
                    description = "Holds leather pieces together",
                    category = "Stitching",
                    imageResource = R.drawable.ic_launcher_background
                ),
                Tool(
                    id = 6,
                    name = "Stitching Chisel",
                    description = "Creates evenly spaced holes",
                    category = "Punching",
                    imageResource = R.drawable.ic_launcher_background
                )
            )
            2 -> listOf(
                Tool(
                    id = 10,
                    name = "Skiving Knife",
                    description = "Used to thin edges",
                    category = "Cutting",
                    imageResource = R.drawable.ic_launcher_background
                ),
                Tool(
                    id = 3,
                    name = "Edge Beveler",
                    description = "Bevels and smooths edges",
                    category = "Finishing",
                    imageResource = R.drawable.ic_launcher_background
                )
            )
            else -> emptyList()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    // Optional if needed separately
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun String.capitalize(): String {
        return this.lowercase().replaceFirstChar { it.uppercase() }
    }
}
