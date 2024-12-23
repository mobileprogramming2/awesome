package com.awesome.awesome;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.awesome.awesome.sql.SQLiteHelper;
import com.awesome.awesome.tab.Complete;
import com.awesome.awesome.tab.Incomplete;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {

    SQLiteHelper sqLiteHelper;
    Complete complete;
    Incomplete incomplete;

    private static final String CHANNEL_ID = "my_channel_id";  // 알림 채널의 고유 ID
    private static final int NOTIFICATION_ID = 1;  // 알림의 고유 ID

    @Override
    protected void onDestroy() {


        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();

        sqLiteHelper = new SQLiteHelper(getApplicationContext());

        complete = new Complete();
        incomplete = new Incomplete();

        getSupportFragmentManager().beginTransaction().add(R.id.list, incomplete).commit();

        TabLayout tabs = findViewById(R.id.tabs);

        tabs.addTab(tabs.newTab().setText("미완료"));
        tabs.addTab(tabs.newTab().setText("완료"));

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();

                Fragment selected = null;
                if (position == 0) {
                    selected = incomplete;
                } else if (position == 1) {
                    selected = complete;
                }

                getSupportFragmentManager().beginTransaction().replace(R.id.list, selected).commit();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        Switch notificationSwitch = findViewById(R.id.notificationSwitch);
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String message = isChecked ? "알림이 활성화되었습니다." : "알림이 비활성화되었습니다.";
            sendNotification("알림", message);
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
        });

        // 알림 권한 요청
        askNotificationPermission();
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(findViewById(android.R.id.content), "알림이 허용되었습니다.", Snackbar.LENGTH_SHORT).show();
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                Snackbar.make(findViewById(android.R.id.content), "알림을 허용해주세요.", Snackbar.LENGTH_SHORT)
                        .setAction("설정", v -> {
                            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    .setData(Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                        })
                        .show();
            } else {
                // 권한 요청
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }
    }

    // 권한 요청 결과 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(findViewById(android.R.id.content), "알림이 허용되었습니다.", Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(findViewById(android.R.id.content), "알림을 허용하지 않으셨습니다.", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    // 알림 채널 생성
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "과제 알림 채널",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("과제 알림을 위한 채널입니다.");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    // 알림 보내기
    private void sendNotification(String title, String message) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없으면 알림을 보내지 않음
            return;
        }

        createNotificationChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}