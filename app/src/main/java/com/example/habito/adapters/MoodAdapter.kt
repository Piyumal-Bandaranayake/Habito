package com.example.habito.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.habito.R
import com.example.habito.data.MoodEntry
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter for displaying mood entries in RecyclerView with management features
 */
class MoodAdapter(
    private val moodEntries: List<MoodEntry>,
    private val onMoodClick: (MoodEntry) -> Unit,
    private val onMoodEdit: (MoodEntry) -> Unit,
    private val onMoodDelete: (MoodEntry) -> Unit
) : RecyclerView.Adapter<MoodAdapter.MoodViewHolder>() {

    class MoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val emojiText: TextView = itemView.findViewById(R.id.textViewEmoji)
        val noteText: TextView = itemView.findViewById(R.id.textViewNote)
        val timeText: TextView = itemView.findViewById(R.id.textViewTime)
        val editButton: com.google.android.material.button.MaterialButton = itemView.findViewById(R.id.buttonEdit)
        val deleteButton: com.google.android.material.button.MaterialButton = itemView.findViewById(R.id.buttonDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood, parent, false)
        return MoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        val moodEntry = moodEntries[position]
        
        holder.emojiText.text = moodEntry.emoji
        holder.noteText.text = moodEntry.note.ifEmpty { "No note" }
        
        // Format time
        val timeFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        holder.timeText.text = timeFormat.format(Date(moodEntry.timestamp))
        
        // Set up click listeners
        holder.itemView.setOnClickListener {
            onMoodClick(moodEntry)
        }
        
        holder.editButton.setOnClickListener {
            onMoodEdit(moodEntry)
        }
        
        holder.deleteButton.setOnClickListener {
            onMoodDelete(moodEntry)
        }
    }

    override fun getItemCount(): Int = moodEntries.size
}

