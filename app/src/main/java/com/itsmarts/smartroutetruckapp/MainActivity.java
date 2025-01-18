package com.itsmarts.smartroutetruckapp;

import static android.view.View.VISIBLE;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
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
import com.itsmarts.smartroutetruckapp.activitys.InicioSesionActivity;
import com.itsmarts.smartroutetruckapp.adaptadores.RouterAsignedAdapter;
import com.itsmarts.smartroutetruckapp.api.ApiService;
import com.itsmarts.smartroutetruckapp.api.RetrofitClient;
import com.itsmarts.smartroutetruckapp.bd.DatabaseHelper;
import com.itsmarts.smartroutetruckapp.clases.AnimatorNew;
import com.itsmarts.smartroutetruckapp.clases.AvoidZonesExample;
import com.itsmarts.smartroutetruckapp.clases.ControlPointsExample;
import com.itsmarts.smartroutetruckapp.clases.NavigationEventHandler;
import com.itsmarts.smartroutetruckapp.clases.NavigationExample;
import com.itsmarts.smartroutetruckapp.clases.NetworkUtil;
import com.itsmarts.smartroutetruckapp.clases.OfflineMap;
import com.itsmarts.smartroutetruckapp.clases.RoutingExample;
import com.itsmarts.smartroutetruckapp.clases.TruckConfig;
import com.itsmarts.smartroutetruckapp.clases.mapHelper;
import com.itsmarts.smartroutetruckapp.fragments.ErrorDialogFragment;
import com.itsmarts.smartroutetruckapp.fragments.ModalBottomSheetFullScreenFragmentPuntos;
import com.itsmarts.smartroutetruckapp.fragments.ModalBottomSheetFullScreenFragmentZonas;
import com.itsmarts.smartroutetruckapp.helpers.Geocercas;
import com.itsmarts.smartroutetruckapp.helpers.Internet;
import com.itsmarts.smartroutetruckapp.helpers.Messages;
import com.itsmarts.smartroutetruckapp.modelos.PointWithId;
import com.itsmarts.smartroutetruckapp.modelos.PolygonWithId;
import com.itsmarts.smartroutetruckapp.modelos.RoutesWithId;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
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
    public TextView messageView, txtNavegacion, speedTextView, txtTerminarRuta, distanceTextView, timeTextView, txtDescargaInfo, txtProcesoActualizacion, speedLabeltextView;
    public GeoCoordinates currentGeoCoordinates, coordenadasDestino, coordenada1, coordenada2, geoCoordinatesPOI = null, destinationGeoCoordinates;
    public AvoidZonesExample avoidZonesExample;
    public ControlPointsExample controlPointsExample;
    public List<RoutesWithId> rutas = new ArrayList<>(), rutasAsignadas = new ArrayList<>();
    public Animation rotateAnimation, cargaAnimacion, animSalida, animacionClick, animEntrada;
    public boolean animacionEjecutada = false, isFirstClick = true, isMenuOpen = false, rutaGenerada = false, isTrackingCamera = false, isExactRouteEnabled = false, isSimularRutaVisible = false, isRutaVisible = false, isDialogShowing = false, routeSuccessfullyProcessed = false, activarGeocercas = true, mapOfflineMexDownload = false, llegoAlDestino = false;
    public RoutesWithId ruta,rutaPre;
    public ImageButton trackCamara, btnTerminarRuta;
    public ImageView imgVelocidad;
    public View detallesRuta;
    public LinearLayout llGeocerca, llPois, llMapas, llLoadingRoute;
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
    public TextView txtProcesoDescarga, txtDescargaTitulo, nav_header_name, nav_header_email, routeTextView;
    public FloatingActionButton fbEliminarPoi, fbMapas, btnGeocercas;
    public int styleCounter=0;
    // INICIALIZACION DE LA VARIABLE TIPO MapScheme PARA EL ESTILO DEL MAPA POR DEFECTO
    private MapScheme style = MapScheme.NORMAL_DAY;
    public TruckConfig truckConfig;
    public LottieAnimationView likeImageView, likeImageView1;
    public AnimatorNew likeAnimator;
    List<CompletableFuture<ResponseBody>> futures = new ArrayList<>();
    private static final String TAG = "MainActivity";
    public List<PolygonWithId> poligonos = new ArrayList<>();
    public List<PointWithId> puntos = new ArrayList<>();
    public ProgressBar loading_spinner;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    public Menu menu_options = null;
    public MenuItem offlineMapItem = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
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
                if(mMapHelper.isGPSEnabled(MainActivity.this)){
                    navigationExample.startLocationProvider();
                }else{
                    mMapHelper.showGPSDisabledDialog(MainActivity.this);
                }
            }else{
                //mMapHelper.permisoLocalizacion(this, this);
                mMapHelper.handleAndroidPermissions();
            }
        }catch (Exception e){
            Messages.showErrorDetail(MainActivity.this, e);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        try{
            String title = ""+item.getTitle();
            Animation animSalida = null;
            View dialogView = null;
            AlertDialog.Builder builder = null;
            switch (title) {
                case "Obtener Ruta":
                    if(ruta==null){
                        // Inicializar UI
                        rutasAsignadas = new ArrayList<>();
                        dialogView = getLayoutInflater().inflate(R.layout.ventana_seleccionar_ruta, null);
                        builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setView(dialogView);
                        final AlertDialog alertDialogRuta = builder.create();

                        // Configurar vistas
                        TextView textView10 = dialogView.findViewById(R.id.textView10);
                        final Button btnCancelarRuta = dialogView.findViewById(R.id.btnCancelar);
                        LinearLayout linearLayout = dialogView.findViewById(R.id.linearLayout);
                        RecyclerView recyclerView = dialogView.findViewById(R.id.routesRecyclerView);
                        TextView sinRutasTextView = dialogView.findViewById(R.id.sinRutasTextView);
                        ScrollView scrollView = dialogView.findViewById(R.id.scrollView);

                        // Configurar estado inicial
                        scrollView.setVisibility(View.GONE);
                        sinRutasTextView.setText("Cargando rutas...");
                        sinRutasTextView.setVisibility(View.VISIBLE);

                        // Configurar botón cancelar
                        btnCancelarRuta.setOnClickListener(v -> {
                            btnCancelarRuta.startAnimation(animacionClick);
                            handler.postDelayed(alertDialogRuta::dismiss, 400);
                        });

                        // Mostrar diálogo
                        alertDialogRuta.show();

                        if(Internet.isNetworkConnected()){
                            // Iniciar descargas
                            futures = new ArrayList<>();
                            futures.add(descargarRutasFaltantes());
                            futures.add(controlPointsExample.descargarPuntosDeControlFaltantes());
                            futures.add(avoidZonesExample.descargarZonasPeligrosasFaltantes());
                            futures.add(avoidZonesExample.descargarZonasProhibidasFaltantes());

                            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                                    .whenComplete((result, ex) -> {
                                        runOnUiThread(() -> {
                                            if (ex != null) {
                                                Log.e(TAG, "Error during downloads", ex);
                                                sinRutasTextView.setText("Error al cargar rutas");
                                                return;
                                            }
                                            rutas = dbHelper.getAllRoutes();
                                            controlPointsExample.pointsWithIds = dbHelper.getAllPuntos();
                                            avoidZonesExample.polygonWithIds = dbHelper.getAllZonas();

                                            // Iniciar descargas
                                            futures = new ArrayList<>();
                                            futures.add(obtenerAsignaciones());

                                            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                                                    .whenComplete((result1, ex1) -> {
                                                        runOnUiThread(() -> {
                                                            if (ex1 != null) {
                                                                Log.e(TAG, "Error during downloads", ex1);
                                                                sinRutasTextView.setText("Error al cargar rutas");
                                                                return;
                                                            }

                                                            adapterAsignedRoutes = new RouterAsignedAdapter(this, alertDialogRuta, rutasAsignadas);

                                                            if (adapterAsignedRoutes.getItemCount() == 0) {
                                                                scrollView.setVisibility(View.GONE);
                                                                sinRutasTextView.setText("No hay rutas disponibles");
                                                                sinRutasTextView.setVisibility(View.VISIBLE);
                                                            } else {
                                                                recyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
                                                                recyclerView.setAdapter(adapterAsignedRoutes);
                                                                scrollView.setVisibility(View.VISIBLE);
                                                                sinRutasTextView.setVisibility(View.GONE);
                                                            }
                                                        });
                                                    });
                                        });
                                    });
                        }else{
                            //sinRutasTextView.setText("Verifique su conexion a internet.");
                            List<Integer> asignaciones = dbHelper.getAllAsignaciones();
                            for (int i = 0; i < asignaciones.size(); i++) {
                                // Extraer rutas asignadas
                                for (int j = 0; j < rutas.size(); j++) {
                                    if (rutas.get(j).id == asignaciones.get(i)) {
                                        rutasAsignadas.add(rutas.get(j));
                                    }
                                }
                            }
                            adapterAsignedRoutes = new RouterAsignedAdapter(this, alertDialogRuta, rutasAsignadas);
                            if (adapterAsignedRoutes.getItemCount() == 0) {
                                scrollView.setVisibility(View.GONE);
                                sinRutasTextView.setText("No hay rutas disponibles");
                                sinRutasTextView.setVisibility(View.VISIBLE);
                            } else {
                                recyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
                                recyclerView.setAdapter(adapterAsignedRoutes);
                                scrollView.setVisibility(View.VISIBLE);
                                sinRutasTextView.setVisibility(View.GONE);
                            }
                        }
                    }else{
                        Messages.showInvalidCredentialsDialog("Ya se tiene una ruta activa.","Es necesario terminar la ruta activa antes de seleccionar otra ruta.",MainActivity.this);
                    }
                    break;
                case "Puntos Cercanos":
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
                case "Cerrar Sesion":
                    if(Internet.isNetworkConnected()){
                        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                        String token = sharedPreferences.getString("token", "");
                        // Use Retrofit to make the POST request
                        ApiService apiService = RetrofitClient.getInstance(token).create(ApiService.class);
                        Call<ResponseBody> call = apiService.desloguearse();

                        call.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    try {
                                        // Obtener el JSON como string
                                        String jsonResponse = response.body().string();
                                        // Convierte la respuesta en un objeto JSON
                                        JSONObject jsonObject = new JSONObject(jsonResponse);
                                        // Verifica si la operación fue exitosa
                                        boolean success = jsonObject.getBoolean("success");
                                        JSONObject resultObject = null;
                                        if (success) {
                                            /*// Remove credentials from SharedPreferences
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.remove("token");  // Remove username key
                                            editor.remove("id_usuario");  // Remove password key (if stored directly)
                                            editor.remove("nombres");  // Remove password key (if stored directly)
                                            editor.remove("apellido_paterno");  // Remove password key (if stored directly)
                                            editor.remove("apellido_materno");  // Remove password key (if stored directly)
                                            editor.remove("telefono");  // Remove password key (if stored directly)
                                            editor.remove("id_rol");  // Remove password key (if stored directly)
                                            editor.apply(); // Apply changes to SharedPreferences*/

                                            // - Redirect to login activity
                                            Intent intent = new Intent(MainActivity.this, InicioSesionActivity.class); // Assuming your login activity is LoginActivity
                                            startActivity(intent);
                                            // Obtén el objeto "result"
                                            Toast.makeText(getApplicationContext(), "Usuario deslogueado con exito", Toast.LENGTH_SHORT).show();
                                            //finish();
                                        }
                                    } catch (Exception e) {
                                        Log.e("Retrofit", "Error al procesar el JSON: " + e.getMessage());
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), "No se pudo desloguear al usuario", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                                Log.e("Retrofit", "Error en la solicitud: " + t.getMessage());
                            }
                        });
                    }else{
                        DialogFragment errorDialog = new ErrorDialogFragment();
                        errorDialog.show(getSupportFragmentManager(), "errorDialog");
                    }
                    break;
                default:
                    String msg = item.getTitle().toString();
                    Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
                    break;
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }catch (Exception e){
            Messages.showErrorDetail(MainActivity.this, e);
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try{
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_bottom_nav, menu);
            menu_options = menu;
            // Find the "Mapa offline" menu item
            for (int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(i);
                if (item.getTitle().toString().equals("Mapa offline")) {
                    offlineMapItem = item;
                    break;
                }
            }
            offlineMap = new OfflineMap(this);
            return super.onCreateOptionsMenu(menu);
        }catch (Exception e){
            Messages.showErrorDetail(MainActivity.this, e);
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        try{
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
                    if(mapOfflineMexDownload){
                        if(item.isChecked()){
                            item.setChecked(false);
                            offlineMap.onSwitchOnlineButtonClicked();
                            routingExample.routingInterface = routingExample.onlineRoutingEngine;
                        }else{
                            item.setChecked(true);
                            offlineMap.onSwitchOfflineButtonClicked();
                            routingExample.routingInterface = routingExample.offlineRoutingEngine;
                        }
                        // Code to handle refresh selection
                        Toast.makeText(this, name+" selected", Toast.LENGTH_SHORT).show();
                    }else{
                        // Code to handle refresh selection
                        Toast.makeText(this, "Imposible cambiar debido a que aun no tienes descargado el mapa de Mexico", Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    // Code to handle other menu items
                    Toast.makeText(this, name+" selected", Toast.LENGTH_SHORT).show();
                    break;
            }

            // If the ID doesn't match any handled items, return false to allow system handling
            return super.onOptionsItemSelected(item);
        }catch (Exception e){
            Messages.showErrorDetail(MainActivity.this, e);
            return false;
        }
    }

    private void initializeFirstsClass(){
        try{
            mMapHelper = new mapHelper(this);
            messages = new Messages(this);
            geocercas = new Geocercas(this);
            likeAnimator = new AnimatorNew();
        }catch (Exception e){
            Messages.showErrorDetail(MainActivity.this, e);
        }
    }
    private void initializeSecondClass(){
        try{
            truckConfig = new TruckConfig(this);
            routingExample = new RoutingExample(this);
            navigationExample = new NavigationExample(this);
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
        }catch (Exception e){
            Messages.showErrorDetail(MainActivity.this, e);
        }
    }

    private void initializeBD(){
        try{
            dbHelper = new DatabaseHelper(this);
            rutas = dbHelper.getAllRoutes();
            if(rutas.size() == 0){
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
                });
            }/*else{
            // Agrega todas las llamadas a los métodos de descarga
            futures.add(descargarRutasFaltantes());
            // Espera a que todos los futures terminen
            CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            allOf.whenComplete((result, ex) -> {
                if (ex != null) {
                    // Manejo de errores, si es necesario
                    Log.e(TAG, "Error during downloads", ex);
                }
                // Recupera la lista de polígonos de la base de datos
                rutas = dbHelper.getAllRoutes();
            });
        }*/
        }catch (Exception e){
            Messages.showErrorDetail(MainActivity.this, e);
        }
    }

    private void initializeComponents(){
        try{
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
            likeImageView = findViewById(R.id.likeImageView);
            recalculateRouteButton = findViewById(R.id.recalculateRouteButton);
            loading_spinner = findViewById(R.id.loading_spinner);
            llLoadingRoute = findViewById(R.id.llLoadingRoute);
            routeTextView = findViewById(R.id.routeTextView);
            speedLabeltextView = findViewById(R.id.speedLabeltextView);
            // Infla el layout del encabezado
            View headerView = nmd.inflateHeaderView(R.layout.nav_header);
            nav_header_name = headerView.findViewById(R.id.nav_header_name);
            nav_header_email = headerView.findViewById(R.id.nav_header_email);
            //likeImageView1 = findViewById(R.id.likeImageView1);

            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            String nombres = sharedPreferences.getString("nombres", "").trim();
            String correo = sharedPreferences.getString("correo", "");

            nav_header_name.setText(String.format("¡Bienvenido, %s!", nombres));
            nav_header_email.setText(String.format("%s", correo));

            //Animacion de cargando
            likeAnimator.beginAnimation(likeImageView,R.raw.loading_2,R.raw.loading_5);
            //likeAnimator.beginAnimation(likeImageView1,R.raw.loading_2,R.raw.loading_5);

            // Set the toolbar as the action bar
            this.setSupportActionBar(toolbar);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.dialog_ok,R.string.dialog_cancel);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
            // Set the navigation view as the content view
            nmd.setNavigationItemSelectedListener(this);

            initializeListeners();
        }catch (Exception e){
            Messages.showErrorDetail(MainActivity.this, e);
        }
    }
    private void initializeListeners(){
        try{
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
        }catch (Exception e){
            Messages.showErrorDetail(MainActivity.this, e);
        }
    }

    private void restartLocationUpdates() {
        try{
            if (navigationExample.herePositioningProvider != null) {
                navigationExample.herePositioningProvider.stopLocating();
            }
            navigationExample.locationAccuracy = isExactRouteEnabled ? LocationAccuracy.NAVIGATION : LocationAccuracy.BEST_AVAILABLE;
            navigationExample.startLocationProvider();
        }catch (Exception e){
            Messages.showErrorDetail(MainActivity.this, e);
        }
    }

    @Override
    public void onSpeedUpdated(double speed) {
        try{
            runOnUiThread(() -> {
                int speedKmh = (int) Math.round(speed * 3.6);
                speedTextView.setText(String.format("%d", speedKmh));
            });
        }catch (Exception e){
            Messages.showErrorDetail(MainActivity.this, e);
        }
    }

    @Override
    public void onDestinationReached() {
        try{
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
        }catch (Exception e){
            Messages.showErrorDetail(MainActivity.this, e);
        }
    }

    @Override
    public void onDestinationInfoUpdated(int distanceInMeters, int timeInSeconds) {
        try{
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
        }catch (Exception e){
            Messages.showErrorDetail(MainActivity.this, e);
        }
    }
    public void limpiezaTotal() {
        try{
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
            if(!llegoAlDestino){
                messages.showCustomToast("Se ha terminado la ruta");
            }else{
                llegoAlDestino = false;
            }
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
            if(geocercas.geocercas!=null){
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
            recalculateRouteButton.setVisibility(View.GONE);
            navigationExample.getNavigationEventHandler().puntos_completados = new ArrayList<Integer>();
            navigationExample.getNavigationEventHandler().id_punto_control = 0;
            trackCamara.setVisibility(View.GONE);
            txtNavegacion.setVisibility(View.GONE);
            for (PointWithId pointWithId : controlPointsExample.pointsWithIds) {
                for (int id : navigationExample.getNavigationEventHandler().puntos_completados) {
                    if(id==pointWithId.id){
                        MapImage mapImage = MapImageFactory.fromResource(getApplicationContext().getResources(), R.drawable.punto_control);
                        MapMarker mapMarker = new MapMarker(new GeoCoordinates(pointWithId.mapMarker.getCoordinates().latitude, pointWithId.mapMarker.getCoordinates().longitude), mapImage);
                        pointWithId.mapMarker = mapMarker;
                    }
                }
            }
        }catch (Exception e){
            Messages.showErrorDetail(MainActivity.this, e);
        }
    }

    public void clearMapPolylines() {
        try{
            for (MapPolyline polyline : routingExample.mapPolylines) {
                mapView.getMapScene().removeMapPolyline(polyline);
            }
            routingExample.mapPolylines.clear();
        }catch (Exception e){
            Messages.showErrorDetail(MainActivity.this, e);
        }
    }

    public void clearMapMarkersPOIsAndCircle(Boolean dejarPoiActivo) {
        try{
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
            routeTextView.setVisibility(View.GONE);
        }catch (Exception e){
            Messages.showErrorDetail(MainActivity.this, e);
        }
    }

    private void setTapGestureHandler() {
        try{
            mapView.getGestures().setTapListener(touchPoint -> pickCartoPois(touchPoint));
        }catch (Exception e){
            Messages.showErrorDetail(MainActivity.this, e);
        }
    }

    private void pickCartoPois(final Point2D touchPoint) {
        try{
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
                    if(offlineMapItem.isChecked()){
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
        }catch (Exception e){
            Messages.showErrorDetail(MainActivity.this, e);
        }
    }
    private void searchNearestPoi(String poiType, int radius) {
        try{
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
        }catch (Exception e){
            Messages.showErrorDetail(MainActivity.this, e);
        }
    }
    private void showPoiOnMap(GeoCoordinates poiCoordinates,Place place) {
        try{
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
        }catch (Exception e){
            Messages.showErrorDetail(MainActivity.this, e);
        }
    }

    @Override
    protected void onPause() {
        try {
            super.onPause();
            mapView.onPause();
            mMapHelper.handleAndroidPermissions();
        }catch (Exception e){
            Messages.showErrorDetail(MainActivity.this, e);
        }
    }

    @Override
    protected void onResume() {
        try {
            super.onResume();
            mapView.onResume();
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if(mMapHelper.isGPSEnabled(MainActivity.this)){
                    navigationExample.startLocationProvider();
                }else{
                    mMapHelper.showGPSDisabledDialog(MainActivity.this);
                }
            }else{
                //mMapHelper.permisoLocalizacion(this, this);
                mMapHelper.handleAndroidPermissions();
            }
        }catch (Exception e){
            Messages.showErrorDetail(MainActivity.this, e);
        }
    }

    @Override
    protected void onDestroy() {
        try {
            //super.onDestroy();
            mapView.onDestroy();
            mMapHelper.disposeHERESDK();
            navigationExample.stopNavigation(false);
            stopLocalVoiceInteraction();
            navigationExample.stopLocating();
            navigationExample.stopRendering();
            /*// Remove credentials from SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("token");  // Remove username key
            editor.remove("id_usuario");  // Remove password key (if stored directly)
            editor.remove("nombres");  // Remove password key (if stored directly)
            editor.remove("apellido_paterno");  // Remove password key (if stored directly)
            editor.remove("apellido_materno");  // Remove password key (if stored directly)
            editor.remove("telefono");  // Remove password key (if stored directly)
            editor.remove("id_rol");  // Remove password key (if stored directly)
            editor.apply(); // Apply changes to SharedPreferences*/
        }catch (Exception e){
            Messages.showErrorDetail(MainActivity.this, e);
        }
    }

    private CompletableFuture<ResponseBody> descargarRutas() {
        try {
            CompletableFuture<ResponseBody> future = new CompletableFuture<>();
            ApiService apiService = RetrofitClient.getInstance(null).create(ApiService.class);
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
                                    String direccion_inicio = rutaObject.optString("direccion_inicio", "Sin direccion");
                                    double latitud_inicio = rutaObject.optDouble("latitud_inicio", 0.0);
                                    double longitud_inicio = rutaObject.optDouble("longitud_inicio", 0.0);
                                    String direccion_fin = rutaObject.optString("direccion_fin", "Sin direccion");
                                    double latitud_fin = rutaObject.optDouble("latitud_fin", 0.0);
                                    double longitud_fin = rutaObject.optDouble("longitud_fin", 0.0);
                                    String polilineaString = rutaObject.optString("polilinea", "");
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
                                    int distancia = rutaObject.optInt("distancia", 0);
                                    int tiempo = rutaObject.optInt("tiempo", 0);
                                    String fecha_creacion = rutaObject.optString("fecha_hora_creacion", "");
                                    Date fechaCreacion = null;
                                    if (fecha_creacion != null && fecha_creacion != "null") {
                                        try {
                                            fechaCreacion = dateFormat.parse(fecha_creacion);

                                            Calendar calendar = Calendar.getInstance();
                                            calendar.setTime(fechaCreacion);
                                            calendar.add(Calendar.HOUR, -6);

                                            // Obtener la cadena formateada directamente
                                            fecha_creacion = dateFormat.format(calendar.getTime()).toString();
                                        } catch (ParseException e) {
                                            Log.e(TAG,"Error al parsear fecha de creacion: "+fecha_creacion);
                                        }
                                    }
                                    String fecha_ultima_modificacion = rutaObject.optString("fecha_hora_ultima_modificacion", "");
                                    Date fechaUltimaModificacion = null;
                                    if (fecha_ultima_modificacion != null && fecha_ultima_modificacion != "null") {
                                        try {
                                            fechaUltimaModificacion = dateFormat.parse(fecha_ultima_modificacion);

                                            Calendar calendar = Calendar.getInstance();
                                            calendar.setTime(fechaUltimaModificacion);
                                            calendar.add(Calendar.HOUR, -6);

                                            // Obtener la cadena formateada directamente
                                            fecha_ultima_modificacion = dateFormat.format(calendar.getTime()).toString();
                                        } catch (ParseException e) {
                                            Log.e(TAG,"Error al parsear fecha de su ultima actualizacion: "+fecha_ultima_modificacion);
                                        }
                                    }
                                    int status = rutaObject.optInt("estatus", 0);
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
                                    //int[] truckSpecIds = {1,2,3};
                                    JSONArray vehiculosArray = rutaObject.getJSONArray("vehiculos");
                                    // Calcular el tamaño del array de enteros de antemano
                                    int[] truckSpecIds = new int[vehiculosArray.length()];
                                    for (int j = 0; j < vehiculosArray.length(); j++) {
                                        truckSpecIds[j] = vehiculosArray.getInt(j);
                                    }
                                    boolean orden_automatico = rutaObject.optBoolean("orden_automatico", true);
                                    // Guardar la zona en la base de datos
                                    try {
                                        dbHelper.saveRuta(
                                                id,
                                                nombre,
                                                direccion_inicio,
                                                new GeoCoordinates(latitud_inicio, longitud_inicio),
                                                direccion_fin,
                                                new GeoCoordinates(latitud_fin, longitud_fin),
                                                mapPolyline,
                                                distancia,
                                                tiempo,
                                                fecha_creacion,
                                                fecha_ultima_modificacion,
                                                truckSpecIds,
                                                puntos,
                                                zonas,
                                                orden_automatico,
                                                status
                                        );
                                    } catch (Exception e) {
                                        Log.e("Database", "Error al guardar la ruta "+id+" : " + e.getMessage());
                                    }
                                }
                            } else {
                                Log.e("Error", "La operación no fue exitosa.");
                            }
                            Log.d("Retrofit", "Rutas guardadas correctamente.");
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
            Log.e(TAG, "Error en la solicitud de las rutas: ", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    private CompletableFuture<ResponseBody> descargarRutasFaltantes() {
        try {
            CompletableFuture<ResponseBody> future = new CompletableFuture<>();
            ApiService apiService = RetrofitClient.getInstance(null).create(ApiService.class);
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
                                    RoutesWithId ruta_previa = null;
                                    try {
                                         ruta_previa = dbHelper.getRouteById(id);
                                    }catch (Exception e){
                                        Log.e(TAG,"Ruta no encontrada en BD");
                                    }
                                    String fecha_ultima_modificacion = rutaObject.optString("fecha_hora_ultima_modificacion", "");
                                    Date fechaUltimaModificacion = null;
                                    if (fecha_ultima_modificacion != null && fecha_ultima_modificacion != "null") {
                                        try {
                                            fechaUltimaModificacion = dateFormat.parse(fecha_ultima_modificacion);

                                            Calendar calendar = Calendar.getInstance();
                                            calendar.setTime(fechaUltimaModificacion);
                                            calendar.add(Calendar.HOUR, -6);

                                            // Obtener la cadena formateada directamente
                                            fecha_ultima_modificacion = dateFormat.format(calendar.getTime()).toString();
                                            fechaUltimaModificacion = dateFormat.parse(fecha_ultima_modificacion);
                                        } catch (ParseException e) {
                                            Log.e(TAG,"Error al parsear fecha de su ultima actualizacion: "+fecha_ultima_modificacion);
                                        }
                                    }
                                    if(ruta_previa != null){
                                        if(ruta_previa.fecha_ultima_modificacion == null){
                                            if(fechaUltimaModificacion == null){
                                                continue;
                                            }
                                        }
                                        else if(ruta_previa.fecha_ultima_modificacion.toString().equalsIgnoreCase(fechaUltimaModificacion.toString())){
                                            continue;
                                        }else{
                                            dbHelper.deleteRoute(id);
                                        }
                                    }
                                    String nombre = rutaObject.optString("nombre", "Sin nombre");
                                    String direccion_inicio = rutaObject.optString("direccion_inicio", "Sin direccion");
                                    double latitud_inicio = rutaObject.optDouble("latitud_inicio", 0.0);
                                    double longitud_inicio = rutaObject.optDouble("longitud_inicio", 0.0);
                                    String direccion_fin = rutaObject.optString("direccion_fin", "Sin direccion");
                                    double latitud_fin = rutaObject.optDouble("latitud_fin", 0.0);
                                    double longitud_fin = rutaObject.optDouble("longitud_fin", 0.0);
                                    String polilineaString = rutaObject.optString("polilinea", "");
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
                                    int distancia = rutaObject.optInt("distancia", 0);
                                    int tiempo = rutaObject.optInt("tiempo", 0);
                                    String fecha_creacion = rutaObject.optString("fecha_hora_creacion", "");
                                    Date fechaCreacion = null;
                                    if (fecha_creacion != null && fecha_creacion != "null") {
                                        try {
                                            fechaCreacion = dateFormat.parse(fecha_creacion);

                                            Calendar calendar = Calendar.getInstance();
                                            calendar.setTime(fechaCreacion);
                                            calendar.add(Calendar.HOUR, -6);

                                            // Obtener la cadena formateada directamente
                                            fecha_creacion = dateFormat.format(calendar.getTime()).toString();
                                        } catch (ParseException e) {
                                            Log.e(TAG,"Error al parsear fecha de creacion: "+fecha_creacion);
                                        }
                                    }
                                    int status = rutaObject.optInt("estatus", 0);
                                    JSONArray puntosArray = rutaObject.getJSONArray("puntos_de_control");
                                    // Calcular el tamaño del array de enteros de antemano
                                    int[] puntos = new int[puntosArray.length()];

                                    for (int j = 0; j < puntosArray.length(); j++) {
                                        puntos[j] = puntosArray.getInt(j);
                                        PointWithId punto_previo = dbHelper.getPuntoById(puntos[j]);
                                        if(punto_previo == null){
                                            dbHelper.deletePunto(puntos[j]);
                                        }
                                    }
                                    JSONArray zonasArray = rutaObject.getJSONArray("zonas");
                                    // Calcular el tamaño del array de enteros de antemano
                                    int[] zonas = new int[zonasArray.length()];
                                    for (int j = 0; j < zonasArray.length(); j++) {
                                        zonas[j] = zonasArray.getInt(j);
                                        PolygonWithId zona_previo = dbHelper.getZonaById(zonas[j]);
                                        if(zona_previo == null){
                                            dbHelper.deleteZona(zonas[j]);
                                        }
                                    }
                                    //int[] truckSpecIds = {1,2,3};
                                    JSONArray vehiculosArray = rutaObject.getJSONArray("vehiculos");
                                    // Calcular el tamaño del array de enteros de antemano
                                    int[] truckSpecIds = new int[vehiculosArray.length()];
                                    for (int j = 0; j < vehiculosArray.length(); j++) {
                                        truckSpecIds[j] = vehiculosArray.getInt(j);
                                    }
                                    boolean orden_automatico = rutaObject.optBoolean("orden_automatico", true);
                                    // Guardar la zona en la base de datos
                                    try {
                                        dbHelper.saveRuta(
                                                id,
                                                nombre,
                                                direccion_inicio,
                                                new GeoCoordinates(latitud_inicio, longitud_inicio),
                                                direccion_fin,
                                                new GeoCoordinates(latitud_fin, longitud_fin),
                                                mapPolyline,
                                                distancia,
                                                tiempo,
                                                fecha_creacion,
                                                fecha_ultima_modificacion,
                                                truckSpecIds,
                                                puntos,
                                                zonas,
                                                orden_automatico,
                                                status
                                        );
                                    } catch (Exception e) {
                                        Log.e("Database", "Error al guardar la ruta: " + e.getMessage());
                                    }
                                }
                            } else {
                                Log.e("Error", "La operación no fue exitosa.");
                            }
                            Log.d("Retrofit", "Rutas actualizadas correctamente.");
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
            Log.e(TAG, "Error en la solicitud de las rutas faltantes: ", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    public void recalculateRoute() {
        try {
            recalculateRouteButton.setVisibility(View.GONE);
            if (currentGeoCoordinates != null && destinationGeoCoordinates != null) {
                List<GeoCoordinates> puntos_de_control = new ArrayList<>();
                List<MapPolygon> zonas = new ArrayList<>();
                int id_vehiculo = 3;
                if(ruta.truckSpectIds!=null){
                    for (int i = 0; i < ruta.truckSpectIds.length; i++) {
                        if(ruta.truckSpectIds[i] > id_vehiculo){
                            id_vehiculo = ruta.truckSpectIds[i];
                        }
                    }
                }
                if(ruta.puntosIds!=null){
                    for (int id : ruta.puntosIds) {
                        for (int i = 0; i < controlPointsExample.pointsWithIds.size(); i++) {
                            if (id == controlPointsExample.pointsWithIds.get(i).id) {
                                if (controlPointsExample.pointsWithIds.get(i).status) {
                                    for (int id_completado : navigationExample.getNavigationEventHandler().puntos_completados){
                                        if (controlPointsExample.pointsWithIds.get(i).id != id_completado) {
                                            controlPointsExample.pointsWithIds.get(i).visibility=true;
                                            controlPointsExample.pointsWithIds.get(i).label=true;
                                            puntos_de_control.add(controlPointsExample.pointsWithIds.get(i).mapMarker.getCoordinates());
                                            puntos.add(controlPointsExample.pointsWithIds.get(i));
                                            geocercas.drawGecocercaControlPoint(controlPointsExample.pointsWithIds.get(i).mapMarker.getCoordinates(), 100);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
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
                GeoCoordinates coordenadasIniciales = null;
                if(navigationExample.getNavigationEventHandler().puntos_completados.size()==0){
                    coordenadasIniciales = ruta.coordinatesInicio;
                }
                ruta.orden_automatico = false;
                routingExample.addRoute(zonas,puntos_de_control,currentGeoCoordinates, ruta.coordinatesFin, null,coordenadasIniciales, id_vehiculo,ruta.orden_automatico, new RoutingExample.RouteCallback() {
                    @Override
                    public void onRouteCalculated(Route route) {
                        if (route != null) {
                            runOnUiThread(() -> {
                                try {
                                    navigationExample.startNavigation(route, false, false);
                                    recalculateRouteButton.setVisibility(View.GONE);
                                    if(isTrackingCamera){
                                        navigationExample.startCameraTracking();
                                    }
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
            Messages.showErrorDetail(MainActivity.this, e);
        }
    }

    private CompletableFuture<ResponseBody> obtenerAsignaciones() {
        try {
            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            int id_usuario = sharedPreferences.getInt("id_usuario", 0);
            CompletableFuture<ResponseBody> future = new CompletableFuture<>();
            ApiService apiService = RetrofitClient.getInstance(null).create(ApiService.class);
            apiService.getAsignaciones(id_usuario).enqueue(new retrofit2.Callback<ResponseBody>() {
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
                                dbHelper.truncateAsignaciones();
                                // Obtén el arreglo "result"
                                JSONArray resultArray = jsonObject.getJSONArray("result");
                                for (int i = 0; i < resultArray.length(); i++) {
                                    JSONObject asignacionObject = resultArray.getJSONObject(i);
                                    int id_ruta = asignacionObject.getInt("id_ruta");
                                    dbHelper.saveAsignacion(id_ruta);
                                    // Extraer rutas asignadas
                                    for (int j = 0; j < rutas.size(); j++) {
                                        if (rutas.get(j).id == id_ruta) {
                                            rutasAsignadas.add(rutas.get(j));
                                        }
                                    }
                                }
                            } else {
                                Log.e("Error", "La operación no fue exitosa.");
                            }
                            Log.d("Retrofit", "Asignaciones obtenidas exitosamente.");
                            future.complete(response.body());
                        } catch (Exception e) {
                            Log.e("Retrofit", "Error al procesar el JSON: " + e.getMessage());
                            DialogFragment errorDialog = new ErrorDialogFragment();
                            errorDialog.show(getSupportFragmentManager(), "errorDialog");
                        }
                    } else {
                        Log.e("Retrofit", "Error en la respuesta del servidor.");
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    DialogFragment errorDialog = new ErrorDialogFragment();
                    errorDialog.show(getSupportFragmentManager(), "errorDialog");
                    future.completeExceptionally(t);
                }
            });
            return future;
        } catch (Error e) {
            Log.e(TAG, "Error en la solicitud de las asignaciones: ", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public void onBackPressed() {
        try{
            //super.onBackPressed();
            // Muestra un mensaje o realiza otra acción si no se permite regresar
            Toast.makeText(this, "Para regresar a la pantalla de inicio debes cerrar sesion", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Messages.showErrorDetail(MainActivity.this, e);
        }
    }
}