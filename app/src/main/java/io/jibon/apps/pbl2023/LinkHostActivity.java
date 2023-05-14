package io.jibon.apps.pbl2023;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.Nullable;

import java.nio.charset.StandardCharsets;

public class LinkHostActivity extends Activity {
    public Activity activity;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;

        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();

        String result = appLinkData.getPath();

        if (result.toLowerCase().contains("/app/")) {
            result = result.split("/app/")[1];
            String[] data = (new String(Base64.decode(result, Base64.DEFAULT), StandardCharsets.UTF_8)).split("\n");
            if (data.length > 1) {
                String name = data[0];
                String value = data[1];

                if (name.equalsIgnoreCase("event_id")){
                    Log.e("errnos", name+":"+value);
                    Intent intent = new Intent(activity, BuyTicketActivity.class);
                    intent.putExtra("event_id", value);
                    activity.startActivity(intent);
                    activity.finish();
                }
            }
        }
    }
}
