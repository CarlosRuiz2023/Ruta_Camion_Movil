package com.itsmarts.SmartRouteTruckApp.api;

import com.itsmarts.SmartRouteTruckApp.modelos.HistorialRequest;
import com.itsmarts.SmartRouteTruckApp.modelos.LoginRequest;
import com.itsmarts.SmartRouteTruckApp.modelos.RecuperarContraseniaRequest;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiService {
    @GET("api/puntos-de-control/obtener")
    Call<ResponseBody> getPuntosDeControl();

    @GET("api/zonas/obtener-zonas-peligrosas")
    Call<ResponseBody> getZonasPeligrosas();

    @GET("api/zonas/obtener-zonas-prohibidas")
    Call<ResponseBody> getZonasProhibidas();

    @GET("api/rutas/obtener/true")
    Call<ResponseBody> getRutas();

    @GET("api/asignaciones/asignadas/{id}")
    Call<ResponseBody> getAsignaciones(@Path("id") int id);

    /*@GET("api/asignaciones/asignadas/2")
    Call<ResponseBody> getAsignaciones();*/

    @POST("api/auth/login")
    Call<ResponseBody> loguearse(@Body LoginRequest loginRequest);

    @POST("api/auth/recuperar")
    Call<ResponseBody> recuperarContrasenia(@Body RecuperarContraseniaRequest recuperarContraseniaRequest);

    @POST("api/auth/logout")
    Call<ResponseBody> desloguearse();

    @POST("api/usuarios/desloguear/{id}")
    Call<ResponseBody> getDesloguearUsuario(@Path("id") int id);

    @POST("api/analisis-de-uso-ruta/agregar")
    Call<ResponseBody> mandarHistorial(@Body HistorialRequest historialRequest);

    @POST("api/logs/registrarLog")
    Call<Void> enviarError(@Body RequestBody requestBody);

    @Multipart
    @POST("api/incidencias/cargarImagen")
    Call<ResponseBody> cargarImagen(
            @Part MultipartBody.Part imagePart
    );

    @POST("api/incidencias/agregarIncidencia")
    Call<Void> agregarIncidencia(@Body RequestBody requestBody);

    @GET("api/versiones/obtenerUltimaVersionMovil")
    Call<ResponseBody> getLastVersion();
}

