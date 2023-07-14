package com.bingqi.urbanapplication;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class GpsData {
    public String currentTime;
    public double latitude;
    public double longitude;

    public GpsData() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public GpsData(String currentTime, double latitude, double longitude) {
        this.currentTime = currentTime;
        this.latitude = latitude;
        this.longitude = longitude;
    }

}
