package com.example.nc2000emulator;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private EmulatorView emulatorView;
    private PermissionManager permissionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissionManager = new PermissionManager(this);

        // Check if we have file access permission
        if (hasFileAccessPermission()) {
            initializeEmulator();
        } else {
            requestFileAccessPermission();
        }
    }

    private boolean hasFileAccessPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            return ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestFileAccessPermission() {
        Intent intent = new Intent(this, PermissionActivity.class);
        startActivity(intent);
    }

    private void initializeEmulator() {
        setContentView(R.layout.activity_main);
        FrameLayout container = findViewById(R.id.emulator_container);

        emulatorView = new EmulatorView(this);
        container.addView(emulatorView);

        // Load native library
        System.loadLibrary("nc2000-emulator");

        // Initialize the emulator
        emulatorView.initializeEmulator();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeEmulator();
            }
        }
    }
}
