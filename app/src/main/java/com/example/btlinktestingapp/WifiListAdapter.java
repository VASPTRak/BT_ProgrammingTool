package com.example.btlinktestingapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class WifiListAdapter extends RecyclerView.Adapter<WifiListAdapter.ViewHolder> {

    private ArrayList<String> wifiList;
    private Context context;

    public WifiListAdapter(Context context, ArrayList<String> wifiList) {
        this.context = context;
        this.wifiList = wifiList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_scanwifi_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String ssid = wifiList.get(position);
        holder.textViewSSID.setText(ssid);

        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = null;
                AppCommon.selectedSSID = ssid;
                if (AppCommon.IsPrint) {
                    intent = new Intent(context, LabelPrintingActivity.class);
                } else {
                        intent = new Intent(context, PulserTestWifiActivity.class);
                }
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return wifiList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewSSID;
        LinearLayout parentLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewSSID = itemView.findViewById(R.id.wifi_name);
            parentLayout = itemView.findViewById(R.id.parent_layout1);
        }
    }
}
