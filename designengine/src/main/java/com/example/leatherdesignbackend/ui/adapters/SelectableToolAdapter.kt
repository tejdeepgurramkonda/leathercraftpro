package com.example.leatherdesignbackend.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.leatherdesignbackend.databinding.ItemToolSelectableBinding
import com.example.leatherdesignbackend.models.Tool

/**
 * Adapter for displaying selectable tools in a RecyclerView
 * Allows selecting tools for a leather project
 */
class SelectableToolAdapter(
    private var tools: List<Tool>,
    private val onToolSelected: (Tool, Boolean) -> Unit
) : RecyclerView.Adapter<SelectableToolAdapter.ToolViewHolder>() {

    // Track selected state for each tool
    private val selectedTools = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToolViewHolder {
        val binding = ItemToolSelectableBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ToolViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ToolViewHolder, position: Int) {
        val tool = tools[position]
        holder.bind(tool, selectedTools.contains(tool.id))
    }

    override fun getItemCount(): Int = tools.size

    /**
     * Update the list of tools displayed in the adapter
     */
    fun updateTools(newTools: List<Tool>) {
        tools = newTools
        notifyDataSetChanged()
    }

    inner class ToolViewHolder(private val binding: ItemToolSelectableBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(tool: Tool, isSelected: Boolean) {
            // Set tool details
            binding.toolName.text = tool.name
            binding.toolCategory.text = "Category: ${tool.category}"
            binding.toolDescription.text = tool.description
            binding.toolImage.setImageResource(tool.imageResource)

            // Set checkbox state without triggering the listener
            binding.toolCheckbox.setOnCheckedChangeListener(null)
            binding.toolCheckbox.isChecked = isSelected
            
            // Setup click listeners
            binding.toolCheckbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedTools.add(tool.id)
                } else {
                    selectedTools.remove(tool.id)
                }
                onToolSelected(tool, isChecked)
            }
            
            // Make the entire item clickable to toggle selection
            binding.root.setOnClickListener {
                val newState = !binding.toolCheckbox.isChecked
                binding.toolCheckbox.isChecked = newState
                // Listener above will handle the rest
            }
        }
    }
} 