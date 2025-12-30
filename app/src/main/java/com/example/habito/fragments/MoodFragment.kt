package com.example.habito.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habito.R
import com.example.habito.data.DataManager
import com.example.habito.data.MoodEntry
import com.example.habito.data.HydrationData
import com.example.habito.databinding.FragmentMoodBinding
import com.example.habito.adapters.MoodAdapter
import com.example.habito.dialogs.AddMoodDialog
import com.example.habito.dialogs.EditMoodDialog
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*

/**
 * MoodFragment displays mood tracking and hydration tracker
 */
class MoodFragment : Fragment() {
    
    private var _binding: FragmentMoodBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var dataManager: DataManager
    private lateinit var moodAdapter: MoodAdapter
    private val moodEntries = mutableListOf<MoodEntry>()
    private var hydrationData = HydrationData()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoodBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize data manager
        dataManager = DataManager(requireContext())
        
        // Setup UI
        setupRecyclerView()
        setupHydrationTracker()
        setupChart()
        setupFloatingActionButton()
        loadData()
    }
    
    private fun setupRecyclerView() {
        moodAdapter = MoodAdapter(
            moodEntries = moodEntries,
            onMoodClick = { moodEntry ->
                // Handle mood entry click if needed
            },
            onMoodEdit = { moodEntry ->
                showEditMoodDialog(moodEntry)
            },
            onMoodDelete = { moodEntry ->
                showDeleteMoodDialog(moodEntry)
            }
        )
        
        binding.recyclerViewMoods.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = moodAdapter
        }
    }
    
    private fun setupHydrationTracker() {
        // Load hydration data
        hydrationData = dataManager.getHydrationData()
        updateHydrationUI()
        
        // Setup hydration buttons
        binding.buttonAddGlass.setOnClickListener {
            addGlass()
        }
        
        binding.buttonRemoveGlass.setOnClickListener {
            removeGlass()
        }
        
        binding.buttonResetHydration.setOnClickListener {
            resetHydration()
        }
    }
    
    private fun setupChart() {
        // Configure mood chart
        binding.chartMoodTrends.apply {
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
                axisMaximum = 5f
                granularity = 1f
            }
            
            axisRight.isEnabled = false
        }
        
        updateMoodChart()
    }
    
    private fun setupFloatingActionButton() {
        binding.fabAddMood.setOnClickListener {
            showAddMoodDialog()
        }
    }
    
    private fun loadData() {
        // Load mood entries
        moodEntries.clear()
        moodEntries.addAll(dataManager.getMoodEntries().sortedByDescending { it.timestamp })
        moodAdapter.notifyDataSetChanged()
        
        // Update UI
        updateEmptyState()
        updateMoodChart()
    }
    
    private fun updateEmptyState() {
        val hasMoods = moodEntries.isNotEmpty()
        binding.emptyState.visibility = if (hasMoods) View.GONE else View.VISIBLE
        binding.recyclerViewMoods.visibility = if (hasMoods) View.VISIBLE else View.GONE
    }
    
    private fun updateHydrationUI() {
        val progress = if (hydrationData.targetGlasses > 0) {
            (hydrationData.glassesDrunk.toFloat() / hydrationData.targetGlasses * 100).toInt()
        } else 0
        
        binding.textViewGlassesDrunk.text = "${hydrationData.glassesDrunk}/${hydrationData.targetGlasses}"
        binding.progressBarHydration.progress = progress
        
        // Update status message
        val statusMessage = when {
            hydrationData.glassesDrunk == 0 -> "Let's start hydrating! Your body needs water! 🚰"
            hydrationData.glassesDrunk < hydrationData.targetGlasses / 2 -> "Keep going! You're doing great! 💪"
            hydrationData.glassesDrunk < hydrationData.targetGlasses -> "Halfway there! You're doing great! ✨"
            else -> "Amazing! You've reached your hydration goal! 🎉"
        }
        
        binding.textViewStatus.text = statusMessage
        
        // Enable/disable remove button
        binding.buttonRemoveGlass.isEnabled = hydrationData.glassesDrunk > 0
    }
    
    private fun updateMoodChart() {
        if (moodEntries.isEmpty()) return
        
        // Group mood entries by date and calculate average mood
        val moodMap = mutableMapOf<String, MutableList<Int>>()
        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        
        moodEntries.forEach { entry ->
            val date = dateFormat.format(Date(entry.timestamp))
            val moodValue = when (entry.emoji) {
                "😢", "😭", "😔" -> 1 // Sad
                "😐", "😑", "😶" -> 2 // Neutral
                "😊", "🙂", "😌" -> 3 // Happy
                "😄", "😃", "🤗" -> 4 // Very Happy
                "🤩", "🥳", "😍" -> 5 // Excited
                else -> 3 // Default to neutral
            }
            
            moodMap.getOrPut(date) { mutableListOf() }.add(moodValue)
        }
        
        // Calculate average mood for each date
        val entries = mutableListOf<Entry>()
        val labels = mutableListOf<String>()
        
        moodMap.entries.sortedBy { it.key }.forEachIndexed { index, (date, moods) ->
            val averageMood = moods.average().toFloat()
            entries.add(Entry(index.toFloat(), averageMood))
            labels.add(date)
        }
        
        if (entries.isNotEmpty()) {
            val dataSet = LineDataSet(entries, "Mood Trend").apply {
                color = resources.getColor(R.color.mood_chart_color, null)
                setCircleColor(resources.getColor(R.color.mood_chart_color, null))
                lineWidth = 3f
                circleRadius = 6f
                setDrawFilled(true)
                fillColor = resources.getColor(R.color.mood_chart_fill, null)
            }
            
            val lineData = LineData(dataSet)
            binding.chartMoodTrends.data = lineData
            binding.chartMoodTrends.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            binding.chartMoodTrends.invalidate()
        }
    }
    
    private fun addGlass() {
        if (hydrationData.glassesDrunk < hydrationData.targetGlasses) {
            hydrationData = hydrationData.copy(
                glassesDrunk = hydrationData.glassesDrunk + 1,
                lastUpdated = System.currentTimeMillis()
            )
            dataManager.updateHydrationData(hydrationData)
            updateHydrationUI()
            Toast.makeText(context, "Glass added! Keep hydrating! 💧", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "You've already reached your daily goal! 🎉", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun removeGlass() {
        if (hydrationData.glassesDrunk > 0) {
            hydrationData = hydrationData.copy(
                glassesDrunk = hydrationData.glassesDrunk - 1,
                lastUpdated = System.currentTimeMillis()
            )
            dataManager.updateHydrationData(hydrationData)
            updateHydrationUI()
            Toast.makeText(context, "Glass removed", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun resetHydration() {
        hydrationData = HydrationData()
        dataManager.updateHydrationData(hydrationData)
        updateHydrationUI()
        Toast.makeText(context, "Hydration reset", Toast.LENGTH_SHORT).show()
    }
    
    private fun showAddMoodDialog() {
        val dialog = AddMoodDialog(requireContext()) { moodEntry ->
            addMoodEntry(moodEntry)
        }
        dialog.show(requireActivity().supportFragmentManager, "AddMoodDialog")
    }
    
    private fun addMoodEntry(moodEntry: MoodEntry) {
        dataManager.addMoodEntry(moodEntry)
        loadData()
        Toast.makeText(context, "Mood added successfully", Toast.LENGTH_SHORT).show()
    }
    
    private fun showEditMoodDialog(moodEntry: MoodEntry) {
        val dialog = EditMoodDialog(requireContext(), moodEntry) { updatedMoodEntry ->
            updateMoodEntry(updatedMoodEntry)
        }
        dialog.show(requireActivity().supportFragmentManager, "EditMoodDialog")
    }
    
    private fun updateMoodEntry(moodEntry: MoodEntry) {
        dataManager.updateMoodEntry(moodEntry)
        loadData()
        Toast.makeText(context, "Mood updated successfully", Toast.LENGTH_SHORT).show()
    }
    
    private fun showDeleteMoodDialog(moodEntry: MoodEntry) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Mood Entry")
            .setMessage("Are you sure you want to delete this mood entry? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteMoodEntry(moodEntry)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun deleteMoodEntry(moodEntry: MoodEntry) {
        dataManager.deleteMoodEntry(moodEntry.id)
        loadData()
        Toast.makeText(context, "Mood entry deleted successfully", Toast.LENGTH_SHORT).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

