package com.example.leatherdesignbackend.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.leatherdesignbackend.R
import com.example.leatherdesignbackend.data.ExportFormat

class ExportFormatAdapter(
    private val context: Context,
    private val formats: List<ExportFormat>,
    private val onExportClick: (ExportFormat) -> Unit
) : RecyclerView.Adapter<ExportFormatAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val formatIcon: ImageView = view.findViewById(R.id.format_icon)
        val formatName: TextView = view.findViewById(R.id.format_name)
        val formatDescription: TextView = view.findViewById(R.id.format_description)
        val exportButton: ImageButton = view.findViewById(R.id.btn_export)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_export_format, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val format = formats[position]

        holder.formatIcon.setImageResource(format.iconResId)
        holder.formatName.text = format.name
        holder.formatDescription.text = format.description

        holder.exportButton.setOnClickListener {
            onExportClick(format)
        }

        holder.itemView.setOnClickListener {
            onExportClick(format)
        }
    }

    override fun getItemCount() = formats.size
}