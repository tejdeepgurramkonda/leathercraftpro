package com.example.leathercraftpro

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * Main dashboard screen that composes all the dashboard components
 */
@Composable
fun DashboardScreen(
    onRecentDesignClick: (RecentDesign) -> Unit,
    onCategoryClick: (Category) -> Unit
) {
    val context = LocalContext.current
    
    // Sample data for carousel banner
    val carouselItems = remember {
        listOf(
            CarouselItem(imageUrl = "https://example.com/banner1.jpg"),
            CarouselItem(imageUrl = "https://example.com/banner2.jpg"),
            CarouselItem(imageUrl = "https://example.com/banner3.jpg")
        )
    }
    
    // Sample data for recent designs
    val recentDesigns = remember {
        listOf(
            RecentDesign(
                id = "1",
                thumbnailUrl = "https://example.com/design1.jpg",
                title = "Leather Wallet",
                progress = 0.75f,
                lastEdited = "2 hours ago"
            ),
            RecentDesign(
                id = "2",
                thumbnailUrl = "https://example.com/design2.jpg",
                title = "Belt Design",
                progress = 0.40f,
                lastEdited = "Yesterday"
            ),
            RecentDesign(
                id = "3",
                thumbnailUrl = "https://example.com/design3.jpg",
                title = "Custom Bag",
                progress = 0.90f,
                lastEdited = "3 days ago"
            ),
            RecentDesign(
                id = "4",
                thumbnailUrl = "https://example.com/design4.jpg",
                title = "Leather Notebook Cover",
                progress = 0.25f,
                lastEdited = "1 week ago"
            )
        )
    }
    
    // Sample data for categories with leather-inspired colors
    val categories = remember {
        listOf(
            Category(
                id = "Fashion",
                icon = Icons.Default.Watch,
                name = "Fashion Accessories",
                gradientColors = listOf(Color(0xFF8B4513), Color(0xFF5D1A00))
            ),
            Category(
                id = "Clothing",
                icon = Icons.Outlined.Lock,
                name = "Clothing",
                gradientColors = listOf(Color(0xFFD2B48C), Color(0xFFA67C52))
            ),
            Category(
                id = "footwear",
                icon = Icons.Default.IceSkating,
                name = "Footwear",
                gradientColors = listOf(Color(0xFF800020), Color(0xFF4C0000))
            ),
            Category(
                id = "bags&wallets",
                icon = Icons.Default.AccountBalanceWallet,
                name = "Bags & Wallets",
                gradientColors = listOf(Color(0xFF654321), Color(0xFF32210F))
            ),

            Category(
                id = "home&furniture",
                icon = Icons.Default.Chair,
                name = "Home & Furniture",
                gradientColors = listOf(Color(0xFFA67A69), Color(0xFF7A4C45))
            ),

            Category(
                id = "professional",
                icon = Icons.Default.Class,
                name = "Professional Gear",
                gradientColors = listOf(Color(0xFF996515), Color(0xFF664008))
            ),
            Category(
                id = "traditional",
                icon = Icons.Default.Build,
                name = "Traditional",
                gradientColors = listOf(Color(0xFF8FAAA9), Color(0xFF535970))
            ),
            Category(
                id = "others",
                icon = Icons.Default.ShoppingBag,
                name = "Others",
                gradientColors = listOf(Color(0xFF8B5C64), Color(0xFF4B294C))
            )
        )
    }
    
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 56.dp) // Add padding at bottom for bottom navigation
        ) {
            // Top carousel/banner
            CarouselBanner(carouselItems = carouselItems)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Recent designs section
            RecentDesignsSection(
                recentDesigns = recentDesigns,
                onResumeClick = { design ->
                    onRecentDesignClick(design)
                    showToast(context, "Resuming design: ${design.title}")
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Categories grid
            CategoriesGrid(
                categories = categories,
                onCategoryClick = { category ->
                    onCategoryClick(category)
                    showToast(context, "Selected category: ${category.name}")
                }
            )
            
            // Extra space at bottom to ensure content isn't covered by bottom navigation
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Helper function to show a toast message
private fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}