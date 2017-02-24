package com.polimi.jaj.roarify.fragment;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.polimi.jaj.roarify.R;
import com.polimi.jaj.roarify.activity.MessageActivity;
import com.polimi.jaj.roarify.adapter.CustomAdapter;
import com.polimi.jaj.roarify.data.RoarifyCursor;
import com.polimi.jaj.roarify.model.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.polimi.jaj.roarify.activity.HomeActivity.db;


public class FavoritesFragment extends Fragment implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private List<Message> favoriteMessages = new ArrayList<Message>();;

    /* Google Maps parameters */
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private boolean mRequestingLocationUpdates;
    private LocationRequest mLocationRequest;
    private LatLng myLocation;
    private GoogleMap map;


    boolean isTablet;
    int orientation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        /* Google Api Client Connection */
        buildGoogleApiClient();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

        isTablet = getResources().getBoolean(R.bool.isTablet);
        orientation = getResources().getConfiguration().orientation;

        if (isTablet && orientation== Configuration.ORIENTATION_LANDSCAPE) {

            /* GMap Setup */
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        if (ContextCompat.checkSelfPermission((getActivity()), android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);

        }
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick ( final Marker marker){
                Intent mIntent = new Intent(getActivity(), MessageActivity.class);
                mIntent.putExtra("idMessage", marker.getTag().toString());
                mIntent.putExtra("currentLat",mLastLocation.getLatitude());
                mIntent.putExtra("currentLon",mLastLocation.getLongitude());
                mIntent.putExtra("latitudeMessage",marker.getPosition().latitude);
                mIntent.putExtra("longitudeMessage",marker.getPosition().longitude);
                startActivity(mIntent);
                return true;
            }
        });
    }

    public void drawMarker() {
        map.clear();
        for (Message m : favoriteMessages) {
            map.addMarker(new MarkerOptions().position(new LatLng(m.getLatitude(), m.getLongitude())).title(m.getText()).snippet(m.getUserName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))).setTag(m.getMessageId());
        }
    }

    public void LoadMessages(final List<Message> dataMessages){

        Collections.sort(dataMessages, new Comparator<Message>() {
            @Override
            public int compare(Message o1, Message o2) {
                return Integer.valueOf(o1.getDistance()).compareTo(Integer.valueOf(o2.getDistance()));
            }
        });

        ListView favorites = (ListView) getActivity().findViewById(R.id.favorites);
        CustomAdapter customAdapter = new CustomAdapter(getActivity(), R.layout.row, dataMessages);
        favorites.setAdapter(customAdapter);

        favorites.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Message message = (Message) parent.getItemAtPosition(position);

                Intent mIntent = new Intent(getActivity() ,MessageActivity.class);
                mIntent.putExtra("idMessage", message.getMessageId());
                mIntent.putExtra("currentLat",mLastLocation.getLatitude());
                mIntent.putExtra("currentLon",mLastLocation.getLongitude());
                mIntent.putExtra("latitudeMessage",message.getLatitude());
                mIntent.putExtra("longitudeMessage",message.getLongitude());
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
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 13));
            RoarifyCursor cursorAllMessages = db.findAll();
            favoriteMessages.clear();

            while(cursorAllMessages.moveToNext()){
                Message iMessage = new Message();
                iMessage.setUserName(cursorAllMessages.getUserName());
                iMessage.setText(cursorAllMessages.getMessage());
                iMessage.setTime(cursorAllMessages.getTime());
                iMessage.setMessageId(cursorAllMessages.getMessageId());
                iMessage.setLatitude(cursorAllMessages.getLat());
                iMessage.setLongitude(cursorAllMessages.getLon());
                Location locationMessage = new Location("Roarify");
                iMessage.setDistance(getDistanceToMessage(locationMessage, iMessage).toString());

                favoriteMessages.add(iMessage);
            }
            LoadMessages(favoriteMessages);
            if (isTablet && orientation==Configuration.ORIENTATION_LANDSCAPE) {
                drawMarker();
            }
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

        return Math.round(mLastLocation.distanceTo(locationMessage));
    }

}