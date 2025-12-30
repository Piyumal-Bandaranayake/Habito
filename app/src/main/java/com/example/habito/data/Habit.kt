package com.example.habito.data

import java.util.*

/**
 * Data class representing a habit
 */
data class Habit(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val targetCount: Int,
    val unit: String,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Data class representing habit completion for a specific date
 */
data class HabitCompletion(
    val habitId: String,
    val date: Long,
    val completedCount: Int,
    val isCompleted: Boolean
)

/**
 * Data class representing a mood entry
 */
data class MoodEntry(
    val id: String = UUID.randomUUID().toString(),
    val emoji: String,
    val note: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Data class representing hydration data
 */
data class HydrationData(
    val glassesDrunk: Int = 0,
    val targetGlasses: Int = 8,
    val lastUpdated: Long = System.currentTimeMillis()
)

