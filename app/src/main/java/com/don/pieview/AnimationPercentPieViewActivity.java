package com.don.pieview;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.don.pieviewlibrary.AnimationPercentPieView;
import com.don.pieviewlibrary.PercentPieView;

/**
 * <p>
 * Description：
 * </p>
 *
 * @author tangzhijie
 */
public class AnimationPercentPieViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animation_percent_pieview);

        int[] data = new int[]{10, 10, 10, 40};
        String[] name = new String[]{"兄弟", "姐妹", "情侣", "基友"};
        AnimationPercentPieView animationPieView = (AnimationPercentPieView)
                findViewById(R.id.animationPieView);
        animationPieView.setData(data, name);

        int[] data2 = new int[]{10, 10, 20, 40, 10, 10, 20};
        String[] name2 = new String[]{"猫", "狗", "奶牛", "羊驼", "大象", "狮子", "老虎"};
        AnimationPercentPieView animationPieView2 = (AnimationPercentPieView)
                findViewById(R.id.animationPieView2);
        animationPieView2.setData(data2, name2);
    }
}
