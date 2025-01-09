package com.ofir.ofirapp.screens;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.ofir.ofirapp.R;
import com.ofir.ofirapp.models.User;
import com.ofir.ofirapp.utils.SharedPreferencesUtil;

public class AccPage extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acc_page);

        TextView tvEmail = findViewById(R.id.tvEmail);
        TextView tvPhone = findViewById(R.id.tvPhone);
        TextView tvDisplayName = findViewById(R.id.tvDisplayName);

        // קבלת המשתמש המחובר
        User user = SharedPreferencesUtil.getUser(getApplicationContext());

        if (user != null) {
            // הצגת פרטי המשתמש
            tvEmail.setText("Email: " + user.getEmail());
            tvPhone.setText("Phone: " + (user.getPhone() != null ? user.getPhone() : "N/A"));
            tvDisplayName.setText("Name: " + (user.getFname() != null ? user.getFname() : "N/A"));
        } else {
            tvEmail.setText("User not logged in");
        }
    }
}