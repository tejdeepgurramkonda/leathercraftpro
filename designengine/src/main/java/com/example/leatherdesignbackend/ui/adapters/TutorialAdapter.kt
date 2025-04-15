package com.example.leatherdesignbackend.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.leatherdesignbackend.R
import com.example.leatherdesignbackend.models.Tutorial

/**
 * Adapter for displaying tutorials in a RecyclerView
 */
class TutorialAdapter(
    private var tutorials: List<Tutorial>,
    private val onTutorialClick: (Tutorial) -> Unit
) : RecyclerView.Adapter<TutorialAdapter.TutorialViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TutorialViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tutorial, parent, false)
        return TutorialViewHolder(view)
    }

    override fun onBindViewHolder(holder: TutorialViewHolder, position: Int) {
        val tutorial = tutorials[position]
        holder.bind(tutorial)
    }

    override fun getItemCount(): Int = tutorials.size

    fun updateTutorials(newTutorials: List<Tutorial>) {
        this.tutorials = newTutorials
        notifyDataSetChanged()
    }

    inner class TutorialViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tutorialImage: ImageView = itemView.findViewById(R.id.imageTutorial)
        private val tutorialTitle: TextView = itemView.findViewById(R.id.textTutorialTitle)
        private val tutorialSummary: TextView = itemView.findViewById(R.id.textTutorialSummary)
        private val tutorialLevel: TextView = itemView.findViewById(R.id.textTutorialLevel)

        fun bind(tutorial: Tutorial) {
            tutorialImage.setImageResource(tutorial.imageResource)
            tutorialTitle.text = tutorial.title
            tutorialSummary.text = tutorial.summary
            tutorialLevel.text = "Level: ${tutorial.level}"

            itemView.setOnClickListener {
                onTutorialClick(tutorial)
            }
        }
    }
}
