package com.polimi.jaj.roarify.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Profile;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.polimi.jaj.roarify.R;
import com.polimi.jaj.roarify.adapter.MessageAdapter;
import com.polimi.jaj.roarify.fragments.CardViewFragment;
import com.polimi.jaj.roarify.model.Message;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.polimi.jaj.roarify.activities.HomeActivity.db;

public class MessageActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener{



    /* Google Maps parameters */
    private GoogleApiClient mGoogleApiClient;
    private String mLastUpdateTime;
    private boolean mRequestingLocationUpdates;
    private LocationRequest mLocationRequest;
    private LatLng messageLocation;
    private Location mLastLocation;
    private LatLng myLocation;
    private Integer distance;
    private Location locationMessage;

    /* Parameters needed for the dialog fragments */
    private View dialogViewReply;
    private LayoutInflater inflaterReply;
    private AlertDialog.Builder builderReply;
    private AlertDialog alertReply;
    private AlertDialog.Builder builderDelete;
    private AlertDialog alertDelete;
    String textPost;

    /* Server Connection parameters */
    private String idMessage;
    Message dataMessage = new Message();
    List<Message> dataMessages = new ArrayList<Message>();
    private static final String ORIGINAL
            = "ÁáÉéÍíÓóÚúÑñÜü";
    private static final String REPLACEMENT
            = "AaEeIiOoUuNnUu";
    DateFormat format = new SimpleDateFormat("d MMM yyyy HH:mm:ss", Locale.ENGLISH);

    private SwipeRefreshLayout swipeContainer;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, CardViewFragment.newInstance())
                    .commit();
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });



         /* Google Api Client Connection */
        buildGoogleApiClient();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

        /* Intent Receiver */
        Intent mIntent = getIntent();
        idMessage = (String) mIntent.getExtras().getSerializable("idMessage");
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);

        new MessageActivity.GetMyMessage().execute();


        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                new MessageActivity.GetChildrenMessages().execute();

            }
        });
        swipeContainer.setColorSchemeResources(R.color.colorPrimary);


        inflaterReply = getLayoutInflater();

        builderReply = new AlertDialog.Builder(this);
        builderReply.setPositiveButton("Reply", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                EditText editText = (EditText) dialogViewReply.findViewById(R.id.response);
                textPost = editText.getText().toString();
                new PostMessage().execute();
                ((EditText) dialogViewReply.findViewById(R.id.response)).setText("");
            }
        });
        builderReply.setNegativeButton("Back", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        builderDelete = new AlertDialog.Builder(this);
        builderDelete.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                new DeleteMessage().execute();
            }
        });
        builderDelete.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

    }

    public void LoadCardItems(final Message dataMessage) {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Message from "+dataMessage.getUserName().split("\\s+")[0]);

        final ImageButton fav = (ImageButton) this.findViewById(R.id.favButton);
        if(db.findById(dataMessage.getMessageId()).moveToNext()) {
            fav.setImageResource(R.drawable.star_fav_yes);
        }

        TextView text = (TextView) this.findViewById(R.id.textMessage);
        text.setText(dataMessage.getText());
        text.setMovementMethod(new ScrollingMovementMethod());

        TextView user = (TextView) this.findViewById(R.id.authorMessage);
        user.setText("From: "+dataMessage.getUserName());

        TextView distance = (TextView) this.findViewById(R.id.distanceMessage);
        distance.setText(dataMessage.getDistance()+"m from you");

        TextView time = (TextView) this.findViewById(R.id.timeMessage);
        String[] s = dataMessage.getTime().split("\\s");
        time.setText("At "+s[0] + ' ' + s[1] + ' ' + s[2] + '\n' + s[3]);

        ImageButton reply = (ImageButton) this.findViewById(R.id.replyButton);
        reply.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                dialogViewReply = inflaterReply.inflate(R.layout.reply_dialog, null);
                builderReply.setView(dialogViewReply);
                builderReply.setTitle(dataMessage.getUserName());
                builderReply.setMessage(dataMessage.getText()).setCancelable(false);

                alertReply = builderReply.create();
                alertReply.show();

            }
        });
        //Check if the user logged is the author
        if(Double.valueOf(dataMessage.getUserId().toString()).equals(Double.valueOf(Profile.getCurrentProfile().getId().toString()))){
        ImageButton delete = (ImageButton) this.findViewById(R.id.deleteButton);
            delete.setVisibility(View.VISIBLE);
        delete.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                builderDelete.setTitle("Delete Message");
                builderDelete.setMessage("Are you sure that you want to delete the message?").setCancelable(false);

                alertDelete= builderDelete.create();
                alertDelete.show();

            }
        });
        }

        fav.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (db.findById(dataMessage.getMessageId()).moveToNext()){
                    db.delete(dataMessage);
                    fav.setImageResource(R.drawable.star_fav_no);
                    Toast.makeText(MessageActivity.this, R.string.favorite_deleted, Toast.LENGTH_SHORT).show();
                }
                else {
                    db.add(dataMessage);
                    fav.setImageResource(R.drawable.star_fav_yes);
                    Toast.makeText(MessageActivity.this, R.string.favorite_added, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void LoadMessages(final List<Message> dataMessages) {

        Collections.sort(dataMessages, new Comparator<Message>() {
            @Override
            public int compare(Message o1, Message o2) {
                return Integer.valueOf(o1.getDistance()).compareTo(Integer.valueOf(o2.getDistance()));
            }
        });

        ListView comments = (ListView) this.findViewById(R.id.comments);
        comments.setAdapter(null);
        MessageAdapter messageAdapter = new MessageAdapter(this, R.layout.row, dataMessages);
        comments.setAdapter(messageAdapter);
        comments.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Message message = (Message) parent.getItemAtPosition(position);

                Intent mIntent = new Intent(getApplicationContext(), MessageActivity.class);
                mIntent.putExtra("idMessage", message.getMessageId());
                startActivity(mIntent);

            }
        });

    }
    /**
     * Server Connection methods
     */

    private class GetMyMessage extends AsyncTask<Void, Message, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();

            pairs.add(new BasicNameValuePair("id", "" + idMessage));


            String paramsString = URLEncodedUtils.format(pairs, "UTF-8");
            HttpGet get = new HttpGet("http://1-dot-roarify-server.appspot.com/getMessage" + "?" + paramsString);

            try {
                HttpClient client = new DefaultHttpClient();
                HttpResponse response = client.execute(get);
                HttpEntity entity = response.getEntity();
                BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), "iso-8859-1"), 8);

                String jsonResponse = reader.readLine();

                Gson gson = new Gson();
                TypeToken<Message> token = new TypeToken<Message>() {
                };
                Message messageReceived = gson.fromJson(jsonResponse, token.getType());
                if (messageReceived != null) {
                    publishProgress(messageReceived);

                }

            } catch (ClientProtocolException e) {
                e.printStackTrace();
                showToastedWarning();
            } catch (IOException e) {
                e.printStackTrace();
                showToastedWarning();
            }
            return null;
        }

        private void showToastedWarning() {
            try {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(MessageActivity.this, R.string.load_messages_fail, Toast.LENGTH_SHORT).show();
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

            swipeContainer.setRefreshing(false);
            new MessageActivity.GetChildrenMessages().execute();
            LoadCardItems(dataMessage);
        }

        @Override
        protected void onProgressUpdate(Message... values) {
            // TODO Auto-generated method stub
            if (values == null) {

            } else {
                Message message = new Message(values[0].getMessageId(), values[0].getUserId(), values[0].getUserName(), values[0].getText(), values[0].getTime(), values[0].getLatitude(), values[0].getLongitude(),values[0].getIsParent(),values[0].getParentId(), null);
                locationMessage = new Location("Roarify");
                message.setDistance(getDistanceToMessage(locationMessage, message).toString());
                dataMessage=message;


            }
        }

    }

    private class GetChildrenMessages extends AsyncTask<Void, Message, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();


            pairs.add(new BasicNameValuePair("id", "" + idMessage));


            String paramsString = URLEncodedUtils.format(pairs, "UTF-8");
            HttpGet get = new HttpGet("http://1-dot-roarify-server.appspot.com/getChildrenMessages" + "?" + paramsString);

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

            return null;
        }

        private void showToastedWarning() {
            try {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(MessageActivity.this, R.string.load_messages_fail, Toast.LENGTH_SHORT).show();
                    }
                });
                Thread.sleep(300);
            } catch (InterruptedException e2) {
                e2.printStackTrace();
            }
        }


        @Override
        protected void onPreExecute() {
            dataMessages.clear();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            swipeContainer.setRefreshing(false);
            LoadMessages(dataMessages);

        }

        @Override
        protected void onProgressUpdate(Message... values) {
            // TODO Auto-generated method stub
            if (values == null) {

            } else {
                Message message = new Message(values[0].getMessageId(), values[0].getUserId(), values[0].getUserName(), values[0].getText(), values[0].getTime(), values[0].getLatitude(), values[0].getLongitude(),values[0].getIsParent(),values[0].getParentId(), null);
                locationMessage = new Location("Roarify");
                message.setDistance(getDistanceToMessage(locationMessage, message).toString());
                dataMessages.add(message);
                LoadMessages(dataMessages);
            }
        }

    }



    private class PostMessage extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {

            HttpPost post = new HttpPost("https://1-dot-roarify-server.appspot.com/postMessage");
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();


            pairs.add(new BasicNameValuePair("userId", Profile.getCurrentProfile().getId()));

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            boolean anonymPref = sharedPref.getBoolean(SettingsActivity.KEY_PREF_ANONYM,false);
            if (anonymPref) {
                pairs.add(new BasicNameValuePair("userName", "Anoymous"));
            }
            else {
                pairs.add(new BasicNameValuePair("userName", stripAccents(Profile.getCurrentProfile().getName())));
            }

            pairs.add(new BasicNameValuePair("time", mLastUpdateTime.toString()));
            pairs.add(new BasicNameValuePair("text", stripAccents(textPost)));
            pairs.add(new BasicNameValuePair("lat", String.valueOf(mLastLocation.getLatitude())));
            pairs.add(new BasicNameValuePair("long", String.valueOf(mLastLocation.getLongitude())));
            pairs.add(new BasicNameValuePair("isParent", "false"));
            pairs.add(new BasicNameValuePair("parentId", idMessage));

            try {
                post.setEntity(new UrlEncodedFormEntity(pairs));
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                HttpClient client = new DefaultHttpClient();
                HttpResponse response = client.execute(post);
                if (response.getStatusLine().getStatusCode() == 200) {
                    return true;
                } else {
                    return false;
                }
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }

        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(Boolean result) {
            new GetChildrenMessages().execute();
        }

    }
    public static String stripAccents(String str) {
        if (str == null) {
            return null;
        }
        char[] array = str.toCharArray();
        for (int index = 0; index < array.length; index++) {
            int pos = ORIGINAL.indexOf(array[index]);
            if (pos > -1) {
                array[index] = REPLACEMENT.charAt(pos);
            }
        }
        return new String(array);
    }


    private class DeleteMessage extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {

            HttpPost post = new HttpPost("https://1-dot-roarify-server.appspot.com/deleteMessage");
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();

            pairs.add(new BasicNameValuePair("id", "" + idMessage));


            try {
                post.setEntity(new UrlEncodedFormEntity(pairs));
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                HttpClient client = new DefaultHttpClient();
                HttpResponse response = client.execute(post);
                if (response.getStatusLine().getStatusCode() == 200) {
                    return true;
                } else {
                    return false;
                }
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }

        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(Boolean result) {
            finish();
        }

    }


    /**
     * Google Play Services Methods
     */

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    @Override
    public void onConnected(Bundle connectionHint) {
        /* When is connected check permissions with the Package Manager */
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            /* Obtain the last Location */
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            /* Obtain the last date */
            mLastUpdateTime = format.format(new Date());
             /* Allows Location Updates */
            mRequestingLocationUpdates = true;
            mLocationRequest = new LocationRequest();
            if (mRequestingLocationUpdates) {
                startLocationUpdates();
            }
        }
         /* If all the process was right draw the marker */
        if (mLastLocation != null) {
            /* Convert Location and call drawMarker method */
            myLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Connection suspended", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Failed to connect...", Toast.LENGTH_SHORT).show();
    }


    protected void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }


    }


    /* Method that is called when Location is changed */
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        mLastUpdateTime = format.format(new Date());
        myLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    public Integer getDistanceToMessage(Location locationMessage, Message message){
        locationMessage.setLatitude(message.getLatitude());
        locationMessage.setLongitude(message.getLongitude());
        distance = new Integer(0);
        distance = Math.round(mLastLocation.distanceTo(locationMessage));
        return distance;
    }

}
