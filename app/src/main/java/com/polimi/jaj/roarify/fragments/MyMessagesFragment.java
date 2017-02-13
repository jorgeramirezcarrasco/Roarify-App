package com.polimi.jaj.roarify.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
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
import com.polimi.jaj.roarify.activities.MessageActivity;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.polimi.jaj.roarify.activities.HomeActivity.db;


/**
 * Created by jorgeramirezcarrasco on 4/1/17.
 */

public class MyMessagesFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {


    /* Google Maps parameters */
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private String mLastUpdateTime;
    private boolean mRequestingLocationUpdates;
    private LocationRequest mLocationRequest;
    private LatLng myLocation;
    private Integer distance;
    private Location locationMessage;

    /* Server Connection parameters */
    List<Message> dataMessages = new ArrayList<Message>();
    DateFormat format = new SimpleDateFormat("d MMM yyyy HH:mm:ss", Locale.ENGLISH);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_mymessages, container, false);

        return view;
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        /* Google Api Client Connection */
        buildGoogleApiClient();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

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

            dataMessages.clear();
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
                locationMessage = new Location("Roarify");
                message.setDistance(getDistanceToMessage(locationMessage, message).toString());
                dataMessages.add(message);
                LoadMessages(dataMessages);

            }
        }

    }

    public void LoadMessages(final List<Message> dataMessages){

        Collections.sort(dataMessages, new Comparator<Message>() {
            @Override
            public int compare(Message o1, Message o2) {
                return Integer.valueOf(o1.getDistance()).compareTo(Integer.valueOf(o2.getDistance()));
            }
        });

        ListView myMessages = (ListView) getActivity().findViewById(R.id.mymessages);
        CustomAdapter customAdapter = new CustomAdapter(getActivity(), R.layout.row, dataMessages);
        myMessages.setAdapter(customAdapter);

        myMessages.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Message message = (Message) parent.getItemAtPosition(position);

                Intent mIntent = new Intent(getActivity() ,MessageActivity.class);
                mIntent.putExtra("idMessage", message.getMessageId());
                mIntent.putExtra("currentLat",mLastLocation.getLatitude());
                mIntent.putExtra("currentLon",mLastLocation.getLongitude());
                startActivity(mIntent);
            }
        });
    }

    /**
     * Google Play Services Methods
     */

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
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
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
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
            LoadMessages(dataMessages);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(getActivity(), "Connection suspended", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getActivity(), "Failed to connect...", Toast.LENGTH_SHORT).show();
    }


    protected void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
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
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
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