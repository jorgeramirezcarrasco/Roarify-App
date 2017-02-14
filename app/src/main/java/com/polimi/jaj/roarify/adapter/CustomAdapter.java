package com.polimi.jaj.roarify.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.polimi.jaj.roarify.R;
import com.polimi.jaj.roarify.model.Message;

import java.util.List;

/**
 * Created by Alberto on 30/12/16.
 */

public class CustomAdapter extends ArrayAdapter<Message> {

    public CustomAdapter(Context context, int resource, List<Message> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

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
                String[] s = p.getTime().split("\\s");
                tt3.setText(s[0] + ' ' + s[1] + ' ' + s[2] + '\n' + s[3]);

            }

            if (tt4 != null) {
                tt4.setText(p.getDistance() + "m");//TESTING
            }
        }


        return v;
    }

}