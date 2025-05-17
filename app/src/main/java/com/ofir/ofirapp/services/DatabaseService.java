package com.ofir.ofirapp.services;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

    private void writeData(@NonNull final String path, @NonNull final Object data, final @Nullable DatabaseCallback<Void> callback) {
        databaseReference.child(path).setValue(data).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (callback != null) callback.onCompleted(null);
            } else {
                if (callback != null) callback.onFailed(task.getException());
            }
        });
    }

    private DatabaseReference readData(@NonNull final String path) {
        return databaseReference.child(path);
    }

    private <T> void getData(@NonNull final String path, @NonNull final Class<T> clazz, @NonNull final DatabaseCallback<T> callback) {
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

    private String generateNewId(@NonNull final String path) {
        return databaseReference.child(path).push().getKey();
    }

    public void createNewUser(@NonNull final User user, @Nullable final DatabaseCallback<Void> callback) {
        writeData("Users/" + user.getId(), user, callback);
    }

    public void createNewEvent(@NonNull final Event event, @Nullable final DatabaseCallback<Void> callback) {
        writeData("events/" + event.getId(), event, callback);
    }

    public void getUser(@NonNull final String uid, @NonNull final DatabaseCallback<User> callback) {
        getData("Users/" + uid, User.class, callback);
    }

    public void getEvent(@NonNull final String eventId, @NonNull final DatabaseCallback<Event> callback) {
        getData("events/" + eventId, Event.class, callback);
    }

    public String generateEventId() {
        return generateNewId("events");
    }

    public String generateCartId() {
        return generateNewId("carts");
    }

    public void getEvents(@NonNull final DatabaseCallback<List<Event>> callback) {
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

    public void getUsers(@NonNull final DatabaseCallback<List<User>> callback) {
        readData("Users").get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting data", task.getException());
                callback.onFailed(task.getException());
                return;
            }
            List<User> users = new ArrayList<>();
            task.getResult().getChildren().forEach(dataSnapshot -> {
                User user = dataSnapshot.getValue(User.class);
                user = new User(user);
                users.add(user);
            });

            callback.onCompleted(users);
        });
    }

    // Add selected users to an event
    public void addSelectedUserToEvent(@NonNull final String eventId, @NonNull final User user, @Nullable final DatabaseCallback<Void> callback) {
        writeData("events/" + eventId + "/users/" + user.getId(), user, callback);
    }

    // Add selected users to an event
    public void addEventToUser(@NonNull final String userId, @NonNull final Event event, @Nullable final DatabaseCallback<Void> callback) {
        writeData("userEvents/" + userId + "/myEvents/" + event.getId(), event, callback);
    }

    // Get events for a user (both owned and invited)
    public void getUserEvents(@NonNull final String userId, @NonNull final DatabaseCallback<List<Event>> callback) {
        // First, get all events to check for ownership
        readData("events").get().addOnCompleteListener(allEventsTask -> {
            if (!allEventsTask.isSuccessful()) {
                Log.e(TAG, "Error getting all events", allEventsTask.getException());
                callback.onFailed(allEventsTask.getException());
                return;
            }

            List<Event> allUserEvents = new ArrayList<>();

            // Get events where user is the owner
            allEventsTask.getResult().getChildren().forEach(dataSnapshot -> {
                Event event = dataSnapshot.getValue(Event.class);
                if (event != null) {
                    // Add all events where the user is the owner
                    if (userId.equals(event.getOwnerId())) {
                        Log.d(TAG, "Found owned event: " + event.getId() + " for user: " + userId);
                        allUserEvents.add(event);
                    }
                }
            });

            // Then get events where user is invited
            readData("userEvents/" + userId + "/myEvents").get().addOnCompleteListener(invitedEventsTask -> {
                if (!invitedEventsTask.isSuccessful()) {
                    Log.e(TAG, "Error getting invited events", invitedEventsTask.getException());
                    callback.onFailed(invitedEventsTask.getException());
                    return;
                }

                invitedEventsTask.getResult().getChildren().forEach(dataSnapshot -> {
                    Event event = dataSnapshot.getValue(Event.class);
                    if (event != null) {
                        // Add events where user is invited (not the owner)
                        if (!userId.equals(event.getOwnerId())) {
                            Log.d(TAG, "Found invited event: " + event.getId() + " for user: " + userId);
                            allUserEvents.add(event);
                        }
                    }
                });

                Log.d(TAG, "Total events found for user " + userId + ": " + allUserEvents.size());
                callback.onCompleted(allUserEvents);
            });
        });
    }

    // Get selected users of an event
    public void getSelectedUsersForEvent(@NonNull final String eventId, @NonNull final DatabaseCallback<List<User>> callback) {
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

    // Remove an event from the main events collection
    public void removeEvent(@NonNull final String eventId, @Nullable final DatabaseCallback<Void> callback) {
        databaseReference.child("events").child(eventId).removeValue()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (callback != null) callback.onCompleted(null);
                } else {
                    if (callback != null) callback.onFailed(task.getException());
                }
            });
    }

    // Remove an event from a user's events list
    public void removeEventFromUser(@NonNull final String userId, @NonNull final String eventId, @Nullable final DatabaseCallback<Void> callback) {
        databaseReference.child("userEvents").child(userId).child("myEvents").child(eventId).removeValue()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (callback != null) callback.onCompleted(null);
                } else {
                    if (callback != null) callback.onFailed(task.getException());
                }
            });
    }

    public void updateUser(User user, DatabaseCallback<Void> callback) {
        writeData("Users/" + user.getId(), user, callback);
    }
}
