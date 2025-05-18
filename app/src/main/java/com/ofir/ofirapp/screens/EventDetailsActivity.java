package com.ofir.ofirapp.screens;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Button;
import android.content.Intent;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import com.ofir.ofirapp.R;
import com.ofir.ofirapp.models.Event;
import com.ofir.ofirapp.services.DatabaseService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import android.util.Log;
import java.util.Calendar;
import android.widget.Toast;

public class EventDetailsActivity extends AppCompatActivity {

    private static final String TAG = "EventDetailsActivity";
    public static final String EXTRA_EVENT_ID = "event_id";
    private TextView tvEventType, tvEventDate, tvEventVenue, tvEventAddress, tvEventCity;
    private TextView tvEventDressCode, tvEventMenuType, tvCountdown;
    private Button btnOpenMaps;
    private FloatingActionButton btnReturn;
    private String eventAddress;
    private Handler countdownHandler;
    private Runnable countdownRunnable;
    private Date eventDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        // Initialize views and handler
        initializeViews();
        countdownHandler = new Handler(Looper.getMainLooper());

        // Get event ID from intent
        String eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
        if (eventId != null && !eventId.isEmpty()) {
            loadEventDetails(eventId);
        } else {
            Toast.makeText(this, "Error: Event ID not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countdownHandler != null && countdownRunnable != null) {
            countdownHandler.removeCallbacks(countdownRunnable);
        }
    }

    private void initializeViews() {
        try {
            tvEventType = findViewById(R.id.tvEventType);
            tvEventDate = findViewById(R.id.tvEventDate);
            tvEventVenue = findViewById(R.id.tvEventVenue);
            tvEventAddress = findViewById(R.id.tvEventAddress);
            tvEventCity = findViewById(R.id.tvEventCity);
            tvEventDressCode = findViewById(R.id.tvEventDressCode);
            tvEventMenuType = findViewById(R.id.tvEventMenuType);
            tvCountdown = findViewById(R.id.tvCountdown);
            btnOpenMaps = findViewById(R.id.btnOpenMaps);
            btnReturn = findViewById(R.id.btnReturn);

            // Set up Maps button click listener
            btnOpenMaps.setOnClickListener(v -> openInGoogleMaps());
            
            // Set up return button click listener
            btnReturn.setOnClickListener(v -> finish());
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage());
            Toast.makeText(this, "Error initializing views", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void openInGoogleMaps() {
        if (eventAddress != null && !eventAddress.isEmpty()) {
            try {
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(eventAddress));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    Toast.makeText(this, "Google Maps app not found", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error opening maps: " + e.getMessage());
                Toast.makeText(this, "Error opening maps", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadEventDetails(String eventId) {
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Error: Invalid event ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            DatabaseService.getInstance().getEvent(eventId, new DatabaseService.DatabaseCallback<Event>() {
                @Override
                public void onCompleted(Event event) {
                    runOnUiThread(() -> {
                        if (isFinishing()) {
                            return;
                        }
                        if (event != null) {
                            displayEventDetails(event);
                        } else {
                            Toast.makeText(EventDetailsActivity.this, "Event not found", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }

                @Override
                public void onFailed(Exception e) {
                    Log.e(TAG, "Error loading event: " + (e != null ? e.getMessage() : "Unknown error"));
                    runOnUiThread(() -> {
                        if (isFinishing()) {
                            return;
                        }
                        Toast.makeText(EventDetailsActivity.this, 
                            "Error loading event details: " + (e != null ? e.getMessage() : "Unknown error"), 
                            Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error initializing database service: " + e.getMessage());
            Toast.makeText(this, "Error connecting to database", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void displayEventDetails(Event event) {
        try {
            if (event == null) {
                Toast.makeText(this, "Error: Event data is missing", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Set event type with null check
            String eventType = event.getType() != null ? event.getType() : "N/A";
            tvEventType.setText(getString(R.string.event_type_format, eventType));

            // Set event date with null check
            String eventDateStr = event.getDate() != null ? event.getDate() : "N/A";
            tvEventDate.setText(getString(R.string.event_date_format, eventDateStr));

            // Set venue with null check
            String venue = event.getVenue() != null ? event.getVenue() : "N/A";
            tvEventVenue.setText(getString(R.string.event_venue_format, venue));
            
            // Store the full address for Maps integration with null checks
            String address = event.getAddress() != null ? event.getAddress() : "";
            String city = event.getCity() != null ? event.getCity() : "";
            eventAddress = address + ((!address.isEmpty() && !city.isEmpty()) ? ", " : "") + city;
            
            tvEventAddress.setText(getString(R.string.event_address_format, address.isEmpty() ? "N/A" : address));
            tvEventCity.setText(getString(R.string.event_city_format, city.isEmpty() ? "N/A" : city));
            
            // Set dress code with null check
            String dressCode = event.getDresscode() != null ? event.getDresscode() : "N/A";
            tvEventDressCode.setText(getString(R.string.event_dress_code_format, dressCode));
            
            // Set menu type with null check
            String menuType = event.getMenutype() != null ? event.getMenutype() : "N/A";
            tvEventMenuType.setText(getString(R.string.event_menu_type_format, menuType));

            // Start countdown only if we have a valid date
            if (event.getDate() != null) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    sdf.setLenient(false);
                    eventDate = sdf.parse(event.getDate());
                    
                    if (eventDate != null) {
                        // Set time to noon to avoid timezone issues
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(eventDate);
                        cal.set(Calendar.HOUR_OF_DAY, 12);
                        cal.set(Calendar.MINUTE, 0);
                        cal.set(Calendar.SECOND, 0);
                        eventDate = cal.getTime();
                        
                        startCountdown();
                    } else {
                        tvCountdown.setText(getString(R.string.invalid_date_format));
                    }
                } catch (ParseException e) {
                    Log.e(TAG, "Date parsing error: " + e.getMessage());
                    tvCountdown.setText(getString(R.string.invalid_date_format));
                }
            } else {
                tvCountdown.setText(getString(R.string.invalid_date_format));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error displaying event details: " + e.getMessage());
            Toast.makeText(this, "Error displaying event details", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void startCountdown() {
        if (countdownRunnable != null) {
            countdownHandler.removeCallbacks(countdownRunnable);
        }

        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    long currentTime = System.currentTimeMillis();
                    long eventTime = eventDate.getTime();
                    long timeDiff = eventTime - currentTime;

                    // Check if the event is today
                    Calendar today = Calendar.getInstance();
                    Calendar eventCal = Calendar.getInstance();
                    eventCal.setTime(eventDate);

                    boolean isToday = today.get(Calendar.YEAR) == eventCal.get(Calendar.YEAR) &&
                                    today.get(Calendar.DAY_OF_YEAR) == eventCal.get(Calendar.DAY_OF_YEAR);

                    if (isToday) {
                        tvCountdown.setText(getString(R.string.event_is_today));
                        tvCountdown.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                    } else if (timeDiff > 0) {
                        long days = TimeUnit.MILLISECONDS.toDays(timeDiff);
                        timeDiff -= TimeUnit.DAYS.toMillis(days);
                        long hours = TimeUnit.MILLISECONDS.toHours(timeDiff);
                        timeDiff -= TimeUnit.HOURS.toMillis(hours);
                        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeDiff);

                        String countdownText = getString(R.string.countdown_format, days, hours, minutes);
                        tvCountdown.setText(countdownText);
                        tvCountdown.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                        
                        // Update every minute
                        countdownHandler.postDelayed(this, 60000);
                    } else {
                        tvCountdown.setText(getString(R.string.event_has_passed));
                        tvCountdown.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error updating countdown: " + e.getMessage());
                }
            }
        };

        // Start the countdown immediately
        countdownHandler.post(countdownRunnable);
    }
} 