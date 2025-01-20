package com.itsmarts.smartroutetruckapp.clases;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.itsmarts.smartroutetruckapp.MainActivity;
import com.itsmarts.smartroutetruckapp.api.ApiService;
import com.itsmarts.smartroutetruckapp.api.RetrofitClient;
import com.itsmarts.smartroutetruckapp.helpers.Messages;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Logger {
    private static MainActivity mainActivity;
    public static final String TAG = "Logger";

    public Logger(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void logError(String module, Exception error) {
        try {
            SharedPreferences sharedPreferences = mainActivity.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            int id_usuario = sharedPreferences.getInt("id_usuario", 0);
            // Crear un StringWriter para almacenar la traza de la pila
            StringWriter sw = new StringWriter();
            // Crear un PrintWriter que escriba en el StringWriter
            PrintWriter pw = new PrintWriter(sw);
            // Imprimir la traza de la pila en el PrintWriter
            error.printStackTrace(pw);
            // Obtener la traza de la pila como una cadena
            String stackTrace = sw.toString();
            JSONObject errorData = new JSONObject();
            errorData.put("usuario", id_usuario);
            errorData.put("fecha_hora", getCurrentTimeStamp());
            errorData.put("modulo", module);
            double latitude = mainActivity.currentGeoCoordinates.latitude;
            double longitude = mainActivity.currentGeoCoordinates.longitude;
            try {
                errorData.put("latitud", latitude);
                errorData.put("longitud", longitude);
                // Obtener el nombre del dispositivo
                String dispositivo = Build.MODEL;
                // Colocar el nombre del dispositivo en el JSONObject
                errorData.put("dispositivo", dispositivo);
                // Obtener la versión de Android
                errorData.put("version_android", Build.VERSION.RELEASE);
                // Obtener la versión de la aplicación
                String versionName = mainActivity.getApplicationContext().getPackageManager().getPackageInfo(mainActivity.getApplicationContext().getPackageName(), 0).versionName;
                errorData.put("version_aplicacion", versionName);
            }catch (Exception e){}
            // Obtener la ubicación (estado y ciudad) de donde se está emitiendo el error
            Geocoder geocoder = new Geocoder(mainActivity.getApplicationContext(), Locale.getDefault());
            List<Address> addresses=null;
            try{
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
            }catch (Exception e){}
            if (addresses != null && addresses.size() > 0) {
                String state = addresses.get(0).getAdminArea(); // Estado
                String city = addresses.get(0).getLocality(); // Ciudad
                try{
                    errorData.put("estado", state);
                    errorData.put("municipio", city);
                    errorData.put("error", stackTrace);
                }catch (Exception e){}
            }
            // Escribir en el archivo después de obtener todas las coordenadas y detalles
            writeToFile("error_log.json", errorData.toString());
            try {
                Messages.showErrorDetail(mainActivity, stackTrace);
            } catch (Exception e) {
                // Forzar la finalización de la aplicación
                System.exit(0);
            }
        } catch (Exception e) {
            writeToFile("critical_log.txt", "Critical Error: " + e.toString());
        }
    }

    public static void writeToFile(String filename, String data) {
        try (FileOutputStream fos = mainActivity.getApplicationContext().openFileOutput(filename, Context.MODE_APPEND)) {
            fos.write((data + "\n").getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getCurrentTimeStamp() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault()).format(new Date());
    }

    public static List<String> readLogFile(String filename) {
        List<String> logEntries = new ArrayList<>();
        try (FileInputStream fis = mainActivity.getApplicationContext().openFileInput(filename);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logEntries.add(line);
            }
        } catch (Exception e) {
            mainActivity.logger.logError(TAG,e);
        }
        return logEntries;
    }

    public static void checkInternetConnectionAndErrors() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                if (isNetworkAvailable()) {
                    JSONObject errorLogArray = readJsonFileToObject("error_log.json");
                    JSONArray eventLogArray = readJsonFileToArray("activity_log.json");
                    if (errorLogArray.length() > 0) {
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("error", errorLogArray);
                            jsonObject.put("events", eventLogArray);
                            //ErrorReporter.sendError(jsonObject);
                            ApiService apiService = RetrofitClient.getInstance(null).create(ApiService.class);
                            Call<Void> call = apiService.enviarError(jsonObject);

                            call.enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    if (!response.isSuccessful()) {
                                        Log.e("ErrorReporter", "Error al enviar el reporte: " + response.code());
                                    }
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    Log.e("ErrorReporter", "Error al enviar el reporte: " + t.getMessage());
                                }
                            });
                        } catch (Exception e) {
                            mainActivity.logger.logError(TAG,e);
                        }
                        // Una vez enviados los errores, podemos limpiar el archivo
                        clearLogFile("error_log.json");
                        // Una vez depurado, también podemos limpiar el otro archivo
                        clearLogFile("activity_log.json");
                    }else{
                        // Una vez depurado, también podemos limpiar el otro archivo
                        clearLogFile("activity_log.json");
                    }
                }

                return null;
            }
        }.execute();
    }

    // Método para leer el archivo JSON que contiene múltiples objetos y almacenarlos en un JSONArray
    private static JSONArray readJsonFileToArray(String filename) {
        JSONArray jsonArray = new JSONArray();
        try (FileInputStream fis = mainActivity.getApplicationContext().openFileInput(filename);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Trim() se utiliza para eliminar espacios en blanco al principio y al final de la línea
                String trimmedLine = line.trim();
                if (!trimmedLine.isEmpty()) {
                    // Si la línea no está vacía, la parseamos como un JSONObject
                    JSONObject jsonObject = new JSONObject(trimmedLine);
                    // Y la agregamos al JSONArray
                    jsonArray.put(jsonObject);
                }
            }
        } catch (Exception e) {
            mainActivity.logger.logError(TAG,e);
        }
        return jsonArray;
    }

    private static JSONObject readJsonFileToObject(String filename) {
        try (FileInputStream fis = mainActivity.getApplicationContext().openFileInput(filename);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            JSONObject result = new JSONObject();
            if (!content.toString().isEmpty()) {
                result = new JSONObject(content.toString());
            }
            return result;
        } catch (Exception e) {
            mainActivity.logger.logError(TAG,e);
            return new JSONObject();  // Retorna un objeto vacío en caso de error
        }
    }

    public static boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mainActivity.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            // return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
            if(activeNetworkInfo != null && activeNetworkInfo.isConnected()){
                try {
                    Process p = Runtime.getRuntime().exec("ping -c 1 www.google.es");
                    int val = p.waitFor();
                    boolean reachable = (val == 0);
                    return reachable;

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return false;
            }
        }
        return false;
    }

    private static void clearLogFile(String filename) {
        try (FileOutputStream fos = mainActivity.getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE)) {
            // Escribimos una cadena vacía para limpiar el archivo
            fos.write("".getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}