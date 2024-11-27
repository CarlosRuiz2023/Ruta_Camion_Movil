package com.itsmarts.smartroutetruckapp.helpers;

import static android.view.View.VISIBLE;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.mapview.MapPolygon;
import com.here.sdk.routing.Route;
import com.itsmarts.smartroutetruckapp.MainActivity;
import com.itsmarts.smartroutetruckapp.R;
import com.itsmarts.smartroutetruckapp.clases.RoutingExample;
import com.itsmarts.smartroutetruckapp.modelos.PointWithId;
import com.itsmarts.smartroutetruckapp.modelos.PolygonWithId;

import java.util.ArrayList;
import java.util.List;

public class Messages {
    MainActivity mainActivity;

    public Messages(MainActivity mainActivity) {
        this.mainActivity = mainActivity;

    }

    public void showCustomToast(String message) {
        LayoutInflater inflater = mainActivity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, mainActivity.findViewById(R.id.custom_toast_container));

        TextView text = layout.findViewById(R.id.toast_text);
        text.setText(message);

        Toast toast = new Toast(mainActivity.getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);

        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 200);
        toast.show();
    }

    public void showDialog(String title, String mainInfo, String additionalInfo, String type, GeoCoordinates geoCoordinatesPOI) {
        if (mainActivity.isDialogShowing) {
            return;
        }

        mainActivity.isDialogShowing = true;

        Dialog dialog = new Dialog(mainActivity.getApplicationContext());
        if(geoCoordinatesPOI != null){
            dialog.setContentView(R.layout.ventana_poi_ruta);
            Button goButton = dialog.findViewById(R.id.dialog_go_button);
            if(mainActivity.navigationExample.getVisualNavigator().getRoute() != null){
                goButton.setText("Pasar por ahi");
                goButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //fbEliminarPoi.setVisibility(View.GONE);
                        //txtEliminarPoi.setVisibility(View.GONE);
                        mainActivity.clearMapPolylines();
                        if (mainActivity.coordenadasDestino != null){
                            mainActivity.destinationGeoCoordinates = mainActivity.coordenadasDestino;
                        }
                        List<GeoCoordinates> puntos = new ArrayList<>();
                        for(PointWithId pointWithId:mainActivity.controlPointsExample.pointsWithIds){
                            if(pointWithId.status){
                                puntos.add(pointWithId.mapMarker.getCoordinates());
                            }
                        }
                        List<MapPolygon> poligonos = new ArrayList<>();
                        for(PolygonWithId polygonWithId:mainActivity.avoidZonesExample.polygonWithIds){
                            if(polygonWithId.status && !polygonWithId.peligrosa){
                                poligonos.add(polygonWithId.polygon);
                            }
                        }
                        mainActivity.routingExample.addRoute(poligonos,puntos,mainActivity.currentGeoCoordinates, mainActivity.destinationGeoCoordinates, mainActivity.geoCoordinatesPOI, mainActivity.waypointsGlobal, new RoutingExample.RouteCallback() {
                            @Override
                            public void onRouteCalculated(Route route) {
                                if (route != null) {
                                    try {
                                        dialog.dismiss();
                                        mainActivity.isTrackingCamera = true;
                                        mainActivity.trackCamara.setImageResource(R.drawable.track_off);
                                        mainActivity.navigationExample.startNavigation(route, false, true);
                                    } catch (Exception e) {
                                        Log.e("MainActivity", "Error starting navigation: ", e);
                                    }
                                } else {
                                    Toast.makeText(mainActivity.getApplicationContext(), "No se pudo recalcular la ruta", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        mainActivity.clearMapMarkersPOIsAndCircle(true);
                        mainActivity.btnTerminarRuta.setVisibility(VISIBLE);
                        mainActivity.txtTerminarRuta.setVisibility(VISIBLE);
                    }
                });
            } else {
                goButton.setText("Ir al lugar");
                goButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //fbEliminarPoi.setVisibility(View.GONE);
                        //txtEliminarPoi.setVisibility(View.GONE);
                        mainActivity.clearMapPolylines();
                        mainActivity.messageView.setVisibility(VISIBLE);
                        mainActivity.detallesRuta.setVisibility(VISIBLE);
                        mainActivity.distanceTextView.setVisibility(VISIBLE);
                        mainActivity.timeTextView.setVisibility(VISIBLE);
                        mainActivity.destinationGeoCoordinates =mainActivity.geoCoordinatesPOI;
                        mainActivity.clearMapMarkersPOIsAndCircle(true);
                        List<GeoCoordinates> puntos = new ArrayList<>();
                        for(PointWithId pointWithId:mainActivity.controlPointsExample.pointsWithIds){
                            if(pointWithId.status){
                                puntos.add(pointWithId.mapMarker.getCoordinates());
                            }
                        }
                        List<MapPolygon> poligonos = new ArrayList<>();
                        for(PolygonWithId polygonWithId:mainActivity.avoidZonesExample.polygonWithIds){
                            if(polygonWithId.status && !polygonWithId.peligrosa){
                                poligonos.add(polygonWithId.polygon);
                            }
                        }
                        mainActivity.routingExample.addRoute(poligonos,puntos,mainActivity.currentGeoCoordinates,mainActivity.geoCoordinatesPOI, null, new ArrayList<>(), new RoutingExample.RouteCallback() {
                            @Override
                            public void onRouteCalculated(Route route) {
                                if (route != null) {
                                    try {
                                        dialog.dismiss();
                                        mainActivity.isTrackingCamera = true;
                                        mainActivity.navigationExample.startNavigation(route, false, true);
                                    } catch (Exception e) {
                                        Log.e("MainActivity", "Error starting navigation: ", e);
                                    }
                                } else {
                                    Toast.makeText(mainActivity.getApplicationContext(), "No se pudo recalcular la ruta", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        mainActivity.clearMapMarkersPOIsAndCircle(true);
                        mainActivity.btnTerminarRuta.setVisibility(VISIBLE);
                        mainActivity.txtTerminarRuta.setVisibility(VISIBLE);
                        mainActivity.trackCamara.setImageResource(R.drawable.track_off);
                    }
                });
            }
        } else {
            dialog.setContentView(R.layout.ventana_poi_normal);
        }
        TextView titleView = dialog.findViewById(R.id.dialog_title);
        TextView addressView = dialog.findViewById(R.id.dialog_address);
        TextView categoriesView = dialog.findViewById(R.id.dialog_categories);
        TextView typeView = dialog.findViewById(R.id.textView3);
        Button closeButton = dialog.findViewById(R.id.dialog_close_button);

        titleView.setText(title);
        addressView.setText(mainInfo);

        if (!additionalInfo.isEmpty()) {
            categoriesView.setText(additionalInfo);
            typeView.setText("Categoría");
        } else {
            categoriesView.setVisibility(View.GONE);
            typeView.setVisibility(View.GONE);
        }

        if (!type.isEmpty()) {
            typeView.setText(type);
        } else {
            typeView.setVisibility(View.GONE);
        }

        closeButton.setOnClickListener(v -> {
            dialog.dismiss();
            mainActivity.isDialogShowing = false;
        });

        dialog.setOnDismissListener(dialogInterface -> {
            mainActivity.isDialogShowing = false;
        });

        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
