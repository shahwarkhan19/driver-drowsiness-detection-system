package com.driver.drowsers.helper;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.core.content.ContextCompat;
import androidx.core.location.LocationManagerCompat;

import java.util.regex.Pattern;

public class Helper {

    public static boolean isEmailValid(String trim) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(trim).matches();
    }

    public static boolean isValidName(String text) {
        return Pattern.compile("^[a-z A-Z]+$").matcher(text).matches();
    }

    /**
     * CHECK WHETHER INTERNET CONNECTION IS AVAILABLE OR NOT
     */

    public static boolean isInternetConnected(Context context) {
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connMgr.getActiveNetworkInfo();
        if (activeNetworkInfo != null) { // connected to the internet

            // connected to the mobile provider's data plan
            return activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI || activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE;

        }
        return false;
    }

    public static boolean isLocationPermissionGranted(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isLocationEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return LocationManagerCompat.isLocationEnabled(locationManager);
    }

    public static void openGps(Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Location is not enabled")  // GPS not found
                .setMessage("Please enable location to continue") // Want to enable?
                .setPositiveButton("Enable", (dialogInterface, i) -> context.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                .setNegativeButton("Skip", null)
                .show();
    }
}
