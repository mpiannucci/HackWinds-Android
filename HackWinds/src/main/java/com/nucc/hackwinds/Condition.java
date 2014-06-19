package com.nucc.hackwinds;

public class Condition {

    public enum ConditionTypes {
        WAVEHEIGHT,
        WIND,
        SWELL,
        TIDE
    }

    public Condition(ConditionTypes condition, String[] text) {
        // Call the constructor
        super();
        this.condition = condition;
        this.text = text;
    }

    public ConditionTypes condition;
    public String[] text;
}