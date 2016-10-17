package com.nucc.hackwinds.listeners;


public interface BuoyChangedListener {
    void buoyRefreshStarted();
    void buoyDataUpdated();
    void buoyDataUpdateFailed();
}
