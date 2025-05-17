package com.ofir.ofirapp.screens;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.ofir.ofirapp.R;
import com.ofir.ofirapp.models.User;
import com.ofir.ofirapp.services.DatabaseService;
import com.ofir.ofirapp.utils.SharedPreferencesUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class EditProfileActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText etDisplayName, etPhone;
    private ImageView ivProfilePic;
    private MaterialButton btnSave, btnChangePhoto;
    private ProgressBar progressBar;
    
    private DatabaseService databaseService;
    private User currentUser;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize services and views
        initializeViews();
        databaseService = DatabaseService.getInstance();
        currentUser = SharedPreferencesUtil.getUser(this);

        // Load current user data
        loadUserData();

        // Set click listeners
        btnChangePhoto.setOnClickListener(v -> openImagePicker());
        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void initializeViews() {
        etDisplayName = findViewById(R.id.etDisplayName);
        etPhone = findViewById(R.id.etPhone);
        ivProfilePic = findViewById(R.id.ivProfilePic);
        btnSave = findViewById(R.id.btnSave);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        progressBar = findViewById(R.id.progressBar);
    }

    private void loadUserData() {
        if (currentUser != null) {
            etDisplayName.setText(currentUser.getFname());
            etPhone.setText(currentUser.getPhone());
            
            // Load profile image if exists
            String profileImage = currentUser.getProfileImage();
            if (profileImage != null && !profileImage.isEmpty()) {
                byte[] decodedString = Base64.decode(profileImage, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                ivProfilePic.setImageBitmap(decodedByte);
            }
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                ivProfilePic.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveProfile() {
        if (currentUser == null) return;

        String displayName = etDisplayName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (displayName.isEmpty()) {
            etDisplayName.setError("Display name is required");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        // Update user object with new values
        currentUser.setFname(displayName);
        currentUser.setPhone(phone);

        // If new image was selected, convert it to Base64
        if (imageUri != null) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                byte[] imageBytes = baos.toByteArray();
                String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                currentUser.setProfileImage(base64Image);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                return;
            }
        }

        // Save to Realtime Database
        databaseService.updateUser(currentUser, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
                runOnUiThread(() -> {
                    // Save updated user to SharedPreferences
                    SharedPreferencesUtil.saveUser(EditProfileActivity.this, currentUser);
                    
                    // Update UI
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    
                    // Set result to force refresh of parent activity
                    setResult(RESULT_OK);
                    finish();
                });
            }

            @Override
            public void onFailed(Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(EditProfileActivity.this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
} 