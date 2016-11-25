package com.don.pieview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.don.pieviewlibrary.PieView;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int[] data = new int[]{10, 10, 10, 40};
        String[] name = new String[]{"兄弟", "姐妹", "情侣", "基友"};
        PieView pieView = (PieView) findViewById(R.id.pieView);
        pieView.setData(data, name);
    }
}
