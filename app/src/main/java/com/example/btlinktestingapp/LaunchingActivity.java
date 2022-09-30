package com.example.btlinktestingapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LaunchingActivity extends AppCompatActivity {


    private static final String TAG = LaunchingActivity.class.getSimpleName();
    public static String webIP = "http://sierravistatest.cloudapp.net";
    public static String API = webIP + "/api/External/getuniquehardwaretestlinkname";
    public static String API_GETHISTORY = webIP + "/api/External/gethardwaretestdetails";
    public static String API_UNIQUE_ID = webIP + "/api/External/checkbatchidunqiue";
    public static String API_SET_NO = webIP + "/api/External/setserialnumber";
    private static final int PERMISSION_REQUEST_CODE_CORSE_LOCATION = 4;
    public static final String PREFS_ISFIRSTTIME_USE = "AppFirstTimeUse";
    private CheckBox chk_changelinkname, chk_astlink;
    TextView tvSelectLink;
    ArrayList<HashMap<String, String>> linkList = new ArrayList<>();





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launching);

        TextView tv_appversion = (TextView) findViewById(R.id.tv_appversion);
        Button btn_go = (Button) findViewById(R.id.btn_go);
        Button btn_history = (Button) findViewById(R.id.btn_history);
        chk_changelinkname = (CheckBox) findViewById(R.id.chk_changelinkname);
        chk_astlink = (CheckBox) findViewById(R.id.chk_astlink);
        Button btnPrint = (Button) findViewById(R.id.btnPrint);
        tvSelectLink = (TextView) findViewById(R.id.tvSelectlinkname);
        ListView lvLinknames = (ListView) findViewById(R.id.lvlinknames);
        String[] links = getResources().getStringArray(R.array.links);

        for (int i=0;i<links.length;i++){
            HashMap<String, String> map = new HashMap<>();
            map.put("item", links[i]);
            linkList.add(map);

        }






        tv_appversion.setText("Version:" + AppCommon.getVersionCode(LaunchingActivity.this));
        //AppCommon.WriteExternalFile(LaunchingActivity.this, "Version:" + AppCommon.getVersionCode(LaunchingActivity.this));

        //AppCommon.ReadExternalFile(LaunchingActivity.this);

        //AppCommon.colorToastBigFont(LaunchingActivity.this,"responseText",Color.RED);
        //Find ad_hoc_toast textview
        //TextView ad_hoc_toast_textview = findViewById(R.id.ad_hoc_toast_textview);

        //Define the text to be shown
        //String text = "This is the custom toast message";

        //Show the ad_hoc toast
        //show_ad_hoc_toast(ad_hoc_toast_textview, text);
        if (tvSelectLink.isPressed()){
            alertSelectLinkList();
        }
        else
        {
            System.out.println("No links");
        }






        btn_go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCommon.IsPrint = false;
                Intent i = new Intent(LaunchingActivity.this, ScanDeviceActivity.class);//ScanDeviceActivity
                LaunchingActivity.this.startActivity(i);
            }
        });

        /*btnPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCommon.IsPrint = true;
                Intent i = new Intent(LaunchingActivity.this, ScanDeviceActivity.class); //LabelPrintingActivity
                //i.putExtra("DeviceName", "PT-P300BT4197"); //PT-P300BT4197
                //i.putExtra("DeviceMac", "EC:79:49:29:37:75"); //EC:79:49:29:37:75
                LaunchingActivity.this.startActivity(i);
            }
        });*/

        btn_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LaunchingActivity.this, HistoryActivity.class);
                LaunchingActivity.this.startActivity(i);
            }
        });

        chk_astlink.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                SharedPreferences.Editor editor = getSharedPreferences(PREFS_ISFIRSTTIME_USE, MODE_PRIVATE).edit();
                if (isChecked) {
                    AppCommon.chk_astlink_status = "Y";
                    editor.putBoolean("ast_link", true);
                    editor.apply();
                } else {
                    AppCommon.chk_astlink_status = "N";
                    editor.putBoolean("ast_link", false);
                    editor.apply();
                }

            }
        });


        chk_changelinkname.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                SharedPreferences.Editor editor = getSharedPreferences(PREFS_ISFIRSTTIME_USE, MODE_PRIVATE).edit();
                if (isChecked) {
                    AppCommon.chk_changelinkname_status = "Y";
                    editor.putBoolean("change_link_name", true);
                    editor.apply();
                } else {
                    AppCommon.chk_changelinkname_status = "N";
                    editor.putBoolean("change_link_name", false);
                    editor.apply();
                }

            }
        });

        try {
            checkPermissionTask checkPermissionTask = new checkPermissionTask();
            checkPermissionTask.execute();
            checkPermissionTask.get();

            if (checkPermissionTask.isValue) {
                //permissions already granted

            } else {

            }

        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            AppCommon.WriteInFile(LaunchingActivity.this, TAG + " Exception in checkPermissionTask: " + ex.getMessage());
        }

        //Enable bluetooth
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter.disable()) {
            mBluetoothAdapter.enable();
        }

        IsAppfirstTimeUse();

    }

//    public boolean CheckIfPresentInPairedDeviceList(String printerMacAddress){
//
//        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        // Get paired devices.
//        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
//        if (pairedDevices.size() > 0) {
//            // There are paired devices. Get the name and address of each paired device.
//            for (BluetoothDevice device : pairedDevices) {
//                String deviceName = device.getName();
//                String deviceHardwareAddress = device.getAddress(); // MAC address
//                if (deviceHardwareAddress.equalsIgnoreCase(printerMacAddress)){
//                    device.createBond();
//                    return true;
//                }
//            }
//
//        }
//        AppCommon.WriteInFile(LaunchingActivity.this, TAG + "Selected device is not in bluetooth pair devices list. (Device Name: " + "; Device Mac Address: " + printerMacAddress + ")");
//        return false;
//    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            mBluetoothAdapter.enable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class checkPermissionTask extends AsyncTask<Void, Void, Void> {
        boolean isValue = false;

        @Override
        protected Void doInBackground(Void... params) {

            isValue = TestPermissions();
            return null;
        }
    }

    private boolean TestPermissions() {
        boolean isValue = false;

        try {
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

            boolean isGranted = false;
            for (String per : permissions) {
                isGranted = checkPermission(LaunchingActivity.this, per);

                if (!isGranted) {
                    break;
                }
            }

            if (!isGranted) {
                ActivityCompat.requestPermissions(LaunchingActivity.this, permissions, PERMISSION_REQUEST_CODE_CORSE_LOCATION);
                isValue = false;
            } else {
                isValue = true;
            }

        } catch (Exception ex) {

        }

        return isValue;
    }

    private boolean checkPermission(Activity context, String permission) {

        int result = ContextCompat.checkSelfPermission(context, permission);
        if (result == PackageManager.PERMISSION_GRANTED) {

            return true;

        } else {

            return false;

        }
    }

    private void show_ad_hoc_toast(final TextView ad_hoc_toast_textview, String text) {


        //Set the text
        ad_hoc_toast_textview.setText(text);


        //Create alpha animation
        AlphaAnimation animation1 = new AlphaAnimation(0f, 1f);

        //Set duration
        animation1.setDuration(300);

        //Set that the animation changes persist once the animation finishes
        animation1.setFillAfter(true);


        //Set on AnimationEnd Listner
        animation1.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                //After 2250 millis -> hide the toast
                new CountDownTimer(2250, 1) {
                    public void onTick(long millisUntilFinished) {
                    }

                    public void onFinish() {
                        hide_ad_hoc_toast(ad_hoc_toast_textview);
                    }
                }.start();


            }

        });


        //Make the view visible
        ad_hoc_toast_textview.setVisibility(View.VISIBLE);


        //Start animation
        ad_hoc_toast_textview.startAnimation(animation1);


    }

    private void hide_ad_hoc_toast(final TextView ad_hoc_toast_textview) {


        //Create alpha animation
        AlphaAnimation animation1 = new AlphaAnimation(1f, 0f);

        //Set duration
        animation1.setDuration(300);

        //Set that the animation changes persist once the animation finishes
        animation1.setFillAfter(true);


        //Set on AnimationEnd Listner
        animation1.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                //Make the view gone
                ad_hoc_toast_textview.setVisibility(View.GONE);

            }

        });


        //Start animation
        ad_hoc_toast_textview.startAnimation(animation1);


    }

    private void IsAppfirstTimeUse() {

        SharedPreferences prefs = getSharedPreferences(PREFS_ISFIRSTTIME_USE, MODE_PRIVATE);
        boolean ast_link = prefs.getBoolean("ast_link", false);
        boolean change_link_name = prefs.getBoolean("change_link_name", true);

        if (ast_link) {
            AppCommon.chk_astlink_status = "Y";
            chk_astlink.setChecked(true);
        } else {
            AppCommon.chk_astlink_status = "N";
            chk_astlink.setChecked(false);
        }

        if (change_link_name){
            AppCommon.chk_changelinkname_status = "Y";
            chk_changelinkname.setChecked(true);
        }else{
            AppCommon.chk_changelinkname_status = "N";
            chk_changelinkname.setChecked(false);
        }
    }

    public void selectLinkAction(View v) {
        alertSelectLinkList();

    }




    public void alertSelectLinkList() {


        final Dialog dialog = new Dialog(LaunchingActivity.this);
        dialog.setTitle("BT Links");
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.link_list);

        TextView tvNoLinks = (TextView) dialog.findViewById(R.id.tvnolinks);
        ListView lvlinkNames = (ListView) dialog.findViewById(R.id.lvlinknames);
        Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);


        if (tvSelectLink.isPressed() ) {
            lvlinkNames.setVisibility(View.VISIBLE);


        } else {
            lvlinkNames.setVisibility(View.GONE);
            tvNoLinks.setVisibility(View.VISIBLE);
        }


        SimpleAdapter adapter = new SimpleAdapter(this,linkList , R.layout.item_link, new String[]{"item"}, new int[]{R.id.tvSingleItem});
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

                String selectedValue =  linkList.get(position).get("item");

                Intent intent = new Intent(LaunchingActivity.this, link_selected.class);;
                intent.putExtra("LinkType",selectedValue);
                startActivity(intent);




                dialog.dismiss();
            }
        });

        dialog.show();
    }


    public void selectLinkByPosition() {
        String[] selectLink = getResources().getStringArray(R.array.links);
                tvSelectLink.setText(selectLink.length);


        }



}