package com.ofir.ofirapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
        initViews();
    }

    private void initViews() {
        btnRegister = findViewById(R.id.btnGoRegister);
        btnRegister.setOnClickListener(this);
        btnLogin = findViewById(R.id.btnGoLogin);
        btnLogin.setOnClickListener(this);
        btnAbout = findViewById(R.id.btnGoAbout);
        btnAbout.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v==btnRegister){





            Intent goReg=new Intent(getApplicationContext(), Register.class);
            startActivity(goReg);
        }
        if (v==btnLogin){
            Intent golog=new Intent(getApplicationContext(), Login.class);
            startActivity(golog);
        }
    }
}
