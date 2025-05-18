package com.ofir.ofirapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ofir.ofirapp.R;
import com.ofir.ofirapp.models.Event;
import com.ofir.ofirapp.screens.EventDetailsActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class SectionedEventAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_EVENT = 1;
    private static final String PREF_HIDDEN_EVENTS = "hidden_events";

    private final Context context;
    private final List<Object> items; // Can be either String (header) or Event
    private final SimpleDateFormat dateFormat;
    private final SharedPreferences preferences;

    public SectionedEventAdapter(Context context) {
        this.context = context;
        this.items = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        this.dateFormat.setLenient(false);
        this.preferences = context.getSharedPreferences("event_prefs", Context.MODE_PRIVATE);
    }

    public void clearItems() {
        items.clear();
        notifyDataSetChanged();
    }

    public void addSection(String sectionTitle) {
        items.add(sectionTitle);
        notifyItemInserted(items.size() - 1);
    }

    public void addEvents(List<Event> events) {
        int startPosition = items.size();
        items.addAll(events);
        notifyItemRangeInserted(startPosition, events.size());
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_HEADER) {
            View view = inflater.inflate(R.layout.item_section_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_event, parent, false);
            return new EventViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int itemType = getItemViewType(position);
        if (itemType == TYPE_HEADER) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            String sectionTitle = (String) items.get(position);
            headerHolder.tvSectionTitle.setText(sectionTitle);
        } else {
            EventViewHolder eventHolder = (EventViewHolder) holder;
            Event event = (Event) items.get(position);
            
            eventHolder.tvEventName.setText(event.getType());
            eventHolder.tvEventDate.setText("Date: " + event.getDate());
            eventHolder.tvEventVenue.setText("Venue: " + event.getVenue());

            // Check if event is past
            boolean isPastEvent = isEventPast(event);
            eventHolder.btnHideEvent.setVisibility(isPastEvent ? View.VISIBLE : View.GONE);

            // Set hide button click listener
            eventHolder.btnHideEvent.setOnClickListener(v -> hideEvent(event.getId(), position));

            // Set click listener for the entire item
            eventHolder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, EventDetailsActivity.class);
                intent.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, event.getId());
                context.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof String ? TYPE_HEADER : TYPE_EVENT;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private boolean isEventPast(Event event) {
        try {
            Date eventDate = dateFormat.parse(event.getDate());
            eventDate.setHours(23);
            eventDate.setMinutes(59);
            eventDate.setSeconds(59);
            return eventDate.before(new Date());
        } catch (ParseException e) {
            return false;
        }
    }

    private void hideEvent(String eventId, int position) {
        if (eventId == null || position < 0 || position >= items.size()) {
            return;
        }

        try {
            // Get current hidden events
            Set<String> hiddenEvents = preferences.getStringSet(PREF_HIDDEN_EVENTS, new HashSet<>());
            Set<String> newHiddenEvents = new HashSet<>(hiddenEvents);
            
            // Add the event to hidden set
            newHiddenEvents.add(eventId);
            
            // Apply changes to SharedPreferences
            SharedPreferences.Editor editor = preferences.edit();
            editor.putStringSet(PREF_HIDDEN_EVENTS, newHiddenEvents);
            editor.apply();
            
            // Remove from adapter's list
            if (position < items.size()) {
                items.remove(position);
                notifyItemRemoved(position);
                
                // Remove section header if this was the last event in the section
                if (position > 0 && position < items.size() && 
                    items.get(position - 1) instanceof String && 
                    (position == items.size() || items.get(position) instanceof String)) {
                    items.remove(position - 1);
                    notifyItemRemoved(position - 1);
                }
            }
        } catch (Exception e) {
            // Log error but don't crash
            Log.e("SectionedEventAdapter", "Error hiding event: " + e.getMessage());
        }
    }

    public static boolean isEventHidden(Context context, String eventId) {
        SharedPreferences prefs = context.getSharedPreferences("event_prefs", Context.MODE_PRIVATE);
        Set<String> hiddenEvents = prefs.getStringSet(PREF_HIDDEN_EVENTS, new HashSet<>());
        return hiddenEvents.contains(eventId);
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvSectionTitle;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSectionTitle = itemView.findViewById(R.id.tvSectionTitle);
        }
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName;
        TextView tvEventDate;
        TextView tvEventVenue;
        ImageButton btnHideEvent;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvEventVenue = itemView.findViewById(R.id.tvEventVenue);
            btnHideEvent = itemView.findViewById(R.id.btnHideEvent);
        }
    }
} 