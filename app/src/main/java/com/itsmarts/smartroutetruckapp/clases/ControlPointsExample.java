package com.itsmarts.smartroutetruckapp.clases;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.errors.InstantiationErrorException;
import com.here.sdk.mapview.MapImage;
import com.here.sdk.mapview.MapImageFactory;
import com.here.sdk.mapview.MapMarker;
import com.here.sdk.mapview.MapView;
import com.here.sdk.search.Address;
import com.here.sdk.search.AddressQuery;
import com.here.sdk.search.Place;
import com.here.sdk.search.SearchCallback;
import com.here.sdk.search.SearchEngine;
import com.here.sdk.search.SearchError;
import com.here.sdk.search.SearchOptions;
import com.itsmarts.smartroutetruckapp.R;
import com.itsmarts.smartroutetruckapp.adaptadores.PointAdapter;
import com.itsmarts.smartroutetruckapp.bd.DatabaseHelper;
import com.itsmarts.smartroutetruckapp.fragments.ModalBottomSheetFullScreenFragmentPuntos;
import com.itsmarts.smartroutetruckapp.modelos.PointWithId;

import java.util.ArrayList;
import java.util.List;

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
    public FloatingActionButton fabSave;
    public LinearLayout llSave;
    public GeoCoordinates last_coordinates;
    public String estado, municipio;

    public ControlPointsExample(Context context, MapView mapView, LayoutInflater layoutInflater, FloatingActionButton fabSave, LinearLayout llSave,DatabaseHelper dbHelper) {
        this.context = context;
        this.mapView = mapView;
        this.layoutInflater = layoutInflater;
        this.fabSave = fabSave;
        this.llSave = llSave;
        this.dbHelper = dbHelper;
        // Recupera la lista de polígonos de la base de datos
        pointsWithIds = dbHelper.getAllPuntos();
        if(pointsWithIds.size()>0){
            for (PointWithId point : pointsWithIds) {
                markers.add(point.mapMarker);
                if(point.status){
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

    /**
     * Método para agregar un marcador en el mapa en las coordenadas especificadas.
     *
     * @param geoCoordinates Las coordenadas donde se agregará el marcador.
     */
    public void showSavePointDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // Infla el layout personalizado
        View dialogView = layoutInflater.inflate(R.layout.dialog_save_point, null);
        builder.setView(dialogView);

        // Obtén las referencias a los elementos del layout
        final EditText input = dialogView.findViewById(R.id.polygon_name_input);
        MaterialButton cancelButton = dialogView.findViewById(R.id.cancel_button);
        MaterialButton saveButton = dialogView.findViewById(R.id.save_button);

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
                String pointName = input.getText().toString();
                if(pointName.isEmpty()){
                    Toast.makeText(context, "Ingrese un nombre para el punto", Toast.LENGTH_SHORT).show();
                    input.setError("Ingrese un nombre para el punto");
                    return;
                }
                AddressQuery query = new AddressQuery("",mapMarker.getCoordinates());
                SearchOptions options = new SearchOptions();
                SearchEngine searchEngine = null;
                try {
                    searchEngine = new SearchEngine();
                } catch (InstantiationErrorException e) {
                    //throw new RuntimeException(e);
                }
                searchEngine.search(query, options, geocodeAddressSearchCallback);
                // Guarda el polígono en la base de datos
                dbHelper.savePunto(mapMarker.getCoordinates(), pointName,estado,municipio);
                for (PointWithId point : pointsWithIds) {
                    mapView.getMapScene().removeMapMarker(point.mapMarker);
                }
                pointsWithIds = dbHelper.getAllPuntos();
                cleanPoint();
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
    public void startGestures(){
        // Configurar el listener de clics en el mapa
        mapView.getGestures().setTapListener(mapViewPoint-> {
            llSave.setVisibility(View.VISIBLE);
            // Obtener las coordenadas geográficas del punto.
            GeoCoordinates geoCoordinates = mapView.viewToGeoCoordinates(mapViewPoint);
            if(mapMarker!=null) mapView.getMapScene().removeMapMarker(mapMarker);
            // Agregar un marcador en la coordenada clicada
            addMapMarker(geoCoordinates, R.drawable.punto_control);
        });
    }

    /**
     * Método para agregar un marcador en el mapa en las coordenadas especificadas.
     *
     * @param geoCoordinates Las coordenadas donde se agregará el marcador.
     */
    public void cleanPoint(){
        llSave.setVisibility(View.GONE);
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
            if(point.status) {
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
    }

    /**
     * Método para dibujar un polígono en el mapa usando una lista de vértices.
     *
     * @param vertices Lista de coordenadas que componen el polígono.
     */
    public ModalBottomSheetFullScreenFragmentPuntos getModalBottomSheetFullScreenFragment(){
        fabSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Eliminar cualquier polígono existente en el mapa
                if(mapMarker!=null){
                    showSavePointDialog();
                } else {
                    Toast.makeText(context, "De click en el mapa para agregar un punto", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return bottomSheetFragment;
    }
}
