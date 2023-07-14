package com.bingqi.urbanapplication;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import pub.devrel.easypermissions.EasyPermissions;

public class BaseActivity extends AppCompatActivity {
    public void requestPermission(Activity context, String tip, int requestCode, String[] perms) {
        EasyPermissions.requestPermissions(context, tip, requestCode, perms);
    }
}
