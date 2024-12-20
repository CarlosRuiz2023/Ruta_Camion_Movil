package com.itsmarts.smartroutetruckapp.api;

import com.itsmarts.smartroutetruckapp.modelos.LoginRequest;
import com.itsmarts.smartroutetruckapp.modelos.PointWithId;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

import java.util.List;

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

    @POST("api/auth/logout")
    Call<ResponseBody> desloguearse();
}

