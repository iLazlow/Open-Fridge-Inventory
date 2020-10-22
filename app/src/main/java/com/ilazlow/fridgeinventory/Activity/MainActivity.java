package com.ilazlow.fridgeinventory.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;

import com.ilazlow.fridgeinventory.R;
import com.ilazlow.fridgeinventory.adapter.InventoryAdapter;
import com.ilazlow.fridgeinventory.adapter.InventoryInfo;
import com.ilazlow.fridgeinventory.utils.JSONParser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity {
    private static final int ZXING_CAMERA_PERMISSION = 1;
    private Class<?> mClss;
    private Spinner fridgeDropdown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        new loadFridges().execute();
        fridgeDropdown = findViewById(R.id.fridgeDropdown);
        fridgeDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Integer fridgeID = Integer.parseInt(fridgeDropdown.getItemAtPosition(position).toString().split("]")[0].replace("[", ""));

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                final SharedPreferences.Editor editor = prefs.edit();
                editor.putString("fridgeText", fridgeDropdown.getItemAtPosition(position).toString());
                editor.putInt("fridgeID", fridgeID);
                editor.apply();

                new loadInventory().execute(fridgeID.toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Button insert = findViewById(R.id.insert);
        insert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                final SharedPreferences.Editor editor = prefs.edit();
                editor.putString("scanMode", "in");
                editor.apply();

                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    mClss = ScanActivity.class;
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CAMERA}, ZXING_CAMERA_PERMISSION);
                } else {
                    Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                    startActivity(intent);
                }
            }
        });

        Button out = findViewById(R.id.out);
        out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                final SharedPreferences.Editor editor = prefs.edit();
                editor.putString("scanMode", "out");
                editor.apply();

                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    mClss = ScanActivity.class;
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CAMERA}, ZXING_CAMERA_PERMISSION);
                } else {
                    Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        new loadFridges().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        final SharedPreferences.Editor editor = prefs.edit();


        //noinspection SimplifiableIfStatement
        if (id == R.id.action_change_setup) {
            editor.putString("INIT_SETUP", "1");
            editor.apply();

            Intent intent = new Intent(MainActivity.this, SetupActivity.class);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String permissions[], int[] grantResults) {
        switch (requestCode) {
            case ZXING_CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(mClss != null) {
                        Intent intent = new Intent(this, mClss);
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(this, "Please grant camera permission to use the QR Scanner", Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }

    //Parse fridges
    private class loadFridges extends AsyncTask<String, String, Boolean> {
        private ArrayList<String> fridgeList;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... args) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

            JSONParser jParser = new JSONParser();
            fridgeList = new ArrayList<>();
            JSONObject json;

            try{
                json = jParser.getJSONFromUrl(prefs.getString("API_URL", "NONE") + "/api/fridges/all/", false, getBaseContext());
                JSONArray fridgeArray = json.getJSONArray("fridges");

                for(int i = 0; i < fridgeArray.length(); i++) {
                    JSONObject jObject = new JSONObject(fridgeArray.getString(i));
                    String id = jObject.getString("id");
                    String name = jObject.getString("name");

                    fridgeList.add("[" + id + "] " + name);
                }
            }catch (Exception e){
                Log.e("OFI", "Error: " + e.toString());
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean status) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.dropdown_menu_popup_item, fridgeList);
            fridgeDropdown.setAdapter(adapter);

            int fridgeVal = adapter.getPosition(prefs.getString("fridgeText", "0"));
            fridgeDropdown.setSelection(fridgeVal);
        }
    }

    //load inventory
    private class loadInventory extends AsyncTask<String, Void, ArrayList<String>> {
        private ArrayList<String> inventoryList;

        protected ArrayList<String> doInBackground(String... fridge) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            JSONParser jParser = new JSONParser();
            inventoryList = new ArrayList<>();
            JSONObject json;

            try{
                json = jParser.getJSONFromUrl(prefs.getString("API_URL", "NONE") + "/api/inventory/list/" + fridge[0], false, getBaseContext());
                JSONArray fridgeArray = json.getJSONArray("inventory");

                for(int i = 0; i < fridgeArray.length(); i++) {
                    JSONObject jObject = new JSONObject(fridgeArray.getString(i));
                    String id = jObject.getString("id");
                    String ean = jObject.getString("ean");
                    String value = jObject.getString("value");
                    String rec_value = jObject.getString("rec_value");
                    String timestamp = jObject.getString("timestamp");
                    String fridgeID = jObject.getString("fridge");
                    String last_update = jObject.getString("timestamp_update");
                    String name = jObject.getString("name");
                    String image_thumb_url = jObject.getString("image_thumb_url");

                    /*
                    JSONObject foodObject;
                    foodObject = new JSONObject(jObject.getString("openfooddata"));
                    */
                    inventoryList.add(id + "|" + name + "|" + ean + "|" + value + "|" + rec_value + "|" + timestamp + "|" + fridgeID + "|" + image_thumb_url + "|" + last_update);
                }
            }catch (Exception e){
                Log.e("OFI", "Error: " + e.toString());
            }

            return inventoryList;
        }

        protected void onPostExecute(ArrayList<String> inventoryList) {
            ArrayList<InventoryInfo> inventoryArray = new ArrayList<InventoryInfo>();
            InventoryAdapter adapter = new InventoryAdapter(MainActivity.this, inventoryArray, MainActivity.this);

            Iterator<String> it = inventoryList.iterator();
            while (it.hasNext()) {
                StringTokenizer item = new StringTokenizer(it.next(), "|");
                String id = item.nextToken();
                String name = item.nextToken();
                String ean = item.nextToken();
                String value = item.nextToken();
                String rec_value = item.nextToken();
                String timestamp = item.nextToken();
                String fridge = item.nextToken();
                String image_thumb_url = item.nextToken();
                String last_update = item.nextToken();

                adapter.add(new InventoryInfo(id, name, ean, value, rec_value, timestamp, fridge, image_thumb_url, last_update));
            }

            ListView listView = (ListView) findViewById(R.id.inventoryListView);
            listView.setAdapter(adapter);
        }
    }

    public void reloadFridges(){
        new loadFridges().execute();
    }
}