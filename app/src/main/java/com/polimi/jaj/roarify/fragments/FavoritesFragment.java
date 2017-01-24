package com.polimi.jaj.roarify.fragments;

import android.content.DialogInterface;
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
import com.polimi.jaj.roarify.model.Message;


import java.util.List;


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

    }

    public void LoadMessages(final List<Message> dataMessages){

        ListView comments = (ListView) getActivity().findViewById(R.id.comments);
        CustomAdapter customAdapter = new CustomAdapter(getActivity(), R.layout.row, dataMessages);
        comments.setAdapter(customAdapter);

        comments.setOnItemClickListener(new AdapterView.OnItemClickListener()
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