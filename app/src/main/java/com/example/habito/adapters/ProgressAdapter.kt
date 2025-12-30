package com.example.habito.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.habito.R
import com.example.habito.data.Habit
import com.example.habito.data.HabitCompletion
import com.google.android.material.progressindicator.LinearProgressIndicator

/**
 * Adapter for displaying habit progress in RecyclerView
 */
class ProgressAdapter(
    private val habits: List<Habit>,
    private val habitCompletions: List<HabitCompletion>,
    private val onHabitClick: (Habit) -> Unit
) : RecyclerView.Adapter<ProgressAdapter.ProgressViewHolder>() {

    class ProgressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.textViewHabitName)
        val progressText: TextView = itemView.findViewById(R.id.textViewProgress)
        val progressBar: LinearProgressIndicator = itemView.findViewById(R.id.progressBar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgressViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_progress, parent, false)
        return ProgressViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProgressViewHolder, position: Int) {
        val habit = habits[position]
        
        holder.nameText.text = habit.name
        
        // Calculate progress for this habit
        val habitCompletions = habitCompletions.filter { it.habitId == habit.id }
        val completedCount = habitCompletions.count { it.isCompleted }
        val totalDays = habitCompletions.size
        
        val progress = if (totalDays > 0) {
            (completedCount.toFloat() / totalDays * 100).toInt()
        } else 0
        
        holder.progressText.text = "$completedCount/$totalDays days ($progress%)"
        holder.progressBar.progress = progress
        
        holder.itemView.setOnClickListener {
            onHabitClick(habit)
        }
    }

    override fun getItemCount(): Int = habits.size
}

