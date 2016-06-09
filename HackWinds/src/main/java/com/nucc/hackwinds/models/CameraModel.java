package com.nucc.hackwinds.models;


import android.content.Context;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nucc.hackwinds.listeners.CameraChangedListener;
import com.nucc.hackwinds.types.Camera;

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
        mForceReload = true;
        cameraLocations = new HashMap<>();
        locationKeys = new ArrayList<>();
        cameraKeys = new ArrayList<>();
        mCameraChangedListeners = new ArrayList<>();
        cameraCount = 0;
        locationCount = 0;
    }

    public void addCameraChangedListener(CameraChangedListener listener) {
        mCameraChangedListeners.add(listener);
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

            final String HACKWINDS_API_URL = "https://mpiannucci.appspot.com/static/API/hackwinds_camera_locations_v3.json";

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
        try {
            JSONObject jsonResp = new JSONObject(rawData);
            JSONObject cameraObject = jsonResp.getJSONObject("camera_locations");

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
                    thisCamera.refreshable = thisCameraObject.getBoolean("Refreshable");
                    thisCamera.refreshInterval = Integer.valueOf(thisCameraObject.getString("RefreshInterval"));

                    cameraLocations.get(locationName).put(cameraName, thisCamera);
                    cameraKeys.get(locationCount).add(cameraName);
                    cameraCount++;
                }
                locationCount++;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
