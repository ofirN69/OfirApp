package com.ofir.ofirapp.services;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.NotNull;
import com.ofir.ofirapp.models.Event;
import com.ofir.ofirapp.models.User;

import java.util.ArrayList;
import java.util.List;

/// A service to interact with the Firebase Realtime Database.
/// This class is a singleton, use getInstance() to get an instance of this class
/// @see #getInstance()
/// @see FirebaseDatabase
public class DatabaseService {

    private static final String TAG = "DatabaseService";
    private static DatabaseService instance;
    private final DatabaseReference databaseReference;

    private DatabaseService() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
    }

    public static DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    public interface DatabaseCallback<T> {
        void onCompleted(T object);
        void onFailed(Exception e);
    }

    private void writeData(@NotNull final String path, @NotNull final Object data, final @Nullable DatabaseCallback<Void> callback) {
        databaseReference.child(path).setValue(data).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (callback != null) callback.onCompleted(null);
            } else {
                if (callback != null) callback.onFailed(task.getException());
            }
        });
    }

    private DatabaseReference readData(@NotNull final String path) {
        return databaseReference.child(path);
    }

    private <T> void getData(@NotNull final String path, @NotNull final Class<T> clazz, @NotNull final DatabaseCallback<T> callback) {
        readData(path).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting data", task.getException());
                callback.onFailed(task.getException());
                return;
            }
            T data = task.getResult().getValue(clazz);
            callback.onCompleted(data);
        });
    }

    private String generateNewId(@NotNull final String path) {
        return databaseReference.child(path).push().getKey();
    }

    public void createNewUser(@NotNull final User user, @Nullable final DatabaseCallback<Void> callback) {
        writeData("Users/" + user.getId(), user, callback);
    }

    public void createNewEvent(@NotNull final Event event, @Nullable final DatabaseCallback<Void> callback) {
        writeData("events/" + event.getId(), event, callback);
    }

    public void getUser(@NotNull final String uid, @NotNull final DatabaseCallback<User> callback) {
        getData("Users/" + uid, User.class, callback);
    }

    public void getEvent(@NotNull final String eventId, @NotNull final DatabaseCallback<Event> callback) {
        getData("events/" + eventId, Event.class, callback);
    }

    public String generateEventId() {
        return generateNewId("events");
    }

    public String generateCartId() {
        return generateNewId("carts");
    }

    public void getEvents(@NotNull final DatabaseCallback<List<Event>> callback) {
        readData("events").get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting data", task.getException());
                callback.onFailed(task.getException());
                return;
            }
            List<Event> events = new ArrayList<>();
            task.getResult().getChildren().forEach(dataSnapshot -> {
                Event event = dataSnapshot.getValue(Event.class);
                events.add(event);
            });

            callback.onCompleted(events);
        });
    }

    public void getUsers(@NotNull final DatabaseCallback<List<User>> callback) {
        readData("Users").get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting data", task.getException());
                callback.onFailed(task.getException());
                return;
            }
            List<User> users = new ArrayList<>();
            task.getResult().getChildren().forEach(dataSnapshot -> {
                User user = dataSnapshot.getValue(User.class);
                user=new User(user);
                users.add(user);
            });

            callback.onCompleted(users);
        });
    }

    // Add selected users to an event
    public void addSelectedUserToEvent(@NotNull final String eventId, @NotNull final User user, @Nullable final DatabaseCallback<Void> callback) {
        writeData("events/" + eventId + "/users/" + user.getId(), user, callback);
    }



    // Add selected users to an event
    public void addEventToUser(@NotNull final String userId, @NotNull final Event event, @Nullable final DatabaseCallback<Void> callback) {
        writeData("userEvents/" + userId + "/myEvents/" + event.getId(), event, callback);
    }


    // Get selected users of an event
    public void getUserEvents(@NotNull final String userId, @NotNull final DatabaseCallback<List<Event>> callback) {

        readData("userEvents/" + userId + "/myEvents/").get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting selected users", task.getException());
                callback.onFailed(task.getException());
                return;
            }
            List<Event> userEvents = new ArrayList<>();
            task.getResult().getChildren().forEach(dataSnapshot -> {
                Event event = dataSnapshot.getValue(Event.class);
                Log.e(TAG, "event "+ event.toString());
                userEvents.add(event);
            });

            callback.onCompleted(userEvents);
        });
    }

    // Get selected users of an event
    public void getSelectedUsersForEvent(@NotNull final String eventId, @NotNull final DatabaseCallback<List<User>> callback) {
        readData("events/" + eventId + "/users").get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting selected users", task.getException());
                callback.onFailed(task.getException());
                return;
            }
            List<User> selectedUsers = new ArrayList<>();
            task.getResult().getChildren().forEach(dataSnapshot -> {
                User user = dataSnapshot.getValue(User.class);
                selectedUsers.add(user);
            });

            callback.onCompleted(selectedUsers);
        });
    }
}
