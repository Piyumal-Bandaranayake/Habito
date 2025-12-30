package com.example.habito.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habito.R
import com.example.habito.adapters.HabitAdapter
import com.example.habito.data.DataManager
import com.example.habito.data.Habit
import com.example.habito.data.HabitCompletion
import com.example.habito.databinding.FragmentHabitsBinding
import com.example.habito.dialogs.AddHabitDialog
import com.example.habito.dialogs.EditHabitDialog
import com.example.habito.dialogs.HabitCompletionDialog
import java.util.*

/**
 * HabitsFragment displays habit tracking with journal-style interface
 */
class HabitsFragment : Fragment() {
    
    private var _binding: FragmentHabitsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var dataManager: DataManager
    private lateinit var habitAdapter: HabitAdapter
    private val habits = mutableListOf<Habit>()
    private val habitCompletions = mutableListOf<HabitCompletion>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHabitsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize data manager
        dataManager = DataManager(requireContext())
        
        // Setup UI
        setupUI()
        setupRecyclerView()
        loadData()
    }
    
    private fun setupUI() {
        // Setup time of day
        updateTimeOfDay()
        
        // Setup welcome message
        updateWelcomeMessage()
        
        // Setup button listeners
        setupButtonListeners()
    }

    private fun setupRecyclerView() {
        habitAdapter = HabitAdapter(
            habits = habits,
            habitCompletions = habitCompletions,
            onHabitClick = { habit ->
                // Show habit completion dialog
                showHabitCompletionDialog(habit)
            },
            onHabitComplete = { habit, isCompleted ->
                handleHabitCompletion(habit, isCompleted)
            },
            onHabitEdit = { habit ->
                showEditHabitDialog(habit)
            },
            onHabitDelete = { habit ->
                showDeleteHabitDialog(habit)
            }
        )
        
        binding.recyclerViewHabits.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = habitAdapter
        }
    }

    private fun updateTimeOfDay() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        
        val timeOfDay = when (hour) {
            in 5..11 -> "Morning"
            in 12..17 -> "Afternoon"
            in 18..21 -> "Evening"
            else -> "Night"
        }
        
        binding.textViewTimeOfDay?.text = timeOfDay
    }
    
    private fun updateWelcomeMessage() {
        val userName = dataManager.getUserName()
        if (userName.isNotEmpty()) {
            binding.textViewTitle?.text = "Welcome back, $userName!"
        } else {
            binding.textViewTitle?.text = "My Journal"
        }
    }

    private fun setupButtonListeners() {
        binding.buttonNewHabit?.setOnClickListener {
            showAddHabitDialog()
        }
        
        binding.buttonBuildHabit?.setOnClickListener {
            showAddHabitDialog()
        }
        
        binding.buttonBreakHabit?.setOnClickListener {
            // TODO: Show break habit dialog
            Toast.makeText(context, "Break habit feature coming soon", Toast.LENGTH_SHORT).show()
        }
        
        binding.buttonFilterHabits?.setOnClickListener {
            // TODO: Show filter options
            Toast.makeText(context, "Filter options coming soon", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showAddHabitDialog() {
        val dialog = AddHabitDialog(requireContext()) { habit ->
            addHabit(habit)
        }
        dialog.show(requireActivity().supportFragmentManager, "AddHabitDialog")
    }
    
    private fun addHabit(habit: Habit) {
        dataManager.addHabit(habit)
        loadData()
        Toast.makeText(context, "Habit added successfully!", Toast.LENGTH_SHORT).show()
    }
    
    private fun handleHabitCompletion(habit: Habit, isCompleted: Boolean) {
        val todayTimestamp = dataManager.getTodayTimestamp()
        val completion = HabitCompletion(
            habitId = habit.id,
            date = todayTimestamp,
            completedCount = if (isCompleted) habit.targetCount else 0,
            isCompleted = isCompleted
        )
        
        dataManager.addHabitCompletion(completion)
        loadData()
        
        val message = if (isCompleted) {
            "Great job! ${habit.name} completed! 🎉"
        } else {
            "Habit unchecked. Keep going! 💪"
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
    
    private fun showEditHabitDialog(habit: Habit) {
        val dialog = EditHabitDialog(requireContext(), habit) { updatedHabit ->
            updateHabit(updatedHabit)
        }
        dialog.show(requireActivity().supportFragmentManager, "EditHabitDialog")
    }
    
    private fun updateHabit(habit: Habit) {
        dataManager.updateHabit(habit)
        loadData()
        Toast.makeText(context, "Habit updated successfully!", Toast.LENGTH_SHORT).show()
    }
    
    private fun showDeleteHabitDialog(habit: Habit) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete '${habit.name}'? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteHabit(habit)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun deleteHabit(habit: Habit) {
        dataManager.deleteHabit(habit.id)
        loadData()
        Toast.makeText(context, "Habit deleted successfully!", Toast.LENGTH_SHORT).show()
    }
    
    private fun showHabitCompletionDialog(habit: Habit) {
        val todayTimestamp = dataManager.getTodayTimestamp()
        val currentCompletion = habitCompletions.find { 
            it.habitId == habit.id && it.date == todayTimestamp 
        }
        
        val dialog = HabitCompletionDialog(
            requireContext(), 
            habit, 
            currentCompletion
        ) { completion ->
            dataManager.addHabitCompletion(completion)
            loadData()
            
            val message = if (completion.isCompleted) {
                "Excellent! ${habit.name} completed! 🎉"
            } else {
                "Progress saved! Keep going! 💪"
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
        dialog.show(requireActivity().supportFragmentManager, "HabitCompletionDialog")
    }
    
    private fun loadData() {
        // Load habits
        habits.clear()
        habits.addAll(dataManager.getHabits().filter { it.isActive })
        
        // Load habit completions for today
        habitCompletions.clear()
        val todayTimestamp = dataManager.getTodayTimestamp()
        habitCompletions.addAll(dataManager.getHabitCompletionsForDate(todayTimestamp))
        
        habitAdapter.notifyDataSetChanged()
        updateWelcomeSectionVisibility()
    }
    
    private fun updateWelcomeSectionVisibility() {
        val hasHabits = habits.isNotEmpty()
        binding.welcomeSection.visibility = if (hasHabits) View.GONE else View.VISIBLE
        binding.recyclerViewHabits.visibility = if (hasHabits) View.VISIBLE else View.GONE
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
