package com.nucc.hackwinds;

/**
 * Created by matthew on 12/19/14.
 */
public class ConditionModel {
    private static ConditionModel ourInstance = new ConditionModel();

    public static ConditionModel getInstance() {
        return ourInstance;
    }

    private ConditionModel() {
    }
}
