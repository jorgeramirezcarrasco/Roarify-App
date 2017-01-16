package com.polimi.jaj.roarify.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Profile;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.polimi.jaj.roarify.R;
import com.polimi.jaj.roarify.adapter.CustomAdapter;
import com.polimi.jaj.roarify.fragments.HomeFragment;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MessageActivity extends AppCompatActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener{



    /* Google Maps parameters */
    private GoogleApiClient mGoogleApiClient;
    private String mLastUpdateTime;
    private boolean mRequestingLocationUpdates;
    private LocationRequest mLocationRequest;
    private GoogleMap map;
    static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;
    private LatLng messageLocation;
    private Location mLastLocation;
    private LatLng myLocation;


    /* Parameters needed for the dialog fragments */
    private View dialogViewReply;
    private LayoutInflater inflaterReply;
    private AlertDialog.Builder builderReply;
    private AlertDialog alertReply;
    private View dialogViewMessage;
    private LayoutInflater inflaterMessage;
    private AlertDialog.Builder builderMessage;
    private AlertDialog alertMessage;
    String textPost;

    /* Server Connection parameters */
    private String idMessage;
    List<Message> dataMessages = new ArrayList<Message>();

    private SwipeRefreshLayout swipeContainer;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


         /* Google Api Client Connection */
        buildGoogleApiClient();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertMessage.show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        Intent mIntent = getIntent();
        idMessage = (String) mIntent.getExtras().getSerializable("idMessage");
        Log.i("idMessage",idMessage);

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);

        new MessageActivity.GetMyMessage().execute();
        new MessageActivity.GetChildrenMessages().execute();

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                new MessageActivity.GetMyMessage().execute();
                new MessageActivity.GetChildrenMessages().execute();

            }
        });


        ListView comments = (ListView) findViewById(R.id.comments);
        CustomAdapter customAdapter = new CustomAdapter(this, R.layout.row, dataMessages);
        comments.setAdapter(customAdapter);

                /* Setup of the dialog fragment when clicking on the '+' button */
        builderMessage = new AlertDialog.Builder(this);
        builderMessage.setPositiveButton("Roar!", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                EditText editText = (EditText) dialogViewMessage.findViewById(R.id.new_message);
                textPost = editText.getText().toString();
                new MessageActivity.PostMessage().execute();
                ((EditText) dialogViewMessage.findViewById(R.id.new_message)).setText("");
            }
        });
        builderMessage.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        inflaterMessage = this.getLayoutInflater();
        dialogViewMessage = inflaterMessage.inflate(R.layout.message_dialog, null);
        builderMessage.setView(dialogViewMessage);
        builderMessage.setTitle("Reply");
        alertMessage = builderMessage.create();

    }


    /**
     * Google Maps methods
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        // Check permission about location before enable location button on map
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);

        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public void drawMarker(LatLng myLocation) {
        new GetMyMessage().execute();//When location is ready obtain messages
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 10));
        map.addMarker(new MarkerOptions().position(myLocation).title("My position"));
    }


    public void LoadMessages(final List<Message> dataMessages) {

        ListView comments = (ListView) this.findViewById(R.id.comments);
        CustomAdapter customAdapter = new CustomAdapter(this, R.layout.row, dataMessages);
        comments.setAdapter(customAdapter);

    }


    private class GetMyMessage extends AsyncTask<Void, Message, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();

            //idMessage="5732568548769792"; //Este valor debe enviarse en el intent del message que se haya tocado


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

            dataMessages.clear();
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
        }

        @Override
        protected void onProgressUpdate(Message... values) {
            // TODO Auto-generated method stub
            if (values == null) {

            } else {
                Message message = new Message(values[0].getMessageId(), values[0].getUserId(), values[0].getUserName(), values[0].getText(), values[0].getTime(), values[0].getLatitude(), values[0].getLongitude(),values[0].getIsParent(),values[0].getParentId());
                dataMessages.add(message);
                LoadMessages(dataMessages);

            }
        }

    }

    private class GetChildrenMessages extends AsyncTask<Void, Message, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();

            //idMessage="5634472569470976"; //Este valor debe enviarse en el intent del message que se haya tocado


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

            dataMessages.clear();
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
        }

        @Override
        protected void onProgressUpdate(Message... values) {
            // TODO Auto-generated method stub
            if (values == null) {

            } else {
                Message message = new Message(values[0].getMessageId(), values[0].getUserId(), values[0].getUserName(), values[0].getText(), values[0].getTime(), values[0].getLatitude(), values[0].getLongitude(),values[0].getIsParent(),values[0].getParentId());
                dataMessages.add(message);
                LoadMessages(dataMessages);

            }
        }

    }

    /**
     * Server Connection methods
     */

    private class PostMessage extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {

            HttpPost post = new HttpPost("https://1-dot-roarify-server.appspot.com/postMessage");
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();

            pairs.add(new BasicNameValuePair("userId", Profile.getCurrentProfile().getId()));
            pairs.add(new BasicNameValuePair("userName", Profile.getCurrentProfile().getName()));
            pairs.add(new BasicNameValuePair("time", mLastUpdateTime.toString()));
            pairs.add(new BasicNameValuePair("text", textPost));
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
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
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
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        myLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        drawMarker(myLocation);
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


}
