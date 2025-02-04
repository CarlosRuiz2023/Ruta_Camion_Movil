package com.itsmarts.SmartRouteTruckApp.adaptadores;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.itsmarts.SmartRouteTruckApp.MainActivity;
import com.itsmarts.SmartRouteTruckApp.R;
import com.itsmarts.SmartRouteTruckApp.api.ApiService;
import com.itsmarts.SmartRouteTruckApp.api.RetrofitClient;
import com.itsmarts.SmartRouteTruckApp.clases.ControlIncidenciasExample;
import com.itsmarts.SmartRouteTruckApp.clases.ControlPointsExample;
import com.itsmarts.SmartRouteTruckApp.fragments.ErrorDialogFragment;
import com.itsmarts.SmartRouteTruckApp.helpers.Internet;
import com.itsmarts.SmartRouteTruckApp.modelos.Incidencia;
import com.itsmarts.SmartRouteTruckApp.modelos.PointWithId;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

            icon_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Incidencia incidencia = controlIncidenciasExample.incidencias.get(position);
                        controlIncidenciasExample.incidencias.remove(position);
                        adapter.notifyItemRemoved(position); // Llama al método desde el adaptador
                        adapter.notifyItemRangeChanged(position, controlIncidenciasExample.incidencias.size());
                        controlIncidenciasExample.dbHelper.deleteIncidencia(incidencia.id);
                    }
                }
            });

            icon_send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Incidencia incidencia = controlIncidenciasExample.incidencias.get(position);
                        if(Internet.isNetworkConnected()){
                            if(incidencia.foto!=null){
                                try{
                                    // Crear el MultipartBody.Part para la imagen
                                    RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), incidencia.foto);
                                    MultipartBody.Part imagePart = MultipartBody.Part.createFormData("archivo", "imagen.jpg", requestBody);
                                    ApiService apiService = RetrofitClient.getInstance(null,controlIncidenciasExample.mainActivity.desarrollo).create(ApiService.class);
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
                                                        JSONObject jsonIncident = new JSONObject();
                                                        jsonIncident.put("id_tipo_incidencia", incidencia.id_tipo_incidencia);
                                                        jsonIncident.put("id_usuario", incidencia.id_usuario);
                                                        jsonIncident.put("id_ruta",incidencia.id_ruta);
                                                        jsonIncident.put("foto", foto);
                                                        jsonIncident.put("comentarios",incidencia.comentarios);
                                                        jsonIncident.put("latitud",incidencia.coordenadas.latitude);
                                                        jsonIncident.put("longitud",incidencia.coordenadas.longitude);
                                                        //ErrorReporter.sendError(jsonObject);
                                                        ApiService apiService = RetrofitClient.getInstance(null,controlIncidenciasExample.mainActivity.desarrollo).create(ApiService.class);
                                                        // Convertir JSONObject a String y crear un RequestBody
                                                        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonIncident.toString());
                                                        Call<Void> call1 = apiService.agregarIncidencia(requestBody);

                                                        call1.enqueue(new Callback<Void>() {
                                                            @Override
                                                            public void onResponse(Call<Void> call1, Response<Void> response) {
                                                                if (!response.isSuccessful()) {
                                                                    Log.e("ErrorReporter", "Error al enviar la incidencia: " + response.code());
                                                                    controlIncidenciasExample.mainActivity.messages.showCustomToast("Error al enviar la incidencia");
                                                                    //dbHelper.saveIncidencia(id_tipo_incidencia_final,id_usuario,ruta.id,imageFile,comentarios,currentGeoCoordinates,0);
                                                                }else{
                                                                    //dbHelper.saveIncidencia(id_tipo_incidencia,id_usuario,ruta.id,imageFile,comentarios,currentGeoCoordinates,1);
                                                                    //TODO: Actualizar el estatus y recargar el adapter
                                                                    controlIncidenciasExample.mainActivity.messages.showCustomToast("Incidencia enviada con exitosamente");
                                                                }
                                                            }

                                                            @Override
                                                            public void onFailure(Call<Void> call1, Throwable t) {
                                                                Log.e("ErrorReporter", "Error al enviar el reporte: " + t.getMessage());
                                                            }
                                                        });
                                                    } else {
                                                        // Mostrar un mensaje de error al usuario
                                                        controlIncidenciasExample.mainActivity.messages.showCustomToast("Error al enviar la imagen");
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
                                                controlIncidenciasExample.mainActivity.messages.showCustomToast("Error al enviar la imagen");
                                                //uploadImageSinConexion();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                                            Log.e("Retrofit", "Error al enviar la imagen", t);
                                            // Mostrar un mensaje de error al usuario
                                            controlIncidenciasExample.mainActivity.messages.showCustomToast("Error al enviar la imagen");
                                            //uploadImageSinConexion();
                                        }
                                    });
                                }catch (Exception e){
                                    //
                                }
                            }else{
                                try {
                                    JSONObject jsonIncident = new JSONObject();
                                    jsonIncident.put("id_tipo_incidencia", incidencia.id_tipo_incidencia);
                                    jsonIncident.put("id_usuario", incidencia.id_usuario);
                                    jsonIncident.put("id_ruta",incidencia.id_ruta);
                                    jsonIncident.put("comentarios",incidencia.comentarios);
                                    jsonIncident.put("latitud",incidencia.coordenadas.latitude);
                                    jsonIncident.put("longitud",incidencia.coordenadas.longitude);
                                    //ErrorReporter.sendError(jsonObject);
                                    ApiService apiService = RetrofitClient.getInstance(null,controlIncidenciasExample.mainActivity.desarrollo).create(ApiService.class);
                                    // Convertir JSONObject a String y crear un RequestBody
                                    RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonIncident.toString());
                                    Call<Void> call = apiService.agregarIncidencia(requestBody);

                                    call.enqueue(new Callback<Void>() {
                                        @Override
                                        public void onResponse(Call<Void> call, Response<Void> response) {
                                            if (!response.isSuccessful()) {
                                                Log.e("ErrorReporter", "Error al enviar la incidencia: " + response.code());
                                                //dbHelper.saveIncidencia(id_tipo_incidencia_final,id_usuario,ruta.id,null,comentarios,currentGeoCoordinates,0);
                                                controlIncidenciasExample.mainActivity.messages.showCustomToast("Error al enviar la incidencia");
                                            }else{
                                                //dbHelper.saveIncidencia(id_tipo_incidencia,id_usuario,ruta.id,null,comentarios,currentGeoCoordinates,1);
                                                //TODO: Actualizar el estatus y recargar el adapter
                                                controlIncidenciasExample.mainActivity.messages.showCustomToast("Incidencia enviada sin foto exitosamente");
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<Void> call, Throwable t) {
                                            Log.e("ErrorReporter", "Error al enviar el reporte: " + t.getMessage());
                                            //dbHelper.saveIncidencia(id_tipo_incidencia_final,id_usuario,ruta.id,null,comentarios,currentGeoCoordinates,0);
                                            controlIncidenciasExample.mainActivity.messages.showCustomToast("Incidencia sin foto guardada dentro de la BD");
                                        }
                                    });
                                }catch (JSONException e){
                                    //
                                }
                            }
                        }else{
                            DialogFragment errorDialog = new ErrorDialogFragment();
                            errorDialog.show(controlIncidenciasExample.mainActivity.getSupportFragmentManager(), "errorDialog");
                        }
                    }
                }
            });
        }
    }
    private static void addMapMarker(GeoCoordinates geoCoordinates, int resourceId) {
        MapImage mapImage = MapImageFactory.fromResource(controlIncidenciasExample.mainActivity.getApplicationContext().getResources(), resourceId);
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

