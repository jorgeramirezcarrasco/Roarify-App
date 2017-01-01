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
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.ProfilePictureView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.polimi.jaj.roarify.adapter.CustomAdapter;
import com.polimi.jaj.roarify.model.DisplayedMessage;
import com.polimi.jaj.roarify.model.Message;
import com.polimi.jaj.roarify.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class HomeActivity extends AppCompatActivity
        implements OnMapReadyCallback,
                   NavigationView.OnNavigationItemSelectedListener,
                   ConnectionCallbacks,
                   GoogleApiClient.OnConnectionFailedListener,
                   LocationListener {


    private View auxView;
    private SwipeRefreshLayout swipeContainer;

    /* Parameters needed for the dialog fragments */
    private View dialogViewReply;
    private LayoutInflater inflaterReply;
    private AlertDialog.Builder builderReply;
    private AlertDialog alertReply;
    private View dialogViewMessage;
    private LayoutInflater inflaterMessage;
    private AlertDialog.Builder builderMessage;
    private AlertDialog alertMessage;

    /* Server Connection parameters */
    private Double lat;
    private Double lon;
    private String user_ID;
    ArrayList<String> dataMessages = new ArrayList<String>();

    /* Google Maps parameters */
    private GoogleMap map;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private String mLastUpdateTime;
    private LatLng myLocation;
    static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0 ;
    private boolean mRequestingLocationUpdates;
    private LocationRequest mLocationRequest;

    /**
     * OnCreate method
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());

        /* Layout setup */
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertMessage.show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                new GetNearMessages().execute();
            }
        });

        swipeContainer.setColorSchemeResources(R.color.colorPrimary);

        /* Initial setup of the dialog fragment when clicking on a message */
        builderReply = new AlertDialog.Builder(this);
        builderReply.setPositiveButton("Reply", new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) {
            /* Method that sends the response to the server or the user that wrote this message (not clear yet).
             * In case of sending the message to the server, it would also be a multicast message. In case of
             * sending the message only to the user, the setup of this button should be done in the LoadMessages
             * method, as we need to know the user who sent the first message; moreover, we need to differentiate
             * between a normal user and an anonymous user. If the user is anonymous, this button shouldn't be
             * shown, or shouldn't be clickable (color it grey). */ }
        });
        builderReply.setNegativeButton("Cancel", new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) {
            dialog.cancel(); }
        });

        /* Setup of the dialog fragment when clicking on the '+' button */
        builderMessage = new AlertDialog.Builder(this);
        builderMessage.setPositiveButton("Roar!", new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) {
            }
        });
        builderMessage.setNegativeButton("Cancel", new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) {
            dialog.cancel(); }
        });
        inflaterMessage = this.getLayoutInflater();
        dialogViewMessage = inflaterMessage.inflate(R.layout.message_dialog, null);
        builderMessage.setView(dialogViewMessage);
        builderMessage.setTitle("New message");
        alertMessage = builderMessage.create();

        /* User data setup */
        if (Profile.getCurrentProfile() != null) {
            setUserData(navigationView);
        }

        /* Google Api Client Connection */
        buildGoogleApiClient();

        if(mGoogleApiClient!= null){
            mGoogleApiClient.connect();
        }
        /* GMap Setup */
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        /* Check Facebook token */
        if (AccessToken.getCurrentAccessToken() == null) {
            goLoginScreen();
        }


    }

    /**
     * Layout setup methods
     */


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_my_messages) {

        } else if (id == R.id.nav_favorites) {

        } else if (id == R.id.nav_help_feedback) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if (id == R.id.nav_logout) {
            logout(auxView);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void LoadMessages(ArrayList<String> dataMessages){
        /*
        dataMessages.toArray();
        //String[] data = {"First", "Second", "Third", "Fourth", "Fifth", "Sixth", "Seventh", "Eighth", "Ninth"};
        ListView comments = (ListView) findViewById(R.id.comments);
        comments.setAdapter(new ArrayAdapter<String>(this, simple_list_item_1, dataMessages));
        */

        DisplayedMessage message1 = new DisplayedMessage("First","This is message 1","17:46","239m");
        DisplayedMessage message2 = new DisplayedMessage("Second","This is a common message. Let's try to make it long, so we can check that it fits in any possible situation (different screens, orientation, etc.). ","13:22","590m");
        DisplayedMessage message3 = new DisplayedMessage("Third","This is a common message. Let's try to make it long. In this case, we are making it so long that ellipsis (puntos suspensivos) will appear. This part of the message is likely to disappear","1:10","25m");
        DisplayedMessage message4 = new DisplayedMessage("Fourth","This is message 4","12:01","29m");
        DisplayedMessage message5 = new DisplayedMessage("Fifth","This is message 5. Yet another example","23:23","851m");
        final List<DisplayedMessage> elements = new ArrayList<>();
        elements.add(message1);
        elements.add(message2);
        elements.add(message3);
        elements.add(message4);
        elements.add(message5);
        ListView comments = (ListView) findViewById(R.id.comments);
        CustomAdapter customAdapter = new CustomAdapter(this, R.layout.row, elements);
        comments.setAdapter(customAdapter);

        comments.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                inflaterReply = getLayoutInflater();
                dialogViewReply = inflaterReply.inflate(R.layout.reply_dialog, null);
                builderReply.setView(dialogViewReply);
                builderReply.setTitle(elements.get(position).getAuthor());
                builderReply.setMessage(elements.get(position).getMessage()).setCancelable(false);
                alertReply = builderReply.create();
                alertReply.show();
            }
        });
    }


    /**
     * Login Management methods
     */

    private void goLoginScreen() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void logout(View view) {
        LoginManager.getInstance().logOut();
        goLoginScreen();
    }
    public void setUserData(NavigationView navigationView) {
        Profile profile = Profile.getCurrentProfile();
        String userId = profile.getId();

        String firstName = profile.getFirstName();
        String lastName = profile.getLastName();
        String profileName = firstName + " " + lastName;

        View headerLayout = navigationView.getHeaderView(0);
        TextView username = (TextView) headerLayout.findViewById(R.id.username);
        username.setText(profileName);

        ProfilePictureView profilePictureView;
        profilePictureView = (ProfilePictureView) headerLayout.findViewById(R.id.profilePicture);
        profilePictureView.setProfileId(userId);


    }

    /**
     * Google Maps methods
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        new GetNearMessages().execute();//When map loading obtain messages
        map = googleMap;
        // Check permission about location before enable location button on map
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public void drawMarker(LatLng myLocation){
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 10));
        map.addMarker(new MarkerOptions().position(myLocation).title("My position"));
    }


    /**
     * Server Connection methods
     */

    private class GetNearMessages extends AsyncTask<Void, Message, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            /*while (!isCancelled()) {*/
                List<NameValuePair> pairs = new ArrayList<NameValuePair>();

                //TESTING...........
                lat = 13.5;
                lon = 10.2;
                user_ID = "12345";


                //...........

                pairs.add(new BasicNameValuePair("lat", "" + lat));
                pairs.add(new BasicNameValuePair("long", "" + lon));
                pairs.add(new BasicNameValuePair("userId", user_ID));


                String paramsString = URLEncodedUtils.format(pairs, "UTF-8");
                HttpGet get = new HttpGet("http://1-dot-roarify-152612.appspot.com/getNearMessages" + "?" + paramsString);

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
                } catch (IOException e) {
                    e.printStackTrace();
                }
                /*try {
                    Thread.sleep(5000);*/
                    dataMessages.clear();//Every 5 seconds clear and refresh with new messages
                /*} catch (InterruptedException e) {
                    e.printStackTrace();
                }*/

            /*}*/
            return null;
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
                dataMessages.add(values[0].getTitle());
                LoadMessages(dataMessages);

            }
        }

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
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
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
            mRequestingLocationUpdates=true;
            mLocationRequest = new LocationRequest();
            if (mRequestingLocationUpdates) {
                startLocationUpdates();
            }
        }
         /* If all the process was right draw the marker */
        if (mLastLocation != null) {
            /* Convert Location and call drawMarker method */
            myLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            drawMarker(myLocation);
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
                    mGoogleApiClient, mLocationRequest, this);}
    }
    /* Method that is called when Location is changed */
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        Toast.makeText(HomeActivity.this, "Location:"+mLastLocation.toString(), Toast.LENGTH_SHORT).show();
        Toast.makeText(HomeActivity.this, "Date:"+mLastUpdateTime.toString(), Toast.LENGTH_SHORT).show();
        myLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        drawMarker(myLocation);
    }
    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
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

}
