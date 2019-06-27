package com.inuker.library;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by dingjikerbo on 17/8/16.
 */

public abstract class CameraActivity extends BaseActivity {

    private static final int REQUEST_CAMERA = 0x7378;

    private static final String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermission(this, PERMISSIONS, REQUEST_CAMERA);
    }

    protected abstract void onPermissionGranted();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAMERA) {
            if (resultCode != RESULT_OK) {
                finish();
            } else {
                dispatchPermissionGranted();
            }
        }
    }

    private void dispatchPermissionGranted() {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                onPermissionGranted();
            }
        }, 0);
    }

    private void requestPermission(Activity activity, String[] permissions, int request) {
        boolean allGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }
        if (!allGranted) {
            ActivityCompat.requestPermissions(activity,
                    permissions,
                    request);
        } else {
            dispatchPermissionGranted();
        }
    }
}
