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

    // Remove an event from the main events collection and all related references
    public void removeEvent(@NonNull final String eventId, @Nullable final DatabaseCallback<Void> callback) {
        // First get the event to check its owner and users
        getEvent(eventId, new DatabaseCallback<Event>() {
            @Override
            public void onCompleted(Event event) {
                if (event == null) {
                    if (callback != null) callback.onFailed(new Exception("Event not found"));
                    return;
                }

                // Create a map of all paths to delete
                java.util.Map<String, Object> deleteUpdates = new java.util.HashMap<>();
                
                // Remove from main events collection
                deleteUpdates.put("events/" + eventId, null);
                
                // Remove from owner's events
                if (event.getOwnerId() != null) {
                    deleteUpdates.put("userEvents/" + event.getOwnerId() + "/myEvents/" + eventId, null);
                }

                // Get all users of the event and remove from their lists
                getSelectedUsersForEvent(eventId, new DatabaseCallback<List<User>>() {
                    @Override
                    public void onCompleted(List<User> users) {
                        if (users != null) {
                            for (User user : users) {
                                deleteUpdates.put("userEvents/" + user.getId() + "/myEvents/" + eventId, null);
                            }
                        }
                        
                        // Perform all deletions in a single update
                        databaseReference.updateChildren(deleteUpdates)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    if (callback != null) callback.onCompleted(null);
                                } else {
                                    if (callback != null) callback.onFailed(task.getException());
                                }
                            });
                    }

                    @Override
                    public void onFailed(Exception e) {
                        // If we can't get users, still try to delete the event
                        databaseReference.updateChildren(deleteUpdates)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    if (callback != null) callback.onCompleted(null);
                                } else {
                                    if (callback != null) callback.onFailed(task.getException());
                                }
                            });
                    }
                });
            }

            @Override
            public void onFailed(Exception e) {
                if (callback != null) callback.onFailed(e);
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

    // Remove an event from all users' events lists
    public void removeEventFromAllUsers(@NonNull final String eventId, @Nullable final DatabaseCallback<Void> callback) {
        // First get all users
        getUsers(new DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {
                if (users == null || users.isEmpty()) {
                    if (callback != null) callback.onCompleted(null);
                    return;
                }

                // Keep track of completed operations
                final int[] completedCount = {0};
                final Exception[] lastError = {null};

                // Remove event from each user's list
                for (User user : users) {
                    removeEventFromUser(user.getId(), eventId, new DatabaseCallback<Void>() {
                        @Override
                        public void onCompleted(Void object) {
                            completedCount[0]++;
                            checkCompletion();
                        }

                        @Override
                        public void onFailed(Exception e) {
                            completedCount[0]++;
                            lastError[0] = e;
                            checkCompletion();
                        }

                        private void checkCompletion() {
                            if (completedCount[0] == users.size()) {
                                if (callback != null) {
                                    if (lastError[0] != null) {
                                        callback.onFailed(lastError[0]);
                                    } else {
                                        callback.onCompleted(null);
                                    }
                                }
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailed(Exception e) {
                if (callback != null) callback.onFailed(e);
            }
        });
    }

    public void updateUser(String userId, User user, DatabaseCallback<Void> callback) {
        databaseReference.child("Users").child(userId).setValue(user)
            .addOnSuccessListener(aVoid -> callback.onCompleted(null))
            .addOnFailureListener(e -> callback.onFailed(e));
    }

    public void updateUserPassword(String userId, String newPassword, DatabaseCallback<Void> callback) {
        // Update only the password field in the user's data
        databaseReference.child("Users").child(userId).child("password").setValue(newPassword)
            .addOnSuccessListener(aVoid -> callback.onCompleted(null))
            .addOnFailureListener(e -> callback.onFailed(e));
    }
}
