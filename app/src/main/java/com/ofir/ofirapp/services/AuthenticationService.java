package com.ofir.ofirapp.services;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;

import org.jetbrains.annotations.NotNull;

public class AuthenticationService {
    private static final String TAG = "AuthenticationService";

    public interface AuthCallback<T> {
        default void onCompleted(T object) {}
        default void onFailed(Exception e) {}
        default void onSuccess() {}
        default void onError(String error) {}
    }

    private static AuthenticationService instance;
    private final FirebaseAuth mAuth;

    private AuthenticationService() {
        mAuth = FirebaseAuth.getInstance();
    }

    public static AuthenticationService getInstance() {
        if (instance == null) {
            instance = new AuthenticationService();
        }
        return instance;
    }

    public void signIn(@NotNull final String email, @NotNull final String password, @NotNull final AuthCallback<String> callback) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onCompleted(getCurrentUserId());
            } else {
                Log.e(TAG, "Error signing in", task.getException());
                callback.onFailed(task.getException());
            }
        });
    }

    public void signUp(@NotNull final String email, @NotNull final String password, @NotNull final AuthCallback<String> callback) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onCompleted(getCurrentUserId());
            } else {
                Log.e(TAG, "Error signing up", task.getException());
                callback.onFailed(task.getException());
            }
        });
    }

    public void signOut() {
        mAuth.signOut();
    }

    public String getCurrentUserId() {
        if (mAuth.getCurrentUser() == null) {
            return null;
        }
        return mAuth.getCurrentUser().getUid();
    }

    public boolean isUserSignedIn() {
        return mAuth.getCurrentUser() != null;
    }

    public void changePassword(String currentPassword, String newPassword, AuthCallback<?> callback) {
        if (mAuth.getCurrentUser() == null) {
            callback.onError("No user is currently signed in");
            return;
        }

        String email = mAuth.getCurrentUser().getEmail();
        if (email == null) {
            callback.onError("User email not found");
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(email, currentPassword);
        
        mAuth.getCurrentUser().reauthenticate(credential)
            .addOnCompleteListener(reauthTask -> {
                if (reauthTask.isSuccessful()) {
                    // Reauthentication successful, now change the password
                    mAuth.getCurrentUser().updatePassword(newPassword)
                        .addOnCompleteListener(updateTask -> {
                            if (updateTask.isSuccessful()) {
                                callback.onSuccess();
                            } else {
                                callback.onError(updateTask.getException() != null ? 
                                    updateTask.getException().getMessage() : 
                                    "Failed to update password");
                            }
                        });
                } else {
                    callback.onError("Current password is incorrect");
                }
            });
    }
}
