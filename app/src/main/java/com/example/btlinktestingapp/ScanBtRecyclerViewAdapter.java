package com.example.btlinktestingapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ScanBtRecyclerViewAdapter extends RecyclerView.Adapter<ScanBtRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "RecyclerViewAdapter";

    public static ArrayList<String> NearByBTDevices = new ArrayList<>();
    private ArrayList<String> mBTNames = new ArrayList<>();
    private ArrayList<String> mBTMac = new ArrayList<>();
    private Context mContext;
    boolean isBTDeviceFound = false;

    public ScanBtRecyclerViewAdapter(Context context, ArrayList<String> BTNames, ArrayList<String> BTsMac) {
        mBTNames = BTNames;
        mBTMac = BTsMac;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_scanbt_listitem, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");

        try{

        if (mBTNames.get(position).equalsIgnoreCase("FSBT_Undertest")) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.parentLayout.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E79104")));
                holder.BTName.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                holder.BT_mac.setTextColor(ContextCompat.getColor(mContext, R.color.white));
            }else{
                holder.parentLayout.setBackgroundColor(R.color.yellow);
                holder.BTName.setTextColor(R.color.white);
                holder.BT_mac.setTextColor(R.color.white);
            }

        }

            if (mBTNames.stream().anyMatch(element -> element.contains(AppCommon.selectedLinkType))) {
            holder.BTName.setText(mBTNames.get(position));
            holder.BT_mac.setText(mBTMac.get(position));
            isBTDeviceFound = true;
        }
        else{
            if(AppCommon.IsPrint){
                holder.BTName.setText(mBTNames.get(position));
                holder.BT_mac.setText(mBTMac.get(position));
                isBTDeviceFound = true;
            }
        }

        }catch (Exception e){
            e.printStackTrace();
        }



        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isBTDeviceFound) {
                    if (NearByBTDevices.contains(mBTMac.get(position))) {
                        Log.d(TAG, "DEVICE NAME: " + mBTNames.get(position) + " \nDEVICE MAC: " + mBTMac.get(position));
                        //Toast.makeText(mContext, "DEVICE NAME: " + mBTNames.get(position) + " \nDEVICE MAC: " + mBTMac.get(position), Toast.LENGTH_SHORT).show();

                    /*//Start service..
                    Intent i = new Intent(mContext,LinkBlueService.class);
                    i.putExtra("DeviceName", mBTNames.get(position));
                    i.putExtra("DeviceMac", mBTMac.get(position));
                    mContext.startService(i);*/

                        //open next activity
                        Intent intent = null;
                        if (AppCommon.IsPrint) {
                            intent = new Intent(mContext, LabelPrintingActivity.class);
                        } else {
                            if (AppCommon.chk_astlink_status.equalsIgnoreCase("Y") || !mBTNames.stream().anyMatch(element -> element.startsWith("FSBT"))) {
                                intent = new Intent(mContext, AstPulsarTestActivity.class);
                            } else {
                                intent = new Intent(mContext, PulsarTestActivity.class);
                            }
                        }

                        isBTDeviceFound = false;

                        intent.putExtra("DeviceName", mBTNames.get(position));
                        intent.putExtra("DeviceMac", mBTMac.get(position));
                        mContext.startActivity(intent);

                    }
                }else{
                    Log.d(TAG, "DEVICE NAME: " + mBTNames.get(position) + " \nDEVICE MAC: " + mBTMac.get(position));
                    Toast.makeText(mContext, "Selected device is not active please check!!\nDEVICE NAME: " + mBTNames.get(position) + " \nDEVICE MAC: " + mBTMac.get(position), Toast.LENGTH_SHORT).show();
                }

            }
        });


    }

    

    @Override
    public int getItemCount() {
        return mBTNames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView BTName, BT_mac;
        LinearLayout parentLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            BTName = itemView.findViewById(R.id.BT_name);
            BT_mac = itemView.findViewById(R.id.BT_mac);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }
    }

}



