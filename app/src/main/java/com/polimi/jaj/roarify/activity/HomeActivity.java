package com.polimi.jaj.roarify.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
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
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.ProfilePictureView;


import com.polimi.jaj.roarify.data.RoarifySQLiteRepository;
import com.polimi.jaj.roarify.fragment.FavoritesFragment;
import com.polimi.jaj.roarify.fragment.FeedbackFragment;
import com.polimi.jaj.roarify.fragment.HomeFragment;
import com.polimi.jaj.roarify.R;
import com.polimi.jaj.roarify.fragment.MyMessagesFragment;


public class HomeActivity extends AppCompatActivity {


    /* Parameters needed for the dialog fragments */
    private View auxView;
    DrawerLayout drawer;
    private int selectedFragment = 0;


    public static RoarifySQLiteRepository db;
    static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;




    /**
     * OnCreate method
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());

        db = new RoarifySQLiteRepository(this);

        initUI();



    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        initUI();
    }

    public void initUI()
    {
       /* Layout setup */

        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        navigationView.getMenu().getItem(selectedFragment).setChecked(true);

        /* User data setup */
        if (Profile.getCurrentProfile() != null) {
            setUserData(navigationView);
        }


        /* Check Facebook token */
        if (AccessToken.getCurrentAccessToken() == null) {
            goLoginScreen();
        }else{

            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                setFragment(selectedFragment);

            }else {
                int hasLocationPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
                if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
                    PermissionLocationHandler();
                }
            }
        }

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
            Intent mIntent = new Intent(this, SettingsActivity.class);
            startActivity(mIntent);
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
                                selectedFragment = 0;
                                setFragment(selectedFragment);
                                drawer.closeDrawer(GravityCompat.START);
                                return true;
                            case R.id.nav_my_messages:
                                selectedFragment = 1;
                                setFragment(selectedFragment);
                                drawer.closeDrawer(GravityCompat.START);
                                return true;
                            case R.id.nav_favorites:
                                selectedFragment = 2;
                                setFragment(selectedFragment);
                                drawer.closeDrawer(GravityCompat.START);
                                return true;
                            case R.id.nav_help_feedback:
                                selectedFragment = 3;
                                setFragment(selectedFragment);
                                drawer.closeDrawer(GravityCompat.START);
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
                FavoritesFragment favoritesFragment = new FavoritesFragment();
                fragmentTransaction.replace(R.id.fragment_container, favoritesFragment);
                break;
            case 3:
                FeedbackFragment feedbackFragment = new FeedbackFragment();
                fragmentTransaction.replace(R.id.fragment_container, feedbackFragment);
                break;
        }
        fragmentTransaction.commitAllowingStateLoss();
    }


    // onClick function for button in feedback screen
    public void sendMail(View view) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"roarifyapp@gmail.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int hasLocationPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
            PermissionLocationHandler();
        }else{
            setFragment(selectedFragment);
        }

    }

    public void PermissionLocationHandler(){
        DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ActivityCompat.requestPermissions(HomeActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            }
        };

        DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        };

        new AlertDialog.Builder(this)
                .setMessage("Please, to use Roarify we need to have access to your location")
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", cancelListener)
                .setCancelable(false)
                .create()
                .show();

    }

}
