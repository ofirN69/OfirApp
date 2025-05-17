package com.ofir.ofirapp.screens;

import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Button;
import android.content.Intent;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import com.ofir.ofirapp.R;
import com.ofir.ofirapp.models.Event;
import com.ofir.ofirapp.services.DatabaseService;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import android.util.Log;
import java.util.Calendar;

public class EventDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "event_id";
    private TextView tvEventType, tvEventDate, tvEventVenue, tvEventAddress, tvEventCity;
    private TextView tvEventDressCode, tvEventMenuType, tvCountdown;
    private Button btnOpenMaps;
    private String eventAddress;
    private Handler countdownHandler;
    private Runnable countdownRunnable;
    private Date eventDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        // Initialize views
        initializeViews();

        // Get event ID from intent
        String eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
        if (eventId != null) {
            loadEventDetails(eventId);
        }

        // Set up Maps button click listener
        btnOpenMaps.setOnClickListener(v -> openInGoogleMaps());

        // Initialize countdown handler
        countdownHandler = new Handler();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countdownHandler != null && countdownRunnable != null) {
            countdownHandler.removeCallbacks(countdownRunnable);
        }
    }

    private void initializeViews() {
        tvEventType = findViewById(R.id.tvEventType);
        tvEventDate = findViewById(R.id.tvEventDate);
        tvEventVenue = findViewById(R.id.tvEventVenue);
        tvEventAddress = findViewById(R.id.tvEventAddress);
        tvEventCity = findViewById(R.id.tvEventCity);
        tvEventDressCode = findViewById(R.id.tvEventDressCode);
        tvEventMenuType = findViewById(R.id.tvEventMenuType);
        tvCountdown = findViewById(R.id.tvCountdown);
        btnOpenMaps = findViewById(R.id.btnOpenMaps);
    }

    private void openInGoogleMaps() {
        if (eventAddress != null && !eventAddress.isEmpty()) {
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(eventAddress));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            }
        }
    }

    private void loadEventDetails(String eventId) {
        DatabaseService.getInstance().getEvent(eventId, new DatabaseService.DatabaseCallback<Event>() {
            @Override
            public void onCompleted(Event event) {
                displayEventDetails(event);
            }

            @Override
            public void onFailed(Exception e) {
                // Handle error
            }
        });
    }

    private void displayEventDetails(Event event) {
        tvEventType.setText("Event Type: " + event.getType());
        tvEventDate.setText("Date: " + event.getDate());
        tvEventVenue.setText("Venue: " + event.getVenue());
        
        // Store the full address for Maps integration
        eventAddress = event.getAddress() + ", " + event.getCity();
        tvEventAddress.setText("Address: " + event.getAddress());
        tvEventCity.setText("City: " + event.getCity());
        tvEventDressCode.setText("Dress Code: " + event.getDresscode());
        tvEventMenuType.setText("Menu Type: " + event.getMenutype());

        // Start countdown
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            sdf.setLenient(false); // Strict parsing
            eventDate = sdf.parse(event.getDate());
            
            // Set time to noon to avoid timezone issues
            eventDate.setHours(12);
            eventDate.setMinutes(0);
            eventDate.setSeconds(0);
            
            startCountdown();
        } catch (ParseException e) {
            Log.e("EventDetails", "Date parsing error: " + e.getMessage());
            tvCountdown.setText("Unable to calculate countdown - Invalid date format");
        }
    }

    private void startCountdown() {
        if (countdownRunnable != null) {
            countdownHandler.removeCallbacks(countdownRunnable);
        }

        countdownRunnable = new Runnable() {
            @Override
            public void run() {
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
                    // Event is today
                    tvCountdown.setText("Today is the big day!");
                    tvCountdown.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                } else if (timeDiff > 0) {
                    // Event is in the future
                    long days = TimeUnit.MILLISECONDS.toDays(timeDiff);
                    timeDiff -= TimeUnit.DAYS.toMillis(days);
                    long hours = TimeUnit.MILLISECONDS.toHours(timeDiff);
                    timeDiff -= TimeUnit.HOURS.toMillis(hours);
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(timeDiff);

                    String countdownText = String.format(Locale.getDefault(),
                            "Time until event: %d days, %d hours, %d minutes",
                            days, hours, minutes);
                    tvCountdown.setText(countdownText);
                    tvCountdown.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    
                    // Update every minute
                    countdownHandler.postDelayed(this, 60000);
                } else {
                    // Event has passed
                    tvCountdown.setText("This event has already taken place");
                    tvCountdown.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                }
            }
        };

        // Start the countdown immediately
        countdownHandler.post(countdownRunnable);
    }
} 