package com.example.leatherdesignbackend.ui.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.leatherdesignbackend.R
import com.example.leatherdesignbackend.databinding.ActivityTutorialBinding
import com.example.leatherdesignbackend.models.Tutorial
import com.example.leatherdesignbackend.ui.adapters.TutorialAdapter

/**
 * Activity for displaying leather crafting tutorials
 */
class TutorialActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTutorialBinding
    private lateinit var tutorialAdapter: TutorialAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorialBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Setup RecyclerView
        setupRecyclerView()
        
        // Load tutorial data
        loadTutorialData()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewTutorials.layoutManager = LinearLayoutManager(this)
        tutorialAdapter = TutorialAdapter(emptyList()) { tutorial ->
            // Handle tutorial click
            showTutorialDetails(tutorial)
        }
        binding.recyclerViewTutorials.adapter = tutorialAdapter
    }

    private fun loadTutorialData() {
        // In a real app, this would come from a database or API
        val tutorialList = createSampleTutorialList()
        tutorialAdapter.updateTutorials(tutorialList)
    }

    private fun createSampleTutorialList(): List<Tutorial> {
        return listOf(
            Tutorial(
                "1",
                "Getting Started with Leather Crafting",
                "Learn the basics of leather crafting and essential tools",
                R.drawable.ic_launcher_background,
                "Beginner",
                "This tutorial covers the basic techniques and tools needed to start leather crafting..."
            ),
            Tutorial(
                "2",
                "Cutting Techniques",
                "Master the art of cutting leather cleanly and precisely",
                R.drawable.ic_launcher_background,
                "Beginner",
                "Learn how to use various cutting tools to achieve clean, precise cuts in leather..."
            ),
            Tutorial(
                "3",
                "Stitching Basics",
                "Learn saddle stitching and other essential stitching methods",
                R.drawable.ic_launcher_background,
                "Intermediate",
                "This tutorial covers the traditional saddle stitch technique and other stitching methods..."
            ),
            Tutorial(
                "4",
                "Dyeing and Finishing",
                "Learn how to dye and finish leather projects",
                R.drawable.ic_launcher_background,
                "Intermediate",
                "Discover how to apply dyes, finishes, and edge treatments to your leather projects..."
            ),
            Tutorial(
                "5",
                "Tooling and Stamping",
                "Create decorative patterns using leather stamps and tools",
                R.drawable.ic_launcher_background,
                "Advanced",
                "Learn how to use stamps and tooling techniques to create decorative patterns on leather..."
            )
        )
    }

    private fun showTutorialDetails(tutorial: Tutorial) {
        // In a real implementation, this would navigate to a detail screen
        val dialogBuilder = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(tutorial.title)
            .setMessage(tutorial.content)
            .setPositiveButton("Close", null)
            .create()
            
        dialogBuilder.show()
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
