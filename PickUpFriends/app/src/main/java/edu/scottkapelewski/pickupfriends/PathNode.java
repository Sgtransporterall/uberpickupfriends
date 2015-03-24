package edu.scottkapelewski.pickupfriends;

/**
 * Created by MACMINI on 3/24/15.
 */
public class PathNode {
    MapPoint point;
    Edge toNext;

    PathNode(MapPoint p, Edge t){
        this.point = p;
        this.toNext = t;
    }
}
