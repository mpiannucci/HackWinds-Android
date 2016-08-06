package com.nucc.hackwinds.models;


import android.content.Context;
import android.preference.PreferenceManager;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nucc.hackwinds.listeners.CameraChangedListener;
import com.nucc.hackwinds.types.Camera;
import com.nucc.hackwinds.views.SettingsActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class CameraModel {
    // Member variables
    public HashMap<String, HashMap<String, Camera> > cameraLocations;
    public ArrayList<String> locationKeys;
    public ArrayList<ArrayList<String> > cameraKeys;
    public int cameraCount;
    public int locationCount;

    private static CameraModel mInstance;
    private Context mContext;
    private ArrayList<CameraChangedListener> mCameraChangedListeners;
    private boolean mForceReload;

    public static CameraModel getInstance(Context ctx) {
        if ( mInstance == null ) {
            mInstance = new CameraModel(ctx);
        }
        return mInstance;
    }

    private CameraModel(Context ctx) {
        // Initialize the context
        mContext = ctx;
        reset();
    }

    public void addCameraChangedListener(CameraChangedListener listener) {
        mCameraChangedListeners.add(listener);
    }

    public void reset() {
        mForceReload = true;
        cameraLocations = new HashMap<>();
        locationKeys = new ArrayList<>();
        cameraKeys = new ArrayList<>();
        mCameraChangedListeners = new ArrayList<>();
        cameraCount = 0;
        locationCount = 0;
    }

    public void fetchCameraURLs() {
        synchronized (this) {
            if (!mForceReload) {
                for (CameraChangedListener listener : mCameraChangedListeners) {
                    if (listener != null) {
                        listener.cameraDataUpdated();
                    }
                }
                return;
            }

            final String HACKWINDS_API_URL = "https://mpiannucci.appspot.com/static/API/hackwinds_camera_locations_v5.json";

            Ion.with(mContext).load(HACKWINDS_API_URL).asString().setCallback(new FutureCallback<String>() {
                @Override
                public void onCompleted(Exception e, String result) {
                    if (e != null) {
                        for (CameraChangedListener listener : mCameraChangedListeners) {
                            if (listener != null) {
                                listener.cameraDataUpdateFailed();
                                return;
                            }
                        }
                    }

                    boolean successfulParse = parseCameras(result);
                    if (successfulParse) {
                        mForceReload = false;
                        for (CameraChangedListener listener : mCameraChangedListeners) {
                            if (listener != null) {
                                listener.cameraDataUpdated();
                                return;
                            }
                        }
                    } else {
                        for (CameraChangedListener listener : mCameraChangedListeners) {
                            if (listener != null) {
                                listener.cameraDataUpdateFailed();
                                return;
                            }
                        }
                    }
                }
            });
        }
    }

    public void forceFetchCameraURLs() {
        mForceReload = true;
        fetchCameraURLs();
    }

    private boolean parseCameras(String rawData) {
        final Boolean PREMIUM_ENABLED = PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(SettingsActivity.SHOW_PREMIUM_CONTENT_KEY, false);

        try {
            JSONObject jsonResp = new JSONObject(rawData);
            JSONObject cameraObject = jsonResp.getJSONObject("CameraLocations");

            Iterator<String> locationIterator = cameraObject.keys();
            while (locationIterator.hasNext()) {
                String locationName = locationIterator.next();
                cameraLocations.put(locationName, new HashMap<String, Camera>());
                locationKeys.add(locationName);
                cameraKeys.add(new ArrayList<String>());

                JSONObject locationObject = cameraObject.getJSONObject(locationName);
                Iterator<String> cameraIterator = locationObject.keys();
                while (cameraIterator.hasNext()) {
                    String cameraName = cameraIterator.next();
                    JSONObject thisCameraObject = locationObject.getJSONObject(cameraName);

                    // Create the new camera object for the camera
                    Camera thisCamera = new Camera();

                    // For now everything else is common
                    thisCamera.imageURL = thisCameraObject.getString("Image");
                    thisCamera.videoURL = thisCameraObject.getString("Video");
                    thisCamera.webURL = thisCameraObject.getString("Web");
                    thisCamera.refreshable = thisCameraObject.getBoolean("Refreshable");
                    thisCamera.refreshInterval = Integer.valueOf(thisCameraObject.getString("RefreshInterval"));
                    thisCamera.premium = thisCameraObject.getBoolean("Premium");

                    if (thisCamera.premium && !PREMIUM_ENABLED) {
                        continue;
                    }

                    cameraLocations.get(locationName).put(cameraName, thisCamera);
                    cameraKeys.get(locationCount).add(cameraName);
                    cameraCount++;
                }

                int locationCameraCount = cameraLocations.get(locationName).size();
                if (locationCameraCount > 0) {
                    locationCount++;
                } else {
                    cameraLocations.remove(locationName);
                    locationKeys.remove(locationName);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
