package io.jibon.apps.pbl2023;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.CookieManager;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class BuyTicketSubmit extends AsyncTask<Void, Void, JSONObject> {
    private final TaskListener taskListener;
    private Activity context;
    private String url;
    private Integer code = 0;
    private String name;
    private String email;
    private String nidNumber;
    private String txnId;
    private Bitmap bitmapAvatar;
    private Bitmap bitmapNID;
    private String boundary = "*****";
    private String lineEnd = "\r\n";
    private String event_id;
    private String txnAmount;

    String twoHyphens = "--";



    public BuyTicketSubmit(Activity context, String url, String name, String email, String nidNumber, String txnId, String txnAmount,
                     Bitmap bitmapAvatar, Bitmap bitmapNID, String event_id, TaskListener listener) {
        this.context = context;
        this.url = url;
        this.name = name;
        this.email = email;
        this.nidNumber = nidNumber;
        this.txnId = txnId;
        this.bitmapAvatar = bitmapAvatar;
        this.bitmapNID = bitmapNID;
        this.taskListener = listener;
        this.event_id = event_id;
        this.txnAmount = txnAmount;
    }

    public interface TaskListener {
        void onFinished(Integer code, JSONObject result);
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
            String allLines = "";
            URL newLink = new URL(url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) newLink.openConnection();
            // Fetch and set cookies in requests
            CookieManager cookieManager = CookieManager.getInstance();
            String cookie = cookieManager.getCookie(httpURLConnection.getURL().toString());
            if (cookie != null) {
                httpURLConnection.setRequestProperty("Cookie", cookie);
            }
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("User-Agent", "Android");
            httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.connect();
            OutputStream outputStream = httpURLConnection.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            // add parameters
            addFormField("buy_ticket", event_id, dataOutputStream);
            addFormField("name", name, dataOutputStream);
            addFormField("email", email, dataOutputStream);
            addFormField("nidNumber", nidNumber, dataOutputStream);
            addFormField("txnId", txnId, dataOutputStream);
            addFormField("txnAmount", txnAmount, dataOutputStream);
            // add avatar image
            if (bitmapAvatar != null) {
                addFilePart("avatar",  bitmapAvatar, dataOutputStream);
            }
            // add NID image
            if (bitmapNID != null) {
                addFilePart("nid", bitmapNID, dataOutputStream);
            }
            dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            dataOutputStream.flush();
            dataOutputStream.close();
            outputStream.close();
            this.code = httpURLConnection.getResponseCode();
            // Get cookies from responses and save into the cookie manager
            List<String> cookieList = httpURLConnection.getHeaderFields().get("Set-Cookie");
            if (cookieList != null) {
                for (String cookieTemp : cookieList) {
                    cookieManager.setCookie(httpURLConnection.getURL().toString(), cookieTemp);
                }
            }
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            allLines = stringBuilder.toString();
            JSONObject jsonObject = new JSONObject(allLines);
            return jsonObject;
        } catch (Exception e) {
            Log.e("errnos", this.url + " - Internet2 error:" + e);
            return null;
        }
    }

    private void addFormField(String fieldName, String fieldValue, OutputStream outputStream) {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append(twoHyphens).append(boundary).append(lineEnd);
            builder.append("Content-Disposition: form-data; name=\"").append(fieldName).append("\"").append(lineEnd);
            builder.append(lineEnd);
            builder.append(fieldValue).append(lineEnd);

            outputStream.write(builder.toString().getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        }catch (Exception e){
            Log.e("errnos", e.getMessage());
        }
    }

    private void addFilePart(String paramName, Bitmap bitmap, OutputStream outputStream) {
        try {
            String fileName = "image.png";
            String contentType = "image/png";

            // Create the file part header
            StringBuilder sb = new StringBuilder();
            sb.append(twoHyphens).append(boundary).append(lineEnd);
            sb.append("Content-Disposition: form-data; name=\"").append(paramName).append("\"; filename=\"").append(fileName).append("\"").append(lineEnd);
            sb.append("Content-Type: ").append(contentType).append(lineEnd);
            sb.append(lineEnd);
            outputStream.write(sb.toString().getBytes());

            // Write the bitmap data to the output stream
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] imageData = baos.toByteArray();
            outputStream.write(imageData);

            // Add the closing boundary
            outputStream.write(lineEnd.getBytes());
            outputStream.write((twoHyphens + boundary + twoHyphens + lineEnd).getBytes());
        }catch (Exception e){
            Log.e("errnos", e.getMessage());
        }
    }



}