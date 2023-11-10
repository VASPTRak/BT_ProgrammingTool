package com.example.btlinktestingapp;

import static java.lang.Thread.sleep;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PulserTestWifiActivity extends AppCompatActivity implements View.OnClickListener {

    Button btn_save_batchid, btn_go_to_top, btn_go_to_bottom, btn_finish, btn_continue, btn_print3;
    EditText edt_batch_number;
    String batchID="", savedBatchID = "", TopTestResult = "F", BottomTestResult = "F";
    ConstraintLayout layout_prepare_toptest, layout_toptest, layout_prepare_bottomtest, layout_bottomtest;
    private static final String TAG = PulserTestWifiActivity.class.getSimpleName();
    private static final int REQUEST_WIFI_SETTINGS = 123;
    String consoleString = "", pulsar_status = "", counts = "", mac_address = "", link_name="", value = "", TestcaseId = "";
    String HTTP_URL = "http://192.168.4.1:80/";
    String URL_INFO = HTTP_URL + "client?command=info";
    String URL_RELAY = HTTP_URL + "config?command=relay";
    String URL_GET_PULSAR = HTTP_URL + "client?command=pulsar ";

    boolean BRisWiFiConnected = false, isStopTimer = false, isDisconnected=false, isRelayOffSent = false;
    String jsonRelayOff = "{\"relay_request\":{\"Password\":\"12345678\",\"Status\":0}}";
    int CNT_LAST = 0;
    boolean stopTimer = true, isRelayOff=false, isBottomTest = false,isRelayOffCalledB=false;
    public static Timer qtyTimer;
    TextView tv_qty, tv_qtyb;
    String jsonRelayOn = "{\"relay_request\":{\"Password\":\"12345678\",\"Status\":1}}";
    private Handler handler = new Handler();







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pulser_test_wifi);

        btn_save_batchid = (Button) findViewById(R.id.btn_save_batchid);
        edt_batch_number = (EditText) findViewById(R.id.edt_batch_number);
        layout_prepare_toptest = (ConstraintLayout) findViewById(R.id.layout_prepare_toptest);
        layout_toptest = (ConstraintLayout) findViewById(R.id.layout_toptest);
        layout_prepare_bottomtest = (ConstraintLayout) findViewById(R.id.layout_prepare_bottomtest);
        layout_bottomtest = (ConstraintLayout) findViewById(R.id.layout_bottomtest);
        btn_finish = (Button) findViewById(R.id.btn_finish);
        btn_continue = (Button) findViewById(R.id.btn_continue);
        btn_print3 = (Button) findViewById(R.id.btnPrint3);


        btn_go_to_top = (Button) findViewById(R.id.btn_go_to_top);
        btn_go_to_bottom = (Button) findViewById(R.id.btn_go_to_bottom);
        tv_qty = (TextView) findViewById(R.id.tv_qty);
        tv_qtyb = (TextView) findViewById(R.id.tv_qtyb);
        btn_save_batchid.setOnClickListener(this);
        btn_go_to_top.setOnClickListener(this);
        btn_go_to_bottom.setOnClickListener(this);
        btn_finish.setOnClickListener(this);
        btn_continue.setOnClickListener(this);
        btn_print3.setOnClickListener(this);


        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
        }

        SharedPreferences sharedPref = PulserTestWifiActivity.this.getSharedPreferences("PulseValue", Context.MODE_PRIVATE);
        value = sharedPref.getString("Pulses", value);
        TestcaseId = sharedPref.getString("TestCaseId",TestcaseId);



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("onDestroy called!");
        isStopTimer = true;
        if(qtyTimer!=null) {
            qtyTimer.cancel();
        }


    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_save_batchid:


                String bid = edt_batch_number.getText().toString().trim();
                batchID = bid.toUpperCase();

                SharedPreferences sharedPrefbatchID = PulserTestWifiActivity.this.getSharedPreferences("saveBatchID", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPrefbatchID.edit();


                if (sharedPrefbatchID.contains("batchID")) {
                    savedBatchID = sharedPrefbatchID.getString("batchID", savedBatchID);
                    if (!savedBatchID.contains(batchID)) {
                        editor.putString("batchID", savedBatchID + "," + batchID);
                    }
                } else {
                    editor.putString("batchID", batchID);
                }
                editor.apply();


                if (validatebatchid(batchID)) {
                    //Server call...
                    if (isConnecting()) {
                        try {
                            new PulserTestWifiActivity.checkbatchidunqiue().execute(batchID);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {
                        Toast.makeText(this, "Please check network connection", Toast.LENGTH_LONG).show();
                        AppCommon.WriteInFile(PulserTestWifiActivity.this, TAG + " Please check network connection");
                    }
                }


                break;

            case R.id.btn_go_to_top:
//                CurrentTest = 1;
//                CurrentQty = 0;
//                previousQty = 0;
//                finalQty = 0;
                layout_prepare_toptest.setVisibility(View.GONE);
                layout_toptest.setVisibility(View.VISIBLE);
                new CommandsGETRELAY().execute(URL_RELAY);
                break;

            case R.id.btn_go_to_bottom:
                layout_prepare_bottomtest.setVisibility(View.GONE);
                layout_bottomtest.setVisibility(View.VISIBLE);
                isBottomTest = true;
                stopTimer = true;
                new CommandsGETRELAYB().execute(URL_RELAY);
                break;

            case R.id.btn_finish:
                AppCommon.isbtnContinuePressed = "False";
//                batchIDList.clear();
//                SharedPreferences sharedPrefbatchID2 = PulsarTestActivity.this.getSharedPreferences("saveBatchID", Context.MODE_PRIVATE);
//                SharedPreferences.Editor editor1 = sharedPrefbatchID2.edit();
//                editor1.clear();
//                editor1.apply();
                Intent i = new Intent(PulserTestWifiActivity.this, LaunchingActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                break;

            case R.id.btn_continue:
                AppCommon.isbtnContinuePressed="True";
                Intent intent = new Intent(PulserTestWifiActivity.this, link_selected.class);
                startActivity(intent);
                break;





        }
    }

    private boolean validatebatchid(String batchID) {


        if (batchID == null || batchID.equals("")) {
            edt_batch_number.setError("required");
            edt_batch_number.setFocusable(true);
            return false;
        } else {

            int str_len = batchID.length();
            String ch_end = "A";
            String ch_start = String.valueOf(batchID.charAt(0));

            if (str_len > 1)
                ch_end = String.valueOf(batchID.charAt(str_len - 1));

            if (str_len < 2) {
                edt_batch_number.setError("Batch Id too short");
                edt_batch_number.setFocusable(true);
                Toast.makeText(this, "Batch Id too short", Toast.LENGTH_LONG).show();
                return false;
            } else if (!Pattern.matches("[a-zA-Z]", ch_start)) {
                edt_batch_number.setError("Enter correct Batch Id");
                edt_batch_number.setFocusable(true);
                return false;
            } else if (!Pattern.matches("[0-9]", ch_end)) {  //[0-9]
                edt_batch_number.setError("Enter correct Batch Id");
                edt_batch_number.setFocusable(true);
                return false;
            }
        }

        return true;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    public class checkbatchidunqiue extends AsyncTask<String, Void, String> {


        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(PulserTestWifiActivity.this);
            pd.setMessage("Please wait...");
            pd.show();
        }

        protected String doInBackground(String... param) {
            String resp = "";

            try {

                EntityClass entityClass = new EntityClass();
                entityClass.BatchId = param[0];

                Gson gson = new Gson();
                final String jsonData = gson.toJson(entityClass);

                Log.i(TAG, "JsonData server call:" + jsonData);

                OkHttpClient client = new OkHttpClient();
                MediaType JSON = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(JSON, jsonData);

                Request request = new Request.Builder()
                        .url(LaunchingActivity.API_UNIQUE_ID)
                        .post(body)
                        .addHeader("Content-Type", "application/json")
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

                //------------------------------

            } catch (Exception e) {
                onSuccessBatchId(false, "Something went wrong. Please try again.");
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
                        Log.i(TAG, "check batchid unqiue Call Success" + result);
                        onSuccessBatchId(true, ResponseText);

                    } else {
                        onSuccessBatchId(false, ResponseText);
                        Log.i(TAG, "check batchid unqiue Call fail" + result);

                    }

                } catch (JSONException e) {
                    onSuccessBatchId(false, "Something went wrong. Please try agian.");
                    e.printStackTrace();
                    pd.cancel();
                }

            } else {
                onSuccessBatchId(false, "Something went wrong. Please try agian.");
                pd.cancel();
                Log.i(TAG, "GenerateFilesAPI InPost Response err:" + result);
            }

        }
    }

    private void onSuccessBatchId(boolean b, String responseText) {


        if (b) {
//            if (QR_ReaderStatus.equalsIgnoreCase("QR Connected")) {
//
//                FDCheck();
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                //startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), REQUEST_WIFI_SETTINGS);



//
//            } else {
//                Toast.makeText(this, "Please wait. Connecting..", Toast.LENGTH_LONG).show();
//                AppCommon.WriteInFile(PulserTestWifiActivity.this, TAG + " Status: " + QR_ReaderStatus);
//            }

        } else {
            //AppCommon.colorToastBigFont(PulsarTestActivity.this,responseText,Color.RED);
            Toast.makeText(this, responseText, Toast.LENGTH_LONG).show();
        }


    }

    @Override
    protected void onResume() {
        super.onResume();

        if(validatebatchid(batchID)) {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ssid = wifiInfo.getSSID(); // Get the SSID of the currently connected Wi-Fi network

            if (checkLinkNameAndSelectedHoseNameEqual(ssid)) {

                edt_batch_number.setVisibility(View.GONE);
                btn_save_batchid.setVisibility(View.GONE);
                layout_prepare_toptest.setVisibility(View.VISIBLE);
                hideKeyboard(PulserTestWifiActivity.this);
                new CommandsGET().execute(URL_INFO);
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

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    public boolean checkLinkNameAndSelectedHoseNameEqual(String wifiSSID) {
        String ssidWO = wifiSSID.replace("\"", "").trim();

        if (ssidWO.trim().equalsIgnoreCase(AppCommon.selectedSSID.trim())) {
            return true;
        } else {
            return false;
        }
    }

    public class CommandsGET extends AsyncTask<String, Void, String> {

        public String resp = "";

       // ProgressDialog pd;

        @Override
        protected void onPreExecute() {
//            pd = new ProgressDialog(PulsarTestActivity.this);
//            pd.setMessage(getResources().getString(R.string.plzwait));
//            pd.setCancelable(false);
        }

        protected String doInBackground(String... param) {


            try {

                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(15, TimeUnit.SECONDS)
                        .readTimeout(15, TimeUnit.SECONDS)
                        .writeTimeout(15, TimeUnit.SECONDS)
                        .build();

                Request request = new Request.Builder()
                        .url(param[0])
                        .build();
                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (SocketException se){
                //StoreLinkDisconnectInfo(se);
                Log.d("Ex",se.getMessage());
            }catch (Exception e) {
                Log.d("Ex", e.getMessage());
                //AppConstants.WriteinFile(DisplayMeterActivity.this, "GET CMD Error: " + e.getMessage());
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {

            //pd.dismiss();
            try {

                consoleString += "OUTPUT- " + result + "\n";


                System.out.println(result);
                getFirmwareVersionByInfoCommand(result);

            } catch (Exception e) {

                System.out.println(e);
            }

        }
    }

    public void getFirmwareVersionByInfoCommand(String resultinfo) {

        try {

            System.out.println("ResultInfo***********-" + resultinfo);
            if (resultinfo.trim().startsWith("{") && resultinfo.trim().contains("Version")) {

                JSONObject jsonObj = new JSONObject(resultinfo);
                String userData = jsonObj.getString("Version");
                JSONObject jsonObject = new JSONObject(userData);
                mac_address = jsonObject.getString("mac_address");

                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                link_name = wifiInfo.getSSID().trim();
                link_name = link_name.replaceAll("\"", "");
                //AppCommon.selectedSSID = link_name;


            }

        } catch (Exception e) {

        }


    }

    public class CommandsGETRELAY extends AsyncTask<String, Void, String> {

        public String resp = "";


        protected String doInBackground(String... param) {


            try {

                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(15, TimeUnit.SECONDS)
                        .readTimeout(15, TimeUnit.SECONDS)
                        .writeTimeout(15, TimeUnit.SECONDS)
                        .build();

                Request request = new Request.Builder()
                        .url(param[0])
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (SocketException se){
                Log.d("CommandsGetRELAY",se.getMessage());
            }catch (Exception e) {
                Log.d("CommandsGetRELAY", e.getMessage());
                //AppConstants.WriteinFile(DisplayMeterActivity.this, "GET RELAY: " + e.getMessage());
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String relayResponse) {


            try {

                    System.out.println("relayResponse:" + relayResponse);

                    String relay_status = "";
                    JSONObject jsonO = new JSONObject(relayResponse);
                    String userData = jsonO.getString("relay_response");
                    JSONObject jsonO2 = new JSONObject(userData);
                    relay_status = jsonO2.getString("status");

                    System.out.println("relay_status:" + relay_status);

                    if (relay_status.equalsIgnoreCase("0")) {
                        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        String ssid = wifiInfo.getSSID();

                        if (checkLinkNameAndSelectedHoseNameEqual(ssid)) {

                            BRisWiFiConnected = true;

                            HashMap<String, String> imap = new HashMap<>();
                            imap.put("jsonData", "");

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //AppConstants.WriteinFile(DisplayMeterActivity.this, "Sending URL_RELAY - ON ");
                                    new CommandsPOST().execute(URL_RELAY, jsonRelayOn);


                                }
                            }, 1000);

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //btnStop.setVisibility(View.VISIBLE);
                                    startQuantityInterval();
                                }
                            }, 3500);

                        } else {
                            //AppConstants.colorToastBigFont(DisplayMeterActivity.this, "Please wait\nConnecting to '" + AppConstants.LAST_CONNECTED_SSID + "'...", Color.BLUE);

                        }
                    }
                } catch(Exception e){

                    System.out.println("CommandsGetRelay PostExecute Ex:"+ e.getMessage());
                }

            }
    }

    public class CommandsGETRELAYB extends AsyncTask<String, Void, String> {

        public String resp = "";


        protected String doInBackground(String... param) {


            try {

                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(15, TimeUnit.SECONDS)
                        .readTimeout(15, TimeUnit.SECONDS)
                        .writeTimeout(15, TimeUnit.SECONDS)
                        .build();

                Request request = new Request.Builder()
                        .url(param[0])
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (SocketException se){
                Log.d("CommandsGetRELAY",se.getMessage());
            }catch (Exception e) {
                Log.d("CommandsGetRELAY", e.getMessage());
                //AppConstants.WriteinFile(DisplayMeterActivity.this, "GET RELAY: " + e.getMessage());
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String relayResponse) {


            try {

                System.out.println("relayResponse:" + relayResponse);

                String relay_status = "";
                JSONObject jsonO = new JSONObject(relayResponse);
                String userData = jsonO.getString("relay_response");
                JSONObject jsonO2 = new JSONObject(userData);
                relay_status = jsonO2.getString("status");

                System.out.println("relay_status:" + relay_status);

                if (relay_status.equalsIgnoreCase("0")) {
                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    String ssid = wifiInfo.getSSID();

                    if (checkLinkNameAndSelectedHoseNameEqual(ssid)) {

                        BRisWiFiConnected = true;

                        HashMap<String, String> imap = new HashMap<>();
                        imap.put("jsonData", "");

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //AppConstants.WriteinFile(DisplayMeterActivity.this, "Sending URL_RELAY - ON ");
                                new CommandsPOST().execute(URL_RELAY, jsonRelayOn);


                            }
                        }, 1000);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //btnStop.setVisibility(View.VISIBLE);
                                startQuantityInterval();
                            }
                        }, 3500);

                    } else {
                        //AppConstants.colorToastBigFont(DisplayMeterActivity.this, "Please wait\nConnecting to '" + AppConstants.LAST_CONNECTED_SSID + "'...", Color.BLUE);

                    }
                }
            } catch(Exception e){

                System.out.println("CommandsGetRelay PostExecute Ex:"+ e.getMessage());
            }

        }
    }

    public void startQuantityInterval() {
        qtyTimer = new Timer();

        qtyTimer.schedule(new TimerTask() {
            @Override
            public void run() {

                try {

                    if (stopTimer) {
                         System.out.println("Sending GET PULSAR command ");
                         new GETPulsarQuantity().execute(URL_GET_PULSAR);
                        }

                } catch (Exception e) {
                    System.out.println("startQuantityInterval Ex"+e.getMessage());
                }

            }
        }, 0, 2000);


    }

    public class GETPulsarQuantity extends AsyncTask<String, Void, String> {

        public String resp = "";


        protected String doInBackground(String... param) {


            try {
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(15, TimeUnit.SECONDS)
                        .readTimeout(15, TimeUnit.SECONDS)
                        .writeTimeout(15, TimeUnit.SECONDS)
                        .build();

                Request request = new Request.Builder()
                        .url(param[0])
                        .build();

                Response response = client.newCall(request).execute();
                //final int statusCode = response.code();
                resp = response.body().string();

                response.body().close();

            } catch (SocketException se){
                Log.d("GETPulsarQuantity Ex",se.getMessage());
            }catch (Exception e) {
                resp = e.getMessage();
                Log.d("GETPulsarQuantity Ex", e.getMessage());
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                    consoleString += "OUTPUT- " + result + "\n";

                    System.out.println(result);

                    if (stopTimer)
                        pulsarQtyLogic(result);

            } catch (Exception e) {

                System.out.println(e);
            }

        }
    }

    public void pulsarQtyLogic(String result) {

        try {
            if (result.contains("pulsar_status")) {

                JSONObject jsonObject = new JSONObject(result);
                JSONObject joPulsarStat = jsonObject.getJSONObject("pulsar_status");
                counts = joPulsarStat.getString("counts");
                pulsar_status = joPulsarStat.getString("pulsar_status");
                String pulsar_secure_status = joPulsarStat.getString("pulsar_secure_status");
                System.out.println("Counts: "+counts);
                isRelayOff = false;
                if(isBottomTest && pulsar_status.equalsIgnoreCase("1")) {
                    tv_qtyb.setText(counts);
                }
                else {
                    tv_qty.setText(counts);
                }

                if(Integer.parseInt(counts) > 10 && !isRelayOffSent && !pulsar_status.equalsIgnoreCase("0")){
                    System.out.println("Sending relay off command");
                    isRelayOffSent = true;
                    new CommandsPOST().execute(URL_RELAY, jsonRelayOff);
                }

            }
        } catch (Exception e) {
            System.out.println("GETPulsarQuantity PostExecute Ex: "+e);
        }
    }

    public class CommandsPOST extends AsyncTask<String, Void, String> {

        public String resp = "";
        public String jsonParam = "";


        @Override
        protected void onPreExecute() {

        }

        protected String doInBackground(String... param) {


            try {
                jsonParam = param[1];

                MediaType JSON = MediaType.parse("application/json");

                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(15, TimeUnit.SECONDS)
                        .readTimeout(15, TimeUnit.SECONDS)
                        .writeTimeout(15, TimeUnit.SECONDS)
                        .build();

                RequestBody body = RequestBody.create(JSON, jsonParam);

                Request request = new Request.Builder()
                        .url(param[0])
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();


            } catch (SocketException se){
                //StoreLinkDisconnectInfo(se);
                isRelayOffSent = false;
                Log.d("CommandsPOST Ex:",se.getMessage());
            }catch (Exception e) {
                isRelayOffSent = false;
                resp = e.getMessage();
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {


            try {

                consoleString += "OUTPUT- " + result + "\n";

                System.out.println("CommandsPost response: "+result);

                if (jsonParam.equalsIgnoreCase(jsonRelayOff)) {

                    if (result.contains("relay_response")) {

                        if(isBottomTest && Integer.parseInt(counts) > 10 && pulsar_status.equalsIgnoreCase("0") && !isRelayOffCalledB){
                            isRelayOffCalledB = true;
                        }

                        final Handler handler = new Handler();

                        final Runnable checkCounts = new Runnable() {
                            @Override
                            public void run() {
                                if (pulsar_status.equalsIgnoreCase("0") && !isRelayOff) {
                                    if(!isBottomTest) {
                                        tv_qty.setText("");
                                        isRelayOff = true;
                                        isRelayOffSent = false;
                                        stopTimer = false;
                                        TopTestResult = "P";
                                        if(qtyTimer!=null) {
                                            qtyTimer.cancel();
                                        }
                                        layout_toptest.setVisibility(View.GONE);
                                        layout_prepare_bottomtest.setVisibility(View.VISIBLE);
                                        Toast.makeText(getApplicationContext(), "Top test pass", Toast.LENGTH_LONG).show();
                                        AppCommon.WriteInFile(PulserTestWifiActivity.this, TAG + " Top test pass");
                                        counts =  "";
                                    }
                                    else{
                                        if(isRelayOffCalledB) {
                                            tv_qtyb.setText("");
                                            isRelayOff = true;
                                            stopTimer = false;
                                            BottomTestResult = "P";
                                            layout_bottomtest.setVisibility(View.GONE);
                                            if(qtyTimer!=null) {
                                                qtyTimer.cancel();
                                            }
                                            checkWifiStatus();
                                            Toast.makeText(getApplicationContext(), "Bottom test pass", Toast.LENGTH_LONG).show();
                                            AppCommon.WriteInFile(PulserTestWifiActivity.this, TAG + " Bottom test pass");
                                            isStopTimer = true;

                                        }
                                    }
                                } else {
                                    handler.postDelayed(this, 2000);
                                    System.out.println("checking");
                                    if(isStopTimer) {
                                        handler.removeCallbacks(this);
                                    }
                                }
                            }
                        };

                        handler.post(checkCounts);

                        }
                    else{
                        isRelayOffSent = false;
                    }
                    }


                    System.out.println(result);




            } catch (Exception e) {
                isRelayOffSent = false;
                System.out.println("CommandsPOST PostExecute Ex:"+e.getMessage());
            }

        }
    }

    private void checkWifiStatus() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
        }


        if (!wifiManager.isWifiEnabled()) {
            // Wi-Fi is disconnected
            isDisconnected = true;
            new UpdateDetails().execute(link_name, mac_address, batchID, TopTestResult, BottomTestResult, TestcaseId, "");

        } else {
            // Wi-Fi is still enabled, schedule the next check
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkWifiStatus();
                }
            }, 2000);
        }
    }

    public class UpdateDetails extends AsyncTask<String, Void, String> {

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
        pd = new ProgressDialog(PulserTestWifiActivity.this);
        pd.setMessage("Please wait...");
        pd.show();
    }

        protected String doInBackground(String... param) {
        String resp = "";


        try {

            EntityClass entityClass = new EntityClass();
            entityClass.LinkNameFromAPP = param[0];
            entityClass.MacAddress = param[1];
            entityClass.BatchId = param[2];
            entityClass.TopPulserTestResult = param[3];
            entityClass.BottomPulserTestResult = param[4];
            entityClass.AssignUniqueLinkName = AppCommon.chk_changelinkname_status;
            entityClass.IsASTLink  = AppCommon.chk_astlink_status;
            entityClass.TestDateTime = AppCommon.getTodaysDateInString();
            entityClass.LINKHardwareTestCaseId  = param[5];
            entityClass.Pulses = param[6];
            //System.out.println(" Pulses stopped:" +entityClass.Pulses );

            Gson gson = new Gson();
            final String jsonData = gson.toJson(entityClass);

            Log.i(TAG, "JsonData server call:" + jsonData);

            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(JSON, jsonData);

            Request request = new Request.Builder()
                    .url(LaunchingActivity.API)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            Response response = client.newCall(request).execute();
            resp = response.body().string();

            //------------------------------

        } catch (Exception e) {
            //serverCallComplete("", false);
            AppCommon.WriteInFile(getApplicationContext(),"UpdateDetails Ex:"+e.getMessage());
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
                String UniqueLinkName = jsonObject.getString("UniqueLinkName");

                if (ResponceMessage.equalsIgnoreCase("success")) {
                    Log.i(TAG, "API Call Success" + result);

                    AppCommon.FSBT_linkQtyToTest = AppCommon.FSBT_linkQtyToTest -1;
                    if(AppCommon.FSBT_linkQtyToTest == 0) {
                        btn_finish.setVisibility(View.VISIBLE);
                        btn_print3.setVisibility(View.VISIBLE);
                    }
                    else{
                        btn_continue.setVisibility(View.VISIBLE);
                    }

                    //serverCallComplete(UniqueLinkName, true);

                } else {
                    Log.i(TAG, "API Call fail" + result);
                    //serverCallComplete(UniqueLinkName, false);
                }

            } catch (JSONException e) {
                //serverCallComplete("", false);
                e.printStackTrace();
                AppCommon.WriteInFile(getApplicationContext(),"UpdateDetails PostExecute Ex:"+e.getMessage());
                pd.cancel();
            }

        } else {
            //serverCallComplete("", false);
            pd.cancel();
            Log.i(TAG, "GenerateFilesAPI InPost Response err:" + result);
        }

    }
    }

}