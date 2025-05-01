package com.furia.furiafanapp.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationCompat.BigTextStyle
import android.app.PendingIntent

/**
 * Helper to manage notification channel and send match reminders.
 */
class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "match_reminder_channel"
        private const val CHANNEL_NAME = "Match Reminders"
        private const val CHANNEL_DESC = "Notifications for upcoming matches"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun sendMatchNotification(matchId: String) {
        val notificationId = matchId.hashCode()
        // Intent para abrir o app na tela principal
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("furiafanapp://chat/$matchId")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.star_big_on)
            .setContentTitle("🔔 Partida em 1h!")
            .setContentText("Sua partida começará em 1 hora. Toque para mais detalhes.")
            .setStyle(BigTextStyle().bigText("Sua partida começará em 1 hora. Abra o app para mais informações."))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }
}
