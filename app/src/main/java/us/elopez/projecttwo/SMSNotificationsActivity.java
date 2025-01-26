package us.elopez.projecttwo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SMSNotificationsActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 1;

    private TextView smsPermissionExplanation;
    private Button requestSmsPermissionButton;
    private TextView smsStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smsnotifications);

        smsPermissionExplanation = findViewById(R.id.sms_permission_explanation);
        requestSmsPermissionButton = findViewById(R.id.request_sms_permission_button);
        smsStatus = findViewById(R.id.sms_status);

        requestSmsPermissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestSmsPermission();
            }
        });

        updateSmsStatus();
    }

    private void requestSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                smsStatus.setText("SMS Status: Granted");
            } else {
                smsStatus.setText("SMS Status: Not Granted");
            }
        }
    }

    private void updateSmsStatus() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            smsStatus.setText("SMS Status: Granted");
        } else {
            smsStatus.setText("SMS Status: Not Granted");
        }
    }
}
