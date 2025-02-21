package com.ofir.ofirapp.screens;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ofir.ofirapp.R;
import com.ofir.ofirapp.models.Event;
import java.util.ArrayList;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private ArrayList<Event> eventList;

    public EventAdapter(ArrayList<Event> eventList) {
        this.eventList = eventList;
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
        holder.eventType.setText(event.getType());
        holder.eventDate.setText(event.getDate());
        holder.eventVenue.setText(event.getVenue());
        holder.eventAddress.setText(event.getAddress());
        holder.eventCity.setText(event.getCity());
        holder.eventDresscode.setText("Dress Code: " + event.getDresscode());
        holder.eventMenuType.setText("Menu: " + event.getMenutype());
        holder.eventStatus.setText("Status: " + event.getStatus());
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventType, eventDate, eventVenue, eventAddress, eventCity, eventDresscode, eventMenuType, eventStatus;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventType = itemView.findViewById(R.id.eventType);
            eventDate = itemView.findViewById(R.id.eventDate);
            eventVenue = itemView.findViewById(R.id.eventVenue);
            eventAddress = itemView.findViewById(R.id.eventAddress);
            eventCity = itemView.findViewById(R.id.eventCity);
            eventDresscode = itemView.findViewById(R.id.eventDresscode);
            eventMenuType = itemView.findViewById(R.id.eventMenuType);
            eventStatus = itemView.findViewById(R.id.eventStatus);
        }
    }
}

