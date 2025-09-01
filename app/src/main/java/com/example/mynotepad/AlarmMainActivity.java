package com.example.mynotepad;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import android.app.ActivityManager;

import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

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
    }

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    // Exact alarm 권한 확인
    private boolean hasExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            return alarmManager != null && alarmManager.canScheduleExactAlarms();
        }
        return true; // Android 12 미만에서는 권한이 필요하지 않음
    }

    // Exact alarm 권한 요청
    private void requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            startActivity(intent);
        }
    }

    // 알람 스케쥴링 요청
    public void onClickScheduleAlarm(View view) {

        if (et_hour.getText().toString().equals("") && et_min.getText().toString().equals("") && et_sec.getText().toString().equals("")) {
            Toast.makeText(this,  "Not Started!! Please input all type of time", Toast.LENGTH_LONG).show();
        } else {
            // Exact alarm 권한 확인
            if (!hasExactAlarmPermission()) {
                Toast.makeText(this, "Exact alarm permission is required", Toast.LENGTH_LONG).show();
                requestExactAlarmPermission();
                return;
            }

//            EditText et = findViewById(R.id.notification_content);
//            AlarmNotification.INSTANCE.setText(et.getText().toString());

            if (!isServiceRunning(this, MyService.class)) {
                Intent intent = new Intent(getApplicationContext(), MyService.class);
                intent.putExtra("command", "show");
//        startService(intent);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent); // foreground service 실행을 위해 이것만 있으면 됨. 윗줄의 startService(intent)는 필요 없음.
                }
            } else {
                Log.i("kongyi1220", "service is already running.");
            }
            diaryAlarm();
        }
    }

    // 알람 cancel 요청
    public void onClickCancel(View view) {
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
        Log.i("kongyi1220", "daily Alarm is registered.");
        PackageManager pm = this.getPackageManager();
        ComponentName receiver = new ComponentName(this, DeviceBootReceiver.class);
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        alarmIntent.setAction("alarm on");
        int alarmId = ThreadLocalRandom.current().nextInt(0, 9999);
        Log.i("kyi123", "alarm Id = " + alarmId);
        alarmIntent.putExtra("id", "alarmId = " + alarmId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_IMMUTABLE);
        // 알람 Broadcast Intent를 만든다. -> alarmManager를 통해 특정시각에 broadcast 날리도록 예약할것이다.
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        // 사용자가 매일 알람을 허용 했다면
        EditText et = findViewById(R.id.edit_text_millisecond);
        String text = et.getText().toString();
        Long sec = Long.parseLong(text);
        if (alarmManager != null) {
            Log.d("kyi123", "sec = " + sec);
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());

            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(et_hour.getText().toString()));
            cal.set(Calendar.MINUTE, Integer.parseInt(et_min.getText().toString()));
            cal.set(Calendar.SECOND, Integer.parseInt(et_sec.getText().toString()));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String formatted = sdf.format(cal.getTime());

            Log.d("kyi123", "시간: " + formatted);

            alarmManager.setExactAndAllowWhileIdle( // doze 모드 상태라 해도 예정된 시각에 정확히 알람
                    AlarmManager.RTC_WAKEUP,
                    cal.getTimeInMillis(),
                    pendingIntent);
        }
        // 부팅 후 실행 되는 리시버 사용 가능하게 설정
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }


}
