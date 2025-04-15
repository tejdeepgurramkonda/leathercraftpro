package com.example.leatherdesignbackend.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.leatherdesignbackend.R
import com.example.leatherdesignbackend.models.Tool

class ToolAdapter(
    private var tools: List<Tool> = emptyList(),
    private val onToolClicked: (Tool) -> Unit
) : RecyclerView.Adapter<ToolAdapter.ToolViewHolder>() {

    class ToolViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.card_view)
        val toolImage: ImageView = itemView.findViewById(R.id.tool_image)
        val toolName: TextView = itemView.findViewById(R.id.tool_name)
        val toolCategory: TextView = itemView.findViewById(R.id.tool_category)
        val toolDescription: TextView = itemView.findViewById(R.id.tool_description)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToolViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tool, parent, false)
        return ToolViewHolder(view)
    }

    override fun onBindViewHolder(holder: ToolViewHolder, position: Int) {
        val tool = tools[position]

        holder.toolImage.setImageResource(tool.imageResource)
        holder.toolName.text = tool.name
        holder.toolCategory.text = tool.category.replaceFirstChar { it.uppercase() }
        holder.toolDescription.text = tool.description

        holder.cardView.setOnClickListener {
            onToolClicked(tool)
        }
    }

    override fun getItemCount(): Int = tools.size

    fun updateTools(newTools: List<Tool>) {
        val diffCallback = ToolDiffCallback(tools, newTools)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        this.tools = newTools
        diffResult.dispatchUpdatesTo(this)
    }

    private class ToolDiffCallback(
        private val oldList: List<Tool>,
        private val newList: List<Tool>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            return oldItem.name == newItem.name &&
                    oldItem.category == newItem.category &&
                    oldItem.description == newItem.description &&
                    oldItem.imageResource == newItem.imageResource
        }
    }
}
