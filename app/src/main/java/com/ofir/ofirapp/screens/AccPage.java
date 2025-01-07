package com.ofir.ofirapp.screens;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ofir.ofirapp.R;

public class AccPage extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acc_page);

        TextView tvEmail = findViewById(R.id.tvEmail);
        TextView tvPhone = findViewById(R.id.tvPhone);
        TextView tvDisplayName = findViewById(R.id.tvDisplayName);

        // קבלת המשתמש המחובר
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // הצגת פרטי המשתמש
            tvEmail.setText("Email: " + user.getEmail());
            tvPhone.setText("Phone: " + (user.getPhoneNumber() != null ? user.getPhoneNumber() : "N/A"));
            tvDisplayName.setText("Name: " + (user.getDisplayName() != null ? user.getDisplayName() : "N/A"));
        } else {
            tvEmail.setText("User not logged in");
        }
    }
}