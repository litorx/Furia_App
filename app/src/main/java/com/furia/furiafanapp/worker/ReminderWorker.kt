package com.furia.furiafanapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.furia.furiafanapp.utils.NotificationHelper

/**
 * Worker to trigger match reminder notifications.
 * Expects inputData with "matchId" and optional "matchTimeMillis".
 */
class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        // Retrieve match ID
        val matchId = inputData.getString("matchId")
            ?: return Result.failure()
        // Send notification
        NotificationHelper(applicationContext).sendMatchNotification(matchId)
        return Result.success()
    }
}
