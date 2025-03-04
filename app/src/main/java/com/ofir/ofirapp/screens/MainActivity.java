package com.ofir.ofirapp.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ofir.ofirapp.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button btnRegister,btnLogin,btnAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // go to landing page
        initViews();
    }

    private void initViews() {
        btnRegister = findViewById(R.id.btnGoRegister);
        btnRegister.setOnClickListener(this);
        btnLogin = findViewById(R.id.btnGoLogin);
        btnLogin.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v == btnRegister) {
            Intent goReg = new Intent(this, Register.class);
            startActivity(goReg);
        }

        if (v == btnLogin){
            Intent goLog = new Intent(this, Login.class);
            startActivity(goLog);
        }

    }
}
