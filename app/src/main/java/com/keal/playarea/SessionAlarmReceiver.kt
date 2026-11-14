package com.keal.playarea

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SessionAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(EXTRA_ID, -1L)
        if (id <= 0L) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                SessionStore.initialize(context, clearPendingDeletes = false)
                val session = SessionStore.getById(context, id) ?: return@launch
                if (session.status != SessionStatus.ACTIVE) return@launch
                ensureChannel(context)
                val notification = NotificationCompat.Builder(context, CHANNEL)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle("Play time is up")
                    .setContentText("${session.name}'s 30-minute play session has ended.")
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setAutoCancel(true)
                    .build()
                try {
                    NotificationManagerCompat.from(context).notify(id.hashCode(), notification)
                } catch (_: SecurityException) {
                    // Notifications may be disabled by the device owner.
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val CHANNEL = "play_session_alerts"
        const val EXTRA_ID = "session_id"

        fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= 26) {
                val channel = NotificationChannel(
                    CHANNEL,
                    context.getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = context.getString(R.string.notification_channel_description)
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 450, 180, 450, 180, 700)
                    setSound(
                        android.provider.Settings.System.DEFAULT_NOTIFICATION_URI,
                        AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build()
                    )
                }
                context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
            }
        }

        private fun pendingIntent(context: Context, id: Long): PendingIntent {
            val intent = Intent(context, SessionAlarmReceiver::class.java).putExtra(EXTRA_ID, id)
            return PendingIntent.getBroadcast(
                context,
                id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        fun schedule(context: Context, session: PlaySession) {
            val alarms = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val operation = pendingIntent(context, session.id)
            if (Build.VERSION.SDK_INT >= 31 && alarms.canScheduleExactAlarms()) {
                alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, session.endsAt, operation)
            } else {
                alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, session.endsAt, operation)
            }
        }

        fun cancel(context: Context, id: Long) {
            val operation = pendingIntent(context, id)
            (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(operation)
            NotificationManagerCompat.from(context).cancel(id.hashCode())
        }
    }
}
