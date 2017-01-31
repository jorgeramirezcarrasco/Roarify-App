package com.polimi.jaj.roarify.fragments;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;


import com.facebook.Profile;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.polimi.jaj.roarify.R;
import com.polimi.jaj.roarify.adapter.CustomAdapter;
import com.polimi.jaj.roarify.data.RoarifyCursor;
import com.polimi.jaj.roarify.model.Message;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static com.polimi.jaj.roarify.activities.HomeActivity.db;


/**
 * Created by jorgeramirezcarrasco on 4/1/17.
 */

public class MyMessagesFragment extends Fragment {


    /* Parameters needed for the dialog fragments */
    private View dialogViewReply;
    private LayoutInflater inflaterReply;
    private AlertDialog.Builder builderReply;
    private AlertDialog alertReply;

    /* Server Connection parameters */
    private Double lat;
    private Double lon;
    private String user_ID;
    List<Message> dataMessages = new ArrayList<Message>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_mymessages, container, false);

        return view;
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        /*Layout Setup*/
        inflaterReply = getLayoutInflater(savedInstanceState);

        /* Initial setup of the dialog fragment when clicking on a message */
        builderReply = new AlertDialog.Builder((getActivity()));
        builderReply.setPositiveButton("Reply", new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) {
            /* Method that sends the response to the server or the user that wrote this message (not clear yet).
             * In case of sending the message to the server, it would also be a multicast message. In case of
             * sending the message only to the user, the setup of this button should be done in the LoadMessages
             * method, as we need to know the user who sent the first message; moreover, we need to differentiate
             * between a normal user and an anonymous user. If the user is anonymous, this button shouldn't be
             * shown, or shouldn't be clickable (color it grey). */ }
        });
        builderReply.setNegativeButton("Back", new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) {
            dialog.cancel(); }
        });

        new GetUserMessages().execute();


    }

    /**
     * Server Connection methods
     */

    private class GetUserMessages extends AsyncTask<Void, Message, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            /*while (!isCancelled()) {*/
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();


            pairs.add(new BasicNameValuePair("userId", "" + Profile.getCurrentProfile().getId().toString()));

            String paramsString = URLEncodedUtils.format(pairs, "UTF-8");
            HttpGet get = new HttpGet("http://1-dot-roarify-server.appspot.com/getUserMessages" + "?" + paramsString);

            try {
                HttpClient client = new DefaultHttpClient();
                HttpResponse response = client.execute(get);
                HttpEntity entity = response.getEntity();
                BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), "iso-8859-1"), 8);

                String jsonResponse = reader.readLine();

                Gson gson = new Gson();
                TypeToken<List<Message>> token = new TypeToken<List<Message>>() {
                };
                List<Message> messagesList = gson.fromJson(jsonResponse, token.getType());
                if (messagesList != null) {
                    publishProgress(null);
                    for (Message a : messagesList) {
                        publishProgress(a);
                    }

                }

            } catch (ClientProtocolException e) {
                e.printStackTrace();
                showToastedWarning();
            } catch (IOException e) {
                e.printStackTrace();
                showToastedWarning();
            }
                /*try {
                    Thread.sleep(5000);*/
            dataMessages.clear();
                /*} catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
            /*}*/
            return null;
        }

        private void showToastedWarning() {
            try {
                (getActivity()).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText((getActivity()), R.string.load_messages_fail, Toast.LENGTH_SHORT).show();
                    }
                });
                Thread.sleep(300);
            } catch (InterruptedException e2) {
                e2.printStackTrace();
            }
        }



        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onPostExecute(Boolean result) {

        }

        @Override
        protected void onProgressUpdate(Message... values) {
            // TODO Auto-generated method stub
            if (values == null) {

            } else {
                Message message = new Message(values[0].getMessageId(),values[0].getUserId(),values[0].getUserName(),values[0].getText(),values[0].getTime(),values[0].getLatitude(),values[0].getLongitude(),values[0].getIsParent(),values[0].getParentId(), null);
                dataMessages.add(message);
                LoadMessages(dataMessages);

            }
        }

    }

    public void LoadMessages(final List<Message> dataMessages){

        ListView myMessages = (ListView) getActivity().findViewById(R.id.mymessages);
        CustomAdapter customAdapter = new CustomAdapter(getActivity(), R.layout.row, dataMessages);
        myMessages.setAdapter(customAdapter);

        myMessages.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub

                dialogViewReply = inflaterReply.inflate(R.layout.reply_dialog, null);
                builderReply.setView(dialogViewReply);
                builderReply.setTitle(dataMessages.get(position).getUserName());
                builderReply.setMessage(dataMessages.get(position).getText()).setCancelable(false);

                final Message message = (Message) parent.getItemAtPosition(position);
                CheckBox favCheckBox = (CheckBox) dialogViewReply.findViewById(R.id.checkbox_favorite);
                if (db.findById(message.getMessageId()).moveToNext()){
                    favCheckBox.setChecked(true);
                }
                favCheckBox.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        System.out.println("DENTRO DE PINCHAR EL CHECKBOX");
                        boolean checked = ((CheckBox) view).isChecked();
                        if (checked){
                            db.add(message);
                            System.out.println("SI esta checkada");
                        }
                        else {
                            db.delete(message);
                            System.out.println("NO esta checkada");
                        }
                    }
                });

                alertReply = builderReply.create();
                alertReply.show();
            }
        });
    }


}