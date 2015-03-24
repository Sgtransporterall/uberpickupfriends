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

    private GoogleMap mMap;
    private String chosenItem = "";
    ArrayList addresses = new ArrayList<>();
    ArrayList<MapPoint> markers = new ArrayList<>();

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

                    final float lat = Float.valueOf(temp.getString("lat"));
                    final float lng = Float.valueOf(temp.getString("lng"));
                    final LatLng USER_INPUT = new LatLng(lat, lng);

                    markers.add(new MapPoint(chosenItem, USER_INPUT, i, 0));
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
                        MapPoint tmp = markers.get(i);
                        setUpMap(tmp.coords, tmp.name, color);
                    }
                }
            });
        }

        public int finished(int i, ArrayList a){
            return (i+1==a.size() ? 1 : 0);
        }
    }

}
