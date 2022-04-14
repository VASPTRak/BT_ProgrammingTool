package com.example.btlinktestingapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {
    private static final String DEFAULT_DIR_UUID = "UUID";
    EditText inputText;
    TextView response;
    Button saveButton,readButton;

    private String filename = "encrypt.txt";
    private String filepath = "IMEI_UUID";
    File appExternalFile;
    String myData = "";
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int CREATE_FILE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputText = (EditText) findViewById(R.id.myInputText);
        response = (TextView) findViewById(R.id.response);


        saveButton =
                (Button) findViewById(R.id.saveExternalStorage);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {

                File file = new File(Environment.DIRECTORY_DOCUMENTS);
                createFile(Uri.fromFile(file));
               // createExternalFile(DEFAULT_DIR_UUID,filename,MainActivity.this);
               // writeUUIDToFile();

                inputText.setText("");
                response.setText("SampleFile.txt saved to External Storage...");
            }
        });

        readButton = (Button) findViewById(R.id.getExternalStorage);
        readButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uuid_sting = ReadUUIDFromFile();
                inputText.setText(uuid_sting);
                response.setText("SampleFile.txt data retrieved from External Storage...");
            }
        });




    }
    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    public void writeUUIDToFile(){

        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Toast.makeText(getApplicationContext(),"Check External storage permission",Toast.LENGTH_LONG).show();
        }else {

            try {
                appExternalFile = new File(getExternalFilesDir(filepath), filename);
                if (!appExternalFile.exists()) {

                    if (appExternalFile.createNewFile()) {
                        Log.i(TAG,filename+" Created txt file");
                    } else {
                        Log.i(TAG,"Fail to create "+filename+" txt file");
                    }

                }

                //Write UUID to file.
                FileOutputStream fos = new FileOutputStream(appExternalFile);
                fos.write(inputText.getText().toString().getBytes());
                fos.close();


            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    public String ReadUUIDFromFile(){

        String uuid_str = "";
        try {
            FileInputStream fis = new FileInputStream(appExternalFile);
            DataInputStream in = new DataInputStream(fis);
            BufferedReader br =
                    new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                uuid_str = uuid_str + strLine;
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return uuid_str;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public File createExternalFile(String dir_name, String file_name, Context context) {
        String dir_path;
        String file_path;
        File dir ;
        File file;
        if (!isExternalStorageWritable(context)) {
            Log.e(TAG,"!!! external storage not writable");
            return null;
        }
        if (dir_name == null) {
            dir_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + File.separator + DEFAULT_DIR_UUID;
        } else {
            dir_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + File.separator + dir_name;
        }
        Log.d(TAG,"... going to access an external dir:" + dir_path);
        dir = new File(dir_path);
        if (!dir.exists()) {
            Log.d(TAG,"... going to mkdirs:" + dir_path);
            if (!dir.mkdirs()) {
                Log.e(TAG,"!!! failed to mkdirs");
                return null;
            }
        }
        if (file_name == null) {
            file_path = dir_path + File.separator + generateFileNameBasedOnTimeStamp();
        } else {
            file_path = dir_path + File.separator + file_name;
        }
        Log.d(TAG,"... going to return an external dir:" + file_path);
        file = new File(file_path);
        if (file.exists()) {
            Log.d(TAG,"... before creating to delete an external dir:" + file.getAbsolutePath());
            if (!file.delete()) {
                Log.e(TAG,"!!! failed to delete file");
                return null;
            }
        }

        return file;
    }

    private boolean isExternalStorageWritable(Context context) {
    /*
    String state = Environment.getExternalStorageState();
    return Environment.MEDIA_MOUNTED.equals(state);
    */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                Log.e(TAG,"!!! checkSelfPermission() not granted");
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }

    private boolean isExternalStorageReadable(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                Log.e(TAG,"!!! checkSelfPermission() not granted");
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }

    @SuppressLint("SimpleDateFormat")
    private String generateFileNameBasedOnTimeStamp() {
        return new SimpleDateFormat("yyyyMMdd_hhmmss").format(new Date()) + ".txt";
    }

    private void createFile(Uri pickerInitialUri) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/txt");
        intent.putExtra(Intent.EXTRA_TITLE, "invoice1.txt");

        // Optionally, specify a URI for the directory that should be opened in
        // the system file picker when your app creates the document.
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);

       startActivityForResult(intent, CREATE_FILE);
    }


    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);

        if (requestCode == CREATE_FILE){
            generateFileNameBasedOnTimeStamp();
        }

    }
}
