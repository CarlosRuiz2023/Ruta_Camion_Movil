package com.itsmarts.smartroutetruckapp.clases;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.here.sdk.core.Color;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.GeoPolygon;
import com.here.sdk.core.errors.InstantiationErrorException;
import com.here.sdk.mapview.MapImage;
import com.here.sdk.mapview.MapImageFactory;
import com.here.sdk.mapview.MapMarker;
import com.here.sdk.mapview.MapPolygon;
import com.here.sdk.mapview.MapView;
import com.here.sdk.search.Address;
import com.here.sdk.search.AddressQuery;
import com.here.sdk.search.Place;
import com.here.sdk.search.SearchCallback;
import com.here.sdk.search.SearchEngine;
import com.here.sdk.search.SearchError;
import com.here.sdk.search.SearchOptions;
import com.itsmarts.smartroutetruckapp.R;
import com.itsmarts.smartroutetruckapp.adaptadores.PolygonAdapter;
import com.itsmarts.smartroutetruckapp.api.ApiService;
import com.itsmarts.smartroutetruckapp.api.RetrofitClient;
import com.itsmarts.smartroutetruckapp.bd.DatabaseHelper;
import com.itsmarts.smartroutetruckapp.fragments.ModalBottomSheetFullScreenFragmentZonas;
import com.itsmarts.smartroutetruckapp.modelos.PointWithId;
import com.itsmarts.smartroutetruckapp.modelos.PolygonWithId;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class AvoidZonesExample {
    public List<GeoCoordinates> polygonVertices = new ArrayList<>();
    public List<PolygonWithId> polygonWithIds = new ArrayList<>();
    public List<MapPolygon> poligonos = new ArrayList<>();
    public List<MapMarker> markers = new ArrayList<>();
    public MapPolygon mapPolygon =null;
    public DatabaseHelper dbHelper;
    public PolygonAdapter adapter;
    // Declaración de la variable dialog
    private AlertDialog dialog;
    private RecyclerView recyclerView;
    public MapView mapView;
    public Context context;
    private LayoutInflater layoutInflater;
    public ModalBottomSheetFullScreenFragmentZonas bottomSheetFragment;
    public GeoPolygon selected_geometry;
    public MapMarker selected_marker;
    public String estado, municipio;
    List<CompletableFuture<ResponseBody>> futures = new ArrayList<>();
    private static final String TAG = "AvoidZonesExample";

    public AvoidZonesExample(Context context, MapView mapView, LayoutInflater layoutInflater,DatabaseHelper dbHelper) {
        this.context = context;
        this.mapView = mapView;
        this.layoutInflater = layoutInflater;
        this.dbHelper = dbHelper;

        // Recupera la lista de polígonos de la base de datos
        polygonWithIds = dbHelper.getAllZonas();
        if(polygonWithIds.size()>0){
            // Agrega todas las llamadas a los métodos de descarga
            futures.add(descargarZonasPeligrosasFaltantes());
            futures.add(descargarZonasProhibidasFaltantes());
            // Espera a que todos los futures terminen
            CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            allOf.whenComplete((result, ex) -> {
                if (ex != null) {
                    // Manejo de errores, si es necesario
                    Log.e(TAG, "Error during downloads", ex);
                }
                // Recupera la lista de polígonos de la base de datos
                polygonWithIds = dbHelper.getAllZonas();
                for (PolygonWithId polygonWithId : polygonWithIds) {
                    poligonos.add(polygonWithId.polygon);
                    if(polygonWithId.visibility){
                        mapView.getMapScene().addMapPolygon(polygonWithId.polygon);
                    }
                    if(polygonWithId.label){
                        // Crea un TextView para la etiqueta
                        TextView textView = new TextView(context);
                        if(polygonWithId.peligrosa){
                            textView.setTextColor(android.graphics.Color.parseColor("#000000"));
                        }else{
                            textView.setTextColor(android.graphics.Color.parseColor("#FF0000"));
                        }
                        textView.setText(polygonWithId.name);
                        textView.setTypeface(Typeface.DEFAULT_BOLD);

                        // Crea un LinearLayout para contener el TextView y agregar padding
                        LinearLayout linearLayout = new LinearLayout(context);
                        //linearLayout.setBackgroundResource(R.color.colorAccent);
                        //linearLayout.setPadding(0, 0, 0, 130);
                        linearLayout.addView(textView);

                        // Crear un nuevo GeoCoordinates para el punto medio
                        GeoCoordinates midpoint = calculateCentroid(polygonWithId.polygon.getGeometry().vertices);

                        // Usar el punto medio para anclar la vista
                        mapView.pinView(linearLayout, midpoint);
                    }
                }
            });
        }else{
            // Agrega todas las llamadas a los métodos de descarga
            futures.add(descargarZonasPeligrosas());
            futures.add(descargarZonasProhibidas());
            // Espera a que todos los futures terminen
            CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            allOf.whenComplete((result, ex) -> {
                if (ex != null) {
                    // Manejo de errores, si es necesario
                    Log.e(TAG, "Error during downloads", ex);
                }
                // Recupera la lista de polígonos de la base de datos
                polygonWithIds = dbHelper.getAllZonas();
                for (PolygonWithId polygonWithId : polygonWithIds) {
                    poligonos.add(polygonWithId.polygon);
                    if(polygonWithId.visibility){
                        mapView.getMapScene().addMapPolygon(polygonWithId.polygon);
                    }
                    if(polygonWithId.label){
                        // Crea un TextView para la etiqueta
                        TextView textView = new TextView(context);
                        if(polygonWithId.peligrosa){
                            textView.setTextColor(android.graphics.Color.parseColor("#000000"));
                        }else{
                            textView.setTextColor(android.graphics.Color.parseColor("#FF0000"));
                        }
                        textView.setText(polygonWithId.name);
                        textView.setTypeface(Typeface.DEFAULT_BOLD);

                        // Crea un LinearLayout para contener el TextView y agregar padding
                        LinearLayout linearLayout = new LinearLayout(context);
                        //linearLayout.setBackgroundResource(R.color.colorAccent);
                        //linearLayout.setPadding(0, 0, 0, 130);
                        linearLayout.addView(textView);

                        // Crear un nuevo GeoCoordinates para el punto medio
                        GeoCoordinates midpoint = calculateCentroid(polygonWithId.polygon.getGeometry().vertices);

                        // Usar el punto medio para anclar la vista
                        mapView.pinView(linearLayout, midpoint);
                    }
                }
            });
        }
        bottomSheetFragment = new ModalBottomSheetFullScreenFragmentZonas(this);
    }

    /**
     * Método para agregar un marcador en el mapa en las coordenadas especificadas.
     *
     * @param geoCoordinates Las coordenadas donde se agregará el marcador.
     */
    private void addMapMarker(GeoCoordinates geoCoordinates, int resourceId) {
        MapImage mapImage = MapImageFactory.fromResource(context.getResources(), resourceId);
        MapMarker mapMarker = new MapMarker(geoCoordinates, mapImage);
        mapView.getMapScene().addMapMarker(mapMarker);
        markers.add(mapMarker);
    }

    /**
     * Método para dibujar un polígono en el mapa usando una lista de vértices.
     *
     * @param vertices Lista de coordenadas que componen el polígono.
     */
    private void drawPolygon(List<GeoCoordinates> vertices) {
        // Eliminar cualquier polígono existente en el mapa
        if(mapPolygon!=null)mapView.getMapScene().removeMapPolygon(mapPolygon);

        // Crear un objeto PolygonLite con los vértices
        GeoPolygon geoPolygon=null;
        try {
            geoPolygon = new GeoPolygon(vertices);
        } catch (InstantiationErrorException e) {
            // Less than three vertices.
            //return null;
        }

        Color fillColor = Color.valueOf(1f, 0f, 0f, 0.63f); // RGBA
        mapPolygon = new MapPolygon(geoPolygon, fillColor);

        // Agregar el polígono al mapa
        mapView.getMapScene().addMapPolygon(mapPolygon);
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
     * Método para dibujar un polígono en el mapa usando una lista de vértices.
     *
     * @param vertices Lista de coordenadas que componen el polígono.
     */
    public void cleanPolygon(){
        // Eliminar cualquier polígono existente
        if(mapPolygon!=null)mapView.getMapScene().removeMapPolygon(mapPolygon);
        for(PolygonWithId polygon : polygonWithIds){
            mapView.getMapScene().removeMapPolygon(polygon.polygon);
        }
        if(polygonVertices.size()>0){
            for (MapMarker marker : markers) {
                mapView.getMapScene().removeMapMarker(marker);
            }
            markers.clear();
            polygonVertices.clear();
        }
        List<MapView.ViewPin> mapViewPins = mapView.getViewPins();
        for (MapView.ViewPin viewPin : new ArrayList<>(mapViewPins)) {
            for (PolygonWithId polygon : polygonWithIds) {
                GeoCoordinates geoCoordinates = calculateCentroid(polygon.polygon.getGeometry().vertices);
                if(geoCoordinates.latitude==viewPin.getGeoCoordinates().latitude && geoCoordinates.longitude==viewPin.getGeoCoordinates().longitude){
                    viewPin.unpin();
                }
            }
        }
        for (PolygonWithId polygon : polygonWithIds) {
            if(polygon.visibility) {
                mapView.getMapScene().addMapPolygon(polygon.polygon);
            }
            if(polygon.label){
                // Crea un TextView para la etiqueta
                TextView textView = new TextView(context);
                if(polygon.peligrosa){
                    textView.setTextColor(android.graphics.Color.parseColor("#000000"));
                }else{
                    textView.setTextColor(android.graphics.Color.parseColor("#FF0000"));
                }
                textView.setText(polygon.name);
                textView.setTypeface(Typeface.DEFAULT_BOLD);

                // Crea un LinearLayout para contener el TextView y agregar padding
                LinearLayout linearLayout = new LinearLayout(context);
                //linearLayout.setBackgroundResource(R.color.colorAccent);
                //linearLayout.setPadding(0, 0, 0, 130);
                linearLayout.addView(textView);

                // Crear un nuevo GeoCoordinates para el punto medio
                GeoCoordinates midpoint = calculateCentroid(polygon.polygon.getGeometry().vertices);

                // Usar el punto medio para anclar la vista
                mapView.pinView(linearLayout, midpoint);
            }
        }
    }

    /**
     * Método para dibujar un polígono en el mapa usando una lista de vértices.
     *
     * @param vertices Lista de coordenadas que componen el polígono.
     */
    public ModalBottomSheetFullScreenFragmentZonas getModalBottomSheetFullScreenFragment(){
        return bottomSheetFragment;
    }

    /**
     * Método para dibujar un polígono en el mapa usando una lista de vértices.
     *
     * @param vertices Lista de coordenadas que componen el polígono.
     */
    public GeoCoordinates calculateCentroid(List<GeoCoordinates> vertices){
        // Calcular el centroide
        double area = 0.0;
        double cx = 0.0;
        double cy = 0.0;

        for (int i = 0; i < vertices.size() - 1; i++) {
            double x1 = vertices.get(i).longitude;
            double y1 = vertices.get(i).latitude;
            double x2 = vertices.get(i + 1).longitude;
            double y2 = vertices.get(i + 1).latitude;double temp = x1 * y2 - x2 * y1;
            area += temp;
            cx += (x1 + x2) * temp;
            cy += (y1 + y2) * temp;
        }

        // Cerrar el polígono
        double x1 = vertices.get(vertices.size() - 1).longitude;
        double y1 = vertices.get(vertices.size() - 1).latitude;
        double x2 = vertices.get(0).longitude;
        double y2 = vertices.get(0).latitude;

        double temp = x1 * y2 - x2 * y1;
        area += temp;
        cx += (x1 + x2) * temp;
        cy += (y1 + y2) * temp;

        area /= 2.0;
        cx /= (6.0 * area);
        cy /= (6.0 * area);

        // Crear un nuevo GeoCoordinates para el centroide
        GeoCoordinates centroid = new GeoCoordinates(cy, cx);
        return centroid;
    }

    private CompletableFuture<ResponseBody> descargarZonasPeligrosas() {
        try {
            CompletableFuture<ResponseBody> future = new CompletableFuture<>();
            ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
            apiService.getZonasPeligrosas().enqueue(new retrofit2.Callback<ResponseBody>() {
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
                                    JSONObject zonaObject = puntosArray.getJSONObject(i);
                                    // Extraer datos del punto
                                    int id = zonaObject.optInt("id_zona", 0);
                                    String nombre = zonaObject.optString("nombre", "Sin nombre");
                                    int id_estado = zonaObject.optInt("id_estado", 0);
                                    int id_municipio = zonaObject.optInt("id_municipio", 0);
                                    Boolean peligrosa = zonaObject.optBoolean("peligrosa", true);
                                    String verticesString = zonaObject.optString("vertices", "");
                                    int status = zonaObject.optInt("estatus", 0);
                                    MapPolygon mapPolygon = null;
                                    List<GeoCoordinates> vertices = new ArrayList<>();
                                    String[] vertexPairs = verticesString.split("\\],\\[");

                                    for (String vertexPair : vertexPairs) {
                                        // Remove extra square brackets
                                        vertexPair = vertexPair.replace("[", "").replace("]", "");
                                        String[] coords = vertexPair.split(",");
                                        try {
                                            double latitude = Double.parseDouble(coords[0].substring(1, coords[0].length() - 1));
                                            double longitude = Double.parseDouble(coords[1].substring(1, coords[1].length() - 1));
                                            vertices.add(new GeoCoordinates(latitude, longitude));
                                        } catch (NumberFormatException e) {
                                            Log.e("Error", "Invalid coordinate format: " + vertexPair);
                                        }
                                    }

                                    try {
                                        mapPolygon = new MapPolygon(new GeoPolygon(vertices), new Color(1f, 0f, 0f, 0.63f));
                                        // Use the mapPolygon object
                                    } catch (InstantiationErrorException e) {
                                        Log.e("Error", "Error creating MapPolygon: " + e.getMessage());
                                    } catch (IllegalArgumentException e) {
                                        Log.e("Error", "Invalid GeoPolygon: " + e.getMessage());
                                    }
                                    // Guardar la zona en la base de datos
                                    try {
                                        dbHelper.saveZona(
                                                id,
                                                mapPolygon,
                                                nombre,
                                                id_estado,
                                                id_municipio,
                                                peligrosa,
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

    private CompletableFuture<ResponseBody> descargarZonasPeligrosasFaltantes() {
        try {
            CompletableFuture<ResponseBody> future = new CompletableFuture<>();
            ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
            apiService.getZonasPeligrosas().enqueue(new retrofit2.Callback<ResponseBody>() {
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
                                    JSONObject zonaObject = puntosArray.getJSONObject(i);
                                    // Extraer datos del punto
                                    int id = zonaObject.optInt("id_zona", 0);
                                    PolygonWithId zona_previa = null;
                                    try {
                                        zona_previa = dbHelper.getZonaById(id);
                                    }catch (Exception e){
                                        Log.e(TAG,"Zona no encontrada en BD");
                                    }
                                    if(zona_previa!=null){
                                        continue;
                                    }
                                    String nombre = zonaObject.optString("nombre", "Sin nombre");
                                    int id_estado = zonaObject.optInt("id_estado", 0);
                                    int id_municipio = zonaObject.optInt("id_municipio", 0);
                                    Boolean peligrosa = zonaObject.optBoolean("peligrosa", true);
                                    String verticesString = zonaObject.optString("vertices", "");
                                    int status = zonaObject.optInt("estatus", 0);
                                    MapPolygon mapPolygon = null;
                                    List<GeoCoordinates> vertices = new ArrayList<>();
                                    String[] vertexPairs = verticesString.split("\\],\\[");

                                    for (String vertexPair : vertexPairs) {
                                        // Remove extra square brackets
                                        vertexPair = vertexPair.replace("[", "").replace("]", "");
                                        String[] coords = vertexPair.split(",");
                                        try {
                                            double latitude = Double.parseDouble(coords[0].substring(1, coords[0].length() - 1));
                                            double longitude = Double.parseDouble(coords[1].substring(1, coords[1].length() - 1));
                                            vertices.add(new GeoCoordinates(latitude, longitude));
                                        } catch (NumberFormatException e) {
                                            Log.e("Error", "Invalid coordinate format: " + vertexPair);
                                        }
                                    }

                                    try {
                                        mapPolygon = new MapPolygon(new GeoPolygon(vertices), new Color(1f, 0f, 0f, 0.63f));
                                        // Use the mapPolygon object
                                    } catch (InstantiationErrorException e) {
                                        Log.e("Error", "Error creating MapPolygon: " + e.getMessage());
                                    } catch (IllegalArgumentException e) {
                                        Log.e("Error", "Invalid GeoPolygon: " + e.getMessage());
                                    }
                                    // Guardar la zona en la base de datos
                                    try {
                                        dbHelper.saveZona(
                                                id,
                                                mapPolygon,
                                                nombre,
                                                id_estado,
                                                id_municipio,
                                                peligrosa,
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

    private CompletableFuture<ResponseBody> descargarZonasProhibidas() {
        try {
            CompletableFuture<ResponseBody> future = new CompletableFuture<>();
            ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
            apiService.getZonasProhibidas().enqueue(new retrofit2.Callback<ResponseBody>() {
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
                                    JSONObject zonaObject = puntosArray.getJSONObject(i);
                                    // Extraer datos del punto
                                    int id = zonaObject.optInt("id_zona", 0);
                                    String nombre = zonaObject.optString("nombre", "Sin nombre");
                                    int id_estado = zonaObject.optInt("id_estado", 0);
                                    int id_municipio = zonaObject.optInt("id_municipio", 0);
                                    Boolean peligrosa = zonaObject.optBoolean("peligrosa", false);
                                    String verticesString = zonaObject.optString("vertices", "");
                                    int status = zonaObject.optInt("estatus", 0);
                                    MapPolygon mapPolygon = null;
                                    List<GeoCoordinates> vertices = new ArrayList<>();
                                    String[] vertexPairs = verticesString.split("\\],\\[");

                                    for (String vertexPair : vertexPairs) {
                                        // Remove extra square brackets
                                        vertexPair = vertexPair.replace("[", "").replace("]", "");
                                        String[] coords = vertexPair.split(",");
                                        try {
                                            double latitude = Double.parseDouble(coords[0].substring(1, coords[0].length() - 1));
                                            double longitude = Double.parseDouble(coords[1].substring(1, coords[1].length() - 1));
                                            vertices.add(new GeoCoordinates(latitude, longitude));
                                        } catch (NumberFormatException e) {
                                            Log.e("Error", "Invalid coordinate format: " + vertexPair);
                                        }
                                    }

                                    try {
                                        mapPolygon = new MapPolygon(new GeoPolygon(vertices), new Color(1f, 0f, 0f, 0.63f));
                                        // Use the mapPolygon object
                                    } catch (InstantiationErrorException e) {
                                        Log.e("Error", "Error creating MapPolygon: " + e.getMessage());
                                    } catch (IllegalArgumentException e) {
                                        Log.e("Error", "Invalid GeoPolygon: " + e.getMessage());
                                    }
                                    // Guardar la zona en la base de datos
                                    try {
                                        dbHelper.saveZona(
                                                id,
                                                mapPolygon,
                                                nombre,
                                                id_estado,
                                                id_municipio,
                                                peligrosa,
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

    private CompletableFuture<ResponseBody> descargarZonasProhibidasFaltantes() {
        try {
            CompletableFuture<ResponseBody> future = new CompletableFuture<>();
            ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
            apiService.getZonasProhibidas().enqueue(new retrofit2.Callback<ResponseBody>() {
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
                                    JSONObject zonaObject = puntosArray.getJSONObject(i);
                                    // Extraer datos del punto
                                    int id = zonaObject.optInt("id_zona", 0);
                                    PolygonWithId zona_previa = dbHelper.getZonaById(id);
                                    if(zona_previa!=null){
                                        continue;
                                    }
                                    String nombre = zonaObject.optString("nombre", "Sin nombre");
                                    int id_estado = zonaObject.optInt("id_estado", 0);
                                    int id_municipio = zonaObject.optInt("id_municipio", 0);
                                    Boolean peligrosa = zonaObject.optBoolean("peligrosa", false);
                                    String verticesString = zonaObject.optString("vertices", "");
                                    int status = zonaObject.optInt("estatus", 0);
                                    MapPolygon mapPolygon = null;
                                    List<GeoCoordinates> vertices = new ArrayList<>();
                                    String[] vertexPairs = verticesString.split("\\],\\[");

                                    for (String vertexPair : vertexPairs) {
                                        // Remove extra square brackets
                                        vertexPair = vertexPair.replace("[", "").replace("]", "");
                                        String[] coords = vertexPair.split(",");
                                        try {
                                            double latitude = Double.parseDouble(coords[0].substring(1, coords[0].length() - 1));
                                            double longitude = Double.parseDouble(coords[1].substring(1, coords[1].length() - 1));
                                            vertices.add(new GeoCoordinates(latitude, longitude));
                                        } catch (NumberFormatException e) {
                                            Log.e("Error", "Invalid coordinate format: " + vertexPair);
                                        }
                                    }

                                    try {
                                        mapPolygon = new MapPolygon(new GeoPolygon(vertices), new Color(1f, 0f, 0f, 0.63f));
                                        // Use the mapPolygon object
                                    } catch (InstantiationErrorException e) {
                                        Log.e("Error", "Error creating MapPolygon: " + e.getMessage());
                                    } catch (IllegalArgumentException e) {
                                        Log.e("Error", "Invalid GeoPolygon: " + e.getMessage());
                                    }
                                    // Guardar la zona en la base de datos
                                    try {
                                        dbHelper.saveZona(
                                                id,
                                                mapPolygon,
                                                nombre,
                                                id_estado,
                                                id_municipio,
                                                peligrosa,
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
}
