package com.example.btlinktestingapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.brother.ptouch.sdk.Printer;
import com.brother.ptouch.sdk.PrinterInfo;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AstPulsarTestActivity extends AppCompatActivity implements View.OnClickListener {

    private String mDeviceAddress = "", mDeviceName = "";
    private BTLinkLeServiceCode mBluetoothLeService;
    public static String btLinkResponse, btLinkPulses;
    public static String QR_ReaderStatus = "QR Waiting..";
    private static final String TAG = AstPulsarTestActivity.class.getSimpleName();
    Timer timer;
    private int CurrentTest = 0, CurrentQty = 0, previousQty = 0;
    public boolean pulseStarted = false, pass_clicked = false;
    protected PrinterInfo printerInfo;
    public static Printer myPrinter;
    public static Bitmap ImageToPrint;
    TextView tv_qty, tv_qtyb;
    //btn_pass_test
    Button btn_conn_status,  btn_fail_test, btn_set_no, btn_finish, btn_save_batchid, btn_go_to_top, btn_print3;
    EditText edt_set_no, edt_batch_number;
    ConstraintLayout layout_toptest, layout_prepare_toptest;
    String batchID = "", TopTestResult = "F",BottomTestResult = "-", TestResult = "Incomplete";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ast_pulsar_test);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        layout_toptest = (ConstraintLayout) findViewById(R.id.layout_toptest);
        layout_prepare_toptest = (ConstraintLayout) findViewById(R.id.layout_prepare_toptest);
        btn_conn_status = (Button) findViewById(R.id.btn_conn_status);
       // btn_pass_test = (Button) findViewById(R.id.btn_pass_test);
        btn_fail_test = (Button) findViewById(R.id.btn_fail_test);
        btn_set_no = (Button) findViewById(R.id.btn_set_no);
        btn_finish = (Button) findViewById(R.id.btn_finish);
        btn_save_batchid = (Button) findViewById(R.id.btn_save_batchid);
        btn_go_to_top = (Button) findViewById(R.id.btn_go_to_top);
        btn_print3 = (Button) findViewById(R.id.btnPrint3);
        tv_qty = (TextView) findViewById(R.id.tv_qty);
        edt_set_no = (EditText) findViewById(R.id.edt_set_no);
        edt_batch_number = (EditText) findViewById(R.id.edt_batch_number);

        btn_conn_status.setOnClickListener(this);
      //  btn_pass_test.setOnClickListener(this);
        btn_fail_test.setOnClickListener(this);
        btn_set_no.setOnClickListener(this);
        btn_finish.setOnClickListener(this);
        btn_save_batchid.setOnClickListener(this);
        btn_go_to_top.setOnClickListener(this);
        btn_print3.setOnClickListener(this);


        Intent intent = getIntent();
        mDeviceName = intent.getStringExtra("DeviceName");
        mDeviceAddress = intent.getStringExtra("DeviceMac");

        /*mDeviceName = "LINK_BLUE";
        mDeviceAddress = "10:52:1C:85:8C:FA";*/
        //GenerateSrNo(mDeviceAddress);

        Intent gattServiceIntent = new Intent(this, BTLinkLeServiceCode.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());


    }


    public Bitmap textToBitmap(String text, float textSize, int textColor) {
        Bitmap image = null;
        try {
            text = text + " : " + text + " : " + text;

            Paint paint = new Paint();
            paint.setTextSize(textSize);
            paint.setColor(Color.WHITE); // Color.parseColor("#FAF9F6")); //#FAF9F6
            paint.setTextAlign(Paint.Align.LEFT);
            //paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            float baseline = -paint.ascent();
            int width = (int) (paint.measureText(text + "   ") + 0.5f);
            int height = (int) (baseline + paint.ascent() + 0.5f);
            image = Bitmap.createBitmap(width + 20, height + 100, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(image);
            canvas.drawRect(0, 0, width + 20, height + 100, paint);
            paint.setColor(textColor);
            canvas.drawText(text + "   ", 0, baseline, paint);

        } catch (Exception e) {
            Log.i(TAG, "Exception in textToBitmap: " + e.getMessage());
            AppCommon.WriteInFile(AstPulsarTestActivity.this, TAG + "Exception in textToBitmap: " + e.getMessage());
        }
        return image;
    }


    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int height) {
        float ratio = Math.min(
                (float) newWidth / bm.getWidth(),
                (float) height / bm.getHeight());
        int width = Math.round((float) ratio * bm.getWidth());

        return Bitmap.createScaledBitmap(bm, width, height, false);
    }

    //region Method 1
    public void PrintLabels(String textToPrint) {

        try {


            //  String selectedPaperSize = "W12";


//            myPrinter = new Printer();
//            myPrinter.setBluetooth(BluetoothAdapter.getDefaultAdapter());
//
//            printerInfo = myPrinter.getPrinterInfo();
//            printerInfo.printerModel = PrinterInfo.Model.PT_P300BT;
//            printerInfo.port = PrinterInfo.Port.BLUETOOTH;
//            printerInfo.paperSize = PrinterInfo.PaperSize.CUSTOM;
//            printerInfo.orientation = PrinterInfo.Orientation.LANDSCAPE;
//            printerInfo.align = PrinterInfo.Align.LEFT;
//            printerInfo.printMode = PrinterInfo.PrintMode.FIT_TO_PAGE;
//            printerInfo.numberOfCopies = 1;
//            printerInfo.printQuality = PrinterInfo.PrintQuality.HIGH_RESOLUTION;
//            printerInfo.macAddress = printerMacAddress;
//            printerInfo.workPath = getApplicationContext().getCacheDir().getPath(); //String.valueOf(getApplicationContext().getExternalFilesDir("PrintMaterial"));
//            //printerInfo.trimTapeAfterData = true;
//            printerInfo.margin.left = 0;
//            printerInfo.margin.top = 0;
//
//            printerInfo.labelNameIndex = LabelInfo.PT3.valueOf(selectedPaperSize).ordinal();
//            printerInfo.labelMargin = 0;
//            printerInfo.isAutoCut = false;
//            printerInfo.isCutAtEnd = true;
//            printerInfo.isHalfCut = false;
//            printerInfo.isSpecialTape = false;
//            printerInfo.isCutMark = true;
//
//            myPrinter.setPrinterInfo(printerInfo);

            ImageToPrint = textToBitmap(textToPrint, 90, Color.BLACK);
            //bitmapToFile(LabelPrintingActivity.this, ImageToPrint, "myLabel1.png");
            ImageToPrint = getResizedBitmap(ImageToPrint, ImageToPrint.getWidth() / 3, ImageToPrint.getHeight());
            bitmapToFile(AstPulsarTestActivity.this, ImageToPrint, mDeviceName+".png");

            // print2();
        } catch (Exception e) {
            e.printStackTrace();
            AppCommon.WriteInFile(AstPulsarTestActivity.this, TAG + "Exception in PrintLabels: " + e.getMessage());
        }
    }

    public static File bitmapToFile(Context context, Bitmap bitmap, String fileNameToSave) {
        //create a file to write bitmap data
        File file = null;
        try {
            file = new File(context.getExternalFilesDir("PrintMaterial") + "/" + fileNameToSave);
            file.createNewFile();

            //Convert bitmap to byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0 , bos);
            byte[] bitmapdata = bos.toByteArray();

            //write the bytes in file
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            AppCommon.WriteInFile(context, TAG + "Exception in bitmapToFile: " + e.getMessage());
            return file; // it will return null
        }
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

        if (TestResult.equalsIgnoreCase("Incomplete")) {
            Log.i(TAG, "*** Result ***\n MacAddress:" + mDeviceAddress + " LinkNameFromAPP:" + mDeviceName + "TestDateTime: 06/10/2021 BatchId:" + batchID + " TopPulserTestResult:" + TopTestResult +" TestResult" + TestResult);
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
            final String action = intent.getAction();
            if (BTLinkLeServiceCode.ACTION_GATT_CONNECTED.equals(action)) {


                QR_ReaderStatus = "QR Connected";
                System.out.println("ACTION_GATT_QR_CONNECTED");
                getBatchNumber();

            } else if (BTLinkLeServiceCode.ACTION_GATT_DISCONNECTED.equals(action)) {

                QR_ReaderStatus = "QR Disconnected";
                AstPulsarTestActivity.btLinkResponse = "BT Disconnected";
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

                if (validatebatchid(batchID)) {
                    //Server call...
                    if (isConnecting()) {
                        try {
                            new AstPulsarTestActivity.checkbatchidunqiue().execute(batchID);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {
                        Toast.makeText(this, "Please check network connection", Toast.LENGTH_LONG).show();
                    }
                }


                break;

//            case R.id.btn_pass_test:  //correct one
//                RelayOffCommand();
//                TopTestResult = "P";
//                //Server call...
//                if (isConnecting()) {
//                    cancelTimer();
//                    layout_toptest.setVisibility(View.GONE);
//                    try {
//                        new AstPulsarTestActivity.UpdateDetails().execute(mDeviceName, mDeviceAddress, batchID, TopTestResult, BottomTestResult);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                } else {
//                    Toast.makeText(this, "Please check network connection", Toast.LENGTH_LONG).show();
//                }
//
//                break;

            case R.id.btn_fail_test: //correct one
                RelayOffCommand();
                TopTestResult = "F";
                layout_toptest.setVisibility(View.GONE);
                //Server call...
                if (isConnecting()) {
                    cancelTimer();
                    layout_toptest.setVisibility(View.GONE);
                    try {
                        new AstPulsarTestActivity.UpdateDetails().execute(mDeviceName, mDeviceAddress, batchID, TopTestResult, BottomTestResult);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    Toast.makeText(this, "Please check network connection", Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.btn_set_no:

                String cmd = edt_set_no.getText().toString().trim();
                if (cmd != null && cmd.startsWith("FSAST-") && QR_ReaderStatus.equalsIgnoreCase("QR Connected")) {

                    if (isConnecting()) {
                        try {
                            mBluetoothLeService.writeCustomCharacteristic("LK_COMM=name:" + cmd);
                            edt_set_no.setVisibility(View.GONE);
                            btn_set_no.setVisibility(View.GONE);
                            new AstPulsarTestActivity.setserialnumber().execute(batchID, cmd);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {
                        Toast.makeText(this, "Please check network connection", Toast.LENGTH_LONG).show();
                    }

                } else {
                    Toast.makeText(this, "Something went wrong. Please try again", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.btn_finish:
                Intent i = new Intent(AstPulsarTestActivity.this, LaunchingActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                break;
            case R.id.btn_go_to_top:

                CurrentTest = 1;
                CurrentQty = 0;
                previousQty = 0;
                layout_prepare_toptest.setVisibility(View.GONE);
                layout_toptest.setVisibility(View.VISIBLE);
                //layout_bottomtest.setVisibility(View.VISIBLE);

                //mBluetoothLeService.writeCustomCharacteristic("LK_COMM=info");
                mBluetoothLeService.writeCustomCharacteristic("LK_COMM=relay:12345=ON");

                break;

            case R.id.btnPrint3:
//                PrintLabels(mDeviceName);
//                Toast.makeText(AstPulsarTestActivity.this, "Image saved", Toast.LENGTH_SHORT).show();
                AppCommon.IsPrint = true;
                AppCommon.LinkNameToPrint = mDeviceName;
                Intent k = new Intent(AstPulsarTestActivity.this, ScanDeviceActivity.class);
                AstPulsarTestActivity.this.startActivity(k);
                break;
        }
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
                            splitRespStr(respStr);
                        } else {
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
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public class UpdateDetails extends AsyncTask<String, Void, String> {


        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(AstPulsarTestActivity.this);
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
                serverCallComplete("", false);
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
                        serverCallComplete(UniqueLinkName, true);

                    } else {
                        Log.i(TAG, "API Call fail" + result);
                        serverCallComplete(UniqueLinkName, false);
                    }

                } catch (JSONException e) {
                    serverCallComplete("", false);
                    e.printStackTrace();
                    pd.cancel();
                }

            } else {
                serverCallComplete("", false);
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
            new AstPulsarTestActivity.setserialnumber().execute(batchID, uniqueLinkName);
        } else {
            TestResult = "Incomplete";
            //Toast.makeText(this, "Something went wrong. please try again.", Toast.LENGTH_LONG).show();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                btn_finish.setVisibility(View.VISIBLE);
                btn_print3.setVisibility(View.VISIBLE);
            }
        }, 2000);

        /////////////////////////////////////////////////////////////////////////////
        /*if (!uniqueLinkName.equals("") && AppCommon.chk_changelinkname_status.equalsIgnoreCase("Y")) {
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
            pd = new ProgressDialog(AstPulsarTestActivity.this);
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
            if (QR_ReaderStatus.equalsIgnoreCase("QR Connected")) {

                FDCheck();
                edt_batch_number.setVisibility(View.GONE);
                btn_save_batchid.setVisibility(View.GONE);
                layout_prepare_toptest.setVisibility(View.VISIBLE);
                hideKeyboard(AstPulsarTestActivity.this);

            } else {
                Toast.makeText(this, "Please wait. Connecting..", Toast.LENGTH_LONG).show();
            }

        } else {
            //AppCommon.colorToastBigFont(AstPulsarTestActivity.this,responseText,Color.RED);
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

    public class setserialnumber extends AsyncTask<String, Void, String> {


        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(AstPulsarTestActivity.this);
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

            btn_finish.setVisibility(View.VISIBLE);
            btn_print3.setVisibility(View.VISIBLE);
            if (result != null && !result.isEmpty()) {

                try {
                    pd.cancel();
                    JSONObject jsonObject = new JSONObject(result);

                    String ResponceMessage = jsonObject.getString("ResponseMessage");
                    String UniqueLinkName = jsonObject.getString("UniqueLinkName");

                    if (ResponceMessage.equalsIgnoreCase("success")) {
                        Log.i(TAG, "API Call Success" + result);
                        TestResult = "Completed";
                        Log.i(TAG, "*** Result ***\n MacAddress:" + mDeviceAddress + " LinkNameFromAPP:" + mDeviceName + "TestDateTime: 06/10/2021 BatchId:" + batchID + " TopPulserTestResult:" + TopTestResult + " BottomPulserTestResult:" + BottomTestResult + " TestResult" + TestResult);

                    } else {
                        Log.i(TAG, "API Call fail" + result);
                        TestResult = "InComplete";
                        Log.i(TAG, "*** Result ***\n MacAddress:" + mDeviceAddress + " LinkNameFromAPP:" + mDeviceName + "TestDateTime: 06/10/2021 BatchId:" + batchID + " TopPulserTestResult:" + TopTestResult + " BottomPulserTestResult:" + BottomTestResult + " TestResult" + TestResult);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    pd.cancel();
                }

            } else {
                pd.cancel();
                Log.i(TAG, "setserialnumber InPost Response err:" + result);
            }

        }
    }

    public void test_top(){

        RelayOffCommand();
                TopTestResult = "P";
                //Server call...
                if (isConnecting()) {
                    cancelTimer();
                    layout_toptest.setVisibility(View.GONE);
                    try {
                        new AstPulsarTestActivity.UpdateDetails().execute(mDeviceName, mDeviceAddress, batchID, TopTestResult, BottomTestResult);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(this, "Please check network connection", Toast.LENGTH_LONG).show();
                }



    }

    private void splitRespStr(String respStr) {

        try {
            if (respStr.contains("pulse:")) {

                String[] resp = respStr.split(":");
                CurrentQty = Integer.parseInt(resp[1].trim());
                Log.i(TAG, "Current qty:" + CurrentQty);
                if (CurrentTest == 1 && CurrentQty > 30 && previousQty == 0) {
                    previousQty = CurrentQty;
                  //  btn_pass_test.performClick();
                    test_top();
                    Toast.makeText(getApplicationContext(), "Top test pass", Toast.LENGTH_LONG).show();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}