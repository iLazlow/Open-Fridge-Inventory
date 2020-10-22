package com.ilazlow.fridgeinventory.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.ilazlow.fridgeinventory.Activity.MainActivity;
import com.ilazlow.fridgeinventory.Activity.ScanActivity;
import com.ilazlow.fridgeinventory.R;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class InventoryAdapter  extends ArrayAdapter<InventoryInfo> {
    MainActivity mainActivity;

    public InventoryAdapter(Context context, ArrayList<InventoryInfo> inventory, MainActivity mainActivity){
        super(context, 0, inventory);
        this.mainActivity = mainActivity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        InventoryInfo inventory = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.inventory_list_item, parent, false);
        }

        RelativeLayout mainRow = (RelativeLayout) convertView.findViewById(R.id.food_row_detail);
        TextView name = (TextView) convertView.findViewById(R.id.food_name);
        TextView value = (TextView) convertView.findViewById(R.id.food_value);
        TextView rec_value = (TextView) convertView.findViewById(R.id.food_rec_value);
        TextView updated = (TextView) convertView.findViewById(R.id.last_updated);
        ImageView foodImage = (ImageView) convertView.findViewById(R.id.food_image);
        MaterialButton deleteButton = (MaterialButton) convertView.findViewById(R.id.delete_item);

        View finalConvertView = convertView;
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new delInventory().execute(inventory.ean, inventory.fridge);
                mainActivity.reloadFridges();
                Snackbar.make(finalConvertView, mainActivity.getResources().getString(R.string.dialog_removed_msg, inventory.ean), Snackbar.LENGTH_SHORT).show();
            }
        });

        name.setText(inventory.name);
        value.setText(inventory.value);
        rec_value.setText(inventory.rec_value);

        if(inventory.last_update.equals("null")){
            updated.setText("");
        }else{
            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
            cal.setTimeInMillis(Long.parseLong(inventory.last_update) * 1000);
            String date = DateFormat.format("dd.MM.yyyy - HH:mm", cal).toString();
            updated.setText(convertView.getResources().getString(R.string.last_updated, date));
        }

        if(inventory.name.equals("null")){
            name.setText(inventory.ean);
        }else{
            name.setText(inventory.name + " (" + inventory.ean + ")");
        }

        if(inventory.image_thumb_url.equals("null")){
            foodImage.setImageDrawable(convertView.getResources().getDrawable(R.drawable.ph));
        }else{
            Glide.with(convertView).load(inventory.image_thumb_url).into(foodImage);
        }

        return convertView;
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    private class delInventory extends AsyncTask<String, Void, Boolean> {
        private Exception exception;

        protected Boolean doInBackground(String... ean) {
            try{
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                URL url = new URL(prefs.getString("API_URL", "NONE") + "/api/inventory/delete/" + ean[1]);
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
