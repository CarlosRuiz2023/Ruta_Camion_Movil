package com.itsmarts.smartroutetruckapp.api;

import android.util.Log;

import com.itsmarts.smartroutetruckapp.modelos.AuthInterceptor;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    //private static final String BASE_URL = "http://ec2-user@ec2-18-205-239-47.compute-1.amazonaws.com:3002/";
    //private static final String BASE_URL = "http://192.168.11.26:3002/";
    private static final String BASE_URL = "http://72.167.220.178:3002/";
    private static Retrofit retrofit;

    public static Retrofit getInstance(String token) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(new AuthInterceptor(token)) // Add the interceptor here
                .build();
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        return retrofit;
    }
}

