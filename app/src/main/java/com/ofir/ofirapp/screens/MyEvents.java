package com.ofir.ofirapp.screens;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ofir.ofirapp.R;
import com.ofir.ofirapp.adapters.EventAdapter;
import com.ofir.ofirapp.models.Event;
import com.ofir.ofirapp.services.AuthenticationService;
import com.ofir.ofirapp.services.DatabaseService;

import java.util.ArrayList;
import java.util.List;

public class MyEvents extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private ArrayList<Event> eventList=new ArrayList<>();

    private String userId;
    private AuthenticationService authenticationService;
    private DatabaseService databaseService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);
        /// get the instance of the authentication service
        authenticationService = AuthenticationService.getInstance();
        /// get the instance of the database service
        databaseService = DatabaseService.getInstance();
        userId=authenticationService.getCurrentUserId();



        //recyclerView = findViewById(R.id.recyclerViewEvents);
      //  recyclerView.setLayoutManager(new LinearLayoutManager(this));

        eventList = new ArrayList<>();
        //eventAdapter = new EventAdapter( eventList,MyEvents.this);
       // recyclerView.setAdapter(eventAdapter);

        databaseService.getUserEvents(userId, new DatabaseService.DatabaseCallback<List<Event>>() {
            @Override
            public void onCompleted(List<Event> object) {
                eventList.clear();
                eventList.addAll(object);




              // eventAdapter.notifyDataSetChanged();

            }

            @Override
            public void onFailed(Exception e) {

            }
        });




    }



}
