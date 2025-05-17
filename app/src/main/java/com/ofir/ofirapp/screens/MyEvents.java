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
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class MyEvents extends AppCompatActivity {

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
        authenticationService = AuthenticationService.getInstance();
        databaseService = DatabaseService.getInstance();
        userId = authenticationService.getCurrentUserId();

        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewEvents);
        tvNoEvents = findViewById(R.id.tvNoEvents);

        // Initialize RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize adapter
        eventAdapter = new SectionedEventAdapter(this);
        recyclerView.setAdapter(eventAdapter);

        // Load events
        loadEvents();
    }

    private void loadEvents() {
        databaseService.getUserEvents(userId, new DatabaseService.DatabaseCallback<List<Event>>() {
            @Override
            public void onCompleted(List<Event> events) {
                // Filter out hidden events
                List<Event> visibleEvents = events.stream()
                    .filter(event -> !SectionedEventAdapter.isEventHidden(MyEvents.this, event.getId()))
                    .collect(Collectors.toList());

                if (visibleEvents.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    tvNoEvents.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    tvNoEvents.setVisibility(View.GONE);
                    
                    // Get today's start and end
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    Date startOfToday = calendar.getTime();

                    calendar.set(Calendar.HOUR_OF_DAY, 23);
                    calendar.set(Calendar.MINUTE, 59);
                    calendar.set(Calendar.SECOND, 59);
                    calendar.set(Calendar.MILLISECOND, 999);
                    Date endOfToday = calendar.getTime();

                    // Sort events into time categories
                    List<Event> futureEvents = new ArrayList<>();
                    List<Event> todayEvents = new ArrayList<>();
                    List<Event> pastEvents = new ArrayList<>();

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    dateFormat.setLenient(false);

                    // Categorize events by time
                    for (Event event : visibleEvents) {
                        try {
                            Date eventDate = dateFormat.parse(event.getDate());
                            if (eventDate != null) {
                                // Set time to noon to avoid timezone issues
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

                    // Clear adapter
                    eventAdapter.clearItems();

                    // Add Future Events section
                    if (!futureEvents.isEmpty()) {
                        eventAdapter.addSection("Future Events");
                        
                        // Add owned future events
                        List<Event> ownedFuture = futureEvents.stream()
                            .filter(event -> event.getOwnerId() != null && event.getOwnerId().equals(userId))
                            .collect(Collectors.toList());
                        if (!ownedFuture.isEmpty()) {
                            eventAdapter.addSection("Events I Own");
                            eventAdapter.addEvents(ownedFuture);
                        }

                        // Add invited future events
                        List<Event> invitedFuture = futureEvents.stream()
                            .filter(event -> event.getOwnerId() == null || !event.getOwnerId().equals(userId))
                            .collect(Collectors.toList());
                        if (!invitedFuture.isEmpty()) {
                            eventAdapter.addSection("Events I'm Invited To");
                            eventAdapter.addEvents(invitedFuture);
                        }
                    }

                    // Add Today's Events section
                    if (!todayEvents.isEmpty()) {
                        eventAdapter.addSection("Today's Events");
                        
                        // Add owned today's events
                        List<Event> ownedToday = todayEvents.stream()
                            .filter(event -> event.getOwnerId() != null && event.getOwnerId().equals(userId))
                            .collect(Collectors.toList());
                        if (!ownedToday.isEmpty()) {
                            eventAdapter.addSection("Events I Own");
                            eventAdapter.addEvents(ownedToday);
                        }

                        // Add invited today's events
                        List<Event> invitedToday = todayEvents.stream()
                            .filter(event -> event.getOwnerId() == null || !event.getOwnerId().equals(userId))
                            .collect(Collectors.toList());
                        if (!invitedToday.isEmpty()) {
                            eventAdapter.addSection("Events I'm Invited To");
                            eventAdapter.addEvents(invitedToday);
                        }
                    }

                    // Add Past Events section
                    if (!pastEvents.isEmpty()) {
                        eventAdapter.addSection("Past Events");
                        
                        // Add owned past events
                        List<Event> ownedPast = pastEvents.stream()
                            .filter(event -> event.getOwnerId() != null && event.getOwnerId().equals(userId))
                            .collect(Collectors.toList());
                        if (!ownedPast.isEmpty()) {
                            eventAdapter.addSection("Events I Own");
                            eventAdapter.addEvents(ownedPast);
                        }

                        // Add invited past events
                        List<Event> invitedPast = pastEvents.stream()
                            .filter(event -> event.getOwnerId() == null || !event.getOwnerId().equals(userId))
                            .collect(Collectors.toList());
                        if (!invitedPast.isEmpty()) {
                            eventAdapter.addSection("Events I'm Invited To");
                            eventAdapter.addEvents(invitedPast);
                        }
                    }
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(MyEvents.this, "Failed to load events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                recyclerView.setVisibility(View.GONE);
                tvNoEvents.setVisibility(View.VISIBLE);
            }
        });
    }
}
