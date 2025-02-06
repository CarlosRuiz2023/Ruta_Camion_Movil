package com.itsmarts.SmartRouteTruckApp.adaptadores;

import android.app.Dialog;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
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
import com.itsmarts.SmartRouteTruckApp.activitys.InicioSesionActivity;
import com.itsmarts.SmartRouteTruckApp.api.ApiService;
import com.itsmarts.SmartRouteTruckApp.api.RetrofitClient;
import com.itsmarts.SmartRouteTruckApp.clases.IncidenciasExample;
import com.itsmarts.SmartRouteTruckApp.fragments.ErrorDialogFragment;
import com.itsmarts.SmartRouteTruckApp.helpers.Internet;
import com.itsmarts.SmartRouteTruckApp.modelos.Incidencia;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IncidenciaAdapter extends RecyclerView.Adapter<IncidenciaAdapter.IncidenciaViewHolder> {
    private static int position=0;
    public static IncidenciasExample incidenciasExample;

    // Constructor para el adaptador
    public IncidenciaAdapter(IncidenciasExample incidenciasExample) {
        this.incidenciasExample = incidenciasExample;
    }

    @NonNull
    @Override
    public IncidenciaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.incidencia_item, parent, false);
        return new IncidenciaViewHolder(view,this);
    }

    @Override
    public void onBindViewHolder(IncidenciaViewHolder holder, int position) {
        // Asigna el nombre del polígono basado en su posición
        holder.tipoIncidenciaTextView.setText(incidenciasExample.getTipoIncidenciaById(incidenciasExample.incidencias.get(position).id_tipo_incidencia));
        if(incidenciasExample.incidencias.get(position).status){
            holder.incidencia_item.setBackgroundColor(Color.parseColor("#1A8B51"));
            holder.icon_send.setVisibility(View.GONE);
        }else{
            holder.incidencia_item.setBackgroundColor(Color.parseColor("#B00020"));
            holder.icon_send.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return incidenciasExample.incidencias.size(); // Devuelve el número total de polígonos
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

            icon_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    icon_delete.startAnimation(incidenciasExample.mainActivity.animacionClick);
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Incidencia incidencia = incidenciasExample.incidencias.get(position);
                        incidenciasExample.dbHelper.deleteIncidencia(incidencia.id);
                        adapter.notifyItemRemoved(position); // Llama al método desde el adaptador
                        adapter.notifyItemRangeChanged(position, incidenciasExample.incidencias.size());
                        incidenciasExample.recargarIncidencias();
                    }
                }
            });

            incidencia_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    incidencia_item.startAnimation(incidenciasExample.mainActivity.animacionClick);
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Incidencia incidencia = incidenciasExample.incidencias.get(position);
                        flyTo(incidenciasExample.mainActivity.mapView, incidencia.mapMarker.getCoordinates());
                        incidenciasExample.mainActivity.messages.showDialogIncidencia(incidenciasExample.getTipoIncidenciaById(incidencia.id_tipo_incidencia), incidencia.direccion, incidencia.comentarios, incidencia.fecha_hora, incidencia.foto);
                    }
                }
            });

            icon_send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    icon_send.startAnimation(incidenciasExample.mainActivity.animacionClick);
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Incidencia incidencia = incidenciasExample.incidencias.get(position);
                        if(Internet.isNetworkConnected()){
                            if(incidencia.foto!=null){
                                try{
                                    // Crear el MultipartBody.Part para la imagen
                                    RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), incidencia.foto);
                                    MultipartBody.Part imagePart = MultipartBody.Part.createFormData("archivo", "imagen.jpg", requestBody);
                                    ApiService apiService = RetrofitClient.getInstance(null, incidenciasExample.mainActivity.desarrollo).create(ApiService.class);
                                    // Llamar al servicio
                                    Call<ResponseBody> call = apiService.cargarImagen(imagePart);
                                    call.enqueue(new Callback<ResponseBody>() {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                            if (response.isSuccessful()) {
                                                try {
                                                    // Obtener el JSON como string
                                                    String jsonResponse = response.body().string();
                                                    // Convierte la respuesta en un objeto JSON
                                                    JSONObject jsonFoto = new JSONObject(jsonResponse);
                                                    // Verifica si la operación fue exitosa
                                                    boolean success = jsonFoto.getBoolean("success");
                                                    if (success) {
                                                        String foto = jsonFoto.optString("result", "");
                                                        Log.d("Retrofit", "Foto enviada exitosamente.");
                                                        Date date = new Date(incidencia.fecha_hora.toString());
                                                        date.setHours(date.getHours() - 6);
                                                        JSONObject jsonIncident = new JSONObject();
                                                        jsonIncident.put("id_tipo_incidencia", incidencia.id_tipo_incidencia);
                                                        jsonIncident.put("id_usuario", incidencia.id_usuario);
                                                        jsonIncident.put("id_ruta",incidencia.id_ruta);
                                                        jsonIncident.put("foto", foto);
                                                        jsonIncident.put("comentarios",incidencia.comentarios);
                                                        jsonIncident.put("latitud",incidencia.mapMarker.getCoordinates().latitude);
                                                        jsonIncident.put("longitud",incidencia.mapMarker.getCoordinates().longitude);
                                                        jsonIncident.put("fecha_hora",date.toString()); // Restar 6 horas
                                                        jsonIncident.put("direccion",incidencia.direccion);
                                                        //ErrorReporter.sendError(jsonObject);
                                                        ApiService apiService = RetrofitClient.getInstance(null, incidenciasExample.mainActivity.desarrollo).create(ApiService.class);
                                                        // Convertir JSONObject a String y crear un RequestBody
                                                        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonIncident.toString());
                                                        Call<Void> call1 = apiService.agregarIncidencia(requestBody);

                                                        call1.enqueue(new Callback<Void>() {
                                                            @Override
                                                            public void onResponse(Call<Void> call1, Response<Void> response) {
                                                                if (response.isSuccessful()) {
                                                                    //dbHelper.saveIncidencia(id_tipo_incidencia,id_usuario,ruta.id,imageFile,comentarios,currentGeoCoordinates,1);
                                                                    //TODO: Actualizar el estatus y recargar el adapter
                                                                    incidenciasExample.mainActivity.messages.showCustomToast("Incidencia enviada con exitosamente");
                                                                    incidenciasExample.dbHelper.updateStatusIncidencia(incidencia.id,true);
                                                                    incidencia.setStatus(true);
                                                                    adapter.notifyDataSetChanged();
                                                                } else if (response.code() == 409) {
                                                                    Dialog limiteIncidenciasDialog = new Dialog(incidenciasExample.mainActivity);
                                                                    limiteIncidenciasDialog.setContentView(R.layout.ventana_limite_de_incidencias);
                                                                    limiteIncidenciasDialog.setCancelable(false);
                                                                    limiteIncidenciasDialog.setCanceledOnTouchOutside(false);
                                                                    Button btnCancelar = limiteIncidenciasDialog.findViewById(R.id.btnCancelar);

                                                                    btnCancelar.setOnClickListener(new View.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(View v) {
                                                                            limiteIncidenciasDialog.dismiss();
                                                                        }
                                                                    });
                                                                    limiteIncidenciasDialog.show();
                                                                } else {
                                                                    Log.e("ErrorReporter", "Error al enviar la incidencia: " + response.code());
                                                                    incidenciasExample.mainActivity.messages.showCustomToast("Error al enviar la incidencia");
                                                                }
                                                            }

                                                            @Override
                                                            public void onFailure(Call<Void> call1, Throwable t) {
                                                                Log.e("ErrorReporter", "Error al enviar el reporte: " + t.getMessage());
                                                            }
                                                        });
                                                    } else {
                                                        // Mostrar un mensaje de error al usuario
                                                        incidenciasExample.mainActivity.messages.showCustomToast("Error al enviar la imagen");
                                                        //uploadImageSinConexion();
                                                    }
                                                } catch (IOException e) {
                                                    //logger.logError(TAG,e, MainActivity.this);
                                                } catch (JSONException e) {
                                                    //logger.logError(TAG,e,MainActivity.this);
                                                }
                                            } else {
                                                Log.e("Retrofit", "Error al enviar la imagen: " + response.code());
                                                // Mostrar un mensaje de error al usuario
                                                incidenciasExample.mainActivity.messages.showCustomToast("Error al enviar la imagen");
                                                //uploadImageSinConexion();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                                            Log.e("Retrofit", "Error al enviar la imagen", t);
                                            // Mostrar un mensaje de error al usuario
                                            incidenciasExample.mainActivity.messages.showCustomToast("Error al enviar la imagen");
                                            //uploadImageSinConexion();
                                        }
                                    });
                                }catch (Exception e){
                                    //
                                }
                            }else{
                                try {
                                    JSONObject jsonIncident = new JSONObject();
                                    Date date = new Date(incidencia.fecha_hora.toString());
                                    date.setHours(date.getHours() - 6);
                                    jsonIncident.put("id_tipo_incidencia", incidencia.id_tipo_incidencia);
                                    jsonIncident.put("id_usuario", incidencia.id_usuario);
                                    jsonIncident.put("id_ruta",incidencia.id_ruta);
                                    jsonIncident.put("comentarios",incidencia.comentarios);
                                    jsonIncident.put("latitud",incidencia.mapMarker.getCoordinates().latitude);
                                    jsonIncident.put("longitud",incidencia.mapMarker.getCoordinates().longitude);
                                    jsonIncident.put("fecha_hora",date.toString()); // Restar 6 horas
                                    jsonIncident.put("direccion",incidencia.direccion);
                                    //ErrorReporter.sendError(jsonObject);
                                    ApiService apiService = RetrofitClient.getInstance(null, incidenciasExample.mainActivity.desarrollo).create(ApiService.class);
                                    // Convertir JSONObject a String y crear un RequestBody
                                    RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonIncident.toString());
                                    Call<Void> call = apiService.agregarIncidencia(requestBody);

                                    call.enqueue(new Callback<Void>() {
                                        @Override
                                        public void onResponse(Call<Void> call, Response<Void> response) {
                                            if (response.isSuccessful()) {
                                                //dbHelper.saveIncidencia(id_tipo_incidencia,id_usuario,ruta.id,null,comentarios,currentGeoCoordinates,1);
                                                //TODO: Actualizar el estatus y recargar el adapter
                                                incidenciasExample.mainActivity.messages.showCustomToast("Incidencia enviada sin foto exitosamente");
                                                incidenciasExample.dbHelper.updateStatusIncidencia(incidencia.id,true);
                                                incidencia.setStatus(true);
                                                adapter.notifyDataSetChanged();
                                            } else if (response.code() == 409) {
                                                Dialog limiteIncidenciasDialog = new Dialog(incidenciasExample.mainActivity);
                                                limiteIncidenciasDialog.setContentView(R.layout.ventana_limite_de_incidencias);
                                                limiteIncidenciasDialog.setCancelable(false);
                                                limiteIncidenciasDialog.setCanceledOnTouchOutside(false);
                                                Button btnCancelar = limiteIncidenciasDialog.findViewById(R.id.btnCancelar);

                                                btnCancelar.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        limiteIncidenciasDialog.dismiss();
                                                    }
                                                });
                                                limiteIncidenciasDialog.show();
                                            } else {
                                                Log.e("ErrorReporter", "Error al enviar la incidencia: " + response.code());
                                                incidenciasExample.mainActivity.messages.showCustomToast("Error al enviar la incidencia");
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<Void> call, Throwable t) {
                                            Log.e("ErrorReporter", "Error al enviar el reporte: " + t.getMessage());
                                            //dbHelper.saveIncidencia(id_tipo_incidencia_final,id_usuario,ruta.id,null,comentarios,currentGeoCoordinates,0);
                                            incidenciasExample.mainActivity.messages.showCustomToast("Incidencia sin foto guardada dentro de la BD");
                                        }
                                    });
                                }catch (JSONException e){
                                    //
                                }
                            }
                        }else{
                            DialogFragment errorDialog = new ErrorDialogFragment();
                            errorDialog.show(incidenciasExample.mainActivity.getSupportFragmentManager(), "errorDialog");
                        }
                    }
                }
            });
        }
    }

    private static void flyTo(MapView mapView, GeoCoordinates geoCoordinates) {
        GeoCoordinatesUpdate geoCoordinatesUpdate = new GeoCoordinatesUpdate(geoCoordinates);
        double bowFactor = 1;
        mapView.getCamera().setDistanceToTarget(18.0);
        MapCameraAnimation animation = MapCameraAnimationFactory.flyTo(geoCoordinatesUpdate, bowFactor, Duration.ofSeconds(3));
        mapView.getCamera().startAnimation(animation);
    }
}

