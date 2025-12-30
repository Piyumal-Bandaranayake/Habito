package com.example.habito.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.habito.MainActivity
import com.example.habito.R
import com.example.habito.data.DataManager
import com.example.habito.databinding.FragmentSettingsBinding
import com.example.habito.utils.ReminderManager

/**
 * SettingsFragment displays app settings
 */
class SettingsFragment : Fragment() {
    
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var dataManager: DataManager
    private lateinit var reminderManager: ReminderManager
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize data manager and reminder manager
        dataManager = DataManager(requireContext())
        reminderManager = ReminderManager(requireContext())
        
        // Setup UI
        setupUI()
    }
    
    private fun setupUI() {
        // Add any settings functionality here
        binding.textViewTitle.text = "Settings"
        binding.textViewSubtitle.text = "Customize your wellness experience"
        
        // Add user info
        val userName = dataManager.getUserName()
        if (userName.isNotEmpty()) {
            binding.textViewSubtitle.text = "Welcome, $userName! Customize your wellness experience"
        }
        
        // Setup button listeners
        binding.buttonClearCache.setOnClickListener {
            clearCacheAndRestart()
        }
        
        binding.buttonResetOnboarding.setOnClickListener {
            dataManager.setOnboardingCompleted(false)
            Toast.makeText(context, "Onboarding reset. Restart the app to see onboarding again.", Toast.LENGTH_LONG).show()
        }
        
        // Setup reminder controls
        setupReminderControls()
    }
    
    private fun clearCacheAndRestart() {
        // Clear all data
        dataManager.clearAllData()
        
        // Show confirmation message
        Toast.makeText(context, "Cache cleared! Restarting app...", Toast.LENGTH_SHORT).show()
        
        // Restart the app by recreating the MainActivity
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        
        // Finish the current activity
        requireActivity().finish()
    }
    
    private fun setupReminderControls() {
        // Load current reminder settings
        val isEnabled = dataManager.isReminderEnabled()
        val interval = dataManager.getReminderInterval()
        
        // Update UI based on current settings
        updateReminderUI(isEnabled, interval)
        
        // Setup toggle button for enabling/disabling reminders
        binding.switchReminderEnabled.setOnCheckedChangeListener { _, isChecked ->
            dataManager.setReminderEnabled(isChecked)
            if (isChecked) {
                reminderManager.scheduleHydrationReminders(interval)
                Toast.makeText(context, "Hydration reminders enabled! 💧", Toast.LENGTH_SHORT).show()
            } else {
                reminderManager.cancelHydrationReminders()
                Toast.makeText(context, "Hydration reminders disabled", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Setup interval buttons
        binding.buttonInterval30.setOnClickListener {
            updateReminderInterval(30L)
        }
        
        binding.buttonInterval60.setOnClickListener {
            updateReminderInterval(60L)
        }
        
        binding.buttonInterval120.setOnClickListener {
            updateReminderInterval(120L)
        }
    }
    
    private fun updateReminderUI(isEnabled: Boolean, interval: Long) {
        binding.switchReminderEnabled.isChecked = isEnabled
        
        // Update interval button states
        binding.buttonInterval30.isSelected = interval == 30L
        binding.buttonInterval60.isSelected = interval == 60L
        binding.buttonInterval120.isSelected = interval == 120L
        
        // Update status text
        val statusText = if (isEnabled) {
            "Reminders every ${formatInterval(interval)}"
        } else {
            "Reminders disabled"
        }
        binding.textViewReminderStatus.text = statusText
    }
    
    private fun updateReminderInterval(intervalMinutes: Long) {
        dataManager.setReminderInterval(intervalMinutes)
        
        // If reminders are enabled, reschedule with new interval
        if (dataManager.isReminderEnabled()) {
            reminderManager.scheduleHydrationReminders(intervalMinutes)
        }
        
        updateReminderUI(dataManager.isReminderEnabled(), intervalMinutes)
        Toast.makeText(context, "Reminder interval set to ${formatInterval(intervalMinutes)}", Toast.LENGTH_SHORT).show()
    }
    
    private fun formatInterval(minutes: Long): String {
        return when (minutes) {
            30L -> "30 minutes"
            60L -> "1 hour"
            120L -> "2 hours"
            else -> "$minutes minutes"
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

