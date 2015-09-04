package com.nucc.hackwinds.types;


import java.util.ArrayList;

public class ForecastDataContainer {
    final public static int CONDITION_DATA_COUNT = 30;
    final public static int FORECAST_DATA_COUNT = 10;

    public int forecastID;
    public ArrayList<Condition> conditions;
    public ArrayList<Forecast> forecasts;

    public ForecastDataContainer(int id) {
        forecastID = id;
        conditions = new ArrayList<>(CONDITION_DATA_COUNT);
        forecasts = new ArrayList<>(FORECAST_DATA_COUNT);
    }
}
