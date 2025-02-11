package com.ofir.ofirapp.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ofir.ofirapp.R;
import com.ofir.ofirapp.models.Event;

public class MyEvents extends AppCompatActivity {

    public class myEvents extends AppCompatActivity {
        private TextView tvEventList;
        private FirebaseDatabase database;
        private DatabaseReference myRef;
        Button btnBack;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_my_events);

            // בדיקת התחברות
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Toast.makeText(this, "Please log in to access this feature.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, Login.class);
                startActivity(intent);
                finish();
                return;
            }

            // אתחול Firebase
            database = FirebaseDatabase.getInstance();
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            myRef = database.getReference("tasks").child(userId);

            // אתחול TextView
            tvEventList = findViewById(R.id.tvEventsList);

            // קריאת משימות מ-Firebase
            loadTasksFromFirebase();
        }

        private void loadTasksFromFirebase() {
            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    StringBuilder tasks = new StringBuilder();

                    // מעבר על כל המשימות
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Event event = snapshot.getValue(Event.class);
                        if (event != null) {
                            tasks.append("Event Type: ").append(event.getType()).append("\n")
                                    .append("Venue: ").append(event.getVenue()).append("\n")
                                    .append("city: ").append(event.getCity()).append("\n")
                                    .append("menu type: ").append(event.getMenutype()).append("\n")
                                    .append("Date: ").append(event.getDate()).append("\n")
                                    .append("Dress code: ").append(event.getDresscode()).append("\n\n");
                        }
                    }

                    // עדכון הטקסט בתצוגה
                    tvEventList.setText(tasks.length() > 0 ? tasks.toString() : "No tasks available.");
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(MyEvents.this, "Failed to load tasks.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        public void MainActivity(View view) {
            if (view == btnBack) {
                Intent btnBack = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(btnBack);
            }
        }
    }
}