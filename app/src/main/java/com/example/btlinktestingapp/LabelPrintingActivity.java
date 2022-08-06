package com.example.btlinktestingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.hardware.usb.UsbManager;
import android.media.Image;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.brother.ptouch.sdk.CustomPaperInfo;
import com.brother.ptouch.sdk.LabelInfo;
import com.brother.ptouch.sdk.PaperKind;
import com.brother.ptouch.sdk.Printer;
import com.brother.ptouch.sdk.PrinterInfo;
import com.brother.ptouch.sdk.PrinterStatus;
import com.brother.ptouch.sdk.Unit;
import com.brother.sdk.lmprinter.Channel;
import com.brother.sdk.lmprinter.OpenChannelError;
import com.brother.sdk.lmprinter.PrintError;
import com.brother.sdk.lmprinter.PrinterDriver;
import com.brother.sdk.lmprinter.PrinterDriverGenerateResult;
import com.brother.sdk.lmprinter.PrinterDriverGenerator;
import com.brother.sdk.lmprinter.PrinterModel;
import com.brother.sdk.lmprinter.setting.MWPrintSettings;
import com.brother.sdk.lmprinter.setting.PJPrintSettings;
import com.brother.sdk.lmprinter.setting.PTPrintSettings;
import com.brother.sdk.lmprinter.setting.PrintImageSettings;
import com.brother.sdk.lmprinter.setting.PrintSettings;
import com.brother.sdk.lmprinter.setting.QLPrintSettings;
import com.brother.sdk.lmprinter.setting.RJPrintSettings;
import com.brother.sdk.lmprinter.setting.TDPrintSettings;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

public class LabelPrintingActivity extends AppCompatActivity {

    private static final String TAG = "LabelPrintingActivity ";
    private String printerName = "", printerMacAddress = "";
    public static Printer myPrinter;
    public static Bitmap ImageToPrint;
    protected PrinterStatus printResult;
    protected PrinterInfo printerInfo;
    Button btn_finish;
    //public RadioGroup rdSizeGroup;
    //public RadioButton rdSelectedSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_label_printing);

        //sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        AppCommon.WriteInFile(LabelPrintingActivity.this, TAG + "Started-----");
        EditText etLabelToPrint = (EditText) findViewById(R.id.etLabelToPrint);
        Button btnPrintLabel = (Button) findViewById(R.id.btnPrintLabel);
        //Button btnPrint2 = (Button) findViewById(R.id.btnPrint2);
        Button btnPreview = (Button) findViewById(R.id.btnPreview);
        Button btn_finish = (Button) findViewById(R.id.btn_finish);
        TextView tvPrinterName = (TextView) findViewById(R.id.tvPrinterName);
        TextView tvPrinterMAC = (TextView) findViewById(R.id.tvPrinterMAC);
        //rdSizeGroup = (RadioGroup) findViewById(R.id.rdSizeGroup);

        Intent intent = getIntent();
        printerName = intent.getStringExtra("DeviceName");
        printerMacAddress = intent.getStringExtra("DeviceMac");
        AppCommon.printerMacAddress = intent.getStringExtra("DeviceMac");
        AppCommon.WriteInFile(LabelPrintingActivity.this, TAG + "(printerName: " + printerName + "; printerMacAddress: " + printerMacAddress + ")");

        tvPrinterName.setText("Printer Name: " + printerName);
        tvPrinterMAC.setText("MAC Address: " + printerMacAddress);
        etLabelToPrint.setText(AppCommon.LinkNameToPrint);

        // Save printer info
        SharedPreferences sharedPref = LabelPrintingActivity.this.getSharedPreferences("PrinterInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("PrinterName", printerName);
        editor.putString("PrinterMacAddress", printerMacAddress);
        editor.commit();
        //=======================================================

        // Check printer is in paired bluetooth devices
        if (!CheckIfPresentInPairedDeviceList(printerMacAddress)) {
            showMessageDialog(LabelPrintingActivity.this, getResources().getString(R.string.PrinterNotInPairList));
        }
        //=======================================================

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        btnPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textToPrint = etLabelToPrint.getText().toString();
                if (textToPrint.trim().isEmpty()) {
                    Toast.makeText(LabelPrintingActivity.this, "Please enter any label to preview", Toast.LENGTH_LONG).show();
                } else {
                    AppCommon.WriteInFile(LabelPrintingActivity.this, TAG + "Entered Label: " + textToPrint);
                    AppCommon.WriteInFile(LabelPrintingActivity.this, TAG + "PREVIEW button clicked.");
                    PrintPreview(textToPrint.trim());
                }
            }
        });

        btnPrintLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CheckIfPresentInPairedDeviceList(printerMacAddress)) {
                    String textToPrint = etLabelToPrint.getText().toString();
                    if (textToPrint.trim().isEmpty()) {
                        Toast.makeText(LabelPrintingActivity.this, "Please enter any label to print", Toast.LENGTH_LONG).show();
                    } else {
                        AppCommon.WriteInFile(LabelPrintingActivity.this, TAG + "Entered Label: " + textToPrint);
                        AppCommon.WriteInFile(LabelPrintingActivity.this, TAG + "PRINT button clicked.");

                        PrintLabels1(textToPrint.trim());
                    }
                } else {
                    showMessageDialog(LabelPrintingActivity.this, getResources().getString(R.string.PrinterNotInPairList));
                }
            }
        });

        btn_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(LabelPrintingActivity.this, LaunchingActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);

            }
        });

    }

    public boolean CheckIfPresentInPairedDeviceList(String printerMacAddress){

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // Get paired devices.
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                if (deviceHardwareAddress.equalsIgnoreCase(printerMacAddress)){
                    device.createBond();
                    return true;
                }
            }

        }
        AppCommon.WriteInFile(LabelPrintingActivity.this, TAG + "Selected device is not in bluetooth pair devices list. (Device Name: " + printerName + "; Device Mac Address: " + printerMacAddress + ")");
        return false;
    }

    public void showMessageDialog(final Activity context, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        alertDialogBuilder
                .setMessage(message)
                .setCancelable(false)
                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                        Intent btSettings = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                        startActivity(btSettings);
                    }
                });
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
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

    public void PrintPreview(String textToPrint) {
        try {
            ImageView ivPreview = (ImageView) findViewById(R.id.iv_Preview);

            Bitmap img = textToBitmap(textToPrint, 80, Color.BLACK);
            img = getResizedBitmap(img, img.getWidth() / 3, img.getHeight());

            ivPreview.setImageBitmap(img);

        } catch (Exception e) {
            Log.i(TAG, "Exception in PrintPreview: " + e.getMessage());
            AppCommon.WriteInFile(LabelPrintingActivity.this, TAG + "Exception in PrintPreview: " + e.getMessage());
        }
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
            AppCommon.WriteInFile(LabelPrintingActivity.this, TAG + "Exception in textToBitmap: " + e.getMessage());
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
    public void PrintLabels1(String textToPrint) {

        try {
            //int selectedSize = rdSizeGroup.getCheckedRadioButtonId();
            //rdSelectedSize = (RadioButton) findViewById(selectedSize);

            String selectedPaperSize = "W12";
            /*String selectedPaperSizeRD = "12";
            if (rdSelectedSize != null) {
                selectedPaperSizeRD = rdSelectedSize.getText().toString().replace("mm", "").trim();
            }
            AppCommon.WriteInFile(LabelPrintingActivity.this, TAG + "Selected Paper Size: " + selectedPaperSizeRD + "mm");
            switch (selectedPaperSizeRD) {
                case "3.5":
                    selectedPaperSize = "W3_5";
                    break;
                case "6":
                    selectedPaperSize = "W6";
                    break;
                case "9":
                    selectedPaperSize = "W9";
                    break;
                case "12":
                    selectedPaperSize = "W12";
                    break;
                default:
                    break;
            }*/

            myPrinter = new Printer();
            myPrinter.setBluetooth(BluetoothAdapter.getDefaultAdapter());

            printerInfo = myPrinter.getPrinterInfo();
            printerInfo.printerModel = PrinterInfo.Model.PT_P300BT;
            printerInfo.port = PrinterInfo.Port.BLUETOOTH;
            printerInfo.paperSize = PrinterInfo.PaperSize.CUSTOM;
            printerInfo.orientation = PrinterInfo.Orientation.LANDSCAPE;
            printerInfo.align = PrinterInfo.Align.LEFT;
            printerInfo.printMode = PrinterInfo.PrintMode.FIT_TO_PAGE;
            printerInfo.numberOfCopies = 1;
            printerInfo.printQuality = PrinterInfo.PrintQuality.HIGH_RESOLUTION;
            printerInfo.macAddress = printerMacAddress;
            printerInfo.workPath = getApplicationContext().getCacheDir().getPath(); //String.valueOf(getApplicationContext().getExternalFilesDir("PrintMaterial"));
            //printerInfo.trimTapeAfterData = true;
            printerInfo.margin.left = 0;
            printerInfo.margin.top = 0;

            printerInfo.labelNameIndex = LabelInfo.PT3.valueOf(selectedPaperSize).ordinal();
            printerInfo.labelMargin = 0;
            printerInfo.isAutoCut = false;
            printerInfo.isCutAtEnd = true;
            printerInfo.isHalfCut = false;
            printerInfo.isSpecialTape = false;
            printerInfo.isCutMark = true;

            myPrinter.setPrinterInfo(printerInfo);

            ImageToPrint = textToBitmap(textToPrint, 90, Color.BLACK);
            //bitmapToFile(LabelPrintingActivity.this, ImageToPrint, "myLabel1.png");
            ImageToPrint = getResizedBitmap(ImageToPrint, ImageToPrint.getWidth() / 3, ImageToPrint.getHeight());
            //bitmapToFile(LabelPrintingActivity.this, ImageToPrint, "myLabel1_new.png");

            print2();
        } catch (Exception e) {
            e.printStackTrace();
            AppCommon.WriteInFile(LabelPrintingActivity.this, TAG + "Exception in PrintLabels1: " + e.getMessage());
        }
    }

    public void print2() {
        //PrinterThread printThread = new PrinterThread();
        //printThread.start();
        try {
            printResult = new PrinterStatus();
            myPrinter.startCommunication();
            AppCommon.WriteInFile(LabelPrintingActivity.this, TAG + "Communication Started-- (Printer Status ==> " + printResult.errorCode + ")");
            printResult = myPrinter.printImage(ImageToPrint);

            /*File file = bitmapToFile(LabelPrintingActivity.this, ImageToPrint, "myLabel.png");
            printResult = myPrinter.printFile(file.toString());*/

            if (printResult.errorCode == PrinterInfo.ErrorCode.ERROR_NONE) {
                // print success
                Toast.makeText(LabelPrintingActivity.this, "Success.!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(LabelPrintingActivity.this, printResult.errorCode.toString(), Toast.LENGTH_LONG).show();
            }

            AppCommon.WriteInFile(LabelPrintingActivity.this, TAG + "printResult: " + printResult.errorCode);
            myPrinter.endCommunication();
            AppCommon.WriteInFile(LabelPrintingActivity.this, TAG + "Communication End---");
        } catch (Exception e) {
            AppCommon.WriteInFile(LabelPrintingActivity.this, TAG + "Exception in print2: " + e.getMessage());
        }
        AppCommon.WriteInFile(LabelPrintingActivity.this, TAG + "======================================================");
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

    //endregion
}