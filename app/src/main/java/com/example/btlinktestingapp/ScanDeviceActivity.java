package com.example.btlinktestingapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class ScanDeviceActivity extends AppCompatActivity {

    private static final String TAG = "ScanDeviceActivity ";
    public final static int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter mBluetoothAdapter;
    private TextView tv_message;
    Timer Scantimer;
    int Count = 0;
    RecyclerView recycleierVw;
    ScanBtRecyclerViewAdapter adapter;

    //vars
    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<String> mImageUrls = new ArrayList<>();
    private BluetoothAdapter bluetoothAdapter;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reader, menu);
        menu.findItem(R.id.mreload).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (item.getItemId()) {

            case R.id.mreload:
                this.recreate();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_device);
        //setTitle("Select device");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Button btn_pair_newdevices = (Button) findViewById(R.id.btn_pairnewdevice);
        Button btn_back = (Button) findViewById(R.id.btn_back);
        tv_message = (TextView) findViewById(R.id.tv_message);


        btn_pair_newdevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intent);
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(ScanDeviceActivity.this, LaunchingActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();

        recycleierVw = (RecyclerView) findViewById(R.id.recyclerv_view);
        adapter = new ScanBtRecyclerViewAdapter(this, mNames, mImageUrls);
        initRecyclerView();
        bluetoothScanning();
        // GetPairedDevicesList();
        //ScanBtRecyclerViewAdapter.NearByBTDevices.clear();
        //mBluetoothAdapter.startDiscovery();
        CheckFordevices();
    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
        /*Intent i = new Intent(PulsarTestActivity.this, LaunchingActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);*/
    }


    private void GetPairedDevicesList() {

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // Get paired devices.
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                if (!mImageUrls.contains(deviceHardwareAddress)) {
                    mImageUrls.add(deviceHardwareAddress);
                    mNames.add(deviceName);
                    Log.i(TAG, "DeviceName:" + deviceName + "\n" + "MacAddress:" + deviceHardwareAddress);
                }
            }
        }

        //initRecyclerView();
    }


    private void initRecyclerView() {
        Log.d(TAG, "initRecyclerView: init recyclerview.");
        recycleierVw.setAdapter(adapter);
        recycleierVw.setLayoutManager(new LinearLayoutManager(this));
    }

    void bluetoothScanning() {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                final BluetoothManager bluetoothManager =
                        (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                mBluetoothAdapter = bluetoothManager.getAdapter();
            }

            // mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            ScanBtRecyclerViewAdapter.NearByBTDevices.clear();
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mReceiver, filter);

            mBluetoothAdapter.startDiscovery();
        }catch (Exception e){
            e.printStackTrace();
            AppCommon.WriteInFile(ScanDeviceActivity.this, TAG + "Exception in bluetoothScanning: " + e.getMessage());
        }

    }


    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                try {

                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address

                    if (!ScanBtRecyclerViewAdapter.NearByBTDevices.contains(deviceHardwareAddress)) {
                        ScanBtRecyclerViewAdapter.NearByBTDevices.add(deviceHardwareAddress);
                        Log.i(TAG, "BT Scan deviceName:" + deviceName + " MacAddress:" + deviceHardwareAddress);
                    }

                    if (!mImageUrls.contains(deviceHardwareAddress)) {
                        mImageUrls.add(deviceHardwareAddress);
                        mNames.add(deviceName);
                        Log.i(TAG, "DeviceName:" + deviceName + "\n" + "MacAddress:" + deviceHardwareAddress);
                    }

                    adapter.notifyDataSetChanged();
                    // initRecyclerView();

                } catch (Exception e) {
                    e.printStackTrace();
                    AppCommon.WriteInFile(ScanDeviceActivity.this, TAG + "Exception in BroadcastReceiver: " + e.getMessage());
                }
            }

        }
    };

    public void CheckFordevices() {

        try {
            Count = 0;
            Scantimer = new Timer();
            TimerTask ttSensor = new TimerTask() {
                @Override
                public void run() {

                    if (mImageUrls != null && mImageUrls.size() > 0) {
                        String msg = "Please select “FSBT_Undertest”";
                        if (AppCommon.IsPrint) {
                            msg = "Please select printer";
                        }
                        runthisOnUi(msg);
                        Scantimer.cancel();
                    } else {
                        if (Count > 15) {
                            runthisOnUi("No device found");
                            Scantimer.cancel();
                        }
                    }
                    Count++;
                }

            };
            Scantimer.schedule(ttSensor, 1000, 1000);
        } catch (Exception e) {
            e.printStackTrace();
            AppCommon.WriteInFile(ScanDeviceActivity.this, TAG + "Exception in CheckFordevices: " + e.getMessage());
        }
    }

    public void runthisOnUi(String msg){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_message.setText(" "+msg+" ");
            }
        });
    }
}