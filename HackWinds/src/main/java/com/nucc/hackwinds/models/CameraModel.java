package com.nucc.hackwinds.models;

import android.content.Context;
import android.preference.PreferenceManager;

import com.appspot.hackwinds.hackwinds.model.MessagesCameraCameraMessage;
import com.appspot.hackwinds.hackwinds.model.MessagesCameraCameraLocationsMessage;
import com.appspot.hackwinds.hackwinds.model.MessagesCameraCameraRegionMessage;
import com.nucc.hackwinds.listeners.CameraChangedListener;
import com.nucc.hackwinds.tasks.FetchCamerasTask;
import com.nucc.hackwinds.views.SettingsActivity;

import java.util.ArrayList;

public class CameraModel {

    private static CameraModel mInstance;
    private Context mContext;
    private ArrayList<CameraChangedListener> mCameraChangedListeners;
    private boolean mForceReload;
    private MessagesCameraCameraMessage mDefaultCamera;
    private MessagesCameraCameraLocationsMessage mCameraLocations;

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
        mCameraChangedListeners = new ArrayList<>();
    }

    public MessagesCameraCameraMessage getDefaultCamera() {
        return mDefaultCamera;
    }

    public MessagesCameraCameraLocationsMessage getmCameraLocations() {
        return mCameraLocations;
    }

    public MessagesCameraCameraMessage getCamera(String regionName, String cameraName) {
        int regionIndex = getRegionIndex(regionName);
        if (regionIndex < 0) {
            return null;
        }

        MessagesCameraCameraRegionMessage region = mCameraLocations.getCameraLocations().get(regionIndex);
        for (MessagesCameraCameraMessage camera : region.getCameras()) {
            if (camera.getName().equals(cameraName)) {
                return camera;
            }
        }
        return null;
    }

    public MessagesCameraCameraMessage getCamera(int regionIndex, int cameraIndex) {
        try {
            return mCameraLocations.getCameraLocations().get(regionIndex).getCameras().get(cameraIndex);
        } catch (Exception e) {
            return null;
        }
    }

    public int getCameraRegionCount() {
        return mCameraLocations.getCameraLocations().size();
    }

    public String getRegionName(int index) {
        try {
            return mCameraLocations.getCameraLocations().get(index).getName();
        } catch (Exception e) {
            return "";
        }
    }

    public int getRegionIndex(String regionName) {
        for (int i = 0; i < mCameraLocations.getCameraLocations().size(); i++) {
            if (mCameraLocations.getCameraLocations().get(i).getName().equals(regionName)) {
                return i;
            }
        }

        return -1;
    }

    public int getCameraCount(String regionName) {
        return getCameraCount(getRegionIndex(regionName));
    }

    public int getCameraCount(int regionIndex) {
        try {
            return mCameraLocations.getCameraLocations().get(regionIndex).getCameras().size();
        } catch (Exception e) {
            return 0;
        }
    }

    public String getCameraName(int regionIndex, int cameraIndex) {
        try {
            return mCameraLocations.getCameraLocations().get(regionIndex).getCameras().get(cameraIndex).getName();
        } catch (Exception e) {
            return "";
        }
    }

    public void fetchCameras() {
        FetchCamerasTask fetchCamerasTask = new FetchCamerasTask(new FetchCamerasTask.CameraTaskListener() {
            @Override
            public void onFinished(MessagesCameraCameraLocationsMessage cameraLocations) {
                if (cameraLocations == null) {
                    for (CameraChangedListener listener : mCameraChangedListeners) {
                        if (listener != null) {
                            listener.cameraDataUpdateFailed();
                        }
                    }
                    return;
                }

                if (cameraLocations.getCameraLocations().size() < 1) {
                    for (CameraChangedListener listener : mCameraChangedListeners) {
                        if (listener != null) {
                            listener.cameraDataUpdateFailed();
                        }
                    }
                    return;
                }

                mCameraLocations = cameraLocations;
                mDefaultCamera = getCamera("Narragansett", "Warm Winds");
                for (CameraChangedListener listener : mCameraChangedListeners) {
                    if (listener != null) {
                        listener.cameraDataUpdated();
                    }
                }
            }
        });

        final Boolean premiumEnabled = PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(SettingsActivity.SHOW_PREMIUM_CONTENT_KEY, false);
        fetchCamerasTask.execute(premiumEnabled);
    }

    public void forceFetchCameras() {
        mForceReload = true;
        fetchCameras();
    }
}
