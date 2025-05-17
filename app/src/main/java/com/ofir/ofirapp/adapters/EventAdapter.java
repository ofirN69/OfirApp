package com.ofir.ofirapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private ArrayList<Event> eventList;
    private Context context;
    private SharedPreferences preferences;
    private SimpleDateFormat dateFormat;
    private static final String PREF_HIDDEN_EVENTS = "hidden_events";

    public EventAdapter(ArrayList<Event> eventList, Context context) {
        this.eventList = eventList;
        this.context = context;
        this.preferences = context.getSharedPreferences("event_prefs", Context.MODE_PRIVATE);
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        this.dateFormat.setLenient(false);
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.tvEventName.setText(event.getType());
        holder.tvEventDate.setText("Date: " + event.getDate());
        holder.tvEventVenue.setText("Venue: " + event.getVenue());

        // Check if event is past
        boolean isPastEvent = isEventPast(event);
        holder.btnHideEvent.setVisibility(isPastEvent ? View.VISIBLE : View.GONE);

        // Set hide button click listener
        holder.btnHideEvent.setOnClickListener(v -> hideEvent(event.getId()));

        // Set click listener for the entire item
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EventDetailsActivity.class);
            intent.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, event.getId());
            context.startActivity(intent);
        });
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

    private void hideEvent(String eventId) {
        Set<String> hiddenEvents = preferences.getStringSet(PREF_HIDDEN_EVENTS, new HashSet<>());
        Set<String> newHiddenEvents = new HashSet<>(hiddenEvents);
        newHiddenEvents.add(eventId);
        preferences.edit().putStringSet(PREF_HIDDEN_EVENTS, newHiddenEvents).apply();
        
        // Remove the event from the list and notify adapter
        for (int i = 0; i < eventList.size(); i++) {
            if (eventList.get(i).getId().equals(eventId)) {
                eventList.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    public static boolean isEventHidden(Context context, String eventId) {
        SharedPreferences prefs = context.getSharedPreferences("event_prefs", Context.MODE_PRIVATE);
        Set<String> hiddenEvents = prefs.getStringSet(PREF_HIDDEN_EVENTS, new HashSet<>());
        return hiddenEvents.contains(eventId);
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName, tvEventDate, tvEventVenue;
        ImageButton btnHideEvent;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvEventVenue = itemView.findViewById(R.id.tvEventVenue);
            btnHideEvent = itemView.findViewById(R.id.btnHideEvent);
        }
    }
}
