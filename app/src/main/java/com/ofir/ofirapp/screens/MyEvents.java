package com.ofir.ofirapp.screens;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ofir.ofirapp.R;
import com.ofir.ofirapp.adapters.SectionedEventAdapter;
import com.ofir.ofirapp.models.Event;
import com.ofir.ofirapp.services.AuthenticationService;
import com.ofir.ofirapp.services.DatabaseService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class MyEvents extends AppCompatActivity {

    private static final String TAG = "MyEvents";
    
    private RecyclerView recyclerView;
    private SectionedEventAdapter eventAdapter;
    private TextView tvNoEvents;

    private String userId;
    private AuthenticationService authenticationService;
    private DatabaseService databaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);
        
        // Initialize services
        initializeServices();
        
        // Initialize views
        initializeViews();

        // Load events
        loadEvents();
    }

    private void initializeServices() {
        authenticationService = AuthenticationService.getInstance();
        databaseService = DatabaseService.getInstance();
        userId = authenticationService.getCurrentUserId();
    }

    private void initializeViews() {
        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerViewEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Initialize adapter
        eventAdapter = new SectionedEventAdapter(this);
        recyclerView.setAdapter(eventAdapter);
        
        // Initialize no events text view
        tvNoEvents = findViewById(R.id.tvNoEvents);
    }

    private void loadEvents() {
        databaseService.getUserEvents(userId, new DatabaseService.DatabaseCallback<List<Event>>() {
            @Override
            public void onCompleted(List<Event> events) {
                runOnUiThread(() -> {
                    // Filter out hidden events
                    List<Event> visibleEvents = events.stream()
                        .filter(event -> !SectionedEventAdapter.isEventHidden(MyEvents.this, event.getId()))
                        .collect(Collectors.toList());

                    updateEventsDisplay(visibleEvents);
                });
            }

            @Override
            public void onFailed(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(MyEvents.this, "Failed to load events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    showNoEventsView();
                });
            }
        });
    }

    private void updateEventsDisplay(List<Event> visibleEvents) {
        if (visibleEvents.isEmpty()) {
            showNoEventsView();
            return;
        }

        showEventsView();
        categorizeAndDisplayEvents(visibleEvents);
    }

    private void showNoEventsView() {
        recyclerView.setVisibility(View.GONE);
        tvNoEvents.setVisibility(View.VISIBLE);
    }

    private void showEventsView() {
        recyclerView.setVisibility(View.VISIBLE);
        tvNoEvents.setVisibility(View.GONE);
    }

    private void categorizeAndDisplayEvents(List<Event> events) {
        // Get today's start and end
        Calendar calendar = Calendar.getInstance();
        setCalendarToStartOfDay(calendar);
        Date startOfToday = calendar.getTime();

        setCalendarToEndOfDay(calendar);
        Date endOfToday = calendar.getTime();

        // Sort events into time categories
        List<Event> futureEvents = new ArrayList<>();
        List<Event> todayEvents = new ArrayList<>();
        List<Event> pastEvents = new ArrayList<>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dateFormat.setLenient(false);

        // Categorize events by time
        for (Event event : events) {
            categorizeEvent(event, dateFormat, startOfToday, endOfToday, futureEvents, todayEvents, pastEvents);
        }

        // Clear adapter and add sections
        eventAdapter.clearItems();
        addEventSections(futureEvents, todayEvents, pastEvents);
    }

    private void setCalendarToStartOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private void setCalendarToEndOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
    }

    private void categorizeEvent(Event event, SimpleDateFormat dateFormat, Date startOfToday, Date endOfToday,
                               List<Event> futureEvents, List<Event> todayEvents, List<Event> pastEvents) {
        try {
            Date eventDate = dateFormat.parse(event.getDate());
            if (eventDate != null) {
                // Set time to noon to avoid timezone issues
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(eventDate);
                calendar.set(Calendar.HOUR_OF_DAY, 12);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                eventDate = calendar.getTime();

                if (eventDate.before(startOfToday)) {
                    pastEvents.add(event);
                } else if (!eventDate.before(startOfToday) && !eventDate.after(endOfToday)) {
                    todayEvents.add(event);
                } else {
                    futureEvents.add(event);
                }
            }
        } catch (ParseException e) {
            // If date parsing fails, add to future events
            futureEvents.add(event);
        }
    }

    private void addEventSections(List<Event> futureEvents, List<Event> todayEvents, List<Event> pastEvents) {
        // Add Future Events section
        if (!futureEvents.isEmpty()) {
            addEventSection("Future Events", futureEvents);
        }

        // Add Today's Events section
        if (!todayEvents.isEmpty()) {
            addEventSection("Today's Events", todayEvents);
        }

        // Add Past Events section
        if (!pastEvents.isEmpty()) {
            addEventSection("Past Events", pastEvents);
        }
    }

    private void addEventSection(String sectionTitle, List<Event> events) {
        eventAdapter.addSection(sectionTitle);
        
        // Add owned events
        List<Event> ownedEvents = events.stream()
            .filter(event -> event.getOwnerId() != null && event.getOwnerId().equals(userId))
            .collect(Collectors.toList());
        if (!ownedEvents.isEmpty()) {
            eventAdapter.addSection("Events I Own");
            eventAdapter.addEvents(ownedEvents);
        }

        // Add invited events
        List<Event> invitedEvents = events.stream()
            .filter(event -> event.getOwnerId() == null || !event.getOwnerId().equals(userId))
            .collect(Collectors.toList());
        if (!invitedEvents.isEmpty()) {
            eventAdapter.addSection("Events I'm Invited To");
            eventAdapter.addEvents(invitedEvents);
        }
    }
}
