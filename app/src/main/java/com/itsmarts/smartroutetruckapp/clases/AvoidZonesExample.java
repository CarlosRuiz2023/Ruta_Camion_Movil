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
import com.itsmarts.smartroutetruckapp.bd.DatabaseHelper;
import com.itsmarts.smartroutetruckapp.fragments.ModalBottomSheetFullScreenFragmentZonas;
import com.itsmarts.smartroutetruckapp.modelos.PolygonWithId;

import java.util.ArrayList;
import java.util.List;

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
    public FloatingActionButton fbReset, fbSave;
    public LinearLayout llSave, llReset;
    public GeoPolygon selected_geometry;
    public MapMarker selected_marker;
    public String estado, municipio;

    public AvoidZonesExample(Context context, MapView mapView, LayoutInflater layoutInflater, FloatingActionButton fbReset, FloatingActionButton fbSave, LinearLayout llReset, LinearLayout llSave,DatabaseHelper dbHelper) {
        this.context = context;
        this.mapView = mapView;
        this.layoutInflater = layoutInflater;
        this.fbReset = fbReset;
        this.fbSave = fbSave;
        this.llSave = llSave;
        this.llReset = llReset;
        this.dbHelper = dbHelper;

        // Recupera la lista de polígonos de la base de datos
        polygonWithIds = dbHelper.getAllZonas();
        if(polygonWithIds.size()>0){
            for (PolygonWithId polygonWithId : polygonWithIds) {
                poligonos.add(polygonWithId.polygon);
                if(polygonWithId.status){
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

    /**
     * Método para dibujar un polígono en el mapa usando una lista de vértices.
     *
     * @param vertices Lista de coordenadas que componen el polígono.
     */
    public void showSavePolygonDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // Infla el layout personalizado
        View dialogView = layoutInflater.inflate(R.layout.dialog_save_polygon, null);
        builder.setView(dialogView);

        // Obtén las referencias a los elementos del layout
        final EditText input = dialogView.findViewById(R.id.polygon_name_input);
        final CheckBox is_dangerous_checkbox = dialogView.findViewById(R.id.is_dangerous_checkbox);
        MaterialButton cancelButton = dialogView.findViewById(R.id.cancel_button);
        MaterialButton saveButton = dialogView.findViewById(R.id.save_button);

        // Configura los listeners de los botones
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cierra el diálogo
                llSave.setVisibility(View.GONE);
                cleanPolygon();
                dialog.dismiss();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String polygonName = input.getText().toString();
                if(polygonName.isEmpty()){
                    Toast.makeText(context, "Ingrese un nombre para el polígono", Toast.LENGTH_SHORT).show();
                    input.setError("Ingrese un nombre para el polígono");
                    return;
                }
                AddressQuery query = new AddressQuery("",calculateCentroid(mapPolygon.getGeometry().vertices));
                SearchOptions options = new SearchOptions();
                SearchEngine searchEngine = null;
                try {
                    searchEngine = new SearchEngine();
                } catch (InstantiationErrorException e) {
                    //throw new RuntimeException(e);
                }
                searchEngine.search(query, options, geocodeAddressSearchCallback);
                int is_dangerous = is_dangerous_checkbox.isChecked() ? 1 : 0;
                // Guarda el polígono en la base de datos
                dbHelper.saveZona(mapPolygon, polygonName,estado,municipio,is_dangerous);
                for (PolygonWithId polygon : polygonWithIds) {
                    mapView.getMapScene().removeMapPolygon(polygon.polygon);
                }
                polygonWithIds = dbHelper.getAllZonas();
                llSave.setVisibility(View.GONE);
                cleanPolygon();
                dialog.dismiss(); // Cierra el diálogo
            }
        });
        mapView.getGestures().setTapListener(null);

        // Muestra el diálogo
        dialog = builder.create();
        dialog.show();
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
    public void showSavePolygonDialog(PolygonWithId polygonWithId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // Infla el layout personalizado
        View dialogView = layoutInflater.inflate(R.layout.dialog_save_polygon, null);
        builder.setView(dialogView);

        // Obtén las referencias a los elementos del layout
        final EditText input = dialogView.findViewById(R.id.polygon_name_input);
        input.setText(polygonWithId.name);
        MaterialButton cancelButton = dialogView.findViewById(R.id.cancel_button);
        MaterialButton saveButton = dialogView.findViewById(R.id.save_button);

        // Configura los listeners de los botones
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                polygonWithId.polygon.setGeometry(selected_geometry);
                cleanPolygon();
                llReset.setVisibility(View.GONE);
                llSave.setVisibility(View.GONE);
                dialog.dismiss();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String polygonName = input.getText().toString();
                // Guarda el polígono en la base de datos
                dbHelper.updateZona(polygonWithId.id,mapPolygon, polygonName);
                polygonWithIds = dbHelper.getAllZonas();
                cleanPolygon();
                llReset.setVisibility(View.GONE);
                llSave.setVisibility(View.GONE);
                dialog.dismiss(); // Cierra el diálogo
            }
        });
        mapView.getGestures().setTapListener(null);

        // Muestra el diálogo
        dialog = builder.create();
        dialog.show();
    }

    /**
     * Método para dibujar un polígono en el mapa usando una lista de vértices.
     *
     * @param vertices Lista de coordenadas que componen el polígono.
     */
    public void startGestures(){
        // Configurar el listener de clics en el mapa
        mapView.getGestures().setTapListener(mapViewPoint-> {
            llReset.setVisibility(View.VISIBLE);
            // Obtener las coordenadas geográficas del punto.
            GeoCoordinates geoCoordinates = mapView.viewToGeoCoordinates(mapViewPoint);
            // Agregar la coordenada clicada a la lista de vértices
            polygonVertices.add(geoCoordinates);
            // Agregar un marcador en la coordenada clicada
            addMapMarker(geoCoordinates, R.drawable.red_dot);
            // Si hay al menos tres vértices, dibujar el polígono
            if (polygonVertices.size() > 2) {
                llSave.setVisibility(View.VISIBLE);
                drawPolygon(polygonVertices);
            }
        });
    }

    /**
     * Método para dibujar un polígono en el mapa usando una lista de vértices.
     *
     * @param vertices Lista de coordenadas que componen el polígono.
     */
    public void startGestures(PolygonWithId polygonWithId){
        // Configurar el listener de clics en el mapa
        mapView.getGestures().setTapListener(mapViewPoint-> {
            llReset.setVisibility(View.VISIBLE);
            // Obtener las coordenadas geográficas del punto.
            GeoCoordinates geoCoordinates = mapView.viewToGeoCoordinates(mapViewPoint);
            // Agregar la coordenada clicada a la lista de vértices
            polygonVertices.add(geoCoordinates);
            // Agregar un marcador en la coordenada clicada
            addMapMarker(geoCoordinates, R.drawable.red_dot);
            // Si hay al menos tres vértices, dibujar el polígono
            if (polygonVertices.size() > 2) {
                llSave.setVisibility(View.VISIBLE);
                fbSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showSavePolygonDialog(polygonWithId);
                    }
                });
                drawPolygon(polygonVertices);
            }
        });
    }

    /**
     * Método para dibujar un polígono en el mapa usando una lista de vértices.
     *
     * @param vertices Lista de coordenadas que componen el polígono.
     */
    public void cleanPolygon(){
        llSave.setVisibility(View.GONE);
        llReset.setVisibility(View.GONE);
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
            if(polygon.status) {
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
        fbSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Eliminar cualquier polígono existente en el mapa
                if(mapPolygon!=null){
                    showSavePolygonDialog();
                } else {
                    Toast.makeText(context, "Genere un polígono válido antes de guardar", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
}
