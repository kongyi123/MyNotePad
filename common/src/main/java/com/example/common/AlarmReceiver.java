package com.example.common;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    private int NOTIFICATION_ID = 1;
    private int REQUEST_CODE = 0;
    private int FLAGS = 0;

    Context context;
    @Override
    public void onReceive(Context context, Intent intent) {
        // 알람을 BroadcastReceiver로 받는다.
        // 받으면 Noti를 주면됨.
        this.context = context;
        Log.i("kyi123", "Alarm Received");
        if (intent.getAction() != null) {

            if (intent.getAction().equals("snooze")) {
                Log.i("kyi123", "Alarm Stop");

            } else {
                Log.i("kyi123", "Alarm Else");

                doNotify(); // 노티
                //        MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.alim); // 소리
                //        mediaPlayer.start();
                ringVibration(); // 진동
            }
        }
    }
//
    private void doNotify() {
        Log.d("kyi123", "doNotify");

        String content = AlarmNotification.INSTANCE.getText();

        // sub noti
//        Intent contentIntent = new Intent(context, PaperWeightActivity.class);
//        PendingIntent contentPendingIntent = PendingIntent.getActivity(
//                        context,
//                        NOTIFICATION_ID,
//                        contentIntent,
//                        PendingIntent.FLAG_UPDATE_CURRENT
//        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context,
                context.getString(com.example.common.R.string.notification_channel_id)
        );

        builder.setSmallIcon(com.example.common.R.drawable.ic_launcher_background);
        builder.setContentTitle(context.getString(com.example.common.R.string.app_name));
        builder.setContentText(content);
//        builder.setContentIntent(contentPendingIntent);
        builder.setAutoCancel(true);
//        builder.setFullScreenIntent(contentPendingIntent, true);
//        builder.setOngoing(true); // Ongoing 붙이면 wear로 noti 안감.

//        builder.setStyle(R.drawable.ic_launcher_background);
//        builder.setLargeIcon(R.drawable.ic_launcher_background);
//        builder.addAction(R.drawable.ic_launcher_foreground,
//                context.getString(R.string.app_name),
//                );
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        builder.setDefaults(Notification.DEFAULT_ALL);
        Intent snoozeIntent = new Intent(context, AlarmReceiver.class);
        snoozeIntent.setAction("snooze");
        snoozeIntent.putExtra("noti_id", 0);
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(context, 0, snoozeIntent, PendingIntent.FLAG_IMMUTABLE);
        builder.addAction(com.example.common.R.drawable.ic_launcher_background, "snooze", snoozePendingIntent);
        NotificationManager notificationManager = context.getSystemService(
                NotificationManager.class
        );
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void ringVibration() {
        Log.d("kyi123", "ringVibration");

        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        long[] timings = new long[]{1000L, 30L, 1000L, 30L, 1000L}; // <- 패턴
        int[] amplitudes = new int[]{0, 100, 200, 100, 200};
        //vibrator.vibrate(VibrationEffect.createOneShot(1000, 50));
        vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes,0)); // 0 -> 반복 -1 -> 반복하지 말라
//        vibrator.cancel(); <- 중단 시키기
    }

}
