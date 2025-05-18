package com.ofir.ofirapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ofir.ofirapp.R;
import com.ofir.ofirapp.models.Event;
import com.ofir.ofirapp.models.User;
import com.ofir.ofirapp.services.DatabaseService;
import com.ofir.ofirapp.utils.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;

public class OwnedEventAdapter extends RecyclerView.Adapter<OwnedEventAdapter.EventViewHolder> {
    private final Context context;
    private final List<Event> events;
    private final DatabaseService databaseService;
    private final User currentUser;

    public OwnedEventAdapter(Context context) {
        this.context = context;
        this.events = new ArrayList<>();
        this.databaseService = DatabaseService.getInstance();
        this.currentUser = SharedPreferencesUtil.getUser(context);
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_owned_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.tvEventName.setText(event.getType());
        holder.tvEventDate.setText(event.getDate());
        holder.tvEventLocation.setText(String.format("%s, %s", event.getVenue(), event.getCity()));

        // Only show delete button if user owns the event
        if (currentUser != null && event.getOwnerId() != null && 
            event.getOwnerId().equals(currentUser.getId())) {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> deleteEvent(event, position));
        } else {
            holder.btnDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void setEvents(List<Event> events) {
        this.events.clear();
        if (events != null) {
            this.events.addAll(events);
        }
        notifyDataSetChanged();
    }

    public void clearItems() {
        events.clear();
        notifyDataSetChanged();
    }

    private void deleteEvent(Event event, int position) {
        if (event == null || event.getId() == null) {
            Toast.makeText(context, "Cannot delete event: Invalid event data", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if current user is the owner
        if (currentUser == null || !currentUser.getId().equals(event.getOwnerId())) {
            Toast.makeText(context, "You can only delete events that you own", Toast.LENGTH_SHORT).show();
            return;
        }

        // Delete the event from Firebase
        databaseService.removeEvent(event.getId(), new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void result) {
                // Remove from local list and update UI
                events.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, events.size());
                Toast.makeText(context, "Event deleted successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(context, "Failed to delete event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName;
        TextView tvEventDate;
        TextView tvEventLocation;
        ImageButton btnDelete;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvEventLocation = itemView.findViewById(R.id.tvEventLocation);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
} 