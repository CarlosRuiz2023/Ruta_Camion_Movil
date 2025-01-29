package com.itsmarts.SmartRouteTruckApp.clases;

import static android.view.View.VISIBLE;

import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.here.sdk.core.LanguageCode;
import com.here.sdk.core.engine.SDKNativeEngine;
import com.here.sdk.maploader.CatalogUpdateInfo;
import com.here.sdk.maploader.CatalogUpdateProgressListener;
import com.here.sdk.maploader.CatalogUpdateTask;
import com.here.sdk.maploader.CatalogsUpdateInfoCallback;
import com.here.sdk.maploader.DownloadRegionsStatusListener;
import com.here.sdk.maploader.DownloadableRegionsCallback;
import com.here.sdk.maploader.InstalledRegion;
import com.here.sdk.maploader.MapDownloader;
import com.here.sdk.maploader.MapDownloaderConstructionCallback;
import com.here.sdk.maploader.MapDownloaderTask;
import com.here.sdk.maploader.MapLoaderError;
import com.here.sdk.maploader.MapLoaderException;
import com.here.sdk.maploader.MapUpdater;
import com.here.sdk.maploader.MapUpdaterConstructionCallback;
import com.here.sdk.maploader.MapVersionHandle;
import com.here.sdk.maploader.Region;
import com.here.sdk.maploader.RegionId;
import com.itsmarts.SmartRouteTruckApp.MainActivity;
import com.itsmarts.SmartRouteTruckApp.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * Descripcion: Esta clase maneja los procesos para descarga y consulta de mapas
 * Autor: Areli S
 * Ultima fecha de actualizacion: 07.09.2024
 */

/**Clase para descarga de mapa de Mexico*/
public class OfflineMap {
    private CatalogUpdateInfo catalogUpdate;
    private boolean isDownloadOnProcess = false;
    public boolean isMexicoMapDownload = false;
    private static final String TAG = "OfflineMap";
    /**
     * Variable para acceder al MainActivity
     */
    private MainActivity mainActivity;
    /**
     * Variable del SDK para procesos de descarga del mapa
     */
    @Nullable
    private MapDownloader mapDownloader;
    /**
     * Variable del SDK para actualizaciones de mapa
     */
    @Nullable
    private MapUpdater mapUpdater;
    /**
     * Objeto para contender la informacion de la Region Mexico
     */
    private Region mexicoRegion;
    /**
     * Lista de tareas del MapDownloaderTask
     */
    private final List<MapDownloaderTask> mapDownloaderTasks = new ArrayList<>();
    ;

    /**
     * Constructor para inicializar variables una vez inicializada esta clase
     *
     * @param mapView      acceso al map view del MainActivity
     * @param mainActivity acceso al MainActivity
     */
    public OfflineMap(MainActivity mainActivity) {
        this.mainActivity = mainActivity;

        SDKNativeEngine sdkNativeEngine = SDKNativeEngine.getSharedInstance();
        if (sdkNativeEngine == null) {
            //throw new RuntimeException("SDKNativeEngine not initialized.");
            Log.e(TAG, "SDKNativeEngine not initialized.");
        }
        // Note that the default storage path can be adapted when creating a new SDKNativeEngine.
        String storagePath = sdkNativeEngine.getOptions().cachePath;
        Log.d(TAG, "StoragePath: " + storagePath);

        MapDownloader.fromEngineAsync(sdkNativeEngine, new MapDownloaderConstructionCallback() {
            @Override
            public void onMapDownloaderConstructedCompleted(@NonNull MapDownloader mapDownloader) {
                OfflineMap.this.mapDownloader = mapDownloader;
                try {
                    List<InstalledRegion> list = mapDownloader.getInstalledRegions();
                    if (list != null && list.size() > 0) {
                        mainActivity.mapOfflineMexDownload = true;
                        isMexicoMapDownload = true;
                        if (mainActivity.offlineMapItem != null ) {
                            mainActivity.offlineMapItem.setChecked(true);
                            onSwitchOfflineButtonClicked();
                            mainActivity.routingExample.routingInterface = mainActivity.routingExample.offlineRoutingEngine;
                        }
                    }
                    Log.d(TAG,"InstalledRegion: "+ list.toString());
                } catch (MapLoaderException e) {
                    Log.d(TAG,"MapLoaderException: "+ e.getLocalizedMessage());
                }
            }

        });

        MapUpdater.fromEngineAsync(sdkNativeEngine, new MapUpdaterConstructionCallback() {
            @Override
            public void onMapUpdaterConstructe(@NonNull MapUpdater mapUpdater) {
                OfflineMap.this.mapUpdater = mapUpdater;
                logCurrentMapVersion();
            }
        });

    }

    /**
     * Funcion para obtener la informacion de descarga del mapa de Mexico
     */
    public void onDownloadMexicoRegionClicked() {
        if (mapDownloader == null) {
            mainActivity.btnDescargar.setEnabled(false);
            mainActivity.txtDescargaTitulo.setText(mainActivity.getString(R.string.MapDownloader_instance_no_esta_listo));
            return;
        }

        // Download a list of Region items that will tell us what map regions are available for later download.
        mapDownloader.getDownloadableRegions(LanguageCode.ES_MX, new DownloadableRegionsCallback() {
            @Override
            public void onCompleted(@Nullable MapLoaderError mapLoaderError, @Nullable List<Region> list) {
                if (mapLoaderError != null) {
                    String message = mainActivity.getString(R.string.Ocurrio_un_error_al_tratar_de_encontrar_el_mapa) + mapLoaderError;
                    mainActivity.txtDescargaTitulo.setText(message);
                    mainActivity.btnDescargar.setEnabled(false);
                    return;
                }
                // If error is null, it is guaranteed that the list will not be null.
                @Nullable
                List<Region> downloadableRegions = list;
                for (Region region : downloadableRegions) {
                    List<Region> childRegions = region.childRegions;
                    if (childRegions == null) {
                        continue;
                    }
                    for (Region childRegion : childRegions) {
                        long sizeOnDiskInMB = childRegion.sizeOnDiskInBytes / (1024 * 1024);
                        if (childRegion.name.equals(mainActivity.getString(R.string.mexico))) {
                            mexicoRegion = childRegion;
                            String messageTxtInfo = mainActivity.getString(R.string.nombre_mapa) + childRegion.name + "\n" +
                                    mainActivity.getString(R.string.id_mapa) + childRegion.regionId.id + "\n" +
                                    mainActivity.getString(R.string.tamano_descarga) + sizeOnDiskInMB + mainActivity.getString(R.string.simbolo_mb);
                            mainActivity.txtDescargaTitulo.setText(messageTxtInfo);
                        }
                    }
                }
            }
        });
    }

    /**
     * Funcion para comenzar proceso de descarga del mapa de Mexico
     */
    public void onDownloadMapMexicoClicked() {
        if (mapDownloader == null) {
            mainActivity.txtProcesoDescarga.setText(mainActivity.getString(R.string.la_instancia_de_madownloader_no_esta_lista));
            return;
        }
        //ID DE MEXICO: 22000493
        if (mexicoRegion == null) {
            mainActivity.txtProcesoDescarga.setText(mainActivity.getString(R.string.error_no_se_encontro_la_region_mexico));
            return;
        }
        List<RegionId> regionIDs = Collections.singletonList(mexicoRegion.regionId);
        MapDownloaderTask mapDownloaderTask = mapDownloader.downloadRegions(regionIDs,
                new DownloadRegionsStatusListener() {
                    @Override
                    public void onDownloadRegionsComplete(@Nullable MapLoaderError mapLoaderError, @Nullable List<RegionId> list) {
                        if (mapLoaderError != null) {
                            if (mapLoaderError == MapLoaderError.ALREADY_INSTALLED) {
                                String message = "";
                                mainActivity.btnDescargar.setText(mainActivity.getString(R.string.descargar));
                                mainActivity.btnDescargar.setEnabled(false);
                                mainActivity.txtProcesoDescarga.setText(mainActivity.getString(R.string.el_mapa_de_mexico_ya_esta_descargado));
                            } else {
                                String message = mainActivity.getString(R.string.error_al_completar_la_descarga_de_mexico) + mapLoaderError;
                                mainActivity.txtProcesoDescarga.setText(message);
                            }
                            isDownloadOnProcess = false;
                            return;
                        }
                        mainActivity.txtProcesoDescarga.setText(mainActivity.getString(R.string.descarga_completada));
                        isMexicoMapDownload = true;
                        mainActivity.mapOfflineMexDownload = true;
                        mainActivity.logger.trackActivity(TAG,"Mapa offline descargado","El usuario descargo el mapa offline");
                        try{
                            mainActivity.offlineMapItem.setChecked(true);
                            onSwitchOfflineButtonClicked();
                            mainActivity.routingExample.routingInterface = mainActivity.routingExample.offlineRoutingEngine;
                        }catch (Exception e){
                            Log.e(TAG,"Error al activar mapa offline: "+e.getMessage());
                        }
                    }

                    @Override
                    public void onProgress(@NonNull RegionId regionId, int percentage) {
                        String message = mainActivity.getString(R.string.descargando_mapa) + "\n" + mainActivity.getString(R.string.progreso) + percentage + mainActivity.getString(R.string.simbolo_porcentaje);
                        mainActivity.txtProcesoDescarga.setText(message);
                        isDownloadOnProcess = true;
                    }

                    @Override
                    public void onPause(@Nullable MapLoaderError mapLoaderError) {
                        if (mapLoaderError == null) {
                            String message = "";
                            mainActivity.txtProcesoDescarga.setText(mainActivity.getString(R.string.la_descarga_fue_pausada));
                        } else {
                            String message = mainActivity.getString(R.string.error_pausa_descarga) + mapLoaderError;
                            mainActivity.txtProcesoDescarga.setText(message);
                        }
                    }

                    @Override
                    public void onResume() {
                        mainActivity.txtProcesoDescarga.setText(mainActivity.getString(R.string.reanudado_descarga));
                    }
                });

        mapDownloaderTasks.add(mapDownloaderTask);
    }

    /**
     * Funcion para cancelar la descarga del mapa de Mexico
     */
    public void onCancelMapDownloadClicked() {
        for (MapDownloaderTask mapDownloaderTask : mapDownloaderTasks) {
            mapDownloaderTask.cancel();
        }
        isDownloadOnProcess = false;
        String message = mainActivity.getString(R.string.proceso_cancelado) + mapDownloaderTasks.size() + mainActivity.getString(R.string.tareas_canceladas);
        mainActivity.txtProcesoDescarga.setText(message);
        mapDownloaderTasks.clear();
        mainActivity.logger.trackActivity(TAG,"Mapa offline cancelado","El usuario cancelo la descarga del mapa offline");
    }

    public boolean onCancelMapDownloadFromDismiss() {
        if (isDownloadOnProcess) {
            for (MapDownloaderTask mapDownloaderTask : mapDownloaderTasks) {
                mapDownloaderTask.cancel();
            }
//        String message = mainActivity.getString(R.string.proceso_cancelado) + mapDownloaderTasks.size() + mainActivity.getString(R.string.tareas_canceladas);
//        mainActivity.txtProcesoDescarga.setText(message);
            mapDownloaderTasks.clear();
        }
        return isDownloadOnProcess;
    }

    private void logCurrentMapVersion() {
        if (mapUpdater == null) {
            String message = "MapUpdater instance not ready. Try again.";
            Log.d("MapUpdate", message);
            return;
        }

        try {
            MapVersionHandle mapVersionHandle = mapUpdater.getCurrentMapVersion();
            // Version string my look like "47.47,47.47".
            String message = mapVersionHandle.stringRepresentation(",");
            Log.d("Installed map version: ", mapVersionHandle.stringRepresentation(","));
        } catch (MapLoaderException e) {
            MapLoaderError mapLoaderError = e.error;
            Log.e("MapLoaderError", "Fetching current map version failed: " + mapLoaderError.toString());
        }
    }

    public void onSwitchOnlineButtonClicked() {
        SDKNativeEngine.getSharedInstance().setOfflineMode(false);
    }

    public void onSwitchOfflineButtonClicked() {
        SDKNativeEngine.getSharedInstance().setOfflineMode(true);
    }

    public void checkForMapUpdates() {
        if (mapUpdater == null) {
            Toast.makeText(mainActivity, "La instancia de MapUpdater no está lista. Inténtalo de nuevo.", Toast.LENGTH_SHORT).show();
            return;
        }

        mapUpdater.retrieveCatalogsUpdateInfo(new CatalogsUpdateInfoCallback() {
            @Override
            public void apply(@Nullable MapLoaderError mapLoaderError, @Nullable List<CatalogUpdateInfo> catalogList) {
                if (mapLoaderError != null) {
                    Log.e("CatalogUpdateCheck", "Error: " + mapLoaderError.name());
                    return;
                }

                // When error is null, then the list is guaranteed to be not null.
                if (catalogList.isEmpty()) {
                    Log.d("CatalogUpdateCheck", "No map updates are available.");
                }

                logCurrentMapVersion();

                // Usually, only one global catalog is available that contains regions for the whole world.
                // For some regions like Japan only a base map is available, by default.
                // If your company has an agreement with HERE to use a detailed Japan map, then in this case you
                // can install and use a second catalog that references the detailed Japan map data.
                // All map data is part of downloadable regions. A catalog contains references to the
                // available regions. The map data for a region may differ based on the catalog that is used
                // or on the version that is downloaded and installed.
                for (CatalogUpdateInfo catalogUpdateInfo : catalogList) {
                    String message = "Versión instalada: " + catalogUpdateInfo.installedCatalog.catalogIdentifier.version +
                            "\nÚltima versión del mapa disponible: " + catalogUpdateInfo.latestVersion;

                    mainActivity.txtDescargaInfo.setText(message);
                    mainActivity.txtProcesoActualizacion.setText("");
                    mainActivity.txtProcesoActualizacion.setTypeface(mainActivity.txtProcesoActualizacion.getTypeface(), Typeface.NORMAL);
                    mainActivity.btnBuscarActualizaciones.setVisibility(View.GONE);
                    if (catalogUpdateInfo.latestVersion > catalogUpdateInfo.installedCatalog.catalogIdentifier.version) {
                        mainActivity.btnIniciarActualizacion.setVisibility(VISIBLE);
                    }
//                    Log.d("CatalogUpdateCheck", "Catalog name:" + catalogUpdateInfo.installedCatalog.catalogIdentifier.hrn);
//                    Log.d("CatalogUpdateCheck", "Installed map version:" + catalogUpdateInfo.installedCatalog.catalogIdentifier.version);
//                    Log.d("CatalogUpdateCheck", "Latest available map version:" + catalogUpdateInfo.latestVersion);
                    catalogUpdate = catalogUpdateInfo;
                }
            }
        });
    }

    public void performMapUpdate() {
        mainActivity.btnIniciarActualizacion.setEnabled(false);
        if (mapUpdater == null) {
            Toast.makeText(mainActivity, "La instancia de MapUpdater no está lista. Inténtalo de nuevo.", Toast.LENGTH_SHORT).show();
            return;
        }

        // This method conveniently updates all installed regions for a catalog if an update is available.
        // Optionally, you can use the CatalogUpdateTask to pause / resume or cancel the update.
        CatalogUpdateTask task = mapUpdater.updateCatalog(catalogUpdate, new CatalogUpdateProgressListener() {
            @Override
            public void onProgress(@NonNull RegionId regionId, int percentage) {
                String message = "Descargando e instalando una actualización de mapa.\nProgreso para México: " + percentage + mainActivity.getString(R.string.simbolo_porcentaje);
                mainActivity.txtProcesoActualizacion.setText(message);
            }

            @Override
            public void onPause(@Nullable MapLoaderError mapLoaderError) {
                if (mapLoaderError == null) {
                    String message = "The map update was paused by the user calling catalogUpdateTask.pause().";
                    Log.e("CatalogUpdate", message);
                } else {
                    String message = "Map update onPause error. The task tried to often to retry the update: " + mapLoaderError;
                    Log.d("CatalogUpdate", message);
                }
            }

            @Override
            public void onResume() {
                String message = "A previously paused map update has been resumed.";
                Log.d("CatalogUpdate", message);
            }

            @Override
            public void onComplete(@Nullable MapLoaderError mapLoaderError) {
                if (mapLoaderError != null) {
                    String message = "Map update completion error: " + mapLoaderError;
                    Log.d("CatalogUpdate", message);
                    return;
                }

                String message = "Se han instalado correctamente una o más actualizaciones del mapa de México.";
                mainActivity.txtProcesoActualizacion.setText(message);
                logCurrentMapVersion();
                mainActivity.logger.trackActivity(TAG,"Mapa offline actualizado","El usuario actualizo el mapa offline");
            }
        });
    }
}