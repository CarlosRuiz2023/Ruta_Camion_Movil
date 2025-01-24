package com.itsmarts.smartroutetruckapp.modelos;

import android.content.Context;
import android.content.res.Resources;
import android.os.LocaleList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.itsmarts.smartroutetruckapp.R;

import java.util.List;

public class ChooserImageAdapter extends RecyclerView.Adapter<ChooserImageAdapter.ChooserImageViewHolder> {
    private Context context;
    private List<ChooserItem> items;
    private OnItemClick listener;

    public ChooserImageAdapter(Context context, List<ChooserItem> items) {
        this.context = context;
        this.items = items;
    }

    public interface OnItemClick
    {
        void onItemClick(ChooserItem item, int position);
    }

    public void setOnItemClick(OnItemClick listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChooserImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chooser_image, parent, false);
        return new ChooserImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChooserImageViewHolder holder, int position) {
        holder.bindComponents(items.get(position), listener, position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ChooserImageViewHolder extends RecyclerView.ViewHolder {
        ImageView ImgIconChooser;
        TextView TxtOpcion;

        public ChooserImageViewHolder(@NonNull View itemView) {
            super(itemView);
            TxtOpcion = itemView.findViewById(R.id.TxtOpcion);
            ImgIconChooser = itemView.findViewById(R.id.ImgIconChooser);
        }

        public void bindComponents(ChooserItem item, OnItemClick listener, int position) {
            TxtOpcion.setText(item.getOpcion());
            ImgIconChooser.setImageDrawable(ContextCompat.getDrawable(context, getIdResourceByString(context, item.getIcon(), "drawable" )));
            itemView.setOnClickListener(v -> listener.onItemClick(item, position));
        }

        //OBTIENE EL ID DEL RECURSO DE ACUERDO A UN NOMBRE
        public int getIdResourceByString(Context context , String name, String typeRoute)
        {
            Resources res = context.getResources();
            String mDrawableName = name;
            int resID = res.getIdentifier(mDrawableName , typeRoute, context.getPackageName());
            return resID;
        }

        //OBTIENE EL ID DEL RECURSO DE IMAGENES PARA OPERADORES
        public int getIdResourceByStringImg(Context context,String name,String typeRoute)
        {
            Resources res = context.getResources();
            int resourceId;

            String operador = name.trim().toLowerCase(LocaleList.getDefault().get(0));
            String cadena = operador.replace("&", "");
            cadena = cadena.replace(" ", "");

            String mDrawableName = "ic_"+cadena;
            int resID = res.getIdentifier(mDrawableName, typeRoute, context.getPackageName());

            if (resID > 0) {
                resourceId=resID;
            } else {
                resourceId=res.getIdentifier("ic_otro",typeRoute,context.getPackageName());
            }

            return resourceId;
        }
    }
}