package com.example.leathercraftpro

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ProfileActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Enable the back button in the action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle("My Profile")

        // Initialize UI components
        val profileImage = findViewById<ImageView>(R.id.profile_image)
        val nameText = findViewById<TextView>(R.id.profile_name)
        val bioText = findViewById<TextView>(R.id.profile_bio)
        val editProfileButton = findViewById<Button>(R.id.edit_profile_button)

        // Set sample profile data
        nameText.text = "John Craftsworker"
        bioText.text = "Leather craftsman specializing in wallets, bags, and custom designs. 5 years of experience in traditional leather working techniques."



        // Set up button click listener
        editProfileButton.setOnClickListener {
            // Open edit profile activity/dialog
            // This would be implemented in a real app
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
