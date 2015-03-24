package edu.scottkapelewski.pickupfriends;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DirectedGraphService extends IntentService {


    /** UBER API CONSTANTS **/
    private static final String UBER_BASE_URL = "https://api.uber.com/v1";
    private static final String UBER_CLIENT_ID = "rbGoc4pYCaiK9ZiTMAiu9UfVaFS7NPY-";
    private static final String UBER_SERVER_TOKEN = "GGk_FUpXdx2ngv7qhAp1usnwOLYtayQQAkgsikkW";
    private static final String UBER_PRICE_ESTIMATE = "/estimates/price";
    /************************/

    public DirectedGraphService() {
        super("DirectedGraphService");
    }

    HashMap<Integer, MapPoint> pointsMap;
    ArrayList<MapPoint> mPoints;
    HashMap<Integer, ArrayList<Edge>> graph = new HashMap<>();

    @Override
    protected void onHandleIntent(Intent intent) {

        //mPoints = intent.getParcelableArrayListExtra("MapPoints");
        for(int i = 0; i < mPoints.size(); i++){
            pointsMap.put(mPoints.get(i).id, mPoints.get(i));
            graph.put(mPoints.get(i).id, new ArrayList<Edge>());
        }
    }

    private class GetUberEstimatedFares extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            for(int i = 0; i < mPoints.size(); i++){
                MapPoint temp = mPoints.get(i);
                for(int j = 0; j < mPoints.size(); j++){
                    if(j==i){}
                    else {
                        Edge e = getUberFareForPoints(temp, mPoints.get(j));
                        ArrayList<Edge> t = graph.get(temp.name);
                        t.add(e);
                        graph.put(temp.id, t);
                    }
                }
            }
          /*  HttpURLConnection conn;
            StringBuilder jsonResults = new StringBuilder();
            try {
                //builds string to be used in API call to Geocode
                StringBuilder sb = new StringBuilder(UBER_BASE_URL + UBER_PRICE_ESTIMATE);
                sb.append("?key=" + UBER_SERVER_TOKEN);


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

              /*  final LatLng USER_INPUT = new LatLng(lat, lng);

                markers.add(USER_INPUT);
                location = new ArrayList<>();
                jsonResults = new StringBuilder();

            } catch (JSONException e) {
                //Log.e(LOG_TAG, "Cannot process JSON results", e);
            }*/

            onPostExecute();
            return null;
        }

        protected void onPostExecute() {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    findShortestPath();
                }
            });
        }

        private Edge getUberFareForPoints(MapPoint from, MapPoint to){
            float rand = new Random().nextFloat()*20;
            return new Edge(to, rand);
        }

        private ArrayList<PathNode> findShortestPath(){

            ArrayList<PathNode> path = new ArrayList<>();
            MapPoint next = null;

            for(int i = 0; i < pointsMap.size(); i++) {
                if(next==null)
                    next = pointsMap.get(i);

                ArrayList<Edge> temp = graph.get(next.id);
                assignWeights(next, temp);

               PathNode shortest = findMin(temp);
               path.add(shortest);
               next = shortest.point;
            }

            return path;
        }

        private void assignWeights(MapPoint input, ArrayList<Edge> e){
            for(int i = 0; i < e.size(); i++){
                Edge tmp = e.get(i);

                MapPoint mp = pointsMap.get(tmp.to.id);
                mp.weight = tmp.estFare + input.weight;
                pointsMap.put(tmp.to.id, mp);
            }
        }

        private PathNode findMin(ArrayList<Edge> e){
            float min = 9999;
            PathNode stored = null;

            for(int i = 0; i < e.size(); i++){
                MapPoint mp = pointsMap.get(e.get(i).to.id);
                if(mp.weight < min){
                    min = mp.weight;
                    stored = new PathNode(mp, e.get(i));
                }
            }
            return stored;
        }

    }
}
