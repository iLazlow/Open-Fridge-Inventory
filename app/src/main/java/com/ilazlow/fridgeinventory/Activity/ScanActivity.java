package com.ilazlow.fridgeinventory.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.zxing.Result;
import com.ilazlow.fridgeinventory.R;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.content.ContentValues.TAG;

public class ScanActivity extends Activity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;
    private String scanMode = "in";
    private Integer fridgeID = 1;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ScanActivity.this);
        scanMode = prefs.getString("scanMode", "in");
        fridgeID = prefs.getInt("fridgeID", 1);
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ScanActivity.this);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString("SCAN_INSTALL", "0");
        editor.apply();
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        Log.v(TAG, rawResult.getText()); // Prints scan results
        Log.v(TAG, rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ScanActivity.this);
        final SharedPreferences.Editor editor = prefs.edit();
        String ean = rawResult.getText();

        if(prefs.getString("SCAN_INSTALL", "0").equals("1")){
            editor.putString("API_URL", ean.split("\\|")[0]);
            editor.putString("API_KEY", ean.split("\\|")[1]);
            editor.apply();

            Intent intent = new Intent(ScanActivity.this, SetupActivity.class);
            startActivity(intent);
            finish();
        }else{
            if (rawResult.getBarcodeFormat().toString().toLowerCase().contains("ean")) {
                String dialogTitle;
                String dialogMessage;

                if (scanMode.equals("in")) {
                    dialogTitle = getResources().getString(R.string.dialog_saved);
                    dialogMessage = getResources().getString(R.string.dialog_saved_msg, ean);
                } else {
                    dialogTitle = getResources().getString(R.string.dialog_removed);
                    dialogMessage = getResources().getString(R.string.dialog_removed_msg, ean);
                }

                new AlertDialog.Builder(ScanActivity.this)
                        .setTitle(dialogTitle)
                        .setMessage(dialogMessage)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Continue with delete operation
                                mScannerView.resumeCameraPreview(ScanActivity.this);

                                if (scanMode.equals("in")) {
                                    new putInventory().execute(ean);
                                } else {
                                    new delInventory().execute(ean);
                                }
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }else{
                mScannerView.resumeCameraPreview(ScanActivity.this);
            }
        }
    }

    private class putInventory extends AsyncTask<String, Void, Boolean> {
        private Exception exception;

        protected Boolean doInBackground(String... ean) {
            try{
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ScanActivity.this);
                URL url = new URL(prefs.getString("API_URL", "NONE") + "/api/inventory/put/" + fridgeID);
                HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
                httpCon.setDoOutput(true);
                httpCon.setRequestMethod("PUT");
                httpCon.setRequestProperty("Authorization", "Bearer " + prefs.getString("API_KEY", "NONE"));
                httpCon.setRequestProperty("Content-Type", "application/json");
                OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
                out.write("{\"ean\": \"" + ean[0] + "\"}");
                out.close();

                InputStream in = httpCon.getInputStream();
                InputStreamReader isw = new InputStreamReader(in);
                int data = isw.read();
                while (data != -1) {
                    char current = (char) data;
                    data = isw.read();
                    System.out.print(current);
                }
                Log.e("ofi", "send to " + prefs.getString("API_URL", "NONE") + "/api/inventory/put/" + fridgeID + " ean code " + ean[0]);

                return true;
            }catch (Exception e){
                Log.e(TAG, "Got error on api put: " + e.toString()); // Prints scan results
                return false;
            }
        }

        protected void onPostExecute(Boolean feed) {
            // TODO: check this.exception
            // TODO: do something with the feed
        }
    }

    private class delInventory extends AsyncTask<String, Void, Boolean> {
        private Exception exception;

        protected Boolean doInBackground(String... ean) {
            try{
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ScanActivity.this);
                URL url = new URL(prefs.getString("API_URL", "NONE") + "/api/inventory/delete/" + fridgeID);
                HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
                httpCon.setDoOutput(true);
                httpCon.setRequestMethod("DELETE");
                httpCon.setRequestProperty("Authorization", "Bearer " + prefs.getString("API_KEY", "NONE"));
                httpCon.setRequestProperty("Content-Type", "application/json");
                OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
                out.write("{\"ean\": \"" + ean[0] + "\"}");
                out.close();

                InputStream in = httpCon.getInputStream();
                InputStreamReader isw = new InputStreamReader(in);
                int data = isw.read();
                while (data != -1) {
                    char current = (char) data;
                    data = isw.read();
                    System.out.print(current);
                }

                return true;
            }catch (Exception e){
                Log.e(TAG, "Got error on api put: " + e.toString()); // Prints scan results
                return false;
            }
        }

        protected void onPostExecute(Boolean feed) {
            // TODO: check this.exception
            // TODO: do something with the feed
        }
    }
}
