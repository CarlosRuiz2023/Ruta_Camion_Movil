package com.itsmarts.smartroutetruckapp.bd;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.here.sdk.core.Color;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.GeoPolygon;
import com.here.sdk.core.GeoPolyline;
import com.here.sdk.core.errors.InstantiationErrorException;
import com.here.sdk.mapview.LineCap;
import com.here.sdk.mapview.MapImage;
import com.here.sdk.mapview.MapImageFactory;
import com.here.sdk.mapview.MapMarker;
import com.here.sdk.mapview.MapMeasureDependentRenderSize;
import com.here.sdk.mapview.MapPolygon;
import com.here.sdk.mapview.MapPolyline;
import com.here.sdk.mapview.RenderSize;
import com.itsmarts.smartroutetruckapp.R;
import com.itsmarts.smartroutetruckapp.modelos.PointWithId;
import com.itsmarts.smartroutetruckapp.modelos.PolygonWithId;
import com.itsmarts.smartroutetruckapp.modelos.RoutesWithId;
import com.itsmarts.smartroutetruckapp.modelos.TruckSpec;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    private static final String DATABASE_NAME = "smartRouteTruckApp.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_PUNTOS = "puntos";
    private static final String TABLE_ZONAS = "zonas";
    private static final String TABLE_CAMIONES = "camiones";
    private static final String TABLE_ROUTES = "rutas";
    private static final String TABLE_ASIGNACIONES = "asignaciones";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_VERTICES = "vertices";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";
    private static final String COLUMN_LABEL = "label";
    private static final String COLUMN_VISIBILITY = "visibility";
    private static final String COLUMN_STATUS = "status";
    private static final String COLUMN_TONELADA = "tonelada";
    private static final String COLUMN_ALTURA = "altura";
    private static final String COLUMN_ANCHO = "ancho";
    private static final String COLUMN_LARGO = "largo";
    private static final String COLUMN_IMAGEN = "imagen";
    private static final String COLUMN_IDESTADO = "id_estado";
    private static final String COLUMN_IDMUNICIPIO = "id_municipio";
    private static final String COLUMN_ESTADO = "estado";
    private static final String COLUMN_MUNICIPIO = "municipio";
    private static final String COLUMN_TUCKSPEC_IDS = "truckSpecIds";
    private static final String COLUMN_ROUTE_ID = "routeId";
    private static final String COLUMN_PUNTOS_IDS = "puntosIds";
    private static final String COLUMN_ZONAS_IDS = "zonasIds";
    private static final String COLUMN_LATITUDE_INICIO = "latitudeInicio";
    private static final String COLUMN_LONGITUDE_INICIO = "longitudeInicio";
    private static final String COLUMN_LATITUDE_FIN = "latitudeFin";
    private static final String COLUMN_LONGITUDE_FIN = "longitudeFin";
    private static final String COLUMN_FECHA_CREACION = "fechaCreacion";
    private static final String COLUMN_FECHA_ULTIMA_MODIFICACION = "fechaUltimaModificacion";
    private static final String COLUMN_POLIGOLINE = "poligoline";
    private static final String COLUMN_PELIGROSA = "peligrosa";
    private static final String COLUMN_DIRECCION_INICIO = "direccion_inicio";
    private static final String COLUMN_DIRECCION_FIN = "direccion_fin";
    private static final String COLUMN_DISTANCIA = "distancia";
    private static final String COLUMN_TIEMPO = "tiempo";
    private static final String COLUMN_ORDEN_AUTOMATICO = "orden_automatico";

    private static Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQueryPuntos = "CREATE TABLE " + TABLE_PUNTOS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_LATITUDE + " REAL, " +
                COLUMN_LONGITUDE + " REAL, " +
                COLUMN_IDESTADO + " INTEGER, " +
                COLUMN_IDMUNICIPIO + " INTEGER, " +
                COLUMN_TUCKSPEC_IDS + " TEXT, " +
                COLUMN_LABEL + " INTEGER, " +
                COLUMN_VISIBILITY + " INTEGER, " +
                COLUMN_STATUS + " INTEGER)";

        String createTableQueryZonas = "CREATE TABLE " + TABLE_ZONAS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_VERTICES + " TEXT, " +
                COLUMN_IDESTADO + " INTEGER, " +
                COLUMN_IDMUNICIPIO + " INTEGER, " +
                COLUMN_TUCKSPEC_IDS + " TEXT, " +
                COLUMN_LABEL + " INTEGER, " +
                COLUMN_VISIBILITY + " INTEGER, " +
                COLUMN_PELIGROSA + " INTEGER, " +
                COLUMN_STATUS + " INTEGER)";

        String createTableQueryTrucks = "CREATE TABLE " + TABLE_CAMIONES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_TONELADA + " INTEGER, " +
                COLUMN_ALTURA + " INTEGER, " +
                COLUMN_ANCHO + " INTEGER, " +
                COLUMN_LARGO + " INTEGER, " +
                COLUMN_IMAGEN + " TEXT)";

        String createTableQueryRoutes = "CREATE TABLE " + TABLE_ROUTES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_DIRECCION_INICIO + " TEXT, " +
                COLUMN_LATITUDE_INICIO + " REAL, " +
                COLUMN_LONGITUDE_INICIO + " REAL, " +
                COLUMN_DIRECCION_FIN + " TEXT, " +
                COLUMN_LATITUDE_FIN + " REAL, " +
                COLUMN_LONGITUDE_FIN + " REAL, " +
                COLUMN_POLIGOLINE + " TEXT, " +
                COLUMN_DISTANCIA + " INTEGER, " +
                COLUMN_TIEMPO + " INTEGER, " +
                COLUMN_FECHA_CREACION + " TEXT, " +
                COLUMN_FECHA_ULTIMA_MODIFICACION + " TEXT, " +
                COLUMN_TUCKSPEC_IDS + " TEXT, " +
                COLUMN_PUNTOS_IDS + " TEXT, " +
                COLUMN_ZONAS_IDS + " TEXT, " +
                COLUMN_ORDEN_AUTOMATICO + " INTEGER, " +
                COLUMN_STATUS + " INTEGER)";

        String createTableQueryAsignaciones = "CREATE TABLE " + TABLE_ASIGNACIONES + " (" +
                COLUMN_ROUTE_ID + " INTEGER)";

        db.execSQL(createTableQueryPuntos);
        db.execSQL(createTableQueryZonas);
        db.execSQL(createTableQueryTrucks);
        db.execSQL(createTableQueryRoutes);
        db.execSQL(createTableQueryAsignaciones);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Maneja actualizaciones de la base de datos si es necesario
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PUNTOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ZONAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CAMIONES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROUTES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ASIGNACIONES);
        onCreate(db);
    }

    /*
     * Descripcion: FUNCIONES PARA LA BASE DE DATOS DE PUNTOS
     * Autor: CHARLY
     * Ultima fecha de actualizacion: 24.09.2024
     */

    // Guarda una coordenada en la base de datos
    public void savePunto(int id,GeoCoordinates coordinate, String name, int id_estado, int id_municipio,int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, id);
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_LATITUDE, coordinate.latitude);
        values.put(COLUMN_LONGITUDE, coordinate.longitude);
        values.put(COLUMN_IDESTADO, id_estado);
        values.put(COLUMN_IDMUNICIPIO, id_municipio);
        values.put(COLUMN_TUCKSPEC_IDS, "1");
        values.put(COLUMN_LABEL, 0);
        values.put(COLUMN_VISIBILITY, 0);
        values.put(COLUMN_STATUS, status);
        db.insert(TABLE_PUNTOS, null, values);
        db.close();
    }

    // Guarda una coordenada en la base de datos
    public void updatePunto(int id,GeoCoordinates coordinate, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_LATITUDE, coordinate.latitude);
        values.put(COLUMN_LONGITUDE, coordinate.longitude);
        values.put(COLUMN_ESTADO, "Jalisco");
        values.put(COLUMN_MUNICIPIO, "Guadalajara");
        db.update(TABLE_PUNTOS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Recupera todas las coordenadas de la base de datos
    public List<PointWithId> getAllPuntos() {
        List<PointWithId> pointsWithIds = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_PUNTOS;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                double latitude = cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE));
                double longitude = cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE));
                int id_estado = cursor.getInt(cursor.getColumnIndex(COLUMN_IDESTADO));
                int id_municipio = cursor.getInt(cursor.getColumnIndex(COLUMN_IDMUNICIPIO));
                String truckSpecIds = cursor.getString(cursor.getColumnIndex(COLUMN_TUCKSPEC_IDS));
                String[] truckSpecIdsArray = truckSpecIds.split(",");
                int[] ids = new int[truckSpecIdsArray.length];
                for (int i = 0; i < truckSpecIdsArray.length; i++) {
                    ids[i] = Integer.parseInt(truckSpecIdsArray[i].trim());
                }
                Boolean label = cursor.getInt(cursor.getColumnIndex(COLUMN_LABEL)) == 1;
                Boolean visibility = cursor.getInt(cursor.getColumnIndex(COLUMN_VISIBILITY)) == 1;
                Boolean status = cursor.getInt(cursor.getColumnIndex(COLUMN_STATUS)) == 1;
                MapImage mapImage = MapImageFactory.fromResource(context.getResources(), R.drawable.punto_control);
                MapMarker mapMarker = new MapMarker(new GeoCoordinates(latitude, longitude), mapImage);
                pointsWithIds.add(new PointWithId(id,mapMarker,name,id_estado,id_municipio,ids,label,visibility,status));
            } while (cursor.moveToNext());
        }

        cursor.close();
         db.close();
        return pointsWithIds;
    }

    public PointWithId getPuntoById(int idPunto) {
        PointWithId punto = null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            String selectQuery = "SELECT * FROM " + TABLE_PUNTOS + " WHERE " + COLUMN_ID + " = ?";
            Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(idPunto)});
            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                double latitude = cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE));
                double longitude = cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE));
                int id_estado = cursor.getInt(cursor.getColumnIndex(COLUMN_IDESTADO));
                int id_municipio = cursor.getInt(cursor.getColumnIndex(COLUMN_IDMUNICIPIO));
                String truckSpecIds = cursor.getString(cursor.getColumnIndex(COLUMN_TUCKSPEC_IDS));
                String[] truckSpecIdsArray = truckSpecIds.split(",");
                int[] ids = new int[truckSpecIdsArray.length];
                for (int i = 0; i < truckSpecIdsArray.length; i++) {
                    ids[i] = Integer.parseInt(truckSpecIdsArray[i].trim());
                }
                Boolean label = cursor.getInt(cursor.getColumnIndex(COLUMN_LABEL)) == 1;
                Boolean visibility = cursor.getInt(cursor.getColumnIndex(COLUMN_VISIBILITY)) == 1;
                Boolean status = cursor.getInt(cursor.getColumnIndex(COLUMN_STATUS)) == 1;
                MapImage mapImage = MapImageFactory.fromResource(context.getResources(), R.drawable.punto_control);
                MapMarker mapMarker = new MapMarker(new GeoCoordinates(latitude, longitude), mapImage);
                punto = new PointWithId(id,mapMarker,name,id_estado,id_municipio,ids,label,visibility,status);
            }
            cursor.close();
            db.close();
        }catch (Exception e){
            Log.e(TAG,e.getMessage());
        }
        return punto;
    }

    // Actualizar el estatus del poligono de la base de datos por su ID
    public void updateVisibilityPunto(int id, Boolean visibility) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_VISIBILITY, visibility ? 1 : 0);
        db.update(TABLE_PUNTOS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});

        db.close();
    }

    // Actualizar el estatus del poligono de la base de datos por su ID
    public void updateLabelPunto(int id, Boolean label) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LABEL, label ? 1 : 0);
        db.update(TABLE_PUNTOS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});

        db.close();
    }

    // Elimina una coordenada de la base de datos por su ID
    public void deletePunto(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PUNTOS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    /*
     * Descripcion: FUNCIONES PARA LA BASE DE DATOS DE ZONAS
     * Autor: CHARLY
     * Ultima fecha de actualizacion: 24.09.2024
     */

    // Guarda un polígono en la base de datos
    public void saveZona(int id,MapPolygon polygon, String name, int id_estado, int id_municipio, boolean peligrosa, int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, id);
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_VERTICES, serializePolygon(polygon));
        values.put(COLUMN_IDESTADO, id_estado);
        values.put(COLUMN_IDMUNICIPIO, id_municipio);
        values.put(COLUMN_TUCKSPEC_IDS, "1");
        values.put(COLUMN_LABEL, 0);
        values.put(COLUMN_VISIBILITY, 0);
        values.put(COLUMN_PELIGROSA, peligrosa ? 1 : 0);
        values.put(COLUMN_STATUS, status);
        db.insert(TABLE_ZONAS, null, values);
        db.close();
    }

    // Recupera todos los polígonos de la base de datos
    public List<PolygonWithId> getAllZonas() {
        List<PolygonWithId> polygons = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_ZONAS;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                String verticesString = cursor.getString(cursor.getColumnIndex(COLUMN_VERTICES));
                String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                int id_estado = cursor.getInt(cursor.getColumnIndex(COLUMN_IDESTADO));
                int id_municipio = cursor.getInt(cursor.getColumnIndex(COLUMN_IDMUNICIPIO));
                String truckSpecIds = cursor.getString(cursor.getColumnIndex(COLUMN_TUCKSPEC_IDS));
                String[] truckSpecIdsArray = truckSpecIds.split(",");
                int[] ids = new int[truckSpecIdsArray.length];
                for (int i = 0; i < truckSpecIdsArray.length; i++) {
                    ids[i] = Integer.parseInt(truckSpecIdsArray[i].trim());
                }
                Boolean label = cursor.getInt(cursor.getColumnIndex(COLUMN_LABEL)) == 1;
                Boolean visibility = cursor.getInt(cursor.getColumnIndex(COLUMN_VISIBILITY)) == 1;
                Boolean peligrosa = cursor.getInt(cursor.getColumnIndex(COLUMN_PELIGROSA)) == 1;
                Boolean status = cursor.getInt(cursor.getColumnIndex(COLUMN_STATUS)) == 1;
                List<GeoCoordinates> vertices = deserializePolygon(verticesString);
                GeoPolygon geometry = null;
                try {
                    geometry = new GeoPolygon(vertices);
                } catch (InstantiationErrorException e) {
                    //throw new RuntimeException(e);
                }
                Color fillColor = null;
                Color strokeColor = null;
                float strokeWidthInPixels = 5.0f;
                if(peligrosa){
                    fillColor = Color.valueOf(0.5f, 0.5f, 0.5f, 0.63f);  // RGBA
                    strokeColor = Color.valueOf(1f, 0f, 0f, 0.63f); // Orange
                }else{
                    fillColor = Color.valueOf(1f, 0f, 0f, 0.63f);  // RGBA
                    strokeColor = Color.valueOf(0.5f, 0.5f, 0.5f, 0.63f); // Orange
                }
                MapPolygon polygon = new MapPolygon(geometry,fillColor);
                polygon.setOutlineColor(strokeColor);
                polygon.setOutlineWidth(strokeWidthInPixels);
                polygons.add(new PolygonWithId(id,polygon,name,id_estado,id_municipio,ids,label,visibility,peligrosa,status));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return polygons;
    }

    public PolygonWithId getZonaById(int idZona) {
        PolygonWithId zona = null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            String selectQuery = "SELECT * FROM " + TABLE_ZONAS + " WHERE " + COLUMN_ID + " = ?";
            Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(idZona)});
            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                String verticesString = cursor.getString(cursor.getColumnIndex(COLUMN_VERTICES));
                String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                int id_estado = cursor.getInt(cursor.getColumnIndex(COLUMN_IDESTADO));
                int id_municipio = cursor.getInt(cursor.getColumnIndex(COLUMN_IDMUNICIPIO));
                String truckSpecIds = cursor.getString(cursor.getColumnIndex(COLUMN_TUCKSPEC_IDS));
                String[] truckSpecIdsArray = truckSpecIds.split(",");
                int[] ids = new int[truckSpecIdsArray.length];
                for (int i = 0; i < truckSpecIdsArray.length; i++) {
                    ids[i] = Integer.parseInt(truckSpecIdsArray[i].trim());
                }
                Boolean label = cursor.getInt(cursor.getColumnIndex(COLUMN_LABEL)) == 1;
                Boolean visibility = cursor.getInt(cursor.getColumnIndex(COLUMN_VISIBILITY)) == 1;
                Boolean peligrosa = cursor.getInt(cursor.getColumnIndex(COLUMN_PELIGROSA)) == 1;
                Boolean status = cursor.getInt(cursor.getColumnIndex(COLUMN_STATUS)) == 1;
                List<GeoCoordinates> vertices = deserializePolygon(verticesString);
                GeoPolygon geometry = null;
                try {
                    geometry = new GeoPolygon(vertices);
                } catch (InstantiationErrorException e) {
                    //throw new RuntimeException(e);
                }
                Color fillColor = null;
                if(peligrosa){
                    fillColor = Color.valueOf(0.5f, 0.5f, 0.5f, 0.63f);  // RGBA
                }else{
                    fillColor = Color.valueOf(1f, 0f, 0f, 0.63f);  // RGBA
                }
                MapPolygon polygon = new MapPolygon(geometry,fillColor);
                zona = new PolygonWithId(id,polygon,name,id_estado,id_municipio,ids,label,visibility,peligrosa,status);
            }
            cursor.close();
            db.close();
        }catch (Exception e){
            Log.e(TAG,e.getMessage());
        }
        return zona;
    }

    // Serializa un MapPolygon en una cadena de texto
    private String serializePolygon(MapPolygon polygon) {
        List<GeoCoordinates> vertices = polygon.getGeometry().vertices;
        StringBuilder verticesString = new StringBuilder();
        for (GeoCoordinates vertex : vertices) {
            verticesString.append(vertex.latitude).append(",").append(vertex.longitude).append(";");
        }
        return verticesString.toString();
    }
    // Serializa un MapPolygon en una cadena de texto
    private String serializePolyline(MapPolyline polyline) {
        List<GeoCoordinates> vertices = polyline.getGeometry().vertices;
        StringBuilder verticesString = new StringBuilder();
        for (GeoCoordinates vertex : vertices) {
            verticesString.append(vertex.latitude).append(",").append(vertex.longitude).append(";");
        }
        return verticesString.toString();
    }

    // Deserializa un polígono desde una cadena de texto
    private List<GeoCoordinates> deserializePolygon(String verticesString) {
        List<GeoCoordinates> vertices = new ArrayList<>();
        String[] vertexStrings = verticesString.split(";");
        for (String vertexString : vertexStrings) {
            String[] coords = vertexString.split(",");
            if (coords.length == 2) {
                double latitude = Double.parseDouble(coords[0]);
                double longitude = Double.parseDouble(coords[1]);
                vertices.add(new GeoCoordinates(latitude, longitude));
            }
        }
        return vertices;
    }

    // Guarda o actualiza un polígono en la base de datos
    public void updateZona(int id, MapPolygon polygon, String name){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_VERTICES, serializePolygon(polygon));
        values.put(COLUMN_ESTADO, "Jalisco");
        values.put(COLUMN_MUNICIPIO, "Guadalajara");
        db.update(TABLE_ZONAS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }
    // Actualizar el estatus del poligono de la base de datos por su ID
    public void updateVisibilityZona(int id, Boolean visibility) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_VISIBILITY, visibility ? 1 : 0);
        db.update(TABLE_ZONAS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});

        db.close();
    }
    // Actualizar el estatus del poligono de la base de datos por su ID
    public void updateLabelZona(int id, Boolean label) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LABEL, label ? 1 : 0);
        db.update(TABLE_ZONAS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});

        db.close();
    }

    // Elimina una coordenada de la base de datos por su ID
    public void deleteZona(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ZONAS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    /*
     * Descripcion: FUNCIONES PARA LA BASE DE DATOS DE CAMIONES
     * Autor: CHARLY
     * Ultima fecha de actualizacion: 24.09.2024
     */

    // Guarda una coordenada en la base de datos
    public void saveCamion(String name,double toneladas,double altura,double ancho,double largo,String imagenBase64) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_TONELADA, toneladas);
        values.put(COLUMN_ALTURA, altura);
        values.put(COLUMN_ANCHO, ancho);
        values.put(COLUMN_LARGO, largo);
        values.put(COLUMN_IMAGEN, imagenBase64);
        db.insert(TABLE_CAMIONES, null, values);
        db.close();
    }


    // Guarda una coordenada en la base de datos
    public void updateCamion(int id,String name,double toneladas,double altura,double ancho,double largo,String imagenBase64) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_TONELADA, toneladas);
        values.put(COLUMN_ALTURA, altura);
        values.put(COLUMN_ANCHO, ancho);
        values.put(COLUMN_LARGO, largo);
        values.put(COLUMN_IMAGEN, imagenBase64);
        db.update(TABLE_CAMIONES, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Recupera todas las coordenadas de la base de datos
    public List<TruckSpec> getAllCamiones() {
        List<TruckSpec> camiones = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_CAMIONES;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                int tonelada = cursor.getInt(cursor.getColumnIndex(COLUMN_TONELADA));
                int altura = cursor.getInt(cursor.getColumnIndex(COLUMN_ALTURA));
                int ancho = cursor.getInt(cursor.getColumnIndex(COLUMN_ANCHO));
                int largo = cursor.getInt(cursor.getColumnIndex(COLUMN_LARGO));
                // Obtener la imagen en Base64
                String imagenBase64 = cursor.getString(cursor.getColumnIndex(COLUMN_IMAGEN));

                // Convertir la imagen Base64 a Bitmap
                Bitmap imagen = null;
                if (imagenBase64 != null && !imagenBase64.isEmpty()) {
                    byte[] decodedString = Base64.decode(imagenBase64, Base64.DEFAULT);
                    imagen = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                }
                camiones.add(new TruckSpec(id,name,tonelada,altura,ancho,largo,imagen));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return camiones;
    }

    // Recupera todas las coordenadas de la base de datos
    public TruckSpec getCamion(int id_vehiculo) {
        TruckSpec camion = null;
        try{
            SQLiteDatabase db = this.getReadableDatabase();
            String selectQuery = "SELECT * FROM " + TABLE_CAMIONES + " WHERE " + COLUMN_ID + " = ?";
            Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(id_vehiculo)});

            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                int tonelada = cursor.getInt(cursor.getColumnIndex(COLUMN_TONELADA));
                int altura = cursor.getInt(cursor.getColumnIndex(COLUMN_ALTURA));
                int ancho = cursor.getInt(cursor.getColumnIndex(COLUMN_ANCHO));
                int largo = cursor.getInt(cursor.getColumnIndex(COLUMN_LARGO));
                // Obtener la imagen en Base64
                String imagenBase64 = cursor.getString(cursor.getColumnIndex(COLUMN_IMAGEN));

                // Convertir la imagen Base64 a Bitmap
                Bitmap imagen = null;
                if (imagenBase64 != null && !imagenBase64.isEmpty()) {
                    byte[] decodedString = Base64.decode(imagenBase64, Base64.DEFAULT);
                    imagen = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                }
                camion = new TruckSpec(id,name,tonelada,altura,ancho,largo,imagen);
            }
            cursor.close();
            db.close();
        }catch (Exception e){
            Log.e(TAG,e.getMessage());
        }
        return camion;
    }

    /*
     * Descripcion: FUNCIONES PARA LA BASE DE DATOS DE RUTAS
     * Autor: CHARLY
     * Ultima fecha de actualizacion: 24.09.2024
     */

    // Guarda una coordenada en la base de datos
    public void saveRuta(int id,String name, String direccion_inicio, GeoCoordinates coordinateInicial , String direccion_fin , GeoCoordinates coordinateFinal, MapPolyline poligoline, int distancia, int tiempo, String fechaCreacion, String fechaUltimaModificacion, int[] truckSpecIds, int[] puntosIds, int[] zonasIds, boolean ordenar_automaticamente, int status) {
         try{
             SQLiteDatabase db = this.getWritableDatabase();
             ContentValues values = new ContentValues();
             values.put(COLUMN_ID, id);
             values.put(COLUMN_NAME, name);
             values.put(COLUMN_DIRECCION_INICIO, direccion_inicio);
             values.put(COLUMN_LATITUDE_INICIO, coordinateInicial.latitude);
             values.put(COLUMN_LONGITUDE_INICIO, coordinateInicial.longitude);
             values.put(COLUMN_DIRECCION_FIN, direccion_fin);
             values.put(COLUMN_LATITUDE_FIN, coordinateFinal.latitude);
             values.put(COLUMN_LONGITUDE_FIN, coordinateFinal.longitude);
             //values.put(COLUMN_FECHA_CREACION, System.currentTimeMillis());
             values.put(COLUMN_POLIGOLINE, serializePolyline(poligoline));
             values.put(COLUMN_DISTANCIA, distancia);
             values.put(COLUMN_TIEMPO, tiempo);
             values.put(COLUMN_FECHA_CREACION, fechaCreacion);
             values.put(COLUMN_FECHA_ULTIMA_MODIFICACION, fechaUltimaModificacion);
             String[] truckSpecIdsStringArray = Arrays.stream(truckSpecIds)
                     .mapToObj(String::valueOf)
                     .toArray(String[]::new);
             String truckSpecIdsString = TextUtils.join(",", truckSpecIdsStringArray);
             values.put(COLUMN_TUCKSPEC_IDS, truckSpecIdsString);
             String[] puntosIdsStringArray = Arrays.stream(puntosIds)
                     .mapToObj(String::valueOf)
                     .toArray(String[]::new);
             String puntosIdsString = TextUtils.join(",", puntosIdsStringArray);
             values.put(COLUMN_PUNTOS_IDS, puntosIdsString);
             String[] zonasIdsStringArray = Arrays.stream(zonasIds)
                     .mapToObj(String::valueOf)
                     .toArray(String[]::new);
             String zonasIdsString = TextUtils.join(",", zonasIdsStringArray);
             values.put(COLUMN_ZONAS_IDS, zonasIdsString);
             values.put(COLUMN_ORDEN_AUTOMATICO, ordenar_automaticamente ? 1 : 0);
             values.put(COLUMN_STATUS, status);
             db.insert(TABLE_ROUTES, null, values);
             db.close();
         }catch (Exception e){
             Log.e(TAG,e.getMessage());
         }
    }


    // Guarda una coordenada en la base de datos
    public void updateRoute(int id,String name, GeoCoordinates coordinateInicial, GeoCoordinates coordinateFinal, MapPolyline poligoline, int[] truckSpecIds, String fechaCreacion) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_LATITUDE_INICIO, coordinateInicial.latitude);
        values.put(COLUMN_LONGITUDE_INICIO, coordinateInicial.longitude);
        values.put(COLUMN_LATITUDE_FIN, coordinateFinal.latitude);
        values.put(COLUMN_LONGITUDE_FIN, coordinateFinal.longitude);
        values.put(COLUMN_FECHA_CREACION, fechaCreacion);
        values.put(COLUMN_FECHA_ULTIMA_MODIFICACION, System.currentTimeMillis());
        values.put(COLUMN_POLIGOLINE, serializePolyline(poligoline));
        values.put(COLUMN_TUCKSPEC_IDS, Arrays.toString(truckSpecIds));
        db.update(TABLE_ROUTES, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Recupera todas las coordenadas de la base de datos
    public List<RoutesWithId> getAllRoutes() {
        List<RoutesWithId> routesWithIds = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_ROUTES;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                String direccion_inicio = cursor.getString(cursor.getColumnIndex(COLUMN_DIRECCION_INICIO));
                double latitude_inicial = cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE_INICIO));
                double longitude_inicial = cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE_INICIO));
                String direccion_fin = cursor.getString(cursor.getColumnIndex(COLUMN_DIRECCION_FIN));
                double latitude_fin = cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE_FIN));
                double longitude_fin = cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE_FIN));
                String poligolineString = cursor.getString(cursor.getColumnIndex(COLUMN_POLIGOLINE));
                GeoPolyline geoPolyline = null;
                try {
                    geoPolyline = new GeoPolyline(deserializePolygon(poligolineString));
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
                int distancia = cursor.getInt(cursor.getColumnIndex(COLUMN_DISTANCIA));
                int tiempo = cursor.getInt(cursor.getColumnIndex(COLUMN_TIEMPO));
                String fechaCreacionString = cursor.getString(cursor.getColumnIndex(COLUMN_FECHA_CREACION));
                Date fechaCreacion = null;
                try {
                    fechaCreacion = dateFormat.parse(fechaCreacionString);
                } catch (ParseException e) {
                    e.printStackTrace(); // Handle the exception appropriately
                }
                String fechaUltimaModificacionString = cursor.getString(cursor.getColumnIndex(COLUMN_FECHA_ULTIMA_MODIFICACION));
                Date fechaUltimaModificacion = null;
                if (fechaUltimaModificacionString != "") {
                    try {
                        fechaUltimaModificacion = dateFormat.parse(fechaUltimaModificacionString);
                    } catch (ParseException e) {
                        //throw new RuntimeException(e);
                    }
                }
                String truckSpecIdsString = cursor.getString(cursor.getColumnIndex(COLUMN_TUCKSPEC_IDS));
                int[] truckSpectIds = null;
                if(truckSpecIdsString.length()!=0){
                    String[] vehiculosIdsArray = truckSpecIdsString.split(",");
                    truckSpectIds = new int[vehiculosIdsArray.length];
                    for (int i = 0; i < vehiculosIdsArray.length; i++) {
                        vehiculosIdsArray[i] = vehiculosIdsArray[i].replace("[", "").replace("]", "");
                        truckSpectIds[i] = Integer.parseInt(vehiculosIdsArray[i].trim());
                    }
                }
                String puntosIdsString = cursor.getString(cursor.getColumnIndex(COLUMN_PUNTOS_IDS));
                int[] puntosIds = null;
                if(puntosIdsString.length()!=0){
                    String[] puntosIdsArray = puntosIdsString.split(",");
                    puntosIds = new int[puntosIdsArray.length];
                    for (int i = 0; i < puntosIdsArray.length; i++) {
                        puntosIdsArray[i] = puntosIdsArray[i].replace("[", "").replace("]", "");
                        puntosIds[i] = Integer.parseInt(puntosIdsArray[i].trim());
                    }
                }
                String zonasIdsString = cursor.getString(cursor.getColumnIndex(COLUMN_ZONAS_IDS));
                int[] zonasIds = null;
                if(zonasIdsString.length()!=0){
                    String[] zonasIdsArray = zonasIdsString.split(",");
                    zonasIds = new int[zonasIdsArray.length];
                    for (int i = 0; i < zonasIdsArray.length; i++) {
                        zonasIdsArray[i] = zonasIdsArray[i].replace("[", "").replace("]", "");
                        zonasIds[i] = Integer.parseInt(zonasIdsArray[i].trim());
                    }
                }
                boolean ordena_automatico = cursor.getInt(cursor.getColumnIndex(COLUMN_ORDEN_AUTOMATICO)) == 1;
                int status = cursor.getInt(cursor.getColumnIndex(COLUMN_STATUS));
                routesWithIds.add(new RoutesWithId(id,name,direccion_inicio,new GeoCoordinates(latitude_inicial,longitude_inicial),direccion_fin,new GeoCoordinates(latitude_fin,longitude_fin),trafficSpanMapPolyline,distancia,tiempo,fechaCreacion,fechaUltimaModificacion,truckSpectIds,puntosIds,zonasIds,ordena_automatico,status));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return routesWithIds;
    }

    public RoutesWithId getRouteById(int idRuta) {
        RoutesWithId ruta = null;
        try {
            //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            SQLiteDatabase db = this.getReadableDatabase();
            String selectQuery = "SELECT * FROM " + TABLE_ROUTES + " WHERE " + COLUMN_ID + " = ?";
            Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(idRuta)});
            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                String direccion_inicio = cursor.getString(cursor.getColumnIndex(COLUMN_DIRECCION_INICIO));
                double latitude_inicial = cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE_INICIO));
                double longitude_inicial = cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE_INICIO));
                String direccion_fin = cursor.getString(cursor.getColumnIndex(COLUMN_DIRECCION_FIN));
                double latitude_fin = cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE_FIN));
                double longitude_fin = cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE_FIN));
                String poligolineString = cursor.getString(cursor.getColumnIndex(COLUMN_POLIGOLINE));
                GeoPolyline geoPolyline = null;
                try {
                    geoPolyline = new GeoPolyline(deserializePolygon(poligolineString));
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
                int distancia = cursor.getInt(cursor.getColumnIndex(COLUMN_DISTANCIA));
                int tiempo = cursor.getInt(cursor.getColumnIndex(COLUMN_TIEMPO));
                String fechaCreacionString = cursor.getString(cursor.getColumnIndex(COLUMN_FECHA_CREACION));
                Date fechaCreacion = null;
                try {
                    fechaCreacion = dateFormat.parse(fechaCreacionString);
                } catch (ParseException e) {
                    e.printStackTrace(); // Handle the exception appropriately
                }
                String fechaUltimaModificacionString = cursor.getString(cursor.getColumnIndex(COLUMN_FECHA_ULTIMA_MODIFICACION));
                Date fechaUltimaModificacion = null;
                if (fechaUltimaModificacionString != "") {
                    try {
                        fechaUltimaModificacion = dateFormat.parse(fechaUltimaModificacionString);
                    } catch (ParseException e) {
                        //throw new RuntimeException(e);
                    }
                }
                String truckSpecIdsString = cursor.getString(cursor.getColumnIndex(COLUMN_TUCKSPEC_IDS));
                String[] truckSpecIdsArray = truckSpecIdsString.split(",");
                int[] truckSpectIds = new int[truckSpecIdsArray.length];
                for (int i = 0; i < truckSpecIdsArray.length; i++) {
                    truckSpectIds[i] = Integer.parseInt(truckSpecIdsArray[i].trim());
                }
                String puntosIdsString = cursor.getString(cursor.getColumnIndex(COLUMN_PUNTOS_IDS));
                int[] puntosIds = null;
                if(puntosIdsString.length()!=0){
                    String[] puntosIdsArray = puntosIdsString.split(",");
                    puntosIds = new int[puntosIdsArray.length];
                    for (int i = 0; i < puntosIdsArray.length; i++) {
                        puntosIdsArray[i] = puntosIdsArray[i].replace("[", "").replace("]", "");
                        puntosIds[i] = Integer.parseInt(puntosIdsArray[i].trim());
                    }
                }
                String zonasIdsString = cursor.getString(cursor.getColumnIndex(COLUMN_ZONAS_IDS));
                int[] zonasIds = null;
                if(zonasIdsString.length()!=0){
                    String[] zonasIdsArray = zonasIdsString.split(",");
                    zonasIds = new int[zonasIdsArray.length];
                    for (int i = 0; i < zonasIdsArray.length; i++) {
                        zonasIdsArray[i] = zonasIdsArray[i].replace("[", "").replace("]", "");
                        zonasIds[i] = Integer.parseInt(zonasIdsArray[i].trim());
                    }
                }
                boolean orden_automatico = cursor.getInt(cursor.getColumnIndex(COLUMN_ORDEN_AUTOMATICO)) == 1;
                int status = cursor.getInt(cursor.getColumnIndex(COLUMN_STATUS));
                ruta = new RoutesWithId(id,name,direccion_inicio,new GeoCoordinates(latitude_inicial,longitude_inicial),direccion_fin,new GeoCoordinates(latitude_fin,longitude_fin),trafficSpanMapPolyline,distancia,tiempo,fechaCreacion,fechaUltimaModificacion,truckSpectIds,puntosIds,zonasIds,orden_automatico,status);
            }
            cursor.close();
            db.close();
        }catch (Exception e){
            Log.e(TAG,e.getMessage());
        }
        return ruta;
    }

    // Elimina una coordenada de la base de datos por su ID
    public void deleteRoute(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ROUTES, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    /**
     * FUNCION PARA REALIZAR INSERTS GENERICOS DE CUALQUIER TABLA
     * @param values ContentValues values del insert a realizar
     * @param tableName String nombre de la tabla a la que se le hara el insert
     **/
    public void insertGenerico(String tableName, ContentValues values ) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.insert(tableName, null, values);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        db.close();
    }

    /**
     * FUNCION PARA REALIZAR DELETE A CUALQUIER TABLA
     * @param query String del query que hara el proceso de "Delete" en la tabla
     */
    public void deleteGenerico(String query) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL(query);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        db.close();
    }

    /**
     * FUNCION PARA CREAR UNA NUEVA TABLA EN LA BASE DE DATOS SQLITE
     * @param query String con el query para crear la tabla
     */
    public void createTableGenerico(String query) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL(query);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        db.close();
    }

    /**
     * FUNCION PARA VALIDAR SI EN BASE DE DATOS YA EXISTE UNA TABLA ES ESPECIFICO
     * @param tableName Nombre de la tabla que se esta validando
     * @return boolean True si la tabla Existe False si la tabla no existe
     */
    public boolean existeTabla(String tableName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean exists = false;
        try {
            // Consulta para buscar la tabla en sqlite_master
            cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", new String[]{tableName});

            if (cursor != null && cursor.getCount() > 0) {
                exists = true; // La tabla existe
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return exists;
    }

    /**
     * FUNCION PARA REALIZAR CONSUTAS DE CUALQUIER TABLA
     * @param tableName String nombre de la tabla a consultar
     * @param columns columnas de la tabla que se desean consultar
     * @param selection alguna validacion que se de deba realizar en la consulta "Where"
     * @param selectionArgs valor con el que se hara el "selection"
     * @param orderBy orden ascendente o descendente y por cual columna
     * @return Lista con los valores que arroja la consulta
     */
    public List<Map<String, Object>> selectGenerico(String tableName, String[] columns, String selection, String[] selectionArgs, String orderBy) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(tableName, columns, selection, selectionArgs, null, null, orderBy);

        try {
            if (cursor.moveToFirst()) {
                do {
                    Map<String, Object> rowMap = new HashMap<>();
                    for (String column : columns) {
                        int columnIndex = cursor.getColumnIndex(column);
                        int type = cursor.getType(columnIndex);
                        switch (type) {
                            case Cursor.FIELD_TYPE_STRING:
                                rowMap.put(column, cursor.getString(columnIndex));
                                break;
                            case Cursor.FIELD_TYPE_INTEGER:
                                rowMap.put(column, cursor.getInt(columnIndex));
                                break;
                            case Cursor.FIELD_TYPE_FLOAT:
                                rowMap.put(column, cursor.getDouble(columnIndex));
                                break;
                            case Cursor.FIELD_TYPE_BLOB:
                                rowMap.put(column, cursor.getBlob(columnIndex));
                                break;
                            case Cursor.FIELD_TYPE_NULL:
                                rowMap.put(column, null);
                                break;
                        }
                    }
                    resultList.add(rowMap);
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
            db.close();
        }

        return resultList;
    }

    /*
     * Descripcion: FUNCIONES PARA LA BASE DE DATOS DE ASIGNACIONES
     * Autor: CHARLY
     * Ultima fecha de actualizacion: 16.01.2025
     */

    // Guarda una asignacion en la base de datos
    public void saveAsignacion(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ROUTE_ID, id);
        db.insert(TABLE_ASIGNACIONES, null, values);
        db.close();
    }

    // Recupera todas las asignaciones de la base de datos
    public List<Integer> getAllAsignaciones() {
        List<Integer> asignaciones = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_ASIGNACIONES;
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ROUTE_ID));
                asignaciones.add(id);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return asignaciones;
    }

    // Elimina todas las asignaciones de la base de datos
    public void truncateAsignaciones() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ASIGNACIONES, null, null);
        db.close();
    }

}