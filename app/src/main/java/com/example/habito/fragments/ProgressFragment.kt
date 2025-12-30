package com.example.habito.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habito.R
import com.example.habito.data.DataManager
import com.example.habito.data.Habit
import com.example.habito.data.HabitCompletion
import com.example.habito.databinding.FragmentProgressBinding
import com.example.habito.adapters.ProgressAdapter
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*

/**
 * ProgressFragment displays overall progress and statistics
 */
class ProgressFragment : Fragment() {
    
    private var _binding: FragmentProgressBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var dataManager: DataManager
    private lateinit var progressAdapter: ProgressAdapter
    private val habits = mutableListOf<Habit>()
    private val habitCompletions = mutableListOf<HabitCompletion>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProgressBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize data manager
        dataManager = DataManager(requireContext())
        
        // Setup UI
        setupRecyclerView()
        setupCharts()
        loadData()
    }
    
    private fun setupRecyclerView() {
        progressAdapter = ProgressAdapter(habits, habitCompletions) { habit ->
            // Handle habit click if needed
        }
        
        binding.recyclerViewProgress.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = progressAdapter
        }
    }
    
    private fun setupCharts() {
        // Setup weekly progress chart
        binding.chartWeeklyProgress.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            
            // Configure axes
            xAxis.apply {
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
                axisMaximum = 100f
                granularity = 20f
            }
            
            axisRight.isEnabled = false
        }
        
        // Setup habit completion chart
        binding.chartHabitCompletion.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            
            // Configure axes
            xAxis.apply {
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
                axisMaximum = 100f
                granularity = 20f
            }
            
            axisRight.isEnabled = false
        }
    }
    
    private fun loadData() {
        // Load habits
        habits.clear()
        habits.addAll(dataManager.getHabits().filter { it.isActive })
        
        // Load habit completions for the last 7 days
        habitCompletions.clear()
        val calendar = Calendar.getInstance()
        for (i in 6 downTo 0) {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            
            val dayTimestamp = calendar.timeInMillis
            val dayCompletions = dataManager.getHabitCompletionsForDate(dayTimestamp)
            habitCompletions.addAll(dayCompletions)
        }
        
        // Update UI
        updateProgressStats()
        updateCharts()
        progressAdapter.notifyDataSetChanged()
        updateEmptyState()
    }
    
    private fun updateProgressStats() {
        val totalHabits = habits.size
        val completedToday = habitCompletions.count { 
            it.date == dataManager.getTodayTimestamp() && it.isCompleted 
        }
        
        val completionRate = if (totalHabits > 0) {
            (completedToday.toFloat() / totalHabits * 100).toInt()
        } else 0
        
        binding.textViewTotalHabits.text = totalHabits.toString()
        binding.textViewCompletedToday.text = completedToday.toString()
        binding.textViewCompletionRate.text = "$completionRate%"
        
        // Update progress bar
        binding.progressBarOverall.progress = completionRate
        
        // Update status message
        val statusMessage = when {
            completionRate == 100 -> "Perfect! You've completed all your habits today! 🎉"
            completionRate >= 80 -> "Great job! You're almost there! 💪"
            completionRate >= 50 -> "Good progress! Keep it up! ✨"
            completionRate > 0 -> "Every step counts! You're doing great! 🌟"
            else -> "Ready to start your wellness journey? Let's go! 🚀"
        }
        
        binding.textViewStatus.text = statusMessage
    }
    
    private fun updateCharts() {
        updateWeeklyProgressChart()
        updateHabitCompletionChart()
    }
    
    private fun updateWeeklyProgressChart() {
        val calendar = Calendar.getInstance()
        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()
        val dateFormat = SimpleDateFormat("EEE", Locale.getDefault())
        
        for (i in 6 downTo 0) {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            
            val dayTimestamp = calendar.timeInMillis
            val dayCompletions = dataManager.getHabitCompletionsForDate(dayTimestamp)
            val completedCount = dayCompletions.count { it.isCompleted }
            val totalHabits = habits.size
            
            val completionRate = if (totalHabits > 0) {
                (completedCount.toFloat() / totalHabits * 100)
            } else 0f
            
            entries.add(BarEntry((6 - i).toFloat(), completionRate))
            labels.add(dateFormat.format(calendar.time))
        }
        
        if (entries.isNotEmpty()) {
            val dataSet = BarDataSet(entries, "Daily Progress").apply {
                color = resources.getColor(R.color.primary, null)
                setDrawValues(true)
                valueTextSize = 12f
            }
            
            val barData = BarData(dataSet)
            binding.chartWeeklyProgress.data = barData
            binding.chartWeeklyProgress.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            binding.chartWeeklyProgress.invalidate()
        }
    }
    
    private fun updateHabitCompletionChart() {
        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()
        
        habits.forEachIndexed { index, habit ->
            val completions = habitCompletions.filter { it.habitId == habit.id }
            val completionRate = if (completions.isNotEmpty()) {
                (completions.count { it.isCompleted }.toFloat() / completions.size * 100)
            } else 0f
            
            entries.add(BarEntry(index.toFloat(), completionRate))
            labels.add(habit.name.take(8)) // Truncate long names
        }
        
        if (entries.isNotEmpty()) {
            val dataSet = BarDataSet(entries, "Habit Completion").apply {
                color = resources.getColor(R.color.accent, null)
                setDrawValues(true)
                valueTextSize = 10f
            }
            
            val barData = BarData(dataSet)
            binding.chartHabitCompletion.data = barData
            binding.chartHabitCompletion.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            binding.chartHabitCompletion.invalidate()
        }
    }
    
    private fun updateEmptyState() {
        val hasHabits = habits.isNotEmpty()
        binding.emptyState.visibility = if (hasHabits) View.GONE else View.VISIBLE
        binding.layoutStats.visibility = if (hasHabits) View.VISIBLE else View.GONE
        binding.layoutCharts.visibility = if (hasHabits) View.VISIBLE else View.GONE
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

