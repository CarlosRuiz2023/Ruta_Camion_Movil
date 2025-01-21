package com.itsmarts.smartroutetruckapp.adaptadores;

import static android.view.View.VISIBLE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.GeoCoordinatesUpdate;
import com.here.sdk.mapview.MapCameraAnimation;
import com.here.sdk.mapview.MapCameraAnimationFactory;
import com.here.sdk.mapview.MapMeasure;
import com.here.sdk.mapview.MapPolygon;
import com.here.sdk.routing.Route;
import com.here.time.Duration;
import com.itsmarts.smartroutetruckapp.MainActivity;
import com.itsmarts.smartroutetruckapp.R;
import com.itsmarts.smartroutetruckapp.api.ApiService;
import com.itsmarts.smartroutetruckapp.api.RetrofitClient;
import com.itsmarts.smartroutetruckapp.clases.RoutingExample;
import com.itsmarts.smartroutetruckapp.fragments.ErrorDialogFragment;
import com.itsmarts.smartroutetruckapp.helpers.Internet;
import com.itsmarts.smartroutetruckapp.modelos.HistorialRequest;
import com.itsmarts.smartroutetruckapp.modelos.LoginRequest;
import com.itsmarts.smartroutetruckapp.modelos.PointWithId;
import com.itsmarts.smartroutetruckapp.modelos.PolygonWithId;
import com.itsmarts.smartroutetruckapp.modelos.RoutesWithId;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RouterAsignedAdapter extends RecyclerView.Adapter<RouterAsignedAdapter.RouteViewHolder> {
    private static int position=0;
    private static MainActivity mainActivity;
    private static AlertDialog alertDialogRutaMaster;
    private static List<RoutesWithId> rutas;
    private static final String TAG = "RouterAsignedAdapter";

    // Constructor para el adaptador
    public RouterAsignedAdapter(MainActivity mainActivity, AlertDialog alertDialogRutaMaster, List<RoutesWithId> rutas) {
        this.mainActivity = mainActivity;
        this.alertDialogRutaMaster = alertDialogRutaMaster;
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
        public CardView routeCardView;

        public RouteViewHolder(View itemView, RouterAsignedAdapter adapter) {
            super(itemView);
            // Enlaza el TextView con el layout
            routeNameTextView = itemView.findViewById(R.id.routeNameTextView);
            btnStartRoute = itemView.findViewById(R.id.btnStartRoute);
            routeCardView = itemView.findViewById(R.id.routeCardView);
            routeCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialogRutaMaster.dismiss();
                    View dialogView = mainActivity.getLayoutInflater().inflate(R.layout.ventana_seleccionar_tipo_ruta, null);
                    AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
                    builder.setView(dialogView);
                    final AlertDialog alertDialogRuta = builder.create();

                    ImageButton btnSimularRuta = dialogView.findViewById(R.id.btnSimularRuta);
                    ImageButton btnObtenerRuta = dialogView.findViewById(R.id.btnObtenerRuta);
                    Button btnCancelar = dialogView.findViewById(R.id.btnCancelar);

                    btnObtenerRuta.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialogRutaMaster.dismiss();
                            if(mainActivity.ruta != null){
                                mainActivity.limpiezaTotal();
                            }
                            if(mainActivity.isTrackingCamera){
                                mainActivity.isTrackingCamera=false;
                                mainActivity.trackCamara.setImageResource(R.drawable.track_on);
                            }
                            mainActivity.llLoadingRoute.setVisibility(VISIBLE);
                            //mainActivity.likeImageView1.setVisibility(VISIBLE);
                            btnStartRoute.startAnimation(mainActivity.animacionClick);
                            mainActivity.handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mainActivity.ruta=rutas.get(getAdapterPosition());
                                    mainActivity.logger.trackActivity(TAG,"Ruta seleccionada","El usuario selecciono la ruta: "+mainActivity.ruta.name);
                                    /*GeoCoordinatesUpdate geoCoordinatesUpdate = new GeoCoordinatesUpdate(mainActivity.currentGeoCoordinates);
                                    MapMeasure mapMeasureZoom = new MapMeasure(MapMeasure.Kind.DISTANCE, 1000.0);
                                    double bowFactor = 1;
                                    MapCameraAnimation animation = MapCameraAnimationFactory.flyTo(
                                            geoCoordinatesUpdate, mapMeasureZoom, bowFactor, Duration.ofSeconds(3));
                                    mainActivity.mapView.getCamera().startAnimation(animation);*/
                                    alertDialogRuta.dismiss();
                                    List<GeoCoordinates> puntos_de_control = new ArrayList<>();
                                    List<MapPolygon> zonas = new ArrayList<>();
                                    int id_vehiculo = 3;
                                    if(mainActivity.ruta.truckSpectIds!=null){
                                        for (int i = 0; i < mainActivity.ruta.truckSpectIds.length; i++) {
                                            if(mainActivity.ruta.truckSpectIds[i] > id_vehiculo){
                                                id_vehiculo = mainActivity.ruta.truckSpectIds[i];
                                            }
                                        }
                                    }
                                    if(mainActivity.ruta.puntosIds!=null){
                                        for (int id : mainActivity.ruta.puntosIds) {
                                            for (int i = 0; i < mainActivity.controlPointsExample.pointsWithIds.size(); i++) {
                                                if (id == mainActivity.controlPointsExample.pointsWithIds.get(i).id) {
                                                    if (mainActivity.controlPointsExample.pointsWithIds.get(i).status) {
                                                        mainActivity.controlPointsExample.pointsWithIds.get(i).visibility=true;
                                                        mainActivity.controlPointsExample.pointsWithIds.get(i).label=true;
                                                        puntos_de_control.add(mainActivity.controlPointsExample.pointsWithIds.get(i).mapMarker.getCoordinates());
                                                        mainActivity.puntos.add(mainActivity.controlPointsExample.pointsWithIds.get(i));
                                                        mainActivity.geocercas.drawGecocercaControlPoint(mainActivity.controlPointsExample.pointsWithIds.get(i).mapMarker.getCoordinates(), 100);
                                                    }
                                                    break;
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
                                    mainActivity.geocercas.drawGeofenceAroundPolyline(mainActivity.ruta.polyline, 100.0);
                                    mainActivity.llLoadingRoute.setVisibility(View.GONE);
                                    //mainActivity.likeImageView1.setVisibility(View.GONE);
                                    mainActivity.controlPointsExample.cleanPoint();
                                    mainActivity.avoidZonesExample.cleanPolygon();
                                    mainActivity.rutaGenerada = true;
                                    mainActivity.routingExample.addRoute(zonas,puntos_de_control,mainActivity.currentGeoCoordinates, mainActivity.ruta.coordinatesFin, null, mainActivity.ruta.coordinatesInicio,id_vehiculo,mainActivity.ruta.orden_automatico, new RoutingExample.RouteCallback() {
                                        @Override
                                        public void onRouteCalculated(Route route) {
                                            if (route != null) {
                                                mainActivity.mapView.getMapScene().addMapPolyline(mainActivity.ruta.polyline);
                                                mainActivity.mapView.getMapScene().addMapPolygon(mainActivity.geocercas.geocercas);
                                                mainActivity.llGeocerca.setVisibility(VISIBLE);
                                                mainActivity.messageView.startAnimation(mainActivity.cargaAnimacion);
                                                mainActivity.messageView.setVisibility(View.VISIBLE);
                                                mainActivity.btnTerminarRuta.setVisibility(VISIBLE);
                                                mainActivity.txtNavegacion.setVisibility(VISIBLE);
                                                mainActivity.txtTerminarRuta.setVisibility(VISIBLE);
                                                mainActivity.detallesRuta.setVisibility(VISIBLE);
                                                mainActivity.distanceTextView.setVisibility(VISIBLE);
                                                mainActivity.timeTextView.setVisibility(VISIBLE);

                                                try {
                                                    mainActivity.navigationExample.startNavigation(route, false, false);
                                                    mainActivity.logger.trackActivity(TAG,"Se inicio ruta","El usuario decidio iniciar la ruta: "+mainActivity.ruta.name);
                                                    mainActivity.routeSuccessfullyProcessed = true;
                                                } catch (Exception e) {
                                                    mainActivity.routeSuccessfullyProcessed = false;
                                                }
                                                // Create a LoginRequest object with the provided username and password
                                                if(Internet.isNetworkConnected()){
                                                    SharedPreferences sharedPreferences = mainActivity.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                                                    int id_usuario = sharedPreferences.getInt("id_usuario", 0);
                                                    HistorialRequest historialRequest = new HistorialRequest(mainActivity.ruta.id, id_usuario);

                                                    // Use Retrofit to make the POST request
                                                    ApiService apiService = RetrofitClient.getInstance(null).create(ApiService.class);
                                                    Call<ResponseBody> call1 = apiService.mandarHistorial(historialRequest);

                                                    call1.enqueue(new Callback<ResponseBody>() {
                                                        @Override
                                                        public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                                                            Toast.makeText(mainActivity.getApplicationContext(), "Uso de la ruta #"+mainActivity.ruta.id+" registrado", Toast.LENGTH_SHORT).show();
                                                            mainActivity.logger.trackActivity(TAG,"Uso de ruta registrado","El uso de la ruta: "+mainActivity.ruta.name+" fue registrado con exito");
                                                        }

                                                        @Override
                                                        public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                                                            Log.e("Retrofit", "Error en la solicitud: " + t.getMessage());
                                                        }
                                                    });
                                                }else {
                                                    DialogFragment errorDialog = new ErrorDialogFragment();
                                                    errorDialog.show(mainActivity.getSupportFragmentManager(), "errorDialog");
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
                    btnSimularRuta.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(mainActivity.ruta != null){
                                mainActivity.limpiezaTotal();
                            }
                            if(mainActivity.isTrackingCamera){
                                mainActivity.isTrackingCamera=false;
                                mainActivity.trackCamara.setImageResource(R.drawable.track_on);
                            }
                            mainActivity.llGeocerca.setVisibility(VISIBLE);
                            mainActivity.llLoadingRoute.setVisibility(VISIBLE);
                            //mainActivity.likeImageView1.setVisibility(VISIBLE);
                            btnStartRoute.startAnimation(mainActivity.animacionClick);
                            mainActivity.handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mainActivity.ruta=rutas.get(getAdapterPosition());
                                    alertDialogRuta.dismiss();
                                    List<GeoCoordinates> puntos_de_control = new ArrayList<>();
                                    List<MapPolygon> zonas = new ArrayList<>();
                                    int id_vehiculo = 3;
                                    if(mainActivity.ruta.truckSpectIds!=null){
                                        for (int i = 0; i < mainActivity.ruta.truckSpectIds.length; i++) {
                                            if(mainActivity.ruta.truckSpectIds[i] > id_vehiculo){
                                                id_vehiculo = mainActivity.ruta.truckSpectIds[i];
                                            }
                                        }
                                    }
                                    if(mainActivity.ruta.puntosIds!=null){
                                        for (int id : mainActivity.ruta.puntosIds) {
                                            for (int i = 0; i < mainActivity.controlPointsExample.pointsWithIds.size(); i++) {
                                                if (id == mainActivity.controlPointsExample.pointsWithIds.get(i).id) {
                                                    if (mainActivity.controlPointsExample.pointsWithIds.get(i).status) {
                                                        mainActivity.controlPointsExample.pointsWithIds.get(i).visibility=true;
                                                        mainActivity.controlPointsExample.pointsWithIds.get(i).label=true;
                                                        puntos_de_control.add(mainActivity.controlPointsExample.pointsWithIds.get(i).mapMarker.getCoordinates());
                                                        mainActivity.puntos.add(mainActivity.controlPointsExample.pointsWithIds.get(i));
                                                        mainActivity.geocercas.drawGecocercaControlPoint(mainActivity.controlPointsExample.pointsWithIds.get(i).mapMarker.getCoordinates(), 100);
                                                    }
                                                    break;
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
                                    mainActivity.geocercas.drawGeofenceAroundPolyline(mainActivity.ruta.polyline, 100.0);
                                    mainActivity.llLoadingRoute.setVisibility(View.GONE);
                                    //mainActivity.likeImageView1.setVisibility(View.GONE);
                                    mainActivity.controlPointsExample.cleanPoint();
                                    mainActivity.avoidZonesExample.cleanPolygon();
                                    mainActivity.rutaGenerada = true;
                                    mainActivity.routingExample.addRoute(zonas,puntos_de_control,mainActivity.currentGeoCoordinates, mainActivity.ruta.coordinatesFin, null, mainActivity.ruta.coordinatesInicio,id_vehiculo,mainActivity.ruta.orden_automatico, new RoutingExample.RouteCallback() {
                                        @Override
                                        public void onRouteCalculated(Route route) {
                                            if (route != null) {
                                                mainActivity.mapView.getMapScene().addMapPolyline(mainActivity.ruta.polyline);
                                                mainActivity.mapView.getMapScene().addMapPolygon(mainActivity.geocercas.geocercas);
                                                mainActivity.messageView.startAnimation(mainActivity.cargaAnimacion);
                                                mainActivity.messageView.setVisibility(View.VISIBLE);
                                                mainActivity.btnTerminarRuta.setVisibility(VISIBLE);
                                                mainActivity.txtNavegacion.setVisibility(VISIBLE);
                                                mainActivity.txtTerminarRuta.setVisibility(VISIBLE);
                                                mainActivity.detallesRuta.setVisibility(VISIBLE);
                                                mainActivity.distanceTextView.setVisibility(VISIBLE);
                                                mainActivity.timeTextView.setVisibility(VISIBLE);
                                                try {
                                                    mainActivity.navigationExample.startNavigation(route, true, false);
                                                    mainActivity.logger.trackActivity(TAG,"Se inicio ruta","El usuario decidio simular la ruta: "+mainActivity.ruta.name);
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
                    btnCancelar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            btnCancelar.startAnimation(mainActivity.animacionClick);
                            mainActivity.handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    alertDialogRuta.dismiss();
                                }
                            }, 400);
                        }
                    });
                    alertDialogRuta.show();
                }
            });
            btnStartRoute.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showRouteInfoDialog(getAdapterPosition());
                    mainActivity.logger.trackActivity(TAG,"Obtuvo info ruta","El usuario consulto la informacion de la ruta: "+mainActivity.ruta.name);
                }
            });
            alertDialogRutaMaster.show();
        }
    }

    public static void showRouteInfoDialog(int position) {
        RoutesWithId selectedRoute = rutas.get(position);

        // Infla el layout personalizado
        View dialogView = LayoutInflater.from(mainActivity).inflate(R.layout.route_info_dialog, null);
        TextView routeNameTextView = dialogView.findViewById(R.id.route_name);
        TextView startAddressTextView = dialogView.findViewById(R.id.start_address);
        TextView endAddressTextView = dialogView.findViewById(R.id.end_address);
        TextView creationDateTextView = dialogView.findViewById(R.id.creation_date);
        TextView lastModifiedTextView = dialogView.findViewById(R.id.last_modified_date);
        TextView durationTextView = dialogView.findViewById(R.id.duration);
        TextView distanceTextView = dialogView.findViewById(R.id.distance);
        TextView pointsTextView = dialogView.findViewById(R.id.points);
        TextView zonesTextView = dialogView.findViewById(R.id.zones);

        // Formatea las fechas
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String creationDate = dateFormat.format(selectedRoute.fecha_creacion);
        String lastModifiedDate = "";
        if(selectedRoute.fecha_ultima_modificacion != null){
            lastModifiedDate = dateFormat.format(selectedRoute.fecha_ultima_modificacion);
        }else{
            lastModifiedDate = "Sin actualizaciones";
        }
        // Establece los valores en los TextView
        routeNameTextView.setText(selectedRoute.name);
        startAddressTextView.setText("Inicio: " + selectedRoute.direccion_inicio);
        endAddressTextView.setText("Fin: " + selectedRoute.direccion_fin);
        creationDateTextView.setText("Fecha de creación: \n" + creationDate);
        lastModifiedTextView.setText("Última modificación: \n" + lastModifiedDate);
        if(selectedRoute.tiempo > 3600){
            durationTextView.setText("Duración: \n" + (selectedRoute.tiempo/3600) + " hrs.");
        }
        else if(selectedRoute.tiempo > 60){
            durationTextView.setText("Duración: \n" + (selectedRoute.tiempo/60) + " mns.");
        }else{
            durationTextView.setText("Duración: \n" + selectedRoute.tiempo + " s.");
        }
        if(selectedRoute.distancia > 1000){
            distanceTextView.setText("Distancia: \n" + (selectedRoute.distancia/1000) + " km.");
        }else{
            distanceTextView.setText("Distancia: \n" + selectedRoute.distancia + " mtrs.");
        }
        if(selectedRoute.puntosIds!=null){
            pointsTextView.setText("Puntos de control: " + selectedRoute.puntosIds.length);
        }else{
            pointsTextView.setText("Puntos de control: " + 0);
        }
        if(selectedRoute.zonasIds!=null){
            zonesTextView.setText("Zonas: " + selectedRoute.zonasIds.length);
        }else{
            zonesTextView.setText("Zonas: " + 0);
        }

        // Crea el AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setView(dialogView);
        builder.setPositiveButton("Cerrar", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}

