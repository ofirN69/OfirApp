package com.ofir.ofirapp.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ofir.ofirapp.R;
import com.ofir.ofirapp.models.User;
import com.ofir.ofirapp.utils.SharedPreferencesUtil;

public class AfterLogPage extends AppCompatActivity implements View.OnClickListener {

    TextView tvHello;
    Button MyEvents , Add, Info;

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
        initviews();
        User user=SharedPreferencesUtil.getUser(this);

        tvHello = findViewById(R.id.tv_hello);
        tvHello.setText("Hello " + user.getFname() + " welcome to the app");
    }

    private void initviews() {
        MyEvents=findViewById(R.id.btnGoMyEvents);
        MyEvents.setOnClickListener(this);
        Add=findViewById(R.id.btnAddEvent);
        Add.setOnClickListener(this);
        Info=findViewById(R.id.btnInfo);
        Info.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == MyEvents) {
            Intent goToEvents = new Intent(getApplicationContext(), MyEvents.class);
            startActivity(goToEvents);
        }
        if (view ==Add){
            Intent goAdd = new Intent(getApplicationContext(), addEVENT.class);
            startActivity(goAdd);

        }
        if (view == Info){
            Intent goInfo = new Intent(getApplicationContext(), AccPage.class);
            startActivity(goInfo);
        }
    }











}