package us.elopez.projecttwo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import us.elopez.projecttwo.data.model.EventEntity;
import us.elopez.projecttwo.util.CustomSpinnerAdapter;
import us.elopez.projecttwo.viewmodel.EventViewModel;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADD_EVENT = 1;
    private RecyclerView recyclerView;
    private Button addEventButton;
    private EditText searchBar;
    private Spinner sortSpinner;
    private ImageView clearSearchIcon;
    private AppDatabase db;
    String username;
    private EventAdapter eventAdapter;
    private SharedPreferences sharedPreferences;
    private EventViewModel eventViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false);

        if (!isLoggedIn) {
            // Redirect to login if not logged in
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        addEventButton = findViewById(R.id.addEventButton);
        searchBar = findViewById(R.id.searchBar);
        sortSpinner = findViewById(R.id.sortSpinner);
        clearSearchIcon = findViewById(R.id.clearSearchIcon);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecyclerView.ItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(500);
        animator.setRemoveDuration(300);
        recyclerView.setItemAnimator(animator);
        eventAdapter = new EventAdapter();
        recyclerView.setAdapter(eventAdapter);

        clearSearchIcon.setOnClickListener(v -> {
            searchBar.setText("");
            clearSearchIcon.setVisibility(View.GONE);
        });

        // Add this in onCreate after setting up the RecyclerView
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            private final Drawable deleteIcon = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_delete);
            private final Drawable editIcon = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_edit);
            private final ColorDrawable deleteBackground = new ColorDrawable(Color.RED);
            private final ColorDrawable editBackground = new ColorDrawable(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary));
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false; // We don't want to support drag & drop, only swipe
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                EventEntity event = eventAdapter.getEventAt(position);

                if (direction == ItemTouchHelper.LEFT) {
                    AlertDialog dialog = new AlertDialog.Builder(MainActivity.this, R.style.CustomAlertDialog)
                            .setTitle("Confirm Deletion")
                            .setMessage("Are you sure you want to delete this event?")
                            .setPositiveButton("Delete", (dialogInterface, which) -> {
                                eventViewModel.deleteEvent(event);
                                eventAdapter.removeEventAt(position);
                                Toast.makeText(MainActivity.this, "Event deleted", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Cancel", (dialogInterface, which) -> {
                                eventAdapter.notifyItemChanged(position); // Reset swipe
                                dialogInterface.dismiss();
                            }).create();
                    dialog.setOnShowListener(d -> {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.holo_red_dark));
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.black));
                    });
                            dialog.show();

                } else if (direction == ItemTouchHelper.RIGHT) {
                    // Swipe right to edit
                    Intent intent = new Intent(MainActivity.this, EventDetailsActivity.class);
                    intent.putExtra("event_id", event.event_id); // Pass the event ID to load details for editing
                    startActivity(intent);
                    eventAdapter.notifyItemChanged(position); // Refresh the item after returning from edit
                }
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                View itemView = viewHolder.itemView;
                int iconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;

                if (dX > 0) { // Swiping to the right (Edit)
                    editBackground.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + (int) dX, itemView.getBottom());
                    editBackground.draw(c);

                    int iconLeft = itemView.getLeft() + iconMargin;
                    int iconTop = itemView.getTop() + iconMargin;
                    int iconRight = iconLeft + editIcon.getIntrinsicWidth();
                    int iconBottom = iconTop + editIcon.getIntrinsicHeight();

                    editIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    editIcon.draw(c);
                } else if (dX < 0) { // Swiping to the left (Delete)
                    deleteBackground.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                    deleteBackground.draw(c);

                    int iconLeft = itemView.getRight() - iconMargin - deleteIcon.getIntrinsicWidth();
                    int iconTop = itemView.getTop() + iconMargin;
                    int iconRight = itemView.getRight() - iconMargin;
                    int iconBottom = iconTop + deleteIcon.getIntrinsicHeight();

                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    deleteIcon.draw(c);
                }
            }
        };

// Attach the ItemTouchHelper to the RecyclerView
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // Get the logged-in user's username from SharedPreferences
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        username = sharedPreferences.getString("username", "");

        // Initialize database
        db = AppDatabase.getInstance(this);
        eventViewModel = new ViewModelProvider(this, new EventViewModel.Factory(db.eventDao(), username)).get(EventViewModel.class);

        eventViewModel.getEvents().observe(this, events -> {
            eventAdapter.setEvents(events);
        });

        // Set listener for adding a new event
        addEventButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, EventDetailsActivity.class);
            startActivity(intent);
        });

        eventAdapter.setOnItemClickListener(position -> {
            EventEntity event = eventAdapter.getEventAt(position);
            eventViewModel.deleteEvent(event);
            eventAdapter.removeEventAt(position);
            Toast.makeText(MainActivity.this, "Event deleted", Toast.LENGTH_SHORT).show();
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                eventViewModel.setFilterQuery(s.toString());
                clearSearchIcon.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // Sorting options
        String[] sortOptions = {"Sort by Date", "Sort by Name"};
        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(this, sortOptions);
        sortSpinner.setAdapter(adapter);

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    eventViewModel.setSortType(EventViewModel.SortType.BY_DATE);
                } else {
                    eventViewModel.setSortType(EventViewModel.SortType.BY_NAME);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
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

    /**
     * Logs out the user by clearing stored credentials and returning to the login screen.
     */
    private void signOut() {
        // Clear the saved username
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("username");
        editor.putBoolean("is_logged_in", false);
        editor.apply();

        // Navigate to the login activity and clear the activity stack
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}