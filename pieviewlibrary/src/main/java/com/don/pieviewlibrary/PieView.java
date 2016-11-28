package com.don.pieviewlibrary;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

/**
 * <p>
 * Description：饼状统计图View
 * </p>
 *
 * @author tangzhijie
 */
public class PieView extends View {

    /**
     * 使用wrap_content时默认的尺寸
     */
    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 800;

    /**
     * 斜线长度
     */
    private static final int SlASH_LINE_OFFSET = 60;

    /**
     * 横线长度
     */
    private static final int HOR_LINE_LENGTH = 180;

    /**
     * 横线上文字的横向偏移量
     */
    private static final int X_OFFSET = 20;

    /**
     * 横线上文字的纵向偏移量
     */
    private static final int Y_OFFSET = 10;

    /**
     * 中心坐标
     */
    private int centerX;
    private int centerY;

    /**
     * 半径
     */
    private float radius;

    /**
     * 弧形外接矩形
     */
    private RectF rectF;

    /**
     * 中间文本的大小
     */
    private Rect centerTextBound = new Rect();

    /**
     * 数据文本的大小
     */
    private Rect dataTextBound = new Rect();

    /**
     * 扇形画笔
     */
    private Paint mArcPaint;

    /**
     * 中心文本画笔
     */
    private Paint textPaint;

    /**
     * 数据画笔
     */
    private Paint linePaint;

    /**
     * 数据源数字数组
     */
    private int[] numbers;

    /**
     * 数据源名称数组
     */
    private String[] names;

    /**
     * 数据源总和
     */
    private int sum;

    /**
     * 颜色数组
     */
    private int[] colors;

    private Random random = new Random();

    //自定义属性 Start

    /**
     * 中间字体大小
     */
    private float centerTextSize = 80;

    /**
     * 数据字体大小
     */
    private float dataTextSize = 40;

    /**
     * 中间字体颜色
     */
    private int centerTextColor = Color.BLACK;

    /**
     * 数据字体颜色
     */
    private int dataTextColor = Color.BLACK;

    /**
     * 圆圈的宽度
     */
    private float circleWidth = 50;

    //自定义属性 End

    public PieView(Context context) {
        super(context);
        init();
    }

    public PieView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PieView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PieView);
        centerTextSize = typedArray.getDimension(R.styleable.PieView_centerTextSize, centerTextSize);
        dataTextSize = typedArray.getDimension(R.styleable.PieView_dataTextSize, dataTextSize);
        circleWidth = typedArray.getDimension(R.styleable.PieView_circleWidth, circleWidth);
        centerTextColor = typedArray.getColor(R.styleable.PieView_centerTextColor, centerTextColor);
        dataTextColor = typedArray.getColor(R.styleable.PieView_dataTextColor, dataTextColor);
        typedArray.recycle();
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        mArcPaint = new Paint();
        mArcPaint.setStrokeWidth(circleWidth);
        mArcPaint.setAntiAlias(true);
        mArcPaint.setStyle(Paint.Style.STROKE);

        textPaint = new Paint();
        textPaint.setTextSize(centerTextSize);
        textPaint.setAntiAlias(true);
        textPaint.setColor(centerTextColor);

        linePaint = new Paint();
        linePaint.setStrokeWidth(2);
        linePaint.setTextSize(dataTextSize);
        linePaint.setAntiAlias(true);
        linePaint.setColor(dataTextColor);
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
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = getMeasuredWidth() / 2;
        centerY = getMeasuredHeight() / 2;
        //设置半径为宽高最小值的1/4
        radius = Math.min(getMeasuredWidth(), getMeasuredHeight()) / 2 * 0.5f;
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
        int startAngle = 0;
        for (int i = 0; i < numbers.length; i++) {
            //所占百分比
            float percent = numbers[i] / (float) sum;
            //所占度数
            float angle = (float) Math.ceil(percent * 360);
            //绘制第i段扇形
            drawArc(canvas, startAngle, angle, colors[i]);
            startAngle += angle;

            //绘制数据
            if (numbers[i] <= 0) {
                continue;
            }
            //当前扇形弧线相对于纵轴的中心点度数,由于扇形的绘制是从三点钟方向开始，所以加90
            float arcCenterDegree = 90 + startAngle - angle / 2;
            calculateLineAndData(canvas, arcCenterDegree, i);
        }
        //绘制中心数字总和
        canvas.drawText(sum + "", centerX - centerTextBound.width() / 2, centerY + centerTextBound.height() / 2, textPaint);
    }

    /**
     * 计算和绘制数据折线
     *
     * @param canvas 画布
     * @param degree 当前扇形中心度数
     * @param i      当前下标
     */
    private void calculateLineAndData(Canvas canvas, float degree, int i) {
        //扇形弧线中心点距离圆心的坐标
        float x;
        float y;
        //斜线开始坐标(扇形弧线中心点相对于view的坐标)
        float startX;
        float startY;
        //斜线结束坐标
        float endX = 0;
        float endY = 0;
        //横线结束坐标
        float horEndX = 0;
        float horEndY = 0;
        //数字开始坐标
        float numberStartX = 0;
        float numberStartY = 0;
        //文本开始坐标
        float textStartX = 0;
        float textStartY = 0;

        //sin 一二正，三四负
        x = (float) (Math.sin(Math.toRadians(degree)) * radius);
        //cos 一四正，二三负
        y = (float) (Math.cos(Math.toRadians(degree)) * radius);

        startX = centerX + x;
        startY = centerY - y;

        //根据角度判断象限，并且计算各个坐标点
        if (degree > 90 && degree < 180) {//二象限
            endX = startX + SlASH_LINE_OFFSET;
            endY = startY + SlASH_LINE_OFFSET;

            horEndX = endX + HOR_LINE_LENGTH;
            horEndY = endY;

            numberStartX = endX + X_OFFSET;
            numberStartY = endY - Y_OFFSET;

            textStartX = endX + X_OFFSET;
            textStartY = endY + dataTextBound.height() + Y_OFFSET / 2;
        } else if (degree == 180) {
            startX = centerX;
            startY = centerY + radius;
            endX = startX + SlASH_LINE_OFFSET;
            endY = startY + SlASH_LINE_OFFSET;


            horEndX = endX + HOR_LINE_LENGTH;
            horEndY = endY;

            numberStartX = endX + X_OFFSET;
            numberStartY = endY - Y_OFFSET;

            textStartX = endX + X_OFFSET;
            textStartY = endY + dataTextBound.height() + Y_OFFSET / 2;
        } else if (degree > 180 && degree < 270) {//三象限
            endX = startX - SlASH_LINE_OFFSET;
            endY = startY + SlASH_LINE_OFFSET;

            horEndX = endX - HOR_LINE_LENGTH;
            horEndY = endY;

            numberStartX = endX - HOR_LINE_LENGTH + X_OFFSET;
            numberStartY = endY - Y_OFFSET;

            textStartX = endX - HOR_LINE_LENGTH + X_OFFSET;
            textStartY = endY + dataTextBound.height() + Y_OFFSET / 2;
        } else if (degree == 270) {
            startX = centerX - radius;
            startY = centerY;
            endX = startX - SlASH_LINE_OFFSET;
            endY = startY - SlASH_LINE_OFFSET;

            horEndX = endX - HOR_LINE_LENGTH;
            horEndY = endY;

            numberStartX = endX - HOR_LINE_LENGTH + X_OFFSET;
            numberStartY = endY - Y_OFFSET;

            textStartX = endX - HOR_LINE_LENGTH + X_OFFSET;
            textStartY = endY + dataTextBound.height() + Y_OFFSET / 2;
        } else if (degree > 270 && degree < 360) {//四象限
            endX = startX - SlASH_LINE_OFFSET;
            endY = startY - SlASH_LINE_OFFSET;

            horEndX = endX - HOR_LINE_LENGTH;
            horEndY = endY;

            numberStartX = endX - HOR_LINE_LENGTH + X_OFFSET;
            numberStartY = endY - Y_OFFSET;

            textStartX = endX - HOR_LINE_LENGTH + X_OFFSET;
            textStartY = endY + dataTextBound.height() + Y_OFFSET / 2;
        } else if (degree == 360) {
            startX = centerX;
            startY = centerY - radius;
            endX = startX - SlASH_LINE_OFFSET;
            endY = startY - SlASH_LINE_OFFSET;

            horEndX = endX - HOR_LINE_LENGTH;
            horEndY = endY;

            numberStartX = endX - HOR_LINE_LENGTH + X_OFFSET;
            numberStartY = endY - Y_OFFSET;

            textStartX = endX - HOR_LINE_LENGTH + X_OFFSET;
            textStartY = endY + dataTextBound.height() + Y_OFFSET / 2;

        } else if (degree > 360) {//一象限
            endX = startX + SlASH_LINE_OFFSET;
            endY = startY - SlASH_LINE_OFFSET;

            horEndX = endX + HOR_LINE_LENGTH;
            horEndY = endY;

            numberStartX = endX + X_OFFSET;
            numberStartY = endY - Y_OFFSET;

            textStartX = endX + X_OFFSET;
            textStartY = endY + dataTextBound.height() + Y_OFFSET / 2;

        }
        //绘制线段和数据
        drawLineAndData(canvas, i, startX, startY, endX, endY,
                horEndX, horEndY, numberStartX, numberStartY, textStartX, textStartY);
    }

    /**
     * 绘制数据
     *
     * @param canvas       画布
     * @param i            下标
     * @param slashStartX  斜线开始X坐标
     * @param slashStartY  斜线开始Y坐标
     * @param slashEndX    斜线结束X坐标
     * @param slashEndY    斜线结束Y坐标
     * @param horEndX      横线结束X坐标
     * @param horEndY      横线结束Y坐标
     * @param numberStartX 数字开始X坐标
     * @param numberStartY 数字开始Y坐标
     * @param textStartX   文本开始X坐标
     * @param textStartY   文本开始Y坐标
     */
    private void drawLineAndData(Canvas canvas, int i, float slashStartX, float slashStartY,
                                 float slashEndX, float slashEndY,
                                 float horEndX, float horEndY,
                                 float numberStartX, float numberStartY,
                                 float textStartX, float textStartY) {
        //绘制折线
        canvas.drawLine(slashStartX, slashStartY, slashEndX, slashEndY, linePaint);
        //绘制横线
        canvas.drawLine(slashEndX, slashEndY, horEndX, horEndY, linePaint);
        //绘制数字
        canvas.drawText(numbers[i] + "", numberStartX, numberStartY, linePaint);
        //绘制文字
        canvas.drawText(names[i] + "", textStartX, textStartY, linePaint);
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
        //-0.5和+0.5是为了让每个扇形之间没有间隙
        canvas.drawArc(rectF, startAngle - 0.5f, angle + 0.5f, false, mArcPaint);
    }

    /**
     * 生成随机颜色
     */
    private int randomColor() {
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);
        return Color.rgb(red, green, blue);
    }

    /**
     * 设置数据
     *
     * @param numbers 数字数组
     * @param names   名称数组
     */
    public void setData(int[] numbers, String[] names) {
        if (numbers == null || numbers.length == 0 || names == null || names.length == 0) {
            return;
        }
        if (numbers.length != names.length) {
            //名称个数与数字个数不相等
            return;
        }

        this.numbers = numbers;
        this.names = names;
        colors = new int[numbers.length];
        for (int i = 0; i < this.numbers.length; i++) {
            sum += numbers[i];
            colors[i] = randomColor();
        }
        textPaint.getTextBounds(sum + "", 0, (sum + "").length(), centerTextBound);
        linePaint.getTextBounds(names[0] + "", 0, (names[0] + "").length(), dataTextBound);
        invalidate();
    }
}
