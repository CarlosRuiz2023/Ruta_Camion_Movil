package com.itsmarts.smartroutetruckapp.clases;
/*
 * Copyright (C) 2019-2024 HERE Europe B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSE
 */
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import com.here.sdk.core.Color;
import com.here.sdk.core.GeoBox;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.GeoOrientation;
import com.here.sdk.core.GeoOrientationUpdate;
import com.here.sdk.core.GeoPolygon;
import com.here.sdk.core.GeoPolyline;
import com.here.sdk.core.errors.InstantiationErrorException;
import com.here.sdk.mapview.LineCap;
import com.here.sdk.mapview.MapCamera;
import com.here.sdk.mapview.MapImage;
import com.here.sdk.mapview.MapImageFactory;
import com.here.sdk.mapview.MapMarker;
import com.here.sdk.mapview.MapMeasure;
import com.here.sdk.mapview.MapMeasureDependentRenderSize;
import com.here.sdk.mapview.MapPolygon;
import com.here.sdk.mapview.MapPolyline;
import com.here.sdk.mapview.MapView;
import com.here.sdk.mapview.RenderSize;
import com.here.sdk.routing.CalculateRouteCallback;
import com.here.sdk.routing.Maneuver;
import com.here.sdk.routing.ManeuverAction;
import com.here.sdk.routing.OfflineRoutingEngine;
import com.here.sdk.routing.OptimizationMode;
import com.here.sdk.routing.PaymentMethod;
import com.here.sdk.routing.Route;
import com.here.sdk.routing.RoutingEngine;
import com.here.sdk.routing.RoutingError;
import com.here.sdk.routing.RoutingInterface;
import com.here.sdk.routing.Section;
import com.here.sdk.routing.SectionNotice;
import com.here.sdk.routing.Span;
import com.here.sdk.routing.Toll;
import com.here.sdk.routing.TollFare;
import com.here.sdk.routing.TollOptions;
import com.here.sdk.routing.TrafficOptimizationMode;
import com.here.sdk.routing.TrafficSpeed;
import com.here.sdk.routing.TruckOptions;
import com.here.sdk.routing.Waypoint;
import com.here.sdk.transport.TruckSpecifications;
import com.here.sdk.transport.TruckType;
import com.itsmarts.smartroutetruckapp.MainActivity;
import com.itsmarts.smartroutetruckapp.R;
import com.itsmarts.smartroutetruckapp.helpers.Distances;
import com.itsmarts.smartroutetruckapp.helpers.Messages;
import com.itsmarts.smartroutetruckapp.modelos.Triple;
import com.itsmarts.smartroutetruckapp.modelos.TruckSpec;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
public class RoutingExample {
    private static final String TAG = RoutingExample.class.getName();
    public final List < MapMarker > mapMarkerList = new ArrayList < > ();
    public final List < MapPolyline > mapPolylines = new ArrayList < > ();
    public RoutingEngine onlineRoutingEngine;
    public OfflineRoutingEngine offlineRoutingEngine;
    public RoutingInterface routingInterface;
    public static final int DEFAULT_TONELADAS = 17;
    public static final int DEFAULT_ALTO = 3;
    public static final int DEFAULT_ANCHO = 4;
    public static final int DEFAULT_LARGO = 8;
    private double toneladasIngresadas, altoIngresado, anchoIngresado, largoIngresado;
    private MainActivity mainActivity;
    // Define la duración de la animación de zoom en milisegundos
    private final long zoomAnimationDuration = 1000L;

    public RoutingExample(MainActivity mainActivity) {
        try{
            this.mainActivity = mainActivity;
            if(onlineRoutingEngine == null) {
                try {
                    onlineRoutingEngine = new RoutingEngine();
                } catch (InstantiationErrorException e) {
                    //throw new RuntimeException("Initialization of RoutingEngine failed: " + e.error.name());
                    Log.e(TAG, "Initialization of RoutingEngine failed: " + e.error.name());
                }
            }
            if(offlineRoutingEngine == null) {
                try {
                    // Allows to calculate routes on already downloaded or cached map data.
                    offlineRoutingEngine = new OfflineRoutingEngine();
                } catch (InstantiationErrorException e) {
                    //throw new RuntimeException("Initialization of OfflineRoutingEngine failed: " + e.error.name());
                    Log.e(TAG, "Initialization of OfflineRoutingEngine failed: " + e.error.name());
                }
            }
            routingInterface = onlineRoutingEngine;
        }catch (Exception e){
            mainActivity.logger.logError(TAG,e);
        }
    }

    private void setRoutingEngine() {
        try{
            if(NetworkUtil.isOnline(mainActivity.getApplicationContext())) {
                routingInterface = onlineRoutingEngine;
            } else {
                routingInterface = offlineRoutingEngine;
            }
        }catch (Exception e){
            mainActivity.logger.logError(TAG,e);
        }
    }
    public void setTruckSpecifications(double toneladas, double alto, double ancho, double largo) {
        try{
            this.toneladasIngresadas = toneladas;
            this.altoIngresado = alto;
            this.anchoIngresado = ancho;
            this.largoIngresado = largo;
        }catch (Exception e){
            mainActivity.logger.logError(TAG,e);
        }
    }
    public void addRoute(List < MapPolygon > poligonos, List < GeoCoordinates > puntos, GeoCoordinates startCoordinates, GeoCoordinates destinationCoordinates, GeoCoordinates geoCoordinatesPOI, GeoCoordinates geoCoordinatesInicioRuta, int id_vehiculo, boolean orden_automatico, RouteCallback callback) {
        try{
            clearMap();
            TruckSpecifications truckSpecifications = new TruckSpecifications();
            TruckSpec truckSpec = mainActivity.dbHelper.getCamion(id_vehiculo);
            truckSpecifications.grossWeightInKilograms = truckSpec.toneladas;
            truckSpecifications.heightInCentimeters = truckSpec.altura;
            truckSpecifications.widthInCentimeters = truckSpec.ancho;
            truckSpecifications.lengthInCentimeters = truckSpec.largo;
            List < Waypoint > waypointsList = new ArrayList < > ();
            waypointsList.add(new Waypoint(startCoordinates));
            if(geoCoordinatesInicioRuta != null) {
                if(startCoordinates.distanceTo(geoCoordinatesInicioRuta) > 3) {
                    waypointsList.add(new Waypoint(geoCoordinatesInicioRuta));
                }
            }
            if(puntos.size() > 0 || geoCoordinatesPOI != null) {
                if(!orden_automatico) {
                    // Crea una lista de pares (waypoint, distancia)
                    List < Triple < Waypoint, Double, Boolean >> waypointDistances = new ArrayList < > ();
                    for(GeoCoordinates punto: puntos) {
                        double distanceToStart = 0.0;
                        if(geoCoordinatesInicioRuta != null && startCoordinates.distanceTo(geoCoordinatesInicioRuta) > 3){
                            distanceToStart = punto.distanceTo(geoCoordinatesInicioRuta);
                        } else {
                            distanceToStart = punto.distanceTo(startCoordinates);
                        }
                        waypointDistances.add(new Triple < > (new Waypoint(punto), distanceToStart, true));
                    }
                    if(geoCoordinatesPOI != null) {
                        double distanceToStartPoi = 0.0;
                        if(geoCoordinatesInicioRuta != null){
                            if(startCoordinates.distanceTo(geoCoordinatesInicioRuta) > 3){
                                distanceToStartPoi = geoCoordinatesPOI.distanceTo(geoCoordinatesInicioRuta);
                            }else {
                                distanceToStartPoi = geoCoordinatesPOI.distanceTo(startCoordinates);
                            }
                        }else {
                            distanceToStartPoi = geoCoordinatesPOI.distanceTo(startCoordinates);
                        }
                        waypointDistances.add(new Triple < > (new Waypoint(geoCoordinatesPOI), distanceToStartPoi, false));
                    }
                    // Ordena la lista de pares por distancia al punto intermedio
                    waypointDistances.sort((p1, p2) -> Double.compare(p1.second, p2.second));
                    int cont = 0;
                    // Agrega los waypoints ordenados a la lista final
                    for(Triple < Waypoint, Double, Boolean > triple: waypointDistances) {
                        if(!triple.third) {
                            waypointsList.add(triple.first);
                        } else {
                            waypointsList.add(new Waypoint(puntos.get(cont)));
                        }
                        cont++;
                    }
                } else {
                    List < Pair < Waypoint, Double >> waypointDistances = new ArrayList < > ();
                    for(GeoCoordinates punto: puntos) {
                        double distanceToStart = 0.0;
                        if(geoCoordinatesInicioRuta != null && startCoordinates.distanceTo(geoCoordinatesInicioRuta) > 3) {
                            distanceToStart = punto.distanceTo(geoCoordinatesInicioRuta);
                        } else {
                            distanceToStart = punto.distanceTo(startCoordinates);
                        }
                        waypointDistances.add(new Pair < > (new Waypoint(punto), distanceToStart));
                    }
                    if(geoCoordinatesPOI != null) {
                        double distanceToStartPoi = 0.0;
                        if(geoCoordinatesInicioRuta != null){
                            if(startCoordinates.distanceTo(geoCoordinatesInicioRuta) > 3){
                                distanceToStartPoi = geoCoordinatesPOI.distanceTo(geoCoordinatesInicioRuta);
                            }else {
                                distanceToStartPoi = geoCoordinatesPOI.distanceTo(startCoordinates);
                            }
                        }else {
                            distanceToStartPoi = geoCoordinatesPOI.distanceTo(startCoordinates);
                        }
                        waypointDistances.add(new Pair < > (new Waypoint(geoCoordinatesPOI), distanceToStartPoi));
                    }
                    // Ordena la lista de pares por distancia al punto intermedio
                    waypointDistances.sort((p1, p2) -> Double.compare(p1.second, p2.second));
                    // Agrega los waypoints ordenados a la lista final
                    for(Pair < Waypoint, Double > pair: waypointDistances) {
                        waypointsList.add(pair.first);
                    }
                }
            }
            if(destinationCoordinates != null){
                waypointsList.add(new Waypoint(destinationCoordinates));
            }
            TruckOptions truckOptions = new TruckOptions();
            //truckOptions.routeOptions.enableTolls = true;
            //truckOptions.routeOptions.optimizeWaypointsOrder = orden_automatico;
            //truckOptions.routeOptions.optimizationMode = OptimizationMode.FASTEST;
            //truckOptions.routeOptions.enableRouteHandle = true;
            //truckOptions.routeOptions.trafficOptimizationMode = TrafficOptimizationMode.TIME_DEPENDENT;
            //truckOptions.truckSpecifications.truckType = TruckType.STRAIGHT;
            //truckOptions.truckSpecifications.isTruckLight = false;
            truckOptions.truckSpecifications = truckSpecifications;
            // GEOPOLYGON
            if(poligonos.size() > 0) {
                List < GeoPolygon > avoidanceZones = new ArrayList < > ();
                for(MapPolygon polygonZone: poligonos) {
                    avoidanceZones.add(polygonZone.getGeometry());
                    //mapView.getMapScene().addMapPolygon(polygonZone);
                }
                // Agrega las zonas de evitación a las opciones de enrutamiento
                truckOptions.avoidanceOptions.avoidPolygonAreas = avoidanceZones;
            }
            // Calculate the route
            //setRoutingEngine();
            routingInterface.calculateRoute(waypointsList, truckOptions, new CalculateRouteCallback() {
                @Override
                public void onRouteCalculated(@Nullable RoutingError routingError, @Nullable List < Route > routes) {
                    // Check if route calculation was successful
                    if(routingError == null) {
                        // Get the first route from the list
                        Route route = routes.get(0);
                        if(mainActivity.rutaGenerada){
                            // Calcular la extensión de la ruta
                            GeoBox geoBox = route.getBoundingBox();
                            // Calcular el centro del GeoBox
                            double centerLat = (geoBox.southWestCorner.latitude + geoBox.northEastCorner.latitude) / 2;
                            double centerLon = (geoBox.southWestCorner.longitude + geoBox.northEastCorner.longitude) / 2;
                            // Devolver el centro del GeoBox
                            GeoCoordinates center = new GeoCoordinates(centerLat, centerLon);
                            // Obtener el nivel de zoom actual
                            double currentZoomLevel = mainActivity.mapView.getCamera().getState().zoomLevel;
                            // Establecer manualmente un nivel de zoom objetivo basado en la extensión del GeoBox
                            double targetZoomLevel = calculateTargetZoomLevel(geoBox);
                            // Configurar el desplazamiento inicial de la cámara para evitar que la ruta no sea visible al inicio
                            mainActivity.mapView.getCamera().lookAt(geoBox, new GeoOrientationUpdate(center.latitude, center.longitude));
                            // Crear un ValueAnimator para interpolar entre el nivel de zoom actual y el objetivo
                            ValueAnimator zoomAnimator = ValueAnimator.ofFloat((float) currentZoomLevel, (float) targetZoomLevel);
                            // Configurar la duración de la animación
                            int animationDuration = 3000; // Duración en milisegundos
                            zoomAnimator.setDuration(animationDuration);
                            // Agregar un oyente para actualizar el nivel de zoom durante la animación
                            zoomAnimator.addUpdateListener(valueAnimator -> {
                                float animatedZoom = (float) valueAnimator.getAnimatedValue();
                                // Actualizar el nivel de zoom en la cámara
                                mainActivity.mapView.getCamera().setDistanceToTarget(animatedZoom);
                            });
                            // Iniciar la animación
                            zoomAnimator.start();
                        }
                        // Crear un ValueAnimator para interpolar entre el nivel de zoom actual y el objetivo
                        showRouteOnMap(route);
                        logRouteSectionDetails(route);
                        logRouteViolations(route);
                        logTollDetails(route);
                        processCalculatedRoute(route);
                        callback.onRouteCalculated(route);
                        if(geoCoordinatesPOI != null){
                            for(int i = 1; i < waypointsList.size() - 1; i++) {
                                if(waypointsList.get(i).coordinates.equals(geoCoordinatesPOI)) {
                                    addCircleMapMarker(waypointsList.get(i).coordinates, R.drawable.waypoint);
                                }
                            }
                        }
                    } else {
                        String errorMessage = (routingError != null) ? routingError.toString() : "No se encontró una ruta";
                        showDialog("Error al calcular la ruta", errorMessage);
                        callback.onRouteCalculated(null);
                    }
                }
            });
        }catch (Exception e){
            mainActivity.logger.logError(TAG,e);
        }
    }
    private void processCalculatedRoute(Route route) {
        try{
            showRouteOnMap(route);
            logRouteSectionDetails(route);
            logRouteViolations(route);
            logTollDetails(route);
        }catch (Exception e){
            mainActivity.logger.logError(TAG,e);
        }
    }
    private void processCalculatedRouteTour(Route route) {
        try{
            logRouteSectionDetails(route);
            logRouteViolations(route);
            logTollDetails(route);
        }catch (Exception e){
            mainActivity.logger.logError(TAG,e);
        }
    }
    private void logRouteViolations(Route route) {
        try{
            for(Section section: route.getSections()) {
                for(SectionNotice notice: section.getSectionNotices()) {
                    Log.e(TAG, "This route contains the following warning: " + notice.code.toString());
                }
            }
        }catch (Exception e){
            mainActivity.logger.logError(TAG,e);
        }
    }
    private void logRouteSectionDetails(Route route) {
        try{
            DateFormat dateFormat = new SimpleDateFormat("HH:mm");
            for(int i = 0; i < route.getSections().size(); i++) {
                Section section = route.getSections().get(i);
                Log.d(TAG, "Route Section : " + (i + 1));
                Log.d(TAG, "Route Section Departure Time : " + dateFormat.format(section.getDepartureLocationTime().localTime));
                Log.d(TAG, "Route Section Arrival Time : " + dateFormat.format(section.getArrivalLocationTime().localTime));
                Log.d(TAG, "Route Section length : " + section.getLengthInMeters() + " m");
                Log.d(TAG, "Route Section duration : " + section.getDuration().getSeconds() + " s");
            }
        }catch (Exception e){
            mainActivity.logger.logError(TAG,e);
        }
    }
    private void logTollDetails(Route route) {
        try{
            for(Section section: route.getSections()) {
                List < Span > spans = section.getSpans();
                List < Toll > tolls = section.getTolls();
                if(!tolls.isEmpty()) {
                    Log.d(TAG, "Attention: This route may require tolls to be paid.");
                }
                for(Toll toll: tolls) {
                    Log.d(TAG, "Toll information valid for this list of spans:");
                    Log.d(TAG, "Toll system: " + toll.tollSystem);
                    Log.d(TAG, "Toll country code (ISO-3166-1 alpha-3): " + toll.countryCode);
                    Log.d(TAG, "Toll fare information: ");
                    for(TollFare tollFare: toll.fares) {
                        Log.d(TAG, "Toll price: " + tollFare.price + " " + tollFare.currency);
                        for(PaymentMethod paymentMethod: tollFare.paymentMethods) {
                            Log.d(TAG, "Accepted payment methods for this price: " + paymentMethod.name());
                        }
                    }
                }
            }
        }catch (Exception e){
            mainActivity.logger.logError(TAG,e);
        }
    }
    private void showRouteOnMap(Route route) {
        try{
            //clearMap();
            //showTrafficOnRoute(route);
            GeoCoordinates startPoint = route.getSections().get(0).getDeparturePlace().mapMatchedCoordinates;
            GeoCoordinates destination = route.getSections().get(route.getSections().size() - 1).getArrivalPlace().mapMatchedCoordinates;
            addCircleMapMarker(startPoint, R.drawable.inicio);
            addCircleMapMarker(destination, R.drawable.destino);
            List < Section > sections = route.getSections();
            for(Section section: sections) {
                logManeuverInstructions(section);
            }
        }catch (Exception e){
            mainActivity.logger.logError(TAG,e);
        }
    }
    private void logManeuverInstructions(Section section) {
        try{
            Log.d(TAG, "Log maneuver instructions per route section:");
            List < Maneuver > maneuverInstructions = section.getManeuvers();
            for(Maneuver maneuverInstruction: maneuverInstructions) {
                ManeuverAction maneuverAction = maneuverInstruction.getAction();
                GeoCoordinates maneuverLocation = maneuverInstruction.getCoordinates();
                String maneuverInfo = maneuverInstruction.getText() + ", Action: " + maneuverAction.name() + ", Location: " + maneuverLocation.toString();
                Log.d(TAG, maneuverInfo);
            }
        }catch (Exception e){
            mainActivity.logger.logError(TAG,e);
        }
    }
    public void clearMap() {
        try{
            clearWaypointMapMarker();
        }catch (Exception e){
            mainActivity.logger.logError(TAG,e);
        }
    }
    private void clearWaypointMapMarker() {
        try{
            for(MapMarker mapMarker: mapMarkerList) {
                mainActivity.mapView.getMapScene().removeMapMarker(mapMarker);
            }
            mapMarkerList.clear();
        }catch (Exception e){
            mainActivity.logger.logError(TAG,e);
        }
    }
    private void showTrafficOnRoute(Route route) {
        try{
            if(route.getLengthInMeters() / 1000 > 5000) {
                Log.d(TAG, "Skip showing traffic-on-route for longer routes.");
                return;
            }
            for(Section section: route.getSections()) {
                for(Span span: section.getSpans()) {
                    TrafficSpeed trafficSpeed = span.getTrafficSpeed();
                    Color lineColor = getTrafficColor(trafficSpeed.jamFactor);
                    if(lineColor == null) {
                        continue;
                    }
                    float widthInPixels = 10;
                    MapPolyline trafficSpanMapPolyline = null;
                    try {
                        trafficSpanMapPolyline = new MapPolyline(span.getGeometry(), new MapPolyline.SolidRepresentation(new MapMeasureDependentRenderSize(RenderSize.Unit.PIXELS, widthInPixels), lineColor, LineCap.ROUND));
                    } catch (MapPolyline.Representation.InstantiationException e) {
                        Log.e("MapPolyline Representation Exception:", e.error.name());
                    } catch (MapMeasureDependentRenderSize.InstantiationException e) {
                        Log.e("MapMeasureDependentRenderSize Exception:", e.error.name());
                    }
                    mainActivity.mapView.getMapScene().addMapPolyline(trafficSpanMapPolyline);
                    mapPolylines.add(trafficSpanMapPolyline);
                }
            }
        }catch (Exception e){
            mainActivity.logger.logError(TAG,e);
        }
    }
    @Nullable
    private Color getTrafficColor(Double jamFactor) {
        try{
            if(jamFactor == null || jamFactor < 4) {
                return null;
            } else if(jamFactor >= 4 && jamFactor < 8) {
                return Color.valueOf(1, 1, 0, 0.63f);
            } else if(jamFactor >= 8 && jamFactor < 10) {
                return Color.valueOf(1, 0, 0, 0.63f);
            }
        }catch (Exception e){
            mainActivity.logger.logError(TAG,e);
        }
        return Color.valueOf(0, 0, 0, 0.63f);
    }

    public void addCircleMapMarker(GeoCoordinates geoCoordinates, int resourceId) {
        try{
            MapImage mapImage = MapImageFactory.fromResource(mainActivity.getResources(), resourceId);
            MapMarker mapMarker = new MapMarker(geoCoordinates, mapImage);
            mainActivity.mapView.getMapScene().addMapMarker(mapMarker);
            mapMarkerList.add(mapMarker);
        }catch (Exception e){
            mainActivity.logger.logError(TAG,e);
        }
    }

    private void showDialog(String title, String message) {
        try{
            new AlertDialog.Builder(mainActivity).setTitle(title).setMessage(message).setPositiveButton("OK", null).show();
        }catch (Exception e){
            mainActivity.logger.logError(TAG,e);
        }
    }

    public interface RouteCallback {
        void onRouteCalculated(Route route);
    }

    /**
     * Calcula un nivel de zoom objetivo para mostrar completamente el GeoBox.
     *
     * @param geoBox El GeoBox que contiene la ruta.
     * @return El nivel de zoom calculado.
     */
    private double calculateTargetZoomLevel(GeoBox geoBox) {
        try{
            // Dimensiones del GeoBox
            double geoBoxWidth = geoBox.northEastCorner.longitude - geoBox.southWestCorner.longitude;
            double geoBoxHeight = geoBox.northEastCorner.latitude - geoBox.southWestCorner.latitude;

            // Estimación del nivel de zoom basado en las dimensiones del GeoBox
            double maxDimension = Math.max(geoBoxWidth, geoBoxHeight);

            GeoCoordinates Top = new GeoCoordinates(geoBox.northEastCorner.latitude, geoBox.northEastCorner.longitude);
            GeoCoordinates Bottom = new GeoCoordinates(geoBox.southWestCorner.latitude, geoBox.southWestCorner.longitude);

            // Estimación del nivel de zoom basado en las dimensiones del GeoBox
            double zoomFactor = Top.distanceTo(Bottom) * 2.8;
            //double zoomFactor = 10.0;

            return zoomFactor - Math.log10(maxDimension);
        }catch (Exception e){
            mainActivity.logger.logError(TAG,e);
            return 0.0;
        }
    }
}