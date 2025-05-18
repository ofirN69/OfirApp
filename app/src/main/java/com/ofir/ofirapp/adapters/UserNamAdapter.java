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
    private final boolean isSelectedList;

    public UserNamAdapter(Context context, int resource, int textViewResourceId, List<User> users, boolean isSelectedList) {
        super(context, resource, textViewResourceId, users);
        this.context = context;
        this.users = users;
        this.activity = (addEVENT) context;
        this.isSelectedList = isSelectedList;
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

        if (isSelectedList) {
            // In the selected list, show items normally
            view.setBackgroundColor(Color.TRANSPARENT);
            textView.setTextColor(Color.BLACK);
            view.setAlpha(1.0f);
            view.setEnabled(true);
        } else {
            // In the available list, darken selected items
            if (activity.isUserAlreadySelected(user)) {
                view.setBackgroundColor(Color.DKGRAY);
                textView.setTextColor(Color.WHITE);
                view.setAlpha(0.7f);
                view.setEnabled(false);
            } else {
                view.setBackgroundColor(Color.TRANSPARENT);
                textView.setTextColor(Color.BLACK);
                view.setAlpha(1.0f);
                view.setEnabled(true);
            }
        }

        return view;
    }
}
