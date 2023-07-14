package com.bingqi.urbanapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 10 meters
    private static final String VOLUME_DATA_PATH = "volume";
    private static final String GPS_DATA_PATH = "gps";

    // The minimum time between updates in milliseconds
//    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    private static final long MIN_TIME_BW_UPDATES = 1000; //1s
    private static final int MAX_DATA_LENGTH = 1200;

    public static SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    private static final int SAMPLE_RATE_IN_HZ = 8000;

    private String mInfo = "";
    private Location mLocation;
    private double mVolume = 0;
    private TextView tvLocationInfo;
    private TextView tvVolumeInfo;
    private LocationManager locationManager;
    private String mCurrentTime;
    private String mVolumeFileName;
    private String mGpsFileName;
    private ArrayList<String> mVolumeData;
    private ArrayList<String> mGpsData;

    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
            AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);

    AudioRecord mAudioRecord;
    boolean isGetVoiceRun;
    final Object mLock = new Object();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvLocationInfo = (TextView) findViewById(R.id.info_location);
        tvVolumeInfo = (TextView) findViewById(R.id.info_volume);

        String[] permissions = new String[]{
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.INTERNET
        };

        requestPermission(this, "Request Permission", 1, permissions);

//        prepareCsvFile();

        findViewById(R.id.start_record).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isGetVoiceRun = true;
                getNoiseLevel();
            }
        });

        findViewById(R.id.stop_record).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isGetVoiceRun = false;
            }
        });

//        get gps location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        updateLocation();
        // getting GPS status
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (isGPSEnabled) {
            Log.d("Gps", "Gps enabled");
//       request new location
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        mLocation = location;
                        updateLocation();
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                        mLocation = locationManager.getLastKnownLocation(provider);
                        updateLocation();
                    }

                    @Override
                    public void onProviderDisabled(String provider) {

                    }
                });

        }
    }

    private void prepareCsvFile() {

        mCurrentTime = sdf.format(System.currentTimeMillis());
        mVolumeFileName = "volume_" + mCurrentTime + ".csv";
        mGpsFileName = "gps_" + mCurrentTime + ".csv";

        ArrayList<String> volumeColumns = new ArrayList<>();
        volumeColumns.add("Time,Sound Level (DB)");
        CsvUtils.saveToCsv(mVolumeFileName, volumeColumns);

        ArrayList<String> gpsColumns = new ArrayList<>();
        gpsColumns.add("Time,Latitude,Longitude");
        CsvUtils.saveToCsv(mGpsFileName, gpsColumns);
    }

    public void updateLocation() {
        if (mLocation != null) {
            String currentTime = sdf.format(System.currentTimeMillis());

            double latitude = mLocation.getLatitude();
            double longitude = mLocation.getLongitude();

            GpsData gpsData = new GpsData(currentTime, latitude, longitude);
            DataUtils dataUtils = new DataUtils(GPS_DATA_PATH);
            dataUtils.writeData(currentTime, gpsData);

            StringBuffer sb = new StringBuffer();
            sb.append("[Location]\n");
            sb.append("Latitude: ");
            sb.append(mLocation.getLatitude());
            sb.append(", ");
            sb.append("Longitude: ");
            sb.append(mLocation.getLongitude());
            tvLocationInfo.setText(sb.toString());
            Log.d(TAG, "location: " + sb.toString());
        }
    }

    public void updateVolume() {
        StringBuffer sb = new StringBuffer();
        sb.append("[Volume] ");
        sb.append(mVolume);
        sb.append("  DB");
        tvVolumeInfo.setText(sb.toString());
    }

    public void getNoiseLevel() {

        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_IN_HZ,
                AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                mAudioRecord.startRecording();
                short[] buffer = new short[BUFFER_SIZE];
                while (isGetVoiceRun) {
                    int r = mAudioRecord.read(buffer, 0, BUFFER_SIZE);
                    long v = 0;
                    for (short value : buffer) {
                        v += value * value;
                    }
                    double mean = v / (double) r;
                    mVolume = 10 * Math.log10(mean);
                    updateVolume();

                    String currentTime = sdf.format(System.currentTimeMillis());
                    StringBuffer sb = new StringBuffer();
                    sb.append(currentTime);
                    sb.append(",");
                    sb.append(mVolume);
//                    only save 1000
//                    if (mVolumeData.size() < MAX_DATA_LENGTH) {
//                        mVolumeData.add(sb.toString());
//                    }

                    VolumeData volumeData = new VolumeData(currentTime, mVolume);
                    DataUtils dataUtils = new DataUtils(VOLUME_DATA_PATH);
                    dataUtils.writeData(currentTime, volumeData);

                    Log.d(TAG, "volume record:\n" + sb.toString());

                    synchronized (mLock) {
                        try {
                            mLock.wait(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                mAudioRecord.stop();
                mAudioRecord.release();
                mAudioRecord = null;
            }
        }).start();
    }

}