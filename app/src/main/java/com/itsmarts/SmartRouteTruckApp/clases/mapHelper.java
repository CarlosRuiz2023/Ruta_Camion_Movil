package com.itsmarts.SmartRouteTruckApp.clases;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.GeoCoordinatesUpdate;
import com.here.sdk.core.GeoOrientationUpdate;
import com.here.sdk.core.engine.SDKNativeEngine;
import com.here.sdk.core.engine.SDKOptions;
import com.here.sdk.core.errors.InstantiationErrorException;
import com.here.sdk.mapview.MapCameraAnimation;
import com.here.sdk.mapview.MapCameraAnimationFactory;
import com.here.sdk.mapview.MapError;
import com.here.sdk.mapview.MapScene;
import com.here.sdk.mapview.MapScheme;
import com.here.sdk.mapview.MapView;
import com.here.time.Duration;
import com.itsmarts.SmartRouteTruckApp.MainActivity;
import com.itsmarts.SmartRouteTruckApp.R;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class mapHelper {
    private static final int REQUEST_INTERNET_PERMISSION = 1;
    static final int REQUEST_LOCATION_PERMISSION = 2;
    private PermissionResultCallback permissionResultCallback;
    private Dialog gpsDialog;
    private PermissionsRequestor permissionsRequestor;
    private MainActivity mainActivity;
    private int count=0;
    private Handler handler = new Handler();

    public mapHelper(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void initializeHERESDK(Context context) {
        String accessKeyID = "6CnQKiwjMhMY9wZ9ftREaw";
//        String accessKeyID = "Lpoaqf87Jq0VqMBz-VKjnQ";
        String accessKeySecret = "NhXkOjWLZESho7uX6gAb5hrc3g03Hkk2DUck6YsKY6B0lrutZZr0XKpqDkcrTMee-ffLnkJTMOBrHDAO0UevGw";
//        String accessKeySecret = "m2I_fbgo-AzYyXB2NXqKKNdi7pN7JCOlXnh31gwNfSQCRkI79RKrFjkV78Fifkvc3G8RpStyhoHqvXtsY3rjnw";
        SDKOptions options = new SDKOptions(accessKeyID, accessKeySecret);
        try {
            SDKNativeEngine.makeSharedInstance(context, options);
        } catch (InstantiationErrorException e) {
            //throw new RuntimeException("Fallo al iniciar el SDK: " + e.error.name());
            Log.e("initializeHERESDK()", "Fallo al iniciar el SDK: " + e.error.name());
        }
    }

    public void loadMapScene(MapView mapView, Context context) {
        mapView.getMapScene().loadScene(MapScheme.NORMAL_DAY, new MapScene.LoadSceneCallback() {
            @Override
            public void onLoadScene(@Nullable MapError mapError) {
                if (mapError == null) {

                } else {
                    Log.d("loadMapScene()", "Error al cargar la escena del mapa: " + mapError.name());
                }
            }
        });
    }

    public void permisoLocalizacion(Activity activity, PermissionResultCallback callback) {
        this.permissionResultCallback = callback;
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            callback.permisoConcedido();
        }
    }

    public interface PermissionResultCallback {
        void permisoConcedido();
        void permisoDenegado();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_INTERNET_PERMISSION:
            case REQUEST_LOCATION_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (permissionResultCallback != null) {
                        permissionResultCallback.permisoConcedido();
                    }
                } else {
                    if (permissionResultCallback != null) {
                        permissionResultCallback.permisoDenegado();
                        permisoLocalizacion(mainActivity, permissionResultCallback);
                    }
                }
                break;
        }
    }

    public void tiltMap(MapView mapView) {
        double bearing = mapView.getCamera().getState().orientationAtTarget.bearing;
        double tilt =  10;
        mapView.getCamera().setOrientationAtTarget(new GeoOrientationUpdate(bearing, tilt));
    }

    public boolean isGPSEnabled(Activity activity) {
        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void disposeHERESDK() {
        SDKNativeEngine sdkNativeEngine = SDKNativeEngine.getSharedInstance();
        if (sdkNativeEngine != null) {
            sdkNativeEngine.dispose();
            SDKNativeEngine.setSharedInstance(null);
        }
    }

    public GeoCoordinates generateCoords(Activity activity, Intent intent) {
        GeoCoordinates coordenadasDestino = null;
        try {
            String dat = "";
            if (intent != null && intent.getData() != null) {
                if (intent.getScheme().equals("geo")) {
                    Uri data = intent.getData();
                    dat = data.toString();
                    Log.d("mapHelper", "URI recibida: " + dat);
                    Pattern p = Pattern.compile("(-?\\d+\\.\\d+)");
                    Matcher m = p.matcher(dat);
                    List<Double> coordinates = new ArrayList<>();
                    while (m.find()) {
                        coordinates.add(Double.parseDouble(m.group()));
                    }
                    if (coordinates.size() >= 2) {
                        coordenadasDestino = new GeoCoordinates(coordinates.get(0), coordinates.get(1));
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(activity.getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return coordenadasDestino;
    }

    public void flyTo(GeoCoordinates geoCoordinates) {
        GeoCoordinatesUpdate geoCoordinatesUpdate = new GeoCoordinatesUpdate(geoCoordinates);
        double bowFactor = 1;
        MapCameraAnimation animation = MapCameraAnimationFactory.flyTo(geoCoordinatesUpdate, bowFactor, Duration.ofSeconds(3));
        mainActivity.mapView.getCamera().startAnimation(animation);
    }

    /**
     * Método para dibujar un polígono en el mapa usando una lista de vértices.
     *
     * @param vertices Lista de coordenadas que componen el polígono.
     */
    public void handleAndroidPermissions() {
        permissionsRequestor = new PermissionsRequestor(mainActivity);
        permissionsRequestor.request(new PermissionsRequestor.ResultListener(){

            @Override
            public void permissionsGranted() {
                mainActivity.navigationExample.startLocationProvider();
            }

            @Override
            public void permissionsDenied() {
                if (ContextCompat.checkSelfPermission(mainActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mainActivity.navigationExample.startLocationProvider();
                }else{
                    if(count==0){
                        count++;
                        handleAndroidPermissions();
                    }else{
                        mainActivity.messages.showCustomToast("Es necesario aprovar los permisos para el funcionamiento de la App.");
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                            }
                        },3000);
                        mainActivity.finish();
                    }
                }
                if (ContextCompat.checkSelfPermission(mainActivity, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                    if(count==0){
                        count++;
                        handleAndroidPermissions();
                    }else{
                        mainActivity.messages.showCustomToast("Es necesario aprovar los permisos para el funcionamiento de la App.");
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                            }
                        },3000);
                        mainActivity.finish();
                    }
                }
            }
        });
    }

    public void showGPSDisabledDialog(final Activity activity) {
        if (gpsDialog != null && gpsDialog.isShowing()) {
            return;
        }

        gpsDialog = new Dialog(activity);
        gpsDialog.setContentView(R.layout.ventana_activar_gps);
        gpsDialog.setCancelable(false);
        gpsDialog.setCanceledOnTouchOutside(false);

        Button btnEnableGPS = gpsDialog.findViewById(R.id.btnEnableGPS);
        Button btnCancel = gpsDialog.findViewById(R.id.btnCancel);

        btnEnableGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                activity.startActivity(intent);
                gpsDialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gpsDialog.dismiss();
                Toast.makeText(activity, "El GPS es necesario para usar esta app", Toast.LENGTH_LONG).show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!isGPSEnabled(activity)) {
                            showGPSDisabledDialog(activity);
                        }
                    }
                }, 100);
            }
        });
        gpsDialog.show();
    }
}
