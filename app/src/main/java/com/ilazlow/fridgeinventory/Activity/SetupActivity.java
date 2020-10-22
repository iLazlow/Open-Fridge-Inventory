package com.ilazlow.fridgeinventory.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.ilazlow.fridgeinventory.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SetupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SetupActivity.this);
        final SharedPreferences.Editor editor = prefs.edit();
        final EditText api_url = findViewById(R.id.edit_api_url);
        final EditText api_key = findViewById(R.id.edit_api_key);

        api_url.setText(prefs.getString("API_URL", getResources().getString(R.string.api_url_field)));
        api_key.setText(prefs.getString("API_KEY", getResources().getString(R.string.api_key_field)));


        if(!prefs.getString("API_URL", "NONE").equals("NONE") && !prefs.getString("API_KEY", "NONE").equals("NONE") && prefs.getString("INIT_SETUP", "0").equals("0")){
            Intent intent = new Intent(SetupActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        Button insert = findViewById(R.id.finish_setup_btn);
        insert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor.putString("API_URL", api_url.getText().toString());
                editor.putString("API_KEY", api_key.getText().toString());
                editor.putString("INIT_SETUP", "0");
                editor.putString("SCAN_INSTALL", "0");
                editor.apply();

                Intent intent = new Intent(SetupActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_setup, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SetupActivity.this);
        final SharedPreferences.Editor editor = prefs.edit();


        //noinspection SimplifiableIfStatement
        if (id == R.id.action_scan_setup) {
            editor.putString("SCAN_INSTALL", "1");
            editor.putString("INIT_SETUP", "1");
            editor.apply();

            Intent intent = new Intent(SetupActivity.this, ScanActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}