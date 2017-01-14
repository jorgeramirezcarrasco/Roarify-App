package com.polimi.jaj.roarify.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.polimi.jaj.roarify.R;
import com.polimi.jaj.roarify.adapter.CustomAdapter;
import com.polimi.jaj.roarify.model.Message;

import java.util.List;

public class MessageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        Intent mIntent = getIntent();
        Message message = (Message) mIntent.getExtras().getSerializable("message");

        TextView textMessage = (TextView) findViewById(R.id.textMessage);
        textMessage.setText(message.getText());

        //ListView comments = (ListView) findViewById(R.id.comments);


    }

    /*
    public void LoadMessages(final List<Message> dataMessages) {

        ListView comments = (ListView) this.findViewById(R.id.comments);
        CustomAdapter customAdapter = new CustomAdapter(this, R.layout.row, dataMessages);
        comments.setAdapter(customAdapter);

    }*/







}
