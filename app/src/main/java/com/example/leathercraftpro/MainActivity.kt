package com.example.leathercraftpro

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.SearchView
import androidx.activity.compose.setContent
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import android.widget.FrameLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.example.leatherdesignbackend.ui.activities.MainActivity as MainAppActivity
import com.example.leatherdesignbackend.ui.activities.DesignCanvasActivity


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var searchView: SearchView
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI components
        drawerLayout = findViewById(R.id.drawer_layout)
        searchView = findViewById(R.id.search_view)
        bottomNav = findViewById(R.id.bottom_navigation)

        // Set up the toolbar
        setSupportActionBar(findViewById(R.id.toolbar))

        // Set up the Navigation Drawer
        toggle = ActionBarDrawerToggle(
            this, drawerLayout, findViewById(R.id.toolbar),
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Set up navigation drawer listeners
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        // Set up bottom navigation
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // We're already at home, so just return true
                    true
                }
                R.id.nav_design -> {
                    startActivity(Intent(this,   com.example.leatherdesignbackend.ui.activities.MainActivity::class.java))
                    true
                }
                R.id.nav_tasks -> {
                    startActivity(Intent(this, TaskManagementActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Set up search view listener
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Handle search query submission
                query?.let { searchDesigns(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Handle search query changes (e.g., real-time search)
                return true
            }
        })

        // Find the fragment container and add our compose view to it
        val fragmentContainer = findViewById<FrameLayout>(R.id.fragment_container)
        
        // Create and add ComposeView to the fragment container
        val composeView = ComposeView(this).apply {
            setContent {
                DashboardScreen(
                    onRecentDesignClick = { recentDesign ->
                        // Navigate to Design Activity with the design ID
                        val intent = Intent(this@MainActivity, com.example.leatherdesignbackend.ui.activities.DesignCanvasActivity::class.java).apply {
                            putExtra("DESIGN_ID", recentDesign.id)
                        }
                        startActivity(intent)
                    },
                    onCategoryClick = { category ->
                        // Navigate to category specific view or filter
                        val intent = Intent(this@MainActivity, com.example.leatherdesignbackend.ui.activities.DesignCanvasActivity::class.java).apply {
                            putExtra("CATEGORY_ID", category.id)
                        }
                        startActivity(intent)
                    }
                )
            }
        }
        
        // Add the ComposeView to the fragment container
        fragmentContainer.addView(composeView)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                // We're already at home
            }
            R.id.nav_design -> {
                startActivity(Intent(this, com.example.leatherdesignbackend.ui.activities.DesignCanvasActivity::class.java))
            }
            R.id.nav_tasks -> {
                startActivity(Intent(this, TaskManagementActivity::class.java))
            }
            R.id.nav_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            R.id.nav_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun searchDesigns(query: String) {
        // Implementation for searching designs
        // You could start a new activity or show results in the current one
    }
}