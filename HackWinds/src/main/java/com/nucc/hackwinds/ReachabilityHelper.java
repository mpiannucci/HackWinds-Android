package com.nucc.hackwinds;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ReachabilityHelper {

    /**
     * Checks if the device has internet access.
     * @param context The activity context to check against
     * @return True if there is internet access, false otherwise
     */
    public static boolean deviceHasInternetAccess(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
