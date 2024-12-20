package com.itsmarts.smartroutetruckapp.helpers;

import java.io.IOException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class Internet {
    public static boolean isNetworkConnected() {
        try {
            Process process = Runtime.getRuntime().exec("ping -c 1 8.8.8.8"); // Google's public DNS server
            int exitValue = process.waitFor();
            return exitValue == 0; // Exit value 0 indicates successful ping
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }
}
