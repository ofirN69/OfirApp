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

import com.ofir.ofirapp.R;
import com.ofir.ofirapp.adapters.UserNamAdapter;
import com.ofir.ofirapp.models.Event;
import com.ofir.ofirapp.models.User;
import com.ofir.ofirapp.services.AuthenticationService;
import com.ofir.ofirapp.services.DatabaseService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class addEVENT extends BaseActivity implements AdapterView.OnItemSelectedListener {
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
    private UserNamAdapter availableUsersAdapter;
    private UserNamAdapter selectedUsersAdapter;

    // Data
    private DatabaseService databaseService;
    private AuthenticationService authenticationService;
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

        databaseService = DatabaseService.getInstance();
        authenticationService = AuthenticationService.getInstance();
        setActionBarTitle("הוספת אירוע");
    }

    private void initializeData() {
        availableUsers = new ArrayList<>();
        selectedUsers = new ArrayList<>();
        
        // Initialize services early
        try {
            databaseService = DatabaseService.getInstance();
            authenticationService = AuthenticationService.getInstance();
            uid = authenticationService.getCurrentUserId();
            if (uid == null) {
                showError("שגיאה: משתמש לא אומת");
                navigateToAfterLogPage();
            }
        } catch (Exception e) {
            Log.e("TAG", "Failed to initialize services", e);
            showError("שגיאה בהתחלת שירותי האפליקציה");
            navigateToAfterLogPage();
        }
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
        availableUsersAdapter = new UserNamAdapter(this, 0, 0, availableUsers, false);
        selectedUsersAdapter = new UserNamAdapter(this, 0, 0, selectedUsers, true);

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
                availableUsersAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(addEVENT.this, "User already added to event", Toast.LENGTH_SHORT).show();
            }
        });

        lvSelectedMembers.setOnItemClickListener((parent, view, position, id) -> {
            selectedUsers.remove(position);
            selectedUsersAdapter.notifyDataSetChanged();
            availableUsersAdapter.notifyDataSetChanged();
        });
    }

    public boolean isUserAlreadySelected(User user) {
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
                for (User user : users) {
                    if (!user.getId().equals(uid)) {
                        availableUsers.add(user);
                    }
                }
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
        Log.d("AddEvent", "Starting event validation");
        if (!validateInputs()) {
            Log.d("AddEvent", "Basic input validation failed");
            return;
        }

        try {
            // Add null checks for spinner
            if (spinnerEventType.getSelectedItem() == null) {
                Log.d("AddEvent", "Event type spinner is null");
                showError("אנא בחר סוג אירוע");
                return;
            }

            // Add null checks for database services
            if (databaseService == null || authenticationService == null) {
                Log.e("AddEvent", "Database services not initialized. DB: " + (databaseService == null) + ", Auth: " + (authenticationService == null));
                showError("שגיאה: השירותים לא אותחלו כראוי");
                return;
            }

            selectedEventType = spinnerEventType.getSelectedItem().toString();
            selectedFood = getSelectedFoodType();
            Log.d("AddEvent", "Selected event type: " + selectedEventType + ", food: " + selectedFood);

            // Validate that at least one member is selected
            if (selectedUsers.isEmpty()) {
                Log.d("AddEvent", "No members selected");
                showError("אנא בחר לפחות משתתף אחד לאירוע");
                return;
            }

            Log.d("AddEvent", "All validations passed, creating event");
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
        } catch (Exception e) {
            Log.e("AddEvent", "Unexpected error during event creation", e);
            showError("אירעה שגיאה לא צפויה. אנא נסה שוב.");
        }
    }

    private boolean validateInputs() {
        if (TextUtils.isEmpty(etVenueName.getText())) {
            showError("אנא הכנס את שם המקום");
            return false;
        }
        if (TextUtils.isEmpty(etAddress.getText())) {
            showError("אנא הכנס כתובת");
            return false;
        }
        if (TextUtils.isEmpty(etCity.getText())) {
            showError("אנא הכנס עיר");
            return false;
        }
        if (TextUtils.isEmpty(etDress.getText())) {
            showError("אנא הכנס קוד לבוש");
            return false;
        }
        if (spinnerEventType.getSelectedItemPosition() == 0) {
            showError("אנא בחר סוג אירוע תקין");
            return false;
        }
        if (rgFood.getCheckedRadioButtonId() == -1) {
            showError("אנא בחר העדפת מזון");
            return false;
        }
        if (TextUtils.isEmpty(selectedDate)) {
            showError("אנא בחר תאריך");
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
                showError("לא ניתן ליצור אירוע בתאריך שעבר");
                return false;
            }
        } catch (Exception e) {
            Log.e("TAG", "Date parsing error", e);
            showError("פורמט תאריך לא תקין");
            return false;
        }

        return true;
    }

    private String getSelectedFoodType() {
        int selectedId = rgFood.getCheckedRadioButtonId();
        if (selectedId == rbDairy.getId()) return "חלבי";
        if (selectedId == rbVegetarian.getId()) return "צמחוני";
        if (selectedId == rbVegan.getId()) return "טבעוני";
        if (selectedId == rbMeat.getId()) return "בשרי";
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
                Toast.makeText(addEVENT.this, "נכשל ביצירת האירוע", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToAfterLogPage() {
        Intent intent = new Intent(addEVENT.this, AfterLogPage.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // Handle spinner selection if needed
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Handle case when nothing is selected in spinner
    }
}





