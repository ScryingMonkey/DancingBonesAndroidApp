package net.dancingbones.dancingbonesapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class VisitFragment extends Fragment implements View.OnClickListener {

    //private ArrayAdapter<String> dbDistanceAdapter;
    //private String distanceInMilesStr;
    //private String distanceInMinutesStr;

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    Button visitButton;

    public VisitFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_visit, container, false);
        //super.onCreate(savedInstanceState);

        visitButton = (Button) rootView.findViewById(R.id.button_visit);
        visitButton.setOnClickListener(this);

        // Inflate the layout for this fragment
        return rootView;
    }

    // Opens an an intent to map current location to dancing bones
    // and start turn by turn directions.
    @Override
    public void onClick(View v) {
        Log.i(LOG_TAG, "Inside onClick method...");
        Context context = this.getActivity();
        String message = "I heard you click the visit button!";
        int duration = Toast.LENGTH_LONG;
        Toast.makeText(context, message, duration).show();
        String zipCode = "03282";
        routeOnMap(zipCode);
    }

    // Handles click event.  onClick opens an intent to map current
    // location to dancing bones and start turn by turn directions.
    public void routeOnMap(String location) {
        // Use the URI scheme for showing a location found on a map.  This super-handy
        // intent is detailed in the "Common Intents" page of Android's development site:
        // http://developer.android.com/guide/components/intents-common.html#Maps

        Uri geolocation = Uri.parse("google.navigation:q=33+Dancing+Bones+Rd,+Wentworth,+NH+03282");

        //Uri geolocation = Uri.parse("geo:0,0?").buildUpon()
        //        .appendQueryParameter("q", location)
        //        .build();
        Log.i(LOG_TAG, "geolocation set to: " + geolocation.toString());


        Intent mapIntent = new Intent(Intent.ACTION_VIEW, geolocation);
        mapIntent.setPackage("com.google.android.apps.maps");
        mapIntent.setData(geolocation);



        if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(mapIntent);
        }else{
            Log.d(LOG_TAG, "Couldn't call " + location + ", no receiving apps installed!");
        }
    }

    //get distance information and send to adapter to display in view
    private void updateDistanceInfo(){
        getDistanceToDBTask distanceTask = new getDistanceToDBTask();
        distanceTask.execute();

    }

    @Override
    public void onStart(){
        super.onStart();
        updateDistanceInfo();

    }
    // Queries Google Maps API for distance from current location to dancing bones.
    public class getDistanceToDBTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = getDistanceToDBTask.class.getSimpleName();

        // Takes in the raw json string from the api and pulls out the information we want
        // (the distance and drive time from the origin to Dancing Bones).
        private String[] getDistanceDataFromJson(String distanceToDBJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_ROWS = "rows";
            final String OWM_ELEMENTS = "elements";
            final String OWM_DISTANCE = "distance";
            final String OWM_DURATION = "duration";
            String[] resultStrs = new String[2];
            String distanceInMiles;
            String distanceInMinutes;

            // get the whole the base JSON object "rows" as an array
            JSONObject distanceToDBJson = new JSONObject(distanceToDBJsonStr);
            //JSONArray distanceToDBArray = distanceToDBJson.getJSONArray(OWM_ROWS);
            // The data we want is in 2 arrays called "distance" and "duration".
            // These are both in a child array called "elements", which is the
            // first element in "rows" and 2 element long.
            JSONObject elements = distanceToDBJson.getJSONArray(OWM_ROWS).getJSONObject(0).getJSONArray(OWM_ELEMENTS).getJSONObject(0);
            Log.v(LOG_TAG, "...Retrieved elements array: " +elements);
            // get distance in miles and set distanceInMiles to this value
            JSONObject distanceObject = elements.getJSONObject(OWM_DISTANCE);
            distanceInMiles = distanceObject.getString("text");
            // get distance in minutes and set distanceInMinutes to this value
            JSONObject durationObject = elements.getJSONObject(OWM_DURATION);
            distanceInMinutes = durationObject.getString("text");

            Log.v(LOG_TAG, "...Retrieved distance to DB: " + distanceInMiles + ", and duration to DB: " + distanceInMinutes);

            resultStrs[0] = distanceInMiles;
            resultStrs[1] = distanceInMinutes;

            return resultStrs;
        }

        @Override
        protected String[] doInBackground(String... params) {

            // If theres is no postal code, there's nothing to look up.  Verify size of params.
            if (params.length == 0) {
                Log.i(LOG_TAG, "...No params given");
                //    return null;
            }
            LatLng deviceLatLng = getDeviceLocation();
            if (getDeviceLocation() != null) {
                return getDistanceToDB(deviceLatLng);
            }else{
                String[] resultStrs = {"I'm sorry, you didn't trust me with your location.",":("};
                return resultStrs;
            }
        }

        @Override
        protected void onPostExecute(String[] result) {
            TextView distanceTo = (TextView) getActivity().findViewById(R.id.distance_to);
            if (result != null) {
                if (result[1] == ":(") {
                    Log.v(LOG_TAG, "...in onPostExecute.  result != null, but result[1] was :(");
                    String distanceToStr = result[0];
                    distanceTo.setText(distanceToStr);
                }else{
                    double distanceInMilesStr = Double.parseDouble(result[0].substring(0,result[0].indexOf(" mi")));
                    double distanceInMinutesStr = Double.parseDouble(result[1].substring(0,result[1].indexOf(" mins")));
                    Log.v(LOG_TAG, "...in onPostExecute.  distanceInMilesStr : "+distanceInMilesStr+", distanceInMinutesStr : "+distanceInMinutesStr );
                    if(distanceInMilesStr < 10) {
                        String distanceToStr = "It looks like you are right around the corner!  Why not stop by?";
                        distanceTo.setText(distanceToStr);
                    }else{
                        String distanceToStr = "You are only " + distanceInMilesStr + " miles from Dancing Bones!  You can be there in " + distanceInMinutesStr + " minutes!";
                        distanceTo.setText(distanceToStr);
                        Log.v(LOG_TAG, "...Updated strings in onPostExecute: distanceInMilesStr= " + distanceInMilesStr + ", distanceInMinutes= " + distanceInMinutesStr);

                        // New data is back from the server.  Hooray!
                    }
                }
            }else{
                Log.v(LOG_TAG, "...in onPostExecute.  result == null." );
                distanceTo.setText("");
            }
        }

        // Takes a LatLng of the devices current location
        // Returns the distance to DB using Google Maps API
        private String[] getDistanceToDB(LatLng deviceLatLng){
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String distanceToDBJsonStr = null;
            //ToDo: Read this key from a text file, instead of hard coding it in like this, before pushing to GitHub.
            //String GOOGLE_MAPS_API_KEY = getString(R.string.key_GoogleMapsApi);
            String GOOGLE_MAPS_API_KEY = "AIzaSyCcI2p6oyU-UzGAW9l4gO8KfzPKgf1Kbc0";
            try {
                // Construct the URL for the Google Maps query
                // Possible parameters are avaiable at Google's API page, at
                // https://developers.google.com/maps/documentation/distance-matrix/intro#Introduction
                // ex. https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins=33+Blackcat+Mountain+Rd,+Poland,+ME+04274&destinations=33+Dancing+Bones+Road,+Wentworth,+NH+03282:&key=MY_API_KEY
                String baseUrl = "https://maps.googleapis.com/maps/api/distancematrix/json";
                String units = "units=imperial";
                String destinationGeolocation = "destinations=33+Dancing+Bones+Road,+Wentworth,+NH+03282";
                String apiKey = "key="+GOOGLE_MAPS_API_KEY;
                String originGeolocation = "origins="+Double.toString(deviceLatLng.latitude)+","+Double.toString(deviceLatLng.longitude);
                URL url = new URL(baseUrl+"?"+units+"&"+originGeolocation+"&"+destinationGeolocation+":&"+apiKey);
                Log.i(LOG_TAG, "Built url: " + url);

                // Create the request to Google Maps API, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                Log.i(LOG_TAG, "...connected to url");

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    //Log.v(LOG_TAG, "...line in reader = " + line);
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                distanceToDBJsonStr = buffer.toString();

                Log.v(LOG_TAG, "Distance to DB JSON String: " + distanceToDBJsonStr);

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the google maps data, there's no point in attempting
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            try {
                return getDistanceDataFromJson(distanceToDBJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the location.

            return null;
        }
        }
    private LatLng getDeviceLocation(){
        // ToDo: Replace this with phone location.  Reference: http://developer.android.com/training/location/retrieve-current.html
        //String originGeolocation = "origins=33+Blackcat+Mountain+Rd,+Poland,+ME+04274";
        LatLng deviceLatLng = ((MainActivity)getActivity()).getDeviceLatLng();
        Log.i(LOG_TAG, "Inside doInBackground method.  deviceLatLng : "+deviceLatLng);
        return deviceLatLng;
    }



}

