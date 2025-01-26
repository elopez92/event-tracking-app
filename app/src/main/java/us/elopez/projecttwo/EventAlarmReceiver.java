package us.elopez.projecttwo;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.widget.Toast;
import androidx.core.content.ContextCompat;

public class EventAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String eventName = intent.getStringExtra("eventName");
        String eventDateTime = intent.getStringExtra("eventDateTime");
        String phoneNumber = intent.getStringExtra("phoneNumber");

        // Show notification
        showNotification(context, "Event Reminder", "Event: " + eventName + " at " + eventDateTime);

        // Send SMS if permission is granted
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            sendSms(phoneNumber, "Reminder: " + eventName + " at " + eventDateTime);
        } else {
            Toast.makeText(context, "SMS permission not granted", Toast.LENGTH_SHORT).show();
        }
    }

    private void showNotification(Context context, String title, String message) {
        NotificationHelper.showNotification(context, title, message);
    }

    private void sendSms(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
}
