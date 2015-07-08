package com.nucc.hackwinds.models;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.nucc.hackwinds.utilities.ServiceHandler;

import org.json.JSONException;
import org.json.JSONObject;

public class CameraModel {

    // Constants
    private final String HACKWINDS_API_URL = "http://blog.mpiannucci.com/static/hackwinds_camera_locations.json";
    private final String NEEDS_RELOAD_KEY = "NeedCameraLocationFetch";

    // Member variables
    private Context mContext;
    private static CameraModel mInstance;
    private boolean mForceReload;

    public static JSONObject cameraLocations;

    public static CameraModel getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new CameraModel(context);
        }
        return mInstance;
    }

    public static JSONObject getCameraLocations() {
        return cameraLocations;
    }

    private CameraModel(Context context) {
        // Initialize the context
        mContext = context.getApplicationContext();
        mForceReload = false;
    }

    public boolean fetchCameraURLs() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean needsReload = sharedPrefs.getBoolean(NEEDS_RELOAD_KEY, true);

        if (!needsReload && !mForceReload) {
            return true;
        }

        ServiceHandler sh = new ServiceHandler();
        String rawData = sh.makeServiceCall(HACKWINDS_API_URL, ServiceHandler.GET);
        JSONObject narragansettCams;

        try {
            JSONObject jsonResp = new JSONObject(rawData);
            cameraLocations = jsonResp.getJSONObject("camera_locations");
            narragansettCams = cameraLocations.getJSONObject("Narragansett");
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

        if (narragansettCams.length() > 3) {
            try {
                String pjURL = narragansettCams.getString("Point Judith");
                String rawPJData = sh.makeServiceCall(pjURL, ServiceHandler.GET);
                JSONObject pjResp = new JSONObject(rawPJData);
                JSONObject pjStreamData = (JSONObject)pjResp.getJSONObject("streamInfo").getJSONArray("stream").get(0);
                narragansettCams.put("Point Judith", pjStreamData);
            } catch (JSONException e) {
                e.printStackTrace();
                return true;
            }
        }

        return true;
    }

    public boolean forceFetchCameraURLs() {
        mForceReload = true;
        return fetchCameraURLs();
    }
}
