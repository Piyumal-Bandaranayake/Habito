package com.example.habito.utils

import android.content.Context
import androidx.work.*
import com.example.habito.workers.HydrationReminderWorker
import java.util.concurrent.TimeUnit

/**
 * Manages hydration reminder scheduling using WorkManager
 */
class ReminderManager(private val context: Context) {
    
    companion object {
        const val REMINDER_WORK_NAME = "hydration_reminder_work"
        const val DEFAULT_INTERVAL_MINUTES = 60L // Default: 1 hour
    }
    
    private val workManager = WorkManager.getInstance(context)
    
    /**
     * Schedules periodic hydration reminders
     * @param intervalMinutes Interval between reminders in minutes
     */
    fun scheduleHydrationReminders(intervalMinutes: Long = DEFAULT_INTERVAL_MINUTES) {
        // Cancel existing reminders first
        cancelHydrationReminders()
        
        // Create constraints for the work
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(true)
            .build()
        
        // Create periodic work request
        val reminderWork = PeriodicWorkRequestBuilder<HydrationReminderWorker>(
            intervalMinutes, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .addTag(REMINDER_WORK_NAME)
            .build()
        
        // Enqueue the work
        workManager.enqueueUniquePeriodicWork(
            REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            reminderWork
        )
    }
    
    /**
     * Cancels all hydration reminder notifications
     */
    fun cancelHydrationReminders() {
        workManager.cancelUniqueWork(REMINDER_WORK_NAME)
        NotificationHelper(context).cancelHydrationReminders()
    }
    
    /**
     * Checks if reminders are currently scheduled
     */
    fun areRemindersScheduled(): Boolean {
        val workInfos = workManager.getWorkInfosForUniqueWork(REMINDER_WORK_NAME).get()
        return workInfos.any { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING }
    }
    
    /**
     * Gets the current reminder interval in minutes
     */
    fun getCurrentReminderInterval(): Long {
        val workInfos = workManager.getWorkInfosForUniqueWork(REMINDER_WORK_NAME).get()
        return workInfos.firstOrNull()?.progress?.getLong("interval_minutes", DEFAULT_INTERVAL_MINUTES) 
            ?: DEFAULT_INTERVAL_MINUTES
    }
}
