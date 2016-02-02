package com.nucc.hackwinds.listeners;


public interface ForecastChangedListener {
    void forecastDataUpdated();
    void forecastDataUpdateFailed();
}
