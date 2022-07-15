package com.example.btlinktestingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.usb.UsbManager;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
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

public class LabelPrintingActivity extends AppCompatActivity {

    //public PTPrintSettings ptPrintSettings;
    //public Gson gson;
    //private SharedPreferences sharedPreferences;

    /*private PrinterModel currentModel() {
        String modelString = sharedPreferences.getString("printerModel", "");
        return PrinterModel.valueOf(modelString);
    }*/
    private static final String TAG = "LabelPrintingActivity ";
    private String printerName = "", printerMacAddress = "";
    public static Printer myPrinter;
    public static Bitmap ImageToPrint;
    protected PrinterStatus printResult;
    protected PrinterInfo printerInfo;
    public RadioGroup rdSizeGroup;
    public RadioButton rdSelectedSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_label_printing);

        //sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        AppCommon.WriteInFile(LabelPrintingActivity.this, TAG + "Started-----");
        EditText etLabelToPrint = (EditText) findViewById(R.id.etLabelToPrint);
        Button btnPrintLabel = (Button) findViewById(R.id.btnPrintLabel);
        Button btnPrint2 = (Button) findViewById(R.id.btnPrint2);
        Button btnPreview = (Button) findViewById(R.id.btnPreview);
        TextView tvPrinterName = (TextView) findViewById(R.id.tvPrinterName);
        TextView tvPrinterMAC = (TextView) findViewById(R.id.tvPrinterMAC);
        rdSizeGroup = (RadioGroup) findViewById(R.id.rdSizeGroup);

        Intent intent = getIntent();
        printerName = intent.getStringExtra("DeviceName");
        printerMacAddress = intent.getStringExtra("DeviceMac");
        AppCommon.printerMacAddress = intent.getStringExtra("DeviceMac");
        AppCommon.WriteInFile(LabelPrintingActivity.this, TAG + "(printerName: " + printerName + "; printerMacAddress: " + printerMacAddress + ")");

        tvPrinterName.setText("Printer Name: " + printerName);
        tvPrinterMAC.setText("MAC Address: " + printerMacAddress);

        // Save printer info
        SharedPreferences sharedPref = LabelPrintingActivity.this.getSharedPreferences("PrinterInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("PrinterName", printerName);
        editor.putString("PrinterMacAddress", printerMacAddress);
        editor.commit();
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
                String textToPrint = etLabelToPrint.getText().toString();
                if (textToPrint.trim().isEmpty()) {
                    Toast.makeText(LabelPrintingActivity.this, "Please enter any label to print", Toast.LENGTH_LONG).show();
                } else {
                    AppCommon.WriteInFile(LabelPrintingActivity.this, TAG + "Entered Label: " + textToPrint);
                    AppCommon.WriteInFile(LabelPrintingActivity.this, TAG + "PRINT button clicked.");

                    PrintLabels1(textToPrint.trim());
                }
            }
        });

        btnPrint2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*String textToPrint = etLabelToPrint.getText().toString();
                if (textToPrint.trim().isEmpty()) {
                    Toast.makeText(LabelPrintingActivity.this, "Please enter any label to print", Toast.LENGTH_LONG).show();
                } else {
                    AppCommon.WriteInFile(LabelPrintingActivity.this, TAG + "Entered Label: " + textToPrint);
                    AppCommon.WriteInFile(LabelPrintingActivity.this, TAG + "PRINT 2 button clicked.");
                    PrintLabels2(textToPrint.trim());
                }*/
            }
        });

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
            ivPreview.setImageBitmap(textToBitmap(textToPrint, 80, Color.BLACK));

        } catch (Exception e) {
            Log.i(TAG, "Exception in PrintPreview: " + e.getMessage());
            AppCommon.WriteInFile(LabelPrintingActivity.this, TAG + "Exception in PrintPreview: " + e.getMessage());
        }
    }

    public Bitmap textToBitmap(String text, float textSize, int textColor) {
        Bitmap image = null;
        try {
            Paint paint = new Paint();
            paint.setTextSize(textSize);
            paint.setColor(Color.WHITE); // Color.parseColor("#FAF9F6")); //#FAF9F6
            paint.setTextAlign(Paint.Align.LEFT);
            float baseline = -paint.ascent();
            int width = (int) (paint.measureText(text) + 0.5f);
            int height = (int) (baseline + paint.ascent() + 0.5f);
            image = Bitmap.createBitmap(width + 20, height + 100, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(image);
            canvas.drawRect(0, 0, width + 20, height + 100, paint);
            paint.setColor(textColor);
            canvas.drawText(text, 0, baseline, paint);

        } catch (Exception e) {
            Log.i(TAG, "Exception in textToBitmap: " + e.getMessage());
            AppCommon.WriteInFile(LabelPrintingActivity.this, TAG + "Exception in textToBitmap: " + e.getMessage());
        }
        return image;
    }

    //region Method 1
    public void PrintLabels1(String textToPrint) {

        try {
            int selectedSize = rdSizeGroup.getCheckedRadioButtonId();
            rdSelectedSize = (RadioButton) findViewById(selectedSize);

            String selectedPaperSize = "W12";
            String selectedPaperSizeRD = "12";
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
            }

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
            printerInfo.trimTapeAfterData = true;
            printerInfo.margin.left = 0;
            printerInfo.margin.top = 0;

            printerInfo.labelNameIndex = LabelInfo.PT3.valueOf(selectedPaperSize).ordinal();
            printerInfo.labelMargin = 0;
            printerInfo.isAutoCut = false;
            printerInfo.isCutAtEnd = false;
            printerInfo.isHalfCut = false;
            printerInfo.isSpecialTape = false;
            printerInfo.isCutMark = true;

            myPrinter.setPrinterInfo(printerInfo);

            ImageToPrint = textToBitmap(textToPrint, 90, Color.BLACK);
            //bitmapToFile(LabelPrintingActivity.this, ImageToPrint, "myLabel1.png");

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

    /*protected class PrinterThread extends Thread {
        @Override
        public void run() {
            try {
                printResult = new PrinterStatus();
                myPrinter.startCommunication();
                AppCommon.WriteInFile(LabelPrintingActivity.this, TAG + "Communication Started---");
                printResult = myPrinter.printImage(ImageToPrint);

                //File file = bitmapToFile(LabelPrintingActivity.this, ImageToPrint, "myLabel.png");
                //printResult = myPrinter.printFile(file.toString());

                if (printResult.errorCode != PrinterInfo.ErrorCode.ERROR_NONE) {
                    // print success
                }
                AppCommon.WriteInFile(LabelPrintingActivity.this, TAG + "printResult: " + printResult.errorCode);
                myPrinter.endCommunication();
                AppCommon.WriteInFile(LabelPrintingActivity.this, TAG + "Communication End---");
            } catch (Exception e) {
                AppCommon.WriteInFile(LabelPrintingActivity.this, TAG + "Exception in PrinterThread: " + e.getMessage());
            }
        }
    }*/
    //endregion

    //region Method 2
    public void PrintLabels2(String textToPrint) {
        try {
            ImageToPrint = textToBitmap(textToPrint, 90, Color.BLACK);
            File file = bitmapToFile(LabelPrintingActivity.this, ImageToPrint, "myLabel.png");

            V4PrinterThread printThread = new V4PrinterThread(this, file);
            printThread.start();
        } catch (Exception e) {
            e.printStackTrace();
            AppCommon.WriteInFile(LabelPrintingActivity.this, TAG + "Exception in PrintLabels2: " + e.getMessage());
        }
    }

    public PrintSettings getPrintSettings(Context context) {
        PTPrintSettings ptPrintSettings = new PTPrintSettings(PrinterModel.PT_P300BT);
        File dir = context.getExternalFilesDir("PrintMaterial");
        ptPrintSettings.setLabelSize(PTPrintSettings.LabelSize.Width12mm);
        ptPrintSettings.setVAlignment(PrintImageSettings.VerticalAlignment.Center);
        ptPrintSettings.setHAlignment(PrintImageSettings.HorizontalAlignment.Center);
        ptPrintSettings.setWorkPath(dir.toString());
        ptPrintSettings.setAutoCut(true);
        return ptPrintSettings;
    }

    private class V4PrinterThread extends Thread {
        final Context context;
        final File fileToPrint;

        private V4PrinterThread(Context context, File file) {
            this.context = context;
            this.fileToPrint = file;
        }

        @Override
        public void run() {

            try {

                Channel channel = Channel.newBluetoothChannel(printerMacAddress, BluetoothAdapter.getDefaultAdapter());

                // Create a `PrinterDriver` instance
                PrinterDriverGenerateResult result = PrinterDriverGenerator.openChannel(channel);
                if (result.getError().getCode() != OpenChannelError.ErrorCode.NoError) {
                    AppCommon.WriteInFile(LabelPrintingActivity.this, TAG + "ErrorCode in V4PrinterThread: " + result.getError().getCode().toString());
                    return;
                }

                PrinterDriver printerDriver = result.getDriver();

                // Initialize `PrintSettings`
                PrintSettings printSettings = getPrintSettings(context);

                PrintError printError = printerDriver.printImage(String.valueOf(fileToPrint), printSettings);

                AppCommon.WriteInFile(LabelPrintingActivity.this, TAG + "Result - Print Image: " + printError.getCode());
                if (printError.getCode() != PrintError.ErrorCode.NoError) {
                    printerDriver.closeChannel();
                    AppCommon.WriteInFile(LabelPrintingActivity.this, TAG + "Error - Print Image: " + printError.getCode());
                    return;
                }

                printerDriver.closeChannel();

            } catch (Exception e) {
                e.printStackTrace();
                AppCommon.WriteInFile(LabelPrintingActivity.this, TAG + "Exception in V4PrinterThread: " + e.getMessage());
            }
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

    //endregion
}