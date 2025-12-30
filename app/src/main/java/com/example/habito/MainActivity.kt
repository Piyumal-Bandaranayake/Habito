package com.example.habito

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.habito.data.DataManager
import com.example.habito.utils.ReminderManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    
    private lateinit var dataManager: DataManager
    private lateinit var reminderManager: ReminderManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        // Initialize data manager and reminder manager
        dataManager = DataManager(this)
        reminderManager = ReminderManager(this)
        
        // Initialize reminders if enabled
        initializeReminders()
        
        setupNavigation()
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        
        // Check if onboarding is completed
        if (dataManager.isOnboardingCompleted()) {
            // Navigate to habits if onboarding is completed
            navController.navigate(R.id.nav_habits)
            bottomNav.setupWithNavController(navController)
            bottomNav.visibility = android.view.View.VISIBLE
        } else {
            // Hide bottom navigation during onboarding
            bottomNav.visibility = android.view.View.GONE
        }
        
        // Listen for navigation changes to show/hide bottom navigation
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.nav_welcome, R.id.nav_onboarding -> {
                    bottomNav.visibility = android.view.View.GONE
                }
                else -> {
                    bottomNav.visibility = android.view.View.VISIBLE
                    if (!bottomNav.hasOnClickListeners()) {
                        bottomNav.setupWithNavController(navController)
                    }
                }
            }
        }
    }
    
    private fun initializeReminders() {
        // If reminders are enabled, schedule them
        if (dataManager.isReminderEnabled()) {
            val interval = dataManager.getReminderInterval()
            reminderManager.scheduleHydrationReminders(interval)
        }
    }
}