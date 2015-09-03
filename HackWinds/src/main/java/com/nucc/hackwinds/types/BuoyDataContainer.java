package com.nucc.hackwinds.types;


import java.util.ArrayList;

public class BuoyDataContainer {
    final static public int BUOY_DATA_POINTS = 20;

    public int BuoyID;
    public ArrayList<Buoy> BuoyData;
    public ArrayList<String> WaveHeights;

    public BuoyDataContainer() {
        // Initialize the capacity of the data structures
        BuoyData = new ArrayList<>(BUOY_DATA_POINTS);
        WaveHeights = new ArrayList<>(BUOY_DATA_POINTS);
    }

}
