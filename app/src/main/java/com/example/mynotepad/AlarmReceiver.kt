package com.example.mynotepad

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.common.R

class AlarmReceiver : BroadcastReceiver() {
    private val NOTIFICATION_ID = 1
    private val REQUEST_CODE = 0
    private val FLAGS = 0

    var context: Context? = null
    override fun onReceive(context: Context, intent: Intent) {
        // 알람을 BroadcastReceiver로 받는다.
        // 받으면 Noti를 주면됨.
        this.context = context
        Log.i("kyi123", "Alarm Received")
        if (intent.action != null) {
            if (intent.action == "snooze") {
                Log.i("kyi123", "Alarm Stop")
                stopVibration()
                cancelNotification(context, NOTIFICATION_ID)
            } else {
                Log.i("kyi123", "Alarm Else")

                doNotify() // 노티
                //        MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.alim); // 소리
                //        mediaPlayer.start();
                ringVibration() // 진동
            }
        }
    }

    //
    private fun doNotify() {
        Log.d("kyi123", "doNotify")

        val content = ""

        // sub noti
//        Intent contentIntent = new Intent(context, PaperWeightActivity.class);
//        PendingIntent contentPendingIntent = PendingIntent.getActivity(
//                        context,
//                        NOTIFICATION_ID,
//                        contentIntent,
//                        PendingIntent.FLAG_UPDATE_CURRENT
//        );
        val builder = NotificationCompat.Builder(
            context!!,
            context!!.getString(R.string.notification_channel_id)
        )

        builder.setSmallIcon(R.drawable.ic_launcher_background)
        builder.setContentTitle(context!!.getString(R.string.app_name))
        builder.setContentText(content)
        //        builder.setContentIntent(contentPendingIntent);
        builder.setAutoCancel(true)

        //        builder.setFullScreenIntent(contentPendingIntent, true);
//        builder.setOngoing(true); // Ongoing 붙이면 wear로 noti 안감.

//        builder.setStyle(R.drawable.ic_launcher_background);
//        builder.setLargeIcon(R.drawable.ic_launcher_background);
//        builder.addAction(R.drawable.ic_launcher_foreground,
//                context.getString(R.string.app_name),
//                );
        builder.setPriority(NotificationCompat.PRIORITY_MAX)
        builder.setDefaults(Notification.DEFAULT_ALL)
        val snoozeIntent = Intent(context, AlarmReceiver::class.java)
        snoozeIntent.setAction("snooze")
        snoozeIntent.putExtra("noti_id", 0)
        val snoozePendingIntent =
            PendingIntent.getBroadcast(context, 0, snoozeIntent, PendingIntent.FLAG_IMMUTABLE)
        builder.addAction(R.drawable.ic_launcher_background, "snooze", snoozePendingIntent)
        val notificationManager = context!!.getSystemService(
            NotificationManager::class.java
        )
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    fun cancelNotification(ctx: Context, id: Int, tag: String? = null) {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (tag == null) nm.cancel(id) else nm.cancel(tag, id)
        // 모두 지우기: nm.cancelAll()
    }

    private fun ringVibration() {
        Log.d("kyi123", "ringVibration")

        val vibrator = context!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val timings = longArrayOf(1000L, 30L, 1000L, 30L, 1000L) // <- 패턴
        val amplitudes = intArrayOf(0, 100, 200, 100, 200)
        //vibrator.vibrate(VibrationEffect.createOneShot(1000, 50));
        vibrator.vibrate(
            VibrationEffect.createWaveform(
                timings,
                amplitudes,
                0
            )
        ) // 0 -> 반복 -1 -> 반복하지 말라
        //        vibrator.cancel(); <- 중단 시키기
    }


    private fun stopVibration() {
        Log.d("kyi123", "ringVibration")

        val vibrator = context!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.cancel()
    }
}