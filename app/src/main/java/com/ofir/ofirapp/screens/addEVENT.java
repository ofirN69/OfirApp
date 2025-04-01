package com.ofir.ofirapp.screens;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;


import com.ofir.ofirapp.R;
import com.ofir.ofirapp.adapters.UserNamAdapter;
import com.ofir.ofirapp.models.Event;
import com.ofir.ofirapp.models.User;
import com.ofir.ofirapp.services.DatabaseService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class addEVENT extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    CalendarView cvEventDate;
    Spinner spinnertype;  // Spinner for sizes
    RadioGroup RGFood;
    RadioButton rbDairy, rbVegetarian, rbVegan, rbMeat;
    String food, stDate;

    TextView dateTextView;

    EditText etVenueName,  etDress,  etAdress, etCity;
    String stVenueName, stDress, stTime, stAdress, stCity;
    Button btnAddEvent;

    private DatabaseService databaseService;
    private String selectedType;


    private String uid;

    User user;

    ListView lvMembers,lvSelectedMembers;


    ArrayList<User> users=new ArrayList<>();
    UserNamAdapter<User> adapter;
    private UserNamAdapter<User> selectedAdapter;


    ArrayList<User> usersSelected=new ArrayList<>();

    String members="";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        databaseService = DatabaseService.getInstance();

        initViews();
        btnAddEvent.setOnClickListener(this);


        users = new ArrayList<>();
        adapter = new UserNamAdapter<>(addEVENT.this, 0, 0, users);


        lvMembers.setAdapter(adapter);
        lvMembers.setOnItemClickListener(this);

        selectedAdapter = new UserNamAdapter<>(addEVENT.this, 0, 0, usersSelected);
        lvSelectedMembers.setAdapter(selectedAdapter);

        // Click listener for removing users from lvSelectedMembers
        lvSelectedMembers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User selectedUser = usersSelected.get(position);  // Get the clicked user

                usersSelected.remove(selectedUser);  // Remove from selected list

                selectedAdapter.notifyDataSetChanged();  // Refresh the selected list
            }
        });



        databaseService.getUsers(new DatabaseService.DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> object) {
                users.clear();
                users.addAll(object);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailed(Exception e) {
                Log.e("TAG", "onFailed: ", e);
            }
        });

        databaseService.getUser(uid, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User u) {
                user = u;
            }

            @Override
            public void onFailed(Exception e) {
            }
        });

    }

    private void initViews() {
        btnAddEvent = findViewById(R.id.btnAddItem);
        cvEventDate = findViewById(R.id.cvEventDate);
        dateTextView = findViewById(R.id.dateTextView);
        spinnertype = findViewById(R.id.spItemTYpe);
        etVenueName = findViewById(R.id.etVenue);
        etDress = findViewById(R.id.etDressCode);
        etAdress = findViewById(R.id.etAdress);
        etCity = findViewById(R.id.etCity);
        RGFood = findViewById(R.id.RGFood);
        rbDairy = findViewById(R.id.rbDairy);
        rbVegetarian = findViewById(R.id.rbVegetarian);
        rbVegan = findViewById(R.id.rbVegan);
        rbMeat = findViewById(R.id.rbMeat);

        lvMembers=findViewById(R.id.lvMembers);
        lvSelectedMembers=findViewById(R.id.lvSelected);
        // Set default date to today
        long currentDate = cvEventDate.getDate();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        stDate = sdf.format(new Date(currentDate));

        // Display default date in TextView
        dateTextView.setText("Selected Date: " + stDate);

        // Update date when user selects a new date
        cvEventDate.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            stDate = year + "-" + (month + 1) + "-" + dayOfMonth; // Convert date to string
            dateTextView.setText("Selected Date: " + stDate); // Display in TextView

            dateTextView.setText(stDate);
        });

        // Set listener to show or hide size spinner based on item type

    }

    @Override
    public void onClick(View view) {

        if (view == btnAddEvent) {
            selectedType = spinnertype.getSelectedItem().toString();
            stDress = etDress.getText().toString() + "";
            stVenueName = etVenueName.getText().toString() + "";
            stAdress = etAdress.getText().toString() + "";
            stCity = etCity.getText().toString() + "";

            int selectedId = RGFood.getCheckedRadioButtonId();
            if (selectedId == rbDairy.getId()) {
                food = "Dairy";
                  }
                else if (selectedId == rbVegetarian.getId()) {
                                food = "Vegetarian";
                    }
                            else if (selectedId == rbVegan.getId()) {
                                      food = "vegan";
                            }


                                    else if (selectedId == rbMeat.getId()) {
                                         food = "Meat";
                                     }




            // בדיקת תקינות קלט
            if (TextUtils.isEmpty(stVenueName)) {
                Toast.makeText(this, "Please enter the venue name", Toast.LENGTH_SHORT).show();
                return;
            }


            if (TextUtils.isEmpty(stAdress)) {
                Toast.makeText(this, "Please enter the address", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(stCity)) {
                Toast.makeText(this, "Please enter the city", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(stDress)) {
                Toast.makeText(this, "Please enter the dress code", Toast.LENGTH_SHORT).show();
                return;
            }

            if (spinnertype.getSelectedItemPosition() == 0) { // Assuming first position is a placeholder
                Toast.makeText(this, "Please select a valid event type", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedId == -1) { // אם לא נבחר שום כפתור
                Toast.makeText(this, "Please select a food preference", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(stDate)) {
                Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
                return;
            }

            // Set a listener to get the date when it's selected
            String itemid = databaseService.generateEventId();

                //  public Event(String type, String date, String hour, String venue, String address, String city, String dresscode, String status, ArrayList < String > menutype, ArrayList < User > users)

                    Event newEvent = new Event(itemid, selectedType, stDate, stVenueName, stAdress, stCity, stDress,"new", food,usersSelected);
                    databaseService.createNewEvent(newEvent, new DatabaseService.DatabaseCallback<Void>() {
                        @Override
                        public void onCompleted(Void object) {
                            // handle completion

                            Log.d("TAG", "Event added successfully");
                            Toast.makeText(addEVENT.this, "Item added successfully", Toast.LENGTH_SHORT).show();


                            for(int i=0;i<usersSelected.size();i++){

                                databaseService.addEventToUser(usersSelected.get(i).getId(), newEvent, new DatabaseService.DatabaseCallback<Void>() {
                                    @Override
                                    public void onCompleted(Void object) {

                                    }

                                    @Override
                                    public void onFailed(Exception e) {

                                    }
                                });
                            }

                            Intent goReg = new Intent(addEVENT.this, AfterLogPage.class);
                            startActivity(goReg);

                        }

                        @Override
                        public void onFailed(Exception e) {
                            // handle failure
                            Log.e("TAG", "Failed to add Event", e);
                            Toast.makeText(addEVENT.this, "Failed to add Item", Toast.LENGTH_SHORT).show();

                        }
                    });
                }
            }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        User selectedUser = (User) parent.getItemAtPosition(position);  // Get the clicked user

        usersSelected.add(selectedUser);  // Remove from selected list

        selectedAdapter.notifyDataSetChanged();  // Refresh the selected list


    }
}





