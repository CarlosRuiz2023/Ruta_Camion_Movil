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
    public GeoPolygon selected_geometry;
    public MapMarker selected_marker;
    public String estado, municipio;

    public AvoidZonesExample(Context context, MapView mapView, LayoutInflater layoutInflater,DatabaseHelper dbHelper) {
        this.context = context;
        this.mapView = mapView;
        this.layoutInflater = layoutInflater;
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
        }else{
            MapPolygon mapPolygon = null, mapPolygon1 = null, mapPolygon2 = null;
            List<GeoCoordinates> vertices = new ArrayList<>();
            vertices.add(new GeoCoordinates(21.101579497244543,-101.61594078421876));
            vertices.add(new GeoCoordinates(21.104292047419793,-101.61396701803433));
            vertices.add(new GeoCoordinates(21.102307254608633,-101.60879553009859));
            vertices.add(new GeoCoordinates(21.09807302994483,-101.61140884063329));
            vertices.add(new GeoCoordinates(21.099230825751338,-101.61590770433858));
            vertices.add(new GeoCoordinates(21.101579497244543,-101.61594078421876));
            List<GeoCoordinates> vertices1 = new ArrayList<>();
            vertices1.add(new GeoCoordinates(21.096174668016264,-101.63235086115643));
            vertices1.add(new GeoCoordinates(21.10113648473711,-101.62627516721253));
            vertices1.add(new GeoCoordinates(21.0961240372334,-101.62126271970882));
            vertices1.add(new GeoCoordinates(21.090048343289503,-101.62569291320958));
            vertices1.add(new GeoCoordinates(21.09192168225554,-101.63711015474581));
            vertices1.add(new GeoCoordinates(21.096174668016264,-101.63235086115643));
            List<GeoCoordinates> vertices2 = new ArrayList<>();
            vertices2.add(new GeoCoordinates(21.096998891067,-101.6302840110248));
            vertices2.add(new GeoCoordinates(21.101087283359682,-101.61989062820845));
            vertices2.add(new GeoCoordinates(21.09212237495886,-101.61900398891606));
            vertices2.add(new GeoCoordinates(21.08936394160476,-101.63240209377884));
            vertices2.add(new GeoCoordinates(21.096998891067,-101.6302840110248));
            try {
                mapPolygon = new MapPolygon(new GeoPolygon(vertices),new Color(1f, 0f, 0f, 0.63f));
                mapPolygon1 = new MapPolygon(new GeoPolygon(vertices1),new Color(1f, 0f, 0f, 0.63f));
                mapPolygon2 = new MapPolygon(new GeoPolygon(vertices2),new Color(1f, 0f, 0f, 0.63f));
            } catch (InstantiationErrorException e) {
                //throw new RuntimeException(e);
            }
            dbHelper.saveZona(mapPolygon,"Zona Prohibida","Guanajuato","Leon",0);
            dbHelper.saveZona(mapPolygon1,"Zona Prohibida2","Guanajuato","Leon",0);
            dbHelper.saveZona(mapPolygon2,"Zona Peligrosa","Guanajuato","Leon",1);
            // Recupera la lista de polígonos de la base de datos
            polygonWithIds = dbHelper.getAllZonas();
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
