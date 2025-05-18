package com.ofir.ofirapp.screens;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ofir.ofirapp.R;
import com.ofir.ofirapp.adapters.ExpandableEventAdapter;
import com.ofir.ofirapp.models.Event;
import com.ofir.ofirapp.services.AuthenticationService;
import com.ofir.ofirapp.services.DatabaseService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class MyEvents extends BaseActivity {
    private static final String TAG = "MyEvents";
    
    private RecyclerView recyclerView;
    private ExpandableEventAdapter eventAdapter;
    private CardView eventsListCard;
    private TextView selectedCategoryTitle;
    private TextView todayEventsCount;
    private TextView futureEventsCount;
    private TextView pastEventsCount;
    private CardView todayEventsCard;
    private CardView futureEventsCard;
    private CardView pastEventsCard;
    private ImageButton closeEventsButton;
    private TextView noCategoryText;

    private String userId;
    private AuthenticationService authenticationService;
    private DatabaseService databaseService;
    
    private List<Event> allTodayEvents;
    private List<Event> allFutureEvents;
    private List<Event> allPastEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);
        
        // Initialize services
        initializeServices();
        
        // Initialize views
        initializeViews();

        // Initialize event lists
        allTodayEvents = new ArrayList<>();
        allFutureEvents = new ArrayList<>();
        allPastEvents = new ArrayList<>();

        // Load events
        loadEvents();
        
        setActionBarTitle("My Events");
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
        eventAdapter = new ExpandableEventAdapter(this);
        recyclerView.setAdapter(eventAdapter);
        
        // Initialize cards and text views
        eventsListCard = findViewById(R.id.eventsListCard);
        selectedCategoryTitle = findViewById(R.id.selectedCategoryTitle);
        todayEventsCount = findViewById(R.id.todayEventsCount);
        futureEventsCount = findViewById(R.id.futureEventsCount);
        pastEventsCount = findViewById(R.id.pastEventsCount);
        todayEventsCard = findViewById(R.id.todayEventsCard);
        futureEventsCard = findViewById(R.id.futureEventsCard);
        pastEventsCard = findViewById(R.id.pastEventsCard);
        closeEventsButton = findViewById(R.id.closeEventsButton);
        noCategoryText = findViewById(R.id.noCategoryText);

        // Set click listeners
        todayEventsCard.setOnClickListener(v -> showCategoryEvents("Today's Events", allTodayEvents));
        futureEventsCard.setOnClickListener(v -> showCategoryEvents("Future Events", allFutureEvents));
        pastEventsCard.setOnClickListener(v -> showCategoryEvents("Past Events", allPastEvents));
        closeEventsButton.setOnClickListener(v -> hideEventsList());
    }

    private void loadEvents() {
        if (databaseService == null || userId == null) {
            Toast.makeText(this, "Error: Services not initialized properly", Toast.LENGTH_SHORT).show();
            updateEventCounts(0, 0, 0);
            return;
        }

        databaseService.getUserEvents(userId, new DatabaseService.DatabaseCallback<List<Event>>() {
            @Override
            public void onCompleted(List<Event> events) {
                runOnUiThread(() -> {
                    if (events == null) {
                        Toast.makeText(MyEvents.this, "No events found", Toast.LENGTH_SHORT).show();
                        updateEventCounts(0, 0, 0);
                        return;
                    }

                    // Filter out hidden events
                    List<Event> visibleEvents = events.stream()
                        .filter(event -> event != null && event.getId() != null)
                        .filter(event -> !ExpandableEventAdapter.isEventHidden(MyEvents.this, event.getId()))
                        .collect(Collectors.toList());

                    categorizeEvents(visibleEvents);
                });
            }

            @Override
            public void onFailed(Exception e) {
                runOnUiThread(() -> {
                    String errorMessage = e != null ? e.getMessage() : "Unknown error";
                    Toast.makeText(MyEvents.this, "Failed to load events: " + errorMessage, Toast.LENGTH_SHORT).show();
                    updateEventCounts(0, 0, 0);
                });
            }
        });
    }

    private void categorizeEvents(List<Event> events) {
        if (events == null) {
            updateEventCounts(0, 0, 0);
            return;
        }

        allTodayEvents.clear();
        allFutureEvents.clear();
        allPastEvents.clear();

        // Get today's start and end
        Calendar calendar = Calendar.getInstance();
        setCalendarToStartOfDay(calendar);
        Date startOfToday = calendar.getTime();

        setCalendarToEndOfDay(calendar);
        Date endOfToday = calendar.getTime();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dateFormat.setLenient(false);

        // Categorize events
        for (Event event : events) {
            if (event == null || event.getDate() == null) {
                continue;
            }
            
            try {
                Date eventDate = dateFormat.parse(event.getDate());
                if (eventDate != null) {
                    // Set time to noon to avoid timezone issues
                    Calendar eventCalendar = Calendar.getInstance();
                    eventCalendar.setTime(eventDate);
                    eventCalendar.set(Calendar.HOUR_OF_DAY, 12);
                    eventCalendar.set(Calendar.MINUTE, 0);
                    eventCalendar.set(Calendar.SECOND, 0);
                    eventCalendar.set(Calendar.MILLISECOND, 0);
                    eventDate = eventCalendar.getTime();

                    if (!eventDate.before(startOfToday) && !eventDate.after(endOfToday)) {
                        allTodayEvents.add(event);
                    } else if (eventDate.after(endOfToday)) {
                        allFutureEvents.add(event);
                    } else {
                        allPastEvents.add(event);
                    }
                }
            } catch (ParseException e) {
                // If date parsing fails, add to future events
                allFutureEvents.add(event);
            }
        }

        // Update counts
        updateEventCounts(allTodayEvents.size(), allFutureEvents.size(), allPastEvents.size());
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

    private void updateEventCounts(int todayCount, int futureCount, int pastCount) {
        todayEventsCount.setText("Today's Events: " + todayCount);
        futureEventsCount.setText("Future Events: " + futureCount);
        pastEventsCount.setText("Past Events: " + pastCount);
    }

    private void showCategoryEvents(String categoryTitle, List<Event> events) {
        if (events == null) {
            Toast.makeText(this, "No events available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        selectedCategoryTitle.setText(categoryTitle != null ? categoryTitle : "Events");
        eventAdapter.clearItems();
        eventAdapter.addEvents(events);
        eventsListCard.setVisibility(View.VISIBLE);
        noCategoryText.setVisibility(View.GONE);
    }

    private void hideEventsList() {
        eventsListCard.setVisibility(View.GONE);
        noCategoryText.setVisibility(View.VISIBLE);
    }
}
