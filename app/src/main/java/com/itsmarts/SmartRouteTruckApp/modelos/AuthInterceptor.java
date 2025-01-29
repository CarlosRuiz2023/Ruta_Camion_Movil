package com.itsmarts.SmartRouteTruckApp.modelos;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private String authorization;

    public AuthInterceptor(String authorization) {
        this.authorization = authorization;
    }



    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request();

        Request.Builder requestBuilder = original.newBuilder();

        // Add the authorization header only if the token is not null or empty
        if (authorization != null) {
            requestBuilder.addHeader("authorization", authorization);
        }

        Request request = requestBuilder.build();
        return chain.proceed(request);
    }
}
