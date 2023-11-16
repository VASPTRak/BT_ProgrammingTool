package com.example.btlinktestingapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class ScanWifiActivity extends AppCompatActivity {

    private ArrayList<String> wifiList = new ArrayList<>();
    private WifiManager wifiManager;
    private BroadcastReceiver wifiScanReceiver;
    private WifiListAdapter wifiListAdapter;
    private RecyclerView recyclerView;
    private static final String TAG = "ScanWifiActivity";

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
        setContentView(R.layout.activity_scan_wifi);

        recyclerView = findViewById(R.id.rvWifi);
        wifiListAdapter = new WifiListAdapter(this, wifiList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(wifiListAdapter);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        // Register a BroadcastReceiver to receive Wi-Fi scan results
        wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
                    List<ScanResult> scanResults = wifiManager.getScanResults();
                    wifiList.clear();
                    for (ScanResult scanResult : scanResults) {
                        String ssid = scanResult.SSID;
                        if (ssid.startsWith("FS-")) {
                            wifiList.add(ssid);
                        }                    }
                    wifiListAdapter.notifyDataSetChanged(); // Notify the adapter of data changes
                }
            }
        };

        // Start a Wi-Fi scan
        wifiManager.startScan();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register the BroadcastReceiver
        registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the BroadcastReceiver to avoid memory leaks
        unregisterReceiver(wifiScanReceiver);
    }
}
