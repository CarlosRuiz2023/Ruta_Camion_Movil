package com.itsmarts.OverFleet.helpers;

import android.util.Log;

import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.GeoPolyline;

import java.util.List;

public class Distances {

    private static final double EARTH_RADIUS_IN_METERS = 6371000; // Radio medio de la Tierra en metros
    private static final String TAG = "Distances";

    public static double distanceToPolyline(GeoCoordinates point, GeoPolyline polyline) {
        double minDistance = 0.0;
        try{
            minDistance = Double.MAX_VALUE;
            GeoCoordinates closestPoint = null;
            List<GeoCoordinates> vertices = polyline.vertices;

            // Encuentra el vértice más cercano al punto dado
            for (GeoCoordinates vertex : vertices) {
                double distance = point.distanceTo(vertex);
                if (distance < minDistance) {
                    minDistance = distance;
                    closestPoint = vertex;
                }
            }

            // Devuelve la distancia al punto más cercano
        }catch(Exception e){
            Log.e(TAG, "Error calculating distance to polyline: " + e.getMessage());
        }
        return minDistance;
    }

    public static GeoCoordinates interpolatePoint(GeoCoordinates start, GeoCoordinates end, double fraction) {
        double lat = 0.0;
        double lon = 0.0;
        try{
            lat = start.latitude + fraction * (end.latitude - start.latitude);
            lon = start.longitude + fraction * (end.longitude - start.longitude);
        }catch(Exception e){
            Log.e(TAG, "Error interpolating points: " + e.getMessage());
        }
        return new GeoCoordinates(lat, lon);
    }

    public static double calculateBearing(GeoCoordinates start, GeoCoordinates end) {
        double y = 0.0;
        double x = 0.0;
        try{
            double startLat = Math.toRadians(start.latitude);
            double startLng = Math.toRadians(start.longitude);
            double endLat = Math.toRadians(end.latitude);
            double endLng = Math.toRadians(end.longitude);

            double dLng = endLng - startLng;

            y = Math.sin(dLng) * Math.cos(endLat);
            x = Math.cos(startLat) * Math.sin(endLat) - Math.sin(startLat) * Math.cos(endLat) * Math.cos(dLng);

        }catch(Exception e){
            Log.e(TAG, "Error calculating bearing: " + e.getMessage());
        }
        return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
    }

    public static GeoCoordinates calculateDestinationPoint(GeoCoordinates start, double bearing, double distance) {
        double endLatRads = 0.0;
        double endLonRads = 0.0;
        try{
            double startLat = Math.toRadians(start.latitude);
            double startLng = Math.toRadians(start.longitude);
            double bearingRad = Math.toRadians(bearing);

            double distRatio = distance / EARTH_RADIUS_IN_METERS;
            double distRatioSine = Math.sin(distRatio);
            double distRatioCosine = Math.cos(distRatio);

            double startLatCos = Math.cos(startLat);
            double startLatSin = Math.sin(startLat);

            endLatRads = Math.asin((startLatSin * distRatioCosine) + (startLatCos * distRatioSine * Math.cos(bearingRad)));
            endLonRads = startLng + Math.atan2(Math.sin(bearingRad) * distRatioSine * startLatCos,
                    distRatioCosine - startLatSin * Math.sin(endLatRads));
        }catch(Exception e){
            Log.e(TAG, "Error calculating destination point: " + e.getMessage());
        }
        return new GeoCoordinates(Math.toDegrees(endLatRads), Math.toDegrees(endLonRads));
    }
}
