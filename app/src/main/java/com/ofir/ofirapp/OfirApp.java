package com.ofir.ofirapp;

import android.app.Application;
import com.google.firebase.FirebaseApp;

public class OfirApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
} 