package com.ofir.ofirapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import com.ofir.ofirapp.R;
import com.ofir.ofirapp.models.User;

import java.util.List;



public class UserAdapter <P> extends ArrayAdapter<User> {
    Context context;
    List<User> objects;

    public  UserAdapter(Context context, int resource, int textViewResourceId, List<User> objects) {
        super(context, resource, textViewResourceId, objects);

        this.context=context;
        this.objects=objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater layoutInflater = ((Activity)context).getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.userrow, parent, false);

        TextView tvfname = (TextView)view.findViewById(R.id.tvFname);
        TextView tvlname = (TextView)view.findViewById(R.id.tvLname);
        TextView tvphone = (TextView)view.findViewById(R.id.tvPhone);


        User temp = objects.get(position);
        tvfname.setText(temp.getFname()+"");
        tvlname.setText(temp.getLname()+"");
        tvphone.setText(temp.getPhone()+"");
        return view;
    }

    private View getInflate(ViewGroup parent, LayoutInflater layoutInflater) {
        return layoutInflater.inflate(R.layout.userrow, parent, false);
    }
}