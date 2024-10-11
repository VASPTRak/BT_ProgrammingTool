package com.example.btlinktestingapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.brother.ptouch.sdk.Printer;
import com.brother.ptouch.sdk.PrinterInfo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<HistoryRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = HistoryRecyclerViewAdapter.class.getSimpleName();
    ArrayList<HashMap<String, String>> ListOfHistory = new ArrayList<>();

    private Context mContext;
    Button btn_print3;
    protected PrinterInfo printerInfo;
    public static Printer myPrinter;
    public static Bitmap ImageToPrint;


    public HistoryRecyclerViewAdapter(HistoryActivity historyActivity, ArrayList<HashMap<String, String>> listOfHistoryData) {

        mContext = historyActivity;
        ListOfHistory = listOfHistoryData;

    }

    @Override
    public HistoryRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_history_itemlist, parent, false);
        HistoryRecyclerViewAdapter.ViewHolder holder = new HistoryRecyclerViewAdapter.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(HistoryRecyclerViewAdapter.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        Log.d(TAG, "onBindViewHolder: called.");

        holder.batchid.setText("BatchID: "+ListOfHistory.get(position).get("BatchId"));
        holder.date_time.setText(ListOfHistory.get(position).get("TestDateTime"));
        holder.BT_name.setText(ListOfHistory.get(position).get("LinkNameFromAPP"));
        holder.BT_mac.setText(ListOfHistory.get(position).get("MacAddress"));
        holder.top_pulsar_test.setText(ListOfHistory.get(position).get("TopPulserTestResult"));
        holder.bottom_pulsar_test.setText(ListOfHistory.get(position).get("BottomPulserTestResult"));
        holder.testName.setText(ListOfHistory.get(position).get("LINKHardwareTestCaseName"));

        String topTestResult = ListOfHistory.get(position).get("TopPulserTestResult");
        String bottomTestResult = ListOfHistory.get(position).get("BottomPulserTestResult");

        if (topTestResult.contains("F") || bottomTestResult.contains("F")) {
            holder.btn_Retest.setVisibility(View.VISIBLE);
        } else {
            holder.btn_Retest.setVisibility(View.GONE);
        }

        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(TAG, "DEVICE NAME: " + ListOfHistory.get(position).get("LinkNameFromAPP") + " \nDEVICE MAC: " + ListOfHistory.get(position).get("MacAddress"));
                Toast.makeText(mContext, "DEVICE NAME: " + ListOfHistory.get(position).get("LinkNameFromAPP") + " \nDEVICE MAC: " + ListOfHistory.get(position).get("MacAddress"), Toast.LENGTH_SHORT).show();

            }
        });

        holder.btn_print3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppCommon.IsPrint = true;
                String LinkNameFromAPP = ListOfHistory.get(position).get("LinkNameFromAPP");
                AppCommon.LinkNameToPrint = LinkNameFromAPP;
                Intent i = new Intent(mContext, ScanDeviceActivity.class);
                mContext.startActivity(i);
            }
        });

        holder.btn_Retest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String testIdForRetest;
                AppCommon.IsRetest = true;
                AppCommon.IsPrint = false;
                Intent i = new Intent(mContext, ScanDeviceActivity.class);
                AppCommon.batchIDForRetest = ListOfHistory.get(position).get("BatchId");
                testIdForRetest = ListOfHistory.get(position).get("LinkHardwareTestCaseId");
                SharedPreferences sharedPref =mContext.getSharedPreferences("PulseValue", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("TestCaseId",testIdForRetest);
                editor.apply();


                mContext.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return ListOfHistory.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView batchid, date_time, BT_name, BT_mac, top_pulsar_test, bottom_pulsar_test, testName;
        LinearLayout parentLayout;
        Button btn_print3, btn_Retest;

        public ViewHolder(View itemView) {
            super(itemView);

            batchid = itemView.findViewById(R.id.batchid);
            date_time = itemView.findViewById(R.id.date_time);
            BT_name = itemView.findViewById(R.id.BT_name);
            BT_mac = itemView.findViewById(R.id.BT_mac);
            top_pulsar_test = itemView.findViewById(R.id.top_pulsar_test);
            bottom_pulsar_test = itemView.findViewById(R.id.bottom_pulsar_test);
            parentLayout = itemView.findViewById(R.id.parent_layout);
            btn_print3 = itemView.findViewById(R.id.btnPrint3);
            btn_Retest = itemView.findViewById(R.id.btnRetest);
            testName = itemView.findViewById(R.id.test_name);

        }
    }

}



