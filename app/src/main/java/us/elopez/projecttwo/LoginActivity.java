package us.elopez.projecttwo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameET;
    private EditText passwordET;
    private Button loginButton;
    private Button signupButton;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameET = findViewById(R.id.userNameET);
        passwordET = findViewById(R.id.passwordET);
        loginButton = findViewById(R.id.loginButton);
        signupButton = findViewById(R.id.signupButton);

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase();
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);

        loginButton.setOnClickListener(view -> {
            String username = usernameET.getText().toString();
            String password = passwordET.getText().toString();
            if (checkLogin(username, password)) {
                Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            }
        });

        signupButton.setOnClickListener(view -> {
            String username = usernameET.getText().toString();
            String password = passwordET.getText().toString();
            if (!username.isEmpty() && !password.isEmpty()) {
                registerUser(username, password);
                Toast.makeText(LoginActivity.this, "Signup successful", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(LoginActivity.this, "Please enter a username and password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean checkLogin(String username, String password) {
        boolean result = false;
        if(username.isEmpty() || password.isEmpty())
            return result;
        try {
            String query = "SELECT * FROM users WHERE username=? AND password=?";
            Cursor cursor = db.rawQuery(query, new String[]{username, password});
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    cursor.close();
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("username", username);
                    editor.apply();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    cursor.close();
                    Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Database query failed", Toast.LENGTH_SHORT).show();
            }
        } catch (SQLiteException e){
            Toast.makeText(LoginActivity.this, "Database error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return result;
    }

    private void registerUser(String username, String password) {
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE username=?", new String[]{username});
        if (cursor.getCount() > 0) {
            Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
        } else {
            db.execSQL("INSERT INTO users VALUES(?, ?);", new String[]{username, password});
        }
        cursor.close();
    }
}
