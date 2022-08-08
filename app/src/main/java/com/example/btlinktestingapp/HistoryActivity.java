package com.example.btlinktestingapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.brother.ptouch.sdk.Printer;
import com.brother.ptouch.sdk.PrinterInfo;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HistoryActivity extends AppCompatActivity {

     static final String TAG = HistoryActivity.class.getSimpleName();
     ArrayList<HashMap<String,String>> ListOfHistoryData = new ArrayList<>();
    RecyclerView recycleier_history;
    HistoryRecyclerViewAdapter madapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        recycleier_history = (RecyclerView) findViewById(R.id.recyclerv_history);
        madapter = new HistoryRecyclerViewAdapter(this, ListOfHistoryData);
        recycleier_history.setAdapter(madapter);
        recycleier_history.setLayoutManager(new LinearLayoutManager(this));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (isConnecting()) {
            new getHistoryDetails().execute();
        } else {
            Toast.makeText(this, "Please check network connection", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public class getHistoryDetails extends AsyncTask<String, Void, String> {

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(HistoryActivity.this);
            pd.setMessage("Please wait...");
            pd.show();
        }

        protected String doInBackground(String... param) {
            String resp = "";

            try {

                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url(LaunchingActivity.API_GETHISTORY)
                        .get()
                        .addHeader("Content-Type", "application/json")
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

                //------------------------------

            } catch (Exception e) {
                e.printStackTrace();
                pd.cancel();
            }

            return resp;
        }


        @Override
        protected void onPostExecute(String result) {

            if (result != null && !result.isEmpty()) {

                try {
                    pd.cancel();
                    JSONObject jsonObject = new JSONObject(result);

                    String ResponceMessage = jsonObject.getString("ResponseMessage");
                    String ResponseText = jsonObject.getString("ResponseText");

                    if (ResponceMessage.equalsIgnoreCase("success")) {
                        //ListOfHistoryData.clear();
                        JSONArray jsonArray = jsonObject.getJSONArray("HardwareTestLinkObj");

                        for (int i=0; i< jsonArray.length(); i++){

                           JSONObject jobj = jsonArray.getJSONObject(i);
                           String BatchId = jobj.getString("BatchId");
                           String TestDateTime = jobj.getString("TestDateTime");
                           String UniqueLinkName = jobj.getString("UniqueLinkName");
                           String MacAddress = jobj.getString("MacAddress");


                           String TopPulserTestResult = jobj.getString("TopPulserTestResult");
                           String BottomPulserTestResult = jobj.getString("BottomPulserTestResult");

                            String LinkNameFromAPP = jobj.getString("LinkNameFromAPP");

                            HashMap<String, String> map = new HashMap<>();
                            map.put("BatchId", BatchId);
                            map.put("TestDateTime", TestDateTime);
                            map.put("UniqueLinkName", UniqueLinkName);
                            map.put("MacAddress", MacAddress);
                            map.put("TopPulserTestResult", TopPulserTestResult);
                            map.put("BottomPulserTestResult", BottomPulserTestResult);
                            map.put("LinkNameFromAPP", LinkNameFromAPP);

                            ListOfHistoryData.add(map);

                        }

                        madapter.notifyDataSetChanged();

                        Log.i(TAG, "API Call Success" + result);

                    } else {
                        Log.i(TAG, "API Call fail" + result);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    pd.cancel();
                }

            } else {
                pd.cancel();
                Log.i(TAG, " InPost Response err:" + result);
            }

        }
    }

    public boolean isConnecting() {
        boolean isConnected = false;

        ConnectivityManager connectivity = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {

            NetworkInfo activeNetwork = connectivity.getActiveNetworkInfo();
            isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return isConnected;
    }
}