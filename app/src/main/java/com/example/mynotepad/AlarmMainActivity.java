package com.example.mynotepad;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.example.common.AlarmNotification;
import com.example.common.AlarmReceiver;

import java.util.Calendar;

public class AlarmMainActivity extends AppCompatActivity {
    EditText et_hour;
    EditText et_min;
    EditText et_sec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_activity_main);
        et_hour = findViewById(R.id.et_hour);
        et_min = findViewById(R.id.et_min);
        et_sec = findViewById(R.id.et_second);


        Log.d("kyi123", "here!");
    }

    // 알람 스케쥴링 요청
    public void onClick(View view) {

        if (et_hour.getText().toString().equals("") && et_min.getText().toString().equals("") && et_sec.getText().toString().equals("")) {
            Toast.makeText(this,  "Not Started!! Please input all type of time", Toast.LENGTH_LONG);
        } else {
            EditText et = findViewById(R.id.notification_content);
            AlarmNotification.INSTANCE.setText(et.getText().toString());

            Intent intent = new Intent(getApplicationContext(), MyService.class);
            intent.putExtra("command", "show");
//        startService(intent);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent); // foreground service 실행을 위해 이것만 있으면 됨. 윗줄의 startService(intent)는 필요 없음.
            }
            diaryAlarm();
        }
    }

    // 알람 cancel 요청
    public void onClick2(View view) {
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        alarmIntent.putExtra("state","alarm on");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_IMMUTABLE);
        // 알람 Broadcast Intent를 만든다. -> alarmManager를 통해 특정시각에 broadcast 날리도록 예약할것이다.
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        // 사용자가 매일 알람을 허용했다면
        if (alarmManager != null) {
            //alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent); // 예약
            alarmManager.cancel(pendingIntent);
        }
    }

    // 일일 알림 상세
    public void diaryAlarm() {
        Boolean dailyNotify = true; // 무조건 알람을 사용
        PackageManager pm = this.getPackageManager();
        ComponentName receiver = new ComponentName(this, DeviceBootReceiver.class);
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        alarmIntent.putExtra("state","alarm on");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_IMMUTABLE);
        // 알람 Broadcast Intent를 만든다. -> alarmManager를 통해 특정시각에 broadcast 날리도록 예약할것이다.
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        // 사용자가 매일 알람을 허용했다면
        EditText et = findViewById(R.id.edit_text_millisecond);
        String text = et.getText().toString();
        Long sec = Long.parseLong(text);
        if (dailyNotify) {
            if (alarmManager != null) {
                //alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent); // 예약
                Log.d("kyi123", "sec = " + sec);
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(System.currentTimeMillis());

                cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(et_hour.getText().toString()));
                cal.set(Calendar.MINUTE, Integer.parseInt(et_min.getText().toString()));
                cal.set(Calendar.SECOND, Integer.parseInt(et_sec.getText().toString()));

                alarmManager.setInexactRepeating(
                        AlarmManager.RTC_WAKEUP,
                        cal.getTimeInMillis(),
                        sec,
                        pendingIntent);
            }
            // 부팅 후 실행되는 리시버 사용가능하게 설정
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
        }
    }


}
