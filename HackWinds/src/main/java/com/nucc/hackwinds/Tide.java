package com.nucc.hackwinds;

public class Tide {

    public Tide(String day) {
        // Call the constructor
        super();
        this.day = day;
        this.dType = new String[6];
        this.dValue = new String[6];
    }

    public void addDataItem(String dataType, String dataValue, int pos) {
        this.dType[pos] = dataType;
        this.dValue[pos] = dataValue;
    }

    String[] dValue;
    String[] dType;
    String day;
}
