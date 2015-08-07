package com.nucc.hackwinds.models;


import android.util.Log;

import com.nucc.hackwinds.types.Camera;
import com.nucc.hackwinds.utilities.ServiceHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class CameraModel {

    // Constants
    private final String HACKWINDS_API_URL = "http://blog.mpiannucci.com/static/API/hackwinds_camera_locations_v3.json";

    // Member variables
    public HashMap<String, HashMap<String, Camera> > cameraLocations;
    public ArrayList<String> locationKeys;
    public ArrayList<ArrayList<String> > cameraKeys;
    public int cameraCount;
    public int locationCount;

    private static CameraModel mInstance;
    private boolean mForceReload;

    public static CameraModel getInstance() {
        if (mInstance == null) {
            mInstance = new CameraModel();
        }
        return mInstance;
    }

    private CameraModel() {
        // Initialize the context
        mForceReload = true;
        cameraLocations = new HashMap<>();
        locationKeys = new ArrayList<>();
        cameraKeys = new ArrayList<>();
        cameraCount = 0;
        locationCount = 0;
    }

    public boolean fetchCameraURLs() {
        if (!mForceReload) {
            return true;
        }

        ServiceHandler sh = new ServiceHandler();
        String rawData = sh.makeServiceCall(HACKWINDS_API_URL, ServiceHandler.GET);

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
                while(cameraIterator.hasNext()) {
                    String cameraName = cameraIterator.next();
                    JSONObject thisCameraObject = locationObject.getJSONObject(cameraName);

                    // Create the new camera object for the camera
                    Camera thisCamera = new Camera();

                    // If its point judith then do some magic
                    if (cameraName.equals("Point Judith")) {
                        thisCamera = fetchPointJudithURLS(thisCameraObject.getString("Info"));
                    } else {
                        thisCamera.VideoURL = thisCameraObject.getString("Video");
                    }

                    // For now everything else is common
                    thisCamera.ImageURL = thisCameraObject.getString("Image");
                    thisCamera.Refreshable = thisCameraObject.getBoolean("Refreshable");
                    thisCamera.RefreshInterval = Integer.valueOf(thisCameraObject.getString("RefreshInterval"));

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

        mForceReload = false;
        return true;
    }

    public boolean forceFetchCameraURLs() {
        mForceReload = true;
        return fetchCameraURLs();
    }

    private Camera fetchPointJudithURLS(String pjURL) {
        Camera pjCamera = new Camera();

        try {
            // Get teh point judith camera data from Surfline
            ServiceHandler sh = new ServiceHandler();
            String rawPJData = sh.makeServiceCall(pjURL, ServiceHandler.GET);
            JSONObject pjResp = new JSONObject(rawPJData);
            JSONObject pjStreamData = (JSONObject)pjResp.getJSONObject("streamInfo").getJSONArray("stream").get(0);

            // Rip the json data into the camera object
            pjCamera.VideoURL = pjStreamData.getString("file");
            pjCamera.Info = String.format("Camera Status: %s\nDate: %s\nTime: %s\n If the video does not play, the camrea may be down. It is a daily upload during the summer and it becomes unavailable each evening.",
                    pjStreamData.getString("camStatus"),
                    pjStreamData.getString("reportDate"),
                    pjStreamData.getString("reportTime"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Send the camera back to the user
        return pjCamera;
    }
}
