package com.example.btlinktestingapp;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AppCommon {

    public static String chk_changelinkname_status = "Y";
    public static String chk_astlink_status = "N";
    public static String FOLDER_BIN = "FSBin";
    public static boolean IsNewBTFirmware = false;
    public static boolean IsPrint = true;
    //public static boolean IsPrintFromHistory = true;
    public static int FSBT_linkQtyToTest = 1;
    public static String printerMacAddress = "";
    public static String LinkNameToPrint = "";
    public static String isbtnContinuePressed = "False";
    public static String TestCaseId = "";

    public static void WriteInternalFile(LaunchingActivity launchingActivity, String data) {

        String filename = "demoFile.txt";

        try
        {
            FileOutputStream fos = launchingActivity.openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write(data.getBytes());
            fos.flush();
            fos.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    public static String ReadInternalFile(LaunchingActivity launchingActivity) {

        StringBuilder temp = null;
        try {
            FileInputStream fin = launchingActivity.openFileInput("demoFile.txt");
            int a;
            temp = new StringBuilder();
            while ((a = fin.read()) != -1) {
                temp.append((char) a);
            }

            fin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return String.valueOf(temp);
    }

    public static String getTodaysDateInString() {

        String CurrantDate = "";
        try {
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            CurrantDate = df.format(c.getTime());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return (CurrantDate);
    }

    public static String getDateInStringForlogFileName() {

        String CurrantDate = "";
        try {
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
            CurrantDate = df.format(c.getTime());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return (CurrantDate);
    }

    public static String getVersionCode(Context ctx) {

        String versioncode = "";
        try {
            PackageInfo pInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
            versioncode = pInfo.versionName;

        } catch (Exception q) {
            System.out.println(q);
        }

        return versioncode;
    }

    public static void colorToastBigFont(Context ctx, String msg, int colr) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Toast toast = Toast.makeText(ctx, " " + msg + " ", Toast.LENGTH_LONG);
            toast.getView().setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ba160c")));
            toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 280);
            ViewGroup group = (ViewGroup) toast.getView();
            TextView messageTextView = (TextView) group.getChildAt(0);
            messageTextView.setTextSize(25);
            toast.show();

        } else {
            Toast toast = Toast.makeText(ctx, " " + msg + " ", Toast.LENGTH_LONG);
            toast.getView().setBackgroundColor(colr);
            toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 280);
            ViewGroup group = (ViewGroup) toast.getView();
            TextView messageTextView = (TextView) group.getChildAt(0);
            messageTextView.setTextSize(25);
            toast.show();
        }
    }

    public static void WriteInFile(Context ctx, String str) {
        try {

            if (str.contains("Responce"))
                str = str.replace("Responce", "Response");

            System.out.println(str);

            File file = new File(Environment.getExternalStorageDirectory() + "/BTProgrammingTool");
            //File file = new File(String.valueOf(ctx.getExternalFilesDir("BTProgrammingTool")));

            if (!file.exists()) {
                if (file.mkdirs()) {
                    //System.out.println("Create FSLog Folder");
                } else {
                     //System.out.println("Fail to create KavachLog folder");
                }
            }

            String dt = GetDateString(System.currentTimeMillis());
            File gpxfile = new File(file + "/Log_" + dt + ".txt");
            if (!gpxfile.exists()) {
                gpxfile.createNewFile();
            }

            Calendar cal = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd HH:mm:ss");
            String UseDate = dateFormat.format(cal.getTime());

            FileWriter fileWritter = new FileWriter(gpxfile, true);
            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
            bufferWritter.write("\n" + UseDate + "--" + str + " ");
            bufferWritter.close();

        }  catch (Exception e) {
            //WriteInFile(ctx, "WriteInFile Exception" + e);
            Log.e("AppCommon","WriteInFile Exception" + e);
        }
    }

    public static String GetDateString(Long dateinms) {
        try {
            Time myDate = new Time();
            myDate.set(dateinms);
            return myDate.format("%Y-%m-%d");
        } catch (Exception e1) {
            return "";
        }
    } // Create logger functionality

}
