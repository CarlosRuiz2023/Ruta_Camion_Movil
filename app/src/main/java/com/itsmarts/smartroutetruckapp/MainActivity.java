package com.itsmarts.smartroutetruckapp;

import static android.view.View.VISIBLE;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.here.sdk.core.Anchor2D;
import com.here.sdk.core.Color;
import com.here.sdk.core.GeoCircle;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.GeoPolyline;
import com.here.sdk.core.LanguageCode;
import com.here.sdk.core.PickedPlace;
import com.here.sdk.core.Point2D;
import com.here.sdk.core.Rectangle2D;
import com.here.sdk.core.Size2D;
import com.here.sdk.core.errors.InstantiationErrorException;
import com.here.sdk.location.LocationAccuracy;
import com.here.sdk.mapview.LineCap;
import com.here.sdk.mapview.MapError;
import com.here.sdk.mapview.MapImage;
import com.here.sdk.mapview.MapImageFactory;
import com.here.sdk.mapview.MapMarker;
import com.here.sdk.mapview.MapMeasureDependentRenderSize;
import com.here.sdk.mapview.MapPolygon;
import com.here.sdk.mapview.MapPolyline;
import com.here.sdk.mapview.MapScene;
import com.here.sdk.mapview.MapScheme;
import com.here.sdk.mapview.MapView;
import com.here.sdk.mapview.MapViewBase;
import com.here.sdk.mapview.PickMapContentResult;
import com.here.sdk.mapview.PickMapItemsResult;
import com.here.sdk.mapview.RenderSize;
import com.here.sdk.routing.Route;
import com.here.sdk.search.OfflineSearchEngine;
import com.here.sdk.search.Place;
import com.here.sdk.search.PlaceCategory;
import com.here.sdk.search.PlaceIdSearchCallback;
import com.here.sdk.search.SearchCallback;
import com.here.sdk.search.SearchEngine;
import com.here.sdk.search.SearchError;
import com.here.sdk.search.SearchOptions;
import com.here.sdk.search.TextQuery;
import com.itsmarts.smartroutetruckapp.adaptadores.RouterAsignedAdapter;
import com.itsmarts.smartroutetruckapp.api.ApiService;
import com.itsmarts.smartroutetruckapp.api.RetrofitClient;
import com.itsmarts.smartroutetruckapp.bd.DatabaseHelper;
import com.itsmarts.smartroutetruckapp.clases.AnimatorNew;
import com.itsmarts.smartroutetruckapp.clases.AvoidZonesExample;
import com.itsmarts.smartroutetruckapp.clases.ControlPointsExample;
import com.itsmarts.smartroutetruckapp.clases.NavigationEventHandler;
import com.itsmarts.smartroutetruckapp.clases.NavigationExample;
import com.itsmarts.smartroutetruckapp.clases.OfflineMap;
import com.itsmarts.smartroutetruckapp.clases.RoutingExample;
import com.itsmarts.smartroutetruckapp.clases.TruckConfig;
import com.itsmarts.smartroutetruckapp.clases.mapHelper;
import com.itsmarts.smartroutetruckapp.fragments.ModalBottomSheetFullScreenFragmentPuntos;
import com.itsmarts.smartroutetruckapp.fragments.ModalBottomSheetFullScreenFragmentZonas;
import com.itsmarts.smartroutetruckapp.helpers.Geocercas;
import com.itsmarts.smartroutetruckapp.helpers.Messages;
import com.itsmarts.smartroutetruckapp.modelos.PointWithId;
import com.itsmarts.smartroutetruckapp.modelos.PolygonWithId;
import com.itsmarts.smartroutetruckapp.modelos.RoutesWithId;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, NavigationEventHandler.SpeedUpdateListener, NavigationEventHandler.DestinationDistanceListener, NavigationEventHandler.DestinationReachedListener{

    public ConstraintLayout hideable_content, home_content, truck_config_content;
    public MapView mapView;
    public mapHelper mMapHelper;
    NavigationView nmd;
    public MaterialToolbar toolbar;
    DrawerLayout drawerLayout;
    public Messages messages;
    public NavigationExample navigationExample;
    public TextView messageView, txtNavegacion, speedTextView, txtTerminarRuta, distanceTextView, timeTextView, txtDescargaInfo, txtProcesoActualizacion;
    public GeoCoordinates currentGeoCoordinates, coordenadasDestino, coordenada1, coordenada2, geoCoordinatesPOI = null, destinationGeoCoordinates;
    public AvoidZonesExample avoidZonesExample;
    public ControlPointsExample controlPointsExample;
    public List<RoutesWithId> rutas = new ArrayList<>(), rutasAsignadas = new ArrayList<>();
    public Animation rotateAnimation, cargaAnimacion, animSalida, animacionClick, animEntrada;
    public boolean animacionEjecutada = false, isFirstClick = true, isMenuOpen = false, rutaGenerada = false, isTrackingCamera = true, isExactRouteEnabled = false, isSimularRutaVisible = false, isRutaVisible = false, isDialogShowing = false, routeSuccessfullyProcessed = false, activarGeocercas = true, mapOfflineMexDownload = false;
    public RoutesWithId ruta,rutaPre;
    public ImageButton trackCamara, btnTerminarRuta, btnGeocercas;
    public ImageView imgVelocidad;
    public View detallesRuta;
    public LinearLayout llGeocerca, llPois, llMapas;
    public Geocercas geocercas;
    public RoutingExample routingExample;
    public DatabaseHelper dbHelper;
    public Handler handler = new Handler();
    public RouterAsignedAdapter adapterAsignedRoutes;
    private List<MapMarker> mapMarkersPOIs = new ArrayList<>();
    private List<Place> placesList = new ArrayList<>();
    private MapPolygon mapPolygon;
    public SearchEngine searchEngine;
    public OfflineSearchEngine offlineSearchEngine;
    /** Clase para funciones de descarga de mapa*/
    public OfflineMap offlineMap;
    /**Botton para comenzar la descarga de mapa*/
    public Button btnDescargar, btnBuscarActualizaciones, btnIniciarActualizacion, recalculateRouteButton;
    /** TextView para mosntrar como se descarga y el porcentaje de descarga del mapa*/
    public TextView txtProcesoDescarga,txtDescargaTitulo;
    public FloatingActionButton fbEliminarPoi, fbMapas;
    public int styleCounter=0;
    // INICIALIZACION DE LA VARIABLE TIPO MapScheme PARA EL ESTILO DEL MAPA POR DEFECTO
    private MapScheme style = MapScheme.NORMAL_DAY;
    public TruckConfig truckConfig;
    private LottieAnimationView likeAnimationView;
    private AnimatorNew likeAnimator;
    List<CompletableFuture<ResponseBody>> futures = new ArrayList<>();
    private static final String TAG = "MainActivity";
    public List<PolygonWithId> poligonos = new ArrayList<>();
    public List<PointWithId> puntos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        initializeFirstsClass();
        mMapHelper.initializeHERESDK(this);
        setContentView(R.layout.activity_main);
        initializeComponents();
        mapView.onCreate(savedInstanceState);
        mMapHelper.loadMapScene(mapView, this);
        mMapHelper.tiltMap(mapView);
        setTapGestureHandler();
        initializeBD();
        initializeSecondClass();
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            navigationExample.startLocationProvider();
        }else{
            //mMapHelper.permisoLocalizacion(this, this);
            mMapHelper.handleAndroidPermissions();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        String title = ""+item.getTitle();
        Animation animSalida = null;
        View dialogView = null;      AlertDialog.Builder builder = null;
        switch (title) {
            case "Obtener Ruta":
                dialogView = getLayoutInflater().inflate(R.layout.ventana_seleccionar_ruta, null);
                builder = new AlertDialog.Builder(MainActivity.this);
                builder.setView(dialogView);
                final AlertDialog alertDialogRuta = builder.create();

                TextView textView10 = dialogView.findViewById(R.id.textView10);
                final Button btnCancelarRuta = dialogView.findViewById(R.id.btnCancelar);
                LinearLayout linearLayout = dialogView.findViewById(R.id.linearLayout);
                RecyclerView recyclerView = dialogView.findViewById(R.id.routesRecyclerView);
                TextView sinRutasTextView = dialogView.findViewById(R.id.sinRutasTextView);
                ScrollView scrollView = dialogView.findViewById(R.id.scrollView);

                List<CompletableFuture<ResponseBody>> futures1 = new ArrayList<>();
                futures1.add(obtenerAsignaciones());
                // Espera a que todos los futures terminen
                CompletableFuture<Void> allOf = CompletableFuture.allOf(futures1.toArray(new CompletableFuture[0]));
                allOf.whenComplete((result, ex) -> {
                    if (ex != null) {
                        // Manejo de errores, si es necesario
                        Log.e(TAG, "Error during downloads", ex);
                    }

                    adapterAsignedRoutes = new RouterAsignedAdapter(this,alertDialogRuta,rutasAsignadas);

                    if (adapterAsignedRoutes.getItemCount() == 0) {
                        scrollView.setVisibility(View.GONE);
                        sinRutasTextView.setVisibility(View.VISIBLE);
                    }else{
                        recyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
                        recyclerView.setAdapter(adapterAsignedRoutes);
                        scrollView.setVisibility(View.VISIBLE);
                        sinRutasTextView.setVisibility(View.GONE);
                    }

                    btnCancelarRuta.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            btnCancelarRuta.startAnimation(animacionClick);
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    alertDialogRuta.dismiss();
                                }
                            }, 400);
                        }
                    });
                    alertDialogRuta.show();
                });
                break;
            case "Puntos cercanos":
                avoidZonesExample.cleanPolygon();
                controlPointsExample.cleanPoint();
                setTapGestureHandler();
                isTrackingCamera = false;
                trackCamara.setImageResource(R.drawable.track_on);
                navigationExample.stopCameraTracking();
                clearMapMarkersPOIsAndCircle(true);

                dialogView = LayoutInflater.from(MainActivity.this).inflate(R.layout.ventana_buscar_poi, null);

                final Dialog dialogPoi = new Dialog(MainActivity.this);
                dialogPoi.setContentView(dialogView);
                dialogPoi.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                Spinner spinnerPoiType = dialogView.findViewById(R.id.spinnerPoiType);
                EditText editTextRadius = dialogView.findViewById(R.id.editTextRadius);
                Button btnBuscar = dialogView.findViewById(R.id.btnBuscar);
                final Button btnCancelarPoi = dialogView.findViewById(R.id.btnCancelar);

                // Configurar el spinner con los tipos de POI
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(MainActivity.this,
                        R.array.poi_types_array, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerPoiType.setAdapter(adapter);

                btnBuscar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String selectedPoiType = spinnerPoiType.getSelectedItem().toString();
                        String radiusStr = editTextRadius.getText().toString().trim();
                        if (!radiusStr.isEmpty() && Integer.parseInt(radiusStr)!=0 && Integer.parseInt(radiusStr)>=1000) {
                            int radius = Integer.parseInt(radiusStr);
                            // Llamar a la función para buscar el POI más cercano
                            searchNearestPoi(selectedPoiType, radius);
                            llPois.setVisibility(VISIBLE);
                            dialogPoi.dismiss();

                        } else {
                            Toast.makeText(MainActivity.this, "Por favor, ingrese un radio válido", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                btnCancelarPoi.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogPoi.dismiss();
                    }
                });

                dialogPoi.show();
                break;
            case "Puntos de Control":
                trackCamara.setImageResource(R.drawable.track_on);
                navigationExample.stopCameraTracking();
                avoidZonesExample.cleanPolygon();
                controlPointsExample.cleanPoint();
                mapView.getGestures().setTapListener(null);
                animSalida = AnimationUtils.loadAnimation(MainActivity.this, R.anim.salida2);
                clearMapMarkersPOIsAndCircle(true);
                controlPointsExample.getModalBottomSheetFullScreenFragment().show(getSupportFragmentManager(), ModalBottomSheetFullScreenFragmentPuntos.TAG);
                break;
            case "Zonas Prohibidas":
                trackCamara.setImageResource(R.drawable.track_on);
                navigationExample.stopCameraTracking();
                controlPointsExample.cleanPoint();
                avoidZonesExample.cleanPolygon();
                mapView.getGestures().setTapListener(null);
                animSalida = AnimationUtils.loadAnimation(MainActivity.this, R.anim.salida2);
                clearMapMarkersPOIsAndCircle(true);
                avoidZonesExample.getModalBottomSheetFullScreenFragment().show(getSupportFragmentManager(), ModalBottomSheetFullScreenFragmentZonas.TAG);
                break;
            case "Descargar Mapa":
                avoidZonesExample.cleanPolygon();
                controlPointsExample.cleanPoint();

                LayoutInflater inflater = getLayoutInflater();
                dialogView = inflater.inflate(R.layout.ventana_descargar_mapa, null);

                builder = new AlertDialog.Builder(MainActivity.this);
                builder.setView(dialogView);

                final AlertDialog alertDialogOffline = builder.create();

                Button btnConsultar = dialogView.findViewById(R.id.btnConsultarDescarga);
                btnDescargar = dialogView.findViewById(R.id.btnDescargarMapa);
                txtDescargaTitulo = dialogView.findViewById(R.id.txtDescargaInfo);
                txtProcesoDescarga =  dialogView.findViewById(R.id.txtProcesoDescarga);
                btnConsultar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        btnDescargar.setEnabled(true);
                        offlineMap.onDownloadMexicoRegionClicked();
                    }
                });

                btnDescargar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (btnDescargar.getText().equals(getString(R.string.descargar))){
                            txtProcesoDescarga.setVisibility(VISIBLE);
                            offlineMap.onDownloadMapMexicoClicked();
                            btnConsultar.setEnabled(false);
                            btnDescargar.setText(getString(R.string.cancelar_descarga));
                        }else{
                            btnDescargar.setText(getString(R.string.descargar));
                            btnConsultar.setEnabled(true);
                            offlineMap.onCancelMapDownloadClicked();

                        }
                    }
                });

                alertDialogOffline.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        boolean response = offlineMap.onCancelMapDownloadFromDismiss();
                        if (response) {
                            Toast.makeText(MainActivity.this, "La descarga del mapa fue cancelada", Toast.LENGTH_SHORT).show();
                        }

                    }

                });

                alertDialogOffline.show();
                break;
            case "Vehiculo":
                avoidZonesExample.cleanPolygon();
                controlPointsExample.cleanPoint();
                mapView.getGestures().setTapListener(null);
                truckConfig.mostrarMenu();
                break;
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

        // Handle other menu items you have defined in your menu resource file
        switch (name) {
            case "GPS preciso":
                // Code to handle settings selection
                Toast.makeText(this, name+" selected", Toast.LENGTH_SHORT).show();
                isExactRouteEnabled = !item.isChecked();
                if(!isExactRouteEnabled){
                    item.setChecked(false);
                }else{
                    item.setChecked(true);
                }
                restartLocationUpdates();
                break;
            case "Mapa offline":
                // Code to handle refresh selection
                Toast.makeText(this, name+" selected", Toast.LENGTH_SHORT).show();
                if(item.isChecked()){
                    item.setChecked(false);
                    offlineMap.onSwitchOfflineButtonClicked();
                }else{
                    item.setChecked(true);
                    offlineMap.onSwitchOnlineButtonClicked();
                }
                break;
            default:
                // Code to handle other menu items
                Toast.makeText(this, name+" selected", Toast.LENGTH_SHORT).show();
                break;
        }

        // If the ID doesn't match any handled items, return false to allow system handling
        return super.onOptionsItemSelected(item);
    }

    private void initializeFirstsClass(){
        mMapHelper = new mapHelper(this);
        messages = new Messages(this);
        geocercas = new Geocercas(this);
        likeAnimator = new AnimatorNew();
    }
    private void initializeSecondClass(){
        routingExample = new RoutingExample(this);
        offlineMap = new OfflineMap(this);
        truckConfig = new TruckConfig(this);
        navigationExample = new NavigationExample(this, mapView, messageView);
        navigationExample.getNavigationEventHandler().setSpeedUpdateListener(this);
        navigationExample.getNavigationEventHandler().setDestinationDistanceListener(this);
        navigationExample.getNavigationEventHandler().setDestinationReachedListener(this);
        avoidZonesExample = new AvoidZonesExample(this,mapView,getLayoutInflater(),dbHelper);
        controlPointsExample = new ControlPointsExample(this,mapView,getLayoutInflater(),dbHelper);
        try{
            searchEngine = new SearchEngine();
            offlineSearchEngine = new OfflineSearchEngine();
        } catch (InstantiationErrorException e) {
            Log.e(TAG, "Fallo al inicializar el motor de búsqueda: " + e.error.name());
        }
    }

    private void initializeBD(){
        dbHelper = new DatabaseHelper(this);
        rutas = dbHelper.getAllRoutes();
//        rutasActivas = dbHelper.getAllRoutesActive();
        if(rutas.size() == 0){
            /*GeoPolyline geoPolyline = null;
            try {
                //,new GeoCoordinates(21.097774, -101.579798)
                geoPolyline = new GeoPolyline(Arrays.asList(new GeoCoordinates(21.14421,-101.6919),new GeoCoordinates(21.14428,-101.6913),new GeoCoordinates(21.14436,-101.69075),new GeoCoordinates(21.14439,-101.69043),new GeoCoordinates(21.14517,-101.69055),new GeoCoordinates(21.14564,-101.6906),new GeoCoordinates(21.14596,-101.69065),new GeoCoordinates(21.14626,-101.69069),new GeoCoordinates(21.14668,-101.69075),new GeoCoordinates(21.14728,-101.6908),new GeoCoordinates(21.14729,-101.69072),new GeoCoordinates(21.14737,-101.69063),new GeoCoordinates(21.14748,-101.69059),new GeoCoordinates(21.14756,-101.69059),new GeoCoordinates(21.14767,-101.69063),new GeoCoordinates(21.14774,-101.6907),new GeoCoordinates(21.14777,-101.69078),new GeoCoordinates(21.14817,-101.69063),new GeoCoordinates(21.14912,-101.69028),new GeoCoordinates(21.1493,-101.69022),new GeoCoordinates(21.14969,-101.69008),new GeoCoordinates(21.15025,-101.68988),new GeoCoordinates(21.1506,-101.68976),new GeoCoordinates(21.1511,-101.68958),new GeoCoordinates(21.1516,-101.6894),new GeoCoordinates(21.1527,-101.68899),new GeoCoordinates(21.15281,-101.68895),new GeoCoordinates(21.15291,-101.6889),new GeoCoordinates(21.15316,-101.68878),new GeoCoordinates(21.15319,-101.68877),new GeoCoordinates(21.15341,-101.68869),new GeoCoordinates(21.15363,-101.68861),new GeoCoordinates(21.15405,-101.68846),new GeoCoordinates(21.15424,-101.68839),new GeoCoordinates(21.15433,-101.68835),new GeoCoordinates(21.15438,-101.68833),new GeoCoordinates(21.1547,-101.68823),new GeoCoordinates(21.15506,-101.68811),new GeoCoordinates(21.15557,-101.68794),new GeoCoordinates(21.15551,-101.68768),new GeoCoordinates(21.15543,-101.68735),new GeoCoordinates(21.1554,-101.68724),new GeoCoordinates(21.1553,-101.68685),new GeoCoordinates(21.15523,-101.68656),new GeoCoordinates(21.15522,-101.68651),new GeoCoordinates(21.15514,-101.68624),new GeoCoordinates(21.15507,-101.68597),new GeoCoordinates(21.15502,-101.68576),new GeoCoordinates(21.155,-101.68567),new GeoCoordinates(21.15493,-101.68536),new GeoCoordinates(21.15481,-101.68482),new GeoCoordinates(21.15475,-101.68465),new GeoCoordinates(21.15459,-101.68417),new GeoCoordinates(21.15454,-101.68397),new GeoCoordinates(21.15447,-101.68371),new GeoCoordinates(21.15435,-101.68329),new GeoCoordinates(21.15434,-101.68324),new GeoCoordinates(21.15433,-101.68315),new GeoCoordinates(21.15431,-101.68298),new GeoCoordinates(21.15429,-101.68265),new GeoCoordinates(21.15421,-101.68222),new GeoCoordinates(21.15412,-101.68187),new GeoCoordinates(21.15404,-101.68158),new GeoCoordinates(21.15402,-101.68145),new GeoCoordinates(21.15386,-101.68082),new GeoCoordinates(21.15316,-101.67813),new GeoCoordinates(21.15303,-101.67769),new GeoCoordinates(21.15279,-101.67678),new GeoCoordinates(21.15268,-101.67636),new GeoCoordinates(21.15237,-101.67522),new GeoCoordinates(21.15233,-101.67508),new GeoCoordinates(21.15229,-101.67488),new GeoCoordinates(21.15213,-101.67428),new GeoCoordinates(21.15177,-101.6729),new GeoCoordinates(21.15146,-101.6717),new GeoCoordinates(21.15131,-101.67113),new GeoCoordinates(21.15107,-101.6705),new GeoCoordinates(21.15098,-101.67003),new GeoCoordinates(21.15087,-101.66958),new GeoCoordinates(21.15078,-101.66911),new GeoCoordinates(21.15071,-101.66882),new GeoCoordinates(21.15066,-101.66861),new GeoCoordinates(21.15042,-101.66765),new GeoCoordinates(21.15021,-101.66683),new GeoCoordinates(21.1501,-101.6664),new GeoCoordinates(21.14993,-101.66575),new GeoCoordinates(21.14984,-101.66537),new GeoCoordinates(21.14981,-101.66525),new GeoCoordinates(21.1497,-101.66488),new GeoCoordinates(21.14956,-101.66434),new GeoCoordinates(21.14952,-101.66416),new GeoCoordinates(21.14948,-101.66399),new GeoCoordinates(21.14943,-101.66378),new GeoCoordinates(21.14939,-101.66363),new GeoCoordinates(21.14936,-101.66353),new GeoCoordinates(21.14925,-101.66312),new GeoCoordinates(21.14919,-101.66293),new GeoCoordinates(21.14913,-101.66274),new GeoCoordinates(21.1491,-101.66261),new GeoCoordinates(21.149,-101.66222),new GeoCoordinates(21.14885,-101.66167),new GeoCoordinates(21.14873,-101.66115),new GeoCoordinates(21.14867,-101.66091),new GeoCoordinates(21.14858,-101.66056),new GeoCoordinates(21.14834,-101.65966),new GeoCoordinates(21.14815,-101.65899),new GeoCoordinates(21.14797,-101.65827),new GeoCoordinates(21.14782,-101.65774),new GeoCoordinates(21.14771,-101.65736),new GeoCoordinates(21.14767,-101.6572),new GeoCoordinates(21.14761,-101.65696),new GeoCoordinates(21.14754,-101.65667),new GeoCoordinates(21.1475,-101.65653),new GeoCoordinates(21.14745,-101.65635),new GeoCoordinates(21.14736,-101.65602),new GeoCoordinates(21.14735,-101.65595),new GeoCoordinates(21.14734,-101.65586),new GeoCoordinates(21.14731,-101.65574),new GeoCoordinates(21.14715,-101.65511),new GeoCoordinates(21.14714,-101.65506),new GeoCoordinates(21.14703,-101.65466),new GeoCoordinates(21.14693,-101.65427),new GeoCoordinates(21.14688,-101.65406),new GeoCoordinates(21.14665,-101.65321),new GeoCoordinates(21.14661,-101.65305),new GeoCoordinates(21.14659,-101.65296),new GeoCoordinates(21.14653,-101.65279),new GeoCoordinates(21.14642,-101.65236),new GeoCoordinates(21.14636,-101.65211),new GeoCoordinates(21.14633,-101.65199),new GeoCoordinates(21.1463,-101.65188),new GeoCoordinates(21.14627,-101.65174),new GeoCoordinates(21.14619,-101.65173),new GeoCoordinates(21.14612,-101.65169),new GeoCoordinates(21.14608,-101.65166),new GeoCoordinates(21.14605,-101.65162),new GeoCoordinates(21.14602,-101.65153),new GeoCoordinates(21.14601,-101.65149),new GeoCoordinates(21.14601,-101.65141),new GeoCoordinates(21.14602,-101.65134),new GeoCoordinates(21.14604,-101.65127),new GeoCoordinates(21.14607,-101.6512),new GeoCoordinates(21.14612,-101.65115),new GeoCoordinates(21.14597,-101.65058),new GeoCoordinates(21.14587,-101.65021),new GeoCoordinates(21.14583,-101.65005),new GeoCoordinates(21.14574,-101.64972),new GeoCoordinates(21.14545,-101.64872),new GeoCoordinates(21.14537,-101.6484),new GeoCoordinates(21.14525,-101.64796),new GeoCoordinates(21.14515,-101.64757),new GeoCoordinates(21.1451,-101.64736),new GeoCoordinates(21.14487,-101.64646),new GeoCoordinates(21.14484,-101.64635),new GeoCoordinates(21.14481,-101.64623),new GeoCoordinates(21.14469,-101.64574),new GeoCoordinates(21.14461,-101.64542),new GeoCoordinates(21.14456,-101.64525),new GeoCoordinates(21.14451,-101.64506),new GeoCoordinates(21.14446,-101.64486),new GeoCoordinates(21.14443,-101.64471),new GeoCoordinates(21.14431,-101.64425),new GeoCoordinates(21.14423,-101.64394),new GeoCoordinates(21.14421,-101.64386),new GeoCoordinates(21.14375,-101.64207),new GeoCoordinates(21.1436,-101.6415),new GeoCoordinates(21.1434,-101.64072),new GeoCoordinates(21.14333,-101.64045),new GeoCoordinates(21.14329,-101.64029),new GeoCoordinates(21.14313,-101.6397),new GeoCoordinates(21.14301,-101.6392),new GeoCoordinates(21.14295,-101.63894),new GeoCoordinates(21.1429,-101.63873),new GeoCoordinates(21.14261,-101.63773),new GeoCoordinates(21.14256,-101.63753),new GeoCoordinates(21.14245,-101.63706),new GeoCoordinates(21.14236,-101.6367),new GeoCoordinates(21.14226,-101.63631),new GeoCoordinates(21.14211,-101.63576),new GeoCoordinates(21.14208,-101.63566),new GeoCoordinates(21.14207,-101.63562),new GeoCoordinates(21.14205,-101.63554),new GeoCoordinates(21.14195,-101.6351),new GeoCoordinates(21.14188,-101.63484),new GeoCoordinates(21.14179,-101.63452),new GeoCoordinates(21.14171,-101.63424),new GeoCoordinates(21.14166,-101.63406),new GeoCoordinates(21.14153,-101.63363),new GeoCoordinates(21.14146,-101.63338),new GeoCoordinates(21.14134,-101.63296),new GeoCoordinates(21.14133,-101.63289),new GeoCoordinates(21.14131,-101.63279),new GeoCoordinates(21.14127,-101.63267),new GeoCoordinates(21.14125,-101.63259),new GeoCoordinates(21.14124,-101.63254),new GeoCoordinates(21.14121,-101.63247),new GeoCoordinates(21.14113,-101.63225),new GeoCoordinates(21.14107,-101.63211),new GeoCoordinates(21.14097,-101.63192),new GeoCoordinates(21.14071,-101.63149),new GeoCoordinates(21.14046,-101.63108),new GeoCoordinates(21.14027,-101.63076),new GeoCoordinates(21.14013,-101.63053),new GeoCoordinates(21.14012,-101.63051),new GeoCoordinates(21.13962,-101.62963),new GeoCoordinates(21.13954,-101.62948),new GeoCoordinates(21.13911,-101.62872),new GeoCoordinates(21.13902,-101.62854),new GeoCoordinates(21.13893,-101.62828),new GeoCoordinates(21.13885,-101.62799),new GeoCoordinates(21.13883,-101.62778),new GeoCoordinates(21.13882,-101.62749),new GeoCoordinates(21.13883,-101.62715),new GeoCoordinates(21.13885,-101.62683),new GeoCoordinates(21.13889,-101.6266),new GeoCoordinates(21.139,-101.62613),new GeoCoordinates(21.13902,-101.626),new GeoCoordinates(21.13903,-101.62588),new GeoCoordinates(21.13904,-101.62571),new GeoCoordinates(21.13905,-101.62559),new GeoCoordinates(21.13905,-101.62551),new GeoCoordinates(21.13904,-101.62546),new GeoCoordinates(21.13902,-101.62511),new GeoCoordinates(21.13897,-101.62484),new GeoCoordinates(21.13893,-101.62464),new GeoCoordinates(21.13889,-101.62452),new GeoCoordinates(21.13886,-101.6244),new GeoCoordinates(21.13879,-101.6242),new GeoCoordinates(21.13874,-101.62407),new GeoCoordinates(21.13865,-101.62387),new GeoCoordinates(21.1386,-101.62375),new GeoCoordinates(21.13847,-101.62347),new GeoCoordinates(21.13828,-101.62314),new GeoCoordinates(21.13817,-101.62292),new GeoCoordinates(21.1381,-101.62279),new GeoCoordinates(21.13797,-101.62254),new GeoCoordinates(21.13786,-101.62232),new GeoCoordinates(21.13777,-101.62213),new GeoCoordinates(21.13753,-101.62161),new GeoCoordinates(21.13747,-101.62149),new GeoCoordinates(21.13734,-101.62121),new GeoCoordinates(21.13711,-101.62067),new GeoCoordinates(21.13668,-101.61966),new GeoCoordinates(21.13654,-101.6193),new GeoCoordinates(21.13608,-101.61829),new GeoCoordinates(21.13589,-101.61783),new GeoCoordinates(21.13569,-101.61742),new GeoCoordinates(21.13556,-101.61711),new GeoCoordinates(21.13538,-101.61673),new GeoCoordinates(21.13431,-101.61432),new GeoCoordinates(21.13422,-101.61412),new GeoCoordinates(21.13392,-101.61349),new GeoCoordinates(21.13358,-101.61275),new GeoCoordinates(21.13342,-101.6124),new GeoCoordinates(21.13317,-101.61188),new GeoCoordinates(21.13295,-101.61142),new GeoCoordinates(21.13284,-101.6112),new GeoCoordinates(21.1328,-101.61113),new GeoCoordinates(21.13262,-101.6108),new GeoCoordinates(21.1324,-101.61043),new GeoCoordinates(21.13231,-101.61028),new GeoCoordinates(21.13217,-101.6101),new GeoCoordinates(21.132,-101.60977),new GeoCoordinates(21.13185,-101.60945),new GeoCoordinates(21.13169,-101.60912),new GeoCoordinates(21.1316,-101.6089),new GeoCoordinates(21.13155,-101.60872),new GeoCoordinates(21.1315,-101.60856),new GeoCoordinates(21.13144,-101.60839),new GeoCoordinates(21.13138,-101.60823),new GeoCoordinates(21.1311,-101.60744),new GeoCoordinates(21.13103,-101.60723),new GeoCoordinates(21.13091,-101.60669),new GeoCoordinates(21.13088,-101.60658),new GeoCoordinates(21.13085,-101.60645),new GeoCoordinates(21.13073,-101.606),new GeoCoordinates(21.13031,-101.60466),new GeoCoordinates(21.13011,-101.60404),new GeoCoordinates(21.12991,-101.6035),new GeoCoordinates(21.12982,-101.60324),new GeoCoordinates(21.12978,-101.60313),new GeoCoordinates(21.12969,-101.60286),new GeoCoordinates(21.12965,-101.60276),new GeoCoordinates(21.12958,-101.60258),new GeoCoordinates(21.12956,-101.6025),new GeoCoordinates(21.12942,-101.60208),new GeoCoordinates(21.12935,-101.60189),new GeoCoordinates(21.12922,-101.60151),new GeoCoordinates(21.1291,-101.60126),new GeoCoordinates(21.12891,-101.60091),new GeoCoordinates(21.12869,-101.60059),new GeoCoordinates(21.12854,-101.60038),new GeoCoordinates(21.12846,-101.60028),new GeoCoordinates(21.12834,-101.60014),new GeoCoordinates(21.12819,-101.60002),new GeoCoordinates(21.12801,-101.59991),new GeoCoordinates(21.12782,-101.59982),new GeoCoordinates(21.12729,-101.59959),new GeoCoordinates(21.12698,-101.59937),new GeoCoordinates(21.12696,-101.59935),new GeoCoordinates(21.12691,-101.59929),new GeoCoordinates(21.12684,-101.59923),new GeoCoordinates(21.1267,-101.59906),new GeoCoordinates(21.12668,-101.59903),new GeoCoordinates(21.12656,-101.59886),new GeoCoordinates(21.12621,-101.59833),new GeoCoordinates(21.12614,-101.59822),new GeoCoordinates(21.12571,-101.59758),new GeoCoordinates(21.12567,-101.59752),new GeoCoordinates(21.1253,-101.59696),new GeoCoordinates(21.12508,-101.59664),new GeoCoordinates(21.1248,-101.59622),new GeoCoordinates(21.12475,-101.59614),new GeoCoordinates(21.12469,-101.59607),new GeoCoordinates(21.12438,-101.59562),new GeoCoordinates(21.12426,-101.59543),new GeoCoordinates(21.124,-101.59507),new GeoCoordinates(21.12385,-101.59486),new GeoCoordinates(21.12369,-101.59466),new GeoCoordinates(21.12323,-101.59412),new GeoCoordinates(21.12286,-101.59375),new GeoCoordinates(21.12224,-101.59322),new GeoCoordinates(21.12198,-101.59303),new GeoCoordinates(21.12168,-101.59283),new GeoCoordinates(21.12112,-101.5925),new GeoCoordinates(21.12026,-101.59209),new GeoCoordinates(21.11974,-101.5919),new GeoCoordinates(21.1196,-101.59185),new GeoCoordinates(21.11935,-101.59175),new GeoCoordinates(21.11902,-101.59161),new GeoCoordinates(21.11858,-101.59145),new GeoCoordinates(21.11808,-101.59127),new GeoCoordinates(21.11735,-101.59098),new GeoCoordinates(21.11713,-101.5909),new GeoCoordinates(21.11691,-101.59081),new GeoCoordinates(21.11637,-101.59061),new GeoCoordinates(21.11597,-101.59045),new GeoCoordinates(21.11541,-101.59019),new GeoCoordinates(21.11534,-101.59016),new GeoCoordinates(21.11512,-101.59004),new GeoCoordinates(21.11494,-101.58994),new GeoCoordinates(21.11482,-101.58985),new GeoCoordinates(21.11479,-101.58983),new GeoCoordinates(21.11466,-101.58973),new GeoCoordinates(21.11429,-101.58949),new GeoCoordinates(21.11406,-101.58933),new GeoCoordinates(21.11387,-101.58918),new GeoCoordinates(21.11378,-101.58911),new GeoCoordinates(21.11345,-101.58882),new GeoCoordinates(21.1131,-101.58846),new GeoCoordinates(21.11267,-101.58799),new GeoCoordinates(21.11258,-101.58788),new GeoCoordinates(21.11236,-101.5876),new GeoCoordinates(21.11187,-101.58703),new GeoCoordinates(21.11107,-101.58608),new GeoCoordinates(21.11068,-101.58562),new GeoCoordinates(21.11025,-101.58512),new GeoCoordinates(21.11019,-101.58504),new GeoCoordinates(21.10993,-101.58474),new GeoCoordinates(21.10985,-101.58464),new GeoCoordinates(21.10976,-101.58453),new GeoCoordinates(21.10943,-101.58415),new GeoCoordinates(21.10823,-101.58274),new GeoCoordinates(21.10806,-101.58255),new GeoCoordinates(21.10787,-101.58232),new GeoCoordinates(21.10779,-101.58222),new GeoCoordinates(21.10755,-101.58194),new GeoCoordinates(21.10743,-101.58176),new GeoCoordinates(21.10739,-101.58171),new GeoCoordinates(21.10727,-101.58156),new GeoCoordinates(21.10721,-101.58149),new GeoCoordinates(21.10715,-101.58141),new GeoCoordinates(21.10711,-101.58136),new GeoCoordinates(21.10697,-101.5812),new GeoCoordinates(21.10694,-101.58116),new GeoCoordinates(21.10679,-101.58101),new GeoCoordinates(21.10655,-101.58072),new GeoCoordinates(21.10638,-101.5805),new GeoCoordinates(21.10615,-101.58025),new GeoCoordinates(21.10609,-101.58015),new GeoCoordinates(21.10601,-101.58008),new GeoCoordinates(21.10564,-101.57972),new GeoCoordinates(21.10506,-101.57913),new GeoCoordinates(21.10477,-101.57881),new GeoCoordinates(21.10465,-101.57868),new GeoCoordinates(21.10421,-101.57818),new GeoCoordinates(21.10346,-101.57734),new GeoCoordinates(21.10343,-101.57731),new GeoCoordinates(21.10221,-101.57595),new GeoCoordinates(21.10195,-101.57565),new GeoCoordinates(21.10049,-101.57401),new GeoCoordinates(21.10026,-101.57375),new GeoCoordinates(21.10008,-101.57402),new GeoCoordinates(21.10014,-101.574),new GeoCoordinates(21.10019,-101.57425),new GeoCoordinates(21.09927,-101.57466),new GeoCoordinates(21.099114,-101.574729)));
            } catch (InstantiationErrorException e) {
                //throw new RuntimeException(e);
            }
            float widthInPixels = 10;
            MapPolyline trafficSpanMapPolyline = null;
            try {
                trafficSpanMapPolyline = new MapPolyline(geoPolyline, new MapPolyline.SolidRepresentation(
                        new MapMeasureDependentRenderSize(RenderSize.Unit.PIXELS, widthInPixels),
                        new Color(1f, 0f, 0f, 1f),
                        LineCap.ROUND));
            }  catch (MapPolyline.Representation.InstantiationException e) {
                Log.e("MapPolyline Representation Exception:", e.error.name());
            } catch (MapMeasureDependentRenderSize.InstantiationException e) {
                Log.e("MapMeasureDependentRenderSize Exception:", e.error.name());
            }
            int[] truckSpecIds = {1,2,3,4,5,6,7,8,9,10};
            dbHelper.saveRuta("Ruta 1",new GeoCoordinates(21.144573, -101.691865),new GeoCoordinates(21.099128, -101.574836),trafficSpanMapPolyline,truckSpecIds);
            dbHelper.saveRuta("Ruta 2",new GeoCoordinates(21.144573, -101.691865),new GeoCoordinates(21.099128, -101.574836),trafficSpanMapPolyline,truckSpecIds);
            dbHelper.saveRuta("Ruta 3",new GeoCoordinates(21.144573, -101.691865),new GeoCoordinates(21.099128, -101.574836),trafficSpanMapPolyline,truckSpecIds);
            dbHelper.saveRuta("Ruta 4",new GeoCoordinates(21.144573, -101.691865),new GeoCoordinates(21.099128, -101.574836),trafficSpanMapPolyline,truckSpecIds);
            dbHelper.saveRuta("Ruta 5",new GeoCoordinates(21.144573, -101.691865),new GeoCoordinates(21.099128, -101.574836),trafficSpanMapPolyline,truckSpecIds);*/
            // Agrega todas las llamadas a los métodos de descarga
            futures.add(descargarRutas());
            // Espera a que todos los futures terminen
            CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            allOf.whenComplete((result, ex) -> {
                if (ex != null) {
                    // Manejo de errores, si es necesario
                    Log.e(TAG, "Error during downloads", ex);
                }
                // Recupera la lista de polígonos de la base de datos
                rutas = dbHelper.getAllRoutes();
//                rutasActivas = dbHelper.getAllRoutesActive();
            });
        }
    }

    private void initializeComponents(){
        // Inicializar animaciones
        animSalida = AnimationUtils.loadAnimation(this, R.anim.salida2);
        animacionClick = AnimationUtils.loadAnimation(this, R.anim.click);
        cargaAnimacion = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate);
        animEntrada = AnimationUtils.loadAnimation(this, R.anim.entrada2);

        // Inicializar componentes
        hideable_content = findViewById(R.id.hideable_content);
        home_content = findViewById(R.id.home_content);
        mapView = findViewById(R.id.map_view);
        messageView = findViewById(R.id.messageView);
        nmd = findViewById(R.id.nmd);
        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        trackCamara = findViewById(R.id.trackCamara);
        txtNavegacion = findViewById(R.id.txtNavegacion);
        speedTextView = findViewById(R.id.speedTextView);
        imgVelocidad = findViewById(R.id.imgVelocidad);
        btnTerminarRuta = findViewById(R.id.btnTerminarRuta);
        txtTerminarRuta = findViewById(R.id.txtTerminarRuta);
        detallesRuta = findViewById(R.id.detallesRuta);
        distanceTextView = findViewById(R.id.distanceTextView);
        timeTextView = findViewById(R.id.timeTextView);
        llGeocerca = findViewById(R.id.llGeocerca);
        btnGeocercas = findViewById(R.id.btnGeocercas);
        llPois = findViewById(R.id.llPois);
        fbEliminarPoi = findViewById(R.id.fbEliminarPoi);
        llMapas = findViewById(R.id.llMapas);
        fbMapas = findViewById(R.id.fbMapas);
        truck_config_content = findViewById(R.id.truck_config_content);
        likeAnimationView = findViewById(R.id.likeImageView);
        recalculateRouteButton = findViewById(R.id.recalculateRouteButton);

        //Animacion de cargando
        //cohete
        likeAnimator.beginAnimation(likeAnimationView,R.raw.loading_2,R.raw.loading_5);

        // Set the toolbar as the action bar
        this.setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.dialog_ok,R.string.dialog_cancel);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        // Set the navigation view as the content view
        nmd.setNavigationItemSelectedListener(this);

        initializeListeners();
    }
    private void initializeListeners(){
        // Inicializar animaciones
        trackCamara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trackCamara.startAnimation(animacionClick);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isTrackingCamera) {
                            trackCamara.setImageResource(R.drawable.track_on);
                            navigationExample.stopCameraTracking();
                        } else {
                            trackCamara.setImageResource(R.drawable.track_off);
                            navigationExample.startCameraTracking();
                        }
                        isTrackingCamera = !isTrackingCamera;
                    }
                }, 400);
            }
        });
        btnGeocercas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(activarGeocercas){
                    mapView.getMapScene().removeMapPolygon(geocercas.geocercas);
                    activarGeocercas = false;
                    btnGeocercas.setImageResource(R.drawable.ic_add_road);
                }else{
                    mapView.getMapScene().addMapPolygon(geocercas.geocercas);
                    activarGeocercas = true;
                    btnGeocercas.setImageResource(R.drawable.ic_remove_road);
                }
            }
        });
        btnTerminarRuta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnTerminarRuta.startAnimation(animacionClick);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        LayoutInflater inflater = getLayoutInflater();
                        View dialogView = inflater.inflate(R.layout.ventana_terminar_ruta, null);

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setView(dialogView);

                        final AlertDialog dialog = builder.create();

                        Button positiveButton = dialogView.findViewById(R.id.positiveButton);
                        Button negativeButton = dialogView.findViewById(R.id.negativeButton);

                        positiveButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                positiveButton.startAnimation(animacionClick);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        limpiezaTotal();
                                        dialog.dismiss();
                                    }
                                }, 400);
                            }
                        });

                        negativeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                negativeButton.startAnimation(animacionClick);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        messages.showCustomToast("La ruta sigue");
                                        dialog.dismiss();
                                    }
                                }, 400);
                            }
                        });

                        dialog.show();
                        //recalculateRouteButton.setVisibility(View.GONE);
                    }
                }, 400);
            }
        });
        fbEliminarPoi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearMapMarkersPOIsAndCircle(true);
                messages.showCustomToast("Se eliminaron los poi");
                llPois.setVisibility(View.GONE);
            }
        });
        fbMapas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                avoidZonesExample.cleanPolygon();
                controlPointsExample.cleanPoint();
                messages.showCustomToast("Cambiando estilo del mapa");
                String filename="";
                // AUMENTAMOS EL CONTADOR DE ESTILOS
                styleCounter++;
                // VERIFICAMOS EL CONTADOR DE ESTILOS NO SALGA DEL RANGO
                if(styleCounter==7)styleCounter=0;
                // VALIDAMOS POR EL CONTADOR DE ESTILOS
                switch (styleCounter) {
                    case 0:
                        // CAMBIAMOS EL ESTILO
                        style = MapScheme.NORMAL_DAY;
                        break;
                    case 1:
                        // CAMBIAMOS EL ESTILO
                        style = MapScheme.NORMAL_NIGHT;
                        break;
                    case 2:
                        // CAMBIAMOS EL ESTILO A NULO
                        style = null;
                        // TOMAMOS EL NOMBRE DEL ARCHIVO JSON
                        filename = "custom-dark-style-neon-rds.json";
                        break;
                    case 3:
                        // CAMBIAMOS EL ESTILO A NULO
                        style = null;
                        // TOMAMOS EL NOMBRE DEL ARCHIVO JSON
                        filename = "Day.json";
                        break;
                    case 4:
                        // CAMBIAMOS EL ESTILO A NULO
                        style = null;
                        // TOMAMOS EL NOMBRE DEL ARCHIVO JSON
                        filename = "prueba.json";
                        break;
                    case 5:
                        // CAMBIAMOS EL ESTILO
                        style = MapScheme.HYBRID_DAY;
                        break;
                    case 6:
                        // CAMBIAMOS EL ESTILO A NULO
                        style = MapScheme.SATELLITE;
                        break;
                }
                // TOMAMOS LOS ASSETS DEL PROYECTO
                AssetManager assetManager = getApplicationContext().getAssets();
                try {
                    // CARGAMOS EL ARCHIVO JSON
                    assetManager.open(filename);
                } catch (Exception e) {
                    // MANDAMOS UN MENSAJE DE ERROR
                    Log.e("Error", e.getMessage());
                }
                // VERIFICAMOS SI EL ESTYLO ES NULO
                if(style==null){
                    // CARGAMOS EL ESTILO POR DEFECTO
                    mapView.getMapScene().loadScene(""+filename, new MapScene.LoadSceneCallback() {
                        @Override
                        public void onLoadScene(@Nullable MapError errorCode) {
                            if (errorCode == null) {
                            } else {
                                // Style loading failed
                            }
                        }
                    });
                }else{
                    // CARGAMOS ALGUNO DE LOS ESTILOS PREDETERMINADOS DE LA SDK
                    mapView.getMapScene().loadScene(style, new MapScene.LoadSceneCallback() {
                        @Override
                        public void onLoadScene(@Nullable MapError errorCode) {
                            if (errorCode == null) {
                                // VERIFICAMOS SI ESTA ACTIVADO EL TRAFICO
                                /*if(isActiveTraffic) {
                                    // ACTIVAMOS EL TRAFICO
                                    trafficExample.enableAll();
                                }*/
                            } else {
                                // Style loading failed
                            }
                        }
                    });
                }
            }
        });
        recalculateRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recalculateRouteButton.setVisibility(View.GONE);
                recalculateRoute();
            }
        });
    }

    private void restartLocationUpdates() {
        if (navigationExample.herePositioningProvider != null) {
            navigationExample.herePositioningProvider.stopLocating();
        }
        navigationExample.locationAccuracy = isExactRouteEnabled ? LocationAccuracy.NAVIGATION : LocationAccuracy.BEST_AVAILABLE;
        navigationExample.startLocationProvider();
    }

    @Override
    public void onSpeedUpdated(double speed) {
        runOnUiThread(() -> {
            int speedKmh = (int) Math.round(speed * 3.6);
            speedTextView.setText(String.format("%d", speedKmh));
        });
    }

    @Override
    public void onDestinationReached() {
        Animation animSalida = AnimationUtils.loadAnimation(MainActivity.this, R.anim.salida2);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnTerminarRuta.startAnimation(animSalida);
                messageView.startAnimation(animSalida);
                detallesRuta.startAnimation(animSalida);
                speedTextView.startAnimation(animSalida);
                distanceTextView.startAnimation(animSalida);
                txtTerminarRuta.startAnimation(animSalida);
                txtNavegacion.startAnimation(animSalida);

                btnTerminarRuta.setVisibility(View.GONE);
                messageView.setVisibility(View.GONE);
                detallesRuta.setVisibility(View.GONE);
                speedTextView.setVisibility(View.GONE);
                distanceTextView.setVisibility(View.GONE);
                txtTerminarRuta.setVisibility(View.GONE);
                txtNavegacion.setVisibility(View.GONE);
                //recalculateRouteButton.setVisibility(View.GONE);

                trackCamara.setImageResource(R.drawable.track_off);
                navigationExample.stopCameraTracking();
            }
        });
    }

    @Override
    public void onDestinationInfoUpdated(int distanceInMeters, int timeInSeconds) {
        runOnUiThread(() -> {
            String distanceString;
            if (distanceInMeters >= 1000) {
                double distanceInKm = distanceInMeters / 1000.0;
                distanceString = String.format("Distancia: %.1f km", distanceInKm);
            } else {
                distanceString = String.format("Distancia: %d m", distanceInMeters);
            }
            distanceTextView.setText(distanceString);

            int hours = timeInSeconds / 3600;
            int minutes = (timeInSeconds % 3600) / 60;
            int seconds = timeInSeconds % 60;
            String timeString;
            if (hours > 0) {
                timeString = String.format("Tiempo: %d:%02d:%02d", hours, minutes, seconds);
            } else {
                timeString = String.format("Tiempo: %02d:%02d", minutes, seconds);
            }
            timeTextView.setText(timeString);
        });
    }
    public void limpiezaTotal() {
        List<MapMarker> startEndMarkers = new ArrayList<>();
        for (MapMarker marker : routingExample.mapMarkerList) {
            if (marker.getCoordinates().equals(coordenada1) || marker.getCoordinates().equals(coordenada2)) {
                startEndMarkers.add(marker);
            }
        }

        for (MapMarker marker : routingExample.mapMarkerList) {
            if (!startEndMarkers.contains(marker)) {
                mapView.getMapScene().removeMapMarker(marker);
            }
        }
        routingExample.mapMarkerList.clear();
        routingExample.mapMarkerList.addAll(startEndMarkers);

        for (MapPolyline polyline : routingExample.mapPolylines) {
            mapView.getMapScene().removeMapPolyline(polyline);
        }
        routingExample.mapPolylines.clear();

        coordenada1 = null;
        coordenada2 = null;

        navigationExample.stopNavigation(true);
        messages.showCustomToast("Se ha terminado la ruta");
        messageView.setText("Indicaciones");
        detallesRuta.startAnimation(animSalida);
        detallesRuta.setVisibility(View.GONE);
        timeTextView.startAnimation(animSalida);
        timeTextView.setVisibility(View.GONE);
        distanceTextView.setVisibility(View.GONE);
        distanceTextView.startAnimation(animSalida);
        messageView.setVisibility(View.GONE);
        messageView.startAnimation(animSalida);
        timeTextView.setText("");
        distanceTextView.setText("");
        int yOffsetPx = Math.round(10 * getResources().getDisplayMetrics().density);
        mapView.setWatermarkLocation(new Anchor2D(0.5, 1), new Point2D(0, -yOffsetPx));
        navigationExample.stopCameraTracking();
        messageView.setText("Indicaciones");
        btnTerminarRuta.startAnimation(animSalida);
        btnTerminarRuta.setVisibility(View.GONE);
        txtTerminarRuta.startAnimation(animSalida);
        txtTerminarRuta.setVisibility(View.GONE);

        rutaGenerada = false;
        coordenadasDestino = null;
        clearMapPolylines();
        routingExample.clearMap();
        clearMapMarkersPOIsAndCircle(false);
        clearMapPolylines();
        routingExample.clearMap();
        clearMapMarkersPOIsAndCircle(false);
        if(ruta!=null){
            llGeocerca.setVisibility(View.GONE);
            mapView.getMapScene().removeMapPolyline(ruta.polyline);
            ruta=null;
        }
        if(geocercas!=null){
            mapView.getMapScene().removeMapPolygon(geocercas.geocercas);
        }
        if(!geocercas.geocercasControlPoint.isEmpty()){
            for (MapPolygon mapPolygon : geocercas.geocercasControlPoint) {
                mapView.getMapScene().removeMapPolygon(mapPolygon);
            }
        }
        for (int i = 0; i < controlPointsExample.pointsWithIds.size(); i++) {
            for (PointWithId pointWithId : puntos) {
                if(pointWithId.id==controlPointsExample.pointsWithIds.get(i).id){
                    controlPointsExample.pointsWithIds.get(i).visibility=false;
                    controlPointsExample.pointsWithIds.get(i).label=false;
                }
            }
        }
        puntos.clear();
        controlPointsExample.cleanPoint();
        for (int i = 0; i < avoidZonesExample.polygonWithIds.size(); i++) {
            for (PolygonWithId polygonWithId : poligonos) {
                if(polygonWithId.id==avoidZonesExample.polygonWithIds.get(i).id) {
                    avoidZonesExample.polygonWithIds.get(i).visibility = false;
                    avoidZonesExample.polygonWithIds.get(i).label = false;
                }
            }
        }
        poligonos.clear();
        avoidZonesExample.cleanPolygon();
        rutasAsignadas.clear();
    }

    public void clearMapPolylines() {
        for (MapPolyline polyline : routingExample.mapPolylines) {
            mapView.getMapScene().removeMapPolyline(polyline);
        }
        routingExample.mapPolylines.clear();
    }

    public void clearMapMarkersPOIsAndCircle(Boolean dejarPoiActivo) {
        if(dejarPoiActivo){
            // Eliminar los marcadores del mapa
            for (MapMarker marker : new ArrayList<>(mapMarkersPOIs)) { // Iterar sobre una copia de la lista
                if (!marker.getCoordinates().equals(geoCoordinatesPOI)) {
                    mapView.getMapScene().removeMapMarker(marker);
                    mapMarkersPOIs.remove(marker); // Eliminar de la lista
                }
            }
        }else{
            // Eliminar los marcadores del mapa
            for (MapMarker marker : new ArrayList<>(mapMarkersPOIs)) { // Iterar sobre una copia de la lista
                mapView.getMapScene().removeMapMarker(marker);
                mapMarkersPOIs.remove(marker); // Eliminar de la lista
            }
            geoCoordinatesPOI = null;
        }
        // Eliminar todas las vistas de pin y los places correspondientes
        List<MapView.ViewPin> mapViewPins = mapView.getViewPins();
        for (MapView.ViewPin viewPin : new ArrayList<>(mapViewPins)) { // Iterar sobre una copia de la lista
            for (Place place : new ArrayList<>(placesList)) { // Iterar sobre una copia de la lista
                if (geoCoordinatesPOI != null) {
                    if (place.getGeoCoordinates().latitude == viewPin.getGeoCoordinates().latitude && place.getGeoCoordinates().longitude == viewPin.getGeoCoordinates().longitude) {
                        if(viewPin.getGeoCoordinates().latitude == geoCoordinatesPOI.latitude && viewPin.getGeoCoordinates().longitude == geoCoordinatesPOI.longitude){
                        }else{
                            viewPin.unpin();
                            placesList.remove(place); // Eliminar de la lista
                        }
                    }
                }else {
                    if (place.getGeoCoordinates().latitude == viewPin.getGeoCoordinates().latitude &&
                            place.getGeoCoordinates().longitude == viewPin.getGeoCoordinates().longitude) {
                        viewPin.unpin();
                        placesList.remove(place); // Eliminar de la lista
                    }
                }
            }
        }
        if(geocercas.mapPolygon!=null){
            mapView.getMapScene().removeMapPolygon(geocercas.mapPolygon);
        }
        llPois.setVisibility(View.GONE);
    }

    private void setTapGestureHandler() {
        mapView.getGestures().setTapListener(touchPoint -> pickCartoPois(touchPoint));
    }

    private void pickCartoPois(final Point2D touchPoint) {
        Rectangle2D rectangle2D = new Rectangle2D(touchPoint, new Size2D(50, 50));
        ArrayList<MapScene.MapPickFilter.ContentType> contentTypesToPickFrom = new ArrayList<>();
        contentTypesToPickFrom.add(MapScene.MapPickFilter.ContentType.MAP_CONTENT);
        MapScene.MapPickFilter filter = new MapScene.MapPickFilter(contentTypesToPickFrom);
        mapView.pick(filter, rectangle2D, mapPickResult -> {
            if (mapPickResult == null) {
                return;
            }
            PickMapContentResult pickedMapContent = mapPickResult.getMapContent();
            List<PickedPlace> cartoPOIList = pickedMapContent.getPickedPlaces();
            List<PickMapContentResult.TrafficIncidentResult> trafficPOIList = pickedMapContent.getTrafficIncidents();
            List<PickMapContentResult.VehicleRestrictionResult> vehicleRestrictionResultList = pickedMapContent.getVehicleRestrictions();

            if (!cartoPOIList.isEmpty()) {
                PickedPlace pickedPlace = cartoPOIList.get(0);
                Log.d("Carto POI picked: ", pickedPlace.name + ", Place category: " + pickedPlace.placeCategoryId);
                if(offlineMap.isMexicoMapDownload){
                    offlineSearchEngine.searchPickedPlace(pickedPlace, LanguageCode.ES_MX, new PlaceIdSearchCallback() {
                        @Override
                        public void onPlaceIdSearchCompleted(@Nullable SearchError searchError, @Nullable Place place) {
                            if (searchError == null) {
                                String address = place.getAddress().addressText;
                                StringBuilder categoriesBuilder = new StringBuilder();
                                for (PlaceCategory category : place.getDetails().categories) {
                                    String name = category.getName();
                                    if (name != null) {
                                        if (categoriesBuilder.length() > 0) {
                                            categoriesBuilder.append(", ");
                                        }
                                        categoriesBuilder.append(name);
                                    }
                                }
                                messages.showDialog("Información de lugar", address, categoriesBuilder.toString(), "Lugar de interés", null);
                            } else {
                                Log.e(TAG, "searchPickedPlace() resulted in an error: " + searchError.name());
                            }
                        }
                    });
                }else {
                    searchEngine.searchPickedPlace(pickedPlace, LanguageCode.ES_MX, new PlaceIdSearchCallback() {
                        @Override
                        public void onPlaceIdSearchCompleted(@Nullable SearchError searchError, @Nullable Place place) {
                            if (searchError == null) {
                                String address = place.getAddress().addressText;
                                StringBuilder categoriesBuilder = new StringBuilder();
                                for (PlaceCategory category : place.getDetails().categories) {
                                    String name = category.getName();
                                    if (name != null) {
                                        if (categoriesBuilder.length() > 0) {
                                            categoriesBuilder.append(", ");
                                        }
                                        categoriesBuilder.append(name);
                                    }
                                }
                                messages.showDialog("Información de lugar", address, categoriesBuilder.toString(), "Lugar de interés", null);
                            } else {
                                Log.e(TAG, "searchPickedPlace() resulted in an error: " + searchError.name());
                            }
                        }
                    });
                }
            } else if (!trafficPOIList.isEmpty()) {
                PickMapContentResult.TrafficIncidentResult topmostContent = trafficPOIList.get(0);
                messages.showDialog("Incidente de tráfico", "" + topmostContent.getType().name(), "", "Incidente de tráfico",null);
            } else if (!vehicleRestrictionResultList.isEmpty()) {
                PickMapContentResult.VehicleRestrictionResult topmostContent = vehicleRestrictionResultList.get(0);
                String restrictionType = topmostContent.restrictionType != null ? topmostContent.restrictionType : "No especificado";
                messages.showDialog("Restricción de vehículo",
                        ""+ restrictionType,
                        "",
                        "",
                        null);
            }
            else {
                // Establece el radio en metros
                float radiusInPixel = 2;
                // Obtener las coordenadas del punto del toque
                mapView.pickMapItems(touchPoint, radiusInPixel, new MapViewBase.PickMapItemsCallback() {
                    @Override
                    public void onPickMapItems(@Nullable PickMapItemsResult pickMapItemsResult) {
                        try {
                            // Verificar si se ha seleccionado un MapMarker
                            if (pickMapItemsResult == null) {
                                return;
                            }
                            // Obtener el MapMarker seleccionado
                            MapMarker topmostMapMarker = pickMapItemsResult.getMarkers().get(0);
                            // Verificar si el MapMarker es nulo
                            if (topmostMapMarker == null) {
                                return;
                            }
                            // Obtener la metadata del MapMarker
                            int index = mapMarkersPOIs.indexOf(topmostMapMarker);
                            if (index >= 0 && index < placesList.size()) {
                                Place place = placesList.get(index);
                                String address = place.getAddress().addressText;
                                StringBuilder categoriesBuilder = new StringBuilder();
                                for (PlaceCategory category : place.getDetails().categories) {
                                    String name = category.getName();
                                    if (name != null) {
                                        if (categoriesBuilder.length() > 0) {
                                            categoriesBuilder.append(", ");
                                        }
                                        categoriesBuilder.append(name);
                                    }
                                }
                                if(geoCoordinatesPOI == null){
                                    GeoCoordinates geoCoordinates = place.getGeoCoordinates();
                                    messages.showDialog("Información de lugar", address, categoriesBuilder.toString(), "Lugar de interés",geoCoordinates);
                                    return;
                                }
                                if(place.getGeoCoordinates().latitude !=geoCoordinatesPOI.latitude && place.getGeoCoordinates().longitude != geoCoordinatesPOI.longitude){
                                    GeoCoordinates geoCoordinates = place.getGeoCoordinates();
                                    messages.showDialog("Información de lugar", address, categoriesBuilder.toString(), "Lugar de interés",geoCoordinates);
                                    return;
                                }
                                messages.showDialog("Información de lugar", address, categoriesBuilder.toString(), "Lugar de interés",null);
                            }
                        } catch (Exception e) {
                            //Toast.makeText(MainActivity.this, "No POIs o marcadores encontrados en la ubicación tocada", Toast.LENGTH_SHORT).show();
                            //throw new RuntimeException(e);
                        }
                    }
                });
            }
        });
    }
    private void searchNearestPoi(String poiType, int radius) {
        // Configurar la búsqueda de lugares con el SDK de HERE
        // TextQuery textQuery = new TextQuery(poiType, new TextQuery.Area(currentGeoCoordinates));
        // Dibujar un círculo de 5km de radio en el mapa
        geocercas.drawCircle(currentGeoCoordinates, radius);

        // Configurar la búsqueda de lugares con el SDK de HERE
        TextQuery textQuery = new TextQuery(poiType, new TextQuery.Area(new GeoCircle(currentGeoCoordinates,radius)));

        SearchOptions searchOptions = new SearchOptions();
        searchOptions.maxItems = 100;

        if(offlineMap.isMexicoMapDownload){
            offlineSearchEngine.search(textQuery, searchOptions, new SearchCallback() {
                @Override
                public void onSearchCompleted(@Nullable SearchError searchError, @Nullable List<Place> list) {
                    if (searchError == null && list != null && !list.isEmpty()) {
                        for (Place place : list) {
                            // Obtener las coordenadas de cada POI
                            GeoCoordinates poiCoordinates = place.getGeoCoordinates();

                            // Mostrar el POI en el mapa o realizar cualquier acción necesaria
                            showPoiOnMap(poiCoordinates,place);
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "No se encontraron POIs cercanos", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else{
            searchEngine.search(textQuery, searchOptions, new SearchCallback() {
                @Override
                public void onSearchCompleted(@Nullable SearchError searchError, @Nullable List<Place> list) {
                    if (searchError == null && list != null && !list.isEmpty()) {
                        for (Place place : list) {
                            // Obtener las coordenadas de cada POI
                            GeoCoordinates poiCoordinates = place.getGeoCoordinates();

                            // Mostrar el POI en el mapa o realizar cualquier acción necesaria
                            showPoiOnMap(poiCoordinates,place);
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "No se encontraron POIs cercanos", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
    private void showPoiOnMap(GeoCoordinates poiCoordinates,Place place) {
        // Crear un marcador en la ubicación del POI
        MapImage mapImage = MapImageFactory.fromResource(getResources(), R.drawable.ic_poi); // Usa un icono de marcador apropiado
        MapMarker mapMarker = new MapMarker(poiCoordinates, mapImage);

        // Crea un TextView para la etiqueta
        boolean found = false;
        for (Place place1 : placesList){
            if(place1.getGeoCoordinates().latitude==poiCoordinates.latitude && place1.getGeoCoordinates().longitude==poiCoordinates.longitude){
                return;
            }
        }
        placesList.add(place); // Asociar el POI con el marcador
        // Agregar el marcador al mapa
        mapView.getMapScene().addMapMarker(mapMarker);
        mapMarkersPOIs.add(mapMarker);
        TextView textView = new TextView(this);
        textView.setTextColor(android.graphics.Color.parseColor("#03B1AF"));
        textView.setText(place.getTitle());
        textView.setTypeface(Typeface.DEFAULT_BOLD);

        // Crea un LinearLayout para contener el TextView y agregar padding
        LinearLayout linearLayout = new LinearLayout(this);
        //linearLayout.setBackgroundResource(R.color.colorAccent);
        linearLayout.setPadding(0, 0, 0, 130);
        linearLayout.addView(textView);

        // Usar el punto medio para anclar la vista
        mapView.pinView(linearLayout, poiCoordinates);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        mMapHelper.handleAndroidPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        mMapHelper.handleAndroidPermissions();
        //mMapHelper.permisoLocalizacion(this, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        mMapHelper.disposeHERESDK();
        navigationExample.stopNavigation(false);
        stopLocalVoiceInteraction();
        navigationExample.stopLocating();
        navigationExample.stopRendering();
    }

    private CompletableFuture<ResponseBody> descargarRutas() {
        try {
            CompletableFuture<ResponseBody> future = new CompletableFuture<>();
            ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
            apiService.getRutas().enqueue(new retrofit2.Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            // Obtener el JSON como string
                            String jsonResponse = response.body().string();
                            // Convierte la respuesta en un objeto JSON
                            JSONObject jsonObject = new JSONObject(jsonResponse);
                            // Verifica si la operación fue exitosa
                            boolean success = jsonObject.getBoolean("success");
                            if (success) {
                                // Obtén el arreglo "result"
                                JSONArray rutasArray = jsonObject.getJSONArray("result");
                                // Itera sobre cada elemento en el arreglo
                                for (int i = 0; i < rutasArray.length(); i++) {
                                    JSONObject rutaObject = rutasArray.getJSONObject(i);
                                    // Extraer datos del punto
                                    int id = rutaObject.optInt("id_ruta", 0);
                                    String nombre = rutaObject.optString("nombre", "Sin nombre");
                                    double latitud_inicio = rutaObject.optDouble("latitud_inicio", 0.0);
                                    double longitud_inicio = rutaObject.optDouble("longitud_inicio", 0.0);
                                    double latitud_fin = rutaObject.optDouble("latitud_fin", 0.0);
                                    double longitud_fin = rutaObject.optDouble("longitud_fin", 0.0);
                                    String fecha_creacion = rutaObject.optString("fecha_creacion", "");
                                    String fecha_ultima_modificacion = rutaObject.optString("fecha_ultima_modificacion", "");
                                    String polilineaString = rutaObject.optString("polilinea", "");
                                    int status = rutaObject.optInt("estatus", 0);
                                    List<GeoCoordinates> vertices = new ArrayList<>();
                                    String[] vertexPairs = polilineaString.split("\\],\\[");
                                    for (String vertexPair : vertexPairs) {
                                        // Remove extra square brackets
                                        vertexPair = vertexPair.replace("[", "").replace("]", "");
                                        String[] coords = vertexPair.split(",");
                                        try {
                                            double latitude = Double.parseDouble(coords[0].substring(1, coords[0].length() - 1));
                                            double longitude = Double.parseDouble(coords[1].substring(1, coords[1].length() - 1));
                                            vertices.add(new GeoCoordinates(latitude, longitude));
                                        } catch (NumberFormatException e) {
                                            Log.e("Error", "Invalid coordinate format: " + vertexPair);
                                        }
                                    }
                                    GeoPolyline geoPolyline = null;
                                    try {
                                        //,new GeoCoordinates(21.097774, -101.579798)
                                        geoPolyline = new GeoPolyline(vertices);
                                    } catch (InstantiationErrorException e) {
                                        //throw new RuntimeException(e);
                                    }
                                    float widthInPixels = 10;
                                    MapPolyline mapPolyline = null;
                                    try {
                                        mapPolyline = new MapPolyline(geoPolyline, new MapPolyline.SolidRepresentation(
                                                new MapMeasureDependentRenderSize(RenderSize.Unit.PIXELS, widthInPixels),
                                                new Color(1f, 0f, 0f, 1f),
                                                LineCap.ROUND));
                                    }  catch (MapPolyline.Representation.InstantiationException e) {
                                        Log.e("MapPolyline Representation Exception:", e.error.name());
                                    } catch (MapMeasureDependentRenderSize.InstantiationException e) {
                                        Log.e("MapMeasureDependentRenderSize Exception:", e.error.name());
                                    }
                                    JSONArray puntosArray = rutaObject.getJSONArray("puntos_de_control");
                                    // Calcular el tamaño del array de enteros de antemano
                                    int[] puntos = new int[puntosArray.length()];

                                    for (int j = 0; j < puntosArray.length(); j++) {
                                        puntos[j] = puntosArray.getInt(j);
                                    }
                                    JSONArray zonasArray = rutaObject.getJSONArray("zonas");
                                    // Calcular el tamaño del array de enteros de antemano
                                    int[] zonas = new int[zonasArray.length()];
                                    for (int j = 0; j < zonasArray.length(); j++) {
                                        zonas[j] = zonasArray.getInt(j);
                                    }
                                    int[] truckSpecIds = {1,2,3};
                                    // Guardar la zona en la base de datos
                                    try {
                                        dbHelper.saveRuta(
                                                id,
                                                nombre,
                                                new GeoCoordinates(latitud_inicio, longitud_inicio),
                                                new GeoCoordinates(latitud_fin, longitud_fin),
                                                mapPolyline,
                                                truckSpecIds,
                                                puntos,
                                                zonas,
                                                fecha_creacion,
                                                fecha_ultima_modificacion,
                                                status
                                        );
                                    } catch (Exception e) {
                                        Log.e("Database", "Error al guardar el punto: " + e.getMessage());
                                    }
                                }
                            } else {
                                Log.e("Error", "La operación no fue exitosa.");
                            }
                            Log.d("Retrofit", "Puntos guardados correctamente.");
                            future.complete(response.body());
                        } catch (Exception e) {
                            Log.e("Retrofit", "Error al procesar el JSON: " + e.getMessage());
                        }
                    } else {
                        Log.e("Retrofit", "Error en la respuesta del servidor.");
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("Retrofit", "Error al obtener datos: " + t.getMessage());
                    future.completeExceptionally(t);
                }
            });
            return future;
        } catch (Error e) {
            Log.e(TAG, "Error en la solicitud de los puntos de control: ", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    public void recalculateRoute() {
        recalculateRouteButton.setVisibility(View.GONE);
        try {
            if (currentGeoCoordinates != null && destinationGeoCoordinates != null) {
                List<GeoCoordinates> puntos_de_control = new ArrayList<>();
                List<MapPolygon> zonas = new ArrayList<>();
                if(ruta.puntosIds!=null){
                    for (int i = 0; i < controlPointsExample.pointsWithIds.size(); i++) {
                        boolean foundPuntoDeControl = false;
                        for (int id : ruta.puntosIds) {
                            if (id == controlPointsExample.pointsWithIds.get(i).id) {
                                foundPuntoDeControl = true;
                                break;
                            }
                        }
                        if (foundPuntoDeControl) {
                            if (controlPointsExample.pointsWithIds.get(i).status) {
                                controlPointsExample.pointsWithIds.get(i).visibility=true;
                                controlPointsExample.pointsWithIds.get(i).label=true;
                                puntos_de_control.add(controlPointsExample.pointsWithIds.get(i).mapMarker.getCoordinates());
                                puntos.add(controlPointsExample.pointsWithIds.get(i));
                            }
                        }
                    }
                }
                if(ruta.zonasIds!=null){
                    for (int i = 0; i < avoidZonesExample.polygonWithIds.size(); i++) {
                        boolean foundZona = false;
                        for (int id : ruta.zonasIds) {
                            if (id == avoidZonesExample.polygonWithIds.get(i).id) {
                                foundZona = true;
                                break;
                            }
                        }

                        if (foundZona) {
                            if (avoidZonesExample.polygonWithIds.get(i).status) {
                                avoidZonesExample.polygonWithIds.get(i).visibility=true;
                                avoidZonesExample.polygonWithIds.get(i).label=true;
                                if(!avoidZonesExample.polygonWithIds.get(i).peligrosa){
                                    zonas.add(avoidZonesExample.polygonWithIds.get(i).polygon);
                                    poligonos.add(avoidZonesExample.polygonWithIds.get(i));
                                }
                            }
                        }
                    }
                }
                routingExample.addRoute(zonas,puntos_de_control,currentGeoCoordinates, destinationGeoCoordinates, null,ruta.coordinatesInicio, new RoutingExample.RouteCallback() {
                    @Override
                    public void onRouteCalculated(Route route) {
                        if (route != null) {
                            runOnUiThread(() -> {
                                try {
                                    navigationExample.startNavigation(route, false, true);
                                    recalculateRouteButton.setVisibility(View.GONE);
                                } catch (Exception e) {
                                    Log.e("MainActivity", "Error starting navigation: ", e);
                                }
                            });
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(MainActivity.this, "No se pudo recalcular la ruta", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                });
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error recalculating route: ", e);
            Toast.makeText(this, "Error al recalcular la ruta", Toast.LENGTH_SHORT).show();
        }
    }

    /*private CompletableFuture<ResponseBody> obtenerDetallesRuta() {
        try {
            CompletableFuture<ResponseBody> future = new CompletableFuture<>();
            ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
            apiService.getRuta(ruta.id).enqueue(new retrofit2.Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            // Obtener el JSON como string
                            String jsonResponse = response.body().string();
                            // Convierte la respuesta en un objeto JSON
                            JSONObject jsonObject = new JSONObject(jsonResponse);
                            // Verifica si la operación fue exitosa
                            boolean success = jsonObject.getBoolean("success");
                            if (success) {
                                // Obtén el arreglo "result"
                                JSONObject resultArray = jsonObject.getJSONObject("result");
                                // Obtén el arreglo "puntos_de_control"
                                JSONArray puntosArray = resultArray.getJSONArray("puntos_de_control");
                                for (int i = 0; i < puntosArray.length(); i++) {
                                    JSONObject puntoDetalleObject = puntosArray.getJSONObject(i);
                                    JSONObject puntoObject = puntoDetalleObject.getJSONObject("puntoDeControlDetalle");
                                    // Extraer datos del punto
                                    for (int j = 0; j < controlPointsExample.pointsWithIds.size(); j++) {
                                        if(controlPointsExample.pointsWithIds.get(j).id == puntoObject.getInt("id_punto_de_control")){
                                            puntos.add(controlPointsExample.pointsWithIds.get(j));
                                        }
                                    }
                                }
                                // Obtén el arreglo "zonas"
                                JSONArray zonasArray = resultArray.getJSONArray("zonas");
                                for (int i = 0; i < zonasArray.length(); i++) {
                                    JSONObject zonaDetalleObject = zonasArray.getJSONObject(i);
                                    JSONObject zonaObject = zonaDetalleObject.getJSONObject("zonaDetalle");
                                    // Extraer datos del punto
                                    for (int j = 0; j < avoidZonesExample.polygonWithIds.size(); j++) {
                                        if (avoidZonesExample.polygonWithIds.get(j).id == zonaObject.getInt("id_zona")) {
                                            poligonos.add(avoidZonesExample.polygonWithIds.get(j));
                                        }
                                    }
                                }
                            } else {
                                Log.e("Error", "La operación no fue exitosa.");
                            }
                            Log.d("Retrofit", "Detalles guardados localmente.");
                            future.complete(response.body());
                        } catch (Exception e) {
                            Log.e("Retrofit", "Error al procesar el JSON: " + e.getMessage());
                        }
                    } else {
                        Log.e("Retrofit", "Error en la respuesta del servidor.");
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("Retrofit", "Error al obtener datos: " + t.getMessage());
                    future.completeExceptionally(t);
                }
            });
            return future;
        } catch (Error e) {
            Log.e(TAG, "Error en la solicitud de los puntos de control: ", e);
            return CompletableFuture.failedFuture(e);
        }
    }*/

    private CompletableFuture<ResponseBody> obtenerAsignaciones() {
        try {
            CompletableFuture<ResponseBody> future = new CompletableFuture<>();
            ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
            apiService.getAsignaciones().enqueue(new retrofit2.Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            // Obtener el JSON como string
                            String jsonResponse = response.body().string();
                            // Convierte la respuesta en un objeto JSON
                            JSONObject jsonObject = new JSONObject(jsonResponse);
                            // Verifica si la operación fue exitosa
                            boolean success = jsonObject.getBoolean("success");
                            if (success) {
                                // Obtén el arreglo "result"
                                JSONArray resultArray = jsonObject.getJSONArray("result");
                                for (int i = 0; i < resultArray.length(); i++) {
                                    JSONObject asignacionObject = resultArray.getJSONObject(i);
                                    // Extraer rutas asignadas
                                    for (int j = 0; j < rutas.size(); j++) {
                                        if (rutas.get(j).id == asignacionObject.getInt("id_ruta")) {
                                            rutasAsignadas.add(rutas.get(j));
                                        }
                                    }
                                }
                            } else {
                                Log.e("Error", "La operación no fue exitosa.");
                            }
                            Log.d("Retrofit", "Detalles guardados localmente.");
                            future.complete(response.body());
                        } catch (Exception e) {
                            Log.e("Retrofit", "Error al procesar el JSON: " + e.getMessage());
                        }
                    } else {
                        Log.e("Retrofit", "Error en la respuesta del servidor.");
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("Retrofit", "Error al obtener datos: " + t.getMessage());
                    future.completeExceptionally(t);
                }
            });
            return future;
        } catch (Error e) {
            Log.e(TAG, "Error en la solicitud de los puntos de control: ", e);
            return CompletableFuture.failedFuture(e);
        }
    }

}