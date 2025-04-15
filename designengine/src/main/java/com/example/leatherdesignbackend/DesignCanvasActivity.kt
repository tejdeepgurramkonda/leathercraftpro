package com.example.leatherdesignbackend

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.leatherdesignbackend.ui.activities.DesignCanvasActivity as NewDesignCanvasActivity

/**
 * This class is deprecated. Use com.example.leatherdesignbackend.ui.activities.DesignCanvasActivity instead.
 * This is a temporary bridge class that forwards to the new implementation to maintain compatibility.
 */
@Deprecated("Use com.example.leatherdesignbackend.ui.activities.DesignCanvasActivity instead")
class DesignCanvasActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Forward to the new implementation
        val intent = Intent(this, NewDesignCanvasActivity::class.java)
        intent.putExtras(this.intent)
        startActivity(intent)
        finish()
    }
}