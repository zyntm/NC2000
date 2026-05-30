package com.example.nc2000emulator;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Environment;

import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.pm.PackageManager;

public class PermissionManager {
    private final Context context;
    private final Activity activity;

    public PermissionManager(Activity activity) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
    }

    public boolean hasFileAccessPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Pre-M devices don't need runtime permissions
    }

    public String getRomPath() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.getExternalStorageDirectory() + "/wqx/";
        } else {
            return Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/wqx/";
        }
    }

    public boolean verifyRomFiles() {
        String romPath = getRomPath();
        java.io.File romDir = new java.io.File(romPath);
        return romDir.exists() && romDir.isDirectory();
    }
}
