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

public class DatabaseService {
    /// tag for logging
    /// @see Log
    private static final String TAG = "DatabaseService";

    /// callback interface for database operations
    /// @param <T> the type of the object to return
    /// @see DatabaseCallback#onCompleted(Object)
    /// @see DatabaseCallback#onFailed(Exception)
    public interface DatabaseCallback<T> {
        /// called when the operation is completed successfully
        void onCompleted(T object);

        /// called when the operation fails with an exception
        void onFailed(Exception e);
    }

    /// the instance of this class
    /// @see #getInstance()
    private static DatabaseService instance;

    /// the reference to the database
    /// @see DatabaseReference
    /// @see FirebaseDatabase#getReference()
    private final DatabaseReference databaseReference;

    /// use getInstance() to get an instance of this class
    /// @see DatabaseService#getInstance()
    private DatabaseService() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
    }

    /// get an instance of this class
    /// @return an instance of this class
    /// @see DatabaseService
    public static DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }


    // private generic methods to write and read data from the database

    /// write data to the database at a specific path
    /// @param path the path to write the data to
    /// @param data the data to write (can be any object, but must be serializable, i.e. must have a default constructor and all fields must have getters and setters)
    /// @param callback the callback to call when the operation is completed
    /// @return void
    /// @see DatabaseCallback
    private void writeData(@NotNull final String path, @NotNull final Object data, final @Nullable DatabaseCallback<Void> callback) {
        databaseReference.child(path).setValue(data).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (callback == null) return;
                callback.onCompleted(task.getResult());
            } else {
                if (callback == null) return;
                callback.onFailed(task.getException());
            }
        });
    }

    /// read data from the database at a specific path
    /// @param path the path to read the data from
    /// @return a DatabaseReference object to read the data from
    /// @see DatabaseReference

    private DatabaseReference readData(@NotNull final String path) {
        return databaseReference.child(path);
    }


    /// get data from the database at a specific path
    /// @param path the path to get the data from
    /// @param clazz the class of the object to return
    /// @param callback the callback to call when the operation is completed
    /// @return void
    /// @see DatabaseCallback
    /// @see Class
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

    /// generate a new id for a new object in the database
    /// @param path the path to generate the id for
    /// @return a new id for the object
    /// @see String
    /// @see DatabaseReference#push()

    private String generateNewId(@NotNull final String path) {
        return databaseReference.child(path).push().getKey();
    }

    // end of private methods for reading and writing data

    // public methods to interact with the database

    /// create a new user in the database
    /// @param user the user object to create
    /// @param callback the callback to call when the operation is completed
    ///              the callback will receive void
    ///            if the operation fails, the callback will receive an exception
    /// @return void
    /// @see DatabaseCallback
    /// @see User
    public void createNewUser(@NotNull final User user, @Nullable final DatabaseCallback<Void> callback) {
        writeData("users/" + user.getId(), user, callback);
    }

    /// create a new food in the database
    /// @param food the food object to create
    /// @param callback the callback to call when the operation is completed
    ///              the callback will receive void
    ///             if the operation fails, the callback will receive an exception
    /// @return void
    /// @see DatabaseCallback
    /// @see Food
    public void createNewEvent(@NotNull final Event event, @Nullable final DatabaseCallback<Void> callback) {
        writeData("foods/" + event.getId(), event, callback);
    }


    /// get a user from the database
    /// @param uid the id of the user to get
    /// @param callback the callback to call when the operation is completed
    ///               the callback will receive the user object
    ///             if the operation fails, the callback will receive an exception
    /// @return void
    /// @see DatabaseCallback
    /// @see User
    public void getUser(@NotNull final String uid, @NotNull final DatabaseCallback<User> callback) {
        getData("users/" + uid, User.class, callback);
    }

    /// get a food from the database
    /// @param foodId the id of the food to get
    /// @param callback the callback to call when the operation is completed
    ///               the callback will receive the food object
    ///              if the operation fails, the callback will receive an exception
    /// @return void
    /// @see DatabaseCallback
    /// @see Food
    public void getFood(@NotNull final String foodId, @NotNull final DatabaseCallback<Event> callback) {
        getData("foods/" + foodId, Event.class, callback);
    }


    /// generate a new id for a new food in the database
    /// @return a new id for the food
    /// @see #generateNewId(String)
    /// @see Food
    public String generateFoodId() {
        return generateNewId("foods");
    }

    /// generate a new id for a new cart in the database
    /// @return a new id for the cart
    /// @see #generateNewId(String)
    /// @see Cart
    public String generateCartId() {
        return generateNewId("carts");
    }

    /// get all the foods from the database
    /// @param callback the callback to call when the operation is completed
    ///              the callback will receive a list of food objects
    ///            if the operation fails, the callback will receive an exception
    /// @return void
    /// @see DatabaseCallback
    /// @see List
    /// @see Food
    /// @see #getData(String, Class, DatabaseCallback)
    public void getFoods(@NotNull final DatabaseCallback<List<Event>> callback) {
        readData("foods").get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting data", task.getException());
                callback.onFailed(task.getException());
                return;
            }
            List<Event> foods = new ArrayList<>();
            task.getResult().getChildren().forEach(dataSnapshot -> {
                Event food = dataSnapshot.getValue(Event.class);
                Log.d(TAG, "Got food: " + food);
                foods.add(food);
            });

            callback.onCompleted(foods);
        });
    }

}
