package com.nucc.hackwinds.listeners;

import com.appspot.mpitester_13.station.model.ApiApiMessagesDataMessage;

public interface LatestBuoyFetchListener {
    void latestBuoyFetchSuccess(ApiApiMessagesDataMessage latestBuoy);
    void latestBuoyFetchFailed();
}
