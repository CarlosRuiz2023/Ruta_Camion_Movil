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

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.GeoPolygon;
import com.here.sdk.core.Point2D;
import com.here.sdk.core.errors.InstantiationErrorException;
import com.here.sdk.gestures.TapListener;
import com.here.sdk.mapview.MapImage;
import com.here.sdk.mapview.MapImageFactory;
import com.here.sdk.mapview.MapMarker;
import com.here.sdk.mapview.MapMeasure;
import com.here.sdk.mapview.MapPolygon;
import com.here.sdk.mapview.MapView;
import com.here.sdk.mapview.MapViewBase;
import com.here.sdk.mapview.PickMapItemsResult;
import com.itsmarts.smartroutetruckapp.R;
import com.itsmarts.smartroutetruckapp.clases.AvoidZonesExample;
import com.itsmarts.smartroutetruckapp.modelos.PolygonWithId;

import java.util.ArrayList;
import java.util.List;

public class PolygonAdapter extends RecyclerView.Adapter<PolygonAdapter.PolygonViewHolder> {

    private static int position=0;
    public static AvoidZonesExample avoidZonesExample;
    private static Drawable visible, no_visible, label, no_label;

    // Constructor para el adaptador con ModalBottomSheetFullScreenFragment
    public PolygonAdapter(AvoidZonesExample avoidZonesExample) {
        this.avoidZonesExample = avoidZonesExample;
        this.visible = avoidZonesExample.context.getResources().getDrawable(R.drawable.ic_visible);
        this.no_visible = avoidZonesExample.context.getResources().getDrawable(R.drawable.ic_no_visible);
        this.label = avoidZonesExample.context.getResources().getDrawable(R.drawable.ic_label);
        this.no_label = avoidZonesExample.context.getResources().getDrawable(R.drawable.ic_no_label);
    }

    @Override
    public PolygonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflar la vista del elemento de la lista
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.polygon_item, parent, false);
        return new PolygonViewHolder(view,this);
    }

    @Override
    public void onBindViewHolder(PolygonViewHolder holder, int position) {
        // Asigna el nombre del polígono basado en su posición
        holder.polygonNameTextView.setText(avoidZonesExample.polygonWithIds.get(position).name);
        if(avoidZonesExample.polygonWithIds.get(position).status){
            holder.icon_visibility.setImageDrawable(no_visible);
        }else{
            holder.icon_visibility.setImageDrawable(visible);
        }
        if(avoidZonesExample.polygonWithIds.get(position).label){
            holder.icon_label.setImageDrawable(no_label);
        }else{
            holder.icon_label.setImageDrawable(label);
        }
        if(avoidZonesExample.polygonWithIds.get(position).peligrosa){
            holder.polygon_item.setBackgroundColor(Color.parseColor("#808080"));
        }else{
            holder.polygon_item.setBackgroundColor(Color.parseColor("#FF8080"));
        }
    }

    @Override
    public int getItemCount() {
        return avoidZonesExample.polygonWithIds.size(); // Devuelve el número total de polígonos
    }

    // Clase interna para el ViewHolder
    public static class PolygonViewHolder extends RecyclerView.ViewHolder {
        public TextView polygonNameTextView;
        public LinearLayout polygon_item;
        public ImageView icon_edit, icon_delete, icon_visibility, icon_label;

        public PolygonViewHolder(View itemView, PolygonAdapter adapter) {
            super(itemView);
            // Enlaza el TextView con el layout
            polygon_item = itemView.findViewById(R.id.polygon_item);
            polygonNameTextView = itemView.findViewById(R.id.polygon_name);
            icon_edit = itemView.findViewById(R.id.icon_edit);
            icon_delete = itemView.findViewById(R.id.icon_delete);
            icon_visibility = itemView.findViewById(R.id.icon_visibility);
            icon_label = itemView.findViewById(R.id.icon_label);

            // Configura el OnClickListener para el ImageView de editar
            icon_edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Eliminar cualquier polígono existente en el mapa
                    avoidZonesExample.cleanPolygon();
                    position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        avoidZonesExample.mapPolygon = avoidZonesExample.polygonWithIds.get(position).polygon;
                        List<MapView.ViewPin> mapViewPins = avoidZonesExample.mapView.getViewPins();
                        for (MapView.ViewPin viewPin : new ArrayList<>(mapViewPins)) {
                            GeoCoordinates geoCoordinates = avoidZonesExample.calculateCentroid(avoidZonesExample.mapPolygon.getGeometry().vertices);
                            if(geoCoordinates.latitude==viewPin.getGeoCoordinates().latitude && geoCoordinates.longitude==viewPin.getGeoCoordinates().longitude){
                                viewPin.unpin();
                            }
                        }
                        avoidZonesExample.selected_geometry = avoidZonesExample.mapPolygon.getGeometry();
                        avoidZonesExample.polygonVertices = avoidZonesExample.mapPolygon.getGeometry().vertices;
                        for (GeoCoordinates vertex : avoidZonesExample.polygonVertices) {
                            addMapMarker(vertex, R.drawable.red_dot);
                        }
                        // Calcula el centro del polígono
                        GeoCoordinates center = calculatePolygonCenter(avoidZonesExample.mapPolygon);

                        // Calcula la distancia diagonal del polígono
                        double diagonalDistance = calculatePolygonDiagonalDistance(avoidZonesExample.mapPolygon);

                        // Calcula el nivel de zoom adecuado (ajusta este valorsegún tus necesidades)
                        double zoomLevel = 15 - Math.log(diagonalDistance / 1000) / Math.log(2);

                        MapMeasure mapMeasure = new MapMeasure(MapMeasure.Kind.ZOOM_LEVEL, zoomLevel);
                        // Centra el mapa en el polígono y ajusta el nivel de zoom
                        avoidZonesExample.mapView.getCamera().lookAt(center, mapMeasure);
                        avoidZonesExample.llSave.setVisibility(View.GONE);
                        for (PolygonWithId polygon : avoidZonesExample.polygonWithIds) {
                            avoidZonesExample.mapView.getMapScene().removeMapPolygon(polygon.polygon);
                        }
                        avoidZonesExample.mapView.getMapScene().addMapPolygon(avoidZonesExample.mapPolygon);
                        avoidZonesExample.llReset.setVisibility(View.VISIBLE);
                        avoidZonesExample.fbReset.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                avoidZonesExample.cleanPolygon();
                                avoidZonesExample.mapView.getMapScene().removeMapPolygon(avoidZonesExample.mapPolygon);
                                avoidZonesExample.startGestures(avoidZonesExample.polygonWithIds.get(position));
                                avoidZonesExample.llReset.setVisibility(View.GONE);
                            }
                        });
                        avoidZonesExample.bottomSheetFragment.mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        avoidZonesExample.mapView.getGestures().setTapListener(touchPoint -> pickMapMarker(touchPoint));
                    }
                }
            });
            // Configura el OnClickListener para el ImageView de eliminar
            icon_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        PolygonWithId polygon = avoidZonesExample.polygonWithIds.get(position);
                        avoidZonesExample.mapView.getMapScene().removeMapPolygon(polygon.polygon); // Elimina el polígono del mapa
                        avoidZonesExample.polygonWithIds.remove(position);
                        adapter.notifyItemRemoved(position); // Llama al método desde el adaptador
                        adapter.notifyItemRangeChanged(position, avoidZonesExample.polygonWithIds.size());
                        avoidZonesExample.dbHelper.deleteZona(polygon.id);
                        if(avoidZonesExample.polygonVertices.size()>0){
                            for (MapMarker marker : avoidZonesExample.markers) {
                                avoidZonesExample.mapView.getMapScene().removeMapMarker(marker);
                            }
                            avoidZonesExample.markers.clear();
                            avoidZonesExample.polygonVertices.clear();
                        }
                        avoidZonesExample.cleanPolygon();
                        List<MapView.ViewPin> mapViewPins = avoidZonesExample.mapView.getViewPins();
                        for (MapView.ViewPin viewPin : new ArrayList<>(mapViewPins)) {
                            GeoCoordinates geoCoordinates = avoidZonesExample.calculateCentroid(polygon.polygon.getGeometry().vertices);
                            if(geoCoordinates.latitude==viewPin.getGeoCoordinates().latitude && geoCoordinates.longitude==viewPin.getGeoCoordinates().longitude){
                                viewPin.unpin();
                            }
                        }
                    }
                }
            });
            //Actualizar status al dar click en el Checkbox
            icon_visibility.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        PolygonWithId polygon = avoidZonesExample.polygonWithIds.get(position);
                        avoidZonesExample.dbHelper.updateStatusZona(polygon.id, !polygon.status);
                        if(!polygon.status){
                            avoidZonesExample.mapView.getMapScene().addMapPolygon(polygon.polygon);
                            polygon.status = true;
                            icon_visibility.setImageDrawable(no_visible);
                        }else{
                            avoidZonesExample.mapView.getMapScene().removeMapPolygon(polygon.polygon);
                            polygon.status = false;
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
                        PolygonWithId polygon = avoidZonesExample.polygonWithIds.get(position);
                        avoidZonesExample.dbHelper.updateLabelZona(polygon.id, !polygon.label);
                        if(!polygon.label){
                            // Crea un TextView para la etiqueta
                            TextView textView = new TextView(avoidZonesExample.context);
                            if(polygon.peligrosa){
                                textView.setTextColor(Color.parseColor("#000000"));
                            }else{
                                textView.setTextColor(Color.parseColor("#FF0000"));
                            }
                            textView.setText(polygon.name);
                            textView.setTypeface(Typeface.DEFAULT_BOLD);

                            // Crea un LinearLayout para contener el TextView y agregar padding
                            LinearLayout linearLayout = new LinearLayout(avoidZonesExample.context);
                            //linearLayout.setBackgroundResource(R.color.colorAccent);
                            //linearLayout.setPadding(0, 0, 0, 130);
                            linearLayout.addView(textView);
                            // Crear un nuevo GeoCoordinates para el punto medio
                            GeoCoordinates midpoint = avoidZonesExample.calculateCentroid(polygon.polygon.getGeometry().vertices);

                            // Usar el punto medio para anclar la vista
                            avoidZonesExample.mapView.pinView(linearLayout, midpoint);
                            polygon.label = true;
                            icon_label.setImageDrawable(no_label);
                        }else{
                            polygon.label = false;
                            icon_label.setImageDrawable(label);
                            avoidZonesExample.cleanPolygon();
                        }
                    }
                }
            });
        }
    }
    private static void addMapMarker(GeoCoordinates geoCoordinates, int resourceId) {
        MapImage mapImage = MapImageFactory.fromResource(avoidZonesExample.context.getResources(), resourceId);
        MapMarker mapMarker = new MapMarker(geoCoordinates, mapImage);
        avoidZonesExample.mapView.getMapScene().addMapMarker(mapMarker);
        avoidZonesExample.markers.add(mapMarker);
    }

    private static void updatePolygonGeometry() {
        // Obtiene las nuevas coordenadas de los marcadores
        List<GeoCoordinates> newVertices = new ArrayList<>();
        for (MapMarker marker : avoidZonesExample.markers) {
            newVertices.add(marker.getCoordinates());
        }

        // Crea una nueva geometría de polígono con las nuevas coordenadas
        GeoPolygon newGeometry = null;
        try {
            newGeometry = new GeoPolygon(newVertices);
        } catch (InstantiationErrorException e) {
            //throw new RuntimeException(e);
        }

        // Actualiza la geometría del polígono existente
        if (avoidZonesExample.mapPolygon != null) {
            avoidZonesExample.mapPolygon.setGeometry(newGeometry);
            avoidZonesExample.polygonVertices=newVertices;
        }
        avoidZonesExample.llSave.setVisibility(View.VISIBLE);
        avoidZonesExample.fbSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                avoidZonesExample.showSavePolygonDialog(avoidZonesExample.polygonWithIds.get(position));
            }
        });
    }

    private static void pickMapMarker(final Point2D touchPoint) {
        // Establece el radio en metros
        float radiusInPixel = 2;

        // Obtener las coordenadas del punto del toque
        avoidZonesExample.mapView.pickMapItems(touchPoint, radiusInPixel, new MapViewBase.PickMapItemsCallback() {
            @Override
            public void onPickMapItems(@Nullable PickMapItemsResult pickMapItemsResult) {
                try{
                    // Verificar si se ha seleccionado un MapMarker
                    if (pickMapItemsResult == null) {
                        return;
                    }

                    // Obtener el MapMarker seleccionado
                    MapMarker topmostMapMarker = pickMapItemsResult.getMarkers().get(0);

                    // Verificar si el MapMarker es nulo
                    if (topmostMapMarker == null) {
                        return;
                    }
                    changeMapMarker(topmostMapMarker);

                } catch (Exception e) {
                    // Manejo de excepciones
                }
            }
        });
    }
    private static void changeMapMarker(MapMarker topmostMapMarker) {

        boolean validacion = false;

        for (MapMarker marker : avoidZonesExample.markers) {
            if (marker == topmostMapMarker) {
                validacion = true;
            }
        }
        if (validacion) {
            avoidZonesExample.selected_marker = topmostMapMarker;
            MapImage mapImage = MapImageFactory.fromResource(avoidZonesExample.context.getResources(), R.drawable.green_dot);
            avoidZonesExample.selected_marker.setImage(mapImage);

            avoidZonesExample.mapView.getGestures().setTapListener(new TapListener() {
                @Override
                public void onTap(@NonNull Point2D point2D) {
                    if (avoidZonesExample.selected_marker != null) { // Solo si hay un marcador seleccionado
                        // Segundo clic: Mover el marcador a la nueva posición
                        GeoCoordinates newCoordinates = avoidZonesExample.mapView.viewToGeoCoordinates(point2D);
                        avoidZonesExample.selected_marker.setCoordinates(newCoordinates);
                        updatePolygonGeometry(); // Actualiza la geometría del polígono si es necesario
                        MapImage mapImage = MapImageFactory.fromResource(avoidZonesExample.context.getResources(), R.drawable.red_dot);
                        avoidZonesExample.selected_marker.setImage(mapImage);
                        avoidZonesExample.selected_marker = null; // Deselecciona el marcador
                        avoidZonesExample.mapView.getGestures().setTapListener(touchPoint -> pickMapMarker(touchPoint));
                    }
                }
            });
        }
    }

    // Funciones auxiliares
    private static GeoCoordinates calculatePolygonCenter(MapPolygon polygon) {
        double latSum = 0;
        double lonSum = 0;
        for (GeoCoordinates vertex : polygon.getGeometry().vertices) {
            latSum += vertex.latitude;
            lonSum += vertex.longitude;
        }
        return new GeoCoordinates(latSum / polygon.getGeometry().vertices.size(), lonSum / polygon.getGeometry().vertices.size());
    }

    private static double calculatePolygonDiagonalDistance(MapPolygon polygon) {
        double maxDistance = 0;List<GeoCoordinates> vertices = polygon.getGeometry().vertices;
        for (int i = 0; i < vertices.size(); i++) {
            for (int j = i + 1; j < vertices.size(); j++) {
                double distance = vertices.get(i).distanceTo(vertices.get(j));
                if (distance > maxDistance) {
                    maxDistance = distance;
                }
            }
        }
        return maxDistance;
    }
}

