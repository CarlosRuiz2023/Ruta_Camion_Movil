package com.itsmarts.smartroutetruckapp.adaptadores;

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
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.GeoCoordinatesUpdate;
import com.here.sdk.core.Point2D;
import com.here.sdk.gestures.TapListener;
import com.here.sdk.mapview.MapCameraAnimation;
import com.here.sdk.mapview.MapCameraAnimationFactory;
import com.here.sdk.mapview.MapImage;
import com.here.sdk.mapview.MapImageFactory;
import com.here.sdk.mapview.MapMarker;
import com.here.sdk.mapview.MapView;
import com.here.sdk.mapview.MapViewBase;
import com.here.sdk.mapview.PickMapItemsResult;
import com.here.time.Duration;
import com.itsmarts.smartroutetruckapp.R;
import com.itsmarts.smartroutetruckapp.clases.ControlPointsExample;
import com.itsmarts.smartroutetruckapp.modelos.PointWithId;

import java.util.ArrayList;
import java.util.List;

public class PointAdapter extends RecyclerView.Adapter<PointAdapter.PointViewHolder> {
    private static int position=0;
    public static ControlPointsExample controlPointsExample;
    private static Drawable visible, no_visible, label, no_label;

    // Constructor para el adaptador
    public PointAdapter(ControlPointsExample controlPointsExample) {
        this.controlPointsExample = controlPointsExample;
        this.visible = controlPointsExample.context.getResources().getDrawable(R.drawable.ic_visible);
        this.no_visible = controlPointsExample.context.getResources().getDrawable(R.drawable.ic_no_visible);
        this.label = controlPointsExample.context.getResources().getDrawable(R.drawable.ic_label);
        this.no_label = controlPointsExample.context.getResources().getDrawable(R.drawable.ic_no_label);
    }

    @Override
    public PointViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflar la vista del elemento de la lista
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.polygon_item, parent, false);
        return new PointViewHolder(view,this);
    }

    @Override
    public void onBindViewHolder(PointViewHolder holder, int position) {
        // Asigna el nombre del polígono basado en su posición
        holder.polygonNameTextView.setText(controlPointsExample.pointsWithIds.get(position).name);
        if(controlPointsExample.pointsWithIds.get(position).visibility){
            holder.icon_visibility.setImageDrawable(no_visible);
        }else{
            holder.icon_visibility.setImageDrawable(visible);
        }
        if(controlPointsExample.pointsWithIds.get(position).label){
            holder.icon_label.setImageDrawable(no_label);
        }else{
            holder.icon_label.setImageDrawable(label);
        }
    }

    @Override
    public int getItemCount() {
        return controlPointsExample.pointsWithIds.size(); // Devuelve el número total de polígonos
    }

    // Clase interna para el ViewHolder
    public static class PointViewHolder extends RecyclerView.ViewHolder {
        public TextView polygonNameTextView;
        public ImageView icon_edit, icon_delete, icon_visibility, icon_label;

        public PointViewHolder(View itemView, PointAdapter adapter) {
            super(itemView);
            // Enlaza el TextView con el layout
            polygonNameTextView = itemView.findViewById(R.id.polygon_name);
            icon_visibility = itemView.findViewById(R.id.icon_visibility);
            icon_label = itemView.findViewById(R.id.icon_label);
            //Actualizar status al dar click en el Checkbox
            icon_visibility.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        PointWithId point = controlPointsExample.pointsWithIds.get(position);
                        controlPointsExample.dbHelper.updateVisibilityPunto(point.id, !controlPointsExample.pointsWithIds.get(position).visibility);
                        if(!controlPointsExample.pointsWithIds.get(position).visibility()){
                            controlPointsExample.mapView.getMapScene().addMapMarker(point.mapMarker);
                            controlPointsExample.pointsWithIds.get(position).visibility = true;
                            icon_visibility.setImageDrawable(no_visible);
                        }else{
                            controlPointsExample.mapView.getMapScene().removeMapMarker(point.mapMarker);
                            controlPointsExample.pointsWithIds.get(position).visibility = false;
                            icon_visibility.setImageDrawable(visible);
                        }
                    }
                }
            });
            //Actualizar status al dar click en el Checkbox
            icon_label.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        PointWithId point = controlPointsExample.pointsWithIds.get(position);
                        controlPointsExample.dbHelper.updateLabelPunto(point.id, !controlPointsExample.pointsWithIds.get(position).label);
                        if(!controlPointsExample.pointsWithIds.get(position).label){
                            // Crea un TextView para la etiqueta
                            TextView textView = new TextView(controlPointsExample.context);
                            textView.setTextColor(Color.parseColor("#7EB8D5"));
                            textView.setText(point.name);
                            textView.setTypeface(Typeface.DEFAULT_BOLD);

                            // Crea un LinearLayout para contener el TextView y agregar padding
                            LinearLayout linearLayout = new LinearLayout(controlPointsExample.context);
                            //linearLayout.setBackgroundResource(R.color.colorAccent);
                            linearLayout.setPadding(0, 0, 0, 130);
                            linearLayout.addView(textView);

                            // Ancla el LinearLayout al mapa en las coordenadas ajustadas
                            controlPointsExample.mapView.pinView(linearLayout, point.mapMarker.getCoordinates());
                            controlPointsExample.pointsWithIds.get(position).label = true;
                            icon_label.setImageDrawable(no_label);
                        }else{
                            controlPointsExample.pointsWithIds.get(position).label = false;
                            icon_label.setImageDrawable(label);
                            controlPointsExample.cleanPoint();
                        }
                    }
                }
            });
        }
    }
    private static void addMapMarker(GeoCoordinates geoCoordinates, int resourceId) {
        MapImage mapImage = MapImageFactory.fromResource(controlPointsExample.context.getResources(), resourceId);
        controlPointsExample.mapMarker = new MapMarker(geoCoordinates, mapImage);
        controlPointsExample.mapView.getMapScene().addMapMarker(controlPointsExample.mapMarker);
    }

    private static void flyTo(MapView mapView, GeoCoordinates geoCoordinates) {
        GeoCoordinatesUpdate geoCoordinatesUpdate = new GeoCoordinatesUpdate(geoCoordinates);
        double bowFactor = 1;
        MapCameraAnimation animation = MapCameraAnimationFactory.flyTo(geoCoordinatesUpdate, bowFactor, Duration.ofSeconds(3));
        mapView.getCamera().startAnimation(animation);
    }
}

