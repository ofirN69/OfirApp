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
    private DatabaseService databaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        
        // Initialize services
        authenticationService = AuthenticationService.getInstance();
        databaseService = DatabaseService.getInstance();
        
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
            etCurrentPassword.setError("אנא הכנס סיסמה נוכחית");
            return;
        }
        
        if (TextUtils.isEmpty(newPassword)) {
            etNewPassword.setError("אנא הכנס סיסמה חדשה");
            return;
        }
        
        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("אנא אשר סיסמה חדשה");
            return;
        }
        
        if (newPassword.length() < 6) {
            etNewPassword.setError("הסיסמה חייבת להכיל לפחות 6 תווים");
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("הסיסמאות אינן תואמות");
            return;
        }
        
        // Change password in both Authentication and Database
        String userId = authenticationService.getCurrentUserId();
        if (userId == null) {
            Toast.makeText(this, "שגיאה: משתמש לא מזוהה", Toast.LENGTH_SHORT).show();
            return;
        }

        // First update in Authentication
        authenticationService.changePassword(currentPassword, newPassword, new AuthenticationService.AuthCallback() {
            @Override
            public void onSuccess() {
                // After successful auth update, update in database
                databaseService.updateUserPassword(userId, newPassword, new DatabaseService.DatabaseCallback<Void>() {
                    @Override
                    public void onCompleted(Void result) {
                        runOnUiThread(() -> {
                            Toast.makeText(ChangePasswordActivity.this, 
                                "הסיסמה שונתה בהצלחה", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }
                    
                    @Override
                    public void onFailed(Exception e) {
                        runOnUiThread(() -> {
                            Toast.makeText(ChangePasswordActivity.this, 
                                "שגיאה בעדכון הסיסמה במסד הנתונים: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(ChangePasswordActivity.this, 
                        "שגיאה: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
} 