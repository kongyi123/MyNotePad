package com.example.common

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

object MyNotification {
    private const val NOTIFICATION_ID = 1

    fun doNotify(context: Context, content:String, arg1:String = "") {
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(
            context,
            context.getString(R.string.notification_channel_id)
        )
        builder.setSmallIcon(R.drawable.ic_launcher_background)
        builder.setContentTitle(context.getString(R.string.app_name))
        builder.setContentText(content)
        builder.setAutoCancel(true)
        builder.setStyle(NotificationCompat.BigTextStyle().bigText(content)) // 노티 우측 expand 아이콘 눌렀을 경우 표시 내용
//        builder.setFullScreenIntent(contentPendingIntent, true)

        builder.priority = NotificationCompat.PRIORITY_MAX
        builder.setDefaults(Notification.DEFAULT_ALL)
        val snoozeIntent = Intent(context, AlarmReceiver::class.java)
        snoozeIntent.action = "snooze"
        snoozeIntent.putExtra("noti_id", 0)
        val snoozePendingIntent = PendingIntent.getBroadcast(context, 0, snoozeIntent, 0)
        builder.addAction(R.drawable.ic_launcher_background, "snooze", snoozePendingIntent)
        val notificationManager: NotificationManager =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                context.getSystemService<NotificationManager>(
                    NotificationManager::class.java
                )
            } else {
                TODO("VERSION.SDK_INT < M")
            }
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }
}