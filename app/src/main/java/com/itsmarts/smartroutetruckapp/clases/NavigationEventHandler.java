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

package com.itsmarts.smartroutetruckapp.clases;

import static android.view.View.VISIBLE;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.LanguageCode;
import com.here.sdk.core.UnitSystem;
import com.here.sdk.mapview.MapMeasure;
import com.here.sdk.mapview.MapPolygon;
import com.here.sdk.navigation.AspectRatio;
import com.here.sdk.navigation.BorderCrossingWarning;
import com.here.sdk.navigation.BorderCrossingWarningListener;
import com.here.sdk.navigation.BorderCrossingWarningOptions;
import com.here.sdk.navigation.DangerZoneWarning;
import com.here.sdk.navigation.DangerZoneWarningListener;
import com.here.sdk.navigation.DangerZoneWarningOptions;
import com.here.sdk.navigation.DimensionRestrictionType;
import com.here.sdk.navigation.DistanceType;
import com.here.sdk.navigation.EventText;
import com.here.sdk.navigation.EventTextListener;
import com.here.sdk.navigation.JunctionViewLaneAssistance;
import com.here.sdk.navigation.JunctionViewLaneAssistanceListener;
import com.here.sdk.navigation.Lane;
import com.here.sdk.navigation.LaneAccess;
import com.here.sdk.navigation.LaneDirectionCategory;
import com.here.sdk.navigation.LaneRecommendationState;
import com.here.sdk.navigation.LaneType;
import com.here.sdk.navigation.ManeuverNotificationOptions;
import com.here.sdk.navigation.ManeuverProgress;
import com.here.sdk.navigation.ManeuverViewLaneAssistance;
import com.here.sdk.navigation.ManeuverViewLaneAssistanceListener;
import com.here.sdk.navigation.MapMatchedLocation;
import com.here.sdk.navigation.Milestone;
import com.here.sdk.navigation.MilestoneStatus;
import com.here.sdk.navigation.MilestoneStatusListener;
import com.here.sdk.navigation.NavigableLocation;
import com.here.sdk.navigation.NavigableLocationListener;
import com.here.sdk.navigation.RealisticViewVectorImage;
import com.here.sdk.navigation.RealisticViewWarning;
import com.here.sdk.navigation.RealisticViewWarningListener;
import com.here.sdk.navigation.RealisticViewWarningOptions;
import com.here.sdk.navigation.RoadAttributes;
import com.here.sdk.navigation.RoadAttributesListener;
import com.here.sdk.navigation.RoadSignVehicleType;
import com.here.sdk.navigation.RoadSignWarning;
import com.here.sdk.navigation.RoadSignWarningListener;
import com.here.sdk.navigation.RoadSignWarningOptions;
import com.here.sdk.navigation.RoadTextsListener;
import com.here.sdk.navigation.RouteDeviation;
import com.here.sdk.navigation.RouteDeviationListener;
import com.here.sdk.navigation.RouteProgress;
import com.here.sdk.navigation.RouteProgressListener;
import com.here.sdk.navigation.SafetyCameraWarning;
import com.here.sdk.navigation.SafetyCameraWarningListener;
import com.here.sdk.navigation.SafetyCameraWarningOptions;
import com.here.sdk.navigation.SchoolZoneWarning;
import com.here.sdk.navigation.SchoolZoneWarningListener;
import com.here.sdk.navigation.SchoolZoneWarningOptions;
import com.here.sdk.navigation.SectionProgress;
import com.here.sdk.navigation.SpeedLimit;
import com.here.sdk.navigation.SpeedLimitListener;
import com.here.sdk.navigation.SpeedLimitOffset;
import com.here.sdk.navigation.SpeedWarningListener;
import com.here.sdk.navigation.SpeedWarningOptions;
import com.here.sdk.navigation.SpeedWarningStatus;
import com.here.sdk.navigation.TextNotificationType;
import com.here.sdk.navigation.TollBooth;
import com.here.sdk.navigation.TollBoothLane;
import com.here.sdk.navigation.TollCollectionMethod;
import com.here.sdk.navigation.TollStop;
import com.here.sdk.navigation.TollStopWarningListener;
import com.here.sdk.navigation.TruckRestrictionWarning;
import com.here.sdk.navigation.TruckRestrictionsWarningListener;
import com.here.sdk.navigation.VisualNavigator;
import com.here.sdk.navigation.WeightRestrictionType;
import com.here.sdk.routing.Maneuver;
import com.here.sdk.routing.ManeuverAction;
import com.here.sdk.routing.PaymentMethod;
import com.here.sdk.routing.RoadTexts;
import com.here.sdk.routing.RoadType;
import com.here.sdk.routing.Route;
import com.here.sdk.trafficawarenavigation.DynamicRoutingEngine;
import com.here.sdk.transport.GeneralVehicleSpeedLimits;
import com.itsmarts.smartroutetruckapp.MainActivity;
import com.itsmarts.smartroutetruckapp.modelos.PointWithId;
import com.itsmarts.smartroutetruckapp.modelos.PolygonWithId;
import com.itsmarts.smartroutetruckapp.helpers.Distances;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NavigationEventHandler {

    private static final String TAG = NavigationEventHandler.class.getName();

    private final Context context;
    private int previousManeuverIndex = -1;
    private MapMatchedLocation lastMapMatchedLocation;
    private final VoiceAssistant voiceAssistant;
    private final TextView messageView;
    private SpeedUpdateListener speedUpdateListener;
    private DestinationDistanceListener destinationDistanceListener;
    private DestinationReachedListener destinationReachedListener;
    private MainActivity mainActivity;
    private GeoCoordinates currentGeoCoordinates, lastGeoCoordinatesOnRoute;
    private int id_punto_control = 0;
    private boolean hasNotifiedDeviation = false;
    private boolean hasNotifiedCheckpoint = false;
    private boolean hasNotifiedForbiddenZoneEarly = false;
    private Handler handler = new Handler();
    private Runnable resetFlagsRunnable = new Runnable() {
        @Override
        public void run() {
            hasNotifiedDeviation = false;
            hasNotifiedCheckpoint = false;
            hasNotifiedForbiddenZoneEarly = false;
        }
    };

    public NavigationEventHandler(MainActivity mainActivity, TextView messageView) {
        this.context = mainActivity.getApplicationContext();
        this.mainActivity = mainActivity;
        this.messageView = messageView;

        voiceAssistant = new VoiceAssistant(context);
    }

    public void setupListeners(VisualNavigator visualNavigator, DynamicRoutingEngine dynamicRoutingEngine) {

        setupSpeedWarnings(visualNavigator);
        setupVoiceGuidance(visualNavigator);

        visualNavigator.setRouteProgressListener(new RouteProgressListener() {
            @Override
            public void onRouteProgressUpdated(@NonNull RouteProgress routeProgress) {
                List<SectionProgress> sectionProgressList = routeProgress.sectionProgress;
                SectionProgress lastSectionProgress = sectionProgressList.get(sectionProgressList.size() - 1);
                int distanceToDestination = (int) lastSectionProgress.remainingDistanceInMeters;
                int timeRemaining = (int) lastSectionProgress.remainingDuration.getSeconds();
                Log.d(TAG, "Distance to destination in meters: " + lastSectionProgress.remainingDistanceInMeters);
                Log.d(TAG, "Traffic delay ahead in seconds: " + lastSectionProgress.remainingDuration.getSeconds());

                if (destinationDistanceListener != null) {
                    destinationDistanceListener.onDestinationInfoUpdated(distanceToDestination, timeRemaining);
                }

                List<ManeuverProgress> nextManeuverList = routeProgress.maneuverProgress;

                ManeuverProgress nextManeuverProgress = nextManeuverList.get(0);
                if (nextManeuverProgress == null) {
                    Log.d(TAG, "No next maneuver available.");
                    return;
                }

                int nextManeuverIndex = nextManeuverProgress.maneuverIndex;
                Maneuver nextManeuver = visualNavigator.getManeuver(nextManeuverIndex);
                if (nextManeuver == null) {
                    return;
                }

                ManeuverAction action = nextManeuver.getAction();
                String roadName = getRoadName(nextManeuver);
                String spanishAction = translateActionToSpanish(action);
                String logMessage = spanishAction + " en " + roadName + " en " + nextManeuverProgress.remainingDistanceInMeters + " metros.";

                Double turnAngle = nextManeuver.getTurnAngleInDegrees();
                if (turnAngle != null) {
                    if (turnAngle > 10) {
                        Log.d(TAG, "At the next maneuver: Make a right turn of " + turnAngle + " degrees.");
                    } else if (turnAngle < -10) {
                        Log.d(TAG, "At the next maneuver: Make a left turn of " + turnAngle + " degrees.");
                    } else {
                        Log.d(TAG, "At the next maneuver: Go straight.");
                    }
                }

                Double roundaboutAngle = nextManeuver.getRoundaboutAngleInDegrees();
                if (roundaboutAngle != null) {
                    Log.d(TAG, "At the next maneuver: Follow the roundabout for " +
                            roundaboutAngle + " degrees to reach the exit.");
                }

                if (previousManeuverIndex != nextManeuverIndex) {
                    messageView.setText(logMessage);
                } else {
                    messageView.setText(logMessage);
                }

                previousManeuverIndex = nextManeuverIndex;

                if (lastMapMatchedLocation != null) {
                    dynamicRoutingEngine.updateCurrentLocation(lastMapMatchedLocation, routeProgress.sectionIndex);
                }
            }
        });

        visualNavigator.setDestinationReachedListener(new com.here.sdk.navigation.DestinationReachedListener() {
            @Override
            public void onDestinationReached() {
                //mainActivity.btnMomentoViaje.setVisibility(View.GONE);
                String message = "Ha llegado a su destino";
                messageView.setText(message);
                //mainActivity.dbHelper.updateStatusRoute(mainActivity.ruta.id,2);
                //mainActivity.ruta.setStatus(2);
                if (destinationReachedListener != null) {
                    destinationReachedListener.onDestinationReached();
                }
            }
        });

        visualNavigator.setMilestoneStatusListener(new MilestoneStatusListener() {
            @Override
            public void onMilestoneStatusUpdated(@NonNull Milestone milestone, @NonNull MilestoneStatus milestoneStatus) {
                if (milestone.waypointIndex != null && milestoneStatus == MilestoneStatus.REACHED) {
                    Log.d(TAG, "A user-defined waypoint was reached, index of waypoint: " + milestone.waypointIndex);
                    Log.d(TAG,"Original coordinates: " + milestone.originalCoordinates);
                }
                else if (milestone.waypointIndex != null && milestoneStatus == MilestoneStatus.MISSED) {
                    Log.d(TAG, "A user-defined waypoint was missed, index of waypoint: " + milestone.waypointIndex);
                    Log.d(TAG,"Original coordinates: " + milestone.originalCoordinates);
                }
                else if (milestone.waypointIndex == null && milestoneStatus == MilestoneStatus.REACHED) {
                    Log.d(TAG, "A system-defined waypoint was reached at: " + milestone.mapMatchedCoordinates);
                }
                else if (milestone.waypointIndex == null && milestoneStatus == MilestoneStatus.MISSED) {
                    Log.d(TAG, "A system-defined waypoint was missed at: " + milestone.mapMatchedCoordinates);
                }
            }
        });

        visualNavigator.setSafetyCameraWarningListener(new SafetyCameraWarningListener() {
            @Override
            public void onSafetyCameraWarningUpdated(@NonNull SafetyCameraWarning safetyCameraWarning) {
                if (safetyCameraWarning.distanceType == DistanceType.AHEAD) {
                    Log.d(TAG,"Safety camera warning " + safetyCameraWarning.type.name() + " ahead in: "
                            + safetyCameraWarning.distanceToCameraInMeters + "with speed limit ="
                            + safetyCameraWarning.speedLimitInMetersPerSecond + "m/s");
                } else if (safetyCameraWarning.distanceType == DistanceType.PASSED) {
                    Log.d(TAG,"Safety camera warning " + safetyCameraWarning.type.name() + " passed: "
                            + safetyCameraWarning.distanceToCameraInMeters + "with speed limit ="
                            + safetyCameraWarning.speedLimitInMetersPerSecond + "m/s");
                } else if (safetyCameraWarning.distanceType == DistanceType.REACHED) {
                    Log.d(TAG,"Safety camera warning " + safetyCameraWarning.type.name() + " reached at: "
                            + safetyCameraWarning.distanceToCameraInMeters + "with speed limit ="
                            + safetyCameraWarning.speedLimitInMetersPerSecond + "m/s");
                }
            }
        });

        SafetyCameraWarningOptions safetyCameraWarningOptions = new SafetyCameraWarningOptions();
        safetyCameraWarningOptions.highwayWarningDistanceInMeters = 1600;
        safetyCameraWarningOptions.ruralWarningDistanceInMeters = 800;
        safetyCameraWarningOptions.urbanWarningDistanceInMeters = 600;
        visualNavigator.setSafetyCameraWarningOptions(safetyCameraWarningOptions);

        visualNavigator.setSpeedWarningListener(new SpeedWarningListener() {
            @Override
            public void onSpeedWarningStatusChanged(@NonNull SpeedWarningStatus speedWarningStatus) {
                if (speedWarningStatus == SpeedWarningStatus.SPEED_LIMIT_EXCEEDED) {
                    Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone ringtone = RingtoneManager.getRingtone(context, ringtoneUri);
                    ringtone.play();
                }

                if (speedWarningStatus == SpeedWarningStatus.SPEED_LIMIT_RESTORED) {
                    Log.d(TAG, "Driver is again slower than current speed limit (plus an optional offset).");
                }
            }
        });

        visualNavigator.setSpeedLimitListener(new SpeedLimitListener() {
            @Override
            public void onSpeedLimitUpdated(@NonNull SpeedLimit speedLimit) {
                Double currentSpeedLimit = getCurrentSpeedLimit(speedLimit);

                if (currentSpeedLimit == null) {
                    Log.d(TAG, "Warning: Speed limits unknown, data could not be retrieved.");
                } else if (currentSpeedLimit == 0) {
                    Log.d(TAG, "No speed limits on this road! Drive as fast as you feel safe ...");
                } else {
                    Log.d(TAG, "Current speed limit (m/s):" + currentSpeedLimit);
                }
            }
        });

        visualNavigator.setNavigableLocationListener(new NavigableLocationListener() {
            @Override
            public void onNavigableLocationUpdated(@NonNull NavigableLocation currentNavigableLocation) {

                if(mainActivity.currentGeoCoordinates==null){
                    mainActivity.currentGeoCoordinates = currentNavigableLocation.originalLocation.coordinates;
                    double distanceInMeters = 1000 * 0.5;
                    MapMeasure mapMeasureZoom = new MapMeasure(MapMeasure.Kind.DISTANCE, distanceInMeters);
                    mainActivity.mapView.getCamera().lookAt(currentNavigableLocation.originalLocation.coordinates, mapMeasureZoom);
                } else if (mainActivity.currentGeoCoordinates!=currentNavigableLocation.originalLocation.coordinates) {
                    mainActivity.currentGeoCoordinates = currentNavigableLocation.originalLocation.coordinates;
                }

                if (mainActivity.rotateAnimation != null) {
                    //mainActivity.loading.clearAnimation();
                    mainActivity.rotateAnimation.cancel();
                    mainActivity.rotateAnimation = null;
                }

                if (!mainActivity.animacionEjecutada) {
                    mainActivity.hideable_content.setVisibility(View.GONE);
                    mainActivity.home_content.setVisibility(View.VISIBLE);
                    mainActivity.toolbar.setVisibility(View.VISIBLE);
                    mainActivity.mapView.setVisibility(VISIBLE);
                    mainActivity.mapView.startAnimation(mainActivity.cargaAnimacion);
                    mainActivity.toolbar.startAnimation(mainActivity.cargaAnimacion);
                    mainActivity.trackCamara.startAnimation(mainActivity.cargaAnimacion);
                    mainActivity.txtNavegacion.startAnimation(mainActivity.cargaAnimacion);
                    mainActivity.speedTextView.startAnimation(mainActivity.cargaAnimacion);
                    mainActivity.imgVelocidad.startAnimation(mainActivity.cargaAnimacion);
                    mainActivity.llMapas.startAnimation(mainActivity.cargaAnimacion);
                    mainActivity.animacionEjecutada = true;
                }

                lastMapMatchedLocation = currentNavigableLocation.mapMatchedLocation;

                if (lastMapMatchedLocation == null) {
                    Log.d(TAG, "The currentNavigableLocation could not be map-matched. Are you off-road?");
                    return;
                }

                if (lastMapMatchedLocation.isDrivingInTheWrongWay) {
                    Log.d(TAG,"User is driving in the wrong direction of the route.");
                }

                if (mainActivity.ruta != null) {
                    if(!hasNotifiedDeviation){
                        double distanceToPolyline = Distances.distanceToPolyline(lastMapMatchedLocation.coordinates, mainActivity.ruta.polyline.getGeometry());
                        if (distanceToPolyline > 100) {
                            NotificationHelper.showNotification(
                                    context,
                                    "Desviación de ruta",
                                    String.format("Te has desviado, estás a %.2f metros de la ruta planificada.", distanceToPolyline)
                            );
                            hasNotifiedDeviation = true;
                            // Iniciar el temporizador al comienzo
                            handler.postDelayed(resetFlagsRunnable, 120000);
                        }
                    }
                    if(!hasNotifiedCheckpoint){
                        for (PointWithId pointWithId : mainActivity.controlPointsExample.pointsWithIds) {
                            for (int i = 0; i < mainActivity.controlPointsExample.pointsWithIds.size(); i++) {
                                if(pointWithId.visibility && pointWithId.status){
                                    if (lastMapMatchedLocation.coordinates.distanceTo(pointWithId.mapMarker.getCoordinates())<100 && id_punto_control!=pointWithId.id) {
                                        NotificationHelper.showNotification(
                                                context,
                                                "Punto de Control",
                                                "Has pasado por el punto de control "+pointWithId.name+"."
                                        );
                                        hasNotifiedCheckpoint=true;
                                        id_punto_control=pointWithId.id;
                                        // Iniciar el temporizador al comienzo
                                        handler.postDelayed(resetFlagsRunnable, 120000);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    /*boolean validacionZona = false;
                    for (PolygonWithId polygonWithId : mainActivity.avoidZonesExample.polygonWithIds) {
                        for (int i = 0; i < mainActivity.controlPointsExample.pointsWithIds.size(); i++) {
                            if(polygonWithId.visibility && polygonWithId.status && !polygonWithId.peligrosa){
                                double minLat = 0;
                                double maxLat = 0;
                                double minLng = 0;
                                double maxLng = 0;
                                for (GeoCoordinates vertex : polygonWithId.polygon.getGeometry().vertices) {
                                    minLat = Math.min(minLat, vertex.latitude);
                                    maxLat = Math.max(maxLat, vertex.latitude);
                                    minLng = Math.min(minLng, vertex.longitude);
                                    maxLng = Math.max(maxLng, vertex.longitude);
                                }
                                // Check if user's location is within the bounding box
                                if (lastMapMatchedLocation.coordinates.latitude >= minLat && lastMapMatchedLocation.coordinates.latitude <= maxLat &&
                                        lastMapMatchedLocation.coordinates.longitude >= minLng && lastMapMatchedLocation.coordinates.longitude <= maxLng) {
                                    NotificationHelper.showNotification(
                                            context,
                                            "Zona Prohibida",
                                            "Has entrado en la zona prohibida "+polygonWithId.name+"."
                                    );
                                    mainActivity.recalculateRouteButton.setVisibility(View.VISIBLE);
                                    mainActivity.recalculateRouteButton.setEnabled(false);
                                    mainActivity.recalculateRouteButton.setText("Has entrado en la zona prohibida "+polygonWithId.name+".");
                                    validacionZona = true;
                                    break;
                                }
                            }
                        }
                    }
                    for (PolygonWithId polygonWithId : mainActivity.avoidZonesExample.polygonWithIds) {
                        for (int i = 0; i < mainActivity.controlPointsExample.pointsWithIds.size(); i++) {
                            if(polygonWithId.visibility && polygonWithId.status && polygonWithId.peligrosa){
                                double minLat = 0;
                                double maxLat = 0;
                                double minLng = 0;
                                double maxLng = 0;
                                for (GeoCoordinates vertex : polygonWithId.polygon.getGeometry().vertices) {
                                    minLat = Math.min(minLat, vertex.latitude);
                                    maxLat = Math.max(maxLat, vertex.latitude);
                                    minLng = Math.min(minLng, vertex.longitude);
                                    maxLng = Math.max(maxLng, vertex.longitude);
                                }
                                // Check if user's location is within the bounding box
                                if (lastMapMatchedLocation.coordinates.latitude >= minLat && lastMapMatchedLocation.coordinates.latitude <= maxLat &&
                                        lastMapMatchedLocation.coordinates.longitude >= minLng && lastMapMatchedLocation.coordinates.longitude <= maxLng) {
                                    NotificationHelper.showNotification(
                                            context,
                                            "Zona Peligrosa",
                                            "Has entrado en la zona peligrosa "+polygonWithId.name+"."
                                    );
                                    mainActivity.recalculateRouteButton.setVisibility(View.VISIBLE);
                                    mainActivity.recalculateRouteButton.setEnabled(false);
                                    mainActivity.recalculateRouteButton.setText("Has entrado en la zona peligrosa "+polygonWithId.name+".");
                                    validacionZona = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (!validacionZona) {
                        mainActivity.recalculateRouteButton.setVisibility(View.GONE);
                    }else{
                        // Iniciar el temporizador al comienzo
                        handler.postDelayed(resetFlagsRunnable, 120000);
                    }*/
                }

                Double speed = currentNavigableLocation.originalLocation.speedInMetersPerSecond;
                Double accuracy = currentNavigableLocation.originalLocation.speedAccuracyInMetersPerSecond;
                Log.d(TAG, "Driving speed (m/s): " + speed + "plus/minus an accuracy of: " +accuracy);

                if (speedUpdateListener != null && speed != null) {
                    speedUpdateListener.onSpeedUpdated(speed);
                }
            }
        });

        visualNavigator.setRouteDeviationListener(new RouteDeviationListener() {
            @Override
            public void onRouteDeviation(@NonNull RouteDeviation routeDeviation) {
                Route route = visualNavigator.getRoute();
                if (route == null) {
                    return;
                }

                MapMatchedLocation currentMapMatchedLocation = routeDeviation.currentLocation.mapMatchedLocation;
                currentGeoCoordinates = currentMapMatchedLocation == null ?
                        routeDeviation.currentLocation.originalLocation.coordinates : currentMapMatchedLocation.coordinates;
                Log.d(TAG, "Coordenadas Actuales " + currentMapMatchedLocation);

                if (routeDeviation.lastLocationOnRoute != null) {
                    MapMatchedLocation lastMapMatchedLocationOnRoute = routeDeviation.lastLocationOnRoute.mapMatchedLocation;
                    lastGeoCoordinatesOnRoute = lastMapMatchedLocationOnRoute == null ?
                            routeDeviation.lastLocationOnRoute.originalLocation.coordinates : lastMapMatchedLocationOnRoute.coordinates;
                    Log.d(TAG, "Coordenadas Actuales finales " + lastMapMatchedLocationOnRoute);
                } else {
                    Log.d(TAG, "User was never following the route. So, we take the start of the route instead.");
                    lastGeoCoordinatesOnRoute = route.getSections().get(0).getDeparturePlace().originalCoordinates;
                }

                int distanceInMeters = (int) currentGeoCoordinates.distanceTo(lastGeoCoordinatesOnRoute);
                Log.d(TAG, "RouteDeviation in meters is " + distanceInMeters);

                if (distanceInMeters > 50) {
                    mainActivity.recalculateRouteButton.setVisibility(View.VISIBLE);
                    mainActivity.recalculateRouteButton.setEnabled(true);
                    mainActivity.recalculateRouteButton.setText("Desviación de: " + distanceInMeters + "m. Recalcular ruta");
                    if (mainActivity.destinationGeoCoordinates == null) {
                        mainActivity.destinationGeoCoordinates = mainActivity.navigationExample.getVisualNavigator().getRoute().getSections().get(0).getArrivalPlace().mapMatchedCoordinates;
                    }

                    if (distanceInMeters >= 250) {
                        mainActivity.recalculateRouteButton.setVisibility(View.GONE);
                        mainActivity.recalculateRoute();
                    }
                } else {
                    mainActivity.recalculateRouteButton.setVisibility(View.GONE);
                }
            }
        });

        visualNavigator.setEventTextListener(new EventTextListener() {
            @Override
            public void onEventTextUpdated(@NonNull EventText eventText) {
                voiceAssistant.speak(eventText.text);
                if (eventText.type == TextNotificationType.MANEUVER && eventText.maneuverNotificationDetails != null) {
                    Maneuver maneuver = eventText.maneuverNotificationDetails.maneuver;
                }
            }
        });

        visualNavigator.setManeuverViewLaneAssistanceListener(new ManeuverViewLaneAssistanceListener() {
            @Override
            public void onLaneAssistanceUpdated(@NonNull ManeuverViewLaneAssistance maneuverViewLaneAssistance) {
                List<Lane> lanes = maneuverViewLaneAssistance.lanesForNextManeuver;
                logLaneRecommendations(lanes);

                List<Lane> nextLanes = maneuverViewLaneAssistance.lanesForNextNextManeuver;
                if (!nextLanes.isEmpty()) {
                    Log.d(TAG, "Attention, the next next maneuver is very close.");
                    Log.d(TAG, "Please take the following lane(s) after the next maneuver: ");
                    logLaneRecommendations(nextLanes);
                }
            }
        });

        visualNavigator.setJunctionViewLaneAssistanceListener(new JunctionViewLaneAssistanceListener() {
            @Override
            public void onLaneAssistanceUpdated(@NonNull JunctionViewLaneAssistance junctionViewLaneAssistance) {
                List<Lane> lanes = junctionViewLaneAssistance.lanesForNextJunction;
                if (lanes.isEmpty()) {
                    Log.d(TAG, "You have passed the complex junction.");
                } else {
                    Log.d(TAG, "Attention, a complex junction is ahead.");
                    logLaneRecommendations(lanes);
                }
            }
        });

        visualNavigator.setRoadAttributesListener(new RoadAttributesListener() {
            @Override
            public void onRoadAttributesUpdated(@NonNull RoadAttributes roadAttributes) {

                Log.d(TAG, "Received road attributes update.");

                if (roadAttributes.isBridge) {
                    Log.d(TAG, "Road attributes: This is a bridge.");
                }
                if (roadAttributes.isControlledAccess) {
                    Log.d(TAG, "Road attributes: This is a controlled access road.");
                }
                if (roadAttributes.isDirtRoad) {
                    Log.d(TAG, "Road attributes: This is a dirt road.");
                }
                if (roadAttributes.isDividedRoad) {
                    Log.d(TAG, "Road attributes: This is a divided road.");
                }
                if (roadAttributes.isNoThrough) {
                    Log.d(TAG, "Road attributes: This is a no through road.");
                }
                if (roadAttributes.isPrivate) {
                    Log.d(TAG, "Road attributes: This is a private road.");
                }
                if (roadAttributes.isRamp) {
                    Log.d(TAG, "Road attributes: This is a ramp.");
                }
                if (roadAttributes.isRightDrivingSide) {
                    Log.d(TAG, "Road attributes: isRightDrivingSide = " + roadAttributes.isRightDrivingSide);
                }
                if (roadAttributes.isRoundabout) {
                    Log.d(TAG, "Road attributes: This is a roundabout.");
                }
                if (roadAttributes.isTollway) {
                    Log.d(TAG, "Road attributes change: This is a road with toll costs.");
                }
                if (roadAttributes.isTunnel) {
                    Log.d(TAG, "Road attributes: This is a tunnel.");
                }
            }
        });

        RoadSignWarningOptions roadSignWarningOptions = new RoadSignWarningOptions();
        roadSignWarningOptions.vehicleTypesFilter = Arrays.asList(RoadSignVehicleType.TRUCKS, RoadSignVehicleType.HEAVY_TRUCKS);
        roadSignWarningOptions.highwayWarningDistanceInMeters = 1600;
        roadSignWarningOptions.ruralWarningDistanceInMeters = 800;
        roadSignWarningOptions.urbanWarningDistanceInMeters = 600;
        visualNavigator.setRoadSignWarningOptions(roadSignWarningOptions);

        visualNavigator.setRoadSignWarningListener(new RoadSignWarningListener() {
            @Override
            public void onRoadSignWarningUpdated(@NonNull RoadSignWarning roadSignWarning) {
                Log.d(TAG, "Road sign distance (m): " + roadSignWarning.distanceToRoadSignInMeters);
                Log.d(TAG, "Road sign type: " + roadSignWarning.type.name());

                if (roadSignWarning.signValue != null) {
                    Log.d(TAG, "Road sign text: " + roadSignWarning.signValue.text);
                }
            }
        });
        visualNavigator.setTruckRestrictionsWarningListener(new TruckRestrictionsWarningListener() {
            @Override
            public void onTruckRestrictionsWarningUpdated(@NonNull List<TruckRestrictionWarning> list) {
                for (TruckRestrictionWarning truckRestrictionWarning : list) {
                    if (truckRestrictionWarning.distanceType == DistanceType.AHEAD) {
                        Log.d(TAG, "TruckRestrictionWarning ahead in: "+ truckRestrictionWarning.distanceInMeters + " meters.");
                        if (truckRestrictionWarning.timeRule != null && !truckRestrictionWarning.timeRule.appliesTo(new Date())) {
                            Log.d(TAG, "Note that this truck restriction warning currently does not apply.");
                        }
                    } else if (truckRestrictionWarning.distanceType == DistanceType.REACHED) {
                        Log.d(TAG, "A restriction has been reached.");
                    } else if (truckRestrictionWarning.distanceType == DistanceType.PASSED) {
                        Log.d(TAG, "A restriction just passed.");
                    }

                    if (truckRestrictionWarning.weightRestriction != null) {
                        WeightRestrictionType type = truckRestrictionWarning.weightRestriction.type;
                        int value = truckRestrictionWarning.weightRestriction.valueInKilograms;
                        Log.d(TAG, "TruckRestriction for weight (kg): " + type.name() + ": " + value);
                    } else if (truckRestrictionWarning.dimensionRestriction != null) {
                        DimensionRestrictionType type = truckRestrictionWarning.dimensionRestriction.type;
                        int value = truckRestrictionWarning.dimensionRestriction.valueInCentimeters;
                        Log.d(TAG, "TruckRestriction for dimension: " + type.name() + ": " + value);
                    } else {
                        Log.d(TAG, "TruckRestriction: General restriction - no trucks allowed.");
                    }
                }
            }
        });

        visualNavigator.setSchoolZoneWarningListener(new SchoolZoneWarningListener() {
            @Override
            public void onSchoolZoneWarningUpdated(@NonNull List<SchoolZoneWarning> list) {
                for (SchoolZoneWarning schoolZoneWarning : list) {
                    if (schoolZoneWarning.distanceType == DistanceType.AHEAD) {
                        Log.d(TAG, "A school zone ahead in: " + schoolZoneWarning.distanceToSchoolZoneInMeters + " meters.");
                        Log.d(TAG, "Speed limit restriction for this school zone: " + schoolZoneWarning.speedLimitInMetersPerSecond + " m/s.");
                        if (schoolZoneWarning.timeRule != null && !schoolZoneWarning.timeRule.appliesTo(new Date())) {
                            Log.d(TAG, "Note that this school zone warning currently does not apply.");
                        }
                    } else if (schoolZoneWarning.distanceType == DistanceType.REACHED) {
                        Log.d(TAG, "A school zone has been reached.");
                    } else if (schoolZoneWarning.distanceType == DistanceType.PASSED) {
                        Log.d(TAG, "A school zone has been passed.");
                    }
                }
            }
        });

        SchoolZoneWarningOptions schoolZoneWarningOptions = new SchoolZoneWarningOptions();
        schoolZoneWarningOptions.filterOutInactiveTimeDependentWarnings = true;
        schoolZoneWarningOptions.warningDistanceInMeters = 150;
        visualNavigator.setSchoolZoneWarningOptions(schoolZoneWarningOptions);
        visualNavigator.setBorderCrossingWarningListener(new BorderCrossingWarningListener() {
            @Override
            public void onBorderCrossingWarningUpdated(@NonNull BorderCrossingWarning borderCrossingWarning) {
                if (borderCrossingWarning.distanceType == DistanceType.AHEAD) {
                    Log.d(TAG, "BorderCrossing: A border is ahead in: " + borderCrossingWarning.distanceToBorderCrossingInMeters + " meters.");
                    Log.d(TAG, "BorderCrossing: Type (such as country or state): " + borderCrossingWarning.type.name());
                    Log.d(TAG, "BorderCrossing: Country code: " + borderCrossingWarning.countryCode.name());
                    if (borderCrossingWarning.stateCode != null) {
                        Log.d(TAG, "BorderCrossing: State code: " + borderCrossingWarning.stateCode);
                    }

                    GeneralVehicleSpeedLimits generalVehicleSpeedLimits = borderCrossingWarning.speedLimits;
                    Log.d(TAG, "BorderCrossing: Speed limit in cities (m/s): " + generalVehicleSpeedLimits.maxSpeedUrbanInMetersPerSecond);
                    Log.d(TAG, "BorderCrossing: Speed limit outside cities (m/s): " + generalVehicleSpeedLimits.maxSpeedRuralInMetersPerSecond);
                    Log.d(TAG, "BorderCrossing: Speed limit on highways (m/s): " + generalVehicleSpeedLimits.maxSpeedHighwaysInMetersPerSecond);
                } else if (borderCrossingWarning.distanceType == DistanceType.PASSED) {
                    Log.d(TAG, "BorderCrossing: A border has been passed.");
                }
            }
        });

        BorderCrossingWarningOptions borderCrossingWarningOptions = new BorderCrossingWarningOptions();
        borderCrossingWarningOptions.filterOutStateBorderWarnings = true;
        borderCrossingWarningOptions.urbanWarningDistanceInMeters = 400;
        visualNavigator.setBorderCrossingWarningOptions(borderCrossingWarningOptions);


        visualNavigator.setDangerZoneWarningListener(new DangerZoneWarningListener() {
            @Override
            public void onDangerZoneWarningsUpdated(@NonNull DangerZoneWarning dangerZoneWarning) {
                if (dangerZoneWarning.distanceType == DistanceType.AHEAD) {
                    Log.d(TAG, "A danger zone ahead in: " + dangerZoneWarning.distanceInMeters + " meters.");
                    Log.d(TAG, "isZoneStart: " + dangerZoneWarning.isZoneStart);
                } else if (dangerZoneWarning.distanceType == DistanceType.REACHED) {
                    Log.d(TAG, "A danger zone has been reached. isZoneStart: " + dangerZoneWarning.isZoneStart);
                } else if (dangerZoneWarning.distanceType == DistanceType.PASSED) {
                    Log.d(TAG, "A danger zone has been passed.");
                }
            }
        });

        DangerZoneWarningOptions dangerZoneWarningOptions = new DangerZoneWarningOptions();
        dangerZoneWarningOptions.urbanWarningDistanceInMeters = 400;
        visualNavigator.setDangerZoneWarningOptions(dangerZoneWarningOptions);

        visualNavigator.setRoadTextsListener(new RoadTextsListener() {
            @Override
            public void onRoadTextsUpdated(@NonNull RoadTexts roadTexts) {
            }
        });

        RealisticViewWarningOptions realisticViewWarningOptions = new RealisticViewWarningOptions();
        realisticViewWarningOptions.aspectRatio = AspectRatio.ASPECT_RATIO_3_X_4;
        realisticViewWarningOptions.darkTheme = false;
        realisticViewWarningOptions.highwayWarningDistanceInMeters = 1600;
        realisticViewWarningOptions.ruralWarningDistanceInMeters = 800;
        realisticViewWarningOptions.urbanWarningDistanceInMeters = 600;
        visualNavigator.setRealisticViewWarningOptions(realisticViewWarningOptions);

        visualNavigator.setRealisticViewWarningListener(new RealisticViewWarningListener() {
            @Override
            public void onRealisticViewWarningUpdated(@NonNull RealisticViewWarning realisticViewWarning) {
                double distance = realisticViewWarning.distanceToRealisticViewInMeters;
                DistanceType distanceType = realisticViewWarning.distanceType;

                if (distanceType == DistanceType.AHEAD) {
                    Log.d(TAG, "A RealisticView ahead in: "+ distance + " meters.");
                } else if (distanceType == DistanceType.PASSED) {
                    Log.d(TAG, "A RealisticView just passed.");
                }

                RealisticViewVectorImage realisticView = realisticViewWarning.realisticViewVectorImage;
                if (realisticView == null) {
                    Log.d(TAG, "A RealisticView just passed. No SVG data delivered.");
                    return;
                }

                String signpostSvgImageContent = realisticView.signpostSvgImageContent;
                String junctionViewSvgImageContent = realisticView.junctionViewSvgImageContent;
                Log.d("signpostSvgImage", signpostSvgImageContent);
                Log.d("junctionViewSvgImage", junctionViewSvgImageContent);
            }
        });

        visualNavigator.setTollStopWarningListener(new TollStopWarningListener() {
            @Override
            public void onTollStopWarning(@NonNull TollStop tollStop) {
                List<TollBoothLane> lanes = tollStop.lanes;

                int laneNumber = 0;
                for (TollBoothLane tollBoothLane : lanes) {
                    logLaneAccess(laneNumber, tollBoothLane.access);
                    TollBooth tollBooth = tollBoothLane.booth;
                    List<TollCollectionMethod> tollCollectionMethods = tollBooth.tollCollectionMethods;
                    List<PaymentMethod> paymentMethods = tollBooth.paymentMethods;
                    for (TollCollectionMethod collectionMethod : tollCollectionMethods) {
                        Log.d(TAG,"This toll stop supports collection via: " + collectionMethod.name());
                    }
                    for (PaymentMethod paymentMethod : paymentMethods) {
                        Log.d(TAG,"This toll stop supports payment via: " + paymentMethod.name());
                    }
                }
            }
        });
    }

    private void setupSpeedWarnings(VisualNavigator visualNavigator) {
        SpeedLimitOffset speedLimitOffset = new SpeedLimitOffset();
        speedLimitOffset.lowSpeedOffsetInMetersPerSecond = 2;
        speedLimitOffset.highSpeedOffsetInMetersPerSecond = 4;
        speedLimitOffset.highSpeedBoundaryInMetersPerSecond = 25;

        visualNavigator.setSpeedWarningOptions(new SpeedWarningOptions(speedLimitOffset));
    }

    private void setupVoiceGuidance(VisualNavigator visualNavigator) {
        LanguageCode ttsLanguageCode = LanguageCode.ES_ES;
        visualNavigator.setManeuverNotificationOptions(new ManeuverNotificationOptions(ttsLanguageCode, UnitSystem.METRIC));
        Log.d(TAG, "LanguageCode for maneuver notifications: " + ttsLanguageCode);

        Locale locale = LanguageCodeConverter.getLocale(ttsLanguageCode);
        if (voiceAssistant.setLanguage(locale)) {
            Log.d(TAG, "TextToSpeech engine uses this language: " + locale);
        } else {
            Log.e(TAG, "TextToSpeech engine does not support this language: " + locale);
        }
    }

    private String getRoadName(Maneuver maneuver) {
        RoadTexts currentRoadTexts = maneuver.getRoadTexts();
        RoadTexts nextRoadTexts = maneuver.getNextRoadTexts();

        String currentRoadName = currentRoadTexts.names.getDefaultValue();
        String currentRoadNumber = currentRoadTexts.numbersWithDirection.getDefaultValue();
        String nextRoadName = nextRoadTexts.names.getDefaultValue();
        String nextRoadNumber = nextRoadTexts.numbersWithDirection.getDefaultValue();

        String roadName = nextRoadName == null ? nextRoadNumber : nextRoadName;

        if (maneuver.getNextRoadType() == RoadType.HIGHWAY) {
            roadName = nextRoadNumber == null ? nextRoadName : nextRoadNumber;
        }

        if (maneuver.getAction() == ManeuverAction.ARRIVE) {
            roadName = currentRoadName == null ? currentRoadNumber : currentRoadName;
        }

        if (roadName == null) {
            roadName = "";
        }

        return roadName;
    }

    private Double getCurrentSpeedLimit(SpeedLimit speedLimit) {
        Log.d(TAG,"speedLimitInMetersPerSecond: " + speedLimit.speedLimitInMetersPerSecond);

        Log.d(TAG,"schoolZoneSpeedLimitInMetersPerSecond: " + speedLimit.schoolZoneSpeedLimitInMetersPerSecond);

        Log.d(TAG,"timeDependentSpeedLimitInMetersPerSecond: " + speedLimit.timeDependentSpeedLimitInMetersPerSecond);

        Log.d(TAG,"advisorySpeedLimitInMetersPerSecond: " + speedLimit.advisorySpeedLimitInMetersPerSecond);
        Log.d(TAG,"fogSpeedLimitInMetersPerSecond: " + speedLimit.fogSpeedLimitInMetersPerSecond);
        Log.d(TAG,"rainSpeedLimitInMetersPerSecond: " + speedLimit.rainSpeedLimitInMetersPerSecond);
        Log.d(TAG,"snowSpeedLimitInMetersPerSecond: " + speedLimit.snowSpeedLimitInMetersPerSecond);
        return speedLimit.effectiveSpeedLimitInMetersPerSecond();
    }

    private void logLaneRecommendations(List<Lane> lanes) {
        int laneNumber = 0;
        for (Lane lane : lanes) {
            if (lane.recommendationState == LaneRecommendationState.RECOMMENDED) {
                Log.d(TAG,"Lane " + laneNumber + " leads to next maneuver, but not to the next next maneuver.");
            }
            if (lane.recommendationState == LaneRecommendationState.HIGHLY_RECOMMENDED) {
                Log.d(TAG,"Lane " + laneNumber + " leads to next maneuver and eventually to the next next maneuver.");
            }

            if (lane.recommendationState == LaneRecommendationState.NOT_RECOMMENDED) {
                Log.d(TAG,"Do not take lane " + laneNumber + " to follow the route.");
            }

            logLaneDetails(laneNumber, lane);

            laneNumber++;
        }
    }

    private void logLaneDetails(int laneNumber, Lane lane) {
        LaneDirectionCategory laneDirectionCategory = lane.directionCategory;
        Log.d(TAG,"Directions for lane " + laneNumber);
        Log.d(TAG,"laneDirectionCategory.straight: " + laneDirectionCategory.straight);
        Log.d(TAG,"laneDirectionCategory.slightlyLeft: " + laneDirectionCategory.slightlyLeft);
        Log.d(TAG,"laneDirectionCategory.quiteLeft: " + laneDirectionCategory.quiteLeft);
        Log.d(TAG,"laneDirectionCategory.hardLeft: " + laneDirectionCategory.hardLeft);
        Log.d(TAG,"laneDirectionCategory.uTurnLeft: " + laneDirectionCategory.uTurnLeft);
        Log.d(TAG,"laneDirectionCategory.slightlyRight: " + laneDirectionCategory.slightlyRight);
        Log.d(TAG,"laneDirectionCategory.quiteRight: " + laneDirectionCategory.quiteRight);
        Log.d(TAG,"laneDirectionCategory.hardRight: " + laneDirectionCategory.hardRight);
        Log.d(TAG,"laneDirectionCategory.uTurnRight: " + laneDirectionCategory.uTurnRight);

        LaneType laneType = lane.type;
        LaneAccess laneAccess = lane.access;
        logLaneAccess(laneNumber, laneAccess);
    }

    private void logLaneAccess(int laneNumber, LaneAccess laneAccess) {
        Log.d(TAG,"Lane access for lane " + laneNumber);
        Log.d(TAG,"Automobiles are allowed on this lane: " + laneAccess.automobiles);
        Log.d(TAG,"Buses are allowed on this lane: " + laneAccess.buses);
        Log.d(TAG,"Taxis are allowed on this lane: " + laneAccess.taxis);
        Log.d(TAG,"Carpools are allowed on this lane: " + laneAccess.carpools);
        Log.d(TAG,"Pedestrians are allowed on this lane: " + laneAccess.pedestrians);
        Log.d(TAG,"Trucks are allowed on this lane: " + laneAccess.trucks);
        Log.d(TAG,"ThroughTraffic is allowed on this lane: " + laneAccess.throughTraffic);
        Log.d(TAG,"DeliveryVehicles are allowed on this lane: " + laneAccess.deliveryVehicles);
        Log.d(TAG,"EmergencyVehicles are allowed on this lane: " + laneAccess.emergencyVehicles);
        Log.d(TAG,"Motorcycles are allowed on this lane: " + laneAccess.motorcycles);
    }

    public interface SpeedUpdateListener {
        void onSpeedUpdated(double speed);
    }

    public interface DestinationDistanceListener {
        void onDestinationInfoUpdated(int distanceInMeters, int timeInSeconds);
    }

    public interface DestinationReachedListener {
        void onDestinationReached();
    }

    public void setDestinationReachedListener(DestinationReachedListener listener) {
        this.destinationReachedListener = listener;
    }

    public void setDestinationDistanceListener(DestinationDistanceListener listener) {
        this.destinationDistanceListener = listener;
    }

    public void setSpeedUpdateListener(SpeedUpdateListener listener) {
        this.speedUpdateListener = listener;
    }

    public GeoCoordinates getCurrentGeoCoordinates() {
        return currentGeoCoordinates;
    }

    private String translateActionToSpanish(ManeuverAction action) {
        switch (action) {
            case RIGHT_TURN:
                return "Gire a la derecha";
            case LEFT_TURN:
                return "Gire a la izquierda";
            case DEPART:
                return "Salida";
            case RIGHT_ROUNDABOUT_ENTER:
                return "Entra a la rotonda por la derecha";
            case RIGHT_ROUNDABOUT_EXIT1:
                return "Toma la primera salida de la rotonda a la derecha";
            case RIGHT_ROUNDABOUT_EXIT2:
                return "Toma la segunda salida de la rotonda a la derecha";
            case RIGHT_ROUNDABOUT_EXIT3:
                return "Toma la tercera salida de la rotonda a la derecha";
            case RIGHT_ROUNDABOUT_EXIT4:
                return "Toma la cuarta salida de la rotonda a la derecha";
            case RIGHT_ROUNDABOUT_EXIT5:
                return "Toma la quinta salida de la rotonda a la derecha";
            case RIGHT_ROUNDABOUT_EXIT6:
                return "Toma la sexta salida de la rotonda a la derecha";
            case RIGHT_ROUNDABOUT_EXIT7:
                return "Toma la séptima salida de la rotonda a la derecha";
            case RIGHT_ROUNDABOUT_EXIT8:
                return "Toma la octava salida de la rotonda a la derecha";
            case RIGHT_ROUNDABOUT_EXIT9:
                return "Toma la novena salida de la rotonda a la derecha";
            case RIGHT_ROUNDABOUT_EXIT10:
                return "Toma la decima salida de la rotonda a la derecha";
            case RIGHT_ROUNDABOUT_EXIT11:
                return "Toma la undécima salida de la rotonda a la derecha";
            case RIGHT_ROUNDABOUT_EXIT12:
                return "Toma la duodécima salida de la rotonda a la derecha";
            case CONTINUE_ON:
                return "Continúe por";
            case ENTER_HIGHWAY_FROM_LEFT:
                return "Entra a la carretera desde la izquierda";
            case ENTER_HIGHWAY_FROM_RIGHT:
                return "Entra a la carretera desde la derecha";
            case LEFT_EXIT:
                return "Salida a la izquierda";
            case LEFT_FORK:
                return "Mantengase a la izquierda";
            case LEFT_RAMP:
                return "Rampa a la izquierda";
            case LEFT_ROUNDABOUT_ENTER:
                return "Entra a la rotonda por la izquierda";
            case LEFT_ROUNDABOUT_EXIT1:
                return "Toma la primera salida de la rotonda a la izquierda";
            case LEFT_ROUNDABOUT_EXIT2:
                return "Toma la segunda salida de la rotonda a la izquierda";
            case LEFT_ROUNDABOUT_EXIT3:
                return "Toma la tercera salida de la rotonda a la izquierda";
            case LEFT_ROUNDABOUT_EXIT4:
                return "Toma la cuarta salida de la rotonda a la izquierda";
            case LEFT_ROUNDABOUT_EXIT5:
                return "Toma la quinta salida de la rotonda a la izquierda";
            case LEFT_ROUNDABOUT_EXIT6:
                return "Toma la sexta salida de la rotonda a la izquierda";
            case LEFT_ROUNDABOUT_EXIT7:
                return "Toma la séptima salida de la rotonda a la izquierda";
            case LEFT_ROUNDABOUT_EXIT8:
                return "Toma la octava salida de la rotonda a la izquierda";
            case LEFT_ROUNDABOUT_EXIT9:
                return "Toma la novena salida de la rotonda a la izquierda";
            case LEFT_ROUNDABOUT_EXIT10:
                return "Toma la decima salida de la rotonda a la izquierda";
            case LEFT_ROUNDABOUT_EXIT11:
                return "Toma la undécima salida de la rotonda a la izquierda";
            case LEFT_ROUNDABOUT_EXIT12:
                return "Toma la duodécima salida de la rotonda a la izquierda";
            case LEFT_ROUNDABOUT_PASS:
                return "Continúe en la rotonda izquierda";
            case LEFT_U_TURN:
                return "Vuelta a la izquierda";
            case MIDDLE_FORK:
                return "Tomar el camino del medio";
            case RIGHT_EXIT:
                return "Salida a la derecha";
            case RIGHT_FORK:
                return "Mantengase a la derecha";
            case RIGHT_RAMP:
                return "Rampa a la derecha";
            case RIGHT_ROUNDABOUT_PASS:
                return "Continúe en la rotonda por la derecha";
            case RIGHT_U_TURN:
                return "Vuelta a la derecha";
            case SHARP_LEFT_TURN:
                return "Giro cerrado a la izquierda";
            case SHARP_RIGHT_TURN:
                return "Giro cerrado a la derecha";
            case SLIGHT_LEFT_TURN:
                return "Giro leve a la izquierda";
            case SLIGHT_RIGHT_TURN:
                return "Giro leve a la derecha";
            case ARRIVE:
                return "Ha llegado a su destino";
            default:
                return "Continúe";
        }
    }
}