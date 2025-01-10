package com.itsmarts.smartroutetruckapp.helpers;

import static android.view.View.VISIBLE;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;

import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.mapview.MapPolygon;
import com.here.sdk.routing.Route;
import com.itsmarts.smartroutetruckapp.MainActivity;
import com.itsmarts.smartroutetruckapp.R;
import com.itsmarts.smartroutetruckapp.clases.RoutingExample;
import com.itsmarts.smartroutetruckapp.fragments.DialogFullFragmentErrorDetail;
import com.itsmarts.smartroutetruckapp.modelos.PointWithId;
import com.itsmarts.smartroutetruckapp.modelos.PolygonWithId;

import java.io.PrintWriter;
import java.io.StringWriter;
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

    //DIALOG DE EXCEPCIONES
    public static void showErrorDetail(AppCompatActivity activity, Exception error)
    {
        // Crear un StringWriter para almacenar la traza de la pila
        StringWriter sw = new StringWriter();
        // Crear un PrintWriter que escriba en el StringWriter
        PrintWriter pw = new PrintWriter(sw);
        // Imprimir la traza de la pila en el PrintWriter
        error.printStackTrace(pw);
        // Obtener la traza de la pila como una cadena
        String stackTrace = sw.toString();
        DialogFullFragmentErrorDetail detail = new DialogFullFragmentErrorDetail(stackTrace);
        //FragmentTransaction transaction = activity.getFragmentManager().beginTransaction();
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        detail.show(transaction, DialogFullFragmentErrorDetail.TAG);
        detail.setCancelable(false);
    }


    public void showDialog(String title, String mainInfo, String additionalInfo, String type, GeoCoordinates geoCoordinatesPOI) {
        if (mainActivity.isDialogShowing) {
            return;
        }

        mainActivity.isDialogShowing = true;

        Dialog dialog = new Dialog(mainActivity);
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
                                        if(!mainActivity.avoidZonesExample.polygonWithIds.get(i).peligrosa){
                                            zonas.add(mainActivity.avoidZonesExample.polygonWithIds.get(i).polygon);
                                            mainActivity.poligonos.add(mainActivity.avoidZonesExample.polygonWithIds.get(i));
                                        }
                                    }
                                }
                            }
                        }
                        mainActivity.routingExample.addRoute(zonas,puntos_de_control,mainActivity.currentGeoCoordinates, mainActivity.ruta.coordinatesFin, geoCoordinatesPOI, mainActivity.ruta.coordinatesInicio,id_vehiculo,mainActivity.ruta.orden_automatico, new RoutingExample.RouteCallback() {
                            @Override
                            public void onRouteCalculated(Route route) {
                                if (route != null) {
                                    try {
                                        dialog.dismiss();
                                        mainActivity.isTrackingCamera = false;
                                        mainActivity.trackCamara.setImageResource(R.drawable.track_on);
                                        mainActivity.navigationExample.startNavigation(route, false, false);
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
                        mainActivity.destinationGeoCoordinates =geoCoordinatesPOI;
                        mainActivity.clearMapMarkersPOIsAndCircle(true);
                        List<GeoCoordinates> puntos = new ArrayList<>();
                        List<MapPolygon> poligonos = new ArrayList<>();
                        mainActivity.routingExample.addRoute(poligonos,puntos,mainActivity.currentGeoCoordinates,geoCoordinatesPOI, null, null,1,true, new RoutingExample.RouteCallback() {
                            @Override
                            public void onRouteCalculated(Route route) {
                                if (route != null) {
                                    try {
                                        dialog.dismiss();
                                        mainActivity.isTrackingCamera = false;
                                        mainActivity.trackCamara.setImageResource(R.drawable.track_on);
                                        mainActivity.navigationExample.startNavigation(route, false, false);
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
    // Método para mostrar un diálogo de error
    public static void showInvalidCredentialsDialog(String titulo, String mensaje, Activity context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(titulo);
        builder.setMessage(mensaje);
        builder.setPositiveButton(context.getString(R.string.error_credenciale_acceptar), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
