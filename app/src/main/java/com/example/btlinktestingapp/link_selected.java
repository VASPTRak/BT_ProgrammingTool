package com.example.btlinktestingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class link_selected extends AppCompatActivity {
    TextView tvselectTest;
    EditText etEnterQty;
    ArrayList<HashMap<String, String>> TestsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link_selected);

        etEnterQty = (EditText) findViewById(R.id.edt_enter_quantity);
        String[] Tests = getResources().getStringArray(R.array.Tests);
        tvselectTest = (TextView) findViewById(R.id.tvSelectTest);
        ListView lvTestNames = (ListView) findViewById(R.id.lvlinknames);




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
        ListView lvTestNames = (ListView) dialog.findViewById(R.id.lvlinknames);
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


        SimpleAdapter adapter = new SimpleAdapter(this,TestsList , R.layout.item_link, new String[]{"item"}, new int[]{R.id.tvSingleItem});
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
                AppCommon.FSBT_linkQtyToTest = Integer.parseInt(etEnterQty.getText().toString().trim());
                String inputValue = etEnterQty.getText().toString().trim();

                if (inputValue.isEmpty()){
                    Toast.makeText(link_selected.this, "Please enter quantity", Toast.LENGTH_SHORT).show();
                }

                else{
                    AppCommon.IsPrint = false;
                    Intent intent = new Intent(link_selected.this, ScanDeviceActivity.class);
                    startActivity(intent);

                    }

                dialog.dismiss();
            }
        });

        dialog.show();
    }
}