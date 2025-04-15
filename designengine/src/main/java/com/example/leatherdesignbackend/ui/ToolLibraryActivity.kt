package com.example.leatherdesignbackend.ui

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.leatherdesignbackend.R
import com.example.leatherdesignbackend.adapter.ToolAdapter
import com.example.leatherdesignbackend.models.Tool
import com.example.leatherdesignbackend.models.ToolCategory
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.util.*

class ToolLibraryActivity : AppCompatActivity() {
    private val TAG = "ToolLibraryActivity"

    private lateinit var recyclerView: RecyclerView
    private lateinit var toolAdapter: ToolAdapter
    private lateinit var categoryChipGroup: ChipGroup
    private lateinit var filterButton: ImageButton
    private lateinit var emptyView: TextView
    private var searchView: SearchView? = null
    private lateinit var allTools: List<Tool>
    private var currentFilterCategory: String? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tool_library)
        Log.d(TAG, "onCreate: Initializing ToolLibraryActivity")
        setSupportActionBar(findViewById(R.id.toolbar))


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.tools_recycler_view)
        categoryChipGroup = findViewById(R.id.category_chip_group)
        filterButton = findViewById(R.id.filter_button)
        emptyView = findViewById(R.id.empty_view)

        setupRecyclerView()
        setupCategoryFilter()

        // Handle intent if activity was started with search intent
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent: ${intent.action}")
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            Log.d(TAG, "Search query from intent: $query")
            searchView?.setQuery(query, false)
        }
    }

    private fun setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView: Setting up RecyclerView")
        recyclerView.layoutManager = LinearLayoutManager(this)
        toolAdapter = ToolAdapter(emptyList()) { tool ->
            navigateToToolDetail(tool)
        }
        recyclerView.adapter = toolAdapter
    }

    private fun navigateToToolDetail(tool: Tool) {
        try {
            Log.d(TAG, "navigateToToolDetail: Attempting to navigate to details for tool: ${tool.name}")
            val intent = Intent(this, ToolDetailActivity::class.java)

            // Ensure Tool is Parcelable (already expected in your model)
            intent.putExtra("TOOL", tool)

            // Add debugging information
            Log.d(TAG, "Tool being passed: id=${tool.id}, name=${tool.name}, category=${tool.category}")

            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to tool detail: ${e.message}", e)
            Toast.makeText(this, "Error loading tool details", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun setupCategoryFilter() {
        Log.d(TAG, "setupCategoryFilter: Setting up category filter")
        filterButton.setOnClickListener {
            Log.d(TAG, "Filter button clicked")
            categoryChipGroup.visibility =
                if (categoryChipGroup.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        createCategoryChips()
        loadAllTools()
    }

    private fun createCategoryChips() {
        Log.d(TAG, "createCategoryChips: Creating category chips")
        categoryChipGroup.removeAllViews()

        val allChip = Chip(this).apply {
            text = "All"
            isCheckable = true
            isChecked = true
            tag = null
            setOnClickListener {
                Log.d(TAG, "All chip clicked")
                filterToolsByCategory(null)
                checkOnlyThisChip(this)
            }
        }
        categoryChipGroup.addView(allChip)

        val categories = listOf("Cutting", "Punching", "Stitching", "Finishing", 
                               "Measuring", "Stamping", "Edge Work", "Miscellaneous")
        
        categories.forEach { category ->
            val chip = Chip(this).apply {
                text = category
                isCheckable = true
                tag = category
                setOnClickListener {
                    Log.d(TAG, "Category chip clicked: $category")
                    filterToolsByCategory(category)
                    checkOnlyThisChip(this)
                }
            }
            categoryChipGroup.addView(chip)
        }
    }

    private fun checkOnlyThisChip(selectedChip: Chip) {
        for (i in 0 until categoryChipGroup.childCount) {
            val chip = categoryChipGroup.getChildAt(i) as Chip
            chip.isChecked = chip == selectedChip
        }
    }

    private fun loadAllTools() {
        Log.d(TAG, "loadAllTools: Loading all tools")
        allTools = listOf(
            Tool(id = 1, name = "Leather Awl", description = "Used for punching holes in leather", 
                 category = "Stitching", imageResource = R.drawable.ic_leather_awl),
            Tool(id = 2, name = "Round Knife", description = "For cutting leather with precision", 
                 category = "Cutting", imageResource = R.drawable.ic_round_knife),
            Tool(id = 3, name = "Edge Beveler", description = "Smooths the edges of leather", 
                 category = "Finishing", imageResource = R.drawable.ic_edge_beveler),
            Tool(id = 4, name = "Stitching Chisel", description = "Evenly spaced holes for stitching", 
                 category = "Punching", imageResource = R.drawable.ic_stitching_chisel),
            Tool(id = 5, name = "Stitching Pony", description = "Holds leather while stitching", 
                 category = "Stitching", imageResource = R.drawable.ic_stitching_pony),
            Tool(id = 6, name = "Cutting Mat", description = "Protects work surface", 
                 category = "Cutting", imageResource = R.drawable.ic_cutting_mat),
            Tool(id = 7, name = "Edge Slicker", description = "Burnishes leather edges", 
                 category = "Finishing", imageResource = R.drawable.ic_edge_slicker),
            Tool(id = 8, name = "Hole Punch", description = "Creates clean circular holes", 
                 category = "Punching", imageResource = R.drawable.ic_hole_punch),
            Tool(id = 9, name = "Skiving Knife", description = "Thins edges of leather", 
                 category = "Cutting", imageResource = R.drawable.ic_skiving_knife),
            Tool(id = 10, name = "Leather Mallet", description = "Used with stamps and punches", 
                 category = "Miscellaneous", imageResource = R.drawable.ic_leather_mallet)
        )
        Log.d(TAG, "Loaded ${allTools.size} tools")
        toolAdapter.updateTools(allTools)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        Log.d(TAG, "onCreateOptionsMenu: Creating options menu")
        menuInflater.inflate(R.menu.menu_tool_library, menu)

        val searchItem = menu.findItem(R.id.action_search)
        if (searchItem == null) {
            Log.e(TAG, "Search menu item not found! Check if menu_tool_library.xml exists and has the correct id")
            return true
        }

        searchView = searchItem.actionView as? SearchView
        if (searchView == null) {
            Log.e(TAG, "SearchView is null! Check that SearchView is properly configured")
            return true
        }

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView?.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView?.setIconifiedByDefault(true)

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.d(TAG, "onQueryTextSubmit: $query")
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Log.d(TAG, "onQueryTextChange: $newText")
                val categoryFiltered = if (currentFilterCategory == null) {
                    allTools
                } else {
                    allTools.filter { it.category == currentFilterCategory }
                }

                val filtered = if (newText.isNullOrEmpty()) {
                    categoryFiltered
                } else {
                    categoryFiltered.filter {
                        it.name.contains(newText, true) ||
                                it.description.contains(newText, true) ||
                                it.category.contains(newText, true)
                    }
                }

                Log.d(TAG, "Filtered from ${categoryFiltered.size} to ${filtered.size} tools")
                toolAdapter.updateTools(filtered)

                if (filtered.isEmpty()) {
                    emptyView.visibility = View.VISIBLE
                    emptyView.text = "No tools found matching '$newText'"
                    Log.d(TAG, "Showing empty view: No matches for '$newText'")
                } else {
                    emptyView.visibility = View.GONE
                }

                return true
            }
        })

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                Log.d(TAG, "Search expanded")
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                Log.d(TAG, "Search collapsed")
                filterToolsByCategory(currentFilterCategory)
                return true
            }
        })

        return true
    }

    private fun filterToolsByCategory(category: String?) {
        Log.d(TAG, "filterToolsByCategory: Filtering by ${category ?: "All"}")
        currentFilterCategory = category

        // Only try to get the search query if searchView is initialized
        val query = searchView?.query?.toString().orEmpty()
        Log.d(TAG, "Current search query: '$query'")

        val categoryFiltered = if (category == null) {
            allTools
        } else {
            allTools.filter { it.category == category }
        }
        Log.d(TAG, "Category filtered tools: ${categoryFiltered.size}")

        val filtered = if (query.isNotEmpty()) {
            categoryFiltered.filter {
                it.name.contains(query, true) ||
                        it.description.contains(query, true) ||
                        it.category.contains(query, true)
            }
        } else {
            categoryFiltered
        }
        Log.d(TAG, "Final filtered tools: ${filtered.size}")

        toolAdapter.updateTools(filtered)

        if (filtered.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            emptyView.text = if (query.isNotEmpty()) {
                "No tools found matching '$query'"
            } else {
                "No tools available in this category"
            }
            Log.d(TAG, "Showing empty view: ${emptyView.text}")
        } else {
            emptyView.visibility = View.GONE
            Log.d(TAG, "Hiding empty view")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val query = searchView?.query?.toString() ?: ""
        Log.d(TAG, "onSaveInstanceState: Saving query: '$query' and category: $currentFilterCategory")
        outState.putString("SEARCH_QUERY", query)
        outState.putString("CATEGORY_FILTER", currentFilterCategory)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val savedQuery = savedInstanceState.getString("SEARCH_QUERY")
        Log.d(TAG, "onRestoreInstanceState: Restoring query: '$savedQuery'")

        if (!savedQuery.isNullOrEmpty() && searchView != null) {
            searchView?.setQuery(savedQuery, false)
        }

        val savedCategory = savedInstanceState.getString("CATEGORY_FILTER")
        Log.d(TAG, "onRestoreInstanceState: Restoring category: $savedCategory")

        savedCategory?.let {
            for (i in 0 until categoryChipGroup.childCount) {
                val chip = categoryChipGroup.getChildAt(i) as Chip
                if (chip.tag == it) {
                    chip.isChecked = true
                    break
                }
            }
            currentFilterCategory = it
            filterToolsByCategory(it)
        }
    }
}