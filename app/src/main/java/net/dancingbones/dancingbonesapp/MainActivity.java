package net.dancingbones.dancingbonesapp;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        ActivityCompat.OnRequestPermissionsResultCallback,
        LocationListener {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    NavigationView navigationView = null;
    Toolbar toolbar = null;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LatLng deviceLatLng;
    /**
     *  Define a request code to send to Google PLay services.
     *  This code is returned in Activity.onActivityResult
     *  */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private final static int MY_PERMISSIONS_REQUEST_READ_LOCATION = 7001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Starting Google Cloud Messaging service
        Intent i = new Intent(this, RegistrationService.class);
        startService(i);

        //<Setting up Location services>
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .enableAutoManage(this /* FragmentActivity */,
                            this /* onConnectionFailedListener */)
                    .addApi(LocationServices.API)
                    .build();
        }
        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(10*1000)           //10 seconds, in milliseconds
                .setFastestInterval(1*1000);    //1 second, in milliseconds
        // </ Setting up Location services>

        setContentView(R.layout.activity_main);

        //<Setting up Fragment management system>
        // Set the fragment intially
        // Create a MainFragment object in which we can place different fragments
        MainFragment fragment = new MainFragment();
        // Create a FragmentTransaction object to handle changing the
        // fragments (the transaction)
        android.support.v4.app.FragmentTransaction fragmentTransaction =
                getSupportFragmentManager().beginTransaction();
        // replace fragment_container from app_bar_main.xml
        // with whatever is in the fragment object
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        // commit the transaction
        fragmentTransaction.commit();
        //</Setting up Fragment management system>

        //<toolbar setup>
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //</toolbar setup>

        //<Navigation drawer setup>
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //</Navigation drawer setup>
    }
    //Creates options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }
    @Override
    protected void onPause(){
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }
    //Handles connecting to Google API Location services.
    //sets the value of mLatitude and mLongitude if mLocation == null
    @Override
    public void onConnected(Bundle connectionHint){
        Log.i(LOG_TAG, "...Location services connected.");
        Log.i(LOG_TAG, "...Location : "+ deviceLatLng);
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if(location == null){
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                // location permissions are granted, but location == null
                Log.d(LOG_TAG, "...in onConnected, location == null.  Checking location.");
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }else{
                // location permissions are not granted and location == null.  Request permissions.
                Log.i(LOG_TAG, "...Location == null.  Requesting permissions");
                requestLocationPermissions();
            }
        }else{
            // location permissions are granted and location != null, but current location has changed.
            Log.i(LOG_TAG, "...Location != null.  Location : "+ deviceLatLng);
            handleNewLocation(location);
        }
    }
    //Check for permission to access location. Asks for permission if necessary.
    private void requestLocationPermissions(){
        // Do we already have permission?
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){
            //Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)){
               /* Show an explanation to the user *asynchronously* -- don't block
                this thread waiting for the user's respoinse!  After the user
                sees the explanation, try again to request the permission.*/
                Log.i(LOG_TAG, "...in requestLocationPermissions.  Showing rationale.");
            }else{
                // No explanation needed, we can request the permission.
                Log.i(LOG_TAG, "...in requestLocationPermissions.  Asking for permissions without rationale.");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_READ_LOCATION);
                /*MY_PERMISSION_REQUEST_READ_CONTACTS is an app-defined
                int constant.  The callback method gets the result request.*/
            }
        }else{
            // Permission is granted
            Log.i(LOG_TAG, "...in requestLocationPermissions.  result == PERMISSION_GRANTED.");
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                          String[] permissions,
                                          int[] grantResults){
        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_READ_LOCATION:{
                // If the request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // Permission was granted, yay! Do the
                    // location related task you need to do.
                    Log.e(LOG_TAG, "...Location permissions accepted. :)");
                }else{
                    // Permission denied, boo! Disable the functionality
                    // that depends on this permission.
                    Log.e(LOG_TAG, "...Location permissions denied. :(");
                    //ToDo: Handle permissions being denied.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    private void handleNewLocation(Location location){
        Log.i(LOG_TAG, "...location : "+location);
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        deviceLatLng = new LatLng(currentLatitude, currentLongitude);
        Log.i(LOG_TAG, "...deviceLatLng : "+ deviceLatLng);
    }
    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LOG_TAG, "...Location services suspended.  Please reconnect.");
    }
    //Handles clicks on the back button in the top bar (navigation drawer)
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    //Handles clicks in the options (...) menu in the top bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }
    @SuppressWarnings("StatementWithEmptyBody")
    //Handles clicks in the navigation drawer
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        // Fragment transaction code is pasted from onCreate.
        // ToDo: Simplify this code by creating an interface that all fragments implement.  This will allow these pasted sections to be cast instead of declared.
        if (id == R.id.nav_about) {
            MainFragment fragment = new MainFragment();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
        } else if (id == R.id.nav_visit) {
            VisitFragment fragment = new VisitFragment();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
        } else if (id == R.id.nav_stayInTouch) {
            StayInTouchFragment fragment = new StayInTouchFragment();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
        } else if (id == R.id.nav_hiker_info) {
            HikerInfoFragment fragment = new HikerInfoFragment();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
        } else if (id == R.id.nav_settings) {
            SettingsFragment fragment = new SettingsFragment();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    //Handles failures in connecting to Google APIs
    @Override
    public void onConnectionFailed(ConnectionResult result){
        /** An unresolved error has occurred and a connection to Google APIs
         * could not be established.  Display an error message, or handle
         * the failure silently.
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google play services activity that can resolve the error.
         */
        Log.e(LOG_TAG, "...Failed to resolve connection to Google APIs");
        if (result.hasResolution()){
            try{
                //Start an ACtivity that tries to resolve the error.
                result.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /**
                 * Thrown if Google Play services canceled the original PendingIntent
                 */
            }catch (IntentSender.SendIntentException e){
                // Log the error
                e.printStackTrace();
                Log.e(LOG_TAG, "...Could not resolve the permissons error : "+e);
            }
        }else{
            /*
            * If no resolution is available, display a dialog to the user with the error
            */
            Log.i(LOG_TAG, "Location services connection failed with the code : "+ result.getErrorCode());
        }
    }
    @Override
    public void onLocationChanged(Location location){
        handleNewLocation(location);
    }
    // returns the location of the device in a (latitude, longitude) touple
    public LatLng getDeviceLatLng(){
        return deviceLatLng;
    }
}
