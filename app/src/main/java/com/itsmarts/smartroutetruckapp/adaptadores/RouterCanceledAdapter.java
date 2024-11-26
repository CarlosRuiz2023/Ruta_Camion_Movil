package com.itsmarts.smartroutetruckapp.adaptadores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.itsmarts.smartroutetruckapp.MainActivity;
import com.itsmarts.smartroutetruckapp.R;
import com.itsmarts.smartroutetruckapp.modelos.RoutesWithId;

public class RouterCanceledAdapter extends RecyclerView.Adapter<RouterCanceledAdapter.RouteViewHolder> {
    private static int position=0;
    private static MainActivity mainActivity;
    private int id=0;
    private int cantidad=0;

    // Constructor para el adaptador
    public RouterCanceledAdapter(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public RouteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflar la vista del elemento de la lista
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.route_item, parent, false);
        return new RouteViewHolder(view,this);
    }

    @Override
    public void onBindViewHolder(RouteViewHolder holder, int position) {
        // Asigna el nombre del polígono basado en su posición
        if(mainActivity.rutas.get(position).status == 3 && id != mainActivity.rutas.get(position).id){
            holder.routeNameTextView.setText(mainActivity.rutas.get(position).name);
            if(cantidad != 1){
                id = mainActivity.rutas.get(position).id;
            }
        }else{
            position++;
            onBindViewHolder(holder,position);
        }
    }

    @Override
    public int getItemCount() {
        if (mainActivity.rutas.size() == 0){
            return 0;
        }else{
            int count =0;
            for (RoutesWithId ruta : mainActivity.rutas){
                if(ruta.status == 3){
                    count++;
                }
            }
            cantidad = count;
            return count;
        }
    }

    // Clase interna para el ViewHolder
    public static class RouteViewHolder extends RecyclerView.ViewHolder {
        public TextView routeNameTextView;

        public RouteViewHolder(View itemView, RouterCanceledAdapter adapter) {
            super(itemView);
            // Enlaza el TextView con el layout
            routeNameTextView = itemView.findViewById(R.id.routeNameTextView);
        }
    }

}

