package com.example.btlinktestingapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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

import androidx.core.content.res.ResourcesCompat;
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


    public Bitmap textToBitmap(String text, float textSize, int textColor) {
        Bitmap image = null;
        try {
            text = text + "  ";// + text + " : " + text;

            Paint paint = new Paint();
            paint.setTextSize(50);
            paint.setColor(Color.WHITE); // Color.parseColor("#FAF9F6")); //#FAF9F6
            paint.setTextAlign(Paint.Align.LEFT);
            //paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            paint.setTypeface(ResourcesCompat.getFont(mContext, R.font.kailasa2));
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
            AppCommon.WriteInFile(mContext, TAG + "Exception in textToBitmap: " + e.getMessage());
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
    public void PrintLabels(String textToPrint) {

        try {


            //  String selectedPaperSize = "W12";


//            myPrinter = new Printer();
//            myPrinter.setBluetooth(BluetoothAdapter.getDefaultAdapter());
//
//            printerInfo = myPrinter.getPrinterInfo();
//            printerInfo.printerModel = PrinterInfo.Model.PT_P300BT;
//            printerInfo.port = PrinterInfo.Port.BLUETOOTH;
//            printerInfo.paperSize = PrinterInfo.PaperSize.CUSTOM;
//            printerInfo.orientation = PrinterInfo.Orientation.LANDSCAPE;
//            printerInfo.align = PrinterInfo.Align.LEFT;
//            printerInfo.printMode = PrinterInfo.PrintMode.FIT_TO_PAGE;
//            printerInfo.numberOfCopies = 1;
//            printerInfo.printQuality = PrinterInfo.PrintQuality.HIGH_RESOLUTION;
//            printerInfo.macAddress = printerMacAddress;
//            printerInfo.workPath = getApplicationContext().getCacheDir().getPath(); //String.valueOf(getApplicationContext().getExternalFilesDir("PrintMaterial"));
//            //printerInfo.trimTapeAfterData = true;
//            printerInfo.margin.left = 0;
//            printerInfo.margin.top = 0;
//
//            printerInfo.labelNameIndex = LabelInfo.PT3.valueOf(selectedPaperSize).ordinal();
//            printerInfo.labelMargin = 0;
//            printerInfo.isAutoCut = false;
//            printerInfo.isCutAtEnd = true;
//            printerInfo.isHalfCut = false;
//            printerInfo.isSpecialTape = false;
//            printerInfo.isCutMark = true;
//
//            myPrinter.setPrinterInfo(printerInfo);

            ImageToPrint = textToBitmap(textToPrint, 50, Color.BLACK);
            //bitmapToFile(LabelPrintingActivity.this, ImageToPrint, "myLabel1.png");
            ImageToPrint = getResizedBitmap(ImageToPrint, ImageToPrint.getWidth() / 2, ImageToPrint.getHeight());
            bitmapToFile(mContext, ImageToPrint, textToPrint +".png");

            // print2();
        } catch (Exception e) {
            e.printStackTrace();
            AppCommon.WriteInFile(mContext, TAG + "Exception in PrintLabels: " + e.getMessage());
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
//                PrintLabels(ListOfHistory.get(position).get("LinkNameFromAPP"));
//                Toast.makeText(mContext, "Image saved", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(mContext, ScanDeviceActivity.class);
                mContext.startActivity(i);

                AppCommon.IsPrint = true;
                String LinkNameFromAPP = ListOfHistory.get(position).get("LinkNameFromAPP");
                AppCommon.LinkNameToPrint = LinkNameFromAPP;
                PrintLabels(LinkNameFromAPP);
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
        Button btn_print3;

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





//            btn_print3.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    PrintLabels("LinkNameFromAPP"+".png");
//                    Toast.makeText(mContext, "Image saved", Toast.LENGTH_SHORT).show();
//                }
//            });
        }
    }

}



