package com.polimi.jaj.roarify.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.ProfilePictureView;


import com.polimi.jaj.roarify.data.RoarifySQLiteRepository;
import com.polimi.jaj.roarify.fragments.FavoritesFragment;
import com.polimi.jaj.roarify.fragments.HomeFragment;
import com.polimi.jaj.roarify.R;
import com.polimi.jaj.roarify.fragments.MyMessagesFragment;


public class HomeActivity extends AppCompatActivity {


    /* Parameters needed for the dialog fragments */
    private View auxView;
    DrawerLayout drawer;
    private AlertDialog alertMessage;
    public static RoarifySQLiteRepository db;




    /**
     * OnCreate method
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());

        db = new RoarifySQLiteRepository(this);

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

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupNavigationDrawerContent(navigationView);
        }

        setupNavigationDrawerContent(navigationView);

        /* User data setup */
        if (Profile.getCurrentProfile() != null) {
            setUserData(navigationView);
        }


        /* Check Facebook token */
        if (AccessToken.getCurrentAccessToken() == null) {
            goLoginScreen();
        }

        //First start (Inbox Fragment)
        setFragment(0);

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
     * Layout setup methods
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
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
    private void setupNavigationDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.nav_home:
                                setFragment(0);
                                drawer.closeDrawer(GravityCompat.START);
                                return true;
                            case R.id.nav_my_messages:
                                setFragment(1);
                                drawer.closeDrawer(GravityCompat.START);
                                return true;
                            case R.id.nav_favorites:
                                setFragment(2);
                                drawer.closeDrawer(GravityCompat.START);
                                return true;
                            case R.id.nav_help_feedback:
                                return true;
                            case R.id.nav_share:
                                return true;
                            case R.id.nav_send:
                                return true;
                            case R.id.nav_logout:
                                logout(auxView);
                                return true;
                        }
                        return true;
                    }
                });
    }

    public void setFragment(int position) {
        FragmentManager fragmentManager;
        FragmentTransaction fragmentTransaction;
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        switch (position) {
            case 0:
                HomeFragment homeFragment = new HomeFragment();
                fragmentTransaction.replace(R.id.fragment_container, homeFragment);
                break;
            case 1:
                MyMessagesFragment myMessagesFragment = new MyMessagesFragment();
                fragmentTransaction.replace(R.id.fragment_container, myMessagesFragment);
                break;
            case 2:
                FavoritesFragment myFavoritesFragment = new FavoritesFragment();
                fragmentTransaction.replace(R.id.fragment_container, myFavoritesFragment);
                break;
        }
        fragmentTransaction.commit();
    }


}
