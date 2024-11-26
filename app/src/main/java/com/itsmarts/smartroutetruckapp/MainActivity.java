package com.itsmarts.smartroutetruckapp;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.here.sdk.core.Anchor2D;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.GeoPolyline;
import com.here.sdk.core.Point2D;
import com.here.sdk.mapview.MapView;
import com.itsmarts.smartroutetruckapp.clases.AvoidZonesExample;
import com.itsmarts.smartroutetruckapp.clases.ControlPointsExample;
import com.itsmarts.smartroutetruckapp.clases.NavigationExample;
import com.itsmarts.smartroutetruckapp.clases.mapHelper;
import com.itsmarts.smartroutetruckapp.helpers.Messages;
import com.itsmarts.smartroutetruckapp.modelos.RoutesWithId;

import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    ConstraintLayout hideable_content, home_content;
    public MapView mapView;
    public mapHelper mMapHelper;
    NavigationView nmd;
    MaterialToolbar toolbar;
    DrawerLayout drawerLayout;
    public Messages messages;
    public NavigationExample navigationExample;
    public TextView messageView;
    public GeoCoordinates currentGeoCoordinates, coordenadasDestino;
    public AvoidZonesExample avoidZonesExample;
    public ControlPointsExample controlPointsExample;
    public List<RoutesWithId> rutas;
    public Animation rotateAnimation, cargaAnimacion, animSalida, animacionClick;
    public boolean animacionEjecutada = false, isFirstClick = true, isNightMode = false, isMenuOpen = false, rutaGenerada = false, isTrackingCamera = true, isExactRouteEnabled = false, isSimularRutaVisible = false, isRutaVisible = false, isDialogShowing = false, routeSuccessfullyProcessed = false;
    public RoutesWithId ruta,rutaPre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        mMapHelper = new mapHelper(this);
        messages = new Messages(this,getApplicationContext());
        mMapHelper.initializeHERESDK(this);
        setContentView(R.layout.activity_main);
        hideable_content = findViewById(R.id.hideable_content);
        home_content = findViewById(R.id.home_content);
        mapView = findViewById(R.id.map_view);
        messageView = findViewById(R.id.messageView);
        mapView.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            navigationExample = new NavigationExample(this, mapView, messageView);
            //avoidZonesExample = new AvoidZonesExample(this,mapView,getLayoutInflater(),fbResetZona,fbSave,llReset,llSave,dbHelper);
            //controlPointsExample = new ControlPointsExample(this,mapView,getLayoutInflater(),fbSave,llSave,dbHelper);
            navigationExample.startLocationProvider();
        }else{
            //mMapHelper.permisoLocalizacion(this, this);
            mMapHelper.handleAndroidPermissions();
        }

        nmd = findViewById(R.id.nmd);
        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        this.setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.dialog_ok,R.string.dialog_cancel);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        nmd.setNavigationItemSelectedListener(this);
        // Crear un handler para ocultar el contenido después de 1 minuto (60,000 ms)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            hideable_content.setVisibility(View.GONE);
            home_content.setVisibility(View.VISIBLE);
            int yOffsetPx = Math.round(16 * this.getResources().getDisplayMetrics().density);
            mapView.setWatermarkLocation(new Anchor2D(0.5, 1), new Point2D(0, -yOffsetPx));
            mMapHelper.loadMapScene(mapView, this);
            mMapHelper.tiltMap(mapView);
            mapView.setVisibility(View.VISIBLE);
            toolbar.setVisibility(View.VISIBLE);
        }, 10000); // 60,000 ms = 1 minuto
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        String title = ""+item.getTitle();
        switch (title) {
            case "Salir":
                finish();
                break;
            default:
                String msg = item.getTitle().toString();
                Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_bottom_nav, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        String name = item.getTitle().toString();
        Log.e("Prueba",""+name);

        // Handle other menu items you have defined in your menu resource file
        switch (name) {
            case "GPS preciso":
                // Code to handle settings selection
                Toast.makeText(this, name+" selected", Toast.LENGTH_SHORT).show();
                if(item.isChecked()){
                    item.setChecked(false);
                }else{
                    item.setChecked(true);
                }
                break;
            case "Mapa offline":
                // Code to handle refresh selection
                Toast.makeText(this, name+" selected", Toast.LENGTH_SHORT).show();
                if(item.isChecked()){
                    item.setChecked(false);
                }else{
                    item.setChecked(true);
                }
                break;
            default:
                Log.e("Prueba","default");
                // Code to handle other menu items
                Toast.makeText(this, name+" selected", Toast.LENGTH_SHORT).show();
                break;
        }

        // If the ID doesn't match any handled items, return false to allow system handling
        return super.onOptionsItemSelected(item);
    }
    public static double distanceToPolyline(GeoCoordinates point, GeoPolyline polyline) {
        double minDistance = Double.MAX_VALUE;
        GeoCoordinates closestPoint = null;
        List<GeoCoordinates> vertices = polyline.vertices;

        // Encuentra el vértice más cercano al punto dado
        for (GeoCoordinates vertex : vertices) {
            double distance = point.distanceTo(vertex);
            if (distance < minDistance) {
                minDistance = distance;
                closestPoint = vertex;
            }
        }

        // Devuelve la distancia al punto más cercano
        return minDistance;
    }
}