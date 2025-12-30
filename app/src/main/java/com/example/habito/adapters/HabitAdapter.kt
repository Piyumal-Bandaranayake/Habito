package com.example.habito.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.habito.R
import com.example.habito.data.Habit
import com.example.habito.data.HabitCompletion
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.LinearProgressIndicator
import android.widget.CheckBox
import com.google.android.material.button.MaterialButton

/**
 * Adapter for displaying habits in RecyclerView with completion tracking
 */
class HabitAdapter(
    private val habits: List<Habit>,
    private val habitCompletions: List<HabitCompletion>,
    private val onHabitClick: (Habit) -> Unit,
    private val onHabitComplete: (Habit, Boolean) -> Unit,
    private val onHabitEdit: (Habit) -> Unit,
    private val onHabitDelete: (Habit) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: MaterialCardView = itemView.findViewById(R.id.cardHabit)
        val nameText: TextView = itemView.findViewById(R.id.textViewHabitName)
        val descriptionText: TextView = itemView.findViewById(R.id.textViewHabitDescription)
        val progressText: TextView = itemView.findViewById(R.id.textViewProgress)
        val progressBar: LinearProgressIndicator = itemView.findViewById(R.id.progressBar)
        val completionCheckbox: CheckBox = itemView.findViewById(R.id.checkboxCompletion)
        val editButton: MaterialButton = itemView.findViewById(R.id.buttonEdit)
        val deleteButton: MaterialButton = itemView.findViewById(R.id.buttonDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit_card, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]
        
        holder.nameText.text = habit.name
        holder.descriptionText.text = habit.description
        
        // Check if habit is completed today
        val todayTimestamp = getTodayTimestamp()
        val todayCompletion = habitCompletions.find { 
            it.habitId == habit.id && it.date == todayTimestamp 
        }
        val isCompletedToday = todayCompletion?.isCompleted ?: false
        
        // Update progress display
        val completedCount = todayCompletion?.completedCount ?: 0
        holder.progressText.text = "$completedCount/${habit.targetCount} ${habit.unit}"
        
        // Update progress bar
        val progress = if (habit.targetCount > 0) {
            (completedCount.toFloat() / habit.targetCount * 100).toInt()
        } else 0
        holder.progressBar.progress = progress
        
        // Update completion checkbox
        holder.completionCheckbox.isChecked = isCompletedToday
        
        // Set up click listeners
        holder.cardView.setOnClickListener {
            onHabitClick(habit)
        }
        
        holder.completionCheckbox.setOnCheckedChangeListener { _, isChecked ->
            onHabitComplete(habit, isChecked)
        }
        
        holder.editButton.setOnClickListener {
            onHabitEdit(habit)
        }
        
        holder.deleteButton.setOnClickListener {
            onHabitDelete(habit)
        }
    }

    override fun getItemCount(): Int = habits.size
    
    private fun getTodayTimestamp(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}

