package com.itsmarts.SmartRouteTruckApp.helpers;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Internet {

    private static final String TAG = "Internet";

    public interface NetworkCallback {
        void onResult(boolean isConnected);
    }

    public static void isNetworkConnected(NetworkCallback callback) {
        try{
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(() -> {
                boolean isConnected = false;
                try {
                    Process process = Runtime.getRuntime().exec("ping -c 1 8.8.8.8"); // Google's public DNS server
                    int exitValue = process.waitFor();
                    isConnected = (exitValue == 0);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }

                boolean finalIsConnected = isConnected;
                handler.post(() -> callback.onResult(finalIsConnected)); // Envía el resultado al hilo principal
            });
        }catch(Exception e){
            Log.e(TAG, "Error checking network connection: " + e.getMessage());
        }
    }
}

