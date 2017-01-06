package com.don.pieview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.don.pieviewlibrary.PieView;
import com.don.pieviewlibrary.PieView2;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int[] data = new int[]{10, 10, 10, 40};
        String[] name = new String[]{"兄弟", "姐妹", "情侣", "基友"};
        PieView pieView = (PieView) findViewById(R.id.pieView);
        pieView.setData(data, name);

        int[] data2 = new int[]{10, 10, 20, 40,10, 10, 20};
        String[] name2 = new String[]{"猫", "狗", "奶牛", "羊驼","大象", "狮子", "老虎"};
        PieView2 pieView2 = (PieView2) findViewById(R.id.pieView2);
        pieView2.setData(data2, name2);
    }
}
