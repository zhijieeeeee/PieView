package com.don.pieview;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {
    private String[] item = {
            "横线数据类型饼状统计图",
            "折线数据类型饼状统计图",
            "不带动画的百分比数据类型饼状统计图",
            "带动画的百分比数据类型饼状统计图"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView lv = (ListView) findViewById(R.id.lv);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, item);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        startActivity(new Intent(MainActivity.this, HorLinePieViewActivity.class));
                        break;
                    case 1:
                        startActivity(new Intent(MainActivity.this, LinePieViewActivity.class));
                        break;
                    case 2:
                        startActivity(new Intent(MainActivity.this, PercentPieViewActivity.class));
                        break;
                    case 3:
                        startActivity(new Intent(MainActivity.this, AnimationPercentPieViewActivity.class));
                        break;
                }
            }
        });
    }
}
