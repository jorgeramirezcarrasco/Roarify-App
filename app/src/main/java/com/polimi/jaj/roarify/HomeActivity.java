package com.polimi.jaj.roarify;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

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
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


import static android.R.layout.simple_list_item_1;

public class HomeActivity extends AppCompatActivity
        implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    private GoogleMap map;
    private View auxView;
    private Double lat;
    private Double lon;
    private String user_ID;
    private String profileName;
    ProgressDialog progressBar;
    ArrayList<String> dataMessages = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);


        if (AccessToken.getCurrentAccessToken() == null) {
            goLoginScreen();
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (Profile.getCurrentProfile() != null) {
            setUserData(navigationView);
        }
    }

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

    private void goLoginScreen() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void logout(View view) {
        LoginManager.getInstance().logOut();
        goLoginScreen();
    }

    public void LoadMessages(ArrayList<String> dataMessages){

        dataMessages.toArray();
        //String[] data = {"First", "Second", "Third", "Fourth", "Fifth", "Sixth", "Seventh", "Eighth", "Ninth"};
        ListView comments = (ListView) findViewById(R.id.comments);
        comments.setAdapter(new ArrayAdapter<String>(this, simple_list_item_1, dataMessages));
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        new GetNearMessages().execute();//When map loading obtain messages
        map = googleMap;


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

    private class GetNearMessages extends AsyncTask<Void, Message, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            while (!isCancelled()) {
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
                try {
                    Thread.sleep(5000);
                    dataMessages.clear();//Every 5 seconds clear and refresh with new messages
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            return null;
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
                        dataMessages.add(values[0].getTitle());
                        LoadMessages(dataMessages);

                    }
                }

    }




}
