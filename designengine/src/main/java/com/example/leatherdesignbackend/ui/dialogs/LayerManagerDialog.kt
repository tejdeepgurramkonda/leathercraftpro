package com.example.leatherdesignbackend.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.leatherdesignbackend.R
import com.example.leatherdesignbackend.data.DesignLayer
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

/**
 * Dialog for managing layers in a design project
 * Allows adding, removing, reordering, and toggling visibility of layers
 */
class LayerManagerDialog(context: Context, private val layers: MutableList<DesignLayer>) : Dialog(context) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LayerAdapter
    private lateinit var btnAddLayer: MaterialButton
    
    // Callback for when layers are modified
    private var onLayersModifiedListener: ((List<DesignLayer>, DesignLayer?) -> Unit)? = null
    
    // Currently selected layer
    private var selectedLayer: DesignLayer? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_layer_manager)
        
        // Set dialog title
        setTitle("Manage Layers")
        
        // Initialize views
        recyclerView = findViewById(R.id.layersRecyclerView)
        btnAddLayer = findViewById(R.id.btnAddLayer)
        
        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = LayerAdapter(layers)
        recyclerView.adapter = adapter
        
        // Setup drag and drop for reordering
        val itemTouchHelper = ItemTouchHelper(LayerReorderCallback())
        itemTouchHelper.attachToRecyclerView(recyclerView)
        
        // Setup add layer button
        btnAddLayer.setOnClickListener {
            showAddLayerDialog()
        }
    }
    
    /**
     * Set the listener for layer modifications
     */
    fun setOnLayersModifiedListener(listener: (List<DesignLayer>, DesignLayer?) -> Unit) {
        onLayersModifiedListener = listener
    }
    
    /**
     * Set the currently selected layer
     */
    fun setSelectedLayer(layer: DesignLayer?) {
        selectedLayer = layer
        adapter.notifyDataSetChanged()
    }
    
    /**
     * Show dialog to add a new layer
     */
    private fun showAddLayerDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_layer, null)
        val nameEditText = dialogView.findViewById<TextInputEditText>(R.id.layerNameEditText)
        
        MaterialAlertDialogBuilder(context)
            .setTitle("Add New Layer")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val layerName = nameEditText.text.toString()
                if (layerName.isNotEmpty()) {
                    val newLayer = DesignLayer(name = layerName, position = layers.size)
                    layers.add(0, newLayer) // Add to top of stack
                    adapter.notifyItemInserted(0)
                    
                    // Update positions
                    updateLayerPositions()
                    
                    // Notify listener
                    onLayersModifiedListener?.invoke(layers, newLayer)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    /**
     * Update the position values of all layers based on their order in the list
     */
    private fun updateLayerPositions() {
        layers.forEachIndexed { index, layer ->
            layer.position = layers.size - index - 1 // Reverse index for z-order
        }
    }
    
    /**
     * Adapter for the layers RecyclerView
     */
    inner class LayerAdapter(private val layers: MutableList<DesignLayer>) : 
            RecyclerView.Adapter<LayerAdapter.LayerViewHolder>() {
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LayerViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_layer, parent, false)
            return LayerViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: LayerViewHolder, position: Int) {
            val layer = layers[position]
            holder.bind(layer)
        }
        
        override fun getItemCount(): Int = layers.size
        
        /**
         * Move a layer from one position to another
         */
        fun moveItem(fromPosition: Int, toPosition: Int) {
            if (fromPosition < toPosition) {
                for (i in fromPosition until toPosition) {
                    layers[i] = layers.set(i + 1, layers[i])
                }
            } else {
                for (i in fromPosition downTo toPosition + 1) {
                    layers[i] = layers.set(i - 1, layers[i])
                }
            }
            notifyItemMoved(fromPosition, toPosition)
            
            // Update positions
            updateLayerPositions()
            
            // Notify listener
            onLayersModifiedListener?.invoke(layers, selectedLayer)
        }
        
        inner class LayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val layerNameTextView: TextView = itemView.findViewById(R.id.layerName)
            private val visibilityButton: ImageButton = itemView.findViewById(R.id.btnToggleVisibility)
            private val lockButton: ImageButton = itemView.findViewById(R.id.btnToggleLock)
            private val deleteButton: ImageButton = itemView.findViewById(R.id.btnDeleteLayer)
            private val selectionIndicator: ImageView = itemView.findViewById(R.id.layerSelectedIndicator)
            
            init {
                // Set click listener for selecting a layer
                itemView.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val layer = layers[position]
                        selectedLayer = layer
                        notifyDataSetChanged()
                        onLayersModifiedListener?.invoke(layers, layer)
                    }
                }
                
                // Set click listener for visibility toggle
                visibilityButton.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val layer = layers[position]
                        layer.toggleVisibility()
                        notifyItemChanged(position)
                        onLayersModifiedListener?.invoke(layers, selectedLayer)
                    }
                }
                
                // Set click listener for lock toggle
                lockButton.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val layer = layers[position]
                        layer.toggleLock()
                        notifyItemChanged(position)
                        onLayersModifiedListener?.invoke(layers, selectedLayer)
                    }
                }
                
                // Set click listener for delete
                deleteButton.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        showDeleteLayerConfirmation(position)
                    }
                }
            }
            
            fun bind(layer: DesignLayer) {
                layerNameTextView.text = layer.name
                
                // Set visibility icon
                visibilityButton.setImageResource(
                    if (layer.visible) R.drawable.ic_visibility_on
                    else R.drawable.ic_visibility_off
                )
                
                // Set lock icon
                lockButton.setImageResource(
                    if (layer.locked) R.drawable.ic_lock
                    else R.drawable.ic_unlock
                )
                
                // Show selection indicator if this is the selected layer
                selectionIndicator.visibility = if (layer == selectedLayer) View.VISIBLE else View.INVISIBLE
            }
            
            /**
             * Show confirmation dialog before deleting a layer
             */
            private fun showDeleteLayerConfirmation(position: Int) {
                val layer = layers[position]
                
                AlertDialog.Builder(context)
                    .setTitle("Delete Layer")
                    .setMessage("Are you sure you want to delete the layer '${layer.name}'?")
                    .setPositiveButton("Delete") { _, _ ->
                        // Check if this is the last layer
                        if (layers.size <= 1) {
                            AlertDialog.Builder(context)
                                .setTitle("Cannot Delete")
                                .setMessage("You must have at least one layer in your design.")
                                .setPositiveButton("OK", null)
                                .show()
                            return@setPositiveButton
                        }
                        
                        // Remove the layer
                        layers.removeAt(position)
                        notifyItemRemoved(position)
                        
                        // Update positions
                        updateLayerPositions()
                        
                        // Update selected layer if needed
                        if (selectedLayer == layer) {
                            selectedLayer = layers.firstOrNull()
                        }
                        
                        // Notify listener
                        onLayersModifiedListener?.invoke(layers, selectedLayer)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }
    
    /**
     * ItemTouchHelper callback for reordering layers
     */
    inner class LayerReorderCallback : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN,
        0
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val fromPosition = viewHolder.adapterPosition
            val toPosition = target.adapterPosition
            
            adapter.moveItem(fromPosition, toPosition)
            return true
        }
        
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            // Not used
        }
        
        override fun isLongPressDragEnabled(): Boolean {
            return true
        }
    }
}