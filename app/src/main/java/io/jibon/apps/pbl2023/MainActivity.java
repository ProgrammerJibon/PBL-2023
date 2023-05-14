package io.jibon.apps.pbl2023;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    public Activity activity;

    public int permissionRequestCode = 41;
    public String[] permissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
//            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R?Manifest.permission.MANAGE_EXTERNAL_STORAGE:Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        setContentView(R.layout.activity_main);

        if (hasPermission()){
            openQrReader();
        }else{
            ActivityCompat.requestPermissions(activity, permissions, permissionRequestCode);
        }

    }

    public boolean hasPermission() {
        boolean allPermissionsGranted = true;
        for (String permission : this.permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                Log.e("errnos", String.valueOf(permission));
                break;
            }
        }
        return allPermissionsGranted;
    }

    protected void openQrReader(){
        startActivity(new Intent(this, QRCodeScannerActivity.class));
        activity.finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (hasPermission()){
            openQrReader();
        }else{
            Toast.makeText(activity, "All permission required", Toast.LENGTH_LONG).show();
            activity.finish();
        }

    }

}