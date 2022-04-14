package com.example.btlinktestingapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

public class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<HistoryRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = HistoryRecyclerViewAdapter.class.getSimpleName();
    ArrayList<HashMap<String, String>> ListOfHistory = new ArrayList<>();

    private Context mContext;

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
    public void onBindViewHolder(HistoryRecyclerViewAdapter.ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");


        holder.batchid.setText("BatchID: "+ListOfHistory.get(position).get("BatchId"));
        holder.date_time.setText(ListOfHistory.get(position).get("TestDateTime"));
        holder.BT_name.setText(ListOfHistory.get(position).get("UniqueLinkName"));
        holder.BT_mac.setText(ListOfHistory.get(position).get("MacAddress"));
        holder.top_pulsar_test.setText(ListOfHistory.get(position).get("TopPulserTestResult"));
        holder.bottom_pulsar_test.setText(ListOfHistory.get(position).get("BottomPulserTestResult"));

        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(TAG, "DEVICE NAME: " + ListOfHistory.get(position).get("UniqueLinkName") + " \nDEVICE MAC: " + ListOfHistory.get(position).get("MacAddress"));
                Toast.makeText(mContext, "DEVICE NAME: " + ListOfHistory.get(position).get("UniqueLinkName") + " \nDEVICE MAC: " + ListOfHistory.get(position).get("MacAddress"), Toast.LENGTH_SHORT).show();

            }
        });


    }


    @Override
    public int getItemCount() {
        return ListOfHistory.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView batchid, date_time, BT_name, BT_mac, top_pulsar_test, bottom_pulsar_test;
        LinearLayout parentLayout;

        public ViewHolder(View itemView) {
            super(itemView);

            batchid = itemView.findViewById(R.id.batchid);
            date_time = itemView.findViewById(R.id.date_time);
            BT_name = itemView.findViewById(R.id.BT_name);
            BT_mac = itemView.findViewById(R.id.BT_mac);
            top_pulsar_test = itemView.findViewById(R.id.top_pulsar_test);
            bottom_pulsar_test = itemView.findViewById(R.id.bottom_pulsar_test);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }
    }

}



