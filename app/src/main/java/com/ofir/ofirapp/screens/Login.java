package com.ofir.ofirapp.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.ofir.ofirapp.R;
import com.ofir.ofirapp.models.User;
import com.ofir.ofirapp.services.AuthenticationService;
import com.ofir.ofirapp.services.DatabaseService;
import com.ofir.ofirapp.utils.SharedPreferencesUtil;
import com.ofir.ofirapp.utils.Validator;

import android.app.ProgressDialog;

public class Login extends AppCompatActivity implements View.OnClickListener,AuthenticationService.AuthCallback<String> {



    private static final String TAG = "Login";

    private EditText etEmail, etPassword;
    private Button btnGoLog;

    private AuthenticationService authenticationService;
    private DatabaseService databaseService;
    private User user=null;

    String admin = "ofiry.nevo555@gmail.com";
    String passadmin = "123456";


    public static boolean isAdmin = false;
    private String email;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        /// set the layout for the activity
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /// get the instance of the authentication service
        authenticationService = AuthenticationService.getInstance();
        /// get the instance of the database service
        databaseService = DatabaseService.getInstance();


        user = SharedPreferencesUtil.getUser(Login.this);
        /// get the views
        etEmail = findViewById(R.id.etEmailLogin);


        etPassword = findViewById(R.id.etPasswordLogin);


        btnGoLog = findViewById(R.id.btnLog);
        btnGoLog.setOnClickListener(this);
        if (user != null)
        {
            etEmail.setText(user.getEmail());
            etPassword.setText(user.getPassword());
          }
        /// set the click listener

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == btnGoLog.getId()) {
            Log.d(TAG, "onClick: Login button clicked");



            /// get the email and password entered by the user
             email = etEmail.getText().toString();
             password = etPassword.getText().toString();

            /// log the email and password
            Log.d(TAG, "onClick: Email: " + email);
            Log.d(TAG, "onClick: Password: " + password);

            Log.d(TAG, "onClick: Validating input...");
            /// Validate input
            if (!checkInput(email, password)) {
                /// stop if input is invalid
                return;
            }

            Log.d(TAG, "onClick: Logging in user...");

            /// Login user
            loginUser(email, password);
        }

    }
    @Override
    public void onCompleted(String id) {
        Log.d(TAG, "signInWithEmail:success");
        // Get user data from database
        databaseService.getUser(id, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user2) {
                Log.d(TAG, "onCompleted: User data retrieved successfully "+user2.toString());
                runOnUiThread(() -> {
                    if (email.equals(admin) && password.equals(passadmin)) {
                        Intent golog = new Intent(Login.this, AdminPage.class);
                        isAdmin = true;
                        startActivity(golog);
                        finish();  // Prevent going back to login
                    } else {
                        Intent mainIntent = new Intent(Login.this, AfterLogPage.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(mainIntent);
                        finish();  // Prevent going back to login
                    }
                });
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "onFailed: Failed to retrieve user data", e);
                runOnUiThread(() -> {
                    Toast.makeText(Login.this, "Failed to retrieve user data", Toast.LENGTH_LONG).show();
                    btnGoLog.setEnabled(true);
                });
            }
        });
    }

    @Override
    public void onFailed(Exception e) {
        Log.e(TAG, "Authentication failed", e);
        runOnUiThread(() -> {
            Toast.makeText(Login.this, "Authentication failed: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            btnGoLog.setEnabled(true);
        });
    }


    /// Method to check if the input is valid
    /// It checks if the email and password are valid
    /// @see Validator#isEmailValid(String)
    /// @see Validator#isPasswordValid(String)
    private boolean checkInput(String email, String password) {
        if (!Validator.isEmailValid(email)) {
            Log.e(TAG, "checkInput: Invalid email address");
            /// show error message to user
            etEmail.setError("Invalid email address");
            /// set focus to email field
            etEmail.requestFocus();
            return false;
        }

        if (!Validator.isPasswordValid(password)) {
            Log.e(TAG, "checkInput: Password must be at least 6 characters long");
            /// show error message to user
            etPassword.setError("Password must be at least 6 characters long");
            /// set focus to password field
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void loginUser(String email, String password) {
        btnGoLog.setEnabled(false);  // Disable button during login
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        
        authenticationService.signIn(email, password, new AuthenticationService.AuthCallback<String>() {
            @Override
            public void onCompleted(String uid) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                Log.d(TAG, "onCompleted: User logged in successfully");
                // Get user data from database
                databaseService.getUser(uid, new DatabaseService.DatabaseCallback<User>() {
                    @Override
                    public void onCompleted(User user2) {
                        Log.d(TAG, "onCompleted: User data retrieved successfully "+user2.toString());
                        // Save user data to shared preferences
                        SharedPreferencesUtil.saveUser(Login.this, user2);
                        
                        runOnUiThread(() -> {
                            if (email.equals(admin) && password.equals(passadmin)) {
                                Intent golog = new Intent(Login.this, AdminPage.class);
                                isAdmin = true;
                                startActivity(golog);
                                finish();  // Prevent going back to login
                            } else {
                                Intent mainIntent = new Intent(Login.this, AfterLogPage.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                finish();  // Prevent going back to login
                            }
                        });
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Log.e(TAG, "onFailed: Failed to retrieve user data", e);
                        runOnUiThread(() -> {
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            Toast.makeText(Login.this, "Failed to retrieve user data", Toast.LENGTH_LONG).show();
                            btnGoLog.setEnabled(true);
                            // Sign out the user if failed to retrieve user data
                            authenticationService.signOut();
                        });
                    }
                });
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "Authentication failed", e);
                runOnUiThread(() -> {
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    Toast.makeText(Login.this, "Authentication failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    btnGoLog.setEnabled(true);
                });
            }
        });
    }
}