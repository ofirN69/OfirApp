package com.ofir.ofirapp.screens;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.ofir.ofirapp.R;
import com.ofir.ofirapp.services.AuthenticationService;
import com.ofir.ofirapp.services.DatabaseService;

public class ChangePasswordActivity extends AppCompatActivity {
    private TextInputEditText etCurrentPassword;
    private TextInputEditText etNewPassword;
    private TextInputEditText etConfirmPassword;
    private MaterialButton btnChangePassword;
    private MaterialButton btnCancel;
    
    private AuthenticationService authenticationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        
        // Initialize services
        authenticationService = AuthenticationService.getInstance();
        
        // Initialize views
        initializeViews();
        
        // Set click listeners
        setClickListeners();
    }
    
    private void initializeViews() {
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnChangePassword = findViewById(R.id.btnConfirmPasswordChange);
        btnCancel = findViewById(R.id.btnCancelPasswordChange);
    }
    
    private void setClickListeners() {
        btnChangePassword.setOnClickListener(v -> validateAndChangePassword());
        btnCancel.setOnClickListener(v -> finish());
    }
    
    private void validateAndChangePassword() {
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        
        // Validate inputs
        if (TextUtils.isEmpty(currentPassword)) {
            etCurrentPassword.setError("Please enter current password");
            return;
        }
        
        if (TextUtils.isEmpty(newPassword)) {
            etNewPassword.setError("Please enter new password");
            return;
        }
        
        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Please confirm new password");
            return;
        }
        
        if (newPassword.length() < 6) {
            etNewPassword.setError("Password must be at least 6 characters long");
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }
        
        // Change password
        authenticationService.changePassword(currentPassword, newPassword, new AuthenticationService.AuthCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(ChangePasswordActivity.this, 
                        "Password changed successfully", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(ChangePasswordActivity.this, 
                        "Error: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
} 