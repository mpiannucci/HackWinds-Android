package com.nucc.hackwinds;

public class Tide {

    public Tide() {
        super();
        this.Time = new String[6];
        this.EventType = new String[6];
    }

    public void addDataItem(String eventType, String time, int pos) {
        if (this.EventType != null) {
            this.EventType[pos] = eventType;
            this.Time[pos] = time;
        }
    }

    String Day;
    String[] Time;
    String[] EventType;
}
