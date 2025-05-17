package com.ofir.ofirapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ofir.ofirapp.R;
import com.ofir.ofirapp.models.Event;
import com.ofir.ofirapp.screens.EventDetailsActivity;
import com.ofir.ofirapp.services.AuthenticationService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ExpandableEventAdapter extends RecyclerView.Adapter<ExpandableEventAdapter.EventViewHolder> {
    private static final String PREF_HIDDEN_EVENTS = "hidden_events";

    private final Context context;
    private final List<Event> events;
    private final SimpleDateFormat dateFormat;
    private final SharedPreferences preferences;
    private final String currentUserId;

    public ExpandableEventAdapter(Context context) {
        this.context = context;
        this.events = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        this.dateFormat.setLenient(false);
        this.preferences = context.getSharedPreferences("event_prefs", Context.MODE_PRIVATE);
        this.currentUserId = AuthenticationService.getInstance().getCurrentUserId();
    }

    public void clearItems() {
        events.clear();
        notifyDataSetChanged();
    }

    public void addEvents(List<Event> newEvents) {
        if (newEvents == null) {
            return;
        }
        int startPosition = events.size();
        events.addAll(newEvents);
        notifyItemRangeInserted(startPosition, newEvents.size());
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.tvEventName.setText(event.getType());
        holder.tvEventDate.setText("Date: " + event.getDate());
        holder.tvEventVenue.setText("Venue: " + event.getVenue());

        // Set ownership status with null checks
        String ownerId = event.getOwnerId();
        boolean isOwner = ownerId != null && ownerId.equals(currentUserId);
        holder.tvEventRole.setText(isOwner ? "Owner" : "Invited");
        holder.tvEventRole.setBackgroundTintList(ContextCompat.getColorStateList(context, 
            isOwner ? R.color.owner_label : R.color.invited_label));
        
        // Set ownership icon
        holder.ivEventOwnership.setImageResource(
            isOwner ? R.drawable.ic_calendar : R.drawable.ic_event);
        holder.ivEventOwnership.setImageTintList(ContextCompat.getColorStateList(context,
            isOwner ? R.color.owner_icon : R.color.invited_icon));

        // Check if event is past
        boolean isPastEvent = isEventPast(event);
        holder.btnHideEvent.setVisibility(isPastEvent ? View.VISIBLE : View.GONE);

        // Set hide button click listener
        holder.btnHideEvent.setOnClickListener(v -> hideEvent(event.getId(), position));

        // Set click listener for the entire item
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EventDetailsActivity.class);
            intent.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, event.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
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
        Set<String> hiddenEvents = preferences.getStringSet(PREF_HIDDEN_EVENTS, new HashSet<>());
        Set<String> newHiddenEvents = new HashSet<>(hiddenEvents);
        newHiddenEvents.add(eventId);
        preferences.edit().putStringSet(PREF_HIDDEN_EVENTS, newHiddenEvents).apply();
        
        events.remove(position);
        notifyItemRemoved(position);
    }

    public static boolean isEventHidden(Context context, String eventId) {
        SharedPreferences prefs = context.getSharedPreferences("event_prefs", Context.MODE_PRIVATE);
        Set<String> hiddenEvents = prefs.getStringSet(PREF_HIDDEN_EVENTS, new HashSet<>());
        return hiddenEvents.contains(eventId);
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName;
        TextView tvEventDate;
        TextView tvEventVenue;
        TextView tvEventRole;
        ImageView ivEventOwnership;
        ImageButton btnHideEvent;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvEventVenue = itemView.findViewById(R.id.tvEventVenue);
            tvEventRole = itemView.findViewById(R.id.tvEventRole);
            ivEventOwnership = itemView.findViewById(R.id.ivEventOwnership);
            btnHideEvent = itemView.findViewById(R.id.btnHideEvent);
        }
    }
} 