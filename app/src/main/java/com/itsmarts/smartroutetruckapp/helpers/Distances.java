package com.itsmarts.smartroutetruckapp.helpers;

import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.GeoPolyline;

import java.util.List;

public class Distances {

    private static final double EARTH_RADIUS_IN_METERS = 6371000; // Radio medio de la Tierra en metros

    public static double distanceToPolyline(GeoCoordinates point, GeoPolyline polyline) {
        double minDistance = Double.MAX_VALUE;
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
        return minDistance;
    }

    public static GeoCoordinates interpolatePoint(GeoCoordinates start, GeoCoordinates end, double fraction) {
        double lat = start.latitude + fraction * (end.latitude - start.latitude);
        double lon = start.longitude + fraction * (end.longitude - start.longitude);
        return new GeoCoordinates(lat, lon);
    }

    public static double calculateBearing(GeoCoordinates start, GeoCoordinates end) {
        double startLat = Math.toRadians(start.latitude);
        double startLng = Math.toRadians(start.longitude);
        double endLat = Math.toRadians(end.latitude);
        double endLng = Math.toRadians(end.longitude);

        double dLng = endLng - startLng;

        double y = Math.sin(dLng) * Math.cos(endLat);
        double x = Math.cos(startLat) * Math.sin(endLat) - Math.sin(startLat) * Math.cos(endLat) * Math.cos(dLng);

        return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
    }

    public static GeoCoordinates calculateDestinationPoint(GeoCoordinates start, double bearing, double distance) {
        double startLat = Math.toRadians(start.latitude);
        double startLng = Math.toRadians(start.longitude);
        double bearingRad = Math.toRadians(bearing);

        double distRatio = distance / EARTH_RADIUS_IN_METERS;
        double distRatioSine = Math.sin(distRatio);
        double distRatioCosine = Math.cos(distRatio);

        double startLatCos = Math.cos(startLat);
        double startLatSin = Math.sin(startLat);

        double endLatRads = Math.asin((startLatSin * distRatioCosine) + (startLatCos * distRatioSine * Math.cos(bearingRad)));
        double endLonRads = startLng + Math.atan2(Math.sin(bearingRad) * distRatioSine * startLatCos,
                distRatioCosine - startLatSin * Math.sin(endLatRads));

        return new GeoCoordinates(Math.toDegrees(endLatRads), Math.toDegrees(endLonRads));
    }
}
