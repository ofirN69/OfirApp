package com.ofir.ofirapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ofir.ofirapp.models.User;

public class login extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "loginToFireBase";
    TextView txtLogin;
    EditText etEmailLogin, etPasswordLogin;
    Button btnLogin;

    String email2, pass2;
    private FirebaseAuth mAuth;
    String admin = "idosshati@gmail.com";

    public static final String MyPREFERENCES = "MyPrefs";


    SharedPreferences sharedpreferences;

    private FirebaseDatabase database;
    private DatabaseReference myRef, farmerRef;
    public static User theUser;

    public static Boolean isAdmin = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        init_views();
        database = FirebaseDatabase.getInstance();
        email2 = sharedpreferences.getString("email", "");
        pass2 = sharedpreferences.getString("password", "");
        etEmailLogin.setText(email2);
        etPasswordLogin.setText(pass2);

    }

    private void init_views() {
        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);
        etEmailLogin = findViewById(R.id.etEmailLogin);
        etPasswordLogin = findViewById(R.id.etPasswordLogin);
    }


    @Override
    public void onClick(View v) {
        email2 = etEmailLogin.getText().toString();
        pass2 = etPasswordLogin.getText().toString();

        mAuth.signInWithEmailAndPassword(email2, pass2)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {


                            SharedPreferences.Editor editor = sharedpreferences.edit();

                            editor.putString("email", email2);
                            editor.putString("password", pass2);

                            editor.commit();
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            final FirebaseUser user = mAuth.getCurrentUser();

                            myRef = database.getReference("Users").child(mAuth.getUid());


                            //    if (email2.equals(admin)) {
                            //        Intent goLog = new Intent(getApplicationContext(), AdminPage.class);
                            //        startActivity(goLog);


                            //    }

                            Intent go = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(go);


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(login.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }

                });
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}