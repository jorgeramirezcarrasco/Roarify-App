package com.polimi.jaj.roarify.fragments;

import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.polimi.jaj.roarify.R;
import com.polimi.jaj.roarify.adapter.CustomAdapter;
import com.polimi.jaj.roarify.data.RoarifyCursor;
import com.polimi.jaj.roarify.data.RoarifyDBContract.*;
import com.polimi.jaj.roarify.data.RoarifySQLiteRepository;
import com.polimi.jaj.roarify.model.Message;

import java.util.ArrayList;
import java.util.List;

import static com.polimi.jaj.roarify.activities.HomeActivity.db;


public class FavoritesFragment extends Fragment {
    /* Parameters needed for the dialog fragments */
    private View dialogViewReply;
    private LayoutInflater inflaterReply;
    private AlertDialog.Builder builderReply;
    private AlertDialog alertReply;
    private View dialogViewMessage;
    private LayoutInflater inflaterMessage;
    private AlertDialog.Builder builderMessage;
    private AlertDialog alertMessage;
    private SwipeRefreshLayout swipeContainer;
    private List<Message> favoriteMessages = new ArrayList<Message>();;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertMessage.show();
            }
        });


        /*Layout Setup*/
        inflaterReply = getLayoutInflater(savedInstanceState);




        /* Initial setup of the dialog fragment when clicking on a message */
        builderReply = new AlertDialog.Builder((getActivity()));
        builderReply.setPositiveButton("Reply", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            /* Method that sends the response to the server or the user that wrote this message (not clear yet).
             * In case of sending the message to the server, it would also be a multicast message. In case of
             * sending the message only to the user, the setup of this button should be done in the LoadMessages
             * method, as we need to know the user who sent the first message; moreover, we need to differentiate
             * between a normal user and an anonymous user. If the user is anonymous, this button shouldn't be
             * shown, or shouldn't be clickable (color it grey). */
            }
        });
        builderReply.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        /* Setup of the dialog fragment when clicking on the '+' button */
        builderMessage = new AlertDialog.Builder(getActivity());
        builderMessage.setPositiveButton("Roar!", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        builderMessage.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        inflaterMessage = getActivity().getLayoutInflater();
        dialogViewMessage = inflaterMessage.inflate(R.layout.message_dialog, null);
        builderMessage.setView(dialogViewMessage);
        builderMessage.setTitle("New message");
        alertMessage = builderMessage.create();

        // DATABASE OPERATIONS
        /*
        RoarifySQLiteRepository db = new RoarifySQLiteRepository(getContext());
        //SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Message messageExample = new Message("12345","54321","Albert","Hola que tal, probando","11:47 PM",17.8,9.98,null,null,null);
        Message messageExample2 = new Message("12346","54321","Albert","Esta es la segunda prueba","1:21 AM",17.8,9.98,null,null,null);
        Message messageExample3 = new Message("12347","54322","Miguel","Esta es la prueba numero 3","8:24 AM",18.0,9.8,null,null,null);
        db.add(messageExample);
        db.add(messageExample2);
        db.add(messageExample3);
        RoarifyCursor cursorExample = db.findAll();
        System.out.println("HOLA  "+cursorExample.getPosition());
        System.out.println("HOLA  "+cursorExample.getCount());
        cursorExample = db.findById("12346");
        cursorExample.moveToFirst();
        System.out.println("HOLA  "+cursorExample.getPosition());
        System.out.println("HOLA  "+cursorExample.getMessage());
        //cursorExample.moveToNext();
        cursorExample.moveToFirst();
        System.out.println("HOLA  "+cursorExample.getPosition());
        System.out.println("HOLA  "+cursorExample.getMessage());
        */

        RoarifyCursor cursorExample = db.findAll();


        while(cursorExample.moveToNext()){
            Message iMessage = new Message();
            iMessage.setUserName(cursorExample.getUserName());
            iMessage.setText(cursorExample.getMessage());
            iMessage.setTime(cursorExample.getTime());
            iMessage.setDistance("200"); //TESTING

            favoriteMessages.add(iMessage);
        }
        LoadMessages(favoriteMessages);

    }

    public void LoadMessages(final List<Message> dataMessages){

        ListView favorites = (ListView) getActivity().findViewById(R.id.favorites);
        CustomAdapter customAdapter = new CustomAdapter(getActivity(), R.layout.row, dataMessages);
        favorites.setAdapter(customAdapter);

        favorites.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub

                dialogViewReply = inflaterReply.inflate(R.layout.reply_dialog, null);
                builderReply.setView(dialogViewReply);
                builderReply.setTitle(dataMessages.get(position).getUserName());
                builderReply.setMessage(dataMessages.get(position).getText()).setCancelable(false);
                alertReply = builderReply.create();
                alertReply.show();
            }
        });
    }

}