package com.polimi.jaj.roarify.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.Profile;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.polimi.jaj.roarify.R;
import com.polimi.jaj.roarify.adapter.CustomAdapter;
import com.polimi.jaj.roarify.fragments.CardViewFragment;
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


/**
 * Created by jorgeramirezcarrasco on 13/2/17.
 */

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    /* Google Maps parameters */
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private String mLastUpdateTime;
    private boolean mRequestingLocationUpdates;
    private LocationRequest mLocationRequest;
    private GoogleMap map;
    static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;
    private LatLng myLocation;
    private Integer distance;
    private Location locationMessage;

    private String idMessage;
    private LatLng messageLocation;
    Message m = new Message();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
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


        /* GMap Setup */
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

         /* Intent Receiver */
        Intent mIntent = getIntent();
        idMessage = (String) mIntent.getExtras().getSerializable("idMessage");
        messageLocation = new LatLng((Double) mIntent.getExtras().getSerializable("LatitudeMessage"),(Double) mIntent.getExtras().getSerializable("LongitudeMessage"));

        new GetMyMessage().execute();



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
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick ( final Marker marker){
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("google.navigation:q="+m.getLatitude()+","+m.getLongitude()));
                startActivity(intent);
                return true;
            }
        });
    }

    public void drawMarker(LatLng myLocation) {
        map.clear();
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 50));
       //Draw
        map.addMarker(new MarkerOptions().position(new LatLng(m.getLatitude(), m.getLongitude())).title(m.getText()).snippet(m.getUserName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))).setTag(m.getMessageId());

    }

    /**
     * Pack Manager Method that manipulates the request permission result
     */


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        map.setMyLocationEnabled(true);
                    }

                } else {

                }
                return;
            }
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
             /* Allows Location Updates */
            mRequestingLocationUpdates = true;
            mLocationRequest = new LocationRequest();
            if (mRequestingLocationUpdates) {
                startLocationUpdates();
            }
        }
        if (mLastLocation != null) {
            /* Convert Location */
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

    /* Method that start the location updates */
    protected void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }

    }

    /* Method that is called when Location is changed */
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
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
                        Toast.makeText(MapActivity.this, R.string.load_messages_fail, Toast.LENGTH_SHORT).show();
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

            drawMarker(messageLocation);

        }

        @Override
        protected void onProgressUpdate(Message... values) {
            // TODO Auto-generated method stub
            if (values == null) {

            } else {
                Message message = new Message(values[0].getMessageId(), values[0].getUserId(), values[0].getUserName(), values[0].getText(), values[0].getTime(), values[0].getLatitude(), values[0].getLongitude(),values[0].getIsParent(),values[0].getParentId(), null);
                m=message;


            }
        }

    }


}