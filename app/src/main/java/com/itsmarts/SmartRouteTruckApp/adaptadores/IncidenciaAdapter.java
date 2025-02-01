package com.itsmarts.SmartRouteTruckApp.adaptadores;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.GeoCoordinatesUpdate;
import com.here.sdk.mapview.MapCameraAnimation;
import com.here.sdk.mapview.MapCameraAnimationFactory;
import com.here.sdk.mapview.MapImage;
import com.here.sdk.mapview.MapImageFactory;
import com.here.sdk.mapview.MapMarker;
import com.here.sdk.mapview.MapView;
import com.here.time.Duration;
import com.itsmarts.SmartRouteTruckApp.R;
import com.itsmarts.SmartRouteTruckApp.clases.ControlIncidenciasExample;
import com.itsmarts.SmartRouteTruckApp.clases.ControlPointsExample;
import com.itsmarts.SmartRouteTruckApp.modelos.PointWithId;

public class IncidenciaAdapter extends RecyclerView.Adapter<IncidenciaAdapter.IncidenciaViewHolder> {
    private static int position=0;
    public static ControlIncidenciasExample controlIncidenciasExample;

    // Constructor para el adaptador
    public IncidenciaAdapter(ControlIncidenciasExample controlIncidenciasExample) {
        this.controlIncidenciasExample = controlIncidenciasExample;
    }

    @NonNull
    @Override
    public IncidenciaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.incidencia_item, parent, false);
        return new IncidenciaViewHolder(view,this);
    }

    @Override
    public void onBindViewHolder(IncidenciaViewHolder holder, int position) {
        String tipo_incidencia = "";
        switch (controlIncidenciasExample.incidencias.get(position).id_tipo_incidencia){
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
        // Asigna el nombre del polígono basado en su posición
        holder.tipoIncidenciaTextView.setText(tipo_incidencia);
        if(controlIncidenciasExample.incidencias.get(position).status){
            holder.incidencia_item.setBackgroundColor(Color.parseColor("#1A8B51"));
        }else{
            holder.incidencia_item.setBackgroundColor(Color.parseColor("#B00020"));
            holder.icon_send.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return controlIncidenciasExample.incidencias.size(); // Devuelve el número total de polígonos
    }

    // Clase interna para el ViewHolder
    public static class IncidenciaViewHolder extends RecyclerView.ViewHolder {
        public TextView tipoIncidenciaTextView;
        public LinearLayout incidencia_item;
        public ImageView icon_send, icon_delete;

        public IncidenciaViewHolder(View itemView, IncidenciaAdapter adapter) {
            super(itemView);
            // Enlaza el TextView con el layout
            tipoIncidenciaTextView = itemView.findViewById(R.id.tipo_incidencia);
            incidencia_item = itemView.findViewById(R.id.incidencia_item);
            icon_send = itemView.findViewById(R.id.icon_send);
            icon_delete = itemView.findViewById(R.id.icon_delete);
        }
    }
    private static void addMapMarker(GeoCoordinates geoCoordinates, int resourceId) {
        MapImage mapImage = MapImageFactory.fromResource(controlIncidenciasExample.context.getResources(), resourceId);
        controlIncidenciasExample.mapMarker = new MapMarker(geoCoordinates, mapImage);
        controlIncidenciasExample.mapView.getMapScene().addMapMarker(controlIncidenciasExample.mapMarker);
    }

    private static void flyTo(MapView mapView, GeoCoordinates geoCoordinates) {
        GeoCoordinatesUpdate geoCoordinatesUpdate = new GeoCoordinatesUpdate(geoCoordinates);
        double bowFactor = 1;
        MapCameraAnimation animation = MapCameraAnimationFactory.flyTo(geoCoordinatesUpdate, bowFactor, Duration.ofSeconds(3));
        mapView.getCamera().startAnimation(animation);
    }
}

