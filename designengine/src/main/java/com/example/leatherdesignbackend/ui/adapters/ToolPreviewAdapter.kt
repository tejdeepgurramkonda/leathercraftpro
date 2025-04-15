package com.example.leatherdesignbackend.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.leatherdesignbackend.databinding.ItemToolPreviewBinding
import com.example.leatherdesignbackend.models.Tool

/**
 * Adapter for displaying tools in the project preview
 */
class ToolPreviewAdapter(
    private val tools: List<Tool>
) : RecyclerView.Adapter<ToolPreviewAdapter.ToolViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToolViewHolder {
        val binding = ItemToolPreviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ToolViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ToolViewHolder, position: Int) {
        val tool = tools[position]
        holder.bind(tool)
    }

    override fun getItemCount(): Int = tools.size

    inner class ToolViewHolder(private val binding: ItemToolPreviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(tool: Tool) {
            binding.toolName.text = tool.name
            binding.toolCategory.text = "Category: ${tool.category}"
            binding.toolImage.setImageResource(tool.imageResource)
        }
    }
} 