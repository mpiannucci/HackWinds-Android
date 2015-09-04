package com.nucc.hackwinds.types;


import java.util.ArrayList;

public class BuoyDataContainer {
    final static public int BUOY_DATA_POINTS = 20;

    public int buoyID;
    public ArrayList<Buoy> buoyData;
    public ArrayList<String> waveHeights;

    public BuoyDataContainer(int id) {
        buoyID = id;
        buoyData = new ArrayList<>(BUOY_DATA_POINTS);
        waveHeights = new ArrayList<>(BUOY_DATA_POINTS);
    }

}
