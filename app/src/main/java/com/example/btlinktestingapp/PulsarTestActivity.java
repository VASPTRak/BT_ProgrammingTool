package com.example.btlinktestingapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.brother.ptouch.sdk.Printer;
import com.brother.ptouch.sdk.PrinterInfo;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PulsarTestActivity extends AppCompatActivity implements View.OnClickListener {

    private String mDeviceAddress = "", mDeviceName = "";
    public static boolean IsPrint = true;

    private BTLinkLeServiceCode mBluetoothLeService;
    public static String btLinkResponse, btLinkPulses;
    public static String QR_ReaderStatus = "QR Waiting..";
    private static final String TAG = PulsarTestActivity.class.getSimpleName();
    Timer timer,timerBT;
    private int CurrentTest = 0,CurrentQty = 0,previousQty = 0, finalQty=0, relayOffCounter = 0;
    public boolean pulseStarted = false, pass_clicked = false, isRelayOff = false;
    TextView tv_qty, tv_qtyb, tv_rssi;
   // private String printerName = "", printerMacAddress = "";
    protected PrinterInfo printerInfo;
    public static Printer myPrinter;
    public static Bitmap ImageToPrint;
    String value="",Pulses;
    int inputValues, qty;
    String TestcaseId;

    //btn_pass_test, btn_pass_testb,
    Button btn_conn_status,  btn_restart_test,  btn_restart_testb, btn_set_no, btn_finish,btn_continue, btn_save_batchid,btn_go_to_top,btn_go_to_bottom, btn_print3, btn_save_notes;
    EditText edt_set_no, edt_batch_number, edt_notes;
    ConstraintLayout layout_toptest, layout_bottomtest,layout_prepare_toptest,layout_prepare_bottomtest, layout_rssi;
    String batchID = "", TopTestResult = "F", BottomTestResult = "F", TestResult = "Incomplete", TestCaseId="", savedBatchID = "", Notes = "" ;
    public static boolean topTestPass = false, isSameValueFor10Sec=false;
    public static boolean bottomTestPass = false, isRestartBtnVisible=false, isRestartBtnVisibleB=false, isRestartButtonClicked=false, isRestartButtonClickedB=false;
    int time=0;
    TimerTask timerTask;
    ArrayList<HashMap<String, String>> batchIDList = new ArrayList<>();
    int countBTDisconnect = 0;




    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pulsar_test);
        //setTitle("Top pulser test");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        layout_toptest = (ConstraintLayout) findViewById(R.id.layout_toptest);
        layout_rssi = (ConstraintLayout) findViewById(R.id.layout_rssi);
        layout_bottomtest = (ConstraintLayout) findViewById(R.id.layout_bottomtest);
        layout_prepare_toptest = (ConstraintLayout) findViewById(R.id.layout_prepare_toptest);
        layout_prepare_bottomtest = (ConstraintLayout) findViewById(R.id.layout_prepare_bottomtest);
        btn_conn_status = (Button) findViewById(R.id.btn_conn_status);
        // btn_pass_test = (Button) findViewById(R.id.btn_pass_test);
        //btn_pass_testb = (Button) findViewById(R.id.btn_pass_testb);
        btn_restart_test = (Button) findViewById(R.id.btn_restart_test);
        btn_restart_testb = (Button) findViewById(R.id.btn_restart_testb);
        btn_set_no = (Button) findViewById(R.id.btn_set_no);
        btn_finish = (Button) findViewById(R.id.btn_finish);
        btn_continue = (Button) findViewById(R.id.btn_continue);
        btn_save_batchid = (Button) findViewById(R.id.btn_save_batchid);
        btn_save_notes = (Button) findViewById(R.id.btn_save_note);
        btn_go_to_top = (Button) findViewById(R.id.btn_go_to_top);
        btn_go_to_bottom = (Button) findViewById(R.id.btn_go_to_bottom);
        btn_print3 = (Button) findViewById(R.id.btnPrint3);
        tv_qty = (TextView) findViewById(R.id.tv_qty);
        tv_qtyb = (TextView) findViewById(R.id.tv_qtyb);
        tv_rssi = (TextView) findViewById(R.id.tv_rssi);
        edt_set_no = (EditText) findViewById(R.id.edt_set_no);
        edt_batch_number = (EditText) findViewById(R.id.edt_batch_number);
        edt_notes = (EditText) findViewById(R.id.edt_note);
        btn_conn_status.setOnClickListener(this);
        //  btn_pass_test.setOnClickListener(this);
        // btn_pass_testb.setOnClickListener(this);
        btn_restart_test.setOnClickListener(this);
        btn_restart_testb.setOnClickListener(this);
        btn_set_no.setOnClickListener(this);
        btn_finish.setOnClickListener(this);
        btn_continue.setOnClickListener(this);
        btn_print3.setOnClickListener(this);
        btn_save_batchid.setOnClickListener(this);
        btn_save_notes.setOnClickListener(this);
        btn_go_to_top.setOnClickListener(this);
        btn_go_to_bottom.setOnClickListener(this);


        SharedPreferences sharedPref = PulsarTestActivity.this.getSharedPreferences("PulseValue", Context.MODE_PRIVATE);
        value = sharedPref.getString("Pulses", value);
        TestcaseId = sharedPref.getString("TestCaseId",TestcaseId);

        inputValues = Integer.parseInt(value.toString());
        timerBT = new Timer();


        Intent intent = getIntent();
        mDeviceName = intent.getStringExtra("DeviceName");
        mDeviceAddress = intent.getStringExtra("DeviceMac");
        //LinkNameToPrint = IsPrint;

        edt_batch_number.addTextChangedListener(new TextWatcher() {
            boolean isEditing = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable et) {
                if (isEditing) return;
                isEditing = true;

                String s = et.toString();
                String upper = s.toUpperCase();
                if (!s.equals(upper)) {
                    edt_batch_number.setText(upper);
                    edt_batch_number.setSelection(upper.length()); // keep cursor at end
                }

                isEditing = false;
            }
        });



        Intent gattServiceIntent = new Intent(this, BTLinkLeServiceCode.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if(!AppCommon.IsRetest) {
            Toast.makeText(this, "Connecting to the LINK\nPlease wait several seconds...", Toast.LENGTH_SHORT).show();
            AppCommon.WriteInFile(PulsarTestActivity.this, TAG + " Connecting to the LINK (" + mDeviceName + "). Please wait several seconds...");

            if (AppCommon.isbtnContinuePressed.equalsIgnoreCase("true")) {
                btn_save_batchid.setEnabled(true);
            } else {
                btn_save_batchid.setEnabled(false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if (!btn_save_batchid.isEnabled()) {
                            Toast.makeText(mBluetoothLeService, "Unable to connect to the LINK... Please try again", Toast.LENGTH_LONG).show();
                            AppCommon.WriteInFile(PulsarTestActivity.this, TAG + " Unable to connect to the LINK... Please try again");
                        }

                    }
                }, 10000);
            }
        }

        else{

            AppCommon.WriteInFile(PulsarTestActivity.this,"**************RETEST**************");
            edt_batch_number.setVisibility(View.INVISIBLE);
            btn_save_batchid.setVisibility(View.INVISIBLE);
            edt_notes.setVisibility(View.VISIBLE);
            btn_save_notes.setVisibility(View.VISIBLE);

            btn_go_to_top.setEnabled(false);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    if (!btn_go_to_top.isEnabled()) {
                        Toast.makeText(mBluetoothLeService, "Unable to connect to the LINK... Please try again", Toast.LENGTH_LONG).show();
                        AppCommon.WriteInFile(PulsarTestActivity.this, TAG + " Unable to connect to the LINK... Please try again");
                    }

                }
            }, 10000);
        }
         timerTask = new TimerTask() {
            @Override
            public void run() {
                if (pulseStarted && PulsarTestActivity.btLinkResponse.contains("DISCONNECTED")){
                    if(AppCommon.Pulses.equalsIgnoreCase(String.valueOf(previousQty)) && previousQty!=0 && !isRestartBtnVisible && !isRestartBtnVisibleB) {

                        if(!isSameValueFor10Sec) {
                            isSameValueFor10Sec=true;
                            qty = previousQty;
                        }

                        time++;
                        System.out.println("prevTimeDiffff-----------" + time);

                        if(time==10) {
                            if (isSameValueFor10Sec) {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (CurrentTest == 1) {
                                            //timerBT.cancel();
                                            time = 0;
                                            if(btLinkResponse.equalsIgnoreCase("STATE_CONNECTED") || btLinkResponse.contains("enabled")) {
                                                btn_restart_test.setVisibility(View.VISIBLE);
                                                isRestartBtnVisible = true;
                                                countDownTimer();
                                            }
                                            else{
                                                try {

                                                    if(countBTDisconnect < 3) {
                                                        unbindService(mServiceConnection);
                                                        unregisterReceiver(mGattUpdateReceiver);
                                                        Thread.sleep(2000);
                                                        Intent gattServiceIntent = new Intent(PulsarTestActivity.this, BTLinkLeServiceCode.class);
                                                        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

                                                        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
                                                        countBTDisconnect++;
                                                    }
                                                    else{
                                                        countDownTimer();
                                                    }
                                                }catch (Exception e){
                                                    
                                                }
                                            }

                                        }
                                        if (CurrentTest == 2) {
                                            //timerBT.cancel();
                                            time = 0;
                                            if(btLinkResponse.equalsIgnoreCase("STATE_CONNECTED") || btLinkResponse.contains("enabled")) {
                                                btn_restart_testb.setVisibility(View.VISIBLE);
                                                isRestartBtnVisibleB = true;
                                                countDownTimer();
                                            }
                                            else {
                                                try {

                                                    if(countBTDisconnect < 3) {
                                                        unbindService(mServiceConnection);
                                                        unregisterReceiver(mGattUpdateReceiver);
                                                        Thread.sleep(2000);
                                                        Intent gattServiceIntent = new Intent(PulsarTestActivity.this, BTLinkLeServiceCode.class);
                                                        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

                                                        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
                                                        countBTDisconnect++;
                                                    }
                                                    else{
                                                        countDownTimer();
                                                    }
                                                }catch (Exception e){

                                                }
                                            }

                                            }
                                    }
                                });

                            }
                            else{
                                isSameValueFor10Sec=false;
                                time=0;
                            }
                        }
                    }
                    else {
                        time=0;
                    }
                }
//                else{
//                    if(AppCommon.BTDisconnected){
//                        pulseStarted=false;
//                        previousQty=CurrentQty;
//                    }
//                }
            }
        };
        timerBT.schedule(timerTask,1000,1000);

        SharedPreferences sharedPrefbatchID4 = PulsarTestActivity.this.getSharedPreferences("saveBatchID", Context.MODE_PRIVATE);
        String savedData = sharedPrefbatchID4.getString("batchID","");

        if(AppCommon.isbtnContinuePressed.equalsIgnoreCase("true") || sharedPrefbatchID4.contains("batchID")){

            String[] savedBatchIDs= savedData.split(",");

            for (String value : savedBatchIDs) {
                HashMap<String, String> map = new HashMap<>();
                map.put("batchID", value);

                batchIDList.add(map);
                //SelectBatchIDFromList();
            }
        }

        if(AppCommon.IsRetest) {
            batchID = AppCommon.batchIDForRetest;
        }
    }

    public void countDownTimer(){
        countBTDisconnect = 0;
        new CountDownTimer(1000,1000){

            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                if(!isRestartButtonClicked && CurrentTest==1 && AppCommon.Pulses.equalsIgnoreCase(String.valueOf(previousQty))){
                    RelayOffCommand();
                    TopTestResult = "F";
                    BottomTestResult = "F";
                    Pulses = String.valueOf(CurrentQty);
                    layout_toptest.setVisibility(View.GONE);
                    layout_bottomtest.setVisibility(View.GONE);
                    //Server call...
                    if (isConnecting()) {
                        cancelTimer();
                        layout_toptest.setVisibility(View.GONE);
                        layout_bottomtest.setVisibility(View.GONE);
                        try {
                            new UpdateDetails().execute(mDeviceName, mDeviceAddress, batchID, TopTestResult, BottomTestResult, TestcaseId, Pulses, Notes);
                            cancel();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {
                        Toast.makeText(PulsarTestActivity.this, "Please check network connection", Toast.LENGTH_LONG).show();
                        AppCommon.WriteInFile(PulsarTestActivity.this, TAG + " Please check network connection");
                    }
                }

                if(!isRestartButtonClickedB && CurrentTest==2 && AppCommon.Pulses.equalsIgnoreCase(String.valueOf(previousQty))){
                    RelayOffCommand();
                    BottomTestResult = "F";
                    Pulses = String.valueOf(CurrentQty);
                    layout_toptest.setVisibility(View.GONE);
                    layout_bottomtest.setVisibility(View.GONE);
                    //Server call...
                    if (isConnecting()) {
                        cancelTimer();
                        layout_toptest.setVisibility(View.GONE);
                        layout_bottomtest.setVisibility(View.GONE);
                        try {
                            new UpdateDetails().execute(mDeviceName, mDeviceAddress, batchID, TopTestResult, BottomTestResult, TestcaseId, Pulses, Notes);
                            timerBT.cancel();
                            cancel();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                    } else {
                        Toast.makeText(PulsarTestActivity.this, "Please check network connection", Toast.LENGTH_LONG).show();
                        AppCommon.WriteInFile(PulsarTestActivity.this, TAG + " Please check network connection");
                    }
                }
                isRestartBtnVisible=false;
                isRestartBtnVisibleB=false;
            }
        }.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        CurrentTest = 0;
        CurrentQty = 0;
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

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(mGattUpdateReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTimer();

        if(timerBT!=null){
            timerBT.cancel();
        }

        if (TestResult.equalsIgnoreCase("Incomplete")) {
            Log.i(TAG, "*** Result ***\n MacAddress:" + mDeviceAddress + " LinkNameFromAPP:" + mDeviceName + "TestDateTime: 06/10/2021 BatchId:" + batchID + " TopPulserTestResult:" + TopTestResult + " BottomPulserTestResult:" + BottomTestResult + " TestResult" + TestResult);
        }

    }


    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BTLinkLeServiceCode.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");

            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }

    };


    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!btn_save_batchid.isEnabled()) {
                btn_save_batchid.setEnabled(true);
                AppCommon.WriteInFile(PulsarTestActivity.this, TAG + " Save_batchId button enabled");
            }

            if (!btn_go_to_top.isEnabled()) {
                btn_go_to_top.setEnabled(true);
                AppCommon.WriteInFile(PulsarTestActivity.this, TAG + " GO button enabled");
            }
            final String action = intent.getAction();
            if (BTLinkLeServiceCode.ACTION_GATT_CONNECTED.equals(action)) {


                QR_ReaderStatus = "QR Connected";
                System.out.println("ACTION_GATT_QR_CONNECTED");
                getBatchNumber();

            } else if (BTLinkLeServiceCode.ACTION_GATT_DISCONNECTED.equals(action)) {

                QR_ReaderStatus = "QR Disconnected";
                PulsarTestActivity.btLinkResponse = "BT Disconnected";
                System.out.println("ACTION_GATT_QR_DISCONNECTED");
                btn_conn_status.setText("Re-Connect");
                btn_conn_status.setVisibility(View.VISIBLE);

            } else if (BTLinkLeServiceCode.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                System.out.println("ACTION_GATT_QR_SERVICES_DISCOVERED");
                QR_ReaderStatus = "QR Discovered";
            } else if (BTLinkLeServiceCode.ACTION_DATA_AVAILABLE.equals(action)) {
                System.out.println("ACTION_GATT_QR_AVAILABLE");
                System.out.println("ACTION_DATA_AVAILABLE");
                QR_ReaderStatus = "QR Connected";

                if (intent.hasExtra("EXTRA_RSSI") && AppCommon.TestCaseName.equalsIgnoreCase("Test 5")) {
                    int rssi = intent.getIntExtra("EXTRA_RSSI", 0);
                    layout_rssi.setVisibility(View.VISIBLE);
                    tv_rssi.setText("Signal strength: " + rssi + " dBm");
                }
                getBatchNumber();
                displayData(intent.getStringExtra(BTLinkLeServiceCode.EXTRA_DATA));
            } else {
                QR_ReaderStatus = "QR Disconnected";
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BTLinkLeServiceCode.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BTLinkLeServiceCode.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BTLinkLeServiceCode.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BTLinkLeServiceCode.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_conn_status:

                if (QR_ReaderStatus.equalsIgnoreCase("QR Disconnected") && !mDeviceAddress.isEmpty()) {
                    // Manually re-connects to the device.
                    mBluetoothLeService.connect(mDeviceAddress);
                } else {
                    Toast.makeText(this, "Connecting..", Toast.LENGTH_LONG).show();
                }

                break;
            case R.id.btn_save_batchid:


                String bid = edt_batch_number.getText().toString().trim();
                batchID = bid.toUpperCase();

                SharedPreferences sharedPrefbatchID = PulsarTestActivity.this.getSharedPreferences("saveBatchID", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPrefbatchID.edit();


                if (sharedPrefbatchID.contains("batchID")) {
                    savedBatchID = sharedPrefbatchID.getString("batchID", savedBatchID);
                    if(!savedBatchID.contains(batchID)) {
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
                            new checkbatchidunqiue().execute(batchID);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {
                        Toast.makeText(this, "Please check network connection", Toast.LENGTH_LONG).show();
                        AppCommon.WriteInFile(PulsarTestActivity.this, TAG + " Please check network connection");
                    }
                }


                break;
//            case R.id.btn_pass_test:
//                RelayOffCommand();
//                TopTestResult = "P";
//                layout_toptest.setVisibility(View.GONE);
//                layout_prepare_bottomtest.setVisibility(View.VISIBLE);
//
//                break;


            case R.id.btn_save_note:
                Notes = edt_notes.getText().toString().trim();
                if (Notes.equals("")) {
                    edt_notes.setError("required");
                    edt_notes.setFocusable(true);
                }
                else {
                    edt_notes.setVisibility(View.INVISIBLE);
                    btn_save_notes.setVisibility(View.INVISIBLE);
                    onSuccessBatchId(true, "");
                }


                break;

            case R.id.btn_restart_test:
                btn_restart_test.setVisibility(View.GONE);
                isRestartBtnVisible=false;
                isSameValueFor10Sec=false;
                isRestartButtonClicked=true;
                RelayOffCommand();
                TopTestResult = "F";
                BottomTestResult = "F";
                Pulses = String.valueOf(CurrentQty);
                layout_toptest.setVisibility(View.GONE);
                layout_bottomtest.setVisibility(View.GONE);
                tv_qty.setText("");
                tv_qtyb.setText("");
                //Server call...
//                if (isConnecting()) {
//                    cancelTimer();
//                    layout_toptest.setVisibility(View.GONE);
//                    layout_bottomtest.setVisibility(View.GONE);
//                    try {
//                        new UpdateDetails().execute(mDeviceName, mDeviceAddress, batchID, TopTestResult, BottomTestResult, TestcaseId, Pulses );
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
//                } else {
//                    Toast.makeText(this, "Please check network connection", Toast.LENGTH_LONG).show();
//                    AppCommon.WriteInFile(PulsarTestActivity.this, TAG + " Please check network connection");
                    CurrentTest = 1;
                    CurrentQty = 0;
                    previousQty = 0;
                    finalQty = 0;
                    layout_prepare_toptest.setVisibility(View.GONE);
                    layout_toptest.setVisibility(View.VISIBLE);
                    //layout_bottomtest.setVisibility(View.VISIBLE);

                    //mBluetoothLeService.writeCustomCharacteristic("LK_COMM=info");
                    mBluetoothLeService.writeCustomCharacteristic("LK_COMM=relay:12345=ON");


               // }

                break;

//            case R.id.btn_pass_testb:
//                RelayOffCommand();
//                BottomTestResult = "P";
//                //Server call...
//                if (isConnecting()) {
//                    cancelTimer();
//                    layout_toptest.setVisibility(View.GONE);
//                    layout_bottomtest.setVisibility(View.GONE);
//                    try {
//                        new UpdateDetails().execute(mDeviceName, mDeviceAddress, batchID, TopTestResult, BottomTestResult);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                } else {
//                    Toast.makeText(this, "Please check network connection", Toast.LENGTH_LONG).show();
//                }
//
//                break;

            case R.id.btn_restart_testb:
                btn_restart_testb.setVisibility(View.GONE);
                isRestartBtnVisibleB=false;
                isSameValueFor10Sec=false;
                isRestartButtonClickedB=true;
                RelayOffCommand();
                tv_qty.setText("");
                tv_qtyb.setText("");
                BottomTestResult = "F";
                Pulses = String.valueOf(CurrentQty);
                layout_toptest.setVisibility(View.GONE);
                layout_bottomtest.setVisibility(View.GONE);
                //Server call...
//                if (isConnecting()) {
//                    cancelTimer();
//                    layout_toptest.setVisibility(View.GONE);
//                    layout_bottomtest.setVisibility(View.GONE);
//                    try {
//                        new UpdateDetails().execute(mDeviceName, mDeviceAddress, batchID, TopTestResult, BottomTestResult, TestcaseId, Pulses);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
//                } else {
//                    Toast.makeText(this, "Please check network connection", Toast.LENGTH_LONG).show();
//                    AppCommon.WriteInFile(PulsarTestActivity.this, TAG + " Please check network connection");
//                }

                CurrentTest = 2;
                CurrentQty = 0;
                previousQty = 0;
                finalQty = 0;
                layout_prepare_bottomtest.setVisibility(View.GONE);
                layout_bottomtest.setVisibility(View.VISIBLE);
                mBluetoothLeService.writeCustomCharacteristic("LK_COMM=relay:12345=ON");
                break;

            case R.id.btn_set_no:

                String cmd = edt_set_no.getText().toString().trim();
                if (cmd != null && cmd.startsWith("FSBT-") && QR_ReaderStatus.equalsIgnoreCase("QR Connected")) {

                    if (isConnecting()) {
                        try {
                            mBluetoothLeService.writeCustomCharacteristic("LK_COMM=name:" + cmd);
                            edt_set_no.setVisibility(View.GONE);
                            btn_set_no.setVisibility(View.GONE);
                            new setserialnumber().execute(batchID, cmd);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {
                        Toast.makeText(this, "Please check network connection", Toast.LENGTH_LONG).show();
                        AppCommon.WriteInFile(PulsarTestActivity.this, TAG + " Please check network connection");
                    }

                } else {
                    Toast.makeText(this, "Something went wrong. Please try again", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.btn_finish:
                AppCommon.isbtnContinuePressed = "False";
                batchIDList.clear();
                SharedPreferences sharedPrefbatchID2 = PulsarTestActivity.this.getSharedPreferences("saveBatchID", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor1 = sharedPrefbatchID2.edit();
                editor1.clear();
                editor1.apply();
                Intent i = new Intent(PulsarTestActivity.this, LaunchingActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                break;

            case R.id.btn_continue:
                AppCommon.isbtnContinuePressed="True";
                Intent intent = new Intent(PulsarTestActivity.this, link_selected.class);
                startActivity(intent);
                break;

            case R.id.btn_go_to_top:
                CurrentTest = 1;
                CurrentQty = 0;
                previousQty = 0;
                finalQty = 0;
                layout_prepare_toptest.setVisibility(View.GONE);
                layout_toptest.setVisibility(View.VISIBLE);
                //layout_bottomtest.setVisibility(View.VISIBLE);

                //mBluetoothLeService.writeCustomCharacteristic("LK_COMM=info");
                mBluetoothLeService.writeCustomCharacteristic("LK_COMM=relay:12345=ON");

                 break;
            case R.id.btn_go_to_bottom:
                CurrentTest = 2;
                CurrentQty = 0;
                previousQty = 0;
                finalQty = 0;
                layout_prepare_bottomtest.setVisibility(View.GONE);
                layout_bottomtest.setVisibility(View.VISIBLE);
                mBluetoothLeService.writeCustomCharacteristic("LK_COMM=relay:12345=ON");
                break;

            case R.id.btnPrint3:
                AppCommon.IsPrint = true;
                AppCommon.LinkNameToPrint = mDeviceName;
                Intent k = new Intent(PulsarTestActivity.this, ScanDeviceActivity.class);
                startActivity(k);
                break;
        }
    }

    public void SelectBatchIDFromList() {


        final Dialog dialog = new Dialog(PulsarTestActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.batchid_list);

        //TextView tvNoLinks = (TextView) dialog.findViewById(R.id.tvnolinks);
        ListView lvlinkNames = (ListView) dialog.findViewById(R.id.lvBatchIDList);
        Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);

        lvlinkNames.setVisibility(View.VISIBLE);

        SimpleAdapter adapter = new SimpleAdapter(this,batchIDList , R.layout.item_link, new String[]{"batchID"}, new int[]{R.id.tvSingleItem});
        lvlinkNames.setAdapter(adapter);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        lvlinkNames.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //SelectedItemPos = position;

                //selectLinkByPosition();

                String selectedValue =  batchIDList.get(position).get("batchID");
                AppCommon.selectedBatchID = selectedValue;
                edt_batch_number.setText(AppCommon.selectedBatchID);


                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void RelayOnCommand(){
        layout_prepare_toptest.setVisibility(View.GONE);
        layout_toptest.setVisibility(View.VISIBLE);
        finalQty = 0;
        mBluetoothLeService.writeCustomCharacteristic("LK_COMM=relay:12345=ON");
        isRelayOff = false;
    }

    public void FDCheck() {

        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    timer = new Timer();
                    TimerTask ttSensor = new TimerTask() {
                        @Override
                        public void run() {

                            if (QR_ReaderStatus.equalsIgnoreCase("QR Connected") && pulseStarted == true) {

                                mBluetoothLeService.writeCustomCharacteristic("LK_COMM=FD_check");
                            }

                        }

                    };

                    timer.schedule(ttSensor, 5000, 4000);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void displayData(String data) {
        if (data != null) {
            try {
                if (!AppCommon.IsNewBTFirmware) {
                    String[] Seperate = data.split("\n");

                    String last_val = "";
                    if (Seperate.length > 1) {

                        String respStr = Seperate[0];
                        String respHex = Seperate[1];

                        btLinkResponse = respStr;

                        if (respStr.contains("pulse")) {
                            pulseStarted = true;
                            btLinkPulses = respStr;
                            tv_qty.setText("  " + respStr + "  ");
                            tv_qtyb.setText("  " + respStr + "  ");
                            splitRespStr(respStr);
                        } else {
                            pulseStarted = false;
                            //tv_qty.setText("");
                            //tv_qtyb.setText("");
                        }


                        System.out.println("-Qrcode data sep1>>" + respStr);
                        System.out.println("-Qrcode data sep2>>" + respHex);

                        last_val = Seperate[Seperate.length - 1];
                    } else {
                        System.out.println("-Qrcode data>>" + data);
                    }
                } else {
                    String newRespStr = data.replace("$$", "");

                    btLinkResponse = newRespStr;

                    if (newRespStr.contains("pulse")) {
                        try {
                            // Changing {"pulse":1} to pulse:1
                            newRespStr = newRespStr.replaceAll("\"", "").replaceAll("\\{", "").replaceAll("\\}", "");
                            newRespStr = String.join(": ", newRespStr.split(":")); // To change pulse:1 to pulse: 1
                        } catch (Exception ex) {
                            System.out.println(ex);
                            newRespStr = data.replace("$$", "");
                        }

                        pulseStarted = true;
                        btLinkPulses = newRespStr;
                        tv_qty.setText("  " + newRespStr + "  ");
                        tv_qtyb.setText("  " + newRespStr + "  ");
                        splitRespStr(newRespStr);
                    }
                    System.out.println("-displayData new BT Firmware data >>" + newRespStr);
                }

            } catch (Exception ex) {
                System.out.println(ex);

            }

        }
    }

    public void GenerateSrNo(String mDeviceAddress) {

        mDeviceAddress = mDeviceAddress.replace(":", "").trim();
        edt_set_no.setText("FSBT-" + mDeviceAddress);

    }

    public void RelayOffCommand() {

        try {
            mBluetoothLeService.writeCustomCharacteristic("LK_COMM=relay:12345=OFF");
            btn_conn_status.setVisibility(View.GONE);
            pulseStarted=false;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void RenameCommand(){
        if (isConnecting() && !AppCommon.UniqueLinkName.isEmpty()) {
            try {
                mBluetoothLeService.writeCustomCharacteristic("LK_COMM=name:" + AppCommon.UniqueLinkName);
                new setserialnumber().execute(batchID, AppCommon.UniqueLinkName);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            Toast.makeText(this, "Please check network connection", Toast.LENGTH_LONG).show();
            AppCommon.WriteInFile(PulsarTestActivity.this, TAG + " Please check network connection");
        }

    }

    public class UpdateDetails extends AsyncTask<String, Void, String> {


        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(PulsarTestActivity.this);
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
                entityClass.LinkTypeFromApp = AppCommon.selectedLinkType;
                entityClass.Notes = param[7];
                System.out.println(" Pulses stopped:" +entityClass.Pulses );

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
                    AppCommon.UniqueLinkName = UniqueLinkName;

                    if (ResponceMessage.equalsIgnoreCase("success")) {
                        Log.i(TAG, "API Call Success" + result);
                        if(!UniqueLinkName.isEmpty()) {
                            //serverCallComplete(UniqueLinkName, true);
                            RenameCommand();
                        }
                        else{
                            Test_Complete_Process();
                        }

                    } else {
                        Log.i(TAG, "API Call fail" + result);
                        //serverCallComplete(UniqueLinkName, false);
                    }

                } catch (JSONException e) {
                    //serverCallComplete("", false);
                    e.printStackTrace();
                    pd.cancel();
                }

            } else {
                //serverCallComplete("", false);
                pd.cancel();
                Log.i(TAG, "GenerateFilesAPI InPost Response err:" + result);
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


    public void getBatchNumber() {
        btn_conn_status.setVisibility(View.GONE);
    }


    public void serverCallComplete(String uniqueLinkName, boolean status) {

        if(status) {
            new setserialnumber().execute(batchID, uniqueLinkName);
        } else {
            TestResult = "Incomplete";
            //Toast.makeText(this, "Something went wrong. please try again.", Toast.LENGTH_LONG).show();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {


                if (AppCommon.FSBT_linkQtyToTest == 0 || AppCommon.FSBT_linkQtyToTest < 0){
                    btn_finish.setVisibility(View.VISIBLE);
                    btn_print3.setVisibility(View.VISIBLE);
                    btn_continue.setVisibility(View.GONE);
                }
                else{
                    btn_continue.setVisibility(View.VISIBLE);
                    btn_print3.setVisibility(View.GONE);
                }

            }
        }, 1000);


        /////////////////////////////////////////////////////////////////
        /*if (!uniqueLinkName.equals("")) {
            Log.i(TAG, "UniqueLinkName returned ny server:" + uniqueLinkName);
            edt_set_no.setText(uniqueLinkName);
            edt_set_no.setVisibility(View.VISIBLE);
            btn_set_no.setVisibility(View.VISIBLE);
        } else {
            TestResult = "Incomplete";
            //Toast.makeText(this, "Something went wrong. please try again.", Toast.LENGTH_LONG).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    btn_finish.setVisibility(View.VISIBLE);
                }
            }, 2000);
        }*/

    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    public void cancelTimer() {

        if (timer != null)
            timer.cancel();
    }

    public class checkbatchidunqiue extends AsyncTask<String, Void, String> {


        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(PulsarTestActivity.this);
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
            if (QR_ReaderStatus.equalsIgnoreCase("QR Connected") || AppCommon.IsRetest) {
                if(AppCommon.chk_changelinkname_status.equalsIgnoreCase("Y")) {
                    FDCheck();
                    edt_batch_number.setVisibility(View.GONE);
                    btn_save_batchid.setVisibility(View.GONE);
                    hideKeyboard(PulsarTestActivity.this);
                    if (isConnecting()) {
                        try {
                            new UpdateDetails().execute(mDeviceName, mDeviceAddress, batchID, TopTestResult, BottomTestResult, TestcaseId, Pulses, Notes);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                else {
                    FDCheck();
                    edt_batch_number.setVisibility(View.GONE);
                    btn_save_batchid.setVisibility(View.GONE);
                    layout_prepare_toptest.setVisibility(View.VISIBLE);
                    hideKeyboard(PulsarTestActivity.this);
                }

            } else {
                Toast.makeText(this, "Please wait. Connecting..", Toast.LENGTH_LONG).show();
                AppCommon.WriteInFile(PulsarTestActivity.this, TAG + " Status: " + QR_ReaderStatus);
            }

        } else {
            //AppCommon.colorToastBigFont(PulsarTestActivity.this,responseText,Color.RED);
            Toast.makeText(this, responseText, Toast.LENGTH_LONG).show();
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

            if (str_len < 6) {
                edt_batch_number.setError("Batch Id must be at least 6 characters");
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

    public class setserialnumber extends AsyncTask<String, Void, String> {


        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(PulsarTestActivity.this);
            pd.setMessage("Please wait...");
            pd.show();
        }

        protected String doInBackground(String... param) {
            String resp = "";

            try {

                EntityClass entityClass = new EntityClass();
                entityClass.BatchId = param[0];
                entityClass.UniqueLinkName = param[1];

                Gson gson = new Gson();
                final String jsonData = gson.toJson(entityClass);

                Log.i(TAG, "JsonData server call:" + jsonData);

                OkHttpClient client = new OkHttpClient();
                MediaType JSON = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(JSON, jsonData);

                Request request = new Request.Builder()
                        .url(LaunchingActivity.API_SET_NO)
                        .post(body)
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
            AppCommon.FSBT_linkQtyToTest = AppCommon.FSBT_linkQtyToTest -1;
            if (AppCommon.FSBT_linkQtyToTest == 0|| AppCommon.chk_changelinkname_status.equalsIgnoreCase("Y")){

                btn_finish.setVisibility(View.VISIBLE);
                btn_print3.setVisibility(View.VISIBLE);
                btn_continue.setVisibility(View.GONE);

            }
            else{
                btn_continue.setVisibility(View.VISIBLE);
                btn_print3.setVisibility(View.GONE);
            }

            if (result != null && !result.isEmpty()) {

                try {
                    pd.cancel();
                    JSONObject jsonObject = new JSONObject(result);

                    String ResponceMessage = jsonObject.getString("ResponseMessage");
                    String UniqueLinkName = jsonObject.getString("UniqueLinkName");

                    if (ResponceMessage.equalsIgnoreCase("success")) {
                        Log.i(TAG, "API Call Success" + result);
                        TestResult = "Completed";
                        Log.i(TAG,"*** Result ***\n MacAddress:"+mDeviceAddress+" LinkNameFromAPP:"+mDeviceName+"TestDateTime: 06/10/2021 BatchId:"+batchID+" TopPulserTestResult:"+TopTestResult+" BottomPulserTestResult:"+BottomTestResult+" TestResult"+TestResult);

                    } else {
                        Log.i(TAG, "API Call fail" + result);
                        TestResult = "InComplete";
                        Log.i(TAG,"*** Result ***\n MacAddress:"+mDeviceAddress+" LinkNameFromAPP:"+mDeviceName+"TestDateTime: 06/10/2021 BatchId:"+batchID+" TopPulserTestResult:"+TopTestResult+" BottomPulserTestResult:"+BottomTestResult+" TestResult"+TestResult);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    AppCommon.WriteInFile(PulsarTestActivity.this, TAG + " Exception in setserialnumber onPostExecute: " + e.getMessage());
                    pd.cancel();
                }

            } else {
                pd.cancel();
                Log.i(TAG, "setserialnumber InPost Response err:" + result);
                AppCommon.WriteInFile(PulsarTestActivity.this, TAG + " setserialnumber InPost Response err: " + result);
            }

        }
    }


    public void Test_Complete_Process() {
        Set<String> validTests = new HashSet<>(Arrays.asList("Test 1", "Test 2", "Test 3", "Test 4"));
        if (validTests.contains(AppCommon.TestCaseName.trim())) {
            if (relayOffCounter < 4) {
                CurrentTest = 1;
                RelayOnCommand();
                relayOffCounter++;  // Increment the counter

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Return from the function to ensure the rest of the code runs only after 5 calls
                return;
            }
        }

        AppCommon.FSBT_linkQtyToTest = AppCommon.FSBT_linkQtyToTest - 1;
        Set<String> validTests1 = new HashSet<>(Arrays.asList("Test 1", "Test 2", "Test 3", "Test 4"));
        if (validTests1.contains(AppCommon.TestCaseName.trim())) {
            if (relayOffCounter == 4 && AppCommon.FSBT_linkQtyToTest == 0 || AppCommon.chk_changelinkname_status.equalsIgnoreCase("Y") || AppCommon.FSBT_linkQtyToTest < 0) {
                relayOffCounter = 0;
                btn_finish.setVisibility(View.VISIBLE);
                btn_print3.setVisibility(View.VISIBLE);
                btn_continue.setVisibility(View.GONE);

            } else {
                relayOffCounter = 0;
                btn_continue.setVisibility(View.VISIBLE);
                btn_print3.setVisibility(View.GONE);
            }
        }
        else {
            if(AppCommon.FSBT_linkQtyToTest == 0 || AppCommon.FSBT_linkQtyToTest < 0){
                btn_finish.setVisibility(View.VISIBLE);
                btn_print3.setVisibility(View.VISIBLE);
                btn_continue.setVisibility(View.GONE);
            }
            else{
                btn_continue.setVisibility(View.VISIBLE);
                btn_print3.setVisibility(View.GONE);
            }
        }

        if(AppCommon.IsRetest){
            AppCommon.IsRetest = false;
        }
    }

    private void test_pass_top() {
        try {
            topTestPass=true;
            RelayOffCommand();
            TopTestResult = "P";
            layout_toptest.setVisibility(View.GONE);
            layout_prepare_bottomtest.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            AppCommon.WriteInFile(PulsarTestActivity.this, TAG + " Exception in test_pass_top: " + e.getMessage());
        }
    }

    private void test_pass_bottom() {
        try {
            timerBT.cancel();
            bottomTestPass=true;
            RelayOffCommand();
            BottomTestResult = "P";
            //Server call...
            if (isConnecting()) {
                cancelTimer();
                layout_toptest.setVisibility(View.GONE);
                layout_bottomtest.setVisibility(View.GONE);
                try {
                    new UpdateDetails().execute(mDeviceName, mDeviceAddress, batchID, TopTestResult, BottomTestResult, TestcaseId, Pulses, Notes);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "Please check network connection", Toast.LENGTH_LONG).show();
                AppCommon.WriteInFile(PulsarTestActivity.this, TAG + " Please check network connection");
            }
        } catch (Exception e) {
            AppCommon.WriteInFile(PulsarTestActivity.this, TAG + " Exception in test_pass_bottom: " + e.getMessage());
        }
    }

    private void test_pass() {
        try {
            topTestPass=true;
            RelayOffCommand();
            TopTestResult = "P";
            BottomTestResult = "F";
            if (isConnecting()) {
                cancelTimer();
                layout_toptest.setVisibility(View.GONE);
                try {
                    new UpdateDetails().execute(mDeviceName, mDeviceAddress, batchID, TopTestResult, BottomTestResult, TestcaseId, Pulses, Notes);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "Please check network connection", Toast.LENGTH_LONG).show();
                AppCommon.WriteInFile(PulsarTestActivity.this, TAG + " Please check network connection");
            }
        } catch (Exception e) {
            AppCommon.WriteInFile(PulsarTestActivity.this, TAG + " Exception in test_pass_top: " + e.getMessage());
        }
    }

    private void splitRespStr(String respStr) {
        try {
            if (respStr.contains("pulse:")) {
                previousQty = CurrentQty;

//                if(CurrentQty>=2 && CurrentQty<=28 && CurrentTest==2){
//                    CurrentQty=2;
//                }
                AppCommon.Pulses = String.valueOf(CurrentQty);
                String[] resp = respStr.split(":");
                CurrentQty = Integer.parseInt(resp[1].trim());
                Log.i(TAG, "Current qty:" + CurrentQty);
                if (CurrentTest == 1 && CurrentQty > inputValues  && finalQty == 0) {
                    //previousQty = CurrentQty;
                    finalQty = CurrentQty;
                    Pulses = String.valueOf(CurrentQty);
                    Set<String> validTests = new HashSet<>(Arrays.asList("Test 1", "Test 2", "Test 3", "Test 4"));
                    if (validTests.contains(AppCommon.TestCaseName.trim())) {
                        test_pass();
                    }
                    else{
                        test_pass_top();
                    }
                    Toast.makeText(getApplicationContext(), "Top test pass", Toast.LENGTH_LONG).show();
                    AppCommon.WriteInFile(PulsarTestActivity.this, TAG + " Top test pass");
                    //btn_go_to_bottom.performClick();
                } else if (CurrentTest == 2 && CurrentQty > inputValues && finalQty == 0) {
                    //previousQty = CurrentQty;
                    finalQty = CurrentQty;
                    Pulses = String.valueOf(CurrentQty);
                    test_pass_bottom();
                    Toast.makeText(getApplicationContext(), "Bottom test pass", Toast.LENGTH_LONG).show();
                    AppCommon.WriteInFile(PulsarTestActivity.this, TAG + " Bottom test pass");
                }
            }
        } catch (Exception e) {
            AppCommon.WriteInFile(PulsarTestActivity.this, TAG + " Exception in splitRespStr: " + e.getMessage());
        }
    }
}