package com.example.habito.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.habito.R
import com.example.habito.data.Habit
import com.example.habito.data.HabitCompletion
import com.google.android.material.button.MaterialButton

/**
 * Dialog for tracking habit completion with counter
 */
class HabitCompletionDialog(
    private val context: Context,
    private val habit: Habit,
    private val currentCompletion: HabitCompletion?,
    private val onSave: (HabitCompletion) -> Unit
) : DialogFragment() {

    private lateinit var progressText: TextView
    private lateinit var progressBar: SeekBar
    private lateinit var completeButton: MaterialButton
    private var currentCount = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_habit_completion, null)
        
        setupViews(view)
        setupProgressBar()
        setupButton()
        
        return AlertDialog.Builder(context)
            .setTitle("Track ${habit.name}")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                saveCompletion()
            }
            .setNegativeButton("Cancel", null)
            .create()
    }

    private fun setupViews(view: View) {
        progressText = view.findViewById(R.id.textViewProgress)
        progressBar = view.findViewById(R.id.seekBarProgress)
        completeButton = view.findViewById(R.id.buttonComplete)
        
        // Set initial values
        currentCount = currentCompletion?.completedCount ?: 0
        progressBar.max = habit.targetCount
        progressBar.progress = currentCount
        updateProgressText()
    }

    private fun setupProgressBar() {
        progressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    currentCount = progress
                    updateProgressText()
                }
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupButton() {
        completeButton.setOnClickListener {
            currentCount = habit.targetCount
            progressBar.progress = currentCount
            updateProgressText()
        }
    }

    private fun updateProgressText() {
        progressText.text = "$currentCount/${habit.targetCount} ${habit.unit}"
        completeButton.isEnabled = currentCount < habit.targetCount
    }

    private fun saveCompletion() {
        val todayTimestamp = getTodayTimestamp()
        val completion = HabitCompletion(
            habitId = habit.id,
            date = todayTimestamp,
            completedCount = currentCount,
            isCompleted = currentCount >= habit.targetCount
        )
        
        onSave(completion)
    }

    private fun getTodayTimestamp(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
