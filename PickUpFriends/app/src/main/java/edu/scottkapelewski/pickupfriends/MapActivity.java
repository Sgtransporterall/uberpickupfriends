package edu.scottkapelewski.pickupfriends;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class MapActivity extends FragmentActivity {

    private static final String LOG_TAG = "AlgoApp";

    /** GOOGLE MAPS API CONSTANTS **/
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";
    private static final String GEOCODE_API_BASE = "https://maps.googleapis.com/maps/api/geocode";
    private static final String API_KEY = "AIzaSyCqTDuTW_yD8XP-Bvd5KkzglpAgNePOw6s";
    private final String BUNDLE_KEY_ADDRESSES = "grabAddressesFromBundle";
    /*********************************/

    /** UBER API CONSTANTS **/
    private static final String UBER_BASE_URL = "https://api.uber.com/v1";
    private static final String UBER_CLIENT_ID = "rbGoc4pYCaiK9ZiTMAiu9UfVaFS7NPY-";
    private static final String UBER_SERVER_TOKEN = "GGk_FUpXdx2ngv7qhAp1usnwOLYtayQQAkgsikkW";
    private static final String UBER_PRICE_ESTIMATE = "/estimates/price";
    /************************/

    private GoogleMap mMap;
    private String chosenItem = "";
    ArrayList<String> location = new ArrayList<>();
    ArrayList addresses = new ArrayList<>();
    ArrayList<LatLng> markers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Intent i = getIntent();
        addresses = i.getParcelableArrayListExtra(BUNDLE_KEY_ADDRESSES);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(42.3601, 71.0589))
                        .zoom(13)
                        .build();
                MapFragment.newInstance(new GoogleMapOptions()
                        .camera(cameraPosition));

                GetLatitudeLongitude g = new GetLatitudeLongitude();
                g.execute();
            }
        }
    }

    private void setUpMap(LatLng l, String title, int color) {
        if(color<0)
            mMap.addMarker(new MarkerOptions().position(l).title(title)).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        else if(color==0)
            mMap.addMarker(new MarkerOptions().position(l).title(title));
        else
            mMap.addMarker(new MarkerOptions().position(l).title(title)).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
    }

    private class GetLatitudeLongitude extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection conn;
            StringBuilder jsonResults = new StringBuilder();

            for (int i = 0; i < addresses.size(); i++) {
                chosenItem = (String) addresses.get(i);
                try {
                    //builds string to be used in API call to Geocode
                    StringBuilder sb = new StringBuilder(GEOCODE_API_BASE + OUT_JSON);
                    sb.append("?key=" + API_KEY);
                    sb.append("&address=" + URLEncoder.encode(chosenItem, "utf8"));


                    URL url = new URL(sb.toString());
                    conn = (HttpURLConnection) url.openConnection();
                    InputStreamReader in = new InputStreamReader(conn.getInputStream());

                    // Load the results into a StringBuilder
                    int read;
                    char[] buff = new char[1024];
                    while ((read = in.read(buff)) != -1) {
                        jsonResults.append(buff, 0, read);
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    // Create a JSON object hierarchy from the results
                    JSONObject jsonObj = new JSONObject(jsonResults.toString());
                    JSONArray predsJsonArray = jsonObj.getJSONArray("results");
                    JSONObject temp = new JSONObject(predsJsonArray.get(0).toString());
                    temp = temp.getJSONObject("geometry");
                    temp = temp.getJSONObject("location");

                    location.add(temp.getString("lat"));
                    location.add(temp.getString("lng"));

                    final float lat = Float.valueOf(location.get(0));
                    final float lng = Float.valueOf(location.get(1));
                    final LatLng USER_INPUT = new LatLng(lat, lng);

                    markers.add(USER_INPUT);
                    location = new ArrayList<>();
                    jsonResults = new StringBuilder();

                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Cannot process JSON results", e);
                }
            }

            onPostExecute();
            return null;
        }

        protected void onPostExecute() {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < markers.size(); i++) {
                        int color;

                        if (i == 0) {
                            color = -1;
                        } else {
                            color = finished(i, markers);
                        }
                        setUpMap(markers.get(i), (String) addresses.get(i), color);
                    }
                }
            });
        }

        public int finished(int i, ArrayList a){
            return (i+1==a.size() ? 1 : 0);
        }
    }
        private class GetUberEstimatedFares extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... params) {
                HttpURLConnection conn;
                StringBuilder jsonResults = new StringBuilder();
                    try {
                        //builds string to be used in API call to Geocode
                        StringBuilder sb = new StringBuilder(UBER_BASE_URL + UBER_PRICE_ESTIMATE);
                        sb.append("?key=" + API_KEY);
                        sb.append("&address=" + URLEncoder.encode(chosenItem, "utf8"));


                        URL url = new URL(sb.toString());
                        conn = (HttpURLConnection) url.openConnection();
                        InputStreamReader in = new InputStreamReader(conn.getInputStream());

                        // Load the results into a StringBuilder
                        int read;
                        char[] buff = new char[1024];
                        while ((read = in.read(buff)) != -1) {
                            jsonResults.append(buff, 0, read);
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        // Create a JSON object hierarchy from the results
                        JSONObject jsonObj = new JSONObject(jsonResults.toString());
                        JSONArray predsJsonArray = jsonObj.getJSONArray("results");
                        JSONObject temp = new JSONObject(predsJsonArray.get(0).toString());
                        temp = temp.getJSONObject("geometry");
                        temp = temp.getJSONObject("location");

                        location.add(temp.getString("lat"));
                        location.add(temp.getString("lng"));

                        final float lat = Float.valueOf(location.get(0));
                        final float lng = Float.valueOf(location.get(1));
                        final LatLng USER_INPUT = new LatLng(lat, lng);

                        markers.add(USER_INPUT);
                        location = new ArrayList<>();
                        jsonResults = new StringBuilder();

                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "Cannot process JSON results", e);
                    }

                onPostExecute();
                return null;
            }

            protected void onPostExecute() {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        
                    }
                });
            }
    }
}
