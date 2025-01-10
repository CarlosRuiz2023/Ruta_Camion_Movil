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
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.Log;
import android.util.Pair;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import com.here.sdk.core.Color;
import com.here.sdk.core.GeoBox;
import com.here.sdk.core.GeoCoordinates;
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
import com.itsmarts.smartroutetruckapp.modelos.Triple;
import com.itsmarts.smartroutetruckapp.modelos.TruckSpec;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
public class RoutingExample {
    private static final String TAG = RoutingExample.class.getName();
    private final Context context;
    private final MapView mapView;
    public final List < MapMarker > mapMarkerList = new ArrayList < > ();
    public final List < MapPolyline > mapPolylines = new ArrayList < > ();
    private RoutingEngine routingEngine;
    private OfflineRoutingEngine offlineRoutingEngine;
    private RoutingInterface routingInterface;
    public static final int DEFAULT_TONELADAS = 17;
    public static final int DEFAULT_ALTO = 3;
    public static final int DEFAULT_ANCHO = 4;
    public static final int DEFAULT_LARGO = 8;
    private double toneladasIngresadas, altoIngresado, anchoIngresado, largoIngresado;
    private MainActivity mainActivity;
    // Define la duración de la animación de zoom en milisegundos
    final long zoomAnimationDuration = 1000L;
    public RoutingExample(MainActivity mainActivity) {
        this.context = mainActivity.getApplicationContext();
        this.mapView = mainActivity.mapView;
        this.mainActivity = mainActivity;
        if(routingEngine == null) {
            try {
                routingEngine = new RoutingEngine();
            } catch (InstantiationErrorException e) {
                throw new RuntimeException("Initialization of RoutingEngine failed: " + e.error.name());
            }
        }
        if(offlineRoutingEngine == null) {
            try {
                // Allows to calculate routes on already downloaded or cached map data.
                offlineRoutingEngine = new OfflineRoutingEngine();
            } catch (InstantiationErrorException e) {
                throw new RuntimeException("Initialization of OfflineRoutingEngine failed: " + e.error.name());
            }
        }
    }
    private void setRoutingEngine() {
        if(NetworkUtil.isOnline(this.context)) {
            routingInterface = routingEngine;
        } else {
            routingInterface = offlineRoutingEngine;
        }
    }
    public void setTruckSpecifications(double toneladas, double alto, double ancho, double largo) {
        this.toneladasIngresadas = toneladas;
        this.altoIngresado = alto;
        this.anchoIngresado = ancho;
        this.largoIngresado = largo;
    }
    public void addRoute(List < MapPolygon > poligonos, List < GeoCoordinates > puntos, GeoCoordinates startCoordinates, GeoCoordinates destinationCoordinates, GeoCoordinates geoCoordinatesPOI, GeoCoordinates geoCoordinatesInicioRuta, int id_vehiculo, boolean orden_automatico, RouteCallback callback) {
        clearMap();
        TruckSpecifications truckSpecifications = new TruckSpecifications();
        TruckSpec truckSpec = mainActivity.dbHelper.getCamion(id_vehiculo);
        truckSpecifications.grossWeightInKilograms = truckSpec.toneladas;
        truckSpecifications.heightInCentimeters = truckSpec.altura;
        truckSpecifications.widthInCentimeters = truckSpec.ancho;
        truckSpecifications.lengthInCentimeters = truckSpec.largo;
        List < Waypoint > waypointsList = new ArrayList < > ();
        waypointsList.add(new Waypoint(startCoordinates));
        if(startCoordinates.distanceTo(geoCoordinatesInicioRuta) > 3) {
            waypointsList.add(new Waypoint(geoCoordinatesInicioRuta));
        }
        if(puntos.size() > 0 || geoCoordinatesPOI != null) {
            /*double routeLength = route.getLengthInMeters();
            GeoPolyline polyline = route.getGeometry();
            double threshold = routeLength * 0.5; // 50% de la longitud de la ruta
            if(threshold > 10000) {
                threshold = 10000;
            }*/
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
                    if(geoCoordinatesInicioRuta != null && startCoordinates.distanceTo(geoCoordinatesInicioRuta) > 3){
                        distanceToStartPoi = geoCoordinatesPOI.distanceTo(geoCoordinatesInicioRuta);
                    } else {
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
                    if(geoCoordinatesInicioRuta != null && startCoordinates.distanceTo(geoCoordinatesInicioRuta) > 3){
                        distanceToStartPoi = geoCoordinatesPOI.distanceTo(geoCoordinatesInicioRuta);
                    } else {
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
        waypointsList.add(new Waypoint(destinationCoordinates));
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
        setRoutingEngine();
        routingInterface.calculateRoute(waypointsList, truckOptions, new CalculateRouteCallback() {
            @Override
            public void onRouteCalculated(@Nullable RoutingError routingError, @Nullable List < Route > routes) {
                // Check if route calculation was successful
                if(routingError == null) {
					/*List<Waypoint> waypointsFinales = new ArrayList<>();
					waypointsFinales.add(new Waypoint(startGeoCoordinates));*/
                    // Get the first route from the list
                    Route route = routes.get(0);
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
                    // Calcular la extensión de la ruta
                    GeoBox geoBox = calculateRouteGeoBox(route);
                    // Calcular el centro del GeoBox
                    GeoCoordinates center = calculateGeoBoxCenter(route);
                    // Configurar la cámara para que se ajuste a la extensión de la ruta
                    mainActivity.mapView.getCamera().lookAt(geoBox, new GeoOrientationUpdate(center.latitude, center.longitude));
                    // Ajustar el nivel de zoom para que toda la ruta sea visible en el mapa
                    //mainActivity.mapView.getCamera().zoomTo(adjustZoomLevel(route));
                    //animateZoom(adjustZoomLevel(route));
                } else {
                    String errorMessage = (routingError != null) ? routingError.toString() : "No se encontró una ruta";
                    showDialog("Error al calcular la ruta", errorMessage);
                    callback.onRouteCalculated(null);
                }
                // Calculate the route
				/*routingInterface.calculateRoute(
				        waypointsFinales,
				        truckOptions,
				        new CalculateRouteCallback() {
				            @Override
				            public void onRouteCalculated(@Nullable RoutingError routingError, @Nullable List<Route> routes) {
				                // Check if route calculation was successful
				                if (routingError == null && routes != null && !routes.isEmpty()) {
				                    Route route = routes.get(0);
				                    showRouteOnMap(route);
				                    logRouteSectionDetails(route);
				                    logRouteViolations(route);
				                    logTollDetails(route);
				                    processCalculatedRoute(route);
				                    callback.onRouteCalculated(route);
				                    for (int i = 1; i < waypointsList.size() - 1; i++) {
				                        if(!waypointsList.get(i).coordinates.equals(geoCoordinatesPOI)){
				                            addCircleMapMarker(waypointsList.get(i).coordinates, R.drawable.waypoint);
				                        }
				                    }
				                    // Calcular la extensión de la ruta
				                    GeoBox geoBox = calculateRouteGeoBox(route);
				                    // Calcular el centro del GeoBox
				                    GeoCoordinates center = calculateGeoBoxCenter(route);
				                    // Configurar la cámara para que se ajuste a la extensión de la ruta
				                    mainActivity.mapView.getCamera().lookAt(geoBox, new GeoOrientationUpdate(center.latitude, center.longitude));
				                    // Ajustar el nivel de zoom para que toda la ruta sea visible en el mapa
				                    //mainActivity.mapView.getCamera().zoomTo(adjustZoomLevel(route));
				                    //animateZoom(adjustZoomLevel(route));
				                } else {
				                    String errorMessage = (routingError != null) ? routingError.toString() : "No se encontró una ruta";
				                    showDialog("Error al calcular la ruta", errorMessage);
				                    callback.onRouteCalculated(null);
				                }
				            }
				        });*/
				/*} else {
				    // Show error message
				    showDialog("Error mientras se calculaba la primer ruta:", routingError.toString());
				}*/
            }
        });
    }
    private void processCalculatedRoute(Route route) {
        showRouteOnMap(route);
        logRouteSectionDetails(route);
        logRouteViolations(route);
        logTollDetails(route);
    }
    private void processCalculatedRouteTour(Route route) {
        logRouteSectionDetails(route);
        logRouteViolations(route);
        logTollDetails(route);
    }
    private void logRouteViolations(Route route) {
        for(Section section: route.getSections()) {
            for(SectionNotice notice: section.getSectionNotices()) {
                Log.e(TAG, "This route contains the following warning: " + notice.code.toString());
            }
        }
    }
    private void logRouteSectionDetails(Route route) {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        for(int i = 0; i < route.getSections().size(); i++) {
            Section section = route.getSections().get(i);
            Log.d(TAG, "Route Section : " + (i + 1));
            Log.d(TAG, "Route Section Departure Time : " + dateFormat.format(section.getDepartureLocationTime().localTime));
            Log.d(TAG, "Route Section Arrival Time : " + dateFormat.format(section.getArrivalLocationTime().localTime));
            Log.d(TAG, "Route Section length : " + section.getLengthInMeters() + " m");
            Log.d(TAG, "Route Section duration : " + section.getDuration().getSeconds() + " s");
        }
    }
    private void logTollDetails(Route route) {
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
    }
    private void showRouteOnMap(Route route) {
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
    }
    private void logManeuverInstructions(Section section) {
        Log.d(TAG, "Log maneuver instructions per route section:");
        List < Maneuver > maneuverInstructions = section.getManeuvers();
        for(Maneuver maneuverInstruction: maneuverInstructions) {
            ManeuverAction maneuverAction = maneuverInstruction.getAction();
            GeoCoordinates maneuverLocation = maneuverInstruction.getCoordinates();
            String maneuverInfo = maneuverInstruction.getText() + ", Action: " + maneuverAction.name() + ", Location: " + maneuverLocation.toString();
            Log.d(TAG, maneuverInfo);
        }
    }
    public void clearMap() {
        clearWaypointMapMarker();
    }
    private void clearWaypointMapMarker() {
        for(MapMarker mapMarker: mapMarkerList) {
            mapView.getMapScene().removeMapMarker(mapMarker);
        }
        mapMarkerList.clear();
    }
    private void showTrafficOnRoute(Route route) {
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
                mapView.getMapScene().addMapPolyline(trafficSpanMapPolyline);
                mapPolylines.add(trafficSpanMapPolyline);
            }
        }
    }
    @Nullable
    private Color getTrafficColor(Double jamFactor) {
        if(jamFactor == null || jamFactor < 4) {
            return null;
        } else if(jamFactor >= 4 && jamFactor < 8) {
            return Color.valueOf(1, 1, 0, 0.63f);
        } else if(jamFactor >= 8 && jamFactor < 10) {
            return Color.valueOf(1, 0, 0, 0.63f);
        }
        return Color.valueOf(0, 0, 0, 0.63f);
    }
    public void addCircleMapMarker(GeoCoordinates geoCoordinates, int resourceId) {
        MapImage mapImage = MapImageFactory.fromResource(context.getResources(), resourceId);
        MapMarker mapMarker = new MapMarker(geoCoordinates, mapImage);
        mapView.getMapScene().addMapMarker(mapMarker);
        mapMarkerList.add(mapMarker);
    }
    private void showDialog(String title, String message) {
        new AlertDialog.Builder(context).setTitle(title).setMessage(message).setPositiveButton("OK", null).show();
    }
    public interface RouteCallback {
        void onRouteCalculated(Route route);
    }
    private GeoBox calculateRouteGeoBox(Route route) {
        // Tomamos los puntos de inicio y fin de la ruta
        List < GeoCoordinates > coordinates = route.getGeometry().vertices;
        double minLat = Double.MAX_VALUE, maxLat = -Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE, maxLon = -Double.MAX_VALUE;
        // Iteramos sobre los puntos de inicio y fin de la ruta
        for(GeoCoordinates coord: coordinates) {
            if(coord.latitude < minLat) {
                minLat = coord.latitude;
            }
            if(coord.latitude > maxLat) {
                maxLat = coord.latitude;
            }
            if(coord.longitude < minLon) {
                minLon = coord.longitude;
            }
            if(coord.longitude > maxLon) {
                maxLon = coord.longitude;
            }
        }
        // Añadimos un margen (ajusta el valor según tus necesidades)
        double margin = 0.2; // Ejemplo: 0.01 grados
        minLat -= margin;
        maxLat += margin;
        minLon -= margin;
        maxLon += margin;
        // Devolvemos el GeoBox calculado
        return new GeoBox(new GeoCoordinates(minLat, minLon), new GeoCoordinates(maxLat, maxLon));
    }
    private GeoCoordinates calculateGeoBoxCenter(Route route) {
        // Tomamos los puntos de inicio y fin de la ruta
        List < GeoCoordinates > coordinates = route.getGeometry().vertices;
        double minLat = Double.MAX_VALUE, maxLat = -Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE, maxLon = -Double.MAX_VALUE;
        // Iteramos sobre los puntos de inicio y fin de la ruta
        for(GeoCoordinates coord: coordinates) {
            if(coord.latitude < minLat) {
                minLat = coord.latitude;
            }
            if(coord.latitude > maxLat) {
                maxLat = coord.latitude;
            }
            if(coord.longitude < minLon) {
                minLon = coord.longitude;
            }
            if(coord.longitude > maxLon) {
                maxLon = coord.longitude;
            }
        }
        // Añadimos un margen (ajusta el valor según tus necesidades)
        double margin = 0.2; // Ejemplo: 0.01 grados
        minLat -= margin;
        maxLat += margin;
        minLon -= margin;
        maxLon += margin;
        // Calcular el centro del GeoBox
        double centerLat = (minLat + maxLat) / 2;
        double centerLon = (minLon + maxLon) / 2;
        // Devolver el centro del GeoBox
        return new GeoCoordinates(centerLat, centerLon);
    }
    private double adjustZoomLevel(Route route) {
        // Tomamos los puntos de inicio y fin de la ruta
        List < GeoCoordinates > coordinates = route.getGeometry().vertices;
        double minLat = Double.MAX_VALUE, maxLat = -Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE, maxLon = -Double.MAX_VALUE;
        // Iteramos sobre los puntos de inicio y fin de la ruta
        for(GeoCoordinates coord: coordinates) {
            if(coord.latitude < minLat) {
                minLat = coord.latitude;
            }
            if(coord.latitude > maxLat) {
                maxLat = coord.latitude;
            }
            if(coord.longitude < minLon) {
                minLon = coord.longitude;
            }
            if(coord.longitude > maxLon) {
                maxLon = coord.longitude;
            }
        }
        // Añadimos un margen (ajusta el valor según tus necesidades)
        double margin = 0.2; // Ejemplo: 0.01 grados
        minLat -= margin;
        maxLat += margin;
        minLon -= margin;
        maxLon += margin;
        // Ejemplo de cálculo basado en la latitud y longitud del GeoBox
        double latDiff = maxLat - minLat;
        double lonDiff = maxLon - minLon;
        // La siguiente línea es un ejemplo y necesitarás ajustarla según cómo tu API maneje el zoom
        double maxDiff = Math.max(latDiff, lonDiff);
        // Calcular el nivel de zoom basado en el tamaño del GeoBox
        double zoomLevel = (Math.log(360 / maxDiff) / Math.log(2)) - 0.5;
        // Suponiendo que tu API de mapas tiene un método para ajustar el nivel de zoom basado en una GeoBox
        return zoomLevel;
    }
    // Método para animar el zoom suavemente
    private void animateZoom(double targetZoomLevel) {
        // Toma el nivel de zoom actual
        double currentZoomLevel = mainActivity.mapView.getCamera().getState().zoomLevel;
        // Genera una animación de valor para el nivel de zoom del actual al que deberia llegar segun la animacion
        ValueAnimator animator = ValueAnimator.ofFloat((float) currentZoomLevel, (float) targetZoomLevel);
        // Establece la duración de la animación en milisegundos
        animator.setDuration(zoomAnimationDuration);
        // Añadimos un oyente en la animación para actualizar del nivel de zoom
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                // Toma el valor actual de la animación
                float animatedValue = (float) valueAnimator.getAnimatedValue();
                // Establece el nuevo nivel de zoom
                mainActivity.mapView.getCamera().setDistanceToTarget(animatedValue);
            }
        });
        // Inicia la animación
        animator.start();
    }
}