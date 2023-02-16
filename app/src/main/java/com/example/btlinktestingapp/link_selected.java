package com.example.btlinktestingapp;

import static com.example.btlinktestingapp.HistoryActivity.TAG;
import static com.example.btlinktestingapp.LaunchingActivity.webIP;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class link_selected extends AppCompatActivity {
    TextView tvselectTest;
    EditText etEnterQty,etEnterPulses;
    ArrayList<HashMap<String, String>> TestsList = new ArrayList<>();
    ArrayList<HashMap<String,String>> ListOfTestCases = new ArrayList<>();
    LinearLayout linearLayout;
    Button btnGo;
    String caseId, inputPulses;
    int pos;
    ListView lvTestNames;

    public static String API = webIP + "/api/External/getuniquehardwaretestlinkname";
    public static String API_TEST_CASES = webIP + "/api/External/getlinkhardwaretestcases";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link_selected);

        etEnterQty = (EditText) findViewById(R.id.edt_enter_quantity);
        String[] Tests = getResources().getStringArray(R.array.Tests);
        tvselectTest = (TextView) findViewById(R.id.tvSelectTest);
        lvTestNames = (ListView) findViewById(R.id.lvlinknames);
       // etEnterPulses=(EditText) findViewById(R.id.edit_enter_pulses);
        linearLayout =(LinearLayout) findViewById(R.id.linearLayout);
        //btnGo = (Button) findViewById(R.id.btnGO);
        new getTestCasesDetails().execute();




        if(AppCommon.isbtnContinuePressed.equalsIgnoreCase("true")){
            etEnterQty.setVisibility(View.GONE);
            etEnterQty.setEnabled(false);
            tvselectTest.setVisibility(View.VISIBLE);
            linearLayout.setVisibility(View.VISIBLE);
            //btnGo.setVisibility(View.VISIBLE);
            //onGoButtonClick();

        }


        Intent intent = getIntent();
        String LinkType = intent.getStringExtra("LinkType");
        //etEnterQty.setText(LinkType);



        for (int i=0; i<Tests.length; i++){
            HashMap<String, String> map = new HashMap<>();
            map.put("item", Tests[i]);
            TestsList.add(map);

        }
        if (tvselectTest.isPressed()){
            alertSelectLinkList();
        }
        else
        {
            System.out.println("No links available");
        }
    }

    public void selectTestAction(View v) {

        alertSelectLinkList();

    }



    public void alertSelectLinkList() {
        final Dialog dialog = new Dialog(link_selected.this);
        dialog.setTitle("BT Links");
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.link_list);

        TextView tvNoLinks = (TextView) dialog.findViewById(R.id.tvnolinks);
         lvTestNames = (ListView) dialog.findViewById(R.id.lvlinknames);
        Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);

        ArrayList<Integer> values = new ArrayList<Integer>();
        for (int i = 0; i < 11; i++)
        {
            values.add(i);
        }


        if (tvselectTest.isPressed() ) {
            lvTestNames.setVisibility(View.VISIBLE);


        } else {
            lvTestNames.setVisibility(View.GONE);
            tvNoLinks.setVisibility(View.VISIBLE);
        }


        SimpleAdapter adapter = new SimpleAdapter(this,ListOfTestCases , R.layout.item_link, new String[]{"LINKHardwareTestCaseName"}, new int[]{R.id.tvSingleItem});
        lvTestNames.setAdapter(adapter);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        lvTestNames.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                String inputValue = etEnterQty.getText().toString().trim();
                int selectedItemPos = position;
                pos = selectedItemPos;
                AppCommon.TestCaseId = ListOfTestCases.get(selectedItemPos).get("LINKHardwareTestCaseId");
                //AppCommon.TestCaseId = AppCommon.TestCaseId + caseId;



                if (inputValue.isEmpty()  && etEnterQty.isEnabled()){
                    Toast.makeText(link_selected.this, "Please enter quantity", Toast.LENGTH_SHORT).show();
                    etEnterQty.setError("required");
                }


                else{
                    if(etEnterQty.isEnabled()) {
                        AppCommon.FSBT_linkQtyToTest = Integer.parseInt(etEnterQty.getText().toString().trim());
                    }
//                    etEnterQty.setVisibility(View.GONE);
//                    linearLayout.setVisibility(View.GONE);
//                    tvselectTest.setVisibility(View.GONE);
                    //etEnterPulses.setVisibility(View.VISIBLE);
                    //btnGo.setVisibility(View.VISIBLE);

                    AppCommon.IsPrint = false;
                    SharedPreferences sharedPref = link_selected.this.getSharedPreferences("PulseValue", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    //                   editor.putString("Pulses", inputPulses);
                    HashMap<String, String> map = ListOfTestCases.get(pos);
                    inputPulses = map.get("Pulses");
                    editor.putString("Pulses", inputPulses);
                    editor.putString("TestCaseId",AppCommon.TestCaseId);
                    editor.commit();
                    Intent intent1 = new Intent(link_selected.this, ScanDeviceActivity.class);
                    startActivity(intent1);



                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }

//    public void onGoButtonClick(){
//        btnGo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//               if(lvTestNames.isPressed()) {
//                   SharedPreferences sharedPref = link_selected.this.getSharedPreferences("PulseValue", Context.MODE_PRIVATE);
//                   SharedPreferences.Editor editor = sharedPref.edit();
//                   //editor.putString("Pulses", inputPulses);
//                   HashMap<String, String> map = ListOfTestCases.get(pos);
//                   inputPulses = map.get("Pulses");
//                   editor.putString("Pulses", inputPulses);
//                   editor.putString("TestCaseId", AppCommon.TestCaseId);
//                   editor.commit();
//                   Intent intent1 = new Intent(link_selected.this, ScanDeviceActivity.class);
//                   startActivity(intent1);
//               }
//               else{
//                   Toast.makeText(link_selected.this, "Please select test", Toast.LENGTH_SHORT).show();
//               }
//                }
//
//        });
//    }

    public class getTestCasesDetails extends AsyncTask<String, Void, String> {

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(link_selected.this);
            pd.setMessage("Please wait...");
            //pd.show();
        }

        protected String doInBackground(String... param) {
            String resp = "";

            try {

                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url(link_selected.API_TEST_CASES)
                        .get()
                        .addHeader("Content-Type", "application/json")
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();
                System.out.println("Response from API: "+resp );

                //------------------------------

            } catch (Exception e) {
                e.printStackTrace();
                pd.cancel();
            }

            return resp;
        }


        @Override
        protected void onPostExecute(String result) {

            System.out.println("Response of API: " +result);

            if (result != null && !result.isEmpty()) {

                try {
                    pd.cancel();
                    JSONObject jsonObject = new JSONObject(result);

                    String ResponceMessage = jsonObject.getString("ResponseMessage");
                    String ResponseText = jsonObject.getString("ResponseText");

                    if (ResponceMessage.equalsIgnoreCase("success")) {
                        //ListOfHistoryData.clear();
                        JSONArray jsonArray = jsonObject.getJSONArray("LINKHardwareTestCaseObj");

                        for (int i=0; i< jsonArray.length(); i++){

                            JSONObject jobj = jsonArray.getJSONObject(i);
                            String TestcaseId = jobj.getString("LINKHardwareTestCaseId");
                            String TestcaseName = jobj.getString("LINKHardwareTestCaseName");
                            String Pulses = jobj.getString("Pulses");
//                            String UniqueLinkName = jobj.getString("UniqueLinkName");
//                            String MacAddress = jobj.getString("MacAddress");


                            HashMap<String, String> map = new HashMap<>();
                            map.put("LINKHardwareTestCaseId", TestcaseId);
                            map.put("LINKHardwareTestCaseName", TestcaseName);
                            map.put("Pulses", Pulses);

                            ListOfTestCases.add(map);

                        }

                        Log.i(TAG, "API Call Success" + result);

                    } else {
                        Log.i(TAG, "API Call fail" + result);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    pd.cancel();
                }

            } else {
                pd.cancel();
                Log.i(TAG, " InPost Response err:" + result);
            }
        }
    }

}