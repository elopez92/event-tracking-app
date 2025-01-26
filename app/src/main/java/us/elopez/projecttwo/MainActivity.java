package us.elopez.projecttwo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADD_EVENT = 1;
    private RecyclerView recyclerView;
    private Button addEventButton;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private EventAdapter eventAdapter;
    private List<Event> eventList = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        addEventButton = findViewById(R.id.addEventButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(eventList);
        recyclerView.setAdapter(eventAdapter);

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase();

        // Get the logged-in user's username from SharedPreferences
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        username = sharedPreferences.getString("username", "");

        displayEvents();

        addEventButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, EventDetailsActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ADD_EVENT);
        });

        eventAdapter.setOnItemClickListener(position -> {
            Event event = eventList.get(position);
            db.execSQL("DELETE FROM events WHERE event_id=?", new String[]{String.valueOf(event.getId())});
            eventList.remove(position);
            eventAdapter.notifyItemRemoved(position);
            Toast.makeText(MainActivity.this, "Event deleted", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_EVENT && resultCode == RESULT_OK) {
            if (data != null) {
                String eventName = data.getStringExtra("eventName");
                String eventDateTime = data.getStringExtra("eventDateTime");
                addEventToList(eventName, eventDateTime);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_signout) {
            signOut();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void signOut() {
        // Clear the saved username
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("username");
        editor.apply();

        // Navigate to the login activity and clear the activity stack
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    private void displayEvents() {
        eventList.clear();
        Cursor cursor = db.rawQuery("SELECT * FROM events WHERE username=?", new String[]{username});
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String datetime = cursor.getString(2);
                Event event = new Event(id, name, datetime);
                eventList.add(event);
                eventAdapter.notifyItemInserted(eventList.size() - 1);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void addEventToList(String name, String datetime) {
        Cursor cursor = db.rawQuery("SELECT * FROM events WHERE event_name=? AND event_datetime=? AND username=?", new String[]{name, datetime, username});
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex("event_id"));
            Event event = new Event(id, name, datetime);
            eventList.add(event);
            eventAdapter.notifyItemInserted(eventList.size() - 1);
        }
        cursor.close();
    }
}