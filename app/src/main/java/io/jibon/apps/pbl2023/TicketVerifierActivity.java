package io.jibon.apps.pbl2023;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TicketVerifierActivity extends AppCompatActivity {
    public Activity activity;
    public String event_id, app_code;
    public EditText app_code_enter;

    private SurfaceView cameraPreview;
    private CameraSource cameraSource;
    private Boolean detected = false;

    public LinearLayout floating;
    public ImageView userAvatar;
    public TextView userName, userStatus;
    public Button btnDecline, btnGrant;
    public RelativeLayout beforeEnterCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;

        Bundle bundle = activity.getIntent().getExtras();
        if (bundle != null){
            setContentView(R.layout.activity_ticket_verifier);

            app_code_enter = activity.findViewById(R.id.app_code_enter);
            cameraPreview = findViewById(R.id.camera_preview);

            floating = activity.findViewById(R.id.floating);
            userAvatar = activity.findViewById(R.id.userAvatar);
            userName = activity.findViewById(R.id.userName);
            userStatus = activity.findViewById(R.id.userStatus);
            btnDecline = activity.findViewById(R.id.btnDecline);
            btnGrant = activity.findViewById(R.id.btnGrant);
            beforeEnterCode = activity.findViewById(R.id.beforeEnterCode);

            event_id = bundle.getString("event_id");
            app_code = bundle.getString("app_code");
            app_code_enter.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 4){
                        if (app_code.equals(s.toString())){
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(app_code_enter.getWindowToken(), 0);
                            app_code_enter.clearFocus();
                            app_code_enter.setClickable(false);
                            app_code_enter.setFocusable(false);
                            beforeEnterCode.setVisibility(View.VISIBLE);
                            app_code_enter.setVisibility(View.GONE);
                            startCameraSource();
                        }else{
                            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                            builder.setTitle("Sorry!")
                                    .setMessage("Wrong Code")
                                    .setPositiveButton("Exit", (dialog, which) -> {
                                        dialog.cancel();
                                        activity.finish();
                                    })
                                    .setCancelable(false);
                            builder.create().show();
                        }
                    }
                }
            });
        }
    }

    private void startCameraSource() {
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.QR_CODE).build();
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrCodes = detections.getDetectedItems();
                if (qrCodes.size() > 0) {
                    String result = qrCodes.valueAt(0).displayValue;
                    if (!detected){
                        detected = true;
                        String url = CustomTools.URL+"/json.php?get_ticket_data="+ URLEncoder.encode(result);
                        Internet2 internet2 = new Internet2(activity, url, (code, result1) -> {
                            try {
                                if (result1.has("get_ticket_data")) {
                                    result1 = result1.getJSONObject("get_ticket_data");
                                    String the_ticket_id = result1.getString("id");
                                    Log.e("errnos", String.valueOf(result1));
                                    floating.setVisibility(View.VISIBLE);
                                    Glide.with(activity)
                                            .load(CustomTools.URL + "/" + result1.getString("avatar"))
                                            .into(userAvatar);
                                    userName.setText(String.valueOf(result1.getString("name")));
                                    userStatus.setText("Status: "+String.valueOf(result1.getString("status")));
                                    btnDecline.setOnClickListener(v -> {
                                        detected = false;
                                        floating.setVisibility(View.GONE);
                                        Toast.makeText(activity, "Access Denied", Toast.LENGTH_LONG).show();
                                    });
                                    btnGrant.setOnClickListener(v -> {
                                        try {
                                            String url1 = CustomTools.URL + "/json.php?ticket_entered=" + URLEncoder.encode(the_ticket_id);
                                            Internet2 internet21 = new Internet2(activity, url1, (code1, result2) -> {
                                                try {
                                                    Log.e("errnos", String.valueOf(result2));
                                                    if (result2.has("ticket_entered")) {
                                                        if (result2.getBoolean("ticket_entered")) {
                                                            detected = false;
                                                            floating.setVisibility(View.GONE);
                                                            Toast.makeText(activity, "Access Granted", Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                } catch (Exception e) {
                                                    Log.e("errnos", e.getMessage());
                                                }
                                            });
                                            internet21.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                        }catch (Exception e){
                                            Log.e("errnos", e.getMessage());
                                        }
                                    });
                                }
                            }catch (Exception e){
                                Log.e("errnos", e.getMessage());
                            }
                        });
                        internet2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                }
            }
        });
        Resources resources = activity.getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int screeHeight = displayMetrics.heightPixels;

        cameraPreview.getLayoutParams().height = screenWidth;

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setAutoFocusEnabled(true)
                .setRequestedFps(60)
                .setRequestedPreviewSize(screenWidth, screenWidth)
                .build();

        cameraPreview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    cameraSource.start(cameraPreview.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.release();
                cameraSource.stop();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
            cameraSource.stop();
        }
    }
}