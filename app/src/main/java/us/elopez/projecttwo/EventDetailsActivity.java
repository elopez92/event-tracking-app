package us.elopez.projecttwo;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import us.elopez.projecttwo.data.model.EventEntity;
import us.elopez.projecttwo.viewmodel.EventViewModel;

public class EventDetailsActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;

    private EditText eventNameEditText, phoneNumberEditText;
    private TextView eventDateTextView, eventTimeTextView;
    private Button saveButton, dateButton, timeButton, cancelButton;
    private int year, month, day, hour, minute;
    private SharedPreferences sharedPreferences;
    private String username;
    private EventViewModel eventViewModel;
    private AppDatabase db;
    private int eventId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        eventNameEditText = findViewById(R.id.eventName);
        phoneNumberEditText = findViewById(R.id.phoneNumberET);
        eventDateTextView = findViewById(R.id.eventDate);
        eventTimeTextView = findViewById(R.id.eventTime);
        saveButton = findViewById(R.id.saveButton);
        dateButton = findViewById(R.id.dateButton);
        timeButton = findViewById(R.id.timeButton);
        cancelButton = findViewById(R.id.cancelButton);

        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        username = sharedPreferences.getString("username", "");

        db = AppDatabase.getInstance(this);
        eventViewModel = new ViewModelProvider(this, new EventViewModel.Factory(db.eventDao(), username)).get(EventViewModel.class);

        NotificationHelper.createNotificationChannel(this);

        cancelButton.setOnClickListener(view ->{
            finish();
        });

        // In onCreate of EventDetailsActivity
        eventId = getIntent().getIntExtra("event_id", -1);
        if (eventId != -1) {
            eventViewModel.getEventById(eventId).observe(this, event -> {
                if (event != null) {
                    eventNameEditText.setText(event.event_name);
                    eventDateTextView.setText(event.event_datetime);
                }
            });
        }


        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(EventDetailsActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                eventDateTextView.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", month + 1, dayOfMonth, year));
                                EventDetailsActivity.this.year = year;
                                EventDetailsActivity.this.month = month;
                                EventDetailsActivity.this.day = dayOfMonth;
                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
        });

        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                hour = calendar.get(Calendar.HOUR_OF_DAY);
                minute = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(EventDetailsActivity.this, R.style.CustomTimePickerDialog,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                eventTimeTextView.setText(String.format(Locale.getDefault(), "%02d:%02d %s", hourOfDay % 12 == 0 ? 12 : hourOfDay % 12, minute, hourOfDay >= 12 ? "PM" : "AM"));
                                EventDetailsActivity.this.hour = hourOfDay;
                                EventDetailsActivity.this.minute = minute;
                            }
                        }, hour, minute, false);
                timePickerDialog.show();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               saveEvent();
            }
        });
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.SEND_SMS,
                            Manifest.permission.POST_NOTIFICATIONS},
                    PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (!allPermissionsGranted) {
                Toast.makeText(this, "All permissions are required to use this feature", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void scheduleNotification(String eventName, String eventDateTime, String phoneNumber) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.getDefault());
        try {
            Date eventDate = dateFormat.parse(eventDateTime);
            Calendar eventCalendar = Calendar.getInstance();
            if (eventDate != null) {
                eventCalendar.setTime(eventDate);
            }

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if(!alarmManager.canScheduleExactAlarms()) {
                System.out.println("Exact alarm permission not granted. Skipping alarm ");
                requestExactAlarmPermission();
                return;
            }

            // Set notification for the start of the event day
            Calendar notificationCalendar = Calendar.getInstance();
            notificationCalendar.set(eventCalendar.get(Calendar.YEAR), eventCalendar.get(Calendar.MONTH), eventCalendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);

            Calendar now = Calendar.getInstance();
            if (notificationCalendar.before(now)) {
                // If the notification time is in the past (same day), send it immediately
                triggerImmediateNotification(eventName, eventDateTime, phoneNumber);
            } else {
                // Schedule it for the start of the event day
                Intent intent = new Intent(this, EventAlarmReceiver.class);
                intent.putExtra("eventName", eventName);
                intent.putExtra("eventDateTime", eventDateTime);
                intent.putExtra("phoneNumber", phoneNumber);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                try {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationCalendar.getTimeInMillis(), pendingIntent);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void triggerImmediateNotification(String eventName, String eventDateTime, String phoneNumber) {
        Intent intent = new Intent(this, EventAlarmReceiver.class);
        intent.putExtra("eventName", eventName);
        intent.putExtra("eventDateTime", eventDateTime);
        intent.putExtra("phoneNumber", phoneNumber);
        sendBroadcast(intent);
    }

    private void saveEvent() {
        try {
            String eventName = eventNameEditText.getText().toString();
            String eventDate = eventDateTextView.getText().toString();
            String eventTime = eventTimeTextView.getText().toString();
            String phoneNumber = phoneNumberEditText.getText().toString();
            String username = sharedPreferences.getString("username", "");
            String eventDateTime = eventDate + " " + eventTime;

            // Ensure the date and time format is consistent
            SimpleDateFormat inputDateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.getDefault());

            try {
                Date date = inputDateFormat.parse(eventDateTime);
                if (date != null) {
                    eventDateTime = inputDateFormat.format(date);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if(eventId != -1){
                EventEntity updatedEvent = new EventEntity(eventId, eventName, eventDateTime, username);

                // Insert event using viewmodel
                eventViewModel.updateEvent(updatedEvent);
                Toast.makeText(this, "Event updated", Toast.LENGTH_SHORT).show();
            }else {
                EventEntity newEvent = new EventEntity(eventName, eventDateTime, username);
                // Insert event using viewmodel
                eventViewModel.insertEvent(newEvent);
                Toast.makeText(this, "Event added", Toast.LENGTH_SHORT).show();
            }

            requestPermissions();

            scheduleNotification(eventName, eventDateTime, phoneNumber);

            finish();
        }catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving event. Please try again", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestExactAlarmPermission() {
        Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
        Toast.makeText(this, "Please enable exact alarm permission in settings", Toast.LENGTH_LONG).show();
    }

}
