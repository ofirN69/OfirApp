package com.ofir.ofirapp.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.ofir.ofirapp.R;
import com.ofir.ofirapp.models.User;
import com.ofir.ofirapp.models.Event;
import com.ofir.ofirapp.services.AuthenticationService;
import com.ofir.ofirapp.services.DatabaseService;
import com.ofir.ofirapp.utils.SharedPreferencesUtil;

import java.util.List;

public class AccPage extends AppCompatActivity implements View.OnClickListener {
    private TextView tvEmail, tvPhone, tvDisplayName, tvEventCount;
    private MaterialButton btnEditProfile, btnChangePassword, btnLogout;
    private ImageView ivProfilePic;
    private User currentUser;
    private DatabaseService databaseService;
    private AuthenticationService authenticationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acc_page);

        // Initialize services
        databaseService = DatabaseService.getInstance();
        authenticationService = AuthenticationService.getInstance();

        // Initialize views
        initializeViews();
        
        // Set click listeners
        btnEditProfile.setOnClickListener(this);
        btnChangePassword.setOnClickListener(this);
        btnLogout.setOnClickListener(this);

        // Load user data
        loadUserData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload user data when returning from EditProfileActivity
        loadUserData();
    }

    private void initializeViews() {
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvDisplayName = findViewById(R.id.tvDisplayName);
        tvEventCount = findViewById(R.id.tvEventCount);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnLogout = findViewById(R.id.btnLogout);
        ivProfilePic = findViewById(R.id.ivProfilePic);
    }

    private void loadUserData() {
        currentUser = SharedPreferencesUtil.getUser(this);
        if (currentUser != null) {
            // Display user information
            tvDisplayName.setText(currentUser.getFname());
            tvEmail.setText(currentUser.getEmail());
            tvPhone.setText(currentUser.getPhone() != null ? currentUser.getPhone() : "No phone number");

            // Load user's events count
            loadUserEvents();
        } else {
            // Handle case where user data is not available
            Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            navigateToLogin();
        }
    }

    private void loadUserEvents() {
        databaseService.getUserEvents(currentUser.getId(), new DatabaseService.DatabaseCallback<List<Event>>() {
            @Override
            public void onCompleted(List<Event> events) {
                runOnUiThread(() -> {
                    tvEventCount.setText("Events: " + events.size());
                });
            }

            @Override
            public void onFailed(Exception e) {
                runOnUiThread(() -> {
                    tvEventCount.setText("Events: 0");
                    Toast.makeText(AccPage.this, "Failed to load events", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnEditProfile) {
            startActivity(new Intent(this, EditProfileActivity.class));
        } else if (v.getId() == R.id.btnChangePassword) {
            // TODO: Implement change password functionality
            Toast.makeText(this, "Change Password - Coming soon!", Toast.LENGTH_SHORT).show();
        } else if (v.getId() == R.id.btnLogout) {
            logout();
        }
    }

    private void logout() {
        authenticationService.signOut();
        SharedPreferencesUtil.signOutUser(this);
        navigateToLogin();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}