package com.itsmarts.smartroutetruckapp.api;

import com.itsmarts.smartroutetruckapp.modelos.PointWithId;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

import java.util.List;

public interface ApiService {
    @GET("api/puntos-de-control/obtener")
    Call<ResponseBody> getPuntosDeControl();

    @GET("api/zonas/obtener-zonas-peligrosas")
    Call<ResponseBody> getZonasPeligrosas();

    @GET("api/zonas/obtener-zonas-prohibidas")
    Call<ResponseBody> getZonasProhibidas();

    @GET("api/rutas/obtener")
    Call<ResponseBody> getRutas();

    @GET("api/rutas/obtener/{id}")
    Call<ResponseBody> getRuta(@Path("id") int id);
}

