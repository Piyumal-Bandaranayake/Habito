package com.example.habito.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.habito.utils.NotificationHelper
import kotlinx.coroutines.delay

/**
 * WorkManager worker that handles hydration reminder notifications
 */
class HydrationReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    private val notificationHelper = NotificationHelper(applicationContext)
    
    override suspend fun doWork(): Result {
        return try {
            // Show the hydration reminder notification
            notificationHelper.showHydrationReminder()
            
            // Return success
            Result.success()
        } catch (e: Exception) {
            // Log error and return retry
            Result.retry()
        }
    }
}
