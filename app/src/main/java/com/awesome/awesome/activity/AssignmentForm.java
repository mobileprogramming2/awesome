package com.awesome.awesome.activity;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.awesome.awesome.R;
import com.awesome.awesome.Status;
import com.awesome.awesome.Priority;
import com.awesome.awesome.entity.Assignment;
import com.awesome.awesome.sql.SQLiteHelper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;

public class AssignmentForm extends AppCompatActivity {

    SQLiteHelper sqLiteHelper;
    private EditText inputDateTime;
    private EditText inputAssignmentName;
    private Button submitBtn;
    private EditText inputSubjectName;

    private LinearLayout statusLayout;
    private Spinner statusSpinner, prioritySpinner;
    private int selectedYear, selectedMonth, selectedDay;
    private int selectedHour, selectedMinute;
    private Status selectedStatus;
    private Priority selectedPriority;

    private Switch notificationSwitch;
    private static final String CHANNEL_ID = "assignment_channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.assignment_form);

        inputAssignmentName = (EditText) findViewById(R.id.assignmentNameEt);
        inputDateTime = (EditText) findViewById(R.id.dateTimeEt);
        submitBtn = (Button) findViewById(R.id.submitBtn);
        statusLayout = (LinearLayout) findViewById(R.id.statusLayout);
        statusSpinner = (Spinner) findViewById(R.id.statusSpinner);
        prioritySpinner = (Spinner) findViewById(R.id.prioritySpinner);
        inputSubjectName = (EditText) findViewById(R.id.subjectEt);
        notificationSwitch = findViewById(R.id.notificationSwitch);

        sqLiteHelper = new SQLiteHelper(getApplicationContext());

        // 우선 순위 Spinner 설정
        List<Priority> priorityList = List.of(Priority.HIGH, Priority.MEDIUM, Priority.LOW);
        ArrayAdapter<Priority> priorityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, priorityList);
        prioritySpinner.setAdapter(priorityAdapter);

        Assignment assignment = (Assignment) getIntent().getSerializableExtra("Assignment");
        if (assignment != null) {
            statusLayout.setVisibility(View.VISIBLE);
            inputAssignmentName.setText(assignment.getName());
            String endDateTime = assignment.getEndDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            inputDateTime.setText(endDateTime);

            List<Status> status = List.of(Status.WAITING, Status.ONGOING, Status.COMPLETE);
            ArrayAdapter<Status> myAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, status);
            statusSpinner.setAdapter(myAdapter);
            statusSpinner.setSelection(assignment.getStatus().ordinal());

            prioritySpinner.setSelection(Priority.PriorityToInt(assignment.getPriority()));  // 수정할 때 우선 순위 설정
            inputSubjectName.setText(assignment.getSubject());
        }

        statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedStatus = Status.intToStatus(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(getApplicationContext(), "상태를 선택해 주세요", Toast.LENGTH_SHORT).show();
            }
        });

        // 우선 순위 선택 리스너
        prioritySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedPriority = Priority.intToPriority(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(getApplicationContext(), "우선 순위를 선택해 주세요", Toast.LENGTH_SHORT).show();
            }
        });

        inputDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePicker();
            }
        });

        // 제출 버튼 클릭 시 데이터 처리
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String assignmentName = inputAssignmentName.getText().toString();
                String dateTime = inputDateTime.getText().toString();
                String subject = inputSubjectName.getText().toString();

                if (assignmentName.isEmpty() || dateTime.isEmpty()) {
                    Toast.makeText(AssignmentForm.this, "모든 필드를 입력하세요!", Toast.LENGTH_SHORT).show();
                } else {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    Assignment newAssignment = new Assignment(assignment != null ? assignment.getID() : -1, assignmentName, LocalDateTime.parse(dateTime, formatter),
                            selectedStatus, selectedPriority, subject);

                    if (assignment == null) {
                        // 생성
                        sqLiteHelper.insertNewAssignment(newAssignment);
                    } else {
                        // 수정
                        sqLiteHelper.modifyAssignments(assignment.getID(), newAssignment);
                    }

                    if (notificationSwitch.isChecked()) {
                        triggerNotification(assignmentName, dateTime);
                    }

                    finish();
                }
            }
        });

        // 뒤로 가기 버튼 설정
        Button backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // 현재 Activity를 종료하고 이전 화면으로 돌아감
            }
        });
    }

    private void showDateTimePicker() {
        // 현재 날짜 가져오기
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        // 날짜 선택 다이얼로그 표시
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    selectedYear = year;
                    selectedMonth = month;
                    selectedDay = dayOfMonth;

                    // 시간 선택 다이얼로그 표시
                    showTimePicker();
                },
                currentYear, currentMonth, currentDay);

        datePickerDialog.show();
    }

    // 시간 선택 다이얼로그 표시
    private void showTimePicker() {
        // 현재 시간 가져오기
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        // 시간 선택 다이얼로그 표시
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    selectedHour = hourOfDay;
                    selectedMinute = minute;

                    // 선택한 날짜와 시간을 EditText에 설정
                    String selectedDateTime = String.format("%04d-%02d-%02d %02d:%02d",
                            selectedYear,
                            (selectedMonth + 1),
                            selectedDay,
                            selectedHour,
                            selectedMinute);
                    inputDateTime.setText(selectedDateTime);
                },
                currentHour, currentMinute, true);

        timePickerDialog.show();
    }

    private static final int REQUEST_CODE_NOTIFICATION_PERMISSION = 1;

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Assignment Notifications";
            String description = "알림 설정된 과제에 대한 알림";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void triggerNotification(String assignmentName, String dateTime) {
        // 권한이 있는지 확인
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없으면 요청
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_NOTIFICATION_PERMISSION);
            return;
        }

        // 권한이 있으면 알림을 보냄
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // 알림 아이콘
                .setContentTitle("과제 알림")
                .setContentText(assignmentName + " - " + dateTime)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    // 권한 요청 결과 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 허용되었을 때 알림을 트리거
                triggerNotification("과제 이름", "날짜/시간");
            } else {
                // 권한이 거부된 경우 처리
                Toast.makeText(this, "알림 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}