package com.itsmarts.smartroutetruckapp.clases;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.itsmarts.smartroutetruckapp.MainActivity;

import org.json.JSONObject;

public class ActivityTracker {

    public static void trackActivity(String module,String action, String details) {
        try {
            JSONObject activityData = new JSONObject();
            activityData.put("modulo", module);
            activityData.put("accion", action);
            activityData.put("detalles", details);
            activityData.put("fecha_hora", Logger.getCurrentTimeStamp());

            Logger.writeToFile("activity_log.json", activityData.toString());
        } catch (Exception e) {
            Log.e("ActivityTracker", "Error al registrar la actividad: " + e.getMessage());
        }
    }
}