package com.itsmarts.SmartRouteTruckApp.clases;

import android.content.Context;
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
import com.itsmarts.SmartRouteTruckApp.R;
import com.itsmarts.SmartRouteTruckApp.adaptadores.PointAdapter;
import com.itsmarts.SmartRouteTruckApp.api.ApiService;
import com.itsmarts.SmartRouteTruckApp.api.RetrofitClient;
import com.itsmarts.SmartRouteTruckApp.bd.DatabaseHelper;
import com.itsmarts.SmartRouteTruckApp.fragments.ModalBottomSheetFullScreenFragmentPuntos;
import com.itsmarts.SmartRouteTruckApp.modelos.PointWithId;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class ControlPointsExample {
    public List<MapMarker> markers = new ArrayList<>();
    public List<PointWithId> pointsWithIds = new ArrayList<>();
    public MapMarker mapMarker;
    public DatabaseHelper dbHelper;
    public PointAdapter adapter;
    // Declaración de la variable dialog
    private AlertDialog dialog;
    public MapView mapView;
    public Context context;
    private LayoutInflater layoutInflater;
    public ModalBottomSheetFullScreenFragmentPuntos bottomSheetFragment;
    public GeoCoordinates last_coordinates;
    public String estado, municipio;
    List<CompletableFuture<ResponseBody>> futures = new ArrayList<>();
    private static final String TAG = "ControlPointsExample";

    public ControlPointsExample(Context context, MapView mapView, LayoutInflater layoutInflater,DatabaseHelper dbHelper) {
        this.context = context;
        this.mapView = mapView;
        this.layoutInflater = layoutInflater;
        this.dbHelper = dbHelper;
        // Recupera la lista de polígonos de la base de datos
        pointsWithIds = dbHelper.getAllPuntos();
        if(pointsWithIds.size()>0){
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
        }else{
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
        }
        bottomSheetFragment = new ModalBottomSheetFullScreenFragmentPuntos(this);
    }

    /**
     * Método para agregar un marcador en el mapa en las coordenadas especificadas.
     *
     * @param geoCoordinates Las coordenadas donde se agregará el marcador.
     */
    private void addMapMarker(GeoCoordinates geoCoordinates, int resourceId) {
        MapImage mapImage = MapImageFactory.fromResource(context.getResources(), resourceId);
        mapMarker = new MapMarker(geoCoordinates, mapImage);
        mapView.getMapScene().addMapMarker(mapMarker);
    }

    private final SearchCallback geocodeAddressSearchCallback = new SearchCallback() {
        @Override
        public void onSearchCompleted(SearchError searchError, List<Place> list) {
            if (searchError != null) {
                Log.d("Search", "Error: " + searchError.toString());
                return;
            }

            for (Place geocodingResult : list) {
                Place place = list.get(0); // Obtenerel lugar con índice 0
                Address address = place.getAddress();

                estado = address.state;
                municipio = address.city;
            }
        }
    };

    /**
     * Método para agregar un marcador en el mapa en las coordenadas especificadas.
     *
     * @param geoCoordinates Las coordenadas donde se agregará el marcador.
     */
    public void showSavePointDialog(PointWithId pointWithId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // Infla el layout personalizado
        View dialogView = layoutInflater.inflate(R.layout.dialog_save_point, null);
        builder.setView(dialogView);
        // Obtén las referencias a los elementos del layout
        final EditText input = dialogView.findViewById(R.id.polygon_name_input);
        MaterialButton cancelButton = dialogView.findViewById(R.id.cancel_button);
        MaterialButton saveButton = dialogView.findViewById(R.id.save_button);
        input.setText(pointWithId.name);
        // Configura los listeners de los botones
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cierra el diálogo
                cleanPoint();
                dialog.dismiss();
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String polygonName = input.getText().toString();
                // Guarda el polígono en la base de datos
                dbHelper.updatePunto(pointWithId.id,pointWithId.mapMarker.getCoordinates(), polygonName);
                for (PointWithId point : pointsWithIds) {
                    if(point.id==pointWithId.id){
                        point.name=polygonName;
                        point.mapMarker.setCoordinates(last_coordinates);
                    }
                }
                cleanPoint();
                dialog.dismiss(); // Cierra el diálogo
            }
        });
        mapView.getGestures().setTapListener(null);
        // Muestra el diálogo
        dialog = builder.create();
        dialog.show();
    }

    /**
     * Método para agregar un marcador en el mapa en las coordenadas especificadas.
     *
     * @param geoCoordinates Las coordenadas donde se agregará el marcador.
     */
    public void cleanPoint(){
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
    }

    /**
     * Método para dibujar un polígono en el mapa usando una lista de vértices.
     *
     * @param vertices Lista de coordenadas que componen el polígono.
     */
    public ModalBottomSheetFullScreenFragmentPuntos getModalBottomSheetFullScreenFragment(){
        return bottomSheetFragment;
    }

    private CompletableFuture<ResponseBody> descargarPuntosDeControl() {
        try {
            CompletableFuture<ResponseBody> future = new CompletableFuture<>();
            ApiService apiService = RetrofitClient.getInstance(null).create(ApiService.class);
            apiService.getPuntosDeControl().enqueue(new retrofit2.Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            // Obtener el JSON como string
                            String jsonResponse = response.body().string();
                            // Convierte la respuesta en un objeto JSON
                            JSONObject jsonObject = new JSONObject(jsonResponse);
                            // Verifica si la operación fue exitosa
                            boolean success = jsonObject.getBoolean("success");
                            if (success) {
                                // Obtén el arreglo "result"
                                JSONArray puntosArray = jsonObject.getJSONArray("result");
                                // Itera sobre cada elemento en el arreglo
                                for (int i = 0; i < puntosArray.length(); i++) {
                                    JSONObject puntoObject = puntosArray.getJSONObject(i);
                                    // Extraer datos del punto
                                    int id = puntoObject.optInt("id_punto_de_control", 0);
                                    double latitud = puntoObject.optDouble("latitud", 0.0);
                                    double longitud = puntoObject.optDouble("longitud", 0.0);
                                    String nombre = puntoObject.optString("nombre", "Sin nombre");
                                    int id_estado = puntoObject.optInt("id_estado", 0);
                                    int id_municipio = puntoObject.optInt("id_municipio", 0);
                                    int status = puntoObject.optInt("estatus", 0);
                                    // Guardar el punto en la base de datos
                                    try {
                                        dbHelper.savePunto(
                                                id,
                                                new GeoCoordinates(latitud, longitud),
                                                nombre,
                                                id_estado,
                                                id_municipio,
                                                status
                                        );
                                    } catch (Exception e) {
                                        Log.e("Database", "Error al guardar el punto: " + e.getMessage());
                                    }
                                }
                            } else {
                                Log.e("Error", "La operación no fue exitosa.");
                            }
                            Log.d("Retrofit", "Puntos guardados correctamente.");
                            future.complete(response.body());
                        } catch (Exception e) {
                            Log.e("Retrofit", "Error al procesar el JSON: " + e.getMessage());
                        }
                    } else {
                        Log.e("Retrofit", "Error en la respuesta del servidor.");
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("Retrofit", "Error al obtener datos: " + t.getMessage());
                    future.completeExceptionally(t);
                }
            });
            return future;
        } catch (Error e) {
            Log.e(TAG, "Error en la solicitud de los puntos de control: ", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    public CompletableFuture<ResponseBody> descargarPuntosDeControlFaltantes() {
        try {
            CompletableFuture<ResponseBody> future = new CompletableFuture<>();
            ApiService apiService = RetrofitClient.getInstance(null).create(ApiService.class);
            apiService.getPuntosDeControl().enqueue(new retrofit2.Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            // Obtener el JSON como string
                            String jsonResponse = response.body().string();
                            // Convierte la respuesta en un objeto JSON
                            JSONObject jsonObject = new JSONObject(jsonResponse);
                            // Verifica si la operación fue exitosa
                            boolean success = jsonObject.getBoolean("success");
                            if (success) {
                                // Obtén el arreglo "result"
                                JSONArray puntosArray = jsonObject.getJSONArray("result");
                                // Itera sobre cada elemento en el arreglo
                                for (int i = 0; i < puntosArray.length(); i++) {
                                    JSONObject puntoObject = puntosArray.getJSONObject(i);
                                    // Extraer datos del punto
                                    int id = puntoObject.optInt("id_punto_de_control", 0);
                                    PointWithId punto_previo = null;
                                    try {
                                        punto_previo = dbHelper.getPuntoById(id);
                                    }catch (Exception e){
                                        Log.e(TAG,"Punto no encontrada en BD");
                                    }
                                    if(punto_previo!=null){
                                        continue;
                                    }
                                    double latitud = puntoObject.optDouble("latitud", 0.0);
                                    double longitud = puntoObject.optDouble("longitud", 0.0);
                                    String nombre = puntoObject.optString("nombre", "Sin nombre");
                                    int id_estado = puntoObject.optInt("id_estado", 0);
                                    int id_municipio = puntoObject.optInt("id_municipio", 0);
                                    int status = puntoObject.optInt("estatus", 0);
                                    // Guardar el punto en la base de datos
                                    try {
                                        dbHelper.savePunto(
                                                id,
                                                new GeoCoordinates(latitud, longitud),
                                                nombre,
                                                id_estado,
                                                id_municipio,
                                                status
                                        );
                                    } catch (Exception e) {
                                        Log.e("Database", "Error al guardar el punto: " + e.getMessage());
                                    }
                                }
                            } else {
                                Log.e("Error", "La operación no fue exitosa.");
                            }
                            Log.d("Retrofit", "Puntos actualizados correctamente.");
                            future.complete(response.body());
                        } catch (Exception e) {
                            Log.e("Retrofit", "Error al procesar el JSON: " + e.getMessage());
                        }
                    } else {
                        Log.e("Retrofit", "Error en la respuesta del servidor.");
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("Retrofit", "Error al obtener datos: " + t.getMessage());
                    future.completeExceptionally(t);
                }
            });
            return future;
        } catch (Error e) {
            Log.e(TAG, "Error en la solicitud de los puntos de control: ", e);
            return CompletableFuture.failedFuture(e);
        }
    }
}
