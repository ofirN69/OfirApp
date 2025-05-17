package com.ofir.ofirapp.screens;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.ofir.ofirapp.R;
import com.ofir.ofirapp.adapters.UserNamAdapter;
import com.ofir.ofirapp.models.Event;
import com.ofir.ofirapp.models.User;
import com.ofir.ofirapp.services.DatabaseService;
import com.ofir.ofirapp.services.AuthenticationService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class addEVENT extends AppCompatActivity {
    // UI Components
    private CalendarView cvEventDate;
    private Spinner spinnerEventType;
    private RadioGroup rgFood;
    private RadioButton rbDairy, rbVegetarian, rbVegan, rbMeat;
    private TextView dateTextView;
    private EditText etVenueName, etDress, etAddress, etCity;
    private Button btnAddEvent;
    private ListView lvMembers, lvSelectedMembers;

    // Adapters
    private UserNamAdapter<User> availableUsersAdapter;
    private UserNamAdapter<User> selectedUsersAdapter;

    // Data
    private DatabaseService databaseService;
    private ArrayList<User> availableUsers;
    private ArrayList<User> selectedUsers;
    private String selectedEventType;
    private String selectedFood;
    private String selectedDate;
    private String uid;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        initializeData();
        initializeViews();
        setupListeners();
        loadUsers();
    }

    private void initializeData() {
        databaseService = DatabaseService.getInstance();
        availableUsers = new ArrayList<>();
        selectedUsers = new ArrayList<>();
        uid = AuthenticationService.getInstance().getCurrentUserId();
    }

    private void initializeViews() {
        // Initialize UI components
        btnAddEvent = findViewById(R.id.btnAddItem);
        cvEventDate = findViewById(R.id.cvEventDate);
        dateTextView = findViewById(R.id.dateTextView);
        spinnerEventType = findViewById(R.id.spItemTYpe);
        etVenueName = findViewById(R.id.etVenue);
        etDress = findViewById(R.id.etDressCode);
        etAddress = findViewById(R.id.etAdress);
        etCity = findViewById(R.id.etCity);
        rgFood = findViewById(R.id.RGFood);
        rbDairy = findViewById(R.id.rbDairy);
        rbVegetarian = findViewById(R.id.rbVegetarian);
        rbVegan = findViewById(R.id.rbVegan);
        rbMeat = findViewById(R.id.rbMeat);
        lvMembers = findViewById(R.id.lvMembers);
        lvSelectedMembers = findViewById(R.id.lvSelected);

        // Setup adapters
        availableUsersAdapter = new UserNamAdapter<>(this, 0, 0, availableUsers);
        selectedUsersAdapter = new UserNamAdapter<>(this, 0, 0, selectedUsers);

        lvMembers.setAdapter(availableUsersAdapter);
        lvSelectedMembers.setAdapter(selectedUsersAdapter);

        // Set default date
        setDefaultDate();
    }

    private void setDefaultDate() {
        long currentDate = cvEventDate.getDate();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        selectedDate = sdf.format(new Date(currentDate));
        dateTextView.setText("Selected Date: " + selectedDate);
    }

    private void setupListeners() {
        btnAddEvent.setOnClickListener(v -> validateAndCreateEvent());

        cvEventDate.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
            dateTextView.setText("Selected Date: " + selectedDate);
        });

        lvMembers.setOnItemClickListener((parent, view, position, id) -> {
            User selectedUser = availableUsers.get(position);
            if (!isUserAlreadySelected(selectedUser)) {
                selectedUsers.add(selectedUser);
                selectedUsersAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(addEVENT.this, "User already added to event", Toast.LENGTH_SHORT).show();
            }
        });

        lvSelectedMembers.setOnItemClickListener((parent, view, position, id) -> {
            selectedUsers.remove(position);
            selectedUsersAdapter.notifyDataSetChanged();
        });
    }

    private boolean isUserAlreadySelected(User user) {
        for (User selectedUser : selectedUsers) {
            if (selectedUser.getId().equals(user.getId())) {
                return true;
            }
        }
        return false;
    }

    private void loadUsers() {
        databaseService.getUsers(new DatabaseService.DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {
                availableUsers.clear();
                availableUsers.addAll(users);
                availableUsersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailed(Exception e) {
                Log.e("TAG", "Failed to load users", e);
                Toast.makeText(addEVENT.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });

        databaseService.getUser(uid, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                currentUser = user;
            }

            @Override
            public void onFailed(Exception e) {
                Log.e("TAG", "Failed to load current user", e);
            }
        });
    }

    private void validateAndCreateEvent() {
        if (!validateInputs()) {
            return;
        }

        selectedEventType = spinnerEventType.getSelectedItem().toString();
        selectedFood = getSelectedFoodType();

        String eventId = databaseService.generateEventId();
        Event newEvent = new Event(
            eventId,
            selectedEventType,
            selectedDate,
            etVenueName.getText().toString(),
            etAddress.getText().toString(),
            etCity.getText().toString(),
            etDress.getText().toString(),
            "new",
            selectedFood,
            uid,
            selectedUsers
        );

        createEvent(newEvent);
    }

    private boolean validateInputs() {
        if (TextUtils.isEmpty(etVenueName.getText())) {
            showError("Please enter the venue name");
            return false;
        }
        if (TextUtils.isEmpty(etAddress.getText())) {
            showError("Please enter the address");
            return false;
        }
        if (TextUtils.isEmpty(etCity.getText())) {
            showError("Please enter the city");
            return false;
        }
        if (TextUtils.isEmpty(etDress.getText())) {
            showError("Please enter the dress code");
            return false;
        }
        if (spinnerEventType.getSelectedItemPosition() == 0) {
            showError("Please select a valid event type");
            return false;
        }
        if (rgFood.getCheckedRadioButtonId() == -1) {
            showError("Please select a food preference");
            return false;
        }
        if (TextUtils.isEmpty(selectedDate)) {
            showError("Please select a date");
            return false;
        }

        // Check if selected date has passed
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            Date selectedEventDate = sdf.parse(selectedDate);
            Date currentDate = new Date();
            
            // Set time to beginning of day for fair comparison
            currentDate = sdf.parse(sdf.format(currentDate));
            
            if (selectedEventDate.before(currentDate)) {
                showError("Cannot create an event for a past date");
                return false;
            }
        } catch (Exception e) {
            Log.e("TAG", "Date parsing error", e);
            showError("Invalid date format");
            return false;
        }

        return true;
    }

    private String getSelectedFoodType() {
        int selectedId = rgFood.getCheckedRadioButtonId();
        if (selectedId == rbDairy.getId()) return "Dairy";
        if (selectedId == rbVegetarian.getId()) return "Vegetarian";
        if (selectedId == rbVegan.getId()) return "Vegan";
        if (selectedId == rbMeat.getId()) return "Meat";
        return "";
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void createEvent(Event newEvent) {
        // First create the event in the main events collection
        databaseService.createNewEvent(newEvent, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
                Log.d("TAG", "Event created successfully with ID: " + newEvent.getId());
                
                // Add event to creator's events list
                databaseService.addEventToUser(uid, newEvent, new DatabaseService.DatabaseCallback<Void>() {
                    @Override
                    public void onCompleted(Void object) {
                        Log.d("TAG", "Event added to creator's list: " + uid);
                        
                        // Add event to selected users' lists
                        for (User user : selectedUsers) {
                            databaseService.addEventToUser(user.getId(), newEvent, new DatabaseService.DatabaseCallback<Void>() {
                                @Override
                                public void onCompleted(Void object) {
                                    Log.d("TAG", "Event added to user's list: " + user.getId());
                                }

                                @Override
                                public void onFailed(Exception e) {
                                    Log.e("TAG", "Failed to add event to user: " + user.getId(), e);
                                }
                            });
                        }

                        // Navigate only after all operations are complete
                        navigateToAfterLogPage();
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Log.e("TAG", "Failed to add event to creator's list: " + e.getMessage());
                        // Still navigate even if adding to creator's list fails
                        navigateToAfterLogPage();
                    }
                });
            }

            @Override
            public void onFailed(Exception e) {
                Log.e("TAG", "Failed to create event: " + e.getMessage());
                Toast.makeText(addEVENT.this, "Failed to create event", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToAfterLogPage() {
        Intent intent = new Intent(addEVENT.this, AfterLogPage.class);
        startActivity(intent);
        finish();
    }
}





