package com.example.common

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.common.R

// Notification ID.
private const val NOTIFICATION_ID = 1

object AlarmNotification {

    var text:String? = null

    fun createNotification(
        context: Context, intent:Intent
    ) : Notification { // default noti
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // from the S OS. FLAG immutable should be added here.
        )

        val builder = NotificationCompat.Builder(
            context,
            context.getString(R.string.notification_channel_id)
        )

        builder.setSmallIcon(R.drawable.ic_launcher_background)
        builder.setContentTitle(context.getString(R.string.app_name))
        builder.setContentText("앱에 접근하려면 알림을 터치하세요.")
        builder.setContentIntent(contentPendingIntent)
//        builder.setAutoCancel(true)
//        builder.setOngoing(true) // 이건 서비스의 startForeground()의 인자로 들어가는 노티라서 안넣어도 강제로 자동 셋 되는것 같음.

        builder.priority = NotificationCompat.PRIORITY_HIGH
        Log.d("kyi123", "made builder.build()")
        return builder.build()
    }
}