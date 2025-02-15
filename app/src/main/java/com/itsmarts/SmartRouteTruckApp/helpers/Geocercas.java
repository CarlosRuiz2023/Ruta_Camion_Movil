package com.itsmarts.SmartRouteTruckApp.helpers;

import android.util.Log;

import com.here.sdk.core.Color;
import com.here.sdk.core.GeoCircle;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.GeoPolygon;
import com.here.sdk.core.errors.InstantiationErrorException;
import com.here.sdk.mapview.MapPolygon;
import com.here.sdk.mapview.MapPolyline;
import com.itsmarts.SmartRouteTruckApp.MainActivity;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.buffer.BufferOp;

import java.util.ArrayList;
import java.util.Collections;
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

    public void drawGecocercaControlPointReached(GeoCoordinates center, double radiusInMeters) {
        //Color fillColor = Color.valueOf(1.0f, 0.5f, 0.0f, 0.3f); // NARANJA
        Color fillColor= Color.valueOf(0.0f, 0.5f, 0.2f, 0.3f);
        //Color fillColor = Color.valueOf(0.0f, 0.5f, 1.0f, 0.3f); // AZUL
        //Color fillColor= Color.valueOf(1.0f, 1.0f, 0.0f, 0.3f); // AMARILLO
        GeoCircle geoCircle = new GeoCircle(center, radiusInMeters);
        GeoPolygon geoPolygon = new GeoPolygon(geoCircle);
        MapPolygon mapPolygon = new MapPolygon(geoPolygon, fillColor);
        geocercasControlPoint.add(mapPolygon);
        mainActivity.mapView.getMapScene().addMapPolygon(mapPolygon);
    }

    public void drawGeofenceAroundPolyline(MapPolyline polyline, double bufferDistanceInMeters) {
        // 1. Convertir polyline de HERE a coordenadas para Mapbox
        List<GeoCoordinates> hereCoordinates = polyline.getGeometry().vertices;
        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate[] coords = new Coordinate[hereCoordinates.size()];
        // Convertir coordenadas HERE a JTS
        for (int i = 0; i < hereCoordinates.size(); i++) {
            coords[i] = new Coordinate(hereCoordinates.get(i).longitude, hereCoordinates.get(i).latitude);
        }
        // Crear la línea en JTS
        LineString lineString = geometryFactory.createLineString(coords);
        // Convertir metros a grados (aproximadamente 1° = 111,320 m, depende de la latitud)
        double bufferDegrees = bufferDistanceInMeters / 111320.0;
        // Generar el buffer
        Geometry bufferedGeometry = BufferOp.bufferOp(lineString, bufferDegrees);
        // Obtener coordenadas del buffer
        List<GeoCoordinates> bufferCoordinates = new ArrayList<>();
        if (bufferedGeometry instanceof Polygon) {
            Polygon polygon = (Polygon) bufferedGeometry;
            Coordinate[] polygonCoords = polygon.getExteriorRing().getCoordinates();
            for (Coordinate coord : polygonCoords) {
                bufferCoordinates.add(new GeoCoordinates(coord.y, coord.x));
            }
        }
        try {
            GeoPolygon geoPolygon = new GeoPolygon(bufferCoordinates);
            Color fillColor = Color.valueOf(0.0f, 179.0f / 255.0f, 172.0f / 255.0f, 0.2f);
            Color strokeColor = Color.valueOf(1.0f, 0.5f, 0.0f, 1.0f); // Orange
            float strokeWidthInPixels = 5.0f;

            MapPolygon mapPolygon = new MapPolygon(geoPolygon, fillColor);
            mapPolygon.setOutlineColor(strokeColor);
            mapPolygon.setOutlineWidth(strokeWidthInPixels);
            geocercas = mapPolygon;
            mainActivity.mapView.getMapScene().addMapPolygon(mapPolygon);
            mainActivity.mapView.getMapScene().addMapPolyline(polyline);
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
