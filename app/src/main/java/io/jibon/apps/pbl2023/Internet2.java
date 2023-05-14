package io.jibon.apps.pbl2023;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.CookieManager;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class Internet2 extends AsyncTask<Void, Void, JSONObject> {
    private final TaskListener taskListener;
    private Activity context;
    public String url;
    private Integer code = 0;

    public Internet2(Activity context, String url, TaskListener listener) {
        this.context = context;
        this.url = url;
        this.taskListener = listener;
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        try {
            super.onPostExecute(result);
            if (this.taskListener != null) {
                this.taskListener.onFinished(code, result);
            }
        } catch (Exception e) {
            Log.e("errnos", e.toString());
        }
    }

    @Override
    protected JSONObject doInBackground(Void... params) {
        try {
            String lines = "";
            String allLines = "";
            URL newLink = new URL(url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) newLink.openConnection();
            // Fetch and set cookies in requests
            CookieManager cookieManager = CookieManager.getInstance();
            String cookie = cookieManager.getCookie(httpURLConnection.getURL().toString());
            if (cookie != null) {
                httpURLConnection.setRequestProperty("Cookie", cookie);
            }
//            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("User-Agent", "Android");
            httpURLConnection.connect();
            this.code = httpURLConnection.getResponseCode();
            // Get cookies from responses and save into the cookie manager
            List cookieList = httpURLConnection.getHeaderFields().get("Set-Cookie");
            if (cookieList != null) {
                for (Object cookieTemp : cookieList) {
                    cookieManager.setCookie(httpURLConnection.getURL().toString(), String.valueOf(cookieTemp));
                }
            }
            httpURLConnection.getErrorStream();
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuffer = new StringBuilder();
            while ((lines = bufferedReader.readLine()) != null) {
                stringBuffer.append(lines);
            }
            allLines = stringBuffer.toString();
            JSONObject jsonObject = new JSONObject(allLines);
            return jsonObject;
        } catch (Exception e) {
            Log.e("errnos", this.url + " - Internet2 error:" + e);
            return null;
        }
    }

    public interface TaskListener {
        void onFinished(Integer code, JSONObject result);
    }
}