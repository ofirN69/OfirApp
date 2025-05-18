package com.ofir.ofirapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ofir.ofirapp.R;
import com.ofir.ofirapp.models.User;
import com.ofir.ofirapp.screens.addEVENT;

import java.util.List;

public class UserNamAdapter extends ArrayAdapter<User> {
    private final Context context;
    private final List<User> users;
    private final addEVENT activity;

    public UserNamAdapter(Context context, int resource, int textViewResourceId, List<User> users) {
        super(context, resource, textViewResourceId, users);
        this.context = context;
        this.users = users;
        this.activity = (addEVENT) context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        User user = users.get(position);
        TextView textView = view.findViewById(android.R.id.text1);
        textView.setText(user.getFname() + " " + user.getLname());

        // Check if user is already selected
        if (activity.isUserAlreadySelected(user)) {
            view.setBackgroundColor(Color.DKGRAY);
            view.setAlpha(0.5f);
            view.setEnabled(false);
        } else {
            view.setBackgroundColor(Color.TRANSPARENT);
            view.setAlpha(1.0f);
            view.setEnabled(true);
        }

        return view;
    }
}
