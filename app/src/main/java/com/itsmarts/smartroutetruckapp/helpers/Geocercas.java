package com.itsmarts.smartroutetruckapp.helpers;

import android.util.Log;

import com.here.sdk.core.Color;
import com.here.sdk.core.GeoCircle;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.GeoPolygon;
import com.here.sdk.core.errors.InstantiationErrorException;
import com.here.sdk.mapview.MapPolygon;
import com.here.sdk.mapview.MapPolyline;
import com.itsmarts.smartroutetruckapp.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class Geocercas {
    MainActivity mainActivity;
    public List<MapPolygon> geocercasControlPoint = new ArrayList<>();
    public MapPolygon geocercas;
    public MapPolygon mapPolygon;
    public Geocercas(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void drawGecocercaControlPoint(GeoCoordinates center, double radiusInMeters) {
        GeoCircle geoCircle = new GeoCircle(center, radiusInMeters);
        GeoPolygon geoPolygon = new GeoPolygon(geoCircle);
        //Color fillColor = Color.valueOf(1.0f, 0.5f, 0.0f, 0.3f); // NARANJA
        //Color fillColor = Color.valueOf(0.0f, 179.0f/255.0f, 172.0f/255.0f, 0.2f); // VERDE
        Color fillColor = Color.valueOf(0.0f, 0.5f, 1.0f, 0.3f); // AZUL
        //Color fillColor= Color.valueOf(1.0f, 1.0f, 0.0f, 0.3f); // AMARILLO
        MapPolygon mapPolygon = new MapPolygon(geoPolygon, fillColor);
        geocercasControlPoint.add(mapPolygon);
        mainActivity.mapView.getMapScene().addMapPolygon(mapPolygon);
    }

    public void drawGeofenceAroundPolyline(MapPolyline polyline, double bufferDistanceInMeters) {
        List<GeoCoordinates> originalCoordinates = polyline.getGeometry().vertices;
        List<GeoCoordinates> bufferCoordinates = new ArrayList<>();

        // Paso 1: Generar puntos intermedios a lo largo de la polilínea
        List<GeoCoordinates> denseCoordinates = new ArrayList<>();
        for (int i = 0; i < originalCoordinates.size() - 1; i++) {
            GeoCoordinates start = originalCoordinates.get(i);
            GeoCoordinates end = originalCoordinates.get(i + 1);
            denseCoordinates.add(start);

            double segmentLength = start.distanceTo(end);
            int numIntermediatePoints = Math.max(1, (int) (segmentLength / 10)); // Un punto cada 10 metros

            for (int j = 1; j < numIntermediatePoints; j++) {
                double fraction = j / (double) numIntermediatePoints;
                denseCoordinates.add(Distances.interpolatePoint(start, end, fraction));
            }
        }
        denseCoordinates.add(originalCoordinates.get(originalCoordinates.size() - 1));

        Log.e("Prueba",""+denseCoordinates.size());

        // Paso 2: Generar puntos del buffer
        for (int i = 0; i < denseCoordinates.size(); i++) {
            GeoCoordinates current = denseCoordinates.get(i);
            GeoCoordinates prev = i > 0 ? denseCoordinates.get(i - 1) : null;
            GeoCoordinates next = i < denseCoordinates.size() - 1 ? denseCoordinates.get(i + 1) : null;

            double bearing = Distances.calculateBearing(prev != null ? prev : current, next != null ? next : current);

            double leftBearing = (bearing - 90 + 360) % 360;
            double rightBearing = (bearing + 90) % 360;

            GeoCoordinates leftPoint = Distances.calculateDestinationPoint(current, leftBearing, bufferDistanceInMeters);
            GeoCoordinates rightPoint = Distances.calculateDestinationPoint(current, rightBearing, bufferDistanceInMeters);

            if(denseCoordinates.size()>150){
                boolean validacion_erronea = false;
                if(i<=150){
                    for (int j = i; j < i+100; j=j+6) {
                        if (leftPoint.distanceTo(denseCoordinates.get(j))<=bufferDistanceInMeters)validacion_erronea=true;
                    }
                    for (int j = 0; j < i; j=j+6) {
                        if (leftPoint.distanceTo(denseCoordinates.get(j))<=bufferDistanceInMeters)validacion_erronea=true;
                    }
                    if(!validacion_erronea){
                        bufferCoordinates.add(leftPoint);
                    }
                    validacion_erronea=false;
                    for (int j = i; j < i+150; j=j+6) {
                        if (rightPoint.distanceTo(denseCoordinates.get(j))<=bufferDistanceInMeters)validacion_erronea=true;
                    }
                    for (int j = 0; j < i; j=j+6) {
                        if (rightPoint.distanceTo(denseCoordinates.get(j))<=bufferDistanceInMeters)validacion_erronea=true;
                    }
                    if(!validacion_erronea){
                        bufferCoordinates.add(0,rightPoint);
                    }
                }else if(i>=denseCoordinates.size()-151){
                    for (int j = i; j < denseCoordinates.size(); j=j+6) {
                        if (leftPoint.distanceTo(denseCoordinates.get(j))<=bufferDistanceInMeters)validacion_erronea=true;
                    }
                    for (int j = i; j > i-150; j=j-6) {
                        if (leftPoint.distanceTo(denseCoordinates.get(j))<=bufferDistanceInMeters)validacion_erronea=true;
                    }
                    if(!validacion_erronea){
                        bufferCoordinates.add(leftPoint);
                    }
                    validacion_erronea=false;
                    for (int j = i; j < denseCoordinates.size(); j=j+6) {
                        if (rightPoint.distanceTo(denseCoordinates.get(j))<=bufferDistanceInMeters)validacion_erronea=true;
                    }
                    for (int j = i; j > i-150; j=j-6) {
                        if (rightPoint.distanceTo(denseCoordinates.get(j))<=bufferDistanceInMeters)validacion_erronea=true;
                    }
                    if(!validacion_erronea){
                        bufferCoordinates.add(0,rightPoint);
                    }
                }else{
                    for (int j = i; j < i+150; j=j+6) {
                        if (leftPoint.distanceTo(denseCoordinates.get(j))<=bufferDistanceInMeters)validacion_erronea=true;
                    }
                    for (int j = i; j > i-150; j=j-6) {
                        if (leftPoint.distanceTo(denseCoordinates.get(j))<=bufferDistanceInMeters)validacion_erronea=true;
                    }
                    if(!validacion_erronea){
                        bufferCoordinates.add(leftPoint);
                    }
                    validacion_erronea=false;
                    for (int j = i; j < i+150; j=j+6) {
                        if (rightPoint.distanceTo(denseCoordinates.get(j))<=bufferDistanceInMeters)validacion_erronea=true;
                    }
                    for (int j = i; j > i-150; j=j-6) {
                        if (rightPoint.distanceTo(denseCoordinates.get(j))<=bufferDistanceInMeters)validacion_erronea=true;
                    }
                    if(!validacion_erronea){
                        bufferCoordinates.add(0,rightPoint);
                    }
                }
            }else{
                if(Distances.distanceToPolyline(leftPoint,polyline.getGeometry())>=bufferDistanceInMeters){
                    bufferCoordinates.add(leftPoint);
                }
                if(Distances.distanceToPolyline(rightPoint,polyline.getGeometry())>=bufferDistanceInMeters){
                    bufferCoordinates.add(0, rightPoint);
                }
            }
        }

        // Cerrar el polígono
        bufferCoordinates.add(bufferCoordinates.get(0));

        try {
            GeoPolygon geoPolygon = new GeoPolygon(bufferCoordinates);
            Color fillColor = Color.valueOf(0.0f, 179.0f / 255.0f, 172.0f / 255.0f, 0.2f);
            Color strokeColor = Color.valueOf(1.0f, 0.5f, 0.0f, 1.0f); // Orange
            float strokeWidthInPixels = 5.0f;

            MapPolygon mapPolygon = new MapPolygon(geoPolygon, fillColor);
            mapPolygon.setOutlineColor(strokeColor);
            mapPolygon.setOutlineWidth(strokeWidthInPixels);
            geocercas = mapPolygon;
        } catch (InstantiationErrorException e) {
            Log.e("Geofence", "Error al crear el polígono: " + e.getMessage());
        }
    }

    public void drawCircle(GeoCoordinates center, double radiusInMeters) {
        GeoCircle geoCircle = new GeoCircle(center, radiusInMeters);
        GeoPolygon geoPolygon = new GeoPolygon(geoCircle);
        //Color fillColor = Color.valueOf(1.0f, 0.5f, 0.0f, 0.3f); // NARANJA
        Color fillColor = Color.valueOf(0.0f, 179.0f/255.0f, 172.0f/255.0f, 0.2f); // VERDE
        //Color fillColor = Color.valueOf(0.0f, 0.5f, 1.0f, 0.3f); // AZUL
        //Color fillColor= Color.valueOf(1.0f, 1.0f, 0.0f, 0.3f); // AMARILLO
        mapPolygon = new MapPolygon(geoPolygon, fillColor);
        mainActivity.mapView.getMapScene().addMapPolygon(mapPolygon);
    }
}
