package io.jibon.apps.pbl2023;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.util.Objects;

public class CustomTools {
    protected SharedPreferences preferences;
    protected SharedPreferences.Editor preferencesEditor;
    protected Activity activity;
    public static String URL = "https://pbl2.barisal.org";

    public CustomTools(Activity activity) {
        this.activity = activity;
        this.preferences = PreferenceManager
                .getDefaultSharedPreferences(this.activity);
    }



    public int licenseTime(){
        String licenseTime = this.setPref("licenseTime", null);
        if (licenseTime.equals("")){
            return licenseTime(1667836800);
        }else{
            return Integer.parseInt(licenseTime);
        }
    }
    public int licenseTime(int licenseTime){
        this.setPref("licenseTime", String.valueOf(licenseTime));
        return licenseTime;
    }


    public String setPref(String id, String data) {
        String result = "";
        id = id;
        if (data != null) {
            preferencesEditor = preferences.edit();
            preferencesEditor.putString(id, data);
            preferencesEditor.commit();
        }
        if (!preferences.getString(id, "").equals("")) {
            result = preferences.getString(id, "");
            if (Objects.equals(result, "")) {
                result = "";
            }
        } else {
            preferencesEditor = preferences.edit();
            preferencesEditor.putString(id, "");
            preferencesEditor.commit();
        }
        return result;
    }

    public boolean toast(String text, Integer drawable, int color) {
        try {
            View view = activity.getLayoutInflater().inflate(R.layout.toast, activity.findViewById(R.id.Custom_toast), false);
            ((TextView) view.findViewById(R.id.Custom_toast_text)).setText(text);
            if (drawable != null) {
                ((ImageView) view.findViewById(R.id.Custom_toast_icon)).setImageResource(drawable);
                ((ImageView) view.findViewById(R.id.Custom_toast_icon)).setColorFilter(ContextCompat.getColor(activity, color), PorterDuff.Mode.SRC_IN);
            }
            Toast toast = new Toast(activity.getApplicationContext());
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(view);
            toast.show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                new CustomTools(activity).vibrate(1500);
            }
            return true;
        } catch (Exception e) {
            Log.e("errnos_ctool_a", "Custom Toast Problem: " + e);
            return false;
        }
    }
    public boolean toast(String text, Integer drawable){
        return toast(text, drawable, Color.GRAY);
    }
    public boolean toast(String text){
        return toast(text, null, 0);
    }
    public boolean toast(Integer text){
        return toast(String.valueOf(text), null);
    }

    public boolean vibrate(int milliseconds) {
        try {
            Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(milliseconds);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                vibrator.vibrate(Vibrator.VIBRATION_EFFECT_SUPPORT_YES);
            }
            return true;
        } catch (Exception e) {
            Log.e("errnos_ctool_b", "Vibrate Problem: " + e);
            return false;
        }
    }

//    public Float twoDecimal(Float number){
//
//    }

    public void alert(String title, String messages, int icon, int color) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        Drawable drawable = activity.getResources().getDrawable(icon);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawable.setTint(color);
        }
        builder.setTitle(title)
                .setMessage(messages)
                .setIcon(drawable)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.cancel();
                })
                .setCancelable(true);
        builder.create().show();
    }
    public void alert(String title, String messages, int icon){
        alert(title, messages, icon, Color.GRAY);
    }
    public void alert(String title, String messages){
        alert(title, messages, R.drawable.baseline_notifications_none_24, Color.GRAY);
    }
}