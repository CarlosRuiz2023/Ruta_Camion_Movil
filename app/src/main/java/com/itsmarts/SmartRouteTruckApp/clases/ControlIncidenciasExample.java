package com.itsmarts.SmartRouteTruckApp.clases;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.mapview.MapImage;
import com.here.sdk.mapview.MapImageFactory;
import com.here.sdk.mapview.MapMarker;
import com.here.sdk.mapview.MapView;
import com.here.sdk.search.Address;
import com.here.sdk.search.Place;
import com.here.sdk.search.SearchCallback;
import com.here.sdk.search.SearchError;
import com.itsmarts.SmartRouteTruckApp.MainActivity;
import com.itsmarts.SmartRouteTruckApp.R;
import com.itsmarts.SmartRouteTruckApp.adaptadores.PointAdapter;
import com.itsmarts.SmartRouteTruckApp.api.ApiService;
import com.itsmarts.SmartRouteTruckApp.api.RetrofitClient;
import com.itsmarts.SmartRouteTruckApp.bd.DatabaseHelper;
import com.itsmarts.SmartRouteTruckApp.fragments.ModalBottomSheetFullScreenFragmentIncidencias;
import com.itsmarts.SmartRouteTruckApp.fragments.ModalBottomSheetFullScreenFragmentPuntos;
import com.itsmarts.SmartRouteTruckApp.modelos.Incidencia;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class ControlIncidenciasExample {
    public List<MapMarker> markers = new ArrayList<>();
    public List<Incidencia> incidencias = new ArrayList<>();
    public MapMarker mapMarker;
    public DatabaseHelper dbHelper;
    public PointAdapter adapter;
    // Declaración de la variable dialog
    private AlertDialog dialog;
    public MapView mapView;
    public MainActivity mainActivity;
    private LayoutInflater layoutInflater;
    public ModalBottomSheetFullScreenFragmentIncidencias bottomSheetFragment;
    public GeoCoordinates last_coordinates;
    public String estado, municipio;
    List<CompletableFuture<ResponseBody>> futures = new ArrayList<>();
    private static final String TAG = "ControlPointsExample";

    public ControlIncidenciasExample(MainActivity mainActivity, MapView mapView, LayoutInflater layoutInflater, DatabaseHelper dbHelper) {
        this.mainActivity = mainActivity;
        this.mapView = mapView;
        this.layoutInflater = layoutInflater;
        this.dbHelper = dbHelper;
        // Recupera la lista de polígonos de la base de datos
        incidencias = dbHelper.getAllIncidencias();
        //if(pointsWithIds.size()>0){
            /*// Agrega todas las llamadas a los métodos de descarga
            futures.add(descargarPuntosDeControlFaltantes());
            // Espera a que todos los futures terminen
            CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            allOf.whenComplete((result, ex) -> {
                if (ex != null) {
                    // Manejo de errores, si es necesario
                    Log.e(TAG, "Error during downloads", ex);
                }
                // Recupera la lista de polígonos de la base de datos
                pointsWithIds = dbHelper.getAllPuntos();
                for (PointWithId point : pointsWithIds) {
                    markers.add(point.mapMarker);
                    if(point.visibility){
                        mapView.getMapScene().addMapMarker(point.mapMarker);
                        if(point.label){
                            // Crea un TextView para la etiqueta
                            TextView textView = new TextView(context);
                            textView.setTextColor(Color.parseColor("#7EB8D5"));
                            textView.setText(point.name);
                            textView.setTypeface(Typeface.DEFAULT_BOLD);
                            // Crea un LinearLayout para contener el TextView y agregar padding
                            LinearLayout linearLayout = new LinearLayout(context);
                            //linearLayout.setBackgroundResource(R.color.colorAccent);
                            linearLayout.setPadding(0, 0, 0, 130);
                            linearLayout.addView(textView);
                            // Ancla el LinearLayout al mapa en las coordenadas ajustadas
                            mapView.pinView(linearLayout, point.mapMarker.getCoordinates());
                        }
                    }
                }
            });*/
        /*}else{
            // Agrega todas las llamadas a los métodos de descarga
            futures.add(descargarPuntosDeControl());
            // Espera a que todos los futures terminen
            CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            allOf.whenComplete((result, ex) -> {
                if (ex != null) {
                    // Manejo de errores, si es necesario
                    Log.e(TAG, "Error during downloads", ex);
                }
                // Recupera la lista de polígonos de la base de datos
                pointsWithIds = dbHelper.getAllPuntos();
                for (PointWithId point : pointsWithIds) {
                    markers.add(point.mapMarker);
                    if(point.visibility){
                        mapView.getMapScene().addMapMarker(point.mapMarker);
                        if(point.label){
                            // Crea un TextView para la etiqueta
                            TextView textView = new TextView(context);
                            textView.setTextColor(Color.parseColor("#7EB8D5"));
                            textView.setText(point.name);
                            textView.setTypeface(Typeface.DEFAULT_BOLD);
                            // Crea un LinearLayout para contener el TextView y agregar padding
                            LinearLayout linearLayout = new LinearLayout(context);
                            //linearLayout.setBackgroundResource(R.color.colorAccent);
                            linearLayout.setPadding(0, 0, 0, 130);
                            linearLayout.addView(textView);
                            // Ancla el LinearLayout al mapa en las coordenadas ajustadas
                            mapView.pinView(linearLayout, point.mapMarker.getCoordinates());
                        }
                    }
                }
            });
        }*/
        bottomSheetFragment = new ModalBottomSheetFullScreenFragmentIncidencias(this);
    }

    /**
     * Método para agregar un marcador en el mapa en las coordenadas especificadas.
     *
     * @param geoCoordinates Las coordenadas donde se agregará el marcador.
     */
    private void addMapMarker(GeoCoordinates geoCoordinates, int resourceId) {
        MapImage mapImage = MapImageFactory.fromResource(mainActivity.getApplicationContext().getResources(), resourceId);
        mapMarker = new MapMarker(geoCoordinates, mapImage);
        mapView.getMapScene().addMapMarker(mapMarker);
    }

    /**
     * Método para agregar un marcador en el mapa en las coordenadas especificadas.
     *
     * @param geoCoordinates Las coordenadas donde se agregará el marcador.
     */
    /*public void cleanPoint(){
        try {
            // Eliminar cualquier mapMarker existente en el Adaptador
            if(mapMarker!=null) mapView.getMapScene().removeMapMarker(mapMarker);
            mapMarker=null;
            for (PointWithId point : pointsWithIds) {
                mapView.getMapScene().removeMapMarker(point.mapMarker);
            }
            List<MapView.ViewPin> mapViewPins = mapView.getViewPins();
            for (MapView.ViewPin viewPin : new ArrayList<>(mapViewPins)) {
                for (PointWithId point : pointsWithIds) {
                    if(point.mapMarker.getCoordinates().latitude==viewPin.getGeoCoordinates().latitude && point.mapMarker.getCoordinates().longitude==viewPin.getGeoCoordinates().longitude){
                        viewPin.unpin();
                    }
                }
            }
            for (PointWithId point : pointsWithIds) {
                if(point.visibility) {
                    mapView.getMapScene().addMapMarker(point.mapMarker);
                }
                if(point.label){
                    // Crea un TextView para la etiqueta
                    TextView textView = new TextView(context);
                    textView.setTextColor(Color.parseColor("#7EB8D5"));
                    textView.setText(point.name);
                    textView.setTypeface(Typeface.DEFAULT_BOLD);
                    // Crea un LinearLayout para contener el TextView y agregar padding
                    LinearLayout linearLayout = new LinearLayout(context);
                    //linearLayout.setBackgroundResource(R.color.colorAccent);
                    linearLayout.setPadding(0, 0, 0, 130);
                    linearLayout.addView(textView);
                    // Ancla el LinearLayout al mapa en las coordenadas ajustadas
                    mapView.pinView(linearLayout, point.mapMarker.getCoordinates());
                }
            }
        }catch (Exception e){
            Log.e("Error",e.getMessage());
        }
    }*/

    /**
     * Método para dibujar un polígono en el mapa usando una lista de vértices.
     *
     * @param vertices Lista de coordenadas que componen el polígono.
     */
    public ModalBottomSheetFullScreenFragmentIncidencias getModalBottomSheetFullScreenFragment(){
        return bottomSheetFragment;
    }
}
