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

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.here.sdk.core.Color;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.engine.SDKNativeEngine;
import com.here.sdk.core.errors.InstantiationErrorException;
import com.here.sdk.location.LocationAccuracy;
import com.here.sdk.mapview.MapView;
import com.here.sdk.navigation.DynamicCameraBehavior;
import com.here.sdk.navigation.RouteProgressColors;
import com.here.sdk.navigation.SpeedBasedCameraBehavior;
import com.here.sdk.navigation.VisualNavigator;
import com.here.sdk.navigation.VisualNavigatorColors;
import com.here.sdk.prefetcher.RoutePrefetcher;
import com.here.sdk.routing.Route;
import com.here.sdk.routing.RoutingError;
import com.here.sdk.routing.SectionTransportMode;
import com.here.sdk.trafficawarenavigation.DynamicRoutingEngine;
import com.here.sdk.trafficawarenavigation.DynamicRoutingEngineOptions;
import com.here.sdk.trafficawarenavigation.DynamicRoutingListener;
import com.here.time.Duration;
import com.itsmarts.smartroutetruckapp.MainActivity;
import com.itsmarts.smartroutetruckapp.helpers.Messages;

public class NavigationExample {

    private static final String TAG = NavigationExample.class.getName();
    public VisualNavigator visualNavigator = null;
    public HEREPositioningProvider herePositioningProvider = null;
    private HEREPositioningSimulator herePositioningSimulator;
    private DynamicRoutingEngine dynamicRoutingEngine;
    private RoutePrefetcher routePrefetcher;
    private NavigationEventHandler navigationEventHandler = null;
    private MainActivity mainActivity;
    public LocationAccuracy locationAccuracy = LocationAccuracy.BEST_AVAILABLE;
    public boolean inicioEscucha = false;

    public NavigationExample(MainActivity mainActivity) {
        try{
            this.mainActivity = mainActivity;
            // A class to receive real location events.
            herePositioningProvider = new HEREPositioningProvider(mainActivity);
            // A class to receive simulated location events.
            herePositioningSimulator = new HEREPositioningSimulator();
            routePrefetcher = new RoutePrefetcher(SDKNativeEngine.getSharedInstance());

            try {
                visualNavigator = new VisualNavigator();
            } catch (InstantiationErrorException e) {
                //throw new RuntimeException("Error en la inicialización del VisualNavigator: " + e.error.name());
                Log.d(TAG,"Error en la inicialización del VisualNavigator: " + e.error.name());
            }

            visualNavigator.startRendering(mainActivity.mapView);

            createDynamicRoutingEngine();

            navigationEventHandler = new NavigationEventHandler(mainActivity);
            navigationEventHandler.setupListeners(visualNavigator, dynamicRoutingEngine);
            updateCameraTracking(false);
        }catch (Exception e){
            Messages.showErrorDetail(mainActivity, e);
        }
    }

    public NavigationEventHandler getNavigationEventHandler() {
        try{
            return navigationEventHandler;
        }catch (Exception e){
            Messages.showErrorDetail(mainActivity, e);
            return null;
        }
    }

    public void startLocationProvider() {
        try{
            inicioEscucha = true;
            herePositioningProvider.startLocating(visualNavigator, locationAccuracy);
        }catch (Exception e){
            Messages.showErrorDetail(mainActivity, e);
        }
    }

    private void prefetchMapData(GeoCoordinates currentGeoCoordinates) {
        try{
            double radiusInMeters = 2000.0;
            routePrefetcher.prefetchAroundLocationWithRadius(currentGeoCoordinates, radiusInMeters);
            routePrefetcher.prefetchAroundRouteOnIntervals(visualNavigator);
        }catch (Exception e){
            Messages.showErrorDetail(mainActivity, e);
        }
    }

    private void createDynamicRoutingEngine() {
        try{
            DynamicRoutingEngineOptions dynamicRoutingOptions = new DynamicRoutingEngineOptions();
            dynamicRoutingOptions.minTimeDifference = Duration.ofSeconds(1);
            dynamicRoutingOptions.minTimeDifferencePercentage = 0.1;
            dynamicRoutingOptions.pollInterval = Duration.ofMinutes(5);

            try {
                dynamicRoutingEngine = new DynamicRoutingEngine(dynamicRoutingOptions);
            } catch (InstantiationErrorException e) {
                throw new RuntimeException("Initialization of DynamicRoutingEngine failed: " + e.error.name());
            }
        }catch (Exception e){
            Messages.showErrorDetail(mainActivity, e);
        }
    }

    public void startNavigation(Route route, boolean isSimulated, boolean isCameraTrackingEnabled) {
        try{
            mainActivity.trackCamara.setVisibility(View.VISIBLE);
            mainActivity.txtNavegacion.setVisibility(View.VISIBLE);
            GeoCoordinates startGeoCoordinates = route.getGeometry().vertices.get(0);
            prefetchMapData(startGeoCoordinates);

            // Set the route and maneuver arrow color.
            customizeVisualNavigatorColors();

            visualNavigator.setCameraBehavior(new DynamicCameraBehavior());

            visualNavigator.setRoute(route);

            if (isSimulated) {
                enableRoutePlayback(route);
                mainActivity.messageView.setText("Starting simulated navgation.");
            } else {
                enableDevicePositioning();
                if(mainActivity.rutaGenerada){
                    mainActivity.routeTextView.setVisibility(View.VISIBLE);
                    mainActivity.routeTextView.setText(String.format("%s", mainActivity.ruta.name));
                }
                mainActivity.messageView.setText("Iniciando navegación...");
            }

            startDynamicSearchForBetterRoutes(route);

            updateCameraTracking(isCameraTrackingEnabled);
        }catch (Exception e){
            Messages.showErrorDetail(mainActivity, e);
        }
    }

    private void startDynamicSearchForBetterRoutes(Route route) {
        try{
            try {
                dynamicRoutingEngine.start(route, new DynamicRoutingListener() {
                    @Override
                    public void onBetterRouteFound(@NonNull Route newRoute, int etaDifferenceInSeconds, int distanceDifferenceInMeters) {
                        Log.d(TAG, "DynamicRoutingEngine: Calculated a new route.");
                        Log.d(TAG, "DynamicRoutingEngine: etaDifferenceInSeconds: " + etaDifferenceInSeconds + ".");
                        Log.d(TAG, "DynamicRoutingEngine: distanceDifferenceInMeters: " + distanceDifferenceInMeters + ".");

                        String logMessage = "Calculated a new route. etaDifferenceInSeconds: " + etaDifferenceInSeconds +
                                " distanceDifferenceInMeters: " + distanceDifferenceInMeters;
                        mainActivity.messageView.setText("DynamicRoutingEngine update: " + logMessage);

                    }

                    @Override
                    public void onRoutingError(@NonNull RoutingError routingError) {
                        Log.d(TAG,"Error while dynamically searching for a better route: " + routingError.name());
                    }
                });
            } catch (DynamicRoutingEngine.StartException e) {
                //throw new RuntimeException("Start of DynamicRoutingEngine failed. Is the RouteHandle missing?");
                Log.d(TAG,"Start of DynamicRoutingEngine failed. Is the RouteHandle missing?");
            }
        }catch (Exception e){
            Messages.showErrorDetail(mainActivity, e);
        }
    }

    public void stopNavigation(boolean isCameraTrackingEnabled) {
        try{
            visualNavigator.setRoute(null);
            visualNavigator.setCameraBehavior(new SpeedBasedCameraBehavior());
            enableDevicePositioning();
            mainActivity.messageView.setText("Indicaciones");

            dynamicRoutingEngine.stop();
            routePrefetcher.stopPrefetchAroundRoute();

            updateCameraTracking(isCameraTrackingEnabled);
        }catch (Exception e){
            Messages.showErrorDetail(mainActivity, e);
        }
    }

    private void updateCameraTracking(boolean isCameraTrackingEnabled) {
        try{
            if (isCameraTrackingEnabled) {
                startCameraTracking();
            } else {
                stopCameraTracking();
            }
        }catch (Exception e){
            Messages.showErrorDetail(mainActivity, e);
        }
    }

    public void enableRoutePlayback(Route route) {
        try{
            herePositioningProvider.stopLocating();
            herePositioningSimulator.startLocating(visualNavigator, route);
        }catch (Exception e) {
            Messages.showErrorDetail(mainActivity, e);
        }
    }

    public void enableDevicePositioning() {
        try{
            herePositioningSimulator.stopLocating();
            herePositioningProvider.startLocating(visualNavigator, locationAccuracy);
        }catch (Exception e){
            Messages.showErrorDetail(mainActivity, e);
        }
    }

    public void startCameraTracking() {
        try{
            visualNavigator.setCameraBehavior(new DynamicCameraBehavior());
        }catch (Exception e){
            Messages.showErrorDetail(mainActivity, e);
        }
    }

    public void stopCameraTracking() {
        try{
            visualNavigator.setCameraBehavior(null);
        }catch (Exception e){
            Messages.showErrorDetail(mainActivity, e);
        }
    }

    public void stopLocating() {
        try{
            herePositioningProvider.stopLocating();
        }catch (Exception e){
            Messages.showErrorDetail(mainActivity, e);
        }
    }

    public void stopRendering() {
        try{
            visualNavigator.stopRendering();
        }catch (Exception e){
            Messages.showErrorDetail(mainActivity, e);
        }
    }

    public VisualNavigator getVisualNavigator() {
        try{
            return visualNavigator;
        }catch (Exception e){
            Messages.showErrorDetail(mainActivity, e);
            return null;
        }
    }
    private void customizeVisualNavigatorColors() {
        try{
            Color routeAheadColor =  Color.valueOf(android.graphics.Color.BLUE);
            Color routeBehindColor = Color.valueOf(android.graphics.Color.RED);
            Color routeAheadOutlineColor = Color.valueOf(android.graphics.Color.YELLOW);
            Color routeBehindOutlineColor = Color.valueOf(android.graphics.Color.DKGRAY);
            Color maneuverArrowColor = Color.valueOf(android.graphics.Color.GREEN);

            VisualNavigatorColors visualNavigatorColors = VisualNavigatorColors.dayColors();
            RouteProgressColors routeProgressColors = new RouteProgressColors(
                    routeAheadColor,
                    routeBehindColor,
                    routeAheadOutlineColor,
                    routeBehindOutlineColor);

            // Sets the color used to draw maneuver arrows.
            visualNavigatorColors.setManeuverArrowColor(maneuverArrowColor);
            // Sets route color for a single transport mode. Other modes are kept using defaults.
            visualNavigatorColors.setRouteProgressColors(SectionTransportMode.CAR, routeProgressColors);
            // Sets the adjusted colors for route progress and maneuver arrows based on the day color scheme.
            visualNavigator.setColors(visualNavigatorColors);
        }catch (Exception e){
            Messages.showErrorDetail(mainActivity, e);
        }
    }
}
