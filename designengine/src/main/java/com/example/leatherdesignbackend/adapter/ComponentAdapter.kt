package com.example.leatherdesignbackend.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.leatherdesignbackend.R
import com.example.leatherdesignbackend.models.ComponentItem

class ComponentAdapter(
    private val context: Context,
    private val components: List<ComponentItem>,
    private val onComponentClick: (ComponentItem) -> Unit
) : RecyclerView.Adapter<ComponentAdapter.ComponentViewHolder>() {

    class ComponentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.component_image)
        val textView: TextView = view.findViewById(R.id.component_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComponentViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_component, parent, false)
        return ComponentViewHolder(view)
    }

    override fun onBindViewHolder(holder: ComponentViewHolder, position: Int) {
        val component = components[position]

        // Set component image
        holder.imageView.setImageResource(component.imageRes)

        // Set component name
        holder.textView.text = component.name

        // Set click listener
        holder.itemView.setOnClickListener {
            onComponentClick(component)
        }
    }

    override fun getItemCount(): Int = components.size
}