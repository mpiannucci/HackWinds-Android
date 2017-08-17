package com.nucc.hackwinds.types;


public class Camera {

    public String videoURL;
    public String imageURL;
    public String webURL;
    public String info;
    public boolean refreshable;
    public int refreshInterval;
    public boolean premium;

    private String mLocationName;
    private String mCameraName;

    public Camera(String locationName, String cameraName) {
        mLocationName = locationName;
        mCameraName = cameraName;
    }

    public String getLocationName() {
        return mLocationName;
    }

    public String getCameraName() {
        return mCameraName;
    }

    public boolean isVideoCamera() {
        return !videoURL.isEmpty();
    }

    public boolean isWebCamera() {
        return !webURL.isEmpty();
    }

    public boolean isImageOnlyCamera() {
        return !isVideoCamera() && !isWebCamera();
    }
}
