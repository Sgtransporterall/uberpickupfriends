package edu.scottkapelewski.pickupfriends;

/**
 * Created by MACMINI on 3/24/15.
 */
public class Edge {
    MapPoint to;
    float estFare;

    Edge(MapPoint t, float est){
        this.to = t;
        this.estFare = est;
    }
}
