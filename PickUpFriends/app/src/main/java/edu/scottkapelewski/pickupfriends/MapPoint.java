package edu.scottkapelewski.pickupfriends;

import com.google.android.gms.maps.model.LatLng;

public class MapPoint {
    String name;
    LatLng coords;
    float weight;
    int id;
    boolean visited = false;

    public MapPoint(String n, LatLng l){
        this.name = n;
        this.coords = l;
    }

    public MapPoint(String n, LatLng l, int i){
        this.name = n;
        this.coords = l;
        this.id = i;
    }

    public MapPoint(String n, LatLng l, int i, float w){
        this.name = n;
        this.coords = l;
        this.id = i;
        this.weight = w;
    }
}
