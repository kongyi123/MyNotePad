package com.example.mynotepad
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.common.AlarmNotification
import com.example.common.R
import com.example.model.DataManager


// Notification ID.

private const val NOTIFICATION_ID = 2 // 0이면 절대 안됨. 노티가 안나온다.

class MyService : Service() {
    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    // 서비스쪽으로 데이터를 전달하고 싶은경우 자주 사용
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("kyi123", "onStartCommand()")

        if (intent != null) {
            if (DataManager.getNotificationState(this)) {
                Log.i("kongyiAAA", "getNotiState is true")
                createChannel(
                    getString(R.string.notification_channel_id),
                    getString(R.string.notification_channel_name)
                )

                startOnGoingNotification()
            }
            DataManager.getAllHistoryData(this)
            DataManager.get14daysSchedule(this, "id_list")
        } else {
            return Service.START_STICKY
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startOnGoingNotification() {
        Log.d("kyi123", "startForegroundService()")
        val intent = Intent(this, AccessActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP) // 액티비티 중복생성 하지 않게 하는 FLAG
        val notification = AlarmNotification.createNotification(this, intent) // foreground Noti
        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("kyi123", "onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("kyi123", "onDestroy")
    }


    // 채널 생성
    @SuppressLint("WrongConstant")
    fun createChannel(channelId: String?, channelName: String?) {
        Log.d("kyi123", "createChannel")
        var notificationChannel: NotificationChannel? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_MAX
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel!!.description =
                getString(R.string.breakfast_notification_channel_description)
            notificationChannel.enableVibration(true)
            notificationChannel.lockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
        }

//        notificationChannel.setShowBadge(false);
//        notificationChannel.enableLights(true);
//        notificationChannel.setLightColor(Color.RED);
//        notificationChannel.setShowBadge(true);
        var notificationManager: NotificationManager? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager = getSystemService(
                NotificationManager::class.java
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager!!.createNotificationChannel(notificationChannel!!)
        }
    }
}
