package com.polimi.jaj.roarify.adapter;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.polimi.jaj.roarify.R;
import com.polimi.jaj.roarify.fragments.HomeFragment;
import com.polimi.jaj.roarify.model.Message;

import java.util.List;

/**
 * Created by Alberto on 30/12/16.
 */

public class CustomAdapter extends ArrayAdapter<Message> {

    public CustomAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public CustomAdapter(Context context, int resource, List<Message> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        final Float distance;
        final Location locationMessage;//to calculate the distance between our position and the message.



        Message p = getItem(position);

        if (p != null) {

            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.row, null);
            }

            TextView tt1 = (TextView) v.findViewById(R.id.author);
            TextView tt2 = (TextView) v.findViewById(R.id.message);
            TextView tt3 = (TextView) v.findViewById(R.id.time_sent);
            TextView tt4 = (TextView) v.findViewById(R.id.distance);

            if (tt1 != null) {
                tt1.setText(p.getUserName());
            }

            if (tt2 != null) {
                tt2.setText(p.getText());
            }

            if (tt3 != null) {
                tt3.setText(p.getTime());
            }

            if (tt4 != null) {

                tt4.setText(p.getDistance() + "m");//TESTING
            }
        }


        return v;
    }

}