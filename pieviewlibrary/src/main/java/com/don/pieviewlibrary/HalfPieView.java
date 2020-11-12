package com.don.pieviewlibrary;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * <p>
 * Description：横线数据类型饼状统计图
 * </p>
 *
 * @author tangzhijie
 */
public class HalfPieView extends View {

    private int centerX;
    private int centerY;
    private float radius;
    private RectF rectF;
    private Paint mArcPaint;
    private int[] numbers;
    private int sum;
    private int[] colors;

    public static final int DEFAULT_WIDTH = 300;
    public static final int DEFAULT_HEIGHT = 180;


    private Canvas thumbCanvas;
    private Bitmap emptyBitmap;
    private Bitmap thumbBitmap;
    private int thumbHeight, thumbWidth;
    private Paint thumbPaint;

    public HalfPieView(Context context) {
        super(context);
        init();
    }

    public HalfPieView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HalfPieView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 初始化
     */
    private void init() {
        mArcPaint = new Paint();
        mArcPaint.setStrokeWidth(20);
        mArcPaint.setAntiAlias(true);
        mArcPaint.setStyle(Paint.Style.STROKE);

        thumbPaint = new Paint();
        thumbPaint.setAntiAlias(true);
        thumbBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.point);
        emptyBitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        thumbCanvas = new Canvas(emptyBitmap);
        thumbHeight = thumbBitmap.getHeight();
        thumbWidth = thumbBitmap.getWidth();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measureWidthSize = MeasureSpec.getSize(widthMeasureSpec);
        int measureHeightSize = MeasureSpec.getSize(heightMeasureSpec);
        int measureWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        int measureHeightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (measureWidthMode == MeasureSpec.AT_MOST
                && measureHeightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        } else if (measureWidthMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(DEFAULT_WIDTH, measureHeightSize);
        } else if (measureHeightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(measureWidthSize, DEFAULT_HEIGHT);
        }

        init();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = getMeasuredWidth() / 2;
        centerY = getMeasuredHeight() - 40;
        //设置半径为宽高最小值的1/4
        radius = Math.min(getMeasuredWidth(), getMeasuredHeight()) / 2;
        //设置扇形外接矩形
        rectF = new RectF(centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        calculateAndDraw(canvas);
    }

    /**
     * 计算比例并且绘制扇形和数据
     */
    private void calculateAndDraw(Canvas canvas) {
        if (numbers == null || numbers.length == 0) {
            return;
        }
        //扇形开始度数
        int startAngle = 180;
        //所占百分比
        float percent;
        //所占度数
        float angle;
        for (int i = 0; i < numbers.length; i++) {
            percent = numbers[i] / (float) sum;
            angle = (float) Math.ceil(percent * 180);
            //绘制第i段扇形
            drawArc(canvas, startAngle, angle, colors[i]);
            startAngle += angle;

            if (i == 0) {//根据第一个弧线的度数绘制指针
                thumbCanvas.save();
                thumbCanvas.rotate(angle, centerX, centerY);
                thumbCanvas.drawBitmap(thumbBitmap, centerX - thumbWidth, centerY - 10, thumbPaint);
                thumbCanvas.restore();
                canvas.drawBitmap(emptyBitmap, 0, 0, null);
            }
        }


    }

    /**
     * 绘制扇形
     *
     * @param canvas     画布
     * @param startAngle 开始度数
     * @param angle      扇形的度数
     * @param color      颜色
     */
    private void drawArc(Canvas canvas, float startAngle, float angle, int color) {
        mArcPaint.setColor(color);
        //+0.5是为了让每个扇形之间没有间隙
        if (angle != 0) {
            angle += 0.5f;
        }
        canvas.drawArc(rectF, startAngle, angle, false, mArcPaint);
    }

    /**
     * 设置数据(自定义颜色)
     *
     * @param numbers 数字数组
     * @param colors  颜色数组
     */
    public void setData(int[] numbers, int[] colors) {
        if (numbers == null || numbers.length == 0
                || colors == null || colors.length == 0) {
            return;
        }
        if (numbers.length != colors.length) {
            return;
        }
        this.numbers = numbers;
        this.colors = colors;
        sum = 0;
        for (int i = 0; i < this.numbers.length; i++) {
            //计算总和
            sum += numbers[i];
        }
        invalidate();
    }
}
