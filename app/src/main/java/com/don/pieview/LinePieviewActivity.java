package com.don.pieview;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.don.pieviewlibrary.LinePieView;

/**
 * <p>
 * Description：
 * </p>
 *
 * @author tangzhijie
 */
public class LinePieViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_pieview);

        int[] data = new int[]{10, 10, 10, 40};
        String[] name = new String[]{"兄弟", "姐妹", "情侣", "基友"};
        LinePieView linePieView = (LinePieView) findViewById(R.id.linePieView);
        linePieView.setData(data, name);

        int[] data2 = new int[]{10, 10, 20, 40, 10, 10, 20};
        String[] name2 = new String[]{"猫", "狗", "奶牛", "羊驼", "大象", "狮子", "老虎"};
        LinePieView linePieView2 = (LinePieView) findViewById(R.id.linePieView2);
        linePieView2.setData(data2, name2);
    }
}
