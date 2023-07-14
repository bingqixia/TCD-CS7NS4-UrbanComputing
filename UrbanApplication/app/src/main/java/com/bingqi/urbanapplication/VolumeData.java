package com.bingqi.urbanapplication;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class VolumeData {
    public String currentTime;
    public double volume;

    public VolumeData() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public VolumeData(String currentTime, double volume) {
        this.currentTime = currentTime;
        this.volume = volume;
    }
}
