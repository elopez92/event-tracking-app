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
import androidx.lifecycle.ViewModelProvider;

import us.elopez.projecttwo.data.model.UserEntity;
import us.elopez.projecttwo.util.SecurityUtil;
import us.elopez.projecttwo.viewmodel.UserViewModel;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameET, passwordET;
    private Button loginButton , signupButton;
    private AppDatabase db;
    private UserViewModel userViewModel;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameET = findViewById(R.id.userNameET);
        passwordET = findViewById(R.id.passwordET);
        loginButton = findViewById(R.id.loginButton);
        signupButton = findViewById(R.id.signupButton);

        // Initialize database
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        db = AppDatabase.getInstance(this);
        userViewModel = new ViewModelProvider(this, new UserViewModel.Factory(db)).get(UserViewModel.class);

        loginButton.setOnClickListener(view -> {
            loginUser();
        });

        signupButton.setOnClickListener(view -> {
            signUp();
        });
    }

    private void loginUser() {
        String username = usernameET.getText().toString();
        String password = SecurityUtil.hashPassword(passwordET.getText().toString());

        userViewModel.login(username, password).observe(this, user -> {
            if (user != null) {
                Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                editor.putString("username", username);
                editor.putBoolean("is_logged_in", true);
                editor.apply();

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void signUp() {
        String username = usernameET.getText().toString();
        String password = SecurityUtil.hashPassword(passwordET.getText().toString());

        if (username.isEmpty() || password == null) {
            Toast.makeText(LoginActivity.this, "Please enter a username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        userViewModel.observeOnce(userViewModel.getUser(username), existingUser -> {
            if (existingUser == null) {
                UserEntity newUser = new UserEntity(username, password);

                    userViewModel.registerUser(newUser, new RegistrationCallback() {
                        @Override
                        public void onSuccess() {
                            editor.putString("username", username);
                            editor.apply();


                            runOnUiThread(() -> {
                                // Remove observer to prevent multiple calls
                                userViewModel.getUser(username).removeObservers(LoginActivity.this);
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                Toast.makeText(LoginActivity.this, "Signup successful", Toast.LENGTH_SHORT).show();
                                startActivity(intent);
                            });
                        }

                        @Override
                        public void onFailure(String message) {
                            runOnUiThread(() -> {
                                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
            }else {
                Toast.makeText(LoginActivity.this, "Username already taken", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
