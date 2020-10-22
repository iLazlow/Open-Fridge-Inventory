package com.ilazlow.fridgeinventory.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;

import com.ilazlow.fridgeinventory.BuildConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class JSONParser {

    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";
    Integer fileLength = 0;
    Integer downloadProgress = 0;

    // constructor
    public JSONParser() {

    }

    public JSONObject getJSONFromUrl(String url, Boolean progressExport, Context mContext) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        // Making HTTP request
        try {
            URL urlObj = new URL(url);
            HttpURLConnection urlConnection = (HttpURLConnection) urlObj.openConnection();
            urlConnection.setRequestProperty("User-Agent", "Open-Fridge-Inventory/" + BuildConfig.VERSION_NAME + " (Linux; " + Build.MODEL + "/" + Build.VERSION.RELEASE.replace(";", "-") + "; " + Build.BRAND + ")");
            urlConnection.setRequestProperty("Authorization", "Bearer " + prefs.getString("API_KEY", "NONE"));
            is = urlConnection.getInputStream();
            fileLength = urlConnection.getContentLength();

            if(progressExport){
                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = is.read(data)) != -1) {
                    total += count;

                    if (fileLength > 0){
                        // only if total length is known
                        Log.e("SundataProgress", "Read buffer total: " + total);

                        // publishing the progress....
                        updateProgress((int) (total * 100 / fileLength));
                    }
                }
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            is.close();
            json = sb.toString();
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString() + " - URL: " + url);
        }

        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString() + " - URL: " + url);
        }

        // return JSON String
        return jObj;

    }

    public void updateProgress(Integer progress){
        Log.e("SundataProgress", "Progress updated: " + progress);
        downloadProgress = progress;
    }

    public Integer getProgress(){
        Log.e("SundataProgress", "Progress: " + downloadProgress);
        return downloadProgress;
    }

    public JSONObject getJSONFromString(String jsonString) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        json = jsonString;

        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }

        // return JSON String
        return jObj;

    }
}

