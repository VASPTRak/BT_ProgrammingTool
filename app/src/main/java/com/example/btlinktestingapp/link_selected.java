package com.example.btlinktestingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.HashMap;

public class link_selected extends AppCompatActivity {

    EditText etEnterQty;
    ArrayList<HashMap<String, String>> TestsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link_selected);

        etEnterQty = (EditText) findViewById(R.id.edt_enter_quantity);
        String[] Tests = getResources().getStringArray(R.array.Tests);


        Intent intent = getIntent();
        String LinkType = intent.getStringExtra("LinkType");
        //etEnterQty.setText(LinkType);

        for (int i=0;i<Tests.length;i++){
            HashMap<String, String> map = new HashMap<>();
            map.put("item", Tests[i]);
            TestsList.add(map);

        }


    }
}