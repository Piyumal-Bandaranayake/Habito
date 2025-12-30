package com.example.habito.dialogs

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.habito.R
import com.example.habito.data.Habit
import com.google.android.material.button.MaterialButton
import java.util.*

/**
 * Dialog for adding a new habit
 */
class AddHabitDialog(
    private val context: Context,
    private val onSave: (Habit) -> Unit
) : DialogFragment() {

    private lateinit var habitNameEditText: EditText
    private lateinit var habitDescriptionEditText: EditText
    private lateinit var habitTypeSpinner: Spinner
    private lateinit var repeatTypeSpinner: Spinner
    private lateinit var timeButton: MaterialButton
    private lateinit var daysContainer: LinearLayout
    private lateinit var targetCountEditText: EditText
    private lateinit var unitEditText: EditText

    private var selectedTime = "09:00"
    private var selectedDays = mutableSetOf<String>()
    private var habitType = "Water Drinking"

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_add_habit, null)
        
        setupViews(view)
        setupSpinners()
        setupTimePicker()
        setupDaysSelection()
        
        return AlertDialog.Builder(context)
            .setTitle("Add New Habit")
            .setView(view)
            .setPositiveButton("Done") { _, _ ->
                saveHabit()
            }
            .setNegativeButton("Cancel", null)
            .create()
    }

    private fun setupViews(view: View) {
        habitNameEditText = view.findViewById(R.id.editTextHabitName)
        habitDescriptionEditText = view.findViewById(R.id.editTextHabitDescription)
        habitTypeSpinner = view.findViewById(R.id.spinnerHabitType)
        repeatTypeSpinner = view.findViewById(R.id.spinnerRepeatType)
        timeButton = view.findViewById(R.id.buttonSelectTime)
        daysContainer = view.findViewById(R.id.daysContainer)
        targetCountEditText = view.findViewById(R.id.editTextTargetCount)
        unitEditText = view.findViewById(R.id.editTextUnit)
    }

    private fun setupSpinners() {
        // Habit Type Spinner
        val habitTypes = arrayOf(
            "Water Drinking", "Exercise", "Sleep", "Meditation", 
            "Reading", "Walking", "Journaling", "Learning", "Other"
        )
        val habitTypeAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, habitTypes)
        habitTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        habitTypeSpinner.adapter = habitTypeAdapter
        
        habitTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                habitType = habitTypes[position]
                updateHabitDefaults()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Repeat Type Spinner
        val repeatTypes = arrayOf("Every Day", "Select Days")
        val repeatTypeAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, repeatTypes)
        repeatTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        repeatTypeSpinner.adapter = repeatTypeAdapter
        
        repeatTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                daysContainer.visibility = if (position == 1) View.VISIBLE else View.GONE
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun updateHabitDefaults() {
        when (habitType) {
            "Water Drinking" -> {
                habitNameEditText.setText("Drink Water")
                habitDescriptionEditText.setText("Stay hydrated throughout the day")
                targetCountEditText.setText("8")
                unitEditText.setText("glasses")
            }
            "Exercise" -> {
                habitNameEditText.setText("Exercise")
                habitDescriptionEditText.setText("Physical activity for health")
                targetCountEditText.setText("1")
                unitEditText.setText("session")
            }
            "Sleep" -> {
                habitNameEditText.setText("Sleep")
                habitDescriptionEditText.setText("Get adequate rest")
                targetCountEditText.setText("8")
                unitEditText.setText("hours")
            }
            "Meditation" -> {
                habitNameEditText.setText("Meditation")
                habitDescriptionEditText.setText("Mindfulness practice")
                targetCountEditText.setText("1")
                unitEditText.setText("session")
            }
            "Reading" -> {
                habitNameEditText.setText("Reading")
                habitDescriptionEditText.setText("Read books or articles")
                targetCountEditText.setText("30")
                unitEditText.setText("minutes")
            }
            "Walking" -> {
                habitNameEditText.setText("Walking")
                habitDescriptionEditText.setText("Take a walk")
                targetCountEditText.setText("30")
                unitEditText.setText("minutes")
            }
            "Journaling" -> {
                habitNameEditText.setText("Journaling")
                habitDescriptionEditText.setText("Write in journal")
                targetCountEditText.setText("1")
                unitEditText.setText("entry")
            }
            "Learning" -> {
                habitNameEditText.setText("Learning")
                habitDescriptionEditText.setText("Learn something new")
                targetCountEditText.setText("1")
                unitEditText.setText("hour")
            }
        }
    }

    private fun setupTimePicker() {
        timeButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val timePickerDialog = TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                    timeButton.text = "Time: $selectedTime"
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            )
            timePickerDialog.show()
        }
    }

    private fun setupDaysSelection() {
        val days = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        
        days.forEach { day ->
            val checkBox = CheckBox(context).apply {
                text = day
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedDays.add(day)
                    } else {
                        selectedDays.remove(day)
                    }
                }
            }
            daysContainer.addView(checkBox)
        }
    }

    private fun saveHabit() {
        val name = habitNameEditText.text.toString().trim()
        val description = habitDescriptionEditText.text.toString().trim()
        val targetCount = targetCountEditText.text.toString().toIntOrNull() ?: 1
        val unit = unitEditText.text.toString().trim().ifEmpty { "times" }
        
        if (name.isEmpty()) {
            Toast.makeText(context, "Please enter habit name", Toast.LENGTH_SHORT).show()
            return
        }

        val habit = Habit(
            name = name,
            description = description,
            targetCount = targetCount,
            unit = unit
        )
        
        onSave(habit)
    }
}

