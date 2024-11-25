package com.itsmarts.smartroutetruckapp;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.here.sdk.core.Anchor2D;
import com.here.sdk.core.Point2D;
import com.here.sdk.mapview.MapView;
import com.itsmarts.smartroutetruckapp.clases.mapHelper;
import com.itsmarts.smartroutetruckapp.helpers.Messages;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    ConstraintLayout hideable_content;
    MapView mapView;
    mapHelper mMapHelper;
    NavigationView nmd;
    MaterialToolbar toolbar;
    DrawerLayout drawerLayout;
    public Messages messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        mMapHelper = new mapHelper(this);
        messages = new Messages(this,getApplicationContext());
        mMapHelper.initializeHERESDK(this);
        setContentView(R.layout.activity_main);
        hideable_content = findViewById(R.id.hideable_content);
        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //navigationExample.startLocationProvider();
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
}