package com.nucc.hackwinds.listeners;

import com.nucc.hackwinds.types.Buoy;

public interface LatestBuoyFetchListener {
    void latestBuoyFetchSuccess(Buoy latestBuoy);
    void latestBuoyFetchFailed();
}
