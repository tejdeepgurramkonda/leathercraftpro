package com.example.leatherdesignbackend.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.leatherdesignbackend.R
import com.example.leatherdesignbackend.models.LeatherTool

/**
 * Adapter for displaying leather tools in a RecyclerView
 */
class ToolAdapter(
    private var tools: List<LeatherTool>,
    private val onToolClick: (LeatherTool) -> Unit
) : RecyclerView.Adapter<ToolAdapter.ToolViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToolViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tool, parent, false)
        return ToolViewHolder(view)
    }

    override fun onBindViewHolder(holder: ToolViewHolder, position: Int) {
        val tool = tools[position]
        holder.bind(tool)
    }

    override fun getItemCount(): Int = tools.size

    fun updateTools(newTools: List<LeatherTool>) {
        this.tools = newTools
        notifyDataSetChanged()
    }

    inner class ToolViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val toolImage: ImageView = itemView.findViewById(R.id.tool_image)
        private val toolName: TextView = itemView.findViewById(R.id.tool_name)
        private val toolCategory: TextView = itemView.findViewById(R.id.tool_category)
        private val toolDescription: TextView = itemView.findViewById(R.id.tool_description)

        fun bind(tool: LeatherTool) {
            // Set data to views
            toolImage.setImageResource(tool.imageResource)
            toolName.text = tool.name
            toolCategory.text = tool.category
            toolDescription.text = tool.shortDescription

            // Set click listener
            itemView.setOnClickListener {
                onToolClick(tool)
            }
        }
    }
}
