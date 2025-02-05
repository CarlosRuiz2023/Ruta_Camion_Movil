package com.itsmarts.SmartRouteTruckApp.clases;

import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.mapview.MapImage;
import com.here.sdk.mapview.MapImageFactory;
import com.here.sdk.mapview.MapMarker;
import com.here.sdk.mapview.MapView;
import com.itsmarts.SmartRouteTruckApp.MainActivity;
import com.itsmarts.SmartRouteTruckApp.R;
import com.itsmarts.SmartRouteTruckApp.adaptadores.PointAdapter;
import com.itsmarts.SmartRouteTruckApp.bd.DatabaseHelper;
import com.itsmarts.SmartRouteTruckApp.fragments.ModalBottomSheetFullScreenFragmentIncidencias;
import com.itsmarts.SmartRouteTruckApp.modelos.Incidencia;
import com.itsmarts.SmartRouteTruckApp.modelos.PointWithId;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import okhttp3.ResponseBody;

public class IncidenciasExample {
    public List<Incidencia> incidencias = new ArrayList<>();
    public List<MapMarker> mapMarkersIncidencias = new ArrayList<>();
    public DatabaseHelper dbHelper;
    public MapView mapView;
    public MainActivity mainActivity;
    private LayoutInflater layoutInflater;
    public ModalBottomSheetFullScreenFragmentIncidencias bottomSheetFragment;
    private static final String TAG = "IncidenciasExample";

    public IncidenciasExample(MainActivity mainActivity, MapView mapView, LayoutInflater layoutInflater, DatabaseHelper dbHelper) {
        this.mainActivity = mainActivity;
        this.mapView = mapView;
        this.layoutInflater = layoutInflater;
        this.dbHelper = dbHelper;
        // Recupera la lista de polígonos de la base de datos
        incidencias = dbHelper.getAllIncidencias();
        mostrarIncidencias();
        bottomSheetFragment = new ModalBottomSheetFullScreenFragmentIncidencias(this);
    }

    /**
     * Método para dibujar un polígono en el mapa usando una lista de vértices.
     *
     * @param vertices Lista de coordenadas que componen el polígono.
     */
    public ModalBottomSheetFullScreenFragmentIncidencias getModalBottomSheetFullScreenFragment(){
        return bottomSheetFragment;
    }

    /**
     * Método para dibujar un polígono en el mapa usando una lista de vértices.
     *
     * @param vertices Lista de coordenadas que componen el polígono.
     */
    public int obtenerIdTipoIncidencia(String selectedIncidentType){
        int id_tipo_incidencia = 0;
        switch (selectedIncidentType) {
            case "Infracción de transito":
                id_tipo_incidencia = 1;
                break;
            case "Cierre vial por obras en la zona":
                id_tipo_incidencia = 2;
                break;
            case "Cierre vial por evento en la zona":
                id_tipo_incidencia = 3;
                break;
            case "Zona prohibida":
                id_tipo_incidencia = 4;
                break;
            case "Trafico excesivo por sobre flujo":
                id_tipo_incidencia = 5;
                break;
            case "Trafico excesivo por accidente":
                id_tipo_incidencia = 6;
                break;
            case "Accidente en la zona":
                id_tipo_incidencia = 7;
                break;
            case "Cansancio por manejar":
                id_tipo_incidencia = 8;
                break;
            case "Malestar de salud":
                id_tipo_incidencia = 9;
                break;
            case "Sin combustible":
                id_tipo_incidencia = 10;
                break;
            case "Robo":
                id_tipo_incidencia = 11;
                break;
            case "Falla mecánica":
                id_tipo_incidencia = 12;
                break;
            case "Ponchadura de llanta":
                id_tipo_incidencia = 13;
                break;
            case "Problema eléctrico":
                id_tipo_incidencia = 14;
                break;
            case "Exceso de peso":
                id_tipo_incidencia = 15;
                break;
            case "Clima adverso":
                id_tipo_incidencia = 16;
                break;
            case "Mal estado de las carreteras":
                id_tipo_incidencia = 17;
                break;
            case "Retraso en la carga":
                id_tipo_incidencia = 18;
                break;
            case "Retraso en la descarga":
                id_tipo_incidencia = 19;
                break;
            default:
                id_tipo_incidencia = 0;
                break;
        }
        return id_tipo_incidencia;
    }

    /**
     * Método para limpiar la lista de incidencias del mapa.
     *
     */
    public void recargarIncidencias() {
        mapMarkersIncidencias.clear();
        for (Incidencia incidencia : incidencias) {
            mapView.getMapScene().removeMapMarker(incidencia.mapMarker);
        }
        List<MapView.ViewPin> mapViewPins = mapView.getViewPins();
        for (MapView.ViewPin viewPin : new ArrayList<>(mapViewPins)) {
            if(viewPin!=null){
                for (Incidencia incidencia : incidencias) {
                    if(incidencia.mapMarker.getCoordinates().latitude==viewPin.getGeoCoordinates().latitude && incidencia.mapMarker.getCoordinates().longitude==viewPin.getGeoCoordinates().longitude){
                        try{
                            viewPin.unpin();
                        }catch (Exception e){
                            Log.e(TAG, "Error al limpiar la lista de incidencias del mapa: " + e.getMessage());
                        }
                    }
                }
            }
        }
        incidencias = dbHelper.getAllIncidencias();
        mostrarIncidencias();
    }

    /**
     * Método para mostrar las incidencias en el mapa.
     *
     */
    public void mostrarIncidencias() {
        for (Incidencia incidencia : incidencias) {
            mapMarkersIncidencias.add(incidencia.mapMarker);
            mapView.getMapScene().addMapMarker(incidencia.mapMarker);
            // Crea un TextView para la etiqueta
            TextView textView = new TextView(mainActivity.getApplicationContext());
            textView.setTextColor(Color.parseColor("#E55657"));
            textView.setText(getTipoIncidenciaById(incidencia.id_tipo_incidencia));
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            // Crea un LinearLayout para contener el TextView y agregar padding
            LinearLayout linearLayout = new LinearLayout(mainActivity.getApplicationContext());
            //linearLayout.setBackgroundResource(R.color.colorAccent);
            linearLayout.setPadding(0, 0, 0, 130);
            linearLayout.addView(textView);
            // Ancla el LinearLayout al mapa en las coordenadas ajustadas
            mapView.pinView(linearLayout, incidencia.mapMarker.getCoordinates());
        }
    }

    /**
     * Método para obtener el tipo de incidencia por su ID.
     *
     * @param id_tipo_incidencia .
     */
    public String getTipoIncidenciaById(int id_tipo_incidencia) {
        String tipo_incidencia = "";
        switch (id_tipo_incidencia) {
            case 1:
                tipo_incidencia = "Infracción de transito";
                break;
            case 2:
                tipo_incidencia = "Cierre vial por obras en la zona";
                break;
            case 3:
                tipo_incidencia = "Cierre vial por evento en la zona";
                break;
            case 4:
                tipo_incidencia = "Zona prohibida";
                break;
            case 5:
                tipo_incidencia = "Trafico excesivo por sobre flujo";
                break;
            case 6:
                tipo_incidencia = "Trafico excesivo por accidente";
                break;
            case 7:
                tipo_incidencia = "Accidente en la zona";
                break;
            case 8:
                tipo_incidencia = "Cansancio por manejar";
                break;
            case 9:
                tipo_incidencia = "Malestar de salud";
                break;
            case 10:
                tipo_incidencia = "Sin combustible";
                break;
            case 11:
                tipo_incidencia = "Robo";
                break;
            case 12:
                tipo_incidencia = "Falla mecánica";
                break;
            case 13:
                tipo_incidencia = "Ponchadura de llanta";
                break;
            case 14:
                tipo_incidencia = "Problema eléctrico";
                break;
            case 15:
                tipo_incidencia = "Exceso de peso";
                break;
            case 16:
                tipo_incidencia = "Clima adverso";
                break;
            case 17:
                tipo_incidencia = "Mal estado de las carreteras";
                break;
            case 18:
                tipo_incidencia = "Retraso en la carga";
                break;
            case 19:
                tipo_incidencia = "Retraso en la descarga";
                break;
            default:
                tipo_incidencia = "NA";
                break;
        }
        return tipo_incidencia;
    }
}
