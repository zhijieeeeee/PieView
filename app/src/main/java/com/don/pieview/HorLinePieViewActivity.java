package com.don.pieview;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.don.pieviewlibrary.HorLinePieView;
import com.don.pieviewlibrary.LinePieView;

/**
 * <p>
 * Description：
 * </p>
 * <p>
 * date 2017/5/17
 */

public class HorLinePieViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hor_line_pieview);

        int[] data = new int[]{15, 85};
        int[] color = new int[]{
                getResources().getColor(R.color.not_pay),
                getResources().getColor(R.color.bug_success),};
        HorLinePieView linePieView = (HorLinePieView) findViewById(R.id.linePieView);
        linePieView.setData(data, color);

    }
}
