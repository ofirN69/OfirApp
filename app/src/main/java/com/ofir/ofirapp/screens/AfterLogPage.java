package com.ofir.ofirapp.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ofir.ofirapp.R;
import com.ofir.ofirapp.models.User;
import com.ofir.ofirapp.utils.SharedPreferencesUtil;

public class AfterLogPage extends AppCompatActivity {

    TextView tvHello;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_after_log_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        User user=SharedPreferencesUtil.getUser(this);

        tvHello = findViewById(R.id.tv_hello);
        tvHello.setText(user.getFname() +"היי ");
    }

    public void onClick(View view) {
        Intent goRe = new Intent(getApplicationContext(), AccPage.class);
        startActivity(goRe);
    }



    public void goMyEvents(View view) {
        Intent goToEvents = new Intent(getApplicationContext(), MyEvents.class);
        startActivity(goToEvents);
    }
}