package com.example.habito.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

/**
 * Manages data persistence using SharedPreferences
 */
class DataManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("habito_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    // Keys for SharedPreferences
    private val KEY_HABITS = "habits"
    private val KEY_HABIT_COMPLETIONS = "habit_completions"
    private val KEY_MOOD_ENTRIES = "mood_entries"
    private val KEY_HYDRATION_DATA = "hydration_data"
    private val KEY_USER_NAME = "user_name"
    private val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    private val KEY_REMINDER_ENABLED = "reminder_enabled"
    private val KEY_REMINDER_INTERVAL = "reminder_interval"
    
    // Habit management
    fun addHabit(habit: Habit) {
        val habits = getHabits().toMutableList()
        habits.add(habit)
        saveHabits(habits)
    }
    
    fun updateHabit(habit: Habit) {
        val habits = getHabits().toMutableList()
        val index = habits.indexOfFirst { it.id == habit.id }
        if (index != -1) {
            habits[index] = habit
            saveHabits(habits)
        }
    }
    
    fun deleteHabit(habitId: String) {
        val habits = getHabits().toMutableList()
        habits.removeAll { it.id == habitId }
        saveHabits(habits)
    }
    
    fun getHabits(): List<Habit> {
        val json = prefs.getString(KEY_HABITS, "[]")
        val type = object : TypeToken<List<Habit>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
    
    private fun saveHabits(habits: List<Habit>) {
        val json = gson.toJson(habits)
        prefs.edit().putString(KEY_HABITS, json).apply()
    }
    
    // Habit completion management
    fun addHabitCompletion(completion: HabitCompletion) {
        val completions = getHabitCompletions().toMutableList()
        val existingIndex = completions.indexOfFirst { 
            it.habitId == completion.habitId && it.date == completion.date 
        }
        
        if (existingIndex != -1) {
            completions[existingIndex] = completion
        } else {
            completions.add(completion)
        }
        
        saveHabitCompletions(completions)
    }
    
    fun getHabitCompletions(): List<HabitCompletion> {
        val json = prefs.getString(KEY_HABIT_COMPLETIONS, "[]")
        val type = object : TypeToken<List<HabitCompletion>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
    
    fun updateHabitCompletion(completion: HabitCompletion) {
        val completions = getHabitCompletions().toMutableList()
        val index = completions.indexOfFirst { 
            it.habitId == completion.habitId && it.date == completion.date 
        }
        if (index != -1) {
            completions[index] = completion
            saveHabitCompletions(completions)
        }
    }
    
    fun deleteHabitCompletion(habitId: String, date: Long) {
        val completions = getHabitCompletions().toMutableList()
        completions.removeAll { it.habitId == habitId && it.date == date }
        saveHabitCompletions(completions)
    }
    
    fun getHabitCompletionsForDate(date: Long): List<HabitCompletion> {
        return getHabitCompletions().filter { it.date == date }
    }
    
    fun getHabitCompletionsForHabit(habitId: String): List<HabitCompletion> {
        return getHabitCompletions().filter { it.habitId == habitId }
    }
    
    private fun saveHabitCompletions(completions: List<HabitCompletion>) {
        val json = gson.toJson(completions)
        prefs.edit().putString(KEY_HABIT_COMPLETIONS, json).apply()
    }
    
    // Mood entry management
    fun addMoodEntry(moodEntry: MoodEntry) {
        val entries = getMoodEntries().toMutableList()
        entries.add(moodEntry)
        saveMoodEntries(entries)
    }
    
    fun updateMoodEntry(moodEntry: MoodEntry) {
        val entries = getMoodEntries().toMutableList()
        val index = entries.indexOfFirst { it.id == moodEntry.id }
        if (index != -1) {
            entries[index] = moodEntry
            saveMoodEntries(entries)
        }
    }
    
    fun deleteMoodEntry(moodEntryId: String) {
        val entries = getMoodEntries().toMutableList()
        entries.removeAll { it.id == moodEntryId }
        saveMoodEntries(entries)
    }
    
    fun getMoodEntries(): List<MoodEntry> {
        val json = prefs.getString(KEY_MOOD_ENTRIES, "[]")
        val type = object : TypeToken<List<MoodEntry>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
    
    private fun saveMoodEntries(entries: List<MoodEntry>) {
        val json = gson.toJson(entries)
        prefs.edit().putString(KEY_MOOD_ENTRIES, json).apply()
    }
    
    // Hydration data management
    fun updateHydrationData(data: HydrationData) {
        val json = gson.toJson(data)
        prefs.edit().putString(KEY_HYDRATION_DATA, json).apply()
    }
    
    fun getHydrationData(): HydrationData {
        val json = prefs.getString(KEY_HYDRATION_DATA, null)
        return if (json != null) {
            gson.fromJson(json, HydrationData::class.java)
        } else {
            HydrationData()
        }
    }
    
    // Utility functions
    fun getTodayTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
    
    // User management
    fun saveUserName(userName: String) {
        prefs.edit().putString(KEY_USER_NAME, userName).apply()
    }
    
    fun getUserName(): String {
        return prefs.getString(KEY_USER_NAME, "") ?: ""
    }
    
    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }
    
    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }
    
    // Clear all data and reset app to initial state
    fun clearAllData() {
        prefs.edit().clear().apply()
    }
    
    // Reminder settings management
    fun setReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REMINDER_ENABLED, enabled).apply()
    }
    
    fun isReminderEnabled(): Boolean {
        return prefs.getBoolean(KEY_REMINDER_ENABLED, true)
    }
    
    fun setReminderInterval(intervalMinutes: Long) {
        prefs.edit().putLong(KEY_REMINDER_INTERVAL, intervalMinutes).apply()
    }
    
    fun getReminderInterval(): Long {
        return prefs.getLong(KEY_REMINDER_INTERVAL, 60L) // Default: 60 minutes
    }
}

