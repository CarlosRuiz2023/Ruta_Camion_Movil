package com.itsmarts.smartroutetruckapp.adaptadores;

import static android.view.View.VISIBLE;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.mapview.MapPolygon;
import com.here.sdk.routing.Route;
import com.itsmarts.smartroutetruckapp.MainActivity;
import com.itsmarts.smartroutetruckapp.R;
import com.itsmarts.smartroutetruckapp.clases.RoutingExample;
import com.itsmarts.smartroutetruckapp.modelos.RoutesWithId;

import java.util.ArrayList;
import java.util.List;

public class RouterAsignedAdapter extends RecyclerView.Adapter<RouterAsignedAdapter.RouteViewHolder> {
    private static int position=0;
    private static MainActivity mainActivity;
    private static AlertDialog alertDialogRuta;
    private static List<RoutesWithId> rutas;
    private static final String TAG = "RouterAsignedAdapter";

    // Constructor para el adaptador
    public RouterAsignedAdapter(MainActivity mainActivity, AlertDialog alertDialogRuta, List<RoutesWithId> rutas) {
        this.mainActivity = mainActivity;
        this.alertDialogRuta = alertDialogRuta;
        this.rutas = rutas;
    }

    @Override
    public RouteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflar la vista del elemento de la lista
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.route_item, parent, false);
        return new RouteViewHolder(view,this);
    }

    @Override
    public void onBindViewHolder(RouteViewHolder holder, int position) {
        if(rutas.size() == 0){
            holder.btnStartRoute.setVisibility(View.GONE);
            holder.btnStartRoute.setEnabled(false);
        }else{
            holder.btnStartRoute.setVisibility(View.VISIBLE);
            // Asigna el nombre del polígono basado en su posición
            holder.routeNameTextView.setText(rutas.get(position).name);
        }
    }

    @Override
    public int getItemCount() {
        return rutas.size();
    }

    // Clase interna para el ViewHolder
    public static class RouteViewHolder extends RecyclerView.ViewHolder {
        public TextView routeNameTextView;
        public ImageButton btnStartRoute;

        public RouteViewHolder(View itemView, RouterAsignedAdapter adapter) {
            super(itemView);
            // Enlaza el TextView con el layout
            routeNameTextView = itemView.findViewById(R.id.routeNameTextView);
            btnStartRoute = itemView.findViewById(R.id.btnStartRoute);
            btnStartRoute.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mainActivity.ruta != null){
                        mainActivity.limpiezaTotal();
                    }
                    mainActivity.llGeocerca.setVisibility(VISIBLE);
                    btnStartRoute.startAnimation(mainActivity.animacionClick);
                    mainActivity.handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mainActivity.ruta=rutas.get(getAdapterPosition());
                            alertDialogRuta.dismiss();
                            List<GeoCoordinates> puntos_de_control = new ArrayList<>();
                            List<MapPolygon> zonas = new ArrayList<>();
                            if(mainActivity.ruta.puntosIds!=null){
                                for (int i = 0; i < mainActivity.controlPointsExample.pointsWithIds.size(); i++) {
                                    boolean foundPuntoDeControl = false;
                                    for (int id : mainActivity.ruta.puntosIds) {
                                        if (id == mainActivity.controlPointsExample.pointsWithIds.get(i).id) {
                                            foundPuntoDeControl = true;
                                            break;
                                        }
                                    }
                                    if (foundPuntoDeControl) {
                                        if (mainActivity.controlPointsExample.pointsWithIds.get(i).status) {
                                            mainActivity.controlPointsExample.pointsWithIds.get(i).visibility=true;
                                            mainActivity.controlPointsExample.pointsWithIds.get(i).label=true;
                                            puntos_de_control.add(mainActivity.controlPointsExample.pointsWithIds.get(i).mapMarker.getCoordinates());
                                            mainActivity.puntos.add(mainActivity.controlPointsExample.pointsWithIds.get(i));
                                            mainActivity.geocercas.drawGecocercaControlPoint(mainActivity.controlPointsExample.pointsWithIds.get(i).mapMarker.getCoordinates(), 100);
                                        }
                                    }
                                }
                            }
                            if(mainActivity.ruta.zonasIds!=null){
                                for (int i = 0; i < mainActivity.avoidZonesExample.polygonWithIds.size(); i++) {
                                    boolean foundZona = false;
                                    for (int id : mainActivity.ruta.zonasIds) {
                                        if (id == mainActivity.avoidZonesExample.polygonWithIds.get(i).id) {
                                            foundZona = true;
                                            break;
                                        }
                                    }

                                    if (foundZona) {
                                        if (mainActivity.avoidZonesExample.polygonWithIds.get(i).status) {
                                            mainActivity.avoidZonesExample.polygonWithIds.get(i).visibility=true;
                                            mainActivity.avoidZonesExample.polygonWithIds.get(i).label=true;
                                            mainActivity.poligonos.add(mainActivity.avoidZonesExample.polygonWithIds.get(i));
                                            if(!mainActivity.avoidZonesExample.polygonWithIds.get(i).peligrosa){
                                                zonas.add(mainActivity.avoidZonesExample.polygonWithIds.get(i).polygon);
                                            }
                                        }
                                    }
                                }
                            }
                            mainActivity.mapView.getMapScene().addMapPolyline(mainActivity.ruta.polyline);
                            mainActivity.geocercas.drawGeofenceAroundPolyline(mainActivity.ruta.polyline, 100.0);
                            mainActivity.mapView.getMapScene().addMapPolygon(mainActivity.geocercas.geocercas);
                            mainActivity.controlPointsExample.cleanPoint();
                            mainActivity.avoidZonesExample.cleanPolygon();
                            mainActivity.routingExample.addRoute(zonas,puntos_de_control,mainActivity.currentGeoCoordinates, mainActivity.ruta.coordinatesFin, null, mainActivity.ruta.coordinatesInicio, new RoutingExample.RouteCallback() {
                                @Override
                                public void onRouteCalculated(Route route) {
                                    if (route != null) {
                                        mainActivity.messageView.startAnimation(mainActivity.cargaAnimacion);
                                        mainActivity.messageView.setVisibility(View.VISIBLE);
                                        mainActivity.btnTerminarRuta.setVisibility(VISIBLE);
                                        mainActivity.txtNavegacion.setVisibility(VISIBLE);
                                        mainActivity.txtTerminarRuta.setVisibility(VISIBLE);
                                        mainActivity.detallesRuta.setVisibility(VISIBLE);
                                        mainActivity.distanceTextView.setVisibility(VISIBLE);
                                        mainActivity.timeTextView.setVisibility(VISIBLE);

                                        mainActivity.rutaGenerada = true;
                                        try {
                                            mainActivity.navigationExample.startNavigation(route, false, true);
                                            mainActivity.routeSuccessfullyProcessed = true;
                                        } catch (Exception e) {
                                            mainActivity.routeSuccessfullyProcessed = false;
                                        }
                                    } else {
                                        mainActivity.messages.showCustomToast("No se pudo calcular la ruta");
                                        mainActivity.routeSuccessfullyProcessed = false;
                                    }
                                }
                            });
                        }
                    }, 400);
                }
            });
        }
    }
}

